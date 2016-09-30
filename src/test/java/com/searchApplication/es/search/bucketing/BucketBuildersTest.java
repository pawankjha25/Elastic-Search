package com.searchApplication.es.search.bucketing;

import static org.junit.Assert.*;

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
	public void testPartialMatch() {
		Bucket b = BucketBuilders.createFromQueryString("corn production", Arrays.asList("iron production", "corn mining"));
		Assert.assertEquals(b.getTotalPerfectMatches(), 2);
		Assert.assertEquals(b.getTotalPartialMatches(), 0);
		Assert.assertTrue(b.getBucketTerms().contains("iron production"));
		Assert.assertTrue(b.getBucketTerms().contains("corn mining"));

		Assert.assertEquals(b.getBucketTerms().size(), 2);
		Assert.assertEquals(b.getTotalLevenstheinDistance(), 0);


	}

}
