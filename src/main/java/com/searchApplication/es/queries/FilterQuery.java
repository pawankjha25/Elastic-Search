package com.searchApplication.es.queries;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.searchApplication.entities.FilterRequest;

public class FilterQuery {

	public static BoolQueryBuilder getQuery(FilterRequest request) throws Exception {
		BoolQueryBuilder booleanQuery = new BoolQueryBuilder();
		try {
			String[] queryString = request.getSearchText().trim().split("\\|");

			for (String query : queryString) {
				if (!query.endsWith("_LOC") && !query.endsWith("_loc")) {
					NestedQueryBuilder q = QueryBuilders.nestedQuery("attributes",
							QueryBuilders.termQuery("attributes.attribute_value.raw", query));
					booleanQuery.filter(q);
				} else {
					NestedQueryBuilder q = QueryBuilders.nestedQuery("locations", QueryBuilders
							.termQuery("locations.location_name.raw", query.replace("_LOC", "").replace("_loc", "")));
					booleanQuery.filter(q);
				}
			}

			if (request.getFilters() != null) {
				for (String key : request.getFilters().keySet()) {
					if (request.getFilters().get(key).size() == 1
							&& request.getFilters().get(key).get(0).equalsIgnoreCase("NULL")) {
						NestedQueryBuilder q2 = QueryBuilders.nestedQuery("attributes",
								QueryBuilders.termQuery("attributes.attribute_name", key));
						booleanQuery.mustNot(q2);
					} else {

						if ("Sector".equalsIgnoreCase(key)) {
							
							BoolQueryBuilder sectorQuery = new BoolQueryBuilder();
							NestedQueryBuilder attName1 = QueryBuilders.nestedQuery("attributes",
									QueryBuilders.termQuery("attributes.attribute_name", key));
							sectorQuery.should(attName1);
							
							NestedQueryBuilder attName2 = QueryBuilders.nestedQuery("attributes",
									QueryBuilders.termQuery("attributes.attribute_name", "SECTOR"));
							sectorQuery.should(attName2);
							
							booleanQuery.must(sectorQuery);

						} else {
							NestedQueryBuilder attName = QueryBuilders.nestedQuery("attributes",
									QueryBuilders.termQuery("attributes.attribute_name", key));
							booleanQuery.filter(attName);
						}

						BoolQueryBuilder booleanQuery1 = new BoolQueryBuilder();
						for (String value : request.getFilters().get(key)) {
							if (value != null && !value.isEmpty() && !value.equalsIgnoreCase("NULL")) {
								NestedQueryBuilder q1 = QueryBuilders.nestedQuery("attributes",
										QueryBuilders.termQuery("attributes.attribute_value.raw", value));
								booleanQuery1.should(q1);
							}
						}
						booleanQuery.filter(booleanQuery1);
					}
				}
			}

			if (request.getLocations() != null) {
				for (String key : request.getLocations().keySet()) {
					BoolQueryBuilder locationQuery = new BoolQueryBuilder();
					for (String locList : request.getLocations().get(key)) {
						String parent = locList.split(":")[0];
						String child = locList.split(":")[1];

						BoolQueryBuilder booleanQuery1 = new BoolQueryBuilder();
						if (!child.equalsIgnoreCase("OVERALL")) {
							NestedQueryBuilder q1 = QueryBuilders.nestedQuery("locations",
									QueryBuilders.termQuery("locations.location_name.raw", child));
							booleanQuery1.filter(q1);

							NestedQueryBuilder q2 = QueryBuilders.nestedQuery("locations",
									QueryBuilders.termQuery("locations.location_type.raw", key));
							booleanQuery1.filter(q2);

							if (parent != null && !parent.equals("null")) {
								NestedQueryBuilder q3 = QueryBuilders.nestedQuery("locations",
										QueryBuilders.termQuery("locations.location_parent.raw", parent));
								booleanQuery1.filter(q3);
							}
							if (request.getLocations().get(key).size() > 1) {
								locationQuery.should(booleanQuery1);
							} else {
								locationQuery.filter(booleanQuery1);
							}
						} else {
							NestedQueryBuilder q2 = QueryBuilders.nestedQuery("locations",
									QueryBuilders.termQuery("locations.location_type.raw", key));
							booleanQuery1.mustNot(q2);
							locationQuery.filter(booleanQuery1);

						}
					}
					booleanQuery.filter(locationQuery);
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return booleanQuery;
	}

	public static BoolQueryBuilder getNotQuery(FilterRequest request, String attributeName) throws Exception {
		BoolQueryBuilder booleanQuery = new BoolQueryBuilder();
		try {
			booleanQuery = getQuery(request);
			NestedQueryBuilder q2 = QueryBuilders.nestedQuery("attributes",
					QueryBuilders.matchQuery("attributes.attribute_name", attributeName));
			booleanQuery.mustNot(q2);

		} catch (Exception e) {
			throw e;
		}
		return booleanQuery;
	}
}
