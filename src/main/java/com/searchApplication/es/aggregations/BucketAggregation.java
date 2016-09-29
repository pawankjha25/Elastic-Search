package com.searchApplication.es.aggregations;

import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;

public class BucketAggregation {

    @SuppressWarnings( "rawtypes" )
    public static AggregationBuilder getAggregation( String searchString ) throws Exception
    {
        try
        {
            System.out.println(searchString);
            return AggregationBuilders.nested("attributes").path("attributes")

                    .subAggregation(AggregationBuilders.terms("attributesValues").field("attributes.attribute_value")
                            .include(searchString)

                            .subAggregation(AggregationBuilders.terms("attributeValuesRaw")
                                    .field("attributes.attribute_value.raw")

                                    .subAggregation(AggregationBuilders.terms("attributeName")
                                            .field("attributes.attribute_name")

                                            .subAggregation(AggregationBuilders.reverseNested("reverseNested")

                                                    .subAggregation(AggregationBuilders.terms("sector").field("sector")

                                                            .subAggregation(AggregationBuilders.terms("subsector")
                                                                    .field("sub_sector")

                                                                    .subAggregation(
                                                                            AggregationBuilders.terms("superregion")
                                                                                    .field("super_region"))))))));
        }
        catch( Exception e )
        {
            throw e;
        }
    }

}
