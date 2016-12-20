package com.searchApplication.es.search.aggs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

public class SectorBreakDownAggregation {

	private static final String SECTOR_AGG_NAME = "sector_agg";

	public static List<InsdustriInfo> getSectors(Client client, String index) {
		SearchRequestBuilder srb = client.prepareSearch(index).addAggregation(generateAggregation()).setSize(0);
		SearchResponse sr = srb.get();
		return processResults(sr);
	}

	private static List<InsdustriInfo> processResults(SearchResponse sr) {
		Iterator<org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket> sectorBuckets = ((StringTerms) sr
				.getAggregations().asMap().get(SECTOR_AGG_NAME)).getBuckets().iterator();
		List<InsdustriInfo> infos = new ArrayList<InsdustriInfo>();
		while (sectorBuckets.hasNext()) {
			InsdustriInfo info = new InsdustriInfo();

			Terms.Bucket b = sectorBuckets.next();
			info.setSector(b.getKeyAsString());
			Iterator<org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket> sectorIt = ((StringTerms) b
					.getAggregations().asList().get(0)).getBuckets().iterator();
			List<RegionInfo> rinfos = new ArrayList<RegionInfo>();
			while (sectorIt.hasNext()) {
				Terms.Bucket rbucket = sectorIt.next();
				RegionInfo rinfo = new RegionInfo();
				rinfo.setRegion(rbucket.getKeyAsString());
				Iterator<org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket> subSectorIt = ((StringTerms) rbucket
						.getAggregations().asList().get(0)).getBuckets().iterator();
				List<String> subsectors = new ArrayList<String>();
				while (subSectorIt.hasNext()) {
					Terms.Bucket subbucket = subSectorIt.next();
					subsectors.add(subbucket.getKeyAsString());
				}
				rinfo.setSubsectors(subsectors);
				rinfos.add(rinfo);
			}
			info.setRegions(rinfos);
			infos.add(info);
		}
		return infos;

	}

	private static AbstractAggregationBuilder generateAggregation() {
		return AggregationBuilders.terms(SECTOR_AGG_NAME).field("sector").size(50)
				.subAggregation(AggregationBuilders.terms("superregion").field("super_region").size(50)
						.subAggregation(AggregationBuilders.terms("subsector").field("sub_sector").size(50)));

	}
}
