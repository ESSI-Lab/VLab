package eu.essi_lab.vlab.ce.engine.services;

import eu.essi_lab.vlab.ce.engine.services.es.AWSESRequestSubmitter;
import eu.essi_lab.vlab.ce.engine.services.es.AWSESUrlParser;
import eu.essi_lab.vlab.ce.engine.services.es.ESClientBPRun;
import eu.essi_lab.vlab.core.datamodel.ESQueryBPRuns;
import eu.essi_lab.vlab.ce.engine.services.utils.BPRunStorageUtils;
import eu.essi_lab.vlab.core.configuration.BPStaticConfigurationParameters;
import eu.essi_lab.vlab.core.configuration.ConfigurationParameter;
import eu.essi_lab.vlab.core.datamodel.BPException;
import eu.essi_lab.vlab.core.datamodel.BPRun;
import eu.essi_lab.vlab.core.datamodel.BPRuns;
import eu.essi_lab.vlab.core.datamodel.BPUser;
import eu.essi_lab.vlab.core.datamodel.QueueMessage;
import eu.essi_lab.vlab.core.datamodel.QueueMessageHandler;
import eu.essi_lab.vlab.core.datamodel.SharedBPRun;
import eu.essi_lab.vlab.core.engine.services.IBPQueueClient;
import eu.essi_lab.vlab.core.engine.services.IBPRunStorage;
import eu.essi_lab.vlab.core.engine.services.IESClient;
import eu.essi_lab.vlab.core.engine.services.IESRequestSubmitter;
import eu.essi_lab.vlab.core.serialization.json.JSONDeserializer;
import eu.essi_lab.vlab.core.serialization.json.JSONSerializer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 * @author Mattia Santoro
 */
public class AWSESBPRunRegistryStorage implements IBPRunStorage {

	public static final String BPRUN_TYPE = "bpruns";
	private IBPQueueClient sqsClient;

	private static Logger logger = LogManager.getLogger(AWSESBPRunRegistryStorage.class);

	private IESClient<BPRuns> esClient;
	private static final String RECEIPT_PREFIX = "run-receipt-";
	private static final String RECEIPT_KEY = "sqsreceipt";
	private static final Map<String, Object> handlerMap = new HashMap<>();

	@Override
	public List<ConfigurationParameter> configurationParameters() {
		return Arrays.asList(BPStaticConfigurationParameters.STORAGE_ES_USER.getParameter(),
				BPStaticConfigurationParameters.STORAGE_ES_PWD.getParameter(), BPStaticConfigurationParameters.AWS_ES_REGION.getParameter(),
				BPStaticConfigurationParameters.AWS_S3_BP_RUN_BUCKET_NAME.getParameter(),
				BPStaticConfigurationParameters.STORAGE_ESURL.getParameter());
	}

	@Override
	public void configure(Map<ConfigurationParameter, String> parameters) throws BPException {
		String esAccessKey = parameters.get(BPStaticConfigurationParameters.STORAGE_ES_USER.getParameter());
		String esSecretKey = parameters.get(BPStaticConfigurationParameters.STORAGE_ES_PWD.getParameter());
		String esRegion = parameters.get(BPStaticConfigurationParameters.AWS_ES_REGION.getParameter());
		String bucketName = parameters.get(BPStaticConfigurationParameters.AWS_S3_BP_RUN_BUCKET_NAME.getParameter());
		String esUrl = parameters.get(BPStaticConfigurationParameters.STORAGE_ESURL.getParameter());

		setEsClient(new ESClientBPRun(esUrl, bucketName, BPRUN_TYPE));

		if (awsElasticSearch(esUrl)) {
			IESRequestSubmitter sub = new AWSESRequestSubmitter();
			sub.setPwd(esSecretKey);
			sub.setUser(esAccessKey);
			((AWSESRequestSubmitter) sub).setRegion(esRegion);
			getEsClient().setSubmitter(sub);

		} else {

			IESRequestSubmitter sub = new ESRequestSubmitter();
			sub.setPwd(esSecretKey);
			sub.setUser(esAccessKey);

			getEsClient().setSubmitter(sub);
		}

	}



