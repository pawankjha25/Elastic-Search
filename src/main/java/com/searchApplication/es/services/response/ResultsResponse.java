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
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import com.searchApplication.entities.QueryResults;
import com.searchApplication.entities.QueryResultsList;
import com.searchApplication.entities.Results;
import com.searchApplication.es.entities.Data;

public class ResultsResponse {

	public static QueryResultsList getResults( SearchResponse tFdocs, Map<String, Set<String>> locationMap )
			throws Exception
	{
		QueryResultsList response = new QueryResultsList();
		Set<QueryResults> results = new TreeSet<QueryResults>();
		try
		{

			InternalNested attributes = tFdocs.getAggregations().get("attributes");
			Terms attTypes = attributes.getAggregations().get("attTypes");
			for( Terms.Bucket attTypeBucket : attTypes.getBuckets() )
			{
				if( attTypeBucket != null && attTypeBucket.getAggregations() != null
						&& attTypeBucket.getAggregations().get("attributesValues") != null )
				{
					Terms attValues = attTypeBucket.getAggregations().get("attributesValues");
					Collection<Bucket> attValuesBuckets = attValues.getBuckets();
					for( Terms.Bucket attValuesBucket : attValuesBuckets )
					{
						if( attValuesBucket.getKeyAsString() != null
								&& !attValuesBucket.getKeyAsString().equals("null") )
						{
							QueryResults qr = new QueryResults();
							List<Data> data = new ArrayList<Data>();
							Data d = new Data();
							Map<String, Set<Results>> seriesId = new HashMap();
							d.setDetails(attValuesBucket.getKeyAsString());

							ReverseNested reverseAtt = attValuesBucket.getAggregations().get("attReverse");
							InternalNested database = reverseAtt.getAggregations().get("database");
							Terms dbNameBuckets = database.getAggregations().get("dbname");

							for( Terms.Bucket dbNameBucket : dbNameBuckets.getBuckets() )
							{
								qr.setDbName(dbNameBucket.getKeyAsString());
								if( dbNameBucket != null && dbNameBucket.getAggregations() != null
										&& dbNameBucket.getAggregations().get("dbproperties") != null )
								{
									Terms db_properties = dbNameBucket.getAggregations().get("dbproperties");
									Collection<Bucket> dbPropertiesBuckets = db_properties.getBuckets();
									for( Terms.Bucket dbPropertiesBucket : dbPropertiesBuckets )
									{
										qr.setPropertyId(new Long(dbPropertiesBucket.getKeyAsString()));
										ReverseNested reverseDb = dbPropertiesBucket.getAggregations().get("dbReverse");
										InternalNested sectorTerms = reverseDb.getAggregations().get("locations");
										Terms locationTypeBuckets = sectorTerms.getAggregations().get("locationType");

										for( Terms.Bucket locationTypeBucket : locationTypeBuckets.getBuckets() )
										{
											Terms locationParentBuckets = locationTypeBucket.getAggregations()
													.get("locationParent");
											String locationType = locationTypeBucket.getKeyAsString();
											String locationName = "";
											String locationParent = "";
											for( Terms.Bucket locationParentBucket : locationParentBuckets
													.getBuckets() )
											{
												locationParent = locationParentBucket.getKeyAsString();
												Terms locationNameBuckets = locationParentBucket.getAggregations()
														.get("locationname");

												for( Terms.Bucket locationNameBucket : locationNameBuckets
														.getBuckets() )
												{

													locationName = locationNameBucket.getKeyAsString();
													if( locationMap != null && locationMap.keySet() != null )
													{
														if( locationMap.get(locationTypeBucket.getKeyAsString()) != null
																&& locationMap.get(locationTypeBucket.getKeyAsString())
																		.contains(locationNameBucket.getKeyAsString())
																&& locationMap.get("parent").contains(locationParent) )
														{
															Terms seriesIdBuckets = locationNameBucket.getAggregations()
																	.get("locationid");

															Set<Results> res = new TreeSet<>();
															Results result = new Results();
															result.setLocationName(locationName);
															result.setLocationParent(locationParent);
															for( Terms.Bucket seriesIdBucket : seriesIdBuckets
																	.getBuckets() )
															{
																result.setSeriesId(
																		new Long(seriesIdBucket.getKeyAsString()));
																res.add(result);
															}

															if( locationType != null
																	&& seriesId.get(locationType) != null )
															{
																res.addAll(seriesId.get(locationType));
																res.add(result);
															}
															seriesId.put(locationType, res);
														}
													}
													else
													{
														Terms seriesIdBuckets = locationNameBucket.getAggregations()
																.get("locationid");

														Set<Results> res = new TreeSet<>();
														Results result = new Results();
														result.setLocationName(locationName);
														result.setLocationParent(locationParent);
														for( Terms.Bucket seriesIdBucket : seriesIdBuckets
																.getBuckets() )
														{
															result.setSeriesId(
																	new Long(seriesIdBucket.getKeyAsString()));
															res.add(result);
														}

														if( locationType != null && seriesId.get(locationType) != null )
														{
															res.addAll(seriesId.get(locationType));
															res.add(result);
														}
														seriesId.put(locationType, res);
													}
												}
											}
										}
										d.setSeriesId(seriesId);
									}
								}
							}
							data.add(d);
							qr.setData(data);
							results.add(qr);
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
