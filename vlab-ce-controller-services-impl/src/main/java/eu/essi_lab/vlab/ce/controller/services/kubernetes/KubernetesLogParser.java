package eu.essi_lab.vlab.ce.controller.services.kubernetes;

import eu.essi_lab.vlab.core.datamodel.BPLog;
import eu.essi_lab.vlab.core.datamodel.BPLogChunk;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mattia Santoro
 */
public class KubernetesLogParser {

	private Logger logger = LogManager.getLogger(KubernetesLogParser.class);

	public BPLogChunk parse(String text) {

		Long lastTS = 0L;
		Long lastNano = 0L;

		var chunk = new BPLogChunk();

		if (null == text) {
			logger.trace("Received null log");
			return chunk;
		}

		logger.trace("Parsing log chunk");

		var scanner = new Scanner(text);

		Integer lines = 0;

		while (scanner.hasNextLine()) {

			String line = scanner.nextLine();

			String logts = line.split(" ")[0];

			String message = line.replace(logts + " ", "");

			try {

				lastTS = parseTimestamp(logts);
				lastNano = parseNanostamp(logts);

			} catch (ParseException e) {

				message = line;
				lastNano = lastNano + 1;
			}

			var bpLog = new BPLog(lastTS, lastNano);

			bpLog.setMessage(message);

			chunk.getSet().add(bpLog);

			lines++;

		}
		scanner.close();

		logger.trace("Parsed {} lines of log", lines);
		return chunk;
	}

	Long parseTimestamp(String kubeTS) throws ParseException {
		String[] splitTs = kubeTS.split("\\.");
		String secs = splitTs[0] + "Z";

		var sdf = new SimpleDateFormat();
		sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
		Date d = sdf.parse(secs);

		return d.getTime();

	}

	Long parseNanostamp(String kubeTS) throws ParseException {
		String[] splitTs = kubeTS.split("\\.");
		String numbers = splitTs[1].replace("Z", "");

		return Long.valueOf(numbers);

	}
}
