package com.searchApplication.es.aggregations;

import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;

public class ResultsAggregation {

	@SuppressWarnings( "rawtypes" )
	public static AggregationBuilder getAggregation( String stratumName ) throws Exception
	{
		try
		{

			if( stratumName != null && !stratumName.isEmpty() && !stratumName.contains("*") )
			{
				return AggregationBuilders.nested("attributes").path("attributes").subAggregation(AggregationBuilders
						.terms("attTypes").size(100).field("attributes.attribute_name").include(stratumName).size(100)

						.subAggregation(AggregationBuilders.terms("attributesValues").size(100)
								.field("attributes.attribute_value.raw")

								.subAggregation(AggregationBuilders.reverseNested("attReverse")

										.subAggregation(AggregationBuilders.nested("database").path("db")
												.subAggregation(AggregationBuilders.terms("dbname").field("db.db_name")

														.subAggregation(AggregationBuilders.terms("dbproperties")
																.field("db.properties").size(100)

																.subAggregation(
																		AggregationBuilders.reverseNested("dbReverse")

																				.subAggregation(AggregationBuilders
																						.nested("locations")
																						.path("locations")

																						.subAggregation(
																								AggregationBuilders
																										.terms("locationType")
																										.field("locations.location_type.raw")
																										.size(100)
																										.subAggregation(
																												AggregationBuilders
																														.terms("locationParent")
																														.field("locations.location_parent.raw")
																														.size(100)

																														.subAggregation(
																																AggregationBuilders
																																		.terms("locationname")
																																		.field("locations.location_name.raw")
																																		.size(100)
																																		.size(100)
																																		.subAggregation(
																																				AggregationBuilders
																																						.terms("locationid")
																																						.field("locations.series_id")
																																						.size(100))))

																		)))))))));
			}
			else if( stratumName != null && !stratumName.isEmpty() && stratumName.contains("*") )
			{
				int length = 100;
				if( !stratumName.replaceAll("\\*", "").isEmpty() )
				{
					length = Integer.parseInt(stratumName.replaceAll("\\*", ""));
				}
				return AggregationBuilders.nested("attributes").path("attributes").subAggregation(
						AggregationBuilders.terms("attTypes").size(100).field("attributes.attribute_name").size(100)

								.subAggregation(AggregationBuilders.terms("attributesValues")
										.field("attributes.attribute_value.raw").size(100)

										.subAggregation(AggregationBuilders.reverseNested("attReverse")

												.subAggregation(AggregationBuilders.nested("database").path("db")
														.subAggregation(
																AggregationBuilders.terms("dbname").field("db.db_name").size(100)

																		.subAggregation(AggregationBuilders
																				.terms("dbproperties")
																				.field("db.properties").size(100)

																				.subAggregation(AggregationBuilders
																						.reverseNested("dbReverse")

																						.subAggregation(
																								AggregationBuilders
																										.nested("locations")
																										.path("locations")

																										.subAggregation(
																												AggregationBuilders
																														.terms("locationType")
																														.field("locations.location_type.raw")
																														.size(100)
																														.subAggregation(
																																AggregationBuilders
																																		.terms("locationParent")
																																		.field("locations.location_parent.raw")
																																		.size(length)

																																		.subAggregation(
																																				AggregationBuilders
																																						.terms("locationname")
																																						.field("locations.location_name.raw")
																																						.size(length)
																																						.subAggregation(
																																								AggregationBuilders
																																										.terms("locationid")
																																										.field("locations.series_id")
																																										.size(length))))

																								)))))))));
			}

		}
		catch( Exception e )
		{
			throw e;
		}
		return null;
	}

}
