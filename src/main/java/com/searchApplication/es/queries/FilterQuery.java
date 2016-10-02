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
            NestedQueryBuilder q = QueryBuilders.nestedQuery("attributes",
                    QueryBuilders.queryStringQuery(request.getSearchText().trim().replaceAll("\\|", ""))
                            .field("attributes.attribute_value"));

            booleanQuery.must(q);

            if( request.getFilters() != null )
            {
                for( String key : request.getFilters().keySet() )
                {
                    BoolQueryBuilder booleanQuery2 = new BoolQueryBuilder();
                    for( String value : request.getFilters().get(key) )
                    {
                        if( value != null && !value.isEmpty() )
                        {
                            BoolQueryBuilder booleanQuery1 = new BoolQueryBuilder();

                            NestedQueryBuilder q1 = QueryBuilders.nestedQuery("attributes",
                                    QueryBuilders.matchQuery("attributes.attribute_value.raw", value));
                            booleanQuery1.must(q1);

                            NestedQueryBuilder q2 = QueryBuilders.nestedQuery("attributes",
                                    QueryBuilders.matchQuery("attributes.attribute_name", key));
                            booleanQuery1.must(q2);

                            booleanQuery2.should(booleanQuery1);
                        }
                    }
                    booleanQuery.must(booleanQuery2);
                }
            }

            if( request.getLocations() != null )
            {
                for( String key : request.getLocations().keySet() )
                {
                    String parent = request.getLocations().get(key).split(":")[0];
                    String child = request.getLocations().get(key).split(":")[1];
                    BoolQueryBuilder booleanQuery1 = new BoolQueryBuilder();

                    NestedQueryBuilder q1 = QueryBuilders.nestedQuery("locations",
                            QueryBuilders.matchQuery("locations.location_name.raw", child));
                    booleanQuery1.must(q1);

                    NestedQueryBuilder q2 = QueryBuilders.nestedQuery("locations",
                            QueryBuilders.matchQuery("locations.location_type", key));
                    booleanQuery1.must(q2);

                    if( parent != null && !parent.equals("null") )
                    {
                        NestedQueryBuilder q3 = QueryBuilders.nestedQuery("locations",
                                QueryBuilders.matchQuery("locations.location_parent", parent));
                        booleanQuery1.must(q3);
                    }

                    booleanQuery.must(booleanQuery1);
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
