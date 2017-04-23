package com.searchApplication.es.interfaces;

import java.util.List;

import com.searchApplication.entities.FilterRequest;
import com.searchApplication.entities.QueryResultsList;
import com.searchApplication.entities.SearchOutput;
import com.searchApplication.es.entities.BucketResponseList;
import com.searchApplication.es.search.aggs.InsdustriInfo;

public interface ZdalyQueryServices {

	public BucketResponseList produceBuckets(String queryText, boolean updateCache) throws Exception;

	public SearchOutput matchQuery(String queryText) throws Exception;

	public SearchOutput queryWithFilters(FilterRequest request) throws Exception;

	public QueryResultsList queryResults(FilterRequest request) throws Exception;

	public List<InsdustriInfo> getIndustryInfo() throws Exception;

	public String health() throws Exception;

}
