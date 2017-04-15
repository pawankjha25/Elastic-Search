package com.searchApplication.es.services.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.Client;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.searchApplication.App;
import com.searchApplication.entities.FilterRequest;
import com.searchApplication.entities.LocationToggle;
import com.searchApplication.entities.LocationToggleResults;
import com.searchApplication.entities.QueryResultsList;
import com.searchApplication.entities.SearchOutput;
import com.searchApplication.es.entities.BucketResponseList;
import com.searchApplication.es.interfaces.ZdalyQueryServices;
import com.searchApplication.es.search.aggs.InsdustriInfo;
import com.searchApplication.es.search.aggs.SectorBreakDownAggregation;
import com.searchApplication.es.search.bucketing.AttributeBucketer;
import com.searchApplication.utils.ElasticSearchUtility;

@Service
public class ZdalyQueryServicesImpl implements ZdalyQueryServices {

	@Resource
	private Environment env;

	private static Client client = null;

	public ZdalyQueryServicesImpl() {
		ZdalyQueryServicesImpl.client = ElasticSearchUtility.addClient();
	}

	@Override
	public String health() throws Exception {
		try {
			ClusterHealthRequest request = new ClusterHealthRequest(env.getProperty("es.index_name"));
			ActionFuture<ClusterHealthResponse> health = client.admin().cluster().health(request);
			System.out.println(health.get().getStatus().name());
			if (!health.get().getStatus().name().equalsIgnoreCase("RED")) {
				return "200";
			} else {
				return "500";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "500";
		}
	}

	@Override
	public BucketResponseList produceBuckets(String queryText) throws Exception {
		try {
			return AttributeBucketer.generateBuckets(client, env.getProperty("es.index_name"),
					env.getProperty("es.search_object"), queryText, 1, 1000, App.LOCATIONS);
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public SearchOutput matchQuery(String queryText) throws Exception {
		return null;
	}

	@Override
	public SearchOutput queryWithFilters(FilterRequest request) throws Exception {
		SearchOutput response = new SearchOutput();
		try {
			if (request.getSearchText() != null && !request.getSearchText().isEmpty()) {
				response = Filtering.getFilteringResults(request, env.getProperty("es.index_name"),
						env.getProperty("es.search_object"), client, env.getProperty("es.query.timeout"));
			}

		} catch (Exception e) {
			throw e;
		}
		return response;
	}

	@Override
	public QueryResultsList queryResults(FilterRequest request) throws Exception {
		QueryResultsList response = new QueryResultsList();
		try {
			if (request.getSearchText() != null && !request.getSearchText().isEmpty()) {
				response = Results.getResults(request, env.getProperty("es.index_name"),
						env.getProperty("es.search_object"), client, env.getProperty("es.query.timeout"));
			}
		} catch (Exception e) {
			throw e;
		}
		return response;
	}

	@Override
	public List<InsdustriInfo> getIndustryInfo() throws Exception {
		List<InsdustriInfo> response = new ArrayList<InsdustriInfo>();
		try {
			response.addAll(SectorBreakDownAggregation.getSectors(client, env.getProperty("es.index_name")));
		} catch (Exception e) {
			throw e;
		}
		return response;
	}

	@Override
	public List<LocationToggleResults> getLocationDetails(List<LocationToggle> request) throws Exception {
		List<LocationToggleResults> response = new ArrayList<>();
		try {
			if (!request.isEmpty()) {
				response = LocationToggleImpl.getInstance().getLocationDetails(request,
						env.getProperty("es.index_name"), env.getProperty("es.search_object"), client,
						env.getProperty("es.query.timeout"));
			}
		} catch (Exception e) {
			throw e;
		}
		return response;
	}

}
