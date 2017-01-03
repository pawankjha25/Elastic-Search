package com.searchApplication.es.search.bucketing;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class BucketTest {

	@Test
	public void test() {
		BucketTerms bta = new BucketTerms("a", 1, true, 1);
		BucketTerms bta1 = new BucketTerms("a1", 1, true, 1);
		BucketTerms bta2 = new BucketTerms("a2", 1, true, 1);
		BucketTerms bta3 = new BucketTerms("a3", 1, true, 1);
		
		BucketTerms bta4 = new BucketTerms("a4", 1, true, 1);
		BucketTerms bta5 = new BucketTerms("a5", 1, true, 1);


		Bucket b = new Bucket(new HashSet<>(Arrays.asList(bta)), 1, 0, 0);
		Bucket b1 = new Bucket(new HashSet<>(Arrays.asList(bta1)), 1, 1, 0);
		Bucket b2 = new Bucket(new HashSet<>(Arrays.asList(bta2)), 1, 1, 1);
		Bucket b3 = new Bucket(new HashSet<>(Arrays.asList(bta3)), 2, 0, 1000);

		Bucket b4 = new Bucket(new HashSet<>(Arrays.asList(bta4)), 0, 2, 0);
		Bucket b5 = new Bucket(new HashSet<>(Arrays.asList(bta5)), 1, 0, 1);

		List<Bucket> list = Arrays.asList(b, b1, b2, b3, b4, b5);

		Collections.sort(list);

		Assert.assertEquals(BucketTerms.createdQuerySortedBucket(list.get(0).getBucketTerms()).toArray()[0], "a3");
		Assert.assertEquals(BucketTerms.createdQuerySortedBucket(list.get(1).getBucketTerms()).toArray()[0], "a");
		Assert.assertEquals(BucketTerms.createdQuerySortedBucket(list.get(2).getBucketTerms()).toArray()[0], "a1");
		Assert.assertEquals(BucketTerms.createdQuerySortedBucket(list.get(3).getBucketTerms()).toArray()[0], "a2");
		Assert.assertEquals(BucketTerms.createdQuerySortedBucket(list.get(4).getBucketTerms()).toArray()[0], "a5");
		Assert.assertEquals(BucketTerms.createdQuerySortedBucket(list.get(5).getBucketTerms()).toArray()[0], "a4");

	}

	@Test
	public void testResults() {
		BucketTerms bta = new BucketTerms("a", 1, true, 1);
		BucketTerms bta1 = new BucketTerms("b", 1, true, 1);
		BucketTerms bta2 = new BucketTerms("b", 1, true, 1);

		Bucket b = new Bucket(new HashSet<>(Arrays.asList(bta, bta1)), 1, 0, 0);
		Bucket b1 = new Bucket(new HashSet<>(Arrays.asList(bta1, bta)), 1, 1, 0);
		Bucket b2 = new Bucket(new HashSet<>(Arrays.asList(bta, bta1)), 1, 1, 1);
		Bucket b3 = new Bucket(new HashSet<>(Arrays.asList(bta2)), 2, 0, 1000);

		Assert.assertTrue(b.equals(b1));
		Assert.assertTrue(b.equals(b2));
		Assert.assertTrue(b2.equals(b1));
		Assert.assertFalse(b.equals(b3));

	}

	@Test
	public void testIncremenet() {
		BucketTerms bta = new BucketTerms("a", 1, true, 1);
		BucketTerms bta1 = new BucketTerms("b", 1, true, 1);
		Bucket b = new Bucket(new HashSet<>(Arrays.asList(bta, bta1)), 1, 0, 0);
		b.setTotalRows(1);
		b.incrementCount();
		Assert.assertEquals(2, b.getTotalRows());
	}

	@Test
	public void testMeta() {
		BucketTerms bta = new BucketTerms("a", 1, true, 1);
		BucketTerms bta1 = new BucketTerms("b", 1, true, 1);
		Bucket b = new Bucket(new HashSet<>(Arrays.asList(bta, bta1)), 1, 0, 0);
		BucketMetaData meta = new BucketMetaData("superRegion", "sector", "subSector");
		BucketMetaData meta1 = new BucketMetaData("superRegion", "sector", "subSector");
		BucketMetaData meta2 = new BucketMetaData("region", "sector", "subSector");

		b.addMetaData(meta);
		b.addMetaData(meta1);
		b.addMetaData(meta2);

		List<BucketMetaData> metaList = b.getBucketMetaData();
		Assert.assertEquals(2, metaList.size());
		Assert.assertEquals(new BucketMetaData("superRegion", "sector", "subSector"), metaList.get(0));
		Assert.assertEquals(2, metaList.get(0).getTotal());

		Assert.assertEquals(1, metaList.get(1).getTotal());

	}

	@Test
	public void testCompare() {
		Bucket b = BucketBuilders.createFromQueryString("corn production", Arrays.asList("corn", "production"),
				new HashSet<String>());
		Bucket b1 = BucketBuilders.createFromQueryString("corn production", Arrays.asList("corn production"),
				new HashSet<String>());
		List<Bucket> lb = Arrays.asList(b, b1);
		Collections.sort(lb);
	}

}
