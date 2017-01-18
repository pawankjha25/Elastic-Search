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

	public static QueryResultsList getResults(SearchResponse tFdocs, Map<String, Set<String>> locationMap,
			String stratumName, Boolean location) throws Exception {
		QueryResultsList response = new QueryResultsList();
		Set<QueryResults> results = new TreeSet<>();
		int length = 500;
		if (stratumName.contains("*") && !stratumName.replaceAll("\\*", "").isEmpty()) {
			length = Integer.parseInt(stratumName.replaceAll("\\*", ""));
		}

		try {

			Map<String, Map<String, String>> mapStratums = new HashMap<>();

			InternalNested database = tFdocs.getAggregations().get("database");

			// get the attributes out of the results
			Terms dbName = database.getAggregations().get("dbname");
			for (Terms.Bucket dbNameBucket : dbName.getBuckets()) {
				Terms dbproperties = dbNameBucket.getAggregations().get("dbproperties");
				for (Terms.Bucket dbpropertiesBucket : dbproperties.getBuckets()) {
					ReverseNested reverseDb = dbpropertiesBucket.getAggregations().get("dbReverse");
					// get the stratums for the row
					Map<String, String> stratums = new HashMap<>();
					InternalNested attributes = reverseDb.getAggregations().get("attributes");
					Terms attTypes = attributes.getAggregations().get("attTypes");
					for (Terms.Bucket attTypesBucket : attTypes.getBuckets()) {
						Terms attributesValues = attTypesBucket.getAggregations().get("attributesValues");
						for (Terms.Bucket attributesValuesBucket : attributesValues.getBuckets()) {
							stratums.put(attTypesBucket.getKeyAsString(), attributesValuesBucket.getKeyAsString());
						}
					}
					mapStratums.put(dbNameBucket.getKeyAsString() + ":" + dbpropertiesBucket.getKeyAsString(),
							stratums);
				}
			}

			Terms dbNames = database.getAggregations().get("dbnames");
			for (Terms.Bucket dbNameBucket : dbNames.getBuckets()) {
				Terms dbproperties = dbNameBucket.getAggregations().get("dbproperties");
				for (Terms.Bucket dbpropertiesBucket : dbproperties.getBuckets()) {
					Map<String, Results> mapData = new HashMap<>();
					InternalNested locations = dbpropertiesBucket.getAggregations().get("locations");
					Terms locationParentBuckets = locations.getAggregations().get("locationParent");
					for (Terms.Bucket locationParentBucket : locationParentBuckets.getBuckets()) {
						Terms locationnameBuckets = locationParentBucket.getAggregations().get("locationname");
						for (Terms.Bucket locationnameBucket : locationnameBuckets.getBuckets()) {
							Terms locationidBuckets = locationnameBucket.getAggregations().get("locationid");
							for (Terms.Bucket locationidBucket : locationidBuckets.getBuckets()) {
								Results data = null;
								Map<String, String> locationData = null;
								String seriesId = locationidBucket.getKeyAsString();
								if (mapData.containsKey(seriesId)) {
									data = mapData.get(seriesId);
									locationData = data.getLocations();
								} else {
									data = new Results();
									locationData = new HashMap<>();
									data.setSeriesId(locationidBucket.getKeyAsString());
								}

								Terms locationTypeBuckets = locationidBucket.getAggregations().get("locationType");
								boolean valid = true;
								for (Terms.Bucket locationTypeBucket : locationTypeBuckets.getBuckets()) {
									if ((locationMap.get(locationTypeBucket.getKeyAsString()) != null
											&& locationMap.get(locationTypeBucket.getKeyAsString())
													.contains(locationnameBucket.getKeyAsString())
											|| locationMap.get(locationTypeBucket.getKeyAsString()) == null)) {
										locationData.put(locationTypeBucket.getKeyAsString(),
												locationnameBucket.getKeyAsString());
									} else {
										valid = false;
									}
								}
								if (valid) {
									data.setLocations(locationData);
									mapData.put(seriesId, data);
								}
							}
						}
					}
					for (String key : mapData.keySet()) {
						boolean valid = true;
						Results data = mapData.get(key);
						for (String locationType : locationMap.keySet()) {
							if (!locationType.equals("parent") && !locationMap.get(locationType).contains("OVERALL")
									&& data.getLocations().get(locationType) == null) {
								valid = false;
							}
						}
						if (results.size() < length && valid) {
							QueryResults qr = new QueryResults();
							qr.setDbName(dbNameBucket.getKeyAsString());
							qr.setPropertyId(new Long(dbpropertiesBucket.getKeyAsString()));
							qr.setStratums(mapStratums
									.get(dbNameBucket.getKeyAsString() + ":" + dbpropertiesBucket.getKeyAsString()));
							qr.setData(data);
							results.add(qr);
						}
					}
				}
			}
			response.setResults(results);
		} catch (Exception e) {
			throw e;
		}
		return response;
	}
}
