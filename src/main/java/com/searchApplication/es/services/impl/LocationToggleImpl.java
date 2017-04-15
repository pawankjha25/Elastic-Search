package com.searchApplication.es.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.searchApplication.entities.LocationToggle;
import com.searchApplication.entities.LocationToggleResults;

public class LocationToggleImpl {

	private static LocationToggleImpl instance = null;

	private static final Logger LOG = LoggerFactory.getLogger(LocationToggleImpl.class);

	private LocationToggleImpl() {

	}

	public static LocationToggleImpl getInstance() {

		if (instance == null) {
			instance = new LocationToggleImpl();
		}
		return instance;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<LocationToggleResults> getLocationDetails(List<LocationToggle> request, String indexName, String objectType,
			Client client, String timeout) {
		
		List<LocationToggleResults> results = new ArrayList<>();

		request.forEach(req -> {

			SearchResponse tFdocs;

			QueryBuilder query = QueryBuilders.boolQuery().filter(QueryBuilders.termQuery("_id", req.getRowIds()));
			long startTime = System.currentTimeMillis();
			tFdocs = client.prepareSearch(indexName).setTypes(req.getDbName().toLowerCase()).setQuery(query)
					.setFetchSource(new String[] { "locations.location_name", "locations.series_id" }, null).setSize(0)
					.get();
			long endTime = System.currentTimeMillis();
			
			for (SearchHit hit : tFdocs.getHits()){
				   Map map = hit.getSource();
				   Map<String,String> locations  = new HashMap<>();
				   LocationToggleResults res = new LocationToggleResults();
				   res.setId(map.get("_id").toString());
				   List<JsonObject> location = (List<JsonObject>) map.get("locations");
				   location.forEach(loc -> {
					   locations.put(loc.get("series_id").getAsString(), loc.get("location_name").getAsString());
				   });
				   res.setLocations(locations);
				   results.add(res);
				}

			LOG.info("Service took - " + (endTime - startTime) + " milliseconds to query");
		});

		return results;
	}

}
