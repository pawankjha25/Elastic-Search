package com.searchApplication.es.services.response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.bucket.nested.InternalNested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import com.google.gson.Gson;
import com.searchApplication.entities.LocationAggrigation;
import com.searchApplication.entities.SearchOutput;
import com.searchApplication.entities.Stratum;

public class QueryFilterResponse {

	public static SearchOutput getResponse( SearchResponse tFdocs ) throws Exception
	{
		SearchOutput response = new SearchOutput();
		Map<String, List<String>> stratum = new HashMap<String, List<String>>();
		Set<Stratum> stratumList = new TreeSet<Stratum>();
		try
		{
			InternalNested nestedAttributes = tFdocs.getAggregations().get("attributes");
			Terms attTypesTerms = nestedAttributes.getAggregations().get("attTypes");
			for( Terms.Bucket bucket : attTypesTerms.getBuckets() )
			{
				Stratum st = new Stratum();
				st.setStratumName(bucket.getKeyAsString());
				Terms levelBuckets = bucket.getAggregations().get("attLevel");
				for( Terms.Bucket levelBucket : levelBuckets.getBuckets() )
				{
					st.setLevel(levelBucket.getKeyAsString());
					if( levelBucket != null && levelBucket.getAggregations() != null
							&& levelBucket.getAggregations().get("attParent") != null )
					{
						Terms attParentTerm = levelBucket.getAggregations().get("attParent");
						Collection<Bucket> attParentBuckets = attParentTerm.getBuckets();
						for( Terms.Bucket attParentBucket : attParentBuckets )
						{
							st.setParent(attParentBucket.getKeyAsString());
							if( attParentBucket != null && attParentBucket.getAggregations() != null
									&& attParentBucket.getAggregations().get("attValues") != null )
							{
								List<String> stratumValues = new ArrayList<String>();
								Terms super_Sector_terms = attParentBucket.getAggregations().get("attValues");
								Collection<Bucket> buckets2 = super_Sector_terms.getBuckets();
								for( Terms.Bucket bucket2 : buckets2 )
								{
									stratumValues.add(bucket2.getKeyAsString());
								}
								stratum.put(bucket.getKeyAsString(), stratumValues);
								stratumList.add(st);
							}
						}
					}
				}

			}
			response.setStratum(stratum);
			response.setStratumList(stratumList);
		}
		catch( Exception e )
		{
			throw e;
		}
		return response;
	}

	public static Map<String, Set<LocationAggrigation>> getLocationAggregation( SearchResponse tFdocs,
			Map<String, Set<String>> map, Map<String, Set<String>> mapParents ) throws Exception
	{
		Map<String, Set<LocationAggrigation>> locationBucket = new HashMap<String, Set<LocationAggrigation>>();
		try
		{
			InternalNested location_terms = tFdocs.getAggregations().get("locations");

			Terms locType = location_terms.getAggregations().get("locationType");
			Collection<Terms.Bucket> locTypeBuckets = locType.getBuckets();
			for( Terms.Bucket locTypeBucket : locTypeBuckets )
			{
				Terms locationParent = locTypeBucket.getAggregations().get("locationParent");
				Collection<Terms.Bucket> locParentBuckets = locationParent.getBuckets();
				Set<LocationAggrigation> locationList = new TreeSet<LocationAggrigation>();
				for( Terms.Bucket locParentBucket : locParentBuckets )
				{
					LocationAggrigation loc = new LocationAggrigation();
					loc.setLocationParent(locParentBucket.getKeyAsString());

					Terms superregion = locParentBucket.getAggregations().get("locationName");
					Collection<Terms.Bucket> buckets6 = superregion.getBuckets();
					Set<String> locationName = new TreeSet<String>();
					for( Terms.Bucket bucket6 : buckets6 )
					{
						locationName.add(bucket6.getKeyAsString());
					}
					if( locationName != null && !locationName.isEmpty() )
					{
						loc.setLocations(locationName);
						locationList.add(loc);
					}
					locationBucket.put(locTypeBucket.getKeyAsString(), locationList);
				}
			}

			if( map != null && map.keySet() != null )
			{
				for( String locationType : map.keySet() )
				{
					Set<LocationAggrigation> newBuckets = new TreeSet<>();
					Set<LocationAggrigation> buckets = locationBucket.get(locationType);

					for( String locations : map.get(locationType) )
					{
						String[] location = locations.split(":");
						if( locationBucket.get(locationType) != null && location[0] != null
								&& !location[0].equals("null") )
						{
							for( LocationAggrigation bucket : buckets )
							{
								if( bucket.getLocationParent().equals(location[0]) )
								{
									LocationAggrigation newBucket = new LocationAggrigation();
									Set<String> names = new TreeSet<>();
									names.add(location[1]);
									newBucket.setLocationParent(bucket.getLocationParent());
									newBucket.setLocations(names);
									newBuckets.add(newBucket);
								}
							}
						}
					}
					locationBucket.put(locationType, newBuckets);
				}
				for( String locationType : locationBucket.keySet() )
				{
					if( !map.containsKey(locationType) )
					{
						Set<LocationAggrigation> listLoc = new TreeSet<>();
						for( LocationAggrigation loc : locationBucket.get(locationType) )
						{
							for( String key : mapParents.keySet() )
							{
								if( mapParents.get(key).contains(loc.getLocationParent()) )
								{
									listLoc.add(loc);
									break;
								}
							}

						}
						locationBucket.put(locationType, listLoc);

					}

				}
			}

		}
		catch( Exception e )
		{
			throw e;
		}
		return locationBucket;
	}

}
