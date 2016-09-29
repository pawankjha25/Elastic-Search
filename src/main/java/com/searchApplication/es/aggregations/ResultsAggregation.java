package com.searchApplication.es.aggregations;

import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;

public class ResultsAggregation {

    @SuppressWarnings( "rawtypes" )
    public static AggregationBuilder getAggregation() throws Exception
    {
        try
        {
            return AggregationBuilders.nested("attributes").path("attributes")
                    .subAggregation(AggregationBuilders.terms("attTypes").size(100).field("attributes.attribute_name")

                            .subAggregation(AggregationBuilders.terms("attributesValues").size(100)
                                    .field("attributes.attribute_value.raw")

                                    .subAggregation(AggregationBuilders.nested("database").path("db")
                                            .subAggregation(AggregationBuilders.terms("dbname").field("db.db_name")

                                                    .subAggregation(AggregationBuilders.terms("dbproperties")
                                                            .field("db.properties")

                                                            .size(100).subAggregation(AggregationBuilders
                                                                    .nested("locations").path("locations")

                                                                    .subAggregation(AggregationBuilders
                                                                            .terms("locationid")
                                                                            .field("locations.series_id").size(100)

            )))))));
        }
        catch( Exception e )
        {
            throw e;
        }
    }

}
