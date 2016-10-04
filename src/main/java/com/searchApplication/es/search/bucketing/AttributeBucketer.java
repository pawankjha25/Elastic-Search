package com.searchApplication.es.search.bucketing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import com.searchApplication.es.entities.BucketResponseList;

public class AttributeBucketer {

	private static final String LOCATION_NAME = "location_name";
	private static final String LOCATIONS = "locations";
	private static final int HITS_IN_SCROLL = 100;
	private static final String SEARCH_FIELD = "description.ngramed";
	private static final String N_GRAM_ANALYZER = "n_gram_analyzer";

	public static BucketResponseList generateBuckets(Client client, String index, String type, String query,
			int loops) {
		List<Bucket> buckets = createBucketList(client, index, type, query, loops);
		return BucketResponseList.buildFromBucketList(buckets, query);
	}

	public static List<Bucket> createBucketList(Client client, String index, String type, String query, int loops) {

		SearchRequestBuilder srb = client.prepareSearch(index).setTypes(type).setQuery(generateQuery(query))
				.setFetchSource(new String[] { "description", "attributes.attribute_value", "sector", "sub_sector",
						"super_region", "locations.location_name" }, null)
				.setSize(HITS_IN_SCROLL).setScroll(new TimeValue(60000));
		int hitCounter = 0;
		SearchResponse sr = srb.get();
		List<Bucket> bucketList = new ArrayList<Bucket>();
		while (hitCounter < HITS_IN_SCROLL * loops && sr.getHits().getHits().length > 0) {
			for (SearchHit hit : sr.getHits()) {

				try {
					Bucket b = processHitsToBuckets(hit, query);

					if (b != null) {
						if (bucketList.contains(b)) {
							bucketList.get(bucketList.indexOf(b)).incrementCount();
							bucketList.get(bucketList.indexOf(b)).addMetaData(b.getBucketMetaData().get(0));

						} else {
							bucketList.add(b);
						}
						hitCounter++;
					}
				} catch (Exception e) {
					System.out.println("ERROR PROCESSING ROW");
					e.printStackTrace();
				}
			}
			sr = client.prepareSearchScroll(sr.getScrollId()).setScroll(new TimeValue(60000)).get();
		}

		Collections.sort(bucketList);
		return bucketList;
	}

	private static Bucket processHitsToBuckets(SearchHit hit, String query) {
		List<String> bucketTerms = new ArrayList<String>();
		BucketMetaData metaData = new BucketMetaData((String) hit.getSource().get("super_region"),
				(String) hit.getSource().get("sector"), (String) hit.getSource().get("sub_sector"));
		for (Map<String, String> attributeData : (List<Map<String, String>>) hit.getSource().get("attributes")) {
			bucketTerms.add(attributeData.get("attribute_value"));

		}
		if (hit.getSource().containsKey(LOCATIONS)) {
			for (Map<String, String> attributeData : (List<Map<String, String>>) hit.getSource().get(LOCATIONS)) {
				bucketTerms.add(attributeData.get(LOCATION_NAME) + "_LOC");

			}
		}
		Bucket b = BucketBuilders.createFromQueryString(query, bucketTerms);
		if (b != null) {
			List<BucketMetaData> metaArray = new ArrayList<BucketMetaData>();
			metaArray.add(metaData);
			b.setBucketMetaData(metaArray);
		}
		return b;
	}

	private static QueryBuilder generateQuery(String query) {
		return QueryBuilders.boolQuery()
				.should(QueryBuilders.queryStringQuery(query).analyzer(N_GRAM_ANALYZER).defaultField(SEARCH_FIELD))
				.should(QueryBuilders.nestedQuery(LOCATIONS,
						QueryBuilders.matchQuery("locations.location_name.shingles", query.toLowerCase().split(" "))
								.analyzer("shingle_analyzer")));
	}
}
