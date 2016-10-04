package com.searchApplication.es.search.bucketing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.searchApplication.es.services.impl.StringCompareUtil;
import com.searchApplication.utils.Stemmer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BucketBuilders {

	private static final String LOCATION_IDENTIFIER = "_LOC";
	private static final String SPACE_DELIMITER = " ";
	private static List<String> STOP_LIST = Arrays.asList("a", "an", "and", "are", "as", "at", "be", "but", "by", "for",
			"if", "in", "into", "is", "it", "no", "not", "of", "on", "or", "such", "that", "the", "their", "then",
			"there", "these", "they", "this", "to", "was", "will", "with", "more");

	private static Stemmer STEMMER = new Stemmer();

	private static final Logger LOGGER = LoggerFactory.getLogger(BucketBuilders.class);

	public static Bucket createFromQueryString(String query, List<String> bucket) {

		Set<String> bucketWords = new HashSet<String>();
		int perfectMatches = 0;
		int partialMathces = 0;
		int totalDistance = 0;
		String[] queryWords = query.toLowerCase().split(SPACE_DELIMITER);
		Set<String> matchedLocations = new HashSet<String>();
		for (String q : queryWords) {
			if (STOP_LIST.contains(q)) {
				continue;
			}
			for (String b : bucket) {

				boolean isLocation = false;
				if (b.endsWith(LOCATION_IDENTIFIER)) {
					isLocation = true;
					b = b.substring(0, b.lastIndexOf(LOCATION_IDENTIFIER)).replaceAll("_", " ");
				}
				String[] bucketTerms = b.split(SPACE_DELIMITER);

				for (String t : bucketTerms) {

					String cleaned = t.toLowerCase().trim().replaceAll("\\p{P}", "");
					if (STOP_LIST.contains(t)) {
						continue;
					}

					if (isLocation) {
						if (q.equals(cleaned)) {
							b += LOCATION_IDENTIFIER;
							perfectMatches++;
							bucketWords.add(b);
							matchedLocations.add(q);
							System.err.println("Matched to loc " + q);
						}
					}
				}
			}
		}
		for (String q : queryWords) {
			if (STOP_LIST.contains(q) || matchedLocations.contains(q)) {
				continue;
			}
			String queryPrefix = q.length() > 2 ? q.substring(0, 3) : q;
			for (String b : bucket) {
				String[] bucketTerms = b.split(SPACE_DELIMITER);
				for (String t : bucketTerms) {
					String cleaned = t.toLowerCase().trim().replaceAll("\\p{P}", "");
					if (STOP_LIST.contains(t)) {
						continue;
					}
					String qStem = STEMMER.stem(q);
					String cStem = STEMMER.stem(cleaned);
					int distance = StringCompareUtil.editDistance(qStem, cStem);
					String termPrefix = cleaned.length() > 2 ? cleaned.substring(0, 3) : cleaned;
					if (isPerfectMatch(qStem, cStem, queryPrefix, termPrefix, distance)) {
						perfectMatches++;
						totalDistance += distance;
						bucketWords.add(b);
					} else if (isPartialMatch(qStem, cStem, queryPrefix, termPrefix, distance)) {
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

	private static boolean isPartialMatch(String fullQuery, String fullTerm, String queryPrefix, String termPrefix,
			int distance) {
		int lcs = StringCompareUtil.getLongestCommonSubsequence(fullQuery, fullTerm);
		LOGGER.debug("partial match {} and {} distance {} lcs {}", fullQuery, fullTerm, distance, lcs);
		if ((queryPrefix.equals(termPrefix) || lcs >= 3) && distance <= 3) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean isPerfectMatch(String fullQuery, String fullTerm, String queryPrefix, String termPrefix,
			int distance) {

		int lcs = Math.min(Math.min(fullQuery.length(), fullTerm.length()),
				StringCompareUtil.getLongestCommonSubsequence(fullQuery, fullTerm));
		LOGGER.debug("perfect match {} and {} distance {} lcs {}", fullQuery, fullTerm, distance, lcs);

		if ((queryPrefix.equals(termPrefix) || lcs >= 4) && distance <= 1) {
			return true;
		} else {

			return false;
		}
	}

}
