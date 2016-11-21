package com.searchApplication.es.search.bucketing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AggregatedBucket {

	private Set<BucketTerms> bucketTerms;
	private int totalRows;
	private int firstAppearance;
	private Set<String> matchedQueryWords; 
	private List<Integer> buckets;
	
	
	public int getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(int totalRows) {
		this.totalRows = totalRows;
	}

	public List<Integer> getBuckets() {
		return buckets;
	}

	public void setBuckets(List<Integer> buckets) {
		this.buckets = buckets;
	}

	public AggregatedBucket() {
		this.matchedQueryWords = new HashSet<String>();
		this.buckets = new ArrayList<Integer>();
	}

	public Set<BucketTerms> getBucketTerms() {
		return bucketTerms;
	}

	public void setBucketTerms( Set<BucketTerms> bucketTerms) {
		this.bucketTerms = bucketTerms;
	}

	public int getCount() {
		return totalRows;
	}

	public void setCount(int count) {
		this.totalRows = count;
	}

	public int getFirstAppearance() {
		return firstAppearance;
	}

	public void setFirstAppearance(int firstAppearance) {
		this.firstAppearance = firstAppearance;
	}
	
	

	public Set<String> getMatchedQueryWords() {
		return matchedQueryWords;
	}

	public void setMatchedQueryWords(Set<String> matchedQueryWords) {
		this.matchedQueryWords = matchedQueryWords;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bucketTerms == null) ? 0 : bucketTerms.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {

		if (getClass() != obj.getClass())
			return false;
		AggregatedBucket other = (AggregatedBucket) obj;
		if (bucketTerms == null) {
			if (other.bucketTerms != null)
				return false;
		} else if (!bucketTerms.equals(other.bucketTerms))
			return false;

		return true;
	}
	
	public void incrementCounts(long total) {
		this.totalRows += total;
	}
	
	public void addIndex(int i) {
		this.buckets.add(i);
	}

	@Override
	public String toString() {
		return "AggregatedBucket [bucketTerms=" + bucketTerms + ", totalRows=" + totalRows + ", firstAppearance="
				+ firstAppearance + ", matchedQueryWords=" + matchedQueryWords + ", buckets=" + buckets + "]";
	}

	
}