	boolean awsElasticSearch(String url) {

		return new AWSESUrlParser(url).isAWSESEndpoint();
	}

	boolean toQueue(BPRun run) throws BPException {

		sqsClient.add(new JSONSerializer().serialize(run));

		return true;
	}

	@Override
	public boolean queue(BPRun run) {

		boolean stored = storeToES(run);

		if (!stored)
			return false;

		try {

			return toQueue(run);

		} catch (BPException e) {

			logger.warn("Unable to queque BPRun {} -- Removing from registry", run.getRunid(), e);

			try {
				if (!remove(run, run.getOwner()))
					logger.error("Can't remove BPRun {} from storage", run.getRunid());

			} catch (BPException e1) {

				logger.error("Exception removing BPRun {} from storage after queue queue failure", run.getRunid(), e1);

			}

			return false;
		}

	}

	boolean storeToES(BPRun run) {

		String s3Key = getS3BPRunKey(run.getRunid());

		try {

			return getEsClient().store(s3Key, new ByteArrayInputStream(new JSONSerializer().serialize(run).getBytes(StandardCharsets.UTF_8)));

		} catch (BPException e) {

			logger.error("Exception storing run {}: ", run.getRunid(), e);

			return false;

		}

	}

	private String getS3BPRunKey(String runid) {

		//If this impl changes you need to update all of its usages since this is erroneously invoked multiple times

		return runid;
	}

	boolean doRemove(String runid) throws BPException {
		return getEsClient().remove(getS3BPRunKey(runid));
	}

	@Override
	public boolean remove(BPRun run, String requestingUserEmail) throws BPException {

		BPRun runFromStorage = get(run.getRunid(), requestingUserEmail);

		BPRunStorageUtils.throwNotAuthorizedExceptionIfNotMyRun(runFromStorage, requestingUserEmail,
				"You are not allowed to remove this BPRun");

		boolean removed = doRemove(getS3BPRunKey(run.getRunid()));

		if (removed)
			logger.info("Successfully removed {} from s3", run.getRunid());
		else
			logger.error("Can not remove {} from s3", run.getRunid());

		return removed;
	}

	BPRun readFromES(String key) throws BPException {

		InputStream stream = readStreamFromES(getS3BPRunKey(key));

		logger.trace("Found {} in my runs folder", key);

		return new JSONDeserializer().deserialize(stream, BPRun.class);

	}

	boolean existsInES(String key) throws BPException {

		return getEsClient().exists(getS3BPRunKey(key));

	}

	@Override
	public BPRun get(String runid, String requestingUserEmail) throws BPException {

		logger.debug("Searching {} in my runs folder", runid);

		BPRun run = readFromES(getS3BPRunKey(runid));

		BPRunStorageUtils.throwNotAuthorizedExceptionIfCantRead(run, requestingUserEmail);

		return run;

	}

	@Override
	public boolean exists(String runid) throws BPException {

		return existsInES(getS3BPRunKey(runid));

	}

	@Override
	public BPRuns search(BPUser user, String text, Integer start, Integer count, String wfid) throws BPException {

		ESQueryBPRuns queryBPRuns = new ESQueryBPRuns();

		queryBPRuns.setCount(count);
		queryBPRuns.setStart(start);
		queryBPRuns.setText(text);
		queryBPRuns.setWfid(wfid);

		return doSearch(user, queryBPRuns);
	}

	BPRuns doSearch(BPUser user, ESQueryBPRuns query) throws BPException {
		return getEsClient().search(user, query);
	}

	@Override
	public boolean udpdateRun(BPRun run, String requestingUserEmail) throws BPException {

		BPRun runFromStorage = get(run.getRunid(), requestingUserEmail);

		BPRunStorageUtils.throwNotAuthorizedExceptionIfNotMyRun(runFromStorage, requestingUserEmail,
				"You are not authorized to modify this experiment");

		BPRun runToStore = mergeRun(run, runFromStorage);

		return storeToES(runToStore);

	}

