package com.searchApplication.es.search.bucketing;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

public class BucketTerms implements Comparable<BucketTerms> {

	private String attributeName;
	private int queryWordMatch;
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
		return "BucketTerms [attributeName=" + attributeName + ", queryWordMatch=" + queryWordMatch + ", isFull="
				+ isFull + ", matchedQueryWordsCount=" + matchedQueryWordsCount + "]";
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

	public static LinkedHashSet<String> createdQuerySortedBucket(List<BucketTerms> terms) {
		LinkedHashSet<String> bucketTermsInOrder = new LinkedHashSet<String>();
		Collections.sort(terms);
		for (BucketTerms t : terms) {
			bucketTermsInOrder.add(t.getAttributeName());
		}
		return bucketTermsInOrder;
	}

}
