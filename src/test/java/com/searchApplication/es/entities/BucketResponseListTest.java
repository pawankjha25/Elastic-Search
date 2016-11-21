package com.searchApplication.es.entities;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;

import org.fest.assertions.api.Assertions;
import org.junit.Test;

import com.searchApplication.es.search.bucketing.Bucket;
import com.searchApplication.es.search.bucketing.BucketMetaData;
import com.searchApplication.es.search.bucketing.BucketTerms;

public class BucketResponseListTest {

	@Test
	public void test() {
		BucketTerms bte = new BucketTerms("a", 1, true, 1);
		BucketTerms bte1 = new BucketTerms("b", 1, true, 1);

		Bucket b = new Bucket(new HashSet(Arrays.asList(bte, bte1)), 1, 0, 0);

		BucketMetaData meta = new BucketMetaData("superRegion", "sector", "subSector");
		BucketMetaData meta1 = new BucketMetaData("superRegion", "sector", "subSector");
		BucketMetaData meta2 = new BucketMetaData("region", "sector", "subSector");

		b.addMetaData(meta);
		b.addMetaData(meta1);
		b.addMetaData(meta2);
		Bucket b1 = new Bucket(new HashSet(Arrays.asList(bte1)), 1, 0, 0);

		b1.addMetaData(meta);

		BucketResponseList l = BucketResponseList.buildFromBucketList(Arrays.asList(b, b1), "q");
		Assertions.assertThat(l.getSearchResponse()).hasSize(3);
		BucketResponse[] buckets = l.getSearchResponse().toArray(new BucketResponse[3]);
		assertBucket(buckets[0], "a|b|", "sector", "subSector", "superRegion");
		assertBucket(buckets[1], "a|b|", "sector", "subSector", "region");

		assertBucket(buckets[2], "b|", "sector", "subSector", "superRegion");

	}

	private void assertBucket(BucketResponse b, String suggestion, String sector, String subSector,
			String superRegion) {
		Assertions.assertThat(b.getSector()).isEqualTo(sector);
		Assertions.assertThat(b.getSubSector()).isEqualTo(subSector);
		Assertions.assertThat(b.getSuperRegion()).isEqualTo(superRegion);
		Assertions.assertThat(b.getSector()).isEqualTo(sector);

	}

}
