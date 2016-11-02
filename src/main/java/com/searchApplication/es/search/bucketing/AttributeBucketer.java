package com.searchApplication.es.search.bucketing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.support.QueryInnerHitBuilder;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.searchApplication.es.entities.BucketResponseList;
import com.searchApplication.utils.StopWords;

public class AttributeBucketer {

	private static final String ATTRIBUTES_ATTRIBUTE_VALUE_NGRAMED = "attributes.attribute_value.ngramed";

	private static final String ATTRIBUTES_ATTRIBUTE_NAME_SHINGLED = "attributes.attribute_value.shingled";

	private static final String ATTRIBUTES = "attributes";

	private static final Logger LOGGER = LoggerFactory.getLogger(AttributeBucketer.class);

	private static final String LOCATION_NAME = "location_name";
	private static final String LOCATIONS = "locations";
	private static final int HITS_IN_SCROLL = 1000;
	private static final String N_GRAM_ANALYZER = "n_gram_analyzer";

	public static BucketResponseList generateBuckets(Client client, String index, String type, String query, int loops,
			int hitsInScroll, Set<String> locations) {
		List<Bucket> buckets = createBucketList(client, index, type, query, loops, hitsInScroll, locations);
		return BucketResponseList.buildFromBucketList(buckets, query);
	}

	public static List<Bucket> createBucketList(Client client, String index, String type, String query, int loops,
			int hitsInScroll, Set<String> locations) {

		hitsInScroll = correctLoops(query, hitsInScroll);
		LOGGER.debug("Fetch {} rows", hitsInScroll);
		LOGGER.debug("Start query ");
		SearchRequestBuilder srb = client.prepareSearch(index).setTypes(type)
				.setFetchSource(new String[] { "sector", "sub_sector", "super_region" }, null).setSize(hitsInScroll)
				.setScroll(new TimeValue(160000));
		srb = generateQuery(srb, generateAttAndLocQueries(query, locations));
		int hitCounter = 0;
		SearchResponse sr = srb.get();
		LOGGER.debug(" query {}", srb.toString());
		LOGGER.debug(" response {}", sr.toString());
		long totalBucektTime = 0;
		List<Bucket> bucketList = new LinkedList<Bucket>();
		while ((hitCounter < hitsInScroll * loops) && (sr.getHits().getHits().length > 0)) {
			LOGGER.info(" response {} {} {}", hitCounter, sr.getHits().getHits().length, sr.getTookInMillis());
			long bucketTime = System.currentTimeMillis();

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
					e.printStackTrace();
				}
			}
			totalBucektTime = System.currentTimeMillis() - bucketTime;

			if (hitCounter < hitsInScroll * loops || hitCounter == sr.getHits().getTotalHits()) {

				sr = client.prepareSearchScroll(sr.getScrollId()).setScroll(new TimeValue(160000)).get();
			}
		}
		Collections.sort(bucketList);

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
		Set<String> bucketTerms = new LinkedHashSet<String>();
		BucketMetaData metaData = createBucketMetaData(hit);
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

		if (bucketTerms.size() > 0) {
			Bucket b = new Bucket(bucketTerms, Integer.MAX_VALUE - counter, 0, bucketTerms.size() / 3);
			List<BucketMetaData> metaArray = new ArrayList<BucketMetaData>();
			metaArray.add(metaData);
			b.setBucketMetaData(metaArray);
			return b;
		} else {
			return null;
		}

	}

	private static BucketMetaData createBucketMetaData(SearchHit hit) {
		String superRegion = hit.getSource().containsKey("super_region") ? (String) hit.getSource().get("super_region")
				: "";
		String sector = hit.getSource().containsKey("sector") ? (String) hit.getSource().get("sector") : "";
		String subSector = hit.getSource().containsKey("sub_sector") ? (String) hit.getSource().get("sub_sector") : "";
		BucketMetaData b = new BucketMetaData(superRegion, sector, subSector);

		return b;
	}

	private static String[] generateAttAndLocQueries(String query, Set<String> locations) {
		String loc = "";
		String atts = "";
		String[] splits = query.split(" ");
		for (int i = 0; i < splits.length; i++) {

			if (locations.contains(splits[i])) {
				loc += splits[i] + " ";
			} else if (splits.length > i + 1 && locations.contains(splits[i] + " " + splits[i + 1])) {
				loc += splits[i] + " " + splits[i + 1];
				i++;
			} else {
				atts += splits[i] + " ";
			}

		}
		return new String[] { atts, loc };
	}

	private static SearchRequestBuilder generateQuery(SearchRequestBuilder srb, String[] query) {

		BoolQueryBuilder bool = QueryBuilders.boolQuery();
		if (!query[0].equals("")) {
			QueryInnerHitBuilder qi = new QueryInnerHitBuilder();
			qi.setFetchSource("attribute_value", null);
			qi.setSize(50);
			QueryBuilder attQuery = QueryBuilders
					.nestedQuery(ATTRIBUTES,
							QueryBuilders.boolQuery().must(QueryBuilders.queryStringQuery(query[0])
									.field(ATTRIBUTES_ATTRIBUTE_NAME_SHINGLED).analyzer("shingle_analyzer").boost(10))
									.should(QueryBuilders.queryStringQuery(query[0])
											.field(ATTRIBUTES_ATTRIBUTE_VALUE_NGRAMED).analyzer(N_GRAM_ANALYZER)))
					.innerHit(qi).scoreMode("avg");
			bool.must(attQuery);
			srb.setQuery(bool);
		}
		if (!query[1].equals("")) {
			QueryInnerHitBuilder q = new QueryInnerHitBuilder();
			q.setFetchSource("location_name", null);
			q.setSize(10);
			QueryBuilder b = QueryBuilders
					.nestedQuery(LOCATIONS, QueryBuilders.termsQuery("locations.location_name.raw", query[1].trim()))
					.innerHit(q);

			srb.setPostFilter(b);

		}
		return srb;
	}

}
