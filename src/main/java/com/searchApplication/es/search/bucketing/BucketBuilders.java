package com.searchApplication.es.search.bucketing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.searchApplication.es.services.impl.StringCompareUtil;
import com.searchApplication.utils.Stemmer;

public class BucketBuilders {

	private static final String LOCATION_IDENTIFIER = "_LOC";
	private static final String SPACE_DELIMITER = " ";
	private static List<String> STOP_LIST = Arrays.asList("a", "an", "and", "are", "as", "at", "be", "but", "by", "for",
			"if", "in", "into", "is", "it", "no", "not", "of", "on", "or", "such", "that", "the", "their", "then",
			"there", "these", "they", "this", "to", "was", "will", "with", "more");

	private static Stemmer STEMMER = new Stemmer();

	public static Bucket createFromQueryString(String query, List<String> bucket) {

		Set<String> bucketWords = new HashSet<String>();
		int perfectMatches = 0;
		int partialMathces = 0;
		int totalDistance = 0;
		String[] queryWords = query.toLowerCase().split(SPACE_DELIMITER);

		for (String b : bucket) {

			boolean isLocation = false;
			int locationMatch = 0;
			int locationLenght = 0;
			if (b.endsWith(LOCATION_IDENTIFIER)) {
				isLocation = true;
				b = b.substring(0, b.lastIndexOf(LOCATION_IDENTIFIER)).replaceAll("_", " ");
				locationLenght = b.split(" ").length;
			}
			String[] bucketTerms = b.split(SPACE_DELIMITER);

			for (String t : bucketTerms) {
				for (String q : queryWords) {
					String queryPrefix = q.length() > 2 ? q.substring(0, 3) : q;

					if (STOP_LIST.contains(q)) {
						continue;
					}
					String cleaned = t.toLowerCase().trim().replaceAll("\\p{P}", "");
					if (STOP_LIST.contains(t)) {
						continue;
					}

					if (isLocation) {
						if (q.equals(cleaned)) {

							locationMatch++;
						}
					} else {
						String qStem = STEMMER.stem(q);
						String termStem = STEMMER.stem(cleaned);
						int distance = StringCompareUtil.editDistance(qStem, termStem);
						String termPrefix = cleaned.length() > 2 ? cleaned.substring(0, 3) : cleaned;
						if (isPerfectMatch(qStem, termStem, queryPrefix, termPrefix, distance)) {
							perfectMatches++;
							totalDistance += distance;
							bucketWords.add(b);

						} else if (isPartialMatch(qStem, termStem, queryPrefix, termPrefix, distance)) {
							partialMathces++;
							totalDistance += distance;
							bucketWords.add(b);

						}
					}
				}

				if (isLocation) {
					if (locationLenght == locationMatch) {
						perfectMatches++;
						b += LOCATION_IDENTIFIER;
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

		if ((queryPrefix.equals(termPrefix) || lcs >= 4) && distance <= 1) {
			return true;
		} else {

			return false;
		}
	}

}
