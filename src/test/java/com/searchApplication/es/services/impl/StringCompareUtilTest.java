package com.searchApplication.es.services.impl;

import org.junit.Assert;
import org.junit.Test;

public class StringCompareUtilTest {

	@Test
	public void test() {
		Assert.assertEquals(StringCompareUtil.editDistance("kitten", "sitting" ), 3d, 0);
		Assert.assertEquals(StringCompareUtil.editDistance("123", "12" ), 1d, 0);

	
	}

}