	@Override
	public void extendVisibilityTimeout(BPRun run, Integer seconds) throws BPException {

		logger.debug("Reading receipt of run {}", run.getRunid());

		QueueMessageHandler receipt = null;
		try {
			receipt = readReceipt(getS3ReceiptKey(run));
		} catch (IOException e) {
			logger.error("IOException reading receipt of run {}", run.getRunid());
			throw new BPException("IOException reading receipt of run " + run.getRunid() + " -- IOException message: " + e.getMessage(),
					BPException.ERROR_CODES.RESOURCE_NOT_FOUND);

		}

		logger.debug("Receipt of run {} is {}", run.getRunid(), receipt);

		doExtendVisibilityTimeout(receipt, seconds);

	}

	@Override
	public Boolean supports(String type) {
		return (type != null && (type.equalsIgnoreCase("awsessqs") || type.equalsIgnoreCase("essqs")));
	}

	@Override
	public void setQueueClient(IBPQueueClient client) {
		this.sqsClient = client;
	}

	boolean doExtendVisibilityTimeout(QueueMessageHandler handler, Integer seconds) throws BPException {
		return sqsClient.extendVisibilityTimeout(handler, seconds);
	}

	private BPRun mergeRun(BPRun run, BPRun runFromStorage) throws BPException {

		runFromStorage.setPublicRun(run.isPublicRun());

		runFromStorage.setRunid(run.getRunid());

		runFromStorage.setInputs(run.getInputs());

		runFromStorage.setWorkflowid(run.getWorkflowid());

		runFromStorage.setName(run.getName());

		runFromStorage.setCreationTime(run.getCreationTime());

		List<String> newShared = run.getSharedWith();

		for (String s : newShared) {

			if (!runFromStorage.getSharedWith().contains(s))
				runFromStorage.shareWithUser(s);

		}

		return runFromStorage;
	}

	@Override
	public BPRun resolveSharedBPRun(SharedBPRun sharedBPRun, String requestingUserEmail) throws BPException {

		return BPRunStorageUtils.resolveSharedBPRun(sharedBPRun, requestingUserEmail, this);

	}

	QueueMessage fromQueue() throws BPException {
		return sqsClient.getNextMessage();
	}

	private InputStream receiptHandleJson(String receiptHandle) {

		JSONObject json = new JSONObject();

		json.put(RECEIPT_KEY, receiptHandle);

		logger.trace("Receipt json {}", json);

		return new ByteArrayInputStream(json.toString().getBytes(StandardCharsets.UTF_8));

	}

	boolean storeReceipt(String receiptid, QueueMessageHandler receiptHandle) throws BPException {
		Optional<String> handleid = receiptHandle.getReceiptHandleId();

		if (handleid.isPresent()) {

			logger.debug("Receipt handler is a string, store to es");

			return storeReceiptHanlderId(receiptid, receiptHandleJson(handleid.get()));
		}

		Optional<Object> handler = receiptHandle.getReceiptHandler();

		if (handler.isPresent()) {
			return storeReceiptHanlderObject(receiptid, handler.get());
		}

		logger.error("No receipt handler found for {}", receiptid);

		throw new BPException("Can't find receipt handler for " + receiptid, BPException.ERROR_CODES.NO_QUEUE_MESSAGE_RECIPT_HANDLER);

	}

	boolean storeReceiptHanlderId(String receiptid, InputStream stream) throws BPException {
		return getEsClient().store(receiptid, stream);
	}

	boolean storeReceiptHanlderObject(String rid, Object o) {
		handlerMap.put(rid, o);
		return true;
	}

	@Override
	public Optional<BPRun> nextQuequedBPRun() throws BPException {

		QueueMessage message = fromQueue();

		if (message == null)
			return Optional.empty();

		logger.trace("Found not null message from {}", sqsClient.getQueueName());

		BPRun run = new JSONDeserializer().deserialize(message.getBody(), BPRun.class);

		storeReceipt(getS3ReceiptKey(run), message.getHandler());

		return Optional.of(run);

	}

