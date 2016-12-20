package com.searchApplication.es.search.bucketing;

import java.util.ArrayList;
import java.util.Arrays;
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
				checkAggregatedBucket(bucketCombination, l, i, buckets.get(i).getTotalRows());
			}

		}
		List<Integer> aggregatedBucketsList = new ArrayList<Integer>();
		for (Map.Entry<String, AggregatedBucket> entry : bucketCombination.entrySet()) {
			LOGGER.debug("ENTRY {}", entry);
			if (entry.getValue().getBuckets().size() > 1) {
				System.out.println("Comb " + entry.getKey() + "  " + entry.getValue().getBuckets());
				Bucket c = new Bucket(entry.getValue().getBucketTerms(),
						buckets.get(entry.getValue().getFirstAppearance()).getTotalPerfectMatches(), 0, 0);
				c.setTotalRows(entry.getValue().getCount());
				c.setBucketMetaData(buckets.get(entry.getValue().getFirstAppearance()).getBucketMetaData());
				aggregatedBuckets[entry.getValue().getFirstAppearance()] = c;
				aggregatedBucketsList.addAll(entry.getValue().getBuckets());

			}
		}
		for (int i = 0; i < buckets.size(); i++) {

			if (aggregatedBuckets[i] != null) {
				resultBuckets.add(aggregatedBuckets[i]);
			} else if (!aggregatedBucketsList.contains(i)) {
				resultBuckets.add(buckets.get(i));
			} 
		}
		return resultBuckets;
	}

	public static List<BucketTerms> generateCandidate(Bucket b) {
		int totalMatch = 0;
		Set<String> matched = new HashSet<String>();
		List<BucketTerms> aggTerms = new ArrayList<BucketTerms>();
		for (BucketTerms bt : b.getBucketTerms()) {
			LOGGER.debug("ENTRY {}", bt);

			boolean forAdd = false;
			// TODO this orders differently if there is a multi match bucket
			// after a single match attribute
			if (bt.getMatchedQueries().size() == b.getTotalPerfectMatches()) {
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
		if (totalMatch >= b.getTotalPerfectMatches()) {
			LOGGER.debug("aggregated candidates {}", aggTerms);

			return aggTerms;
		} else {
			return null;
		}

	}

	private static void checkAggregatedBucket(HashMap<String, AggregatedBucket> bucketCombination,
			List<BucketTerms> combs, int index, long totalRows) {

		String comb = "";
		for (BucketTerms b : combs) {
			comb += b.getAttributeName() + " | ";
		}
		
		if (bucketCombination.containsKey(comb)) {
			LOGGER.debug("incremeating {}", comb);
			bucketCombination.get(comb).incrementCounts(totalRows);
			bucketCombination.get(comb).addIndex(index);

		} else {
			LOGGER.debug("adding {}", comb);

			AggregatedBucket b = new AggregatedBucket();
			b.setBucketTerms(new HashSet<BucketTerms>(combs));
			b.setCount(1);
			b.setFirstAppearance(index);
			b.addIndex(index);
			bucketCombination.put(comb, b);
		}
	}

}
