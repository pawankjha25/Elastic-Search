package com.searchApplication.es.search.bucketing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.searchApplication.es.services.impl.StringCompareUtil;
import com.searchApplication.utils.Stemmer;
import com.searchApplication.utils.StopWords;

public class BucketBuilders {

	private static final String SPACE_DELIMITER = " ";

	private static Stemmer STEMMER = new Stemmer();

	public static Bucket createFromQueryString(String query, Set<String> locations, List<String> bucket,
			Set<String> hits) {

		Set<String> bucketWords = new HashSet<String>();
		int perfectMatches = 0;
		int partialMathces = 0;
		int totalDistance = 0;
		List<QueryTerm> queryWords = new ArrayList<QueryTerm>();
		int qc = 0;
		for (String q : query.toLowerCase().split(SPACE_DELIMITER)) {
			if (!StopWords.STOP_LIST.contains(q.toLowerCase())) {
				String queryPrefix = q.length() > 2 ? q.substring(0, 3) : q;
				QueryTerm qt = new QueryTerm();
				qt.setStem(STEMMER.stem(q));
				qt.setPrefix(queryPrefix);
				qt.setOriginal(q);
				queryWords.add(qt);
				qc++;
			}

		}
		List<BucketTerms> bucketTerms = new ArrayList<BucketTerms>();
		Set<String> matchedQueries = new HashSet<String>();
		for (String b : bucket) {
			String[] bs = b.toLowerCase().trim().replaceAll("\\p{P}", "").split(SPACE_DELIMITER);
			int localMatches = 0;
			int firstMatch = 0;

			for (String t : bs) {
				if (StopWords.STOP_LIST.contains(t)) {
					continue;
				}
				if (t.length() < 2) {
					continue;
				}
				String termStem = STEMMER.stem(t);
				String termPrefix = t.length() > 2 ? t.substring(0, 3) : t;
				int localQC = 0;
				for (QueryTerm q : queryWords) {
					localQC++;
					int distance = StringCompareUtil.editDistance(q.getStem(), termStem);
					if (isPerfectMatch(q.getOriginal(), termStem, q.getPrefix(), termPrefix, distance)) {
						if (!matchedQueries.contains(q.getOriginal())) {
							perfectMatches++;
							matchedQueries.add(q.getOriginal());
						}
						localMatches++;
						if (firstMatch == 0) {
							firstMatch = localQC;
						}
						totalDistance += distance;
						bucketWords.add(b);

					}

				}

			}
			if (localMatches > 0) {
				BucketTerms bte = new BucketTerms();
				bte.setAttributeName(b);
				bte.setQueryWordMatch(firstMatch);
				bte.setFull(bs.length <= localMatches ? true : false);
				bte.setMatchedQueryWordsCount(localMatches);
				bucketTerms.add(bte);
			}

		}
		if (!locations.isEmpty()) {
			for (String location : locations) {
				BucketTerms bte = new BucketTerms();
				bte.setAttributeName(location);
				bte.setQueryWordMatch(qc);
				bte.setFull(true);
				bte.setMatchedQueryWordsCount(1);
				perfectMatches++;
				bucketTerms.add(bte);
			}
		}
		if (perfectMatches > 0)

		{
			return new Bucket(BucketTerms.createdQuerySortedBucket(bucketTerms), perfectMatches, partialMathces,
					totalDistance);
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