	private String getS3ReceiptKey(BPRun run) {
		return RECEIPT_PREFIX + run.getRunid();
	}

	Optional<Object> readReceiptFromLocal(String receiptid) {
		return Optional.ofNullable(handlerMap.get(receiptid));
	}

	void reemoveReceiptFromLocal(String receiptid) {

		handlerMap.remove(receiptid);

	}

	QueueMessageHandler readReceipt(String receiptid) throws BPException, IOException {

		QueueMessageHandler queueMessageHandler = new QueueMessageHandler();

		Optional<Object> local = readReceiptFromLocal(receiptid);

		if (local.isPresent()) {

			queueMessageHandler.setReceiptHandler(local.get());

			return queueMessageHandler;

		}

		InputStream stream = readStreamFromES(receiptid);

		String txt = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

		logger.trace("Text receipt read from es {}", txt);

		JSONObject json = new JSONObject(txt);

		logger.trace("Json receipt read from es {}", json);

		queueMessageHandler.setReceiptHandleId(json.getString(RECEIPT_KEY));

		return queueMessageHandler;

	}

	InputStream readStreamFromES(String key) throws BPException {

		return getEsClient().read(getS3BPRunKey(key));

	}

	@Override
	public boolean moveToTriggered(BPRun run) {

		try {

			logger.debug("Reading receipt of run {}", run.getRunid());

			QueueMessageHandler receipt = readReceipt(getS3ReceiptKey(run));

			logger.debug("Receipt of run {} is {}", run.getRunid(), receipt);

			boolean deleted = deleteMessage(receipt);

			logger.debug("Receipt of run {} is {} and was deleted {}", run.getRunid(), receipt, deleted);

			if (receipt.getReceiptHandleId().isPresent()) {
				boolean removed = doRemove(getS3ReceiptKey(run));

				logger.debug("Receipt of run {} is {}, was deleted {} and removed from es {}", run.getRunid(), receipt, deleted, removed);

				return deleted && removed;
			}

			reemoveReceiptFromLocal(getS3ReceiptKey(run));

			return deleted;

		} catch (BPException | IOException e) {

			logger.error("Can not delete run {} from queque {}", run.getRunid(), sqsClient.getQueueName(), e);

			return false;
		}

	}

	boolean deleteMessage(QueueMessageHandler receipt) throws BPException {
		return sqsClient.deleteMessage(receipt);
	}

	@Override
	public boolean shareWith(BPRun run, String requestingUserEmail, String userToShareWithEmail) throws BPException {

		BPRunStorageUtils.throwNotAuthorizedExceptionIfNotMyRun(run, requestingUserEmail,
				"You are not allowed to share a run you don't own.");

		run.shareWithUser(userToShareWithEmail);

		return storeToES(run);

	}

	@Override
	public boolean revokeShare(BPRun run, String requestingUserEmail, String userToShareWithEmail) throws BPException {

		BPRunStorageUtils.throwNotAuthorizedExceptionIfNotMyRun(run, requestingUserEmail,
				"You are not allowed to revoke a share of a run you don't own.");

		run.deleteShareWithUser(userToShareWithEmail);

		return storeToES(run);

	}

	@Override
	public boolean makePublic(BPRun run, String requestingUserEmail) throws BPException {

		BPRunStorageUtils.throwNotAuthorizedExceptionIfNotMyRun(run, requestingUserEmail,
				"You are not allowed to make public a run you don't own.");

		run.setPublicRun(true);

		return storeToES(run);

	}

	@Override
	public boolean revokePublic(BPRun run, String requestingUserEmail) throws BPException {

		BPRunStorageUtils.throwNotAuthorizedExceptionIfNotMyRun(run, requestingUserEmail,
				"You are not allowed to make public a run you don't own.");

		run.setPublicRun(false);

		return storeToES(run);

	}



	public IESClient<BPRuns> getEsClient() {
		return esClient;
	}

	public void setEsClient(IESClient<BPRuns> es_client) {
		this.esClient = es_client;
	}
}
