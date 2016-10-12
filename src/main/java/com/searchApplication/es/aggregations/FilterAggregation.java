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

									))));
		}
		catch( Exception e )
		{
			throw e;
		}
	}

	@SuppressWarnings( "rawtypes" )
	public static AggregationBuilder getLocationAggregation() throws Exception
	{
		try
		{
			return AggregationBuilders.nested("locations").path("locations")

					.subAggregation(
							AggregationBuilders.terms("locationType").field("locations.location_type.raw").size(100)

									.subAggregation(AggregationBuilders.terms("locationParent")
											.field("locations.location_parent.raw").size(100)

											.subAggregation(AggregationBuilders.terms("locationName")
													.field("locations.location_name.raw").size(100))));
		}
		catch( Exception e )
		{
			throw e;
		}
	}

}
