package com.searchApplication.es.services.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import com.google.gson.Gson;
import com.searchApplication.entities.FilterRequest;
import com.searchApplication.entities.LocationAggrigation;
import com.searchApplication.entities.SearchOutput;
import com.searchApplication.es.aggregations.FilterAggregation;
import com.searchApplication.es.queries.FilterQuery;
import com.searchApplication.es.services.response.QueryFilterResponse;

public class Filtering {

	public static SearchOutput getFilteringResults( FilterRequest request, String indexName, String objectType,
			Client client ) throws Exception
	{
		SearchOutput response = new SearchOutput();
		BoolQueryBuilder booleanQuery = null;
		try
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
			tFdocs = client.prepareSearch(indexName).setSize(0).setTypes(objectType).setQuery(booleanQuery)
					.addAggregation(FilterAggregation.getAggregation()).execute().actionGet();

			response = QueryFilterResponse.getResponse(tFdocs);

			Map<String, List<String>> stratum = new HashMap<>();

			if( request.getReqAttList() != null && !request.getReqAttList().isEmpty() && response.getStratum() != null
					&& !response.getStratum().isEmpty() && response.getStratum().keySet() != null )
			{
				BoolQueryBuilder booleanQuery1 = FilterQuery.getNotQuery(request, request.getReqAttList());
				long hits = client.prepareSearch(indexName).setTypes(objectType).setQuery(booleanQuery1).execute()
						.actionGet().getHits().getTotalHits();
				Iterator<String> keys = response.getStratum().keySet().iterator();
				while( keys.hasNext() )
				{
					String key = keys.next();
					if( response.getStratum().get(key).size() <= 1 )
					{
						stratum.put(key, response.getStratum().get(key));
					}
					else if( request.getReqAttList().equals(key) )
					{
						if( hits > 0 )
						{
							response.getStratum().get(key).add("NULL");
						}
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

				SearchResponse tFdocs1 = null;
				tFdocs1 = client.prepareSearch(indexName).setSize(0).setTypes(objectType).setQuery(booleanQuery)
						.addAggregation(FilterAggregation.getLocationAggregation()).execute().actionGet();

				SearchOutput res = QueryFilterResponse.getLocationAggregation(tFdocs1, request.getLocations(),
						getLocationMap(request.getLocations()), request);

				Map<String, Set<LocationAggrigation>> loc = res.getLocations();

				if( res != null && res.getTotalSeriesIds() > 0 )
				{
					response.setTotalSeriesIds(res.getTotalSeriesIds());
				}
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
							else
							{
								newLocAgg.add(locationDetails);
								newLoc.put(keys, newLocAgg);
							}
						}
					}

					response.setLocations(newLoc);
				}
				else
				{
					Map<String, Set<LocationAggrigation>> newLoc = new HashMap<>();
					if( request.getLocations() == null || request.getLocations().keySet() == null
							|| request.getLocations().keySet().isEmpty() )
					{
						for( String keys : loc.keySet() )
						{
							if( keys.equalsIgnoreCase("Country") )
							{
								newLoc.put(keys, loc.get(keys));
								break;
							}
							else
							{
								Set<LocationAggrigation> locVal = loc.get(keys);
								for( LocationAggrigation l : locVal )
								{
									if( l.getLocationParent().equalsIgnoreCase("NULL") )
									{
										newLoc.put(keys, loc.get(keys));
										break;
									}
								}
							}
						}
						response.setLocations(newLoc);
					}
					else
					{
						for (String keys : loc.keySet()) {
							Set<LocationAggrigation> locVal = loc.get(keys);
							for (LocationAggrigation l : locVal) {
								if (!l.getLocationParent().equalsIgnoreCase("NULL")) {
									newLoc.put(keys, loc.get(keys));
									break;
								}
							}
						}
						response.setLocations(newLoc);
					}
					//response.setLocations(loc);
				}
			}
			response.setTotalRows(tFdocs.getHits().getTotalHits());

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
				Set<String> all = new TreeSet<>();
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
							all.add(locString[1]);
						}
					}
					res.put(locationType, loc);
				}
				res.put("parent", parent);
				res.put("all", all);
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
