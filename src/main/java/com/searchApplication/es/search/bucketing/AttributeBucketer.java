package com.searchApplication.es.search.bucketing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.searchApplication.es.entities.BucketResponseList;

public class AttributeBucketer {

	private static final String LOCATION_CONTEXT = "_LOC";

	private static final String SHINGLE_ANALYZER = "shingle_analyzer";

	private static final Logger LOGGER = LoggerFactory.getLogger(AttributeBucketer.class);

	private static final String LOCATION_NAME = "location_name";
	private static final String LOCATIONS = "locations";
	private static final int HITS_IN_SCROLL = 500;
	private static final String SEARCH_FIELD = "description.shingled";
	private static final String N_GRAM_ANALYZER = "n_gram_analyzer";

	public static BucketResponseList generateBuckets(Client client, String index, String type, String query, int loops,
			int hitsInScroll, Set<String> locations) {
		List<Bucket> buckets = aggregateBuckets(
				createBucketList(client, index, type, query, loops, hitsInScroll, locations));
		return BucketResponseList.buildFromBucketList(buckets, query);
	}

	private static List<Bucket> aggregateBuckets(List<Bucket> buckets) {
		return Aggregator.generateAggregated(buckets);
	}

	private static SearchResponse hitEsSingle(Client client, String index, String type, int hitsInScroll,
			String[] querySplit) {
		SearchRequestBuilder srb = client.prepareSearch(index).setTypes(type.split(","))
				.setFetchSource(new String[] { "attributes.attribute_value", "sector", "sub_sector", "super_region" },
						null)
				.setSize(0).setScroll(new TimeValue(160000));
		srb = generateQuery(srb, querySplit);
		srb.addAggregation(generateAggregaion(querySplit[0]));
		LOGGER.debug(" query {}", srb.toString());

		SearchResponse sr = srb.get();
		LOGGER.debug(" resutls {}", sr.toString());

		return sr;
	}

	public static List<Bucket> createBucketList(Client client, String index, String type, String query, int loops,
			int hitsInScroll, Set<String> locations) {

		LOGGER.debug("Start query ");
		String[] querySplit = generateAttAndLocQueries(cleanQuery(query), locations, 1);
		List<Bucket> bucketList = getBucketsFromSearch(querySplit, hitsInScroll, loops, client, index, type);
		LOGGER.debug("Query {} split size {}", query, query.split(" ").length);
		if (bucketList.size() == 0 && query.split(" ").length == 1) {
			querySplit = generateAttAndLocQueries(cleanQuery(query), locations, 2);
			LOGGER.debug("Queries {}", Arrays.toString(querySplit));
			LOGGER.debug("Next one {}", querySplit[1].equals(""));
			if (!querySplit[1].equals("")) {
				bucketList = getBucketsFromSearch(querySplit, hitsInScroll, loops, client, index, type);
			}
		}
		return bucketList;
	}

	private static List<Bucket> getBucketsFromSearch(String[] querySplit, int hitsInScroll, int loops, Client client,
			String index, String type) {

		if (querySplit[0].split(" ").length == 1 && querySplit[1].split(" ").length <= 1) {
			SearchResponse sr = hitEsSingle(client, index, type, hitsInScroll, querySplit);
			List<Bucket> bucketList = getBucketsFromSearchResponseWithAgg(sr, querySplit, hitsInScroll, loops, client);
			return bucketList;
		} else {
			SearchResponse sr = hitEsMulti(client, index, type, hitsInScroll, querySplit);
			List<Bucket> bucketList = getBucketsFromSearchResponse(sr, querySplit, hitsInScroll, loops, client);
			return bucketList;
		}
	}

	private static List<Bucket> getBucketsFromSearchResponseWithAgg(SearchResponse sr, String[] querySplit,
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

	private static String cleanQuery(String query) {
		return query.toLowerCase().trim().replaceAll("\\p{P}", "").trim();
	}

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

	private static String[] generateAttAndLocQueries(String query, Set<String> locations, int attempt) {
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
		if (attempt == 1 && splits.length == 1 && !locations.equals("")) {
			atts = query;
			loc = "";
		}

		return new String[] { atts.trim(), loc.trim() };

	}

	private static AbstractAggregationBuilder generateAggregaion(String query) {

		return AggregationBuilders.terms("values").field("description.raw").size(2000)
				.subAggregation(AggregationBuilders.terms("sectors").field("sector")
						.subAggregation(AggregationBuilders.terms("sub").field("sub_sector")
								.subAggregation(AggregationBuilders.terms("region").field("super_region"))));

	}

	private static SearchRequestBuilder generateQuery(SearchRequestBuilder srb, String[] query) {

		BoolQueryBuilder bool = QueryBuilders.boolQuery();
		if (!query[0].equals("")) {
			QueryBuilder attQuery = QueryBuilders.queryStringQuery(query[0]).defaultField(SEARCH_FIELD);
			bool.must(attQuery);
			srb.setQuery(bool);
		}
		if (!query[1].equals("")) {
			QueryInnerHitBuilder q = new QueryInnerHitBuilder();
			q.setFetchSource("location_name", null);
			q.setSize(10);
			QueryBuilder b = QueryBuilders.nestedQuery(LOCATIONS,
					QueryBuilders.termsQuery("locations.location_name.raw", query[1].toUpperCase().trim()));
			srb.setPostFilter(b);

		}
		return srb;
	}

	private static SearchResponse hitEsMulti(Client client, String index, String type, int hitsInScroll,
			String[] querySplit) {
		SearchRequestBuilder srb = client.prepareSearch(index)
				.setTypes(type.split(","))
				.setFetchSource(new String[] { "attributes.attribute_value", "sector", "sub_sector", "super_region" },
						null)
				.setSize(hitsInScroll).setScroll(new TimeValue(160000));
		srb = generateQuery(srb, querySplit);
		LOGGER.debug(" query {}", srb.toString());

		SearchResponse sr = srb.get();
		return sr;
	}

	private static List<Bucket> getBucketsFromSearchResponse(SearchResponse sr, String[] querySplit, int hitsInScroll,
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
