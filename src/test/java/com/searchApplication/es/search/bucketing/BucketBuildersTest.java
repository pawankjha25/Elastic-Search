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
		
		
		Bucket b1 = BucketBuilders.createFromQueryString("corn production", Arrays.asList("production","corn", "xs"));
		Assert.assertEquals(b1.getTotalPerfectMatches(), 2);
		Assert.assertEquals(b1.getTotalPartialMatches(), 0);
		Assert.assertEquals(b1.getTotalLevenstheinDistance(), 0);
		Assert.assertTrue(b1.getBucketTerms().contains("corn"));
		Assert.assertTrue(b1.getBucketTerms().contains("production"));

		Assert.assertEquals(b1.getBucketTerms().size(), 2);

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
		
		Bucket b1= BucketBuilders.createFromQueryString("production in us", Arrays.asList("corn production in china"));
		Assert.assertEquals(b1.getTotalPerfectMatches(), 1);
		
		Bucket b2 = BucketBuilders.createFromQueryString("corn production", Arrays.asList("corn production in china"));
		Assert.assertEquals(b2.getTotalPerfectMatches(), 2);

	}
	
	@Test
	public void testPartialMatch() {
		Bucket b = BucketBuilders.createFromQueryString("corn production", Arrays.asList("iron production", "corn mining"));
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
		Assert.assertEquals(b.getTotalPerfectMatches(), 0);
		Assert.assertEquals(b.getTotalPartialMatches(), 1);
		Assert.assertTrue(b.getBucketTerms().contains("tons"));

	
	}

}
