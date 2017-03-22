package com.searchApplication.es.search.bucketing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fest.assertions.api.Assertions;
import org.junit.Test;

import com.searchApplication.es.search.bucketing.wordnet.WordNetSynonims;

public class PermuattionsTest {

	@Test
	public void test() throws IOException{
		List<List<String>> qs = new ArrayList<List<String>>();
		qs.add(WordNetSynonims.getSingleton().testDictionary("dog"));
		qs.add(WordNetSynonims.getSingleton().testDictionary("love"));
		List<String> synQueries = new ArrayList<String>();
		Permuattions.generatePermutations(qs, synQueries, 0, "");
		Assertions.assertThat(synQueries).hasSize(210);
	}

}
