package com.searchApplication.es.services.response;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.bucket.nested.InternalNested;
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
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
							data.setLocations(locationData);

						}
						boolean valid = true;
						if( location )
						{
							for( String locationType : locationMap.keySet() )
							{
								if( !locationType.equals("parent") && data.getLocations().get(locationType) != null )
								{
									String actualLocName = data.getLocations().get(locationType);
									if( !locationMap.get(locationType).contains(actualLocName) )
									{
										valid = false;
										break;
									}

								}
								else if( !locationType.equals("parent") )
								{
									valid = false;
									break;
								}
							}
						}
						if( results.size() < length && valid )
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
			response.setResults(results);
		}
		catch( Exception e )
		{
			throw e;
		}
		return response;
	}
}
