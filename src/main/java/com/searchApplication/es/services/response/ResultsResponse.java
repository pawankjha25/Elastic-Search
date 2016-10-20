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
								Terms locationidBuckets = locationnameBucket.getAggregations().get("locationid");
								for( Terms.Bucket locationidBucket : locationidBuckets.getBuckets() )
								{
									Results data = null;
									long seriesId = new Long(locationidBucket.getKeyAsString());
									if( mapData.containsKey(seriesId) )
									{
										data = mapData.get(seriesId);
									}
									else
									{
										data = new Results();
										data.setSeriesId(new Long(locationidBucket.getKeyAsString()));
									}

									Terms locationTypeBuckets = locationidBucket.getAggregations().get("locationType");
									for( Terms.Bucket locationTypeBucket : locationTypeBuckets.getBuckets() )
									{
										if( locationTypeBucket.getKeyAsString().equalsIgnoreCase("Country") )
										{
											data.setCountry(locationnameBucket.getKeyAsString());
										}
										else if( locationTypeBucket.getKeyAsString().equalsIgnoreCase("State") )
										{
											data.setState(locationnameBucket.getKeyAsString());
										}
										else if( locationTypeBucket.getKeyAsString().equalsIgnoreCase("County") )
										{
											data.setCounty(locationnameBucket.getKeyAsString());
										}
										else if( locationTypeBucket.getKeyAsString().equalsIgnoreCase("Zipcode") )
										{
											data.setZipcode(locationnameBucket.getKeyAsString());
										}

									}
									mapData.put(seriesId, data);
								}
							}
						}
						for( Long key : mapData.keySet() )
						{
							boolean valid = true;
							Results data = mapData.get(key);/*
							for( String locationType : locationMap.keySet() )
							{
								if( locationType.equalsIgnoreCase("Country") && (data.getCountry() == null
										|| !locationMap.get(locationType).contains(data.getCountry())) )
								{
									valid = false;
								}
								else if( locationType.equalsIgnoreCase("State") && (data.getState() == null
										|| !locationMap.get(locationType).contains(data.getState())) )
								{
									valid = false;
								}
								else if( locationType.equalsIgnoreCase("County") && (data.getCounty() == null
										|| !locationMap.get(locationType).contains(data.getCounty())) )
								{
									valid = false;
								}
							}*/
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
							data.setSeriesId(new Long(locationidBucket.getKeyAsString()));

							Terms locationTypeBuckets = locationidBucket.getAggregations().get("locationType");
							for( Terms.Bucket locationTypeBucket : locationTypeBuckets.getBuckets() )
							{
								Terms locationname = locationTypeBucket.getAggregations().get("locationname");
								for( Terms.Bucket locationnameBucket : locationname.getBuckets() )
								{
									if( locationTypeBucket.getKeyAsString().equalsIgnoreCase("Country") )
									{
										data.setCountry(locationnameBucket.getKeyAsString());
									}
									else if( locationTypeBucket.getKeyAsString().equalsIgnoreCase("State") )
									{
										data.setState(locationnameBucket.getKeyAsString());
									}
									else if( locationTypeBucket.getKeyAsString().equalsIgnoreCase("County") )
									{
										data.setCounty(locationnameBucket.getKeyAsString());
									}
									else if( locationTypeBucket.getKeyAsString().equalsIgnoreCase("Zipcode") )
									{
										data.setZipcode(locationnameBucket.getKeyAsString());
									}
								}

							}
							if( results.size() < length )
							{
								QueryResults qr = new QueryResults();
								qr.setDbName(dbNameBucket.getKeyAsString());
								qr.setPropertyId(new Long(dbpropertiesBucket.getKeyAsString()));
								qr.setStratums(stratums);
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
