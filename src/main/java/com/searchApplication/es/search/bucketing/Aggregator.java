package com.searchApplication.es.search.bucketing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.searchApplication.utils.BucketTimer;

public class Aggregator {

	private static final Logger LOGGER = LoggerFactory.getLogger(Aggregator.class);

	public static List<Bucket> generateAggregated(List<Bucket> buckets) {
		Bucket[] aggregatedBuckets = new Bucket[buckets.size()];
		List<Bucket> resultBuckets = new ArrayList<Bucket>();
		HashMap<String, AggregatedBucket> bucketCombination = new HashMap<String, AggregatedBucket>();
		for (int i=0; i< buckets.size(); i++) {
			List<BucketTerms> l = generateCandidate(buckets.get(i));
			if (l != null) {
				checkAggregatedBucket(bucketCombination, l, i, buckets.get(i).getTotalRows(), buckets.get(i).getBucketMetaData());
				LOGGER.debug("Next bucket \n=========\n");

			}

		}
		List<Integer> aggregatedBucketsList = new ArrayList<Integer>();
		for (Map.Entry<String, AggregatedBucket> entry : bucketCombination.entrySet()) {
			LOGGER.debug("ENTRY {} terms {} size {} ", entry.getKey(), entry.getValue().getBucketTerms(), entry.getValue().getBuckets().size());
			if (entry.getValue().getBuckets().size() > 1) {
				Bucket c = new Bucket(entry.getValue().getBucketTerms(),
						buckets.get(entry.getValue().getFirstAppearance()).getTotalPerfectMatches(), 0, 0);
				c.setTotalRows(entry.getValue().getCount());
				c.setBucketMetaData(buckets.get(entry.getValue().getFirstAppearance()).getBucketMetaData());
				aggregatedBuckets[entry.getValue().getFirstAppearance()] = c;
				aggregatedBucketsList.addAll(entry.getValue().getBuckets());
				LOGGER.debug("Added to aggregation buckets {}", entry.getValue().getBuckets());

			}
		}

		for (int i = 0; i < buckets.size(); i++) {
			if (aggregatedBuckets[i] != null) {
				LOGGER.debug("Aggregated {} " + aggregatedBuckets[i].getBucketTerms());
				resultBuckets.add(aggregatedBuckets[i]);
			} else if (!aggregatedBucketsList.contains(i)) {
				LOGGER.debug("Old {} " +buckets.get(i).getBucketTerms());
				
				resultBuckets.add(buckets.get(i));
			} 
		}
		LOGGER.debug("Generated size {} " + resultBuckets.size());

		LOGGER.debug("Generated {} " + resultBuckets);
		Collections.sort(resultBuckets);
		return resultBuckets;
	}

	public static List<BucketTerms> generateCandidate(Bucket b) {
		int totalMatch = 0;
		Set<String> matched = new HashSet<String>();
		List<BucketTerms> aggTerms = new ArrayList<BucketTerms>();
		
		for (BucketTerms bt : BucketTerms.createdQuerySortedBucketSet(b.getBucketTerms())) {
			LOGGER.debug("ENTRY {}", bt);

			boolean forAdd = false;
			// TODO this orders differently if there is a multi match bucket
			// after a single match attribute
			if (bt.getMatchedQueries().size() == b.getTotalPerfectMatches()) {
				LOGGER.debug("match found {}", totalMatch );
				return Arrays.asList(bt);
			} else {
				for (String m : bt.getMatchedQueries()) {
					if (!matched.contains(m)) {
						totalMatch++;
						matched.add(m);
						forAdd = true;
					}
				}

				if (forAdd) {
					aggTerms.add(bt);
				}
				if (totalMatch == b.getTotalPerfectMatches()) {
					break;
				}
			}
		}
		LOGGER.debug("totalMatch {}", totalMatch );
		if (totalMatch >= b.getTotalPerfectMatches()) {
			LOGGER.debug("aggregated candidates {}", aggTerms);
			return aggTerms;
		} else {
			return null;
		}

	}

	private static void checkAggregatedBucket(HashMap<String, AggregatedBucket> bucketCombination,
			List<BucketTerms> combs, int index, long totalRows, List<BucketMetaData> metadata) {

		String comb = "";
		for (BucketTerms b : combs) {
			comb += b.getAttributeName() + " | ";
		}
		
		if (bucketCombination.containsKey(comb)) {
			LOGGER.debug("incremeating {}", comb);
			bucketCombination.get(comb).incrementCounts(totalRows);
			bucketCombination.get(comb).addIndex(index);
			bucketCombination.get(comb).incrementMetaData(metadata);

		} else {
			LOGGER.debug("adding {}", comb);

			AggregatedBucket b = new AggregatedBucket();
			b.setBucketTerms(new HashSet<BucketTerms>(combs));
			b.setMetadata(metadata);
			b.setCount(1);
			b.setFirstAppearance(index);
			b.addIndex(index);
			bucketCombination.put(comb, b);
		}
	}

}
