package com.searchApplication.es.services.response;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.bucket.nested.InternalNested;
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import com.google.gson.Gson;
import com.searchApplication.entities.QueryResults;
import com.searchApplication.entities.QueryResultsList;
import com.searchApplication.entities.Results;

public class ResultsResponse {

	public static QueryResultsList getResults( SearchResponse tFdocs, Map<String, Set<String>> locationMap,
			String stratumName, Boolean location ) throws Exception
	{
		QueryResultsList response = new QueryResultsList();
		Set<QueryResults> results = new TreeSet<>();
		int length = 500;
		if( stratumName.contains("*") && !stratumName.replaceAll("\\*", "").isEmpty() )
		{
			length = Integer.parseInt(stratumName.replaceAll("\\*", ""));
		}

		try
		{
			Set<String> type=new TreeSet<>();

			InternalNested database = tFdocs.getAggregations().get("database");
			Terms dbName = database.getAggregations().get("dbname");
			for( Terms.Bucket dbNameBucket : dbName.getBuckets() )
			{
				Terms dbproperties = dbNameBucket.getAggregations().get("dbproperties");
				for( Terms.Bucket dbpropertiesBucket : dbproperties.getBuckets() )
				{
					ReverseNested reverseDb = dbpropertiesBucket.getAggregations().get("dbReverse");
					//get the stratums for the row
					Map<String, String> stratums = new HashMap<>();
					InternalNested attributes = reverseDb.getAggregations().get("attributes");
					Terms attTypes = attributes.getAggregations().get("attTypes");
					for( Terms.Bucket attTypesBucket : attTypes.getBuckets() )
					{
						Terms attributesValues = attTypesBucket.getAggregations().get("attributesValues");
						for( Terms.Bucket attributesValuesBucket : attributesValues.getBuckets() )
						{
							stratums.put(attTypesBucket.getKeyAsString(), attributesValuesBucket.getKeyAsString());
						}
					}

					if( location )
					{
						Map<Long, Results> mapData = new HashMap<>();
						InternalNested locations = reverseDb.getAggregations().get("locations");
						Terms locationParentBuckets = locations.getAggregations().get("locationParent");
						for( Terms.Bucket locationParentBucket : locationParentBuckets.getBuckets() )
						{
							Terms locationnameBuckets = locationParentBucket.getAggregations().get("locationname");
							for( Terms.Bucket locationnameBucket : locationnameBuckets.getBuckets() )
							{
								//System.out.println(locationnameBucket.getKeyAsString());
								Terms locationidBuckets = locationnameBucket.getAggregations().get("locationid");
								for( Terms.Bucket locationidBucket : locationidBuckets.getBuckets() )
								{
									Results data = null;
									Map<String, String> locationData = null;
									long seriesId = new Long(locationidBucket.getKeyAsString());
									if( mapData.containsKey(seriesId) )
									{
										data = mapData.get(seriesId);
										locationData = data.getLocations();
									}
									else
									{
										data = new Results();
										locationData = new HashMap<>();
										data.setSeriesId(new Long(locationidBucket.getKeyAsString()));
									}

									Terms locationTypeBuckets = locationidBucket.getAggregations().get("locationType");
									boolean valid = true;
									for( Terms.Bucket locationTypeBucket : locationTypeBuckets.getBuckets() )
									{
										if( (locationMap.get(locationTypeBucket.getKeyAsString()) != null
												&& locationMap.get(locationTypeBucket.getKeyAsString())
														.contains(locationnameBucket.getKeyAsString())
												|| locationMap.get(locationTypeBucket.getKeyAsString()) == null) )
										{
											type.add(locationTypeBucket.getKeyAsString());
											locationData.put(locationTypeBucket.getKeyAsString(),
													locationnameBucket.getKeyAsString());
										}
										else
										{
											valid = false;
										}
									}
									if( valid )
									{
										data.setLocations(locationData);
										mapData.put(seriesId, data);
									}
								}
							}
						}
						for( Long key : mapData.keySet() )
						{
							boolean valid = true;
							Results data = mapData.get(key);
							for( String locationType : locationMap.keySet() )
							{
								if( !locationType.equals("parent")
										&& data.getLocations().get(locationType) == null )
								{
									valid = false;
								}
							}
							if( results.size() < length && valid )
							{
								QueryResults qr = new QueryResults();
								qr.setDbName(dbNameBucket.getKeyAsString());
								qr.setPropertyId(new Long(dbpropertiesBucket.getKeyAsString()));
								qr.setStratums(stratums);
								qr.setData(data);
								results.add(qr);
							}
						}
					}
					else
					{
						InternalNested locations = reverseDb.getAggregations().get("locations");
						Terms locationidBuckets = locations.getAggregations().get("locationid");
						for( Terms.Bucket locationidBucket : locationidBuckets.getBuckets() )
						{
							Results data = new Results();
							Map<String, String> locationData = new HashMap<>();
							data.setSeriesId(new Long(locationidBucket.getKeyAsString()));

							Terms locationTypeBuckets = locationidBucket.getAggregations().get("locationType");
							for( Terms.Bucket locationTypeBucket : locationTypeBuckets.getBuckets() )
							{
								Terms locationname = locationTypeBucket.getAggregations().get("locationname");
								for( Terms.Bucket locationnameBucket : locationname.getBuckets() )
								{
									locationData.put(locationTypeBucket.getKeyAsString(),
											locationnameBucket.getKeyAsString());
								}

							}
							if( results.size() < length )
							{
								QueryResults qr = new QueryResults();
								qr.setDbName(dbNameBucket.getKeyAsString());
								qr.setPropertyId(new Long(dbpropertiesBucket.getKeyAsString()));
								qr.setStratums(stratums);
								data.setLocations(locationData);
								qr.setData(data);
								results.add(qr);
							}
							else
							{
								break;
							}
						}
					}
				}
			}
			response.setResults(results);
		}
		catch( Exception e )
		{
			throw e;
		}
		return response;
	}
}
