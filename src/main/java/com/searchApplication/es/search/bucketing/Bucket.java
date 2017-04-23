package com.searchApplication.es.search.bucketing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Bucket implements Comparable<Bucket>, Serializable {

	private Set<BucketTerms> bucketTerms;
	private Integer totalPerfectMatches;
	private Integer totalPartialMatches;
	private Integer totalLevenstheinDistance;
	private Long totalRows;
	private Integer totalLength;
	private List<BucketMetaData> bucketMetaData;

	public Bucket(Set<BucketTerms> bucketTerms, int totalPerfectMatches, int totalPartialMatches,
			int totalLevenstheinDistance) {
		super();
		this.bucketTerms = bucketTerms;
		this.totalPerfectMatches = totalPerfectMatches;
		this.totalPartialMatches = totalPartialMatches;
		this.totalLevenstheinDistance = totalLevenstheinDistance;
		this.totalRows = Long.valueOf(1);
		this.bucketMetaData = new ArrayList<BucketMetaData>();
		totalLength = calucalteTotalBucketLength();
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
			if (this.totalRows > o.totalRows) {
				return -1;
			} else if (this.totalRows < o.totalRows) {
				return 1;
			} else {
				if (this.getBucketTerms().size() < o.getBucketTerms().size()) {
					return -1;
				} else if (this.getBucketTerms().size() > o.getBucketTerms().size()) {
					return 1;
				}

			}
		}

		return Integer.compare(totalLength, o.totalLength);
	}

	private int calucalteTotalBucketLength() {
		int l = 0;
		for (BucketTerms s : this.bucketTerms) {
			l += s.getAttributeName().replaceAll(" ", "").length() + 1;
		}
		return l + this.bucketTerms.size();
	}

	public long getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(long totalRows) {
		this.totalRows = totalRows;
	}

	public Set<BucketTerms> getBucketTerms() {
		return bucketTerms;
	}

	public void setBucketTerms(Set<BucketTerms> bucketTerms) {
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
		return "Bucket [bucketTerms=" + bucketTerms + "\n, totalPerfectMatches=" + totalPerfectMatches
				+ ", totalPartialMatches=" + totalPartialMatches + ", totalLevenstheinDistance="
				+ totalLevenstheinDistance + ", totalRows=" + totalRows + ", totalLength=" + totalLength
				+ ", bucketMetaData=" + bucketMetaData + "]\n";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Bucket) {
			if (this.getBucketTerms().size() != ((Bucket) obj).getBucketTerms().size()) {
				return false;
			} else {
				for (BucketTerms term : ((Bucket) obj).getBucketTerms()) {
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

		for (BucketTerms t : bucketTerms) {
			hash += t.getAttributeName().hashCode();
		}
		return hash;
	}
}
