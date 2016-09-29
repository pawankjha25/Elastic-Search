package com.searchApplication.es.interfaces;

import com.searchApplication.entities.FilterRequest;
import com.searchApplication.entities.QueryResultsList;
import com.searchApplication.entities.SearchOutput;
import com.searchApplication.es.entities.WildCardSearchResponseList;

public interface ZdalyQueryServices {

	public WildCardSearchResponseList wildcardQuery( String queryText ) throws Exception;

	public SearchOutput matchQuery( String queryText ) throws Exception;
	
	public SearchOutput queryWithFilters( FilterRequest request ) throws Exception;
	
	public QueryResultsList queryResults( FilterRequest request ) throws Exception;
}
