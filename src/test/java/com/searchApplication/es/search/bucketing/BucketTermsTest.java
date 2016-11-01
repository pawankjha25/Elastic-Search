package com.searchApplication.es.search.bucketing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.fest.assertions.api.Assertions;
import org.junit.Test;

public class BucketTermsTest {

	@Test
	public void test() {
		BucketTerms t = new BucketTerms("a", 1, true, 1);
		BucketTerms t1 = new BucketTerms("a b", 1, true, 2);
	
		Assertions.assertThat(t.compareTo(t1)).isEqualTo(1);
		List<BucketTerms> list = new ArrayList<BucketTerms>(Arrays.asList(t, t1));
		Collections.sort(list);
		Assertions.assertThat(list).containsExactly(t1, t);
		BucketTerms t2 = new BucketTerms("b", 2, true, 1);
		list.add(t2);
		Collections.sort(list);
		Assertions.assertThat(list).containsExactly(t1, t, t2);

		
		BucketTerms t3 = new BucketTerms("b b", 2, false, 1);
		
		list.add(0, t3);
		Collections.sort(list);
		Assertions.assertThat(list).containsExactly(t1, t, t2, t3);
		


	}
	@Test
	public void testOrder() {
		BucketTerms t = new BucketTerms("a", 3, true, 1);
		BucketTerms t1 = new BucketTerms("a b", 2, false, 1);
	
		Assertions.assertThat(t.compareTo(t1)).isEqualTo(1);
		List<BucketTerms> list = new ArrayList<BucketTerms>(Arrays.asList(t, t1));
		Collections.sort(list);
		Assertions.assertThat(list).containsExactly(t1, t);
	
		
	}

}
