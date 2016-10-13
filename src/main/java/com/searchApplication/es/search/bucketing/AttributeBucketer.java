package com.searchApplication.es.search.bucketing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

public class AttributeBucketer {

	private static final Logger LOGGER = LoggerFactory.getLogger(AttributeBucketer.class);

	private static final String LOCATION_NAME = "location_name";
	private static final String LOCATIONS = "locations";
	private static final int HITS_IN_SCROLL = 500;
	private static final String SEARCH_FIELD = "description.ngramed";
	private static final String N_GRAM_ANALYZER = "n_gram_analyzer";

	public static BucketResponseList generateBuckets( Client client, String index, String type, String query, int loops,
			int hitsInScroll )
	{
		long startTime = System.currentTimeMillis();
		List<Bucket> buckets = createBucketList(client, index, type, query, loops, hitsInScroll);
		long endTime = System.currentTimeMillis();
		System.out.println("Service took - " + (endTime - startTime) + " milliseconds to createBucketList");

		return BucketResponseList.buildFromBucketList(buckets, query);
	}

	public static List<Bucket> createBucketList( Client client, String index, String type, String query, int loops,
			int hitsInScroll )
	{

		LOGGER.debug("Start query ");

		long startTime = System.currentTimeMillis();

		SearchRequestBuilder srb = client.prepareSearch(index).setTypes(type).setQuery(generateQuery(query))
				.setFetchSource(new String[] { "attributes.attribute_value", "sector", "sub_sector", "super_region" },
						null)
				.setSize(HITS_IN_SCROLL).setScroll(new TimeValue(160000));
		System.out.println("fetch size :" + HITS_IN_SCROLL);

		long endTime = System.currentTimeMillis();
		System.out.println(
				"Service took - " + (endTime - startTime) + " milliseconds to get first set of results from scroll");

		int hitCounter = 0;
		System.out.println("Service took - " + (startTime - System.currentTimeMillis())
				+ " milliseconds to come here");
		SearchResponse sr = srb.get();
		System.out.println("Service took - " + (startTime - System.currentTimeMillis())
				+ " milliseconds to come here");
		LOGGER.debug(" query {}", srb.toString());
		List<Bucket> bucketList = new ArrayList<Bucket>();
		Set<String> hits = new HashSet<String>();
		Set<String> misses = new HashSet<String>();
		while( (hitCounter < HITS_IN_SCROLL * loops) && (sr.getHits().getHits().length > 0) )
		{
			System.out.println("Fetched size : " + sr.getHits().getHits().length);
			LOGGER.debug(" response {} {} {}", hitCounter, sr.getHits().getHits().length, sr.getTookInMillis());
			
			for( SearchHit hit : sr.getHits() )
			{
				try
				{
					Bucket b = processHitsToBuckets(hit, query, hits, misses);
					if( b != null )
					{
						if( bucketList.contains(b) )
						{
							bucketList.get(bucketList.indexOf(b)).incrementCount();
							bucketList.get(bucketList.indexOf(b)).addMetaData(b.getBucketMetaData().get(0));

						}
						else
						{
							bucketList.add(b);
						}
						hitCounter++;
					}
					else
						hitCounter++;
				}
				catch( Exception e )
				{
					LOGGER.error("Error processing row {}", e.getCause().getMessage());
					e.printStackTrace();
				}
			}
			if( hitCounter < HITS_IN_SCROLL * loops )
			{
				long startTim = System.currentTimeMillis();
				sr = client.prepareSearchScroll(sr.getScrollId()).setScroll(new TimeValue(160000)).get();
				long endTim = System.currentTimeMillis();
				System.out.println("Service took - " + (endTim - startTim)
						+ " milliseconds to get next set of results from scroll");
				System.out.println(hitCounter + "<" + HITS_IN_SCROLL * loops);
			}
		}

		Collections.sort(bucketList);
		LOGGER.debug(" list {}", bucketList);

		return bucketList;
	}

	private static Bucket processHitsToBuckets( SearchHit hit, String query, Set<String> checked, Set<String> misses )
	{
		List<String> bucketTerms = new ArrayList<String>();
		BucketMetaData metaData = new BucketMetaData((String) hit.getSource().get("super_region"),
				(String) hit.getSource().get("sector"), (String) hit.getSource().get("sub_sector"));
		Set<String> localOK = new HashSet<String>();
		for( Map<String, String> attributeData : (List<Map<String, String>>) hit.getSource().get("attributes") )
		{
			if( !misses.contains(attributeData.get("attribute_value")) )
			{
				bucketTerms.add(attributeData.get("attribute_value"));
			}
		}
		if( hit.getInnerHits().containsKey(LOCATIONS) )
		{
			for( SearchHit innerHit : hit.getInnerHits().get(LOCATIONS) )
			{
				if( !misses.contains(innerHit.getSource().get(LOCATION_NAME)) )
				{
					bucketTerms.add(innerHit.getSource().get(LOCATION_NAME) + "_LOC");
				}
			}
		}

		Bucket b = BucketBuilders.createFromQueryString(query, bucketTerms, checked);

		if( b != null )
		{
			for( String terms : bucketTerms )
			{
				if( !b.getBucketTerms().contains(terms) )
				{
					misses.add(terms);
				}
			}
			b.getBucketTerms().addAll(localOK);
			List<BucketMetaData> metaArray = new ArrayList<BucketMetaData>();
			metaArray.add(metaData);
			b.setBucketMetaData(metaArray);
		}
		return b;
	}

	private static QueryBuilder generateQuery( String query )
	{
		long startTime = System.currentTimeMillis();

		QueryInnerHitBuilder q = new QueryInnerHitBuilder();
		q.setFetchSource("location_name", null);
		q.setSize(10);
		return QueryBuilders.boolQuery()
				.should(QueryBuilders.queryStringQuery(query)
						.analyzer(
								N_GRAM_ANALYZER)
						.defaultField(SEARCH_FIELD))
				.should(QueryBuilders.nestedQuery(LOCATIONS,
						QueryBuilders.matchQuery("locations.location_name.shingled",
								query.toLowerCase().replaceAll("apple", "")).analyzer("shingle_analyzer"))
						.innerHit(new QueryInnerHitBuilder()));
	}
}
