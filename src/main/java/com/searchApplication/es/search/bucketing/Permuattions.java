package com.searchApplication.es.search.bucketing;

import java.util.List;

public class Permuattions {
	public static void generatePermutations(List<List<String>> Lists, List<String> result, int depth, String current) {
		if (depth == Lists.size()) {
			result.add(current);
			return;
		}

		for (int i = 0; i < Lists.get(depth).size(); ++i) {
			generatePermutations(Lists, result, depth + 1, current + " "+ Lists.get(depth).get(i));
		}
	}
}
