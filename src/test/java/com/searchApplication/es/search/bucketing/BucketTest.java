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
		Bucket b = new Bucket(new HashSet<String>(Arrays.asList("a")), 1, 0, 0);
		Bucket b1 = new Bucket(new HashSet<String>(Arrays.asList("a1")), 1, 1, 0);
		Bucket b2 = new Bucket(new HashSet<String>(Arrays.asList("a2")), 1, 1, 1);
		Bucket b3 = new Bucket(new HashSet<String>(Arrays.asList("a3")), 2, 0, 1000);

		Bucket b4 = new Bucket(new HashSet<String>(Arrays.asList("a4")), 0, 2, 0);
		Bucket b5 = new Bucket(new HashSet<String>(Arrays.asList("a5")), 1, 0, 1);

		List<Bucket> list = Arrays.asList(b, b1, b2, b3, b4, b5);

		Collections.sort(list);

		Assert.assertEquals(list.get(0).getBucketTerms().toArray()[0], "a3");
		Assert.assertEquals(list.get(1).getBucketTerms().toArray()[0], "a");
		Assert.assertEquals(list.get(2).getBucketTerms().toArray()[0], "a1");
		Assert.assertEquals(list.get(3).getBucketTerms().toArray()[0], "a2");
		Assert.assertEquals(list.get(4).getBucketTerms().toArray()[0], "a5");
		Assert.assertEquals(list.get(5).getBucketTerms().toArray()[0], "a4");

	}

	@Test
	public void testResults() {
		Bucket b = new Bucket(new HashSet<String>(Arrays.asList("a", "b")), 1, 0, 0);
		Bucket b1 = new Bucket(new HashSet<String>(Arrays.asList("b", "a")), 1, 1, 0);
		Bucket b2 = new Bucket(new HashSet<String>(Arrays.asList("a", "b")), 1, 1, 1);
		Bucket b3 = new Bucket(new HashSet<String>(Arrays.asList("a b")), 2, 0, 1000);

		Assert.assertTrue(b.equals(b1));
		Assert.assertTrue(b.equals(b2));
		Assert.assertTrue(b2.equals(b1));
		Assert.assertFalse(b.equals(b3));

	}

	@Test
	public void testIncremenet() {
		Bucket b = new Bucket(new HashSet<String>(Arrays.asList("a", "b")), 1, 0, 0);
		b.setTotalRows(1);
		b.incrementCount();
		Assert.assertEquals(2, b.getTotalRows());
	}

	@Test
	public void testMeta() {
		Bucket b = new Bucket(new HashSet<String>(Arrays.asList("a", "b")), 1, 0, 0);
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
		Bucket b = BucketBuilders.createFromQueryString("corn production", new HashSet<String>(),
				Arrays.asList("corn", "production"), new HashSet<String>());
		Bucket b1 = BucketBuilders.createFromQueryString("corn production", new HashSet<String>(),
				Arrays.asList("corn production"), new HashSet<String>());
		List<Bucket> lb = Arrays.asList(b, b1);
		Collections.sort(lb);
	}

}
