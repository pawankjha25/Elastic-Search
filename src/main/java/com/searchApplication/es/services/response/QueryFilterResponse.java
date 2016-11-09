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
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;
import com.google.gson.Gson;
import com.searchApplication.entities.FilterRequest;
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
			/* ReverseNested reverseAtt = nestedAttributes.getAggregations().get("attReverse");
			 * InternalNested locations = reverseAtt.getAggregations().get("locations"); ValueCount
			 * locationIds = locations.getAggregations().get("locationid");
			 * response.setTotalSeriesIds(locationIds.getValue()); */ response.setStratum(stratum);
			response.setStratumList(stratumList);
		}
		catch( Exception e )
		{
			throw e;
		}
		return response;
	}

	public static SearchOutput getLocationAggregation( SearchResponse tFdocs, Map<String, Set<String>> map,
			Map<String, Set<String>> mapParents, FilterRequest request ) throws Exception
	{
		long seriesIdCount = 0;
		SearchOutput response = new SearchOutput();
		Map<String, Set<LocationAggrigation>> locationBucket = new HashMap<String, Set<LocationAggrigation>>();
		Map<String, Set<LocationAggrigation>> locationBucketFinal = new HashMap<String, Set<LocationAggrigation>>();
		try
		{
			InternalNested location_terms = tFdocs.getAggregations().get("locations");

			Terms locType = location_terms.getAggregations().get("locationType");
			Collection<Terms.Bucket> locTypeBuckets = locType.getBuckets();
			for( Terms.Bucket locTypeBucket : locTypeBuckets )
			{
				System.out.println("Type -----------------" + locTypeBucket.getKeyAsString());
				Terms locationParent = locTypeBucket.getAggregations().get("locationParent");
				Collection<Terms.Bucket> locParentBuckets = locationParent.getBuckets();

				Set<LocationAggrigation> locationList = new TreeSet<LocationAggrigation>();
				for( Terms.Bucket locParentBucket : locParentBuckets )
				{
					if( (mapParents != null && mapParents.get("all").contains(locParentBucket.getKeyAsString()))
							|| (mapParents == null || mapParents.isEmpty()) )
					{
						System.out.println(locParentBucket.getKeyAsString());
						LocationAggrigation loc = new LocationAggrigation();
						loc.setLocationParent(locParentBucket.getKeyAsString());

						if( request.getLocationLevels() != null && !request.getLocationLevels().isEmpty() )
						{
							if( request.getLocationLevels().containsKey(locTypeBucket.getKeyAsString()) )
							{
								loc.setLevel(request.getLocationLevels().get(locTypeBucket.getKeyAsString()));
							}
							else
							{
								loc.setLevel(0);
							}
						}

						Terms superregion = locParentBucket.getAggregations().get("locationName");
						Collection<Terms.Bucket> buckets6 = superregion.getBuckets();
						Set<String> locationName = new TreeSet<String>();
						long seriesIds = 0;
						for( Terms.Bucket bucket6 : buckets6 )
						{
							locationName.add(bucket6.getKeyAsString());

							ValueCount locationIds = bucket6.getAggregations().get("locationid");
							if( map != null && map.get(locTypeBucket.getKeyAsString()) != null
									&& mapParents.get("all").contains(bucket6.getKeyAsString()) )
							{
								seriesIds = seriesIds + locationIds.getValue();
							}
							else if( map != null && map.get(locTypeBucket.getKeyAsString()) == null )
							{
								seriesIds = seriesIds + locationIds.getValue();
							}
						}
						if( locationName != null && !locationName.isEmpty() )
						{
							loc.setLocations(locationName);
							loc.setSeriesIds(seriesIds);
							locationList.add(loc);
						}
					}
				}
				locationBucket.put(locTypeBucket.getKeyAsString(), locationList);
				locationBucketFinal.put(locTypeBucket.getKeyAsString(), locationList);
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
									newBucket.setSeriesIds(bucket.getSeriesIds());
									newBucket.setLevel(bucket.getLevel());
									newBuckets.add(newBucket);
								}
							}
						}
					}
					locationBucket.put(locationType, newBuckets);
					locationBucketFinal.put(locationType, newBuckets);
				}
			}

			long currentLevelBucketIds = 0;
			long nextLevelBucketIds = 0;
			int level = -1;
			for( String key : locationBucket.keySet() )
			{
				for( LocationAggrigation locAgg : locationBucket.get(key) )
				{
					if( locAgg.getLevel() > level && locAgg.getLevel() != 0 )
					{
						level = locAgg.getLevel();
						currentLevelBucketIds = locAgg.getSeriesIds();
						seriesIdCount = currentLevelBucketIds;
					}
					else if( locAgg.getLevel() == 0 )
					{
						nextLevelBucketIds = nextLevelBucketIds + locAgg.getSeriesIds();
					}
				}
			}

			if( nextLevelBucketIds < currentLevelBucketIds )
			{
				for( String key : locationBucketFinal.keySet() )
				{
					for( LocationAggrigation locAgg : locationBucketFinal.get(key) )
					{
						if( locAgg.getLevel() == 0 )
						{
							Set<LocationAggrigation> newAggList = new TreeSet<>();
							locAgg.getLocations().add("OVERALL");
							newAggList.add(locAgg);
							locationBucketFinal.put(key, newAggList);
						}
					}
				}
			}
			response.setLocations(locationBucket);
			response.setTotalSeriesIds(seriesIdCount);
		}
		catch( Exception e )
		{
			throw e;
		}
		return response;
	}
}
