package com.searchApplication.es.services.impl;

import javax.annotation.Resource;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import com.searchApplication.entities.FilterRequest;
import com.searchApplication.entities.QueryResultsList;
import com.searchApplication.entities.SearchOutput;
import com.searchApplication.es.aggregations.FilterAggregation;
import com.searchApplication.es.aggregations.ResultsAggregation;
import com.searchApplication.es.entities.BucketResponseList;
import com.searchApplication.es.interfaces.ZdalyQueryServices;
import com.searchApplication.es.queries.FilterQuery;
import com.searchApplication.es.search.bucketing.AttributeBucketer;
import com.searchApplication.es.services.response.QueryFilterResponse;
import com.searchApplication.es.services.response.ResultsResponse;
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
	public BucketResponseList produceBuckets(String queryText) throws Exception {
		try {
			return AttributeBucketer.generateBuckets(client, env.getProperty("es.index_name"),
					env.getProperty("es.search_object"), queryText, 10);
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public SearchOutput matchQuery(String queryText) throws Exception {
		return null;
	}

	@Override
    public SearchOutput queryWithFilters( FilterRequest request ) throws Exception
    {
        SearchOutput response = new SearchOutput();
        BoolQueryBuilder booleanQuery = new BoolQueryBuilder();
        try
        {
            if( request.getSearchText() != null && !request.getSearchText().isEmpty() )
            {
                booleanQuery = FilterQuery.getQuery(request);

                SearchResponse tFdocs = null;
                tFdocs = client.prepareSearch(env.getProperty("es.index_name"))
                        .setTypes(env.getProperty("es.search_object")).setQuery(booleanQuery)
                        .addAggregation(FilterAggregation.getAggregation()).execute().actionGet();

                response = QueryFilterResponse.getResponse(tFdocs);

                tFdocs = client.prepareSearch(env.getProperty("es.index_name"))
                        .setTypes(env.getProperty("es.search_object")).setQuery(booleanQuery)
                        .addAggregation(FilterAggregation.getLocationAggregation()).execute().actionGet();

                response.setLocations(QueryFilterResponse.getLocationAggregation(tFdocs,request.getLocations()));

            }

        }
        catch( Exception e )
        {
            throw e;
        }
        return response;
    }

    @SuppressWarnings( "rawtypes" )
    @Override
    public QueryResultsList queryResults( FilterRequest request ) throws Exception
    {
        QueryResultsList response = new QueryResultsList();
        BoolQueryBuilder booleanQuery = new BoolQueryBuilder();
        try
        {
            if( request.getSearchText() != null && !request.getSearchText().isEmpty() )
            {
                booleanQuery = FilterQuery.getQuery(request);

                AggregationBuilder aggregation = ResultsAggregation.getAggregation();

                SearchResponse tFdocs = null;

                tFdocs = client.prepareSearch(env.getProperty("es.index_name"))
                        .setTypes(env.getProperty("es.search_object")).setQuery(booleanQuery).setSize(0)
                        .addAggregation(aggregation).execute().actionGet();

                response = ResultsResponse.getResults(tFdocs);
            }

        }
        catch( Exception e )
        {
            throw e;
        }
        return response;
    }

}
