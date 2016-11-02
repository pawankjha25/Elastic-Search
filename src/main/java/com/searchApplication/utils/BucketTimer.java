package com.searchApplication.utils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.searchApplication.es.entities.BucketResponse;
import com.searchApplication.es.search.bucketing.AttributeBucketer;

public class BucketTimer {
	private static final Logger LOGGER = LoggerFactory.getLogger(BucketTimer.class);

	private static Set<BucketResponse> getTimeForQuery(String query, Client client, String index, String type,
			int loops, int hitsInScroll, Set<String> locations) throws IOException {

		long start = System.currentTimeMillis();
		Set<BucketResponse> b = AttributeBucketer
				.generateBuckets(client, index, type, query, loops, hitsInScroll, locations).getSearchResponse();
		LOGGER.info("{}", b);

		long end = System.currentTimeMillis() - start;
		LOGGER.info("The process lasted {} ", end);
		return b;
	}

	public static void main(String[] args) throws Exception {
		ESConnection client = new ESConnection(args[0], args[1]);
		Set<String> locations = LocationLoader.getLocationsFromFile(args[7]);

		Set<BucketResponse> b1 = getTimeForQuery(args[2], client.getClient(), args[3], args[4],
				Integer.parseInt(args[5]), Integer.parseInt(args[6]), locations);

		Set<BucketResponse> b2 = getTimeForQuery(args[2], client.getClient(), args[3], args[4],
				Integer.parseInt(args[5]), Integer.parseInt(args[6]) / 2, locations);

		compareResults(b1, b2);
	}

	private static void compareResults(Set<BucketResponse> b1, Set<BucketResponse> b2) {
		BucketResponse[] ba = b1.toArray(new BucketResponse[b1.size()]);
		BucketResponse[] ba1 = b1.toArray(new BucketResponse[b2.size()]);
		Set<String> s = new HashSet<String>();
		Set<String> s1 = new HashSet<String>();

		int samePos = 0;
		int contain = 0;
		for (int i = 0; i < ba.length; i++) {
			if (ba[i].getSuggestionString().equals(ba1[i].getSuggestionString())) {
				samePos++;
			}
			s.add(ba[i].getSuggestionString());
			s1.add(ba1[i].getSuggestionString());

		}

		for (String sug : s) {
			if (s1.contains(sug)) {
				contain++;
			}
		}
		
		LOGGER.info("total on the same position {} and total contain {}", samePos, contain);

	}

}
