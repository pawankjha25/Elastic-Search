package com.searchApplication.es.services.impl;

import org.junit.Assert;
import org.junit.Test;

public class StringCompareUtilTest {

	@Test
	public void test() {
		Assert.assertEquals(StringCompareUtil.editDistance("kitten", "sitting" ), 3d, 0);
		Assert.assertEquals(StringCompareUtil.editDistance("123", "12" ), 1d, 0);

	
	}
	
	
	@Test
	public void testLCS() {
		Assert.assertEquals(StringCompareUtil.getLongestCommonSubsequence("kitten", "sitting" ), 4);
		Assert.assertEquals(StringCompareUtil.getLongestCommonSubsequence("123", "12" ), 2);

	
	}

}
