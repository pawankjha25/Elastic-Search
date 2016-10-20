package com.searchApplication.es.aggregations;

import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;

public class ResultsAggregation {

	@SuppressWarnings( "rawtypes" )
	public static AggregationBuilder getAggregation( String stratumName , Boolean location,String[] locations) throws Exception
	{
		try
		{
			if( stratumName != null && !stratumName.isEmpty() && !stratumName.contains("*") )
			{
				if(!location)
				{
					return AggregationBuilders.nested("database").path("db")
							.subAggregation(AggregationBuilders.terms("dbname").field("db.db_name").size(100)
							.subAggregation(AggregationBuilders.terms("dbproperties").field("db.properties").size(100)
							.subAggregation(AggregationBuilders.reverseNested("dbReverse")
									
							.subAggregation(AggregationBuilders.nested("attributes").path("attributes")
							.subAggregation(AggregationBuilders.terms("attTypes").field("attributes.attribute_name").include(stratumName).size(100)
							.subAggregation(AggregationBuilders.terms("attributesValues").field("attributes.attribute_value.raw").size(100)
							.subAggregation(AggregationBuilders.reverseNested("attReverse")))))
							
							.subAggregation(AggregationBuilders.nested("locations").path("locations")
							.subAggregation(AggregationBuilders.terms("locationid").field("locations.series_id").size(500)
							.subAggregation(AggregationBuilders.terms("locationType").field("locations.location_type.raw")
							.subAggregation(AggregationBuilders.terms("locationname").field("locations.location_name.raw")))))
							
							)));
				}
				else 
				{
					return AggregationBuilders.nested("database").path("db")
							.subAggregation(AggregationBuilders.terms("dbname").field("db.db_name").size(100)
							.subAggregation(AggregationBuilders.terms("dbproperties").field("db.properties").size(100)
							.subAggregation(AggregationBuilders.reverseNested("dbReverse")
									
							.subAggregation(AggregationBuilders.nested("attributes").path("attributes")
							.subAggregation(AggregationBuilders.terms("attTypes").field("attributes.attribute_name").include(stratumName).size(100)
							.subAggregation(AggregationBuilders.terms("attributesValues").field("attributes.attribute_value.raw").size(100)
							.subAggregation(AggregationBuilders.reverseNested("attReverse")))))
							
							.subAggregation(AggregationBuilders.nested("locations").path("locations")
							.subAggregation(AggregationBuilders.terms("locationParent").field("locations.location_parent.raw").include(locations).size(10000)
							.subAggregation(AggregationBuilders.terms("locationname").field("locations.location_name.raw").include(locations).size(10000)
							.subAggregation(AggregationBuilders.terms("locationid").field("locations.series_id").size(100)
							.subAggregation(AggregationBuilders.terms("locationType").field("locations.location_type.raw")
							)))))
							
							)));
				}
			}
			else if( stratumName != null && !stratumName.isEmpty() && stratumName.contains("*") )
			{
				int length = 500;
				if( stratumName.contains("*") && !stratumName.replaceAll("\\*", "").isEmpty() )
				{
					length = Integer.parseInt(stratumName.replaceAll("\\*", ""));
				}
				if(!location)
				{
					return AggregationBuilders.nested("database").path("db")
							.subAggregation(AggregationBuilders.terms("dbname").field("db.db_name").size(100)
							.subAggregation(AggregationBuilders.terms("dbproperties").field("db.properties").size(100)
							.subAggregation(AggregationBuilders.reverseNested("dbReverse")
									
							.subAggregation(AggregationBuilders.nested("attributes").path("attributes")
							.subAggregation(AggregationBuilders.terms("attTypes").field("attributes.attribute_name").size(100)
							.subAggregation(AggregationBuilders.terms("attributesValues").field("attributes.attribute_value.raw").size(100)
							.subAggregation(AggregationBuilders.reverseNested("attReverse")))))
							
							.subAggregation(AggregationBuilders.nested("locations").path("locations")
							.subAggregation(AggregationBuilders.terms("locationid").field("locations.series_id").size(length)
							.subAggregation(AggregationBuilders.terms("locationType").field("locations.location_type.raw")
							.subAggregation(AggregationBuilders.terms("locationname").field("locations.location_name.raw")))))
							
							)));
				}
				else 
				{
					return AggregationBuilders.nested("database").path("db")
							.subAggregation(AggregationBuilders.terms("dbname").field("db.db_name").size(100)
							.subAggregation(AggregationBuilders.terms("dbproperties").field("db.properties").size(100)
							.subAggregation(AggregationBuilders.reverseNested("dbReverse")
									
							.subAggregation(AggregationBuilders.nested("attributes").path("attributes")
							.subAggregation(AggregationBuilders.terms("attTypes").field("attributes.attribute_name").size(100)
							.subAggregation(AggregationBuilders.terms("attributesValues").field("attributes.attribute_value.raw").size(100)
							.subAggregation(AggregationBuilders.reverseNested("attReverse")))))
							
							.subAggregation(AggregationBuilders.nested("locations").path("locations")
							.subAggregation(AggregationBuilders.terms("locationParent").field("locations.location_parent.raw").include(locations).size(10000)
							.subAggregation(AggregationBuilders.terms("locationname").field("locations.location_name.raw").include(locations).size(10000)
							.subAggregation(AggregationBuilders.terms("locationid").field("locations.series_id").size(100)
							.subAggregation(AggregationBuilders.terms("locationType").field("locations.location_type.raw")
							)))))
							
							)));
				}
			}

		}
		catch( Exception e )
		{
			throw e;
		}
		return null;
	}

}
