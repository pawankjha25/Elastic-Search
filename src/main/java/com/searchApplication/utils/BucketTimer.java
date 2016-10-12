package com.searchApplication.utils;

import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.searchApplication.es.search.bucketing.AttributeBucketer;

public class BucketTimer {
	private static final Logger LOGGER = LoggerFactory.getLogger(BucketTimer.class);

	private static void getTimeForQuery(String query, Client client, String index, String type, int loops,
			int hitsInScroll) {

		long start = System.currentTimeMillis();

		LOGGER.info("{}", AttributeBucketer.generateBuckets(client, index, type, query, loops, hitsInScroll).getSearchResponse());

		long end = System.currentTimeMillis() - start;
		LOGGER.info("The process lasted {} ", end);
	}

	public static void main(String[] args) {
		ESConnection client = new ESConnection(args[0], args[1]);

		getTimeForQuery(args[2], client.getClient(), args[3], args[4], Integer.parseInt(args[5]),
				Integer.parseInt(args[6]));
	}

}
