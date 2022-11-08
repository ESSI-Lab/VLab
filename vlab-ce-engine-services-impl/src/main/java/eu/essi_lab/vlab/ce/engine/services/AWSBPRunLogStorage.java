package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.ce.engine.services.aws.AWSLogEventTSAligner;
import eu.essi_lab.vlab.ce.engine.services.aws.BPLogToAWSLogMapper;
import eu.essi_lab.vlab.core.configuration.BPStaticConfigurationParameters;
import eu.essi_lab.vlab.core.configuration.ConfigurationParameter;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPLog;
import eu.essi_lab.vlab.core.datamodel.BPLogChunk;
import eu.essi_lab.vlab.core.datamodel.LogEvent;
import eu.essi_lab.vlab.core.datamodel.LogMessagesResponse;
import eu.essi_lab.vlab.core.engine.services.IBPRunLogStorage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogStreamRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsResponse;

/**
 * @author Mattia Santoro
 */
public class AWSBPRunLogStorage implements IBPRunLogStorage {

	private CloudWatchLogsClient client;
	private Logger logger = LogManager.getLogger(AWSBPRunLogStorage.class);

	private String modelLogGroup;

	private static final String NO_LOG_YET = "Model Execution has not produced any log yet.";
	private Map<String, String> writeNextSequenceToken = new HashMap<>();
	private Map<String, Long> lastWrittenTS = new HashMap<>();
	private String logPrefix;
	private String containerName;

	@Override
	public List<ConfigurationParameter> configurationParameters() {
		return Arrays.asList(BPStaticConfigurationParameters.AWS_LOG_ACCESS_KEY.getParameter(),
				BPStaticConfigurationParameters.AWS_LOG_SECRET_KEY.getParameter(),
				BPStaticConfigurationParameters.AWS_LOG_REGION.getParameter(),
				BPStaticConfigurationParameters.AWS_MODEL_EXECUTION_LOG_GROUP.getParameter(),
				BPStaticConfigurationParameters.AWS_MODEL_EXECUTION_LOG_PREFIX.getParameter(),
				BPStaticConfigurationParameters.AWS_ECS_TASK_CONTAINER_NAME.getParameter());

	}

	@Override
	public void configure(Map<ConfigurationParameter, String> parameters) throws BPException {

		String accessKey = parameters.get(BPStaticConfigurationParameters.AWS_LOG_ACCESS_KEY.getParameter());
		String secretKey = parameters.get(BPStaticConfigurationParameters.AWS_LOG_SECRET_KEY.getParameter());
		String region = parameters.get(BPStaticConfigurationParameters.AWS_LOG_REGION.getParameter());

		logPrefix = parameters.get(BPStaticConfigurationParameters.AWS_MODEL_EXECUTION_LOG_PREFIX.getParameter());
		containerName = parameters.get(BPStaticConfigurationParameters.AWS_ECS_TASK_CONTAINER_NAME.getParameter());

		var awsCreds = AwsBasicCredentials.create(accessKey, secretKey);

		var staticCredentials = StaticCredentialsProvider.create(awsCreds);

		setModelLogGroup(parameters.get(BPStaticConfigurationParameters.AWS_MODEL_EXECUTION_LOG_GROUP.getParameter()));

		setClient(CloudWatchLogsClient.builder().region(Region.of(region)).credentialsProvider(staticCredentials).build());

	}

	public String getLogStreamName(String taskid) {

		return logPrefix + "/" + containerName + "/" + taskid;
	}

	@Override
	public LogMessagesResponse readLog(String mti, Boolean head, String nextToken) {

		logger.debug("Requested log of model task id {}", mti);
		String modeltaskid = mti;

		if (mti.contains("/") && mti.split("/").length > 1) {

			logger.trace("Removing cluster name");

			modeltaskid = mti.split("/")[1];

		}

		String streamName = getLogStreamName(modeltaskid);

		logger.trace("Reading stream {}", streamName);

		GetLogEventsRequest request = GetLogEventsRequest.builder().startFromHead(head).logGroupName(modelLogGroup).logStreamName(
				streamName).nextToken((nextToken != null && !"".equalsIgnoreCase(nextToken)) ? nextToken : null).limit(100).build();

		var logMessagesResponse = new LogMessagesResponse();

		List<LogEvent> events = new ArrayList<>();

		GetLogEventsResponse response = doGetLogEvents(request);

		logger.trace("Response received for log events of stream {}", streamName);

		logMessagesResponse.setNextBackwardToken(response.nextBackwardToken());
		logMessagesResponse.setNextForwardToken(response.nextForwardToken());

		response.events().forEach(outputLogEvent -> {
			var event = new LogEvent();
			event.setMessage(outputLogEvent.message());
			event.setIngestionTime(outputLogEvent.ingestionTime());
			event.setTimestamp(outputLogEvent.timestamp());

			events.add(event);

		});

		logger.trace("Ended events copy from stream {}", streamName);

		if (events.isEmpty()) {
			logger.trace("No events found in stream {}", streamName);
			events.add(notProducedLogs());
		}

		logMessagesResponse.setEvents(events);
		return logMessagesResponse;
	}

