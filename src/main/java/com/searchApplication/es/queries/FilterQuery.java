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
			String[] queryString = request.getSearchText().trim().split("\\|");

			for( String query : queryString )
			{
				if( !query.endsWith("_LOC") && !query.endsWith("_loc") )
				{
					NestedQueryBuilder q = QueryBuilders.nestedQuery("attributes",
							QueryBuilders.matchQuery("attributes.attribute_value.raw", query));
					booleanQuery.must(q);
				}
				else
				{
					NestedQueryBuilder q = QueryBuilders.nestedQuery("locations", QueryBuilders
							.matchQuery("locations.location_name.raw", query.replace("_LOC", "").replace("_loc", "")));
					booleanQuery.must(q);
				}
			}

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
				BoolQueryBuilder locationQuery = new BoolQueryBuilder();
				for( String key : request.getLocations().keySet() )
				{
					for( String locList : request.getLocations().get(key) )
					{
						String parent = locList.split(":")[0];
						String child = locList.split(":")[1];
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
						locationQuery.should(booleanQuery1);
					}
				}
				booleanQuery.must(locationQuery);
			}
		}
		catch( Exception e )
		{
			throw e;
		}
		return booleanQuery;
	}
}
