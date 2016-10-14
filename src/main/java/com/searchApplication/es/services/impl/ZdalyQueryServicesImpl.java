package com.searchApplication.es.services.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Resource;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import com.searchApplication.entities.FilterRequest;
import com.searchApplication.entities.LocationAggrigation;
import com.searchApplication.entities.QueryResultsList;
import com.searchApplication.entities.SearchOutput;
import com.searchApplication.es.aggregations.FilterAggregation;
import com.searchApplication.es.aggregations.ResultsAggregation;
import com.searchApplication.es.entities.BucketResponseList;
import com.searchApplication.es.interfaces.ZdalyQueryServices;
import com.searchApplication.es.queries.FilterQuery;
import com.searchApplication.es.search.bucketing.AttributeBucketer;
import com.searchApplication.es.services.response.QueryFilterResponse;
import com.searchApplication.es.services.response.ResultsResponse;
import com.searchApplication.utils.ElasticSearchUtility;

@Service
public class ZdalyQueryServicesImpl implements ZdalyQueryServices {

	@Resource
	private Environment env;

	private static Client client = null;

	public ZdalyQueryServicesImpl()
	{
		ZdalyQueryServicesImpl.client = ElasticSearchUtility.addClient();
	}

	@Override
	public BucketResponseList produceBuckets( String queryText ) throws Exception
	{
		try
		{
			return AttributeBucketer.generateBuckets(client, env.getProperty("es.index_name"),
					env.getProperty("es.search_object"), queryText, 1, 1000);
		}
		catch( Exception e )
		{
			throw e;
		}
	}

	@Override
	public SearchOutput matchQuery( String queryText ) throws Exception
	{
		return null;
	}

	@Override
	public SearchOutput queryWithFilters( FilterRequest request ) throws Exception
	{
		SearchOutput response = new SearchOutput();
		BoolQueryBuilder booleanQuery = null;
		try
		{
			if( request.getSearchText() != null && !request.getSearchText().isEmpty() )
			{

				String[] queryString = request.getSearchText().trim().split("\\|");

				String location = "";
				for( String query : queryString )
				{
					if( query.contains("_LOC") || query.contains("_loc") )
					{
						location = query.replace("_LOC", "").replace("_loc", "");
					}
				}

				booleanQuery = FilterQuery.getQuery(request);

				SearchResponse tFdocs = null;
				tFdocs = client.prepareSearch(env.getProperty("es.index_name"))
						.setTypes(env.getProperty("es.search_object")).setQuery(booleanQuery)
						.addAggregation(FilterAggregation.getAggregation()).execute().actionGet();

				response = QueryFilterResponse.getResponse(tFdocs);

				Map<String, List<String>> stratum = new HashMap<>();

				if( request.getReqAttList() != null && !request.getReqAttList().isEmpty()
						&& response.getStratum() != null && !response.getStratum().isEmpty()
						&& response.getStratum().keySet() != null )
				{
					Iterator<String> keys = response.getStratum().keySet().iterator();
					while( keys.hasNext() )
					{
						String key = keys.next();
						if( response.getStratum().get(key).size() <= 1 || request.getReqAttList().contains(key) )
						{
							stratum.put(key, response.getStratum().get(key));
						}
					}
				}
				else
				{
					if( response != null && response.getStratum() != null && !response.getStratum().isEmpty() )
					{
						Iterator<String> keys = response.getStratum().keySet().iterator();
						if( keys != null )
						{
							while( keys.hasNext() )
							{
								String key = keys.next();
								// if (response.getStratum().get(key).size() <=
								// 1) {
								stratum.put(key, response.getStratum().get(key));
								// }
							}
						}
					}
				}

				response.setStratum(stratum);

				if( request.getLocation() )
				{
					tFdocs = client.prepareSearch(env.getProperty("es.index_name"))
							.setTypes(env.getProperty("es.search_object")).setQuery(booleanQuery)
							.addAggregation(FilterAggregation.getLocationAggregation()).execute().actionGet();

					Map<String, Set<LocationAggrigation>> loc = QueryFilterResponse.getLocationAggregation(tFdocs,
							request.getLocations());
					if( location != "" && !location.isEmpty() )
					{
						Map<String, Set<LocationAggrigation>> newLoc = new HashMap<>();

						for( String keys : loc.keySet() )
						{
							Set<LocationAggrigation> locAgg = loc.get(keys);
							Set<LocationAggrigation> newLocAgg = new TreeSet<>();

							for( LocationAggrigation locationDetails : locAgg )
							{
								if( locationDetails != null && locationDetails.getLocationParent() != null
										&& locationDetails.getLocationParent().equalsIgnoreCase(location) )
								{
									newLocAgg.add(locationDetails);
									newLoc.put(keys, newLocAgg);
								}
								else if( locationDetails != null && locationDetails.getLocations() != null
										&& !locationDetails.getLocations().isEmpty()
										&& (locationDetails.getLocations().contains(location.toLowerCase())
												|| locationDetails.getLocations().contains(location.toUpperCase())) )
								{
									LocationAggrigation newLocationDetails = new LocationAggrigation();
									newLocationDetails.setLocationParent(locationDetails.getLocationParent());
									Set<String> place = new TreeSet<>();
									place.add(location);
									newLocationDetails.setLocations(place);

									newLocAgg.add(newLocationDetails);
									newLoc.put(keys, newLocAgg);
								}
							}
						}

						response.setLocations(newLoc);
					}
					else
					{
						response.setLocations(loc);
					}
				}

			}

		}
		catch( Exception e )
		{
			throw e;
		}
		return response;
	}

	@SuppressWarnings( "rawtypes" )
	@Override
	public QueryResultsList queryResults( FilterRequest request ) throws Exception
	{
		QueryResultsList response = new QueryResultsList();
		BoolQueryBuilder booleanQuery = new BoolQueryBuilder();
		try
		{
			if( request.getSearchText() != null && !request.getSearchText().isEmpty() )
			{
				booleanQuery = FilterQuery.getQuery(request);

				AggregationBuilder aggregation = ResultsAggregation.getAggregation(request.getStratumName());

				SearchResponse tFdocs = null;

				tFdocs = client.prepareSearch(env.getProperty("es.index_name"))
						.setTypes(env.getProperty("es.search_object")).setQuery(booleanQuery).setSize(0)
						.addAggregation(aggregation).execute().actionGet();

				long start = System.currentTimeMillis();
				response = ResultsResponse.getResults(tFdocs, getLocationMap(request.getLocations()),
						request.getStratumName());
				long end = System.currentTimeMillis();
				System.out.println(end - start);

				/* tFdocs = client.prepareSearch(env.getProperty("es.index_name"))
				 * .setTypes(env.getProperty("es.search_object")).setQuery(booleanQuery)
				 * .addAggregation(FilterAggregation.getLocationAggregation()).execute().actionGet()
				 * ;
				 * 
				 * response.setLocations(QueryFilterResponse.getLocationAggregation(tFdocs,
				 * request.getLocations())); */
			}

		}
		catch( Exception e )
		{
			throw e;
		}
		return response;
	}

	private Map<String, Set<String>> getLocationMap( Map<String, Set<String>> map )
	{
		Map<String, Set<String>> res = new HashMap<>();
		try
		{
			if( map != null && map.keySet() != null )
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

}