	@Override
	public Integer writeLog(String runid, String modelTaskId, BPLogChunk bpLogChunk) {

		logger.debug("Write Log requested for run {} with model task id {}", runid, modelTaskId);

		List<InputLogEvent> awsLogEventList = bpLogChunk.getSet().stream().filter(
				bpLog -> bpLog.getMessage() != null && !("".equalsIgnoreCase(bpLog.getMessage()))).sorted(BPLog::compareTo).map(
				new BPLogToAWSLogMapper()).collect(Collectors.toList());

		awsLogEventList = adjustTimestamps(awsLogEventList, modelTaskId);

		var size = 2000;
		var it = 0;

		Integer total = 0;

		while (awsLogEventList.size() > it * size) {

			List<InputLogEvent> sublist = awsLogEventList.subList(it * size, Math.min((it * size) + size, awsLogEventList.size()));

			total += writeList(sublist, modelTaskId);

			it++;

		}

		return total;

	}

	List<InputLogEvent> adjustTimestamps(List<InputLogEvent> awsLogEventList, String modelTaskId) {

		Long lastTS = 0L;

		if (lastWrittenTS.containsKey(writeTokenKey(modelLogGroup, modelTaskId))) {
			lastTS = lastWrittenTS.get(writeTokenKey(modelLogGroup, modelTaskId));
		}

		var adjuster = new AWSLogEventTSAligner(lastTS);

		List<InputLogEvent> adjusted = awsLogEventList.stream().map(adjuster).collect(Collectors.toList());

		lastWrittenTS.put(writeTokenKey(modelLogGroup, modelTaskId), adjuster.getTs());

		return adjusted;
	}

	private Integer writeList(List<InputLogEvent> awsLogEventList, String modelTaskId) {

		var builder = PutLogEventsRequest.builder().logEvents(awsLogEventList);

		String token = writeNextSequenceToken.get(writeTokenKey(modelLogGroup, modelTaskId));

		logger.trace("SequenceToken {}", token);

		builder.sequenceToken(token).logGroupName(modelLogGroup).logStreamName(getLogStreamName(modelTaskId));

		return doWriteLog(builder.build(), modelTaskId);

	}

	@Override
	public Boolean createLog(String runid, String modelTaskId) {

		return doCreateLogStream(CreateLogStreamRequest.builder().logStreamName(getLogStreamName(modelTaskId)).logGroupName(modelLogGroup)
				.build());
	}

	@Override
	public Boolean supports(String type) {
		return "aws".equalsIgnoreCase(type);
	}

	Boolean doCreateLogStream(CreateLogStreamRequest createLogStreamRequest) {

		try {

			getClient().createLogStream(createLogStreamRequest);

		} catch (AwsServiceException | SdkClientException ex) {
			logger.error("Exception creating log stream", ex);

			return Boolean.FALSE;
		}

		return Boolean.TRUE;
	}

	public Integer doWriteLog(PutLogEventsRequest putLogEventsRequest, String modelTaskId) {

		try {

			PutLogEventsResponse putResponse = getClient().putLogEvents(putLogEventsRequest);

			Integer towrite = putLogEventsRequest.logEvents().size();
			Integer rejected = 0;

			if (null != putResponse.rejectedLogEventsInfo())
				rejected = putResponse.rejectedLogEventsInfo().expiredLogEventEndIndex() + putResponse.rejectedLogEventsInfo()
						.tooOldLogEventEndIndex() + putResponse.rejectedLogEventsInfo().tooNewLogEventStartIndex();

			String token = putResponse.nextSequenceToken();
			logger.trace("PutLogs completed - Next sequenceToken {}", token);

			writeNextSequenceToken.put(writeTokenKey(modelLogGroup, modelTaskId), token);

			return towrite - rejected;

		} catch (AwsServiceException | SdkClientException ex) {
			logger.error("Exception writing logs", ex);

		}

		return 0;

	}

	private String writeTokenKey(String modelLogGroup, String modelTaskId) {
		return modelLogGroup + "--" + modelTaskId;
	}

	CloudWatchLogsClient getClient() {
		return client;
	}

	GetLogEventsResponse doGetLogEvents(GetLogEventsRequest request) {
		try {

			return getClient().getLogEvents(request);

		} catch (AwsServiceException | SdkClientException ex) {

			logger.error("Exception reading logs from AWS", ex);

		}

		return GetLogEventsResponse.builder().events(Arrays.asList()).build();

	}

	private LogEvent notProducedLogs() {

		var event = new LogEvent();
		event.setMessage(NO_LOG_YET);
		return event;
	}

	public void setModelLogGroup(String modelLogGroup) {
		this.modelLogGroup = modelLogGroup;
	}

	public void setClient(CloudWatchLogsClient client) {
		this.client = client;
	}
}
