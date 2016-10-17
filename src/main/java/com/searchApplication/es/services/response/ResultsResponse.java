package com.searchApplication.es.services.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.bucket.nested.InternalNested;
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import com.searchApplication.entities.QueryResults;
import com.searchApplication.entities.QueryResultsList;
import com.searchApplication.entities.Results;

public class ResultsResponse {

	public static QueryResultsList getResults( SearchResponse tFdocs, Map<String, Set<String>> locationMap,
			String stratumName ) throws Exception
	{
		QueryResultsList response = new QueryResultsList();
		List<QueryResults> results = new ArrayList<>();
		int length = 9999;
		if( stratumName.contains("*") && !stratumName.replaceAll("\\*", "").isEmpty() )
		{
			length = Integer.parseInt(stratumName.replaceAll("\\*", ""));
		}

		String locationType = "Country";
		if( locationMap != null && locationMap.keySet() != null )
		{
			if( locationMap.containsKey("State") )
			{
				locationType = "State";
			}
			if( locationMap.containsKey("County") )
			{
				locationType = "County";
			}
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
					int lengthTemp = length;
					ReverseNested reverseDb = dbpropertiesBucket.getAggregations().get("dbReverse");
					InternalNested locations = reverseDb.getAggregations().get("locations");
					Terms locationTypeBuckets = locations.getAggregations().get("locationType");
					for( Terms.Bucket locationTypeBucket : locationTypeBuckets.getBuckets() )
					{
						if( locationType.equals(locationTypeBucket.getKeyAsString()) )
						{
							Terms locationParentBuckets = locationTypeBucket.getAggregations().get("locationParent");
							for( Terms.Bucket locationParentBucket : locationParentBuckets.getBuckets() )
							{
								Terms locationname = locationParentBucket.getAggregations().get("locationname");
								for( Terms.Bucket locationnameBucket : locationname.getBuckets() )
								{
									QueryResults qr = new QueryResults();
									qr.setDbName(dbNameBucket.getKeyAsString());
									qr.setPropertyId(new Long(dbpropertiesBucket.getKeyAsString()));

									Map<String, String> stratums = new HashMap<>();
									Boolean stratum = false;
									Boolean idsAdded = false;
									if( locationMap != null && locationMap.keySet() != null
											&& locationMap.get(locationTypeBucket.getKeyAsString()) != null
											&& locationMap.get(locationTypeBucket.getKeyAsString())
													.contains(locationnameBucket.getKeyAsString())
											&& locationMap.get("parent")
													.contains(locationParentBucket.getKeyAsString()) )
									{
										Results idDetails = new Results();

										if( locationTypeBucket.getKeyAsString().equals("Country") )
										{
											idDetails.setSuperRegion(locationParentBucket.getKeyAsString());
											idDetails.setCountry(locationnameBucket.getKeyAsString());
										}
										else if( locationTypeBucket.getKeyAsString().equals("State") )
										{
											idDetails.setCountry(locationParentBucket.getKeyAsString());
											idDetails.setState(locationnameBucket.getKeyAsString());
										}
										else if( locationTypeBucket.getKeyAsString().equals("County") )
										{
											idDetails.setState(locationParentBucket.getKeyAsString());
											idDetails.setCounty(locationnameBucket.getKeyAsString());
										}

										Terms locationid = locationnameBucket.getAggregations().get("locationid");
										List<Long> ids = new ArrayList<>();
										for( Terms.Bucket locationidBucket : locationid.getBuckets() )
										{
											if( lengthTemp > 0 )
											{
												ids.add(new Long(locationidBucket.getKeyAsString()));
												lengthTemp--;
												idsAdded = true;
											}

											if( !stratum )
											{
												ReverseNested locReverse = locationidBucket.getAggregations()
														.get("locReverse");
												InternalNested attributes = locReverse.getAggregations()
														.get("attributes");
												Terms attTypes = attributes.getAggregations().get("attTypes");
												for( Terms.Bucket attTypesBucket : attTypes.getBuckets() )
												{
													Terms attributesValues = attTypesBucket.getAggregations()
															.get("attributesValues");
													for( Terms.Bucket attributesValuesBucket : attributesValues
															.getBuckets() )
													{
														stratums.put(attTypesBucket.getKeyAsString(),
																attributesValuesBucket.getKeyAsString());
													}
												}
												stratum = true;
											}
										}
										if( idsAdded )
										{
											idDetails.setSeriesId(ids);
											qr.setStratums(stratums);
											qr.setData(idDetails);
											results.add(qr);
										}
									}
									else if( locationMap == null || locationMap.keySet() == null )
									{
										Results idDetails = new Results();

										if( locationTypeBucket.getKeyAsString().equals("Country") )
										{
											idDetails.setSuperRegion(locationParentBucket.getKeyAsString());
											idDetails.setCountry(locationnameBucket.getKeyAsString());
										}
										else if( locationTypeBucket.getKeyAsString().equals("State") )
										{
											idDetails.setCountry(locationParentBucket.getKeyAsString());
											idDetails.setState(locationnameBucket.getKeyAsString());
										}
										else if( locationTypeBucket.getKeyAsString().equals("County") )
										{
											idDetails.setState(locationParentBucket.getKeyAsString());
											idDetails.setCounty(locationnameBucket.getKeyAsString());
										}

										Terms locationid = locationnameBucket.getAggregations().get("locationid");
										List<Long> ids = new ArrayList<>();
										for( Terms.Bucket locationidBucket : locationid.getBuckets() )
										{
											if( lengthTemp > 0 )
											{
												ids.add(new Long(locationidBucket.getKeyAsString()));
												lengthTemp--;
												idsAdded = true;
											}

											if( !stratum )
											{
												ReverseNested locReverse = locationidBucket.getAggregations()
														.get("locReverse");
												InternalNested attributes = locReverse.getAggregations()
														.get("attributes");
												Terms attTypes = attributes.getAggregations().get("attTypes");
												for( Terms.Bucket attTypesBucket : attTypes.getBuckets() )
												{
													Terms attributesValues = attTypesBucket.getAggregations()
															.get("attributesValues");
													for( Terms.Bucket attributesValuesBucket : attributesValues
															.getBuckets() )
													{
														stratums.put(attTypesBucket.getKeyAsString(),
																attributesValuesBucket.getKeyAsString());
													}
												}
												stratum = true;
											}
										}
										if( idsAdded )
										{
											idDetails.setSeriesId(ids);
											qr.setStratums(stratums);
											qr.setData(idDetails);
											results.add(qr);
										}
									}

								}
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
