package com.searchApplication.es.queries;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public class BucketQuery {

    public static BoolQueryBuilder getQuery( String queryText ) throws Exception
    {
        BoolQueryBuilder booleanQuery = new BoolQueryBuilder();
        try

        {
            queryText = queryText.toLowerCase();
            String[] queryValue = queryText.split(" ");
            if( queryText != null && !queryText.isEmpty() )
            {
                queryValue = queryText.trim().split(" ");
                if( queryValue.length > 1 )
                {
                    for( String value : queryValue )
                    {
                        if( value.length() > 2 )
                        {
                            booleanQuery.should(QueryBuilders.matchQuery("description.ngramed", value));
                        }
                    }
                }
                else
                {
                    booleanQuery.must(QueryBuilders.matchQuery("description.ngramed", queryText));
                }
            }
        }
        catch( Exception e )
        {
            throw e;
        }
        return booleanQuery;
    }

}
