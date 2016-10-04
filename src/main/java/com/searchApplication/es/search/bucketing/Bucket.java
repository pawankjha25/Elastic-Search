package com.searchApplication.es.search.bucketing;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Bucket implements Comparable<Bucket> {

	private Set<String> bucketTerms;
	private int totalPerfectMatches;
	private int totalPartialMatches;
	private int totalLevenstheinDistance;
	private int totalRows;
	private List<BucketMetaData> bucketMetaData;

	public Bucket(Set<String> bucketTerms, int totalPerfectMatches, int totalPartialMatches,
			int totalLevenstheinDistance) {
		super();
		this.bucketTerms = bucketTerms;
		this.totalPerfectMatches = totalPerfectMatches;
		this.totalPartialMatches = totalPartialMatches;
		this.totalLevenstheinDistance = totalLevenstheinDistance;
		this.totalRows = 1;
		this.bucketMetaData = new ArrayList<BucketMetaData>();
	}

	public void addMetaData(BucketMetaData meta) {
		if (this.bucketMetaData.contains(meta)) {
			this.bucketMetaData.get(this.bucketMetaData.indexOf(meta)).incrementCount();
		} else {
			this.bucketMetaData.add(meta);

		}
	}

	@Override
	public int compareTo(Bucket o) {
		if (this.totalPerfectMatches > o.totalPerfectMatches) {
			return -1;
		} else if (this.totalPerfectMatches < o.totalPerfectMatches) {
			return 1;
		} else {
			if (this.totalPartialMatches > o.totalPartialMatches) {
				return -1;
			} else if (this.totalPartialMatches < o.totalPartialMatches) {
				return 1;
			} else {
				if (this.totalLevenstheinDistance > o.totalLevenstheinDistance) {
					return 1;
				} else if (this.totalLevenstheinDistance < o.totalLevenstheinDistance) {
					return -1;
				} else {
					int tc = this.calucalteTotalBucketLength();
					int otc = o.calucalteTotalBucketLength();
					if (tc < otc) {
						return -1;
					} else if (tc > otc) {
						return 1;
					}
				}

			}
		}

		return 0;
	}

	private int calucalteTotalBucketLength() {
		int l = 0;
		for (String s : this.bucketTerms) {
			l += s.replaceAll(" ", "").length() + 1;
		}
		return l;
	}

	public int getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(int totalRows) {
		this.totalRows = totalRows;
	}

	public Set<String> getBucketTerms() {
		return bucketTerms;
	}

	public void setBucketTerms(Set<String> bucketTerms) {
		this.bucketTerms = bucketTerms;
	}

	public int getTotalPerfectMatches() {
		return totalPerfectMatches;
	}

	public void setTotalPerfectMatches(int totalPerfectMatches) {
		this.totalPerfectMatches = totalPerfectMatches;
	}

	public int getTotalPartialMatches() {
		return totalPartialMatches;
	}

	public void setTotalPartialMatches(int totalPartialMatches) {
		this.totalPartialMatches = totalPartialMatches;
	}

	public int getTotalLevenstheinDistance() {
		return totalLevenstheinDistance;
	}

	public void setTotalLevenstheinDistance(int totalLevenstheinDistance) {
		this.totalLevenstheinDistance = totalLevenstheinDistance;
	}

	public void incrementCount() {
		this.totalRows += 1;
	}

	public List<BucketMetaData> getBucketMetaData() {
		return bucketMetaData;
	}

	public void setBucketMetaData(List<BucketMetaData> bucketMetaData) {
		this.bucketMetaData = bucketMetaData;
	}

	@Override
	public String toString() {
		return "Bucket [bucketTerms=" + bucketTerms + ", totalPerfectMatches=" + totalPerfectMatches
				+ ", totalPartialMatches=" + totalPartialMatches + ", totalLevenstheinDistance="
				+ totalLevenstheinDistance + ", totalRows=" + totalRows + ", bucketMetaData=" + bucketMetaData + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Bucket) {
			if (this.getBucketTerms().size() != ((Bucket) obj).getBucketTerms().size()) {
				return false;
			} else {
				for (String term : ((Bucket) obj).getBucketTerms()) {
					if (!this.bucketTerms.contains(term)) {
						return false;
					}
				}
				return true;
			}
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int hash = 0;
		for (String s : bucketTerms) {
			hash += s.hashCode();
		}
		return hash;
	}
}
