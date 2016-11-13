package com.searchApplication.es.search.bucketing;

import java.util.Arrays;
import java.util.HashSet;

import org.fest.assertions.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

public class BucketBuildersTest {

	@Test
	public void testPerfectMatch() {
		Bucket b = BucketBuilders.createFromQueryString("corn production", Arrays.asList("corn production", "xs"),
				new HashSet<String>());
		Assert.assertEquals(b.getTotalPerfectMatches(), 2);
		Assert.assertEquals(b.getTotalPartialMatches(), 0);
		Assert.assertEquals(b.getTotalLevenstheinDistance(), 0);
		Assert.assertTrue(b.getBucketTerms().contains("corn production"));
		Assert.assertEquals(b.getBucketTerms().size(), 1);

		Bucket b1 = BucketBuilders.createFromQueryString("corn production", Arrays.asList("production", "corn", "xs"),
				new HashSet<String>());
		Assert.assertEquals(b1.getTotalPerfectMatches(), 2);
		Assert.assertEquals(b1.getTotalPartialMatches(), 0);
		Assert.assertEquals(b1.getTotalLevenstheinDistance(), 0);
		Assert.assertTrue(b1.getBucketTerms().contains("corn"));
		Assert.assertTrue(b1.getBucketTerms().contains("production"));

		Bucket b2 = BucketBuilders.createFromQueryString("corn production", Arrays.asList("corn production", "corn"),
				new HashSet<String>());
		Assert.assertEquals(b2.getTotalPerfectMatches(), 2);
		Assert.assertEquals(b2.getTotalPartialMatches(), 0);
		Assert.assertEquals(b2.getTotalLevenstheinDistance(), 0);
		Assert.assertTrue(b2.getBucketTerms().contains("corn production"));
		Assert.assertEquals(b2.getBucketTerms().size(), 2);

	}

	@Test
	public void testStems() {
		Bucket b = BucketBuilders.createFromQueryString("run", Arrays.asList("running shoes"), new HashSet<String>());
		Assert.assertEquals(b.getTotalPerfectMatches(), 1);

	}

	@Test
	public void testStopWords() {
		Bucket b = BucketBuilders.createFromQueryString("production in us", Arrays.asList("production in us"),
				new HashSet<String>());
		Assert.assertEquals(b.getTotalPerfectMatches(), 2);

		Bucket b1 = BucketBuilders.createFromQueryString("production in us", Arrays.asList("corn production in china"),
				new HashSet<String>());
		Assert.assertEquals(b1.getTotalPerfectMatches(), 1);

		Bucket b2 = BucketBuilders.createFromQueryString("corn production", Arrays.asList("corn production in china"),
				new HashSet<String>());
		Assert.assertEquals(b2.getTotalPerfectMatches(), 2);

	}

	@Test
	public void testPartialMatch() {
		Bucket b = BucketBuilders.createFromQueryString("corn production",
				Arrays.asList("iron production", "corn mining"), new HashSet<String>());
		Assert.assertEquals(b.getTotalPerfectMatches(), 2);
		Assert.assertEquals(b.getTotalPartialMatches(), 0);
		Assert.assertTrue(b.getBucketTerms().contains("iron production"));
		Assert.assertTrue(b.getBucketTerms().contains("corn mining"));

		Assert.assertEquals(b.getBucketTerms().size(), 2);
		Assert.assertEquals(b.getTotalLevenstheinDistance(), 0);

	}

	@Test
	public void testStrange() {

		Bucket b = BucketBuilders.createFromQueryString("corn production", Arrays.asList("tons", "1000 acers"),
				new HashSet<String>());
		Assert.assertNull(b);

		b = BucketBuilders.createFromQueryString("corn", Arrays.asList("production"), new HashSet<String>());
		Assert.assertNull(b);

		b = BucketBuilders.createFromQueryString("whey", Arrays.asList("wheat"), new HashSet<String>());
		Assert.assertNull(b);

		b = BucketBuilders.createFromQueryString("berries california", Arrays.asList("BUSINESS AND DEMOGRAPHICS"),
				new HashSet<String>());
		Assert.assertNull(b);
	}

	@Test
	public void testPopcorn() {

		Bucket b = BucketBuilders.createFromQueryString("corn production",
				Arrays.asList("popcorn", "popcorn production"), new HashSet<String>());
		Assert.assertEquals(b.getTotalPerfectMatches(), 1);
		Assert.assertEquals(b.getTotalPartialMatches(), 0);

		Bucket b1 = BucketBuilders.createFromQueryString("corn production",
				Arrays.asList("popcor", "popcorn production"), new HashSet<String>());
		Assert.assertEquals(b1.getTotalPerfectMatches(), 1);
		Assert.assertEquals(b1.getTotalPartialMatches(), 0);

	}

	@Test
	public void testShort() {

		Bucket b = BucketBuilders.createFromQueryString("row", Arrays.asList("toe", "popcorn production"),
				new HashSet<String>());
		Assert.assertNull(b);
	}

	@Test
	public void testBucketOrder() {

		Bucket b = BucketBuilders.createFromQueryString("corn production illinois",
				Arrays.asList("corn production", "corn", "all production practices"), new HashSet<String>());
		Assert.assertEquals(b.getTotalPerfectMatches(), 2);
		Assert.assertEquals(b.getTotalPartialMatches(), 0);
		Assertions.assertThat(b.getBucketTerms()).containsExactly("corn production", "corn",
				"all production practices");

	}

	@Test
	public void testLocation() {

		Bucket b = BucketBuilders.createFromQueryString("corn production illinois",
				Arrays.asList("corn production", "corn"), new HashSet<String>());
		Assert.assertEquals(b.getTotalPerfectMatches(), 2);
		Assert.assertEquals(b.getTotalPartialMatches(), 0);

		Bucket b1 = BucketBuilders.createFromQueryString("corn production united states",
				Arrays.asList("corn production"), new HashSet<String>());
		Assert.assertEquals(b1.getTotalPerfectMatches(), 2);
		Assert.assertEquals(b1.getTotalPartialMatches(), 0);

	}

	@Test
	public void testOverlappingLocations() {
		Bucket b2 = BucketBuilders.createFromQueryString("corn in new york", Arrays.asList("corn production", "corn"),
				new HashSet<String>());
		Assert.assertEquals(b2.getTotalPerfectMatches(), 1);

	}

	@Test
	public void testMultiAttr() {
		Bucket b = BucketBuilders.createFromQueryString("corn production",
				Arrays.asList("ALL PRODUCTION PRACTICES", "CORN", "FOR ALCOHOL & OTHER PRODUCTS"),
				new HashSet<String>());
		Assert.assertEquals(2, b.getTotalPerfectMatches());

		Bucket b1 = BucketBuilders.createFromQueryString("corn production",
				Arrays.asList("DAIRY PRODUCT TOTALS", "ALL PRODUCTION PRACTICES", "ANIMALS & PRODUCTS"),
				new HashSet<String>());

		Assert.assertEquals(1, b1.getTotalPerfectMatches());

	}

	@Test
	public void testWithHits() {
		Bucket b = BucketBuilders.createFromQueryString("corn production",
				Arrays.asList("corn porduction", "CORN", "FOR ALCOHOL & OTHER PRODUCTS"),
				new HashSet<String>(Arrays.asList("corn", "production")));
		Assert.assertEquals(2, b.getTotalPerfectMatches());

	}

}
