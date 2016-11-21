package com.searchApplication.es.search.bucketing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class BucketTerms implements Comparable<BucketTerms> {

	private String attributeName;
	private int queryWordMatch;
	private Set<String> matchedQueries;
	private boolean isFull;
	private int matchedQueryWordsCount;

	public BucketTerms() {

	}

	public BucketTerms(String attributeName, int queryWordMatch, boolean isFull, int matchedQueryWordsCount) {
		super();
		this.attributeName = attributeName;
		this.queryWordMatch = queryWordMatch;
		this.isFull = isFull;
		this.matchedQueryWordsCount = matchedQueryWordsCount;
		this.matchedQueries = new HashSet<String>();
	}

	public Set<String> getMatchedQueries() {
		return matchedQueries;
	}

	public void setMatchedQueries(Set<String> matchedQueries) {
		this.matchedQueries = matchedQueries;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public int getQueryWordMatch() {
		return queryWordMatch;
	}

	public void setQueryWordMatch(int queryWordMatch) {
		this.queryWordMatch = queryWordMatch;
	}

	public boolean isFull() {
		return isFull;
	}

	public void setFull(boolean isFull) {
		this.isFull = isFull;
	}

	public int getMatchedQueryWordsCount() {
		return matchedQueryWordsCount;
	}

	public void setMatchedQueryWordsCount(int matchedQueryWordsCount) {
		this.matchedQueryWordsCount = matchedQueryWordsCount;
	}

	@Override
	public String toString() {
		return "BucketTerms [attributeName=" + attributeName + "\n, queryWordMatch=" + queryWordMatch + ", isFull="
				+ isFull + ", matchedQueryWordsCount=" + matchedQueryWordsCount + "]\n";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributeName == null) ? 0 : attributeName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BucketTerms other = (BucketTerms) obj;
		if (attributeName == null) {
			if (other.attributeName != null)
				return false;
		} else if (!attributeName.equals(other.attributeName))
			return false;
		return true;
	}

	@Override
	public int compareTo(BucketTerms o) {
		if (o.getQueryWordMatch() < this.queryWordMatch) {
			return 1;
		} else {
			if (o.getQueryWordMatch() > this.queryWordMatch) {
				return -1;
			} else {
				if (o.getMatchedQueryWordsCount() > this.matchedQueryWordsCount) {
					return 1;
				} else {
					if (o.getMatchedQueryWordsCount() < this.matchedQueryWordsCount) {
						return -1;
					}
					if (o.isFull && !this.isFull) {
						return 1;
					} else {
						return -1;
					}
				}
			}
		}

	}

	public static LinkedHashSet<String> createdQuerySortedBucket(Set<BucketTerms> terms) {
		LinkedHashSet<String> bucketTermsInOrder = new LinkedHashSet<String>();
		ArrayList<BucketTerms> list = new ArrayList<BucketTerms>(terms);
		Collections.sort(list);
		for (int i = 0; i < list.size(); i++) {
			bucketTermsInOrder.add(list.get(i).getAttributeName());
		}
		return bucketTermsInOrder;
	}

}
