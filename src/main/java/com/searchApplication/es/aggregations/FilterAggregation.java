package com.searchApplication.es.aggregations;

import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;

public class FilterAggregation {

    @SuppressWarnings( "rawtypes" )
    public static AggregationBuilder getAggregation() throws Exception
    {
        try
        {
            return AggregationBuilders.nested("attributes").path("attributes")
                    .subAggregation(AggregationBuilders.terms("attTypes").field("attributes.attribute_name").size(100)

                            .subAggregation(
                                    AggregationBuilders.terms("attLevel").field("attributes.attribute_level").size(100)

                                            .subAggregation(AggregationBuilders.terms("attParent")
                                                    .field("attributes.attribute_parent").size(100)

                                                    .subAggregation(AggregationBuilders.terms("attValues")
                                                            .field("attributes.attribute_value.raw").size(100)

                                                            .subAggregation(
                                                                    AggregationBuilders.reverseNested("reverseNested")

                                                                            .subAggregation(AggregationBuilders
                                                                                    .nested("locations")
                                                                                    .path("locations")

                                                                                    .subAggregation(AggregationBuilders
                                                                                            .terms("locationType")
                                                                                            .field("locations.location_type")

                                                                                            .subAggregation(
                                                                                                    AggregationBuilders
                                                                                                            .terms("locationName")
                                                                                                            .field("locations.location_name.raw")))))))));
        }
        catch( Exception e )
        {
            throw e;
        }
    }

}
