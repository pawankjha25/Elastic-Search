package com.searchApplication.es.search.bucketing.bucketeers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.searchApplication.es.search.bucketing.Bucket;
import com.searchApplication.es.search.bucketing.BucketBuilders;
import com.searchApplication.es.search.bucketing.BucketMetaData;
import com.searchApplication.es.search.bucketing.BucketTerms;

public class ESAggregationBucketeer {

	private static final String LOCATION_CONTEXT = "_LOC";

	private static final Logger LOGGER = LoggerFactory.getLogger(ESAggregationBucketeer.class);

	public static List<Bucket> getBucketsFromSearchResponseWithAgg(SearchResponse sr, String[] querySplit,
			int hitsInScroll, int loops, Client client) {
		int hitCounter = 0;
		List<Bucket> bucketList = new ArrayList<Bucket>();
		Set<String> hits = new HashSet<String>();
		LOGGER.debug(" query {}", sr.toString());

		LOGGER.debug(" response {} {} {}", hitCounter, sr.getHits().getHits().length, sr.getTookInMillis());

		Iterator<org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket> buckets = ((StringTerms) sr
				.getAggregations().asMap().get("values")).getBuckets().iterator();

		while (buckets.hasNext()) {
			Terms.Bucket b = buckets.next();
			Bucket result = BucketBuilders.createFromQueryString(querySplit[0], Arrays.asList(b.getKeyAsString()),
					hits);
			if (result == null && querySplit[1].length() == 0) {
				continue;
			} else if (result == null && querySplit[1].length() > 0) {
				BucketTerms bts = new BucketTerms(querySplit[1].toUpperCase() + LOCATION_CONTEXT, 100, true, 0);
				result = new Bucket(new HashSet(Arrays.asList(bts)), 1, 1, 0);
			} else if (result != null && querySplit.length > 1 && querySplit[1].length() > 1) {
				BucketTerms bts = new BucketTerms(querySplit[1].toUpperCase() + LOCATION_CONTEXT, 100, true, 0);
				result.getBucketTerms().add(bts);

			}
			Iterator<org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket> sectorIt = ((StringTerms) b
					.getAggregations().asList().get(0)).getBuckets().iterator();
			List<BucketMetaData> metaDataList = new ArrayList<BucketMetaData>();
			while (sectorIt.hasNext()) {
				Terms.Bucket sectorBucket = sectorIt.next();
				Iterator<org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket> subSectorIT = ((StringTerms) sectorBucket
						.getAggregations().asList().get(0)).getBuckets().iterator();
				while (subSectorIT.hasNext()) {
					Terms.Bucket subSectorBucket = subSectorIT.next();
					Iterator<org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket> superRegionIT = ((StringTerms) subSectorBucket
							.getAggregations().asList().get(0)).getBuckets().iterator();

					while (superRegionIT.hasNext()) {
						Terms.Bucket regionBucket = superRegionIT.next();
						BucketMetaData metaData = new BucketMetaData(regionBucket.getKeyAsString(),
								sectorBucket.getKeyAsString(), subSectorBucket.getKeyAsString());
						metaData.setTotal(regionBucket.getDocCount());
						LOGGER.debug("adding metadata {}", metaData);
						metaDataList.add(metaData);
					}
				}
			}
			LOGGER.debug("bucket {}", result);
			result.setBucketMetaData(metaDataList);
			result.setTotalRows(b.getDocCount());
			bucketList.add(result);

		}

		Collections.sort(bucketList);
		LOGGER.debug(" list {}", bucketList);

		return bucketList;
	}
}
