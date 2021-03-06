package com.searchApplication.utils;

import java.io.IOException;
import java.util.Set;

import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.searchApplication.es.search.bucketing.AttributeBucketer;

public class BucketTimer {
	private static final Logger LOGGER = LoggerFactory.getLogger(BucketTimer.class);

	private static void getTimeForQuery(String query, Client client, String index, String type, int loops,
			int hitsInScroll, String path)  throws IOException{
		Set<String> locations =  LocationLoader.getLocationsFromFile(path);
		long start = System.currentTimeMillis();

		LOGGER.info("{}", AttributeBucketer.generateBuckets(client, index, type, query, loops, hitsInScroll,
				locations, false).getSearchResponse());

		long end = System.currentTimeMillis() - start;
		LOGGER.info("The process lasted {} ", end);
	}

	public static void main(String[] args) throws Exception {
		ESConnection client = new ESConnection(args[0], args[1]);

		getTimeForQuery(args[2], client.getClient(), args[3], args[4], Integer.parseInt(args[5]),
				Integer.parseInt(args[6]), args[7]);
	}

}
