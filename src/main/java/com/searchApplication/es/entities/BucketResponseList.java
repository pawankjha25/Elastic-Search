package com.searchApplication.es.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.searchApplication.es.search.bucketing.Bucket;
import com.searchApplication.es.search.bucketing.BucketMetaData;

public class BucketResponseList {

	private String searchString;
	private Set<BucketResponse> searchResponse;
	private long totalTimesInMillis;
	private long totalRows;
	
	
	
	
	public long getTotalTimesInMillis() {
		return totalTimesInMillis;
	}

	public void setTotalTimesInMillis(long totalTimesInMillis) {
		this.totalTimesInMillis = totalTimesInMillis;
	}

	public long getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(long totalRows) {
		this.totalRows = totalRows;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public Set<BucketResponse> getSearchResponse() {
		return searchResponse;
	}

	public void setSearchResponse(Set<BucketResponse> searchResponse) {
		this.searchResponse = searchResponse;
	}

	public static BucketResponseList buildFromBucketList(List<Bucket> buckets) {
		BucketResponseList b = new BucketResponseList();
		List<BucketResponse> responses = new ArrayList<BucketResponse>();
		for (Bucket bucket : buckets) {
			for (BucketMetaData meta : bucket.getBucketMetaData()) {
				BucketResponse r = new BucketResponse();
				r.setSector(meta.getSector());
				r.setSubSector(meta.getSubSector());
				r.setSuperRegion(meta.getSuperRegion());
				responses.add(r);
			}
		}

		return b;
	}
}
