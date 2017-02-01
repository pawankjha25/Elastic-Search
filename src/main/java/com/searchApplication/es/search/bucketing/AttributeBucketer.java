package com.searchApplication.es.search.bucketing;

import java.io.IOException;
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
import com.searchApplication.es.search.bucketing.bucketeers.ESAggregationBucketeer;
import com.searchApplication.es.search.bucketing.bucketeers.ESHitBucketeer;
import com.searchApplication.es.search.bucketing.wordnet.WordNetSynonims;

import edu.mit.jwi.morph.WordnetStemmer;

public class AttributeBucketer {

	private static final String LOCATION_CONTEXT = "_LOC";
	private static final Logger LOGGER = LoggerFactory.getLogger(AttributeBucketer.class);
	private static final String LOCATIONS = "locations";
	private static final String SEARCH_FIELD = "description.shingled";

	public static BucketResponseList generateBuckets(Client client, String index, String type, String query, int loops,
			int hitsInScroll, Set<String> locations) throws IOException {
		List<Bucket> buckets = aggregateBuckets(
				createBucketList(client, index, type, query, loops, hitsInScroll, locations));
		return BucketResponseList.buildFromBucketList(buckets, query);
	}

	private static List<Bucket> aggregateBuckets(List<Bucket> buckets) {
		return Aggregator.generateAggregated(buckets);
	}

	private static SearchResponse hitEsSingle(Client client, String index, String type, int hitsInScroll,
			String[] querySplit) {
		SearchRequestBuilder srb = client.prepareSearch(index).setTypes(type)
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
			int hitsInScroll, Set<String> locations) throws IOException {

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
			String index, String type) throws IOException {
		List<Bucket> bucketList;
		if (querySplit[0].split(" ").length == 1 && querySplit[1].split(" ").length <= 1) {
			SearchResponse sr = hitEsSingle(client, index, type, hitsInScroll, querySplit);
			bucketList = ESAggregationBucketeer.getBucketsFromSearchResponseWithAgg(sr, querySplit, hitsInScroll, loops,
					client);
		} else {
			SearchResponse sr = hitEsMulti(client, index, type, hitsInScroll, querySplit);
			bucketList = ESHitBucketeer.getBucketsFromSearchResponse(sr, querySplit, hitsInScroll, loops, client);

		}
		if (bucketList.size() > 0) {
			return bucketList;
		} else {
			return doSynonimSearch(querySplit, hitsInScroll, loops, client, index, type);
		}
	}

	private static List<Bucket> doSynonimSearch(String[] querySplit, int hitsInScroll, int loops, Client client,
			String index, String type) throws IOException {
		List<Bucket> bucketList = new ArrayList<Bucket>();

		List<List<String>> queries = new ArrayList<List<String>>();
		for (String q : querySplit[0].split(" ")) {
			if (q.length() > 2) {
				List<String> qs = WordNetSynonims.getSingleton().testDictionary(q);
				queries.add(qs);
			}
		}
		List<String> synonimQueries = new ArrayList<String>();
		Permuattions.generatePermutations(queries, synonimQueries, 0, "");
		int synCounter = 0;
		while (synCounter < synonimQueries.size()) {
			synCounter++;
			String[] synQueries = new String[2];
			synQueries[1] = querySplit[1];
			synQueries[0] = synonimQueries.get(synCounter).replaceAll("_", " ");
			bucketList = getBucketsFromSearch(synQueries, hitsInScroll, loops, client, index, type);
			if (bucketList.isEmpty()) {
				break;
			}

		}
		return bucketList;
	}

	private static String cleanQuery(String query) {
		return query.toLowerCase().trim().replaceAll("\\p{P}", "").trim();
	}

	private static String[] generateAttAndLocQueries(String query, Set<String> locations, int attempt) {
		String loc = "";
		String atts = "";
		String[] splits = query.split(" ");
		boolean lastLocation = false;
		for (int i = 0; i < splits.length; i++) {

			if (locations.contains(splits[i])) {
				if (!loc.equals("")) {
					atts = loc + " " + atts;
				}
				loc = splits[i] + " ";
				if (i == splits.length - 1) {
					lastLocation = true;
				}
			} else if (splits.length > i + 1 && locations.contains(splits[i] + " " + splits[i + 1])) {
				if (!loc.equals("")) {
					atts = loc + " " + atts;
				}
				loc = splits[i] + " " + splits[i + 1];
				i++;

				if (i == splits.length - 1) {
					lastLocation = true;
				}
			} else {
				atts += splits[i] + " ";
			}

		}
		if (attempt == 1 && splits.length == 1 && !locations.equals("")) {
			atts = query;
			loc = "";
		} else if (attempt == 1 && !lastLocation && !locations.equals("")) {
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
		SearchRequestBuilder srb = client.prepareSearch(index).setTypes(type)
				.setFetchSource(new String[] { "attributes.attribute_value", "sector", "sub_sector", "super_region" },
						null)
				.setSize(hitsInScroll).setScroll(new TimeValue(160000));
		srb = generateQuery(srb, querySplit);
		LOGGER.debug(" query {}", srb.toString());
		SearchResponse sr = srb.get();
		return sr;
	}

}
