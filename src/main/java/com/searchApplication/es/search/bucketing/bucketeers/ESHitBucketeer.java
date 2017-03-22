package com.searchApplication.es.search.bucketing.bucketeers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.searchApplication.es.search.bucketing.Bucket;
import com.searchApplication.es.search.bucketing.BucketBuilders;
import com.searchApplication.es.search.bucketing.BucketMetaData;
import com.searchApplication.es.search.bucketing.BucketTerms;

public class ESHitBucketeer {
	private static final String LOCATION_CONTEXT = "_LOC";

	private static final Logger LOGGER = LoggerFactory.getLogger(ESAggregationBucketeer.class);

	private static Bucket processHitsToBuckets(SearchHit hit, String query, Set<String> checked, Set<String> misses) {
		List<String> bucketTerms = new ArrayList<String>();
		BucketMetaData metaData = new BucketMetaData((String) hit.getSource().get("super_region"),
				(String) hit.getSource().get("sector"), (String) hit.getSource().get("sub_sector"));
		if (hit.getSource() != null && hit.getSource().containsKey("attributes")) {
			try {
				for (Map<String, String> attributeData : (List<Map<String, String>>) hit.getSource()
						.get("attributes")) {
					if (!misses.contains(attributeData.get("attribute_value"))) {
						if (attributeData.get("attribute_value") != null) {
							bucketTerms.add(attributeData.get("attribute_value"));
						} else {
							LOGGER.debug("Attribute value is NULL");
						}
					}
				}
			} catch (Exception e) {
				LOGGER.debug("skipped attribute");
			}
		}
		Bucket b = null;
		if (!query.equals("")) {
			b = BucketBuilders.createFromQueryString(query, bucketTerms, checked);

		} else {
			b = new Bucket(new HashSet<BucketTerms>(), 0, 0, 0);
		}

		if (b != null) {

			List<BucketMetaData> metaArray = new ArrayList<BucketMetaData>();
			metaArray.add(metaData);
			b.setBucketMetaData(metaArray);
		}

		return b;
	}
	public static List<Bucket> getBucketsFromSearchResponse(SearchResponse sr, String[] querySplit, int hitsInScroll,
			int loops, Client client) {
		int hitCounter = 0;
		List<Bucket> bucketList = new ArrayList<Bucket>();
		Set<String> hits = new HashSet<String>();
		Set<String> misses = new HashSet<String>();
		while ((hitCounter < hitsInScroll * loops) && (sr.getHits().getHits().length > 0)) {
			LOGGER.debug(" query {}", sr.toString());

			LOGGER.debug(" response {} {} {}", hitCounter, sr.getHits().getHits().length, sr.getTookInMillis());
			for (SearchHit hit : sr.getHits()) {
				try {
					Bucket b = processHitsToBuckets(hit, querySplit[0], hits, misses);
					if (querySplit.length > 1 && querySplit[1].length() > 1) {
						BucketTerms bts = new BucketTerms(querySplit[1].toUpperCase() + LOCATION_CONTEXT, 100, true, 0);
						b.getBucketTerms().add(bts);

					} else if (b == null && querySplit[1].length() > 0) {
						BucketTerms bts = new BucketTerms(querySplit[1].toUpperCase() + LOCATION_CONTEXT, 100, true, 0);
						b = new Bucket(new HashSet(Arrays.asList(bts)), 1, 1, 0);

					}
					if (b != null) {
						if (bucketList.contains(b)) {
							bucketList.get(bucketList.indexOf(b)).incrementCount();
							if (b.getBucketMetaData() != null) {
								bucketList.get(bucketList.indexOf(b)).addMetaData(b.getBucketMetaData().get(0));
							}

						} else {
							bucketList.add(b);
						}
						hitCounter++;
					} else
						hitCounter++;
				} catch (Exception e) {
					LOGGER.debug("Error processing row {}", e.getCause().getMessage());
					LOGGER.debug("Hit Counter: " + hitCounter);
					e.printStackTrace();
				}
			}
			if (hitCounter < hitsInScroll) {
				break;
			}
			if (hitCounter < hitsInScroll * loops) {
				sr = client.prepareSearchScroll(sr.getScrollId()).setScroll(new TimeValue(160000)).get();
			}
		}

		Collections.sort(bucketList);
		LOGGER.debug(" list {}", bucketList);

		return bucketList;
	}
}
