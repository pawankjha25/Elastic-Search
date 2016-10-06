package com.searchApplication.es.indexing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ProcessDumpFile {

	private static String INDEX_LINE = "{\"index\":{\"_id\":\"";
	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessDumpFile.class);
	private static String INDEX_END = "\" } }";
	private static double BYTES_AS_MB = 1000000;
	private static ObjectMapper MAPPER = new ObjectMapper();

	public static void runIndexing(String sourceFile, String output, int batchSize)
			throws Exception {

		BufferedReader br = new BufferedReader(new FileReader(sourceFile));
		String line = "";
		long length = 0;
		int count = 0;
		int splitCounter = 0;
		int lastWritten = -1;
		BufferedWriter bw = new BufferedWriter(new FileWriter(output + " - " + "split" + "-" + splitCounter));
		BufferedWriter bwLoadScript = new BufferedWriter(new FileWriter("script-batch"));
		int error = 0;

		while ((line = br.readLine()) != null) {
			try {
				String bulkLine = INDEX_LINE + count + INDEX_END + "\n"
						+ MAPPER.writeValueAsString(CSVParser.parseLine(line));
				System.out.println(bulkLine);
				if (!bulkLine.equals("")) {
					count++;
					length += line.getBytes("UTF-16").length;
					bw.write(bulkLine);
					if (count == 1000) {
						LOGGER.info("Processed 1000 ");
						count = 0;
					}

				}
			} catch (Exception e) {
				LOGGER.error("Problem {} with line {}", error++, line);
			}
			if (length / BYTES_AS_MB > batchSize) {

				bw.flush();
				bw.close();

				bwLoadScript.write("curl -s -XPOST localhost:9200/\"$1\"/\"$2\"/_bulk --data-binary @" + output + " - "
						+ "split" + "-" + splitCounter + "-" + splitCounter + "\n");
				length = 0;
				lastWritten = splitCounter;
				splitCounter++;
				bw = new BufferedWriter(new FileWriter("split-" + splitCounter));

			}

		}
		if (splitCounter != lastWritten) {
			bwLoadScript.write("curl -s -XPOST localhost:9200/\"$1\"/\"$2\"/_bulk --data-binary @" + "split-"
					+ splitCounter + "\n");
		}
		LOGGER.info("FINISHING batch {} with {} data points", splitCounter);
		bw.flush();
		bw.close();
		bwLoadScript.close();
		br.close();
	}
	
	// args[0] -- input csv file
	// args[1] -- the output split name (can be anything)
	///args[2] -- size of splits in mb (20-25 should be the best)
	public static void main(String[] args) throws Exception {
		runIndexing(args[0], args[1], Integer.parseInt(args[2]));
	}

}
