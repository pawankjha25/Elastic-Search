package com.searchApplication.es.queries;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import com.searchApplication.entities.FilterRequest;

public class FilterQuery {

    public static BoolQueryBuilder getQuery( FilterRequest request ) throws Exception
    {
        BoolQueryBuilder booleanQuery = new BoolQueryBuilder();
        try
        {
        	//not sure why you do this spliting can you explain please
            String[] queryList = request.getSearchText().split("\\|");
            if( queryList.length > 1 )
            {
                for( String query : queryList )
                {
                	//instructions for this is to use a query_string query instead of match query
                    booleanQuery.must(QueryBuilders.matchQuery("description.ngramed", query.trim()));
                }
            }
            else
            {  
            	//if I am reading this correctly unless there is a pipline char | the search changes completly?
            	//why is that so, I am not sure I understand where in the instructions this comes to play?
            	//is this related to bucketing?
                NestedQueryBuilder q = QueryBuilders.nestedQuery("attributes",
                        QueryBuilders.matchQuery("attributes.attribute_value.raw", request.getSearchText()));

                booleanQuery.must(q);
            }
            //can you explain what these do? how are requests parsed from the front end?
            if( request.getFilters() != null )
            {
                for( String key : request.getFilters().keySet() )
                {
                    BoolQueryBuilder booleanQuery2 = new BoolQueryBuilder();
                    for( String value : request.getFilters().get(key) )
                    {
                        BoolQueryBuilder booleanQuery1 = new BoolQueryBuilder();

                        NestedQueryBuilder q1 = QueryBuilders.nestedQuery("attributes", QueryBuilders
                                .matchQuery("attributes.attribute_value.raw", value).analyzer("whitespace"));
                        booleanQuery1.must(q1);

                        NestedQueryBuilder q2 = QueryBuilders.nestedQuery("attributes",
                                QueryBuilders.matchQuery("attributes.attribute_name", key));
                        booleanQuery1.must(q2);

                        booleanQuery2.should(booleanQuery1);
                    }
                    booleanQuery.must(booleanQuery2);
                }
            }

            if( request.getLocations() != null )
            {
                for( String key : request.getLocations().keySet() )
                {
                    BoolQueryBuilder booleanQuery2 = new BoolQueryBuilder();
                    for( String value : request.getLocations().get(key) )
                    {
                        BoolQueryBuilder booleanQuery1 = new BoolQueryBuilder();

                        NestedQueryBuilder q1 = QueryBuilders.nestedQuery("locations",
                                QueryBuilders.matchQuery("locations.location_name.raw", value));
                        booleanQuery1.must(q1);

                        NestedQueryBuilder q2 = QueryBuilders.nestedQuery("locations",
                                QueryBuilders.matchQuery("locations.location_type", key));
                        booleanQuery1.must(q2);

                        booleanQuery2.should(booleanQuery1);
                    }
                    booleanQuery.must(booleanQuery2);
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
