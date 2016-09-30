package com.searchApplication.utils;

import org.elasticsearch.client.Client;

import com.searchApplication.es.search.bucketing.AttributeBucketer;

public class BucketTimer {

	private static void getTimeForQuery(String query, Client client, String index, String type, int loops) {

		long start = System.currentTimeMillis();

		System.out.println(AttributeBucketer.generateBuckets(client, index, type, query, loops));

		long end = System.currentTimeMillis() - start;
		System.out.println("The process lasted " + end);
	}

	public static void main(String[] args) {
		ESConnection client = new ESConnection(args[0], args[1]);

		getTimeForQuery(args[2], client.getClient(), args[3], args[4], Integer.parseInt(args[5]));
	}

}
