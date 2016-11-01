package com.searchApplication.es.search.bucketing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.util.AttributeReflector;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.support.QueryInnerHitBuilder;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.searchApplication.es.entities.BucketResponseList;
import com.searchApplication.utils.StopWords;

public class AttributeBucketer {

	private static final String ATTRIBUTES_ATTRIBUTE_NAME_SHINGLED = "attributes.attribute_value.shingled";

	private static final String ATTRIBUTES = "attributes";

	private static final Logger LOGGER = LoggerFactory.getLogger(AttributeBucketer.class);

	private static final String LOCATION_NAME = "location_name";
	private static final String LOCATIONS = "locations";
	private static final int HITS_IN_SCROLL = 1000;
	private static final String SEARCH_FIELD = "description.ngramed";
	private static final String N_GRAM_ANALYZER = "n_gram_analyzer";

	public static BucketResponseList generateBuckets(Client client, String index, String type, String query, int loops,
			int hitsInScroll) {
		List<Bucket> buckets = createBucketList(client, index, type, query, loops, hitsInScroll);
		return BucketResponseList.buildFromBucketList(buckets, query);
	}

	public static List<Bucket> createBucketList(Client client, String index, String type, String query, int loops,
			int hitsInScroll) {

		hitsInScroll = correctLoops(query, hitsInScroll);
		LOGGER.debug("Fetch {} rows", hitsInScroll);
		LOGGER.debug("Start query ");
		SearchRequestBuilder srb = client.prepareSearch(index).setTypes(type).setQuery(generateQuery(query))
				.setFetchSource(new String[] { "sector", "sub_sector", "super_region" }, null).setSize(hitsInScroll)
				.setScroll(new TimeValue(160000));
		int hitCounter = 0;
		SearchResponse sr = srb.get();
		LOGGER.debug(" query {}", srb.toString());
		LOGGER.debug(" response {}", sr.toString());

		List<Bucket> bucketList = new LinkedList<Bucket>();
		while ((hitCounter < hitsInScroll * loops) && (sr.getHits().getHits().length > 0)) {
			LOGGER.debug(" response {} {} {}", hitCounter, sr.getHits().getHits().length, sr.getTookInMillis());
			for (SearchHit hit : sr.getHits()) {
				try {
					Bucket b = processHitsToBuckets(hit, query, hitCounter);
					if (b != null) {
						if (bucketList.contains(b)) {
							bucketList.get(bucketList.indexOf(b)).incrementCount();
							bucketList.get(bucketList.indexOf(b)).addMetaData(b.getBucketMetaData().get(0));
						} else {
							bucketList.add(b);
						}
						hitCounter++;
					} else
						hitCounter++;
				} catch (Exception e) {
					LOGGER.error("Error processing row {}", e.getCause().getMessage());
					e.printStackTrace();
				}
			}
			if (hitCounter < hitsInScroll * loops) {
				sr = client.prepareSearchScroll(sr.getScrollId()).setScroll(new TimeValue(160000)).get();
			}
		}
		System.out.println(bucketList);
		Collections.sort(bucketList);
		LOGGER.debug(" list {}", bucketList);

		return bucketList;
	}

	private static int correctLoops(String query, int hits) {
		String[] queries = query.split(" ");
		int count = 0;
		for (String q : queries) {
			if (!StopWords.STOP_LIST.contains(q.toLowerCase())) {
				count++;
			}
		}

		return hits / count;
	}

	private static Bucket processHitsToBuckets(SearchHit hit, String query, int counter) {
		Set<String> bucketTerms = new HashSet<String>();
		BucketMetaData metaData = new BucketMetaData((String) hit.getSource().get("super_region"),
				(String) hit.getSource().get("sector"), (String) hit.getSource().get("sub_sector"));

		if (hit.getInnerHits().containsKey(ATTRIBUTES)) {
			for (SearchHit innerHit : hit.getInnerHits().get(ATTRIBUTES)) {
				bucketTerms.add((String) innerHit.getSource().get("attribute_value"));
			}
		}
		if (hit.getInnerHits().containsKey(LOCATIONS)) {
			for (SearchHit innerHit : hit.getInnerHits().get(LOCATIONS)) {
				bucketTerms.add(innerHit.getSource().get(LOCATION_NAME) + "_LOC");
			}
		}

		Bucket b = new Bucket(bucketTerms, Integer.MAX_VALUE - counter, 0, bucketTerms.size() / 3);
		if (bucketTerms.size() > 0) {
			List<BucketMetaData> metaArray = new ArrayList<BucketMetaData>();
			metaArray.add(metaData);
			b.setBucketMetaData(metaArray);
		}
		return b;
	}

	private static QueryBuilder generateQuery(String query) {

		QueryInnerHitBuilder q = new QueryInnerHitBuilder();
		q.setFetchSource("location_name", null);
		q.setSize(10);

		QueryInnerHitBuilder qi = new QueryInnerHitBuilder();
		qi.setFetchSource("attribute_value", null);
		qi.setSize(50);

		QueryBuilder b = QueryBuilders.boolQuery().must(
				QueryBuilders
						.nestedQuery(ATTRIBUTES, QueryBuilders.boolQuery()
								.must(QueryBuilders.queryStringQuery(query).field(ATTRIBUTES_ATTRIBUTE_NAME_SHINGLED)
										.analyzer("shingle_analyzer").boost(10))
								.must(QueryBuilders.queryStringQuery(query).field("attributes.attribute_value.ngramed")
										.analyzer("n_gram_analyzer")))
						.innerHit(qi).scoreMode("sum"))
				.should(QueryBuilders.nestedQuery(LOCATIONS, QueryBuilders.termsQuery("locations.location_name.raw",
						query.toLowerCase().replaceAll("apple", "").split(" "))).innerHit(q));
		return b;
	}

}
