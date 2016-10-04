package com.searchApplication.es.search.bucketing;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class BucketBuildersTest {

	@Test
	public void testPerfectMatch() {
		Bucket b = BucketBuilders.createFromQueryString("corn production", Arrays.asList("corn production", "xs"));
		Assert.assertEquals(b.getTotalPerfectMatches(), 2);
		Assert.assertEquals(b.getTotalPartialMatches(), 0);
		Assert.assertEquals(b.getTotalLevenstheinDistance(), 0);
		Assert.assertTrue(b.getBucketTerms().contains("corn production"));
		Assert.assertEquals(b.getBucketTerms().size(), 1);

		Bucket b1 = BucketBuilders.createFromQueryString("corn production", Arrays.asList("production", "corn", "xs"));
		Assert.assertEquals(b1.getTotalPerfectMatches(), 2);
		Assert.assertEquals(b1.getTotalPartialMatches(), 0);
		Assert.assertEquals(b1.getTotalLevenstheinDistance(), 0);
		Assert.assertTrue(b1.getBucketTerms().contains("corn"));
		Assert.assertTrue(b1.getBucketTerms().contains("production"));

		Bucket b2 = BucketBuilders.createFromQueryString("corn production", Arrays.asList("corn production", "corn"));
		Assert.assertEquals(b2.getTotalPerfectMatches(), 3);
		Assert.assertEquals(b2.getTotalPartialMatches(), 0);
		Assert.assertEquals(b2.getTotalLevenstheinDistance(), 0);
		Assert.assertTrue(b2.getBucketTerms().contains("corn production"));
		Assert.assertEquals(b2.getBucketTerms().size(), 2);

	}

	@Test
	public void testStems() {
		Bucket b = BucketBuilders.createFromQueryString("run", Arrays.asList("running shoes"));
		Assert.assertEquals(b.getTotalPerfectMatches(), 1);

	}

	@Test
	public void testStopWords() {
		Bucket b = BucketBuilders.createFromQueryString("production in us", Arrays.asList("production in us"));
		Assert.assertEquals(b.getTotalPerfectMatches(), 2);

		Bucket b1 = BucketBuilders.createFromQueryString("production in us", Arrays.asList("corn production in china"));
		Assert.assertEquals(b1.getTotalPerfectMatches(), 1);

		Bucket b2 = BucketBuilders.createFromQueryString("corn production", Arrays.asList("corn production in china"));
		Assert.assertEquals(b2.getTotalPerfectMatches(), 2);

	}

	@Test
	public void testPartialMatch() {
		Bucket b = BucketBuilders.createFromQueryString("corn production",
				Arrays.asList("iron production", "corn mining"));
		Assert.assertEquals(b.getTotalPerfectMatches(), 2);
		Assert.assertEquals(b.getTotalPartialMatches(), 0);
		Assert.assertTrue(b.getBucketTerms().contains("iron production"));
		Assert.assertTrue(b.getBucketTerms().contains("corn mining"));

		Assert.assertEquals(b.getBucketTerms().size(), 2);
		Assert.assertEquals(b.getTotalLevenstheinDistance(), 0);

	}

	@Test
	public void testStrange() {

		Bucket b = BucketBuilders.createFromQueryString("corn production", Arrays.asList("tons", "1000 acers"));
		Assert.assertNull(b);

		b = BucketBuilders.createFromQueryString("corn", Arrays.asList("production"));
		Assert.assertNull(b);

		b = BucketBuilders.createFromQueryString("whey", Arrays.asList("wheat"));
		Assert.assertEquals(b.getTotalPerfectMatches(), 0);
		Assert.assertEquals(b.getTotalPartialMatches(), 1);

		
	}

	@Test
	public void testLocationAttrMatch() {
		Bucket b = BucketBuilders.createFromQueryString("kansas", Arrays.asList("CENSUS", "KANSAS_LOC",
				"COTTON - PRODUCTION", "Cotton", "FIELD CROPS: CORN, LEGUMES ETC"));
		System.err.println(b);
	}
	@Test
	public void testPopcorn() {

		Bucket b = BucketBuilders.createFromQueryString("corn production",
				Arrays.asList("popcorn", "popcorn production"));
		Assert.assertEquals(b.getTotalPerfectMatches(), 1);
		Assert.assertEquals(b.getTotalPartialMatches(), 2);

		Bucket b1 = BucketBuilders.createFromQueryString("corn production",
				Arrays.asList("popcor", "popcorn production"));
		Assert.assertEquals(b1.getTotalPerfectMatches(), 1);
		Assert.assertEquals(b1.getTotalPartialMatches(), 1);

	}

	@Test
	public void testShort() {

		Bucket b = BucketBuilders.createFromQueryString("row", Arrays.asList("toe", "popcorn production"));
		Assert.assertNull(b);
	}

	@Test
	public void testLocation() {

		Bucket b = BucketBuilders.createFromQueryString("corn production illinois",
				Arrays.asList("corn production", "illinois_LOC", "corn", "united states_LOC"));
		Assert.assertEquals(b.getTotalPerfectMatches(), 4);
		Assert.assertEquals(b.getTotalPartialMatches(), 0);

		Bucket b1 = BucketBuilders.createFromQueryString("corn production united states",
				Arrays.asList("corn production", "united_states_LOC"));
		Assert.assertEquals(b1.getTotalPerfectMatches(), 4);
		Assert.assertEquals(b1.getTotalPartialMatches(), 0);

	}

	@Test
	public void exactTestLocation() {
		Bucket b2 = BucketBuilders.createFromQueryString("illinois",
				Arrays.asList("corn production", "illinoi_LOC", "corn", "united states_LOC"));
		Assert.assertNull(b2);
	}

}
