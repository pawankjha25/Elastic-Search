package com.searchApplication.es.services.impl;

import javax.annotation.Resource;
import org.elasticsearch.client.Client;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.searchApplication.App;
import com.searchApplication.entities.FilterRequest;
import com.searchApplication.entities.QueryResultsList;
import com.searchApplication.entities.SearchOutput;
import com.searchApplication.es.entities.BucketResponseList;
import com.searchApplication.es.interfaces.ZdalyQueryServices;
import com.searchApplication.es.search.bucketing.AttributeBucketer;
import com.searchApplication.utils.ElasticSearchUtility;
import com.searchApplication.utils.LocationLoader;

@Service
public class ZdalyQueryServicesImpl implements ZdalyQueryServices {

	@Resource
	private Environment env;

	private static Client client = null;

	public ZdalyQueryServicesImpl()
	{
		ZdalyQueryServicesImpl.client = ElasticSearchUtility.addClient();
	}

	@Override
	public BucketResponseList produceBuckets( String queryText ) throws Exception
	{
		try
		{
			return AttributeBucketer.generateBuckets(client, env.getProperty("es.index_name"),
					env.getProperty("es.search_object"), queryText, 1, 1000, App.LOCATIONS);
		}
		catch( Exception e )
		{
			throw e;
		}
	}

	@Override
	public SearchOutput matchQuery( String queryText ) throws Exception
	{
		return null;
	}

	@Override
	public SearchOutput queryWithFilters( FilterRequest request ) throws Exception
	{
		SearchOutput response = new SearchOutput();
		try
		{
			if( request.getSearchText() != null && !request.getSearchText().isEmpty() )
			{
				response = Filtering.getFilteringResults(request, env.getProperty("es.index_name"),
						env.getProperty("es.search_object"), client);
			}

		}
		catch( Exception e )
		{
			throw e;
		}
		return response;
	}

	@Override
	public QueryResultsList queryResults( FilterRequest request ) throws Exception
	{
		QueryResultsList response = new QueryResultsList();
		try
		{
			if( request.getSearchText() != null && !request.getSearchText().isEmpty() )
			{
				response = Results.getResults(request, env.getProperty("es.index_name"),
						env.getProperty("es.search_object"), client);
			}
		}
		catch( Exception e )
		{
			throw e;
		}
		return response;
	}
}
