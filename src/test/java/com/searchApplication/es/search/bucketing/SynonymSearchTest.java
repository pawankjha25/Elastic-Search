package com.searchApplication.es.search.bucketing;

import org.fest.assertions.api.Assertions;
import org.junit.Test;

public class SynonymSearchTest {

	@Test
	public void test() throws Exception {
		Assertions.assertThat(new SynonymSearch().getSynonims(new String[]{"airdata"})).hasSize(0);
	}

}
