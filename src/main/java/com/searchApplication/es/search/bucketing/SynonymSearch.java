package com.searchApplication.es.search.bucketing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.searchApplication.es.search.bucketing.wordnet.WordNetSynonims;

public class SynonymSearch {

	public static List<String> getSynonims(String[] querySplit) throws IOException {

		List<List<String>> queries = new ArrayList<List<String>>();
		for (String q : querySplit[0].split(" ")) {
			if (q.length() > 2) {
				List<String> qs = WordNetSynonims.getSingleton().testDictionary(q);
				queries.add(qs);
			}
		}
		List<String> synonimQueries = new ArrayList<String>();
		Permuattions.generatePermutations(queries, synonimQueries, 0, "");
		return synonimQueries;
	}
}
