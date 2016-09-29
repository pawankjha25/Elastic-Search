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
                //this is a really bad use of bool queries; there is no need to have each separate word in a bool query
                //doing just one query string would do fine. there is no need for so many should queries
                //can you explain what are you trying to do here.
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
