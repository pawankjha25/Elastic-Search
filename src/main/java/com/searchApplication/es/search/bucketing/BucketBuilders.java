package com.searchApplication.es.search.bucketing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.searchApplication.es.services.impl.StringCompareUtil;

public class BucketBuilders {

	private static final String SPACE_DELIMITER = " ";

	public static Bucket createFromQueryString(String query, List<String> bucket) {

		Set<String> bucketWords = new HashSet<String>();
		int perfectMatches = 0;
		int partialMathces = 0;
		int totalDistance = 0;

		String[] queryWords = query.toLowerCase().split(SPACE_DELIMITER);
		for (String q : queryWords) {
			String queryPrefix = q.length() > 2 ? q.substring(0, 3) : q;
			for (String b : bucket) {
				String[] bucketTerms = b.split(SPACE_DELIMITER);

				for (String t : bucketTerms) {
					String cleaned = t.toLowerCase().trim().replaceAll("\\p{P}", "");
					int distance = StringCompareUtil.editDistance(q, cleaned);
					String termPrefix = cleaned.length() > 2 ? cleaned.substring(0, 3) : cleaned;
					if (isPerfectMatch(queryPrefix, termPrefix, distance)) {
						perfectMatches++;
						totalDistance += distance;
						bucketWords.add(b);
					} else if (isPartialMatch(distance)) {
						partialMathces++;
						totalDistance += distance;
						bucketWords.add(b);

					}
				}
			}
		}
		if (perfectMatches > 0 || partialMathces > 0) {
			return new Bucket(bucketWords, perfectMatches, partialMathces, totalDistance);
		} else {
			return null;
		}
	}

	private static boolean isPartialMatch(int distance) {
		return distance < 3;
	}

	private static boolean isPerfectMatch(String queryPrefix, String termPrefix, int distance) {

		if (queryPrefix.equals(termPrefix) && distance < 2) {
			return true;
		} else {
			return false;
		}
	}
}
