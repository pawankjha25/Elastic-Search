package com.searchApplication.es.services.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import com.searchApplication.entities.FilterRequest;
import com.searchApplication.entities.QueryResultsList;
import com.searchApplication.es.aggregations.ResultsAggregation;
import com.searchApplication.es.queries.FilterQuery;
import com.searchApplication.es.services.response.ResultsResponse;

public class Results {

	@SuppressWarnings( "rawtypes" )
	public static QueryResultsList getResults( FilterRequest request, String indexName, String objectType,
			Client client ) throws Exception
	{
		QueryResultsList response = new QueryResultsList();
		Boolean location = false;
		try
		{
			BoolQueryBuilder booleanQuery = FilterQuery.getQuery(request);

			if( request.getLocations() != null && !request.getLocations().isEmpty()
					&& request.getLocations().keySet().size() > 0 )
			{
				location = true;
			}

			String[] locations = null;
			if( request.getLocations() != null && !request.getLocations().isEmpty() )
			{
				Set<String> locationsSet = getLocationList(request.getLocations(), 1);
				locations = new String[locationsSet.size()];
				int i = 0;
				for( String loc : locationsSet )
				{
					locations[i] = loc;
					i++;
				}
			}

			AggregationBuilder aggregation = ResultsAggregation.getAggregation(request.getStratumName(), location,
					locations);

			SearchResponse tFdocs = null;

			long startTime = System.currentTimeMillis();
			tFdocs = client.prepareSearch(indexName).setTypes(objectType).setQuery(booleanQuery).setSize(0)
					.addAggregation(aggregation).execute().actionGet();
			long endTime = System.currentTimeMillis();

			System.out.println("Service took - " + (endTime - startTime) + " milliseconds to query");

			response = ResultsResponse.getResults(tFdocs, getLocationMap(request.getLocations()),
					request.getStratumName(), location);
		}
		catch( Exception e )
		{
			throw e;
		}
		return response;

	}

	private static Map<String, Set<String>> getLocationMap( Map<String, Set<String>> map )
	{
		Map<String, Set<String>> res = new HashMap<>();
		try
		{
			if( map != null && !map.isEmpty() && map.keySet() != null )
			{
				Set<String> parent = new TreeSet<>();
				for( String locationType : map.keySet() )
				{
					Set<String> loc = new TreeSet<>();
					for( String locName : map.get(locationType) )
					{
						String[] locString = locName.split(":");
						if( locString[1] != null && locString[0] != null )
						{
							loc.add(locString[1]);
							parent.add(locString[0]);
						}
					}
					res.put(locationType, loc);
				}
				res.put("parent", parent);
			}
			else
			{
				return null;
			}
		}
		catch( Exception e )
		{
			throw e;
		}
		return res;
	}

	private static Set<String> getLocationList( Map<String, Set<String>> map, int loc )
	{
		Set<String> res = new TreeSet<>();
		try
		{
			if( map != null && !map.isEmpty() && map.keySet() != null )
			{
				for( String locationType : map.keySet() )
				{
					for( String locName : map.get(locationType) )
					{
						String[] locString = locName.split(":");
						if( locString[1] != null && locString[0] != null )
						{
							res.add(locString[loc]);
						}
					}
				}
			}
			else
			{
				System.out.println("returning null");
				return null;
			}
		}
		catch( Exception e )
		{
			throw e;
		}
		return res;
	}
}
