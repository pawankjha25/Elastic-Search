package com.searchApplication.es.search.bucketing;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fest.assertions.api.Assertions;
import org.junit.Test;

public class AggregatorTest {

	@Test
	public void testMulti() {
		Set<String> queryMatchA = new HashSet<String>(Arrays.asList("a b"));
		

		BucketTerms bta = new BucketTerms("a b", 2, true, 1);
		bta.setMatchedQueries(queryMatchA);
		bta.setQueryWordMatch(1);

		BucketTerms bta1 = new BucketTerms("a b y", 2, false, 1);
		bta1.setMatchedQueries(queryMatchA);
		bta.setQueryWordMatch(1);

		BucketTerms bta2 = new BucketTerms("a b z", 2, false, 1);
		bta2.setMatchedQueries(queryMatchA);
		bta2.setQueryWordMatch(2);
		BucketTerms bta3 = new BucketTerms("a b a3  ", 2, false, 1);
		bta3.setMatchedQueries(queryMatchA);

		BucketTerms bta4 = new BucketTerms("a b a4", 1, false, 1);
		bta4.setMatchedQueries(queryMatchA);

		BucketTerms bta5 = new BucketTerms("a b a5", 1, false, 1);
		bta5.setMatchedQueries(queryMatchA);

		Bucket b = new Bucket(new HashSet<>(Arrays.asList(bta, bta1)), 1, 0, 10);
		Bucket b1 = new Bucket(new HashSet<>(Arrays.asList(bta, bta2)), 1, 0, 10);
		Bucket b2 = new Bucket(new HashSet<>(Arrays.asList(bta, bta2, bta1)), 1, 0, 10);
		Bucket b3 = new Bucket(new HashSet<>(Arrays.asList(bta, bta3)), 1, 0, 1000);

		Bucket b4 = new Bucket(new HashSet<>(Arrays.asList(bta4)), 1, 0, 0);
		Bucket b5 = new Bucket(new HashSet<>(Arrays.asList(bta5)), 1, 0, 1);

		List<Bucket> list = Arrays.asList(b, b1, b2, b3, b4, b5);
		List<Bucket> aggegatedList = Aggregator.generateAggregated(list);
		
		Assertions.assertThat(aggegatedList).hasSize(3);
		String[] bucketsNames = new String[aggegatedList.size()];
		for (int i = 0; i < aggegatedList.size(); i++) {
			String x = "";
			for (BucketTerms bt : aggegatedList.get(i).getBucketTerms()) {
				x += bt.getAttributeName() + "|";
			}
			bucketsNames[i] = x;
		}
		System.out.println(Arrays.toString(bucketsNames));
		Assertions.assertThat(bucketsNames[0]).isEqualTo("a b|");
		Assertions.assertThat(bucketsNames[1]).isEqualTo("a b a4|");
		Assertions.assertThat(bucketsNames[2]).isEqualTo("a b a5|");


	}
	
	@Test
	public void test() {
		Set<String> queryMatchA = new HashSet<String>(Arrays.asList("a"));
		Set<String> queryMatchB = new HashSet<String>(Arrays.asList("b"));
		Set<String> queryMatchAB = new HashSet<String>(Arrays.asList("a", "b"));

		BucketTerms bta = new BucketTerms("a", 1, true, 1);
		bta.setMatchedQueries(queryMatchA);
		bta.setQueryWordMatch(1);

		BucketTerms bta1 = new BucketTerms("b", 1, true, 1);
		bta1.setMatchedQueries(queryMatchB);
		bta.setQueryWordMatch(1);

		BucketTerms bta2 = new BucketTerms("a b", 1, true, 1);
		bta2.setMatchedQueries(queryMatchAB);
		bta2.setQueryWordMatch(2);
		BucketTerms bta3 = new BucketTerms("a3", 1, true, 1);
		bta3.setMatchedQueries(queryMatchA);

		BucketTerms bta4 = new BucketTerms("a4", 1, true, 1);
		bta4.setMatchedQueries(queryMatchA);

		BucketTerms bta5 = new BucketTerms("a5", 1, true, 1);
		bta5.setMatchedQueries(queryMatchA);

		Bucket b = new Bucket(new HashSet<>(Arrays.asList(bta, bta1)), 2, 0, 10);
		Bucket b1 = new Bucket(new HashSet<>(Arrays.asList(bta1, bta, bta2)), 2, 0, 10);
		Bucket b2 = new Bucket(new HashSet<>(Arrays.asList(bta2, bta1)), 2, 0, 10);
		Bucket b3 = new Bucket(new HashSet<>(Arrays.asList(bta2, bta3)), 2, 0, 1000);

		Bucket b4 = new Bucket(new HashSet<>(Arrays.asList(bta4)), 1, 0, 0);
		Bucket b5 = new Bucket(new HashSet<>(Arrays.asList(bta5)), 1, 0, 1);

		List<Bucket> list = Arrays.asList(b, b1, b2, b3, b4, b5);
		List<Bucket> aggegatedList = Aggregator.generateAggregated(list);
		Assertions.assertThat(aggegatedList).hasSize(4);
		String[] bucketsNames = new String[aggegatedList.size()];
		for (int i = 0; i < aggegatedList.size(); i++) {
			String x = "";
			for (BucketTerms bt : aggegatedList.get(i).getBucketTerms()) {
				x += bt.getAttributeName() + "|";
			}
			bucketsNames[i] = x;
		}
		Assertions.assertThat(bucketsNames[0]).isEqualTo("a|b|");
		Assertions.assertThat(bucketsNames[1]).isEqualTo("a b|");
		Assertions.assertThat(bucketsNames[2]).isEqualTo("a4|");
		Assertions.assertThat(bucketsNames[3]).isEqualTo("a5|");


	}
	
	@Test
	public void testWithMetaData() {
		Set<String> queryMatchA = new HashSet<String>(Arrays.asList("a"));
		Set<String> queryMatchB = new HashSet<String>(Arrays.asList("b"));
		Set<String> queryMatchAB = new HashSet<String>(Arrays.asList("a", "b"));

		BucketTerms bta = new BucketTerms("a", 1, true, 1);
		bta.setMatchedQueries(queryMatchA);
		bta.setQueryWordMatch(1);

		BucketTerms bta1 = new BucketTerms("b", 1, true, 1);
		bta1.setMatchedQueries(queryMatchB);
		bta.setQueryWordMatch(1);

		BucketTerms bta2 = new BucketTerms("a b", 1, true, 1);
		bta2.setMatchedQueries(queryMatchAB);
		bta2.setQueryWordMatch(2);
		BucketTerms bta3 = new BucketTerms("a3", 1, true, 1);
		bta3.setMatchedQueries(queryMatchA);

		BucketTerms bta4 = new BucketTerms("a4", 1, true, 1);
		bta4.setMatchedQueries(queryMatchA);

		BucketTerms bta5 = new BucketTerms("a5", 1, true, 1);
		bta5.setMatchedQueries(queryMatchA);

		Bucket b = new Bucket(new HashSet<>(Arrays.asList(bta, bta1)), 2, 0, 10);
		b.setBucketMetaData(new ArrayList<BucketMetaData>(Arrays.asList(new BucketMetaData("region", "sector", "subSector"))));
		Bucket b1 = new Bucket(new HashSet<>(Arrays.asList(bta1, bta, bta2)), 2, 0, 10);
		b1.setBucketMetaData(new ArrayList<BucketMetaData>(Arrays.asList(new BucketMetaData("region1", "sector1", "subSector1"))));

		Bucket b2 = new Bucket(new HashSet<>(Arrays.asList(bta2, bta1)), 2, 0, 10);
		b2.setBucketMetaData(new ArrayList<BucketMetaData>(Arrays.asList(new BucketMetaData("region1", "sector1", "subSector1"))));

		Bucket b3 = new Bucket(new HashSet<>(Arrays.asList(bta2, bta3)), 2, 0, 1000);

		Bucket b4 = new Bucket(new HashSet<>(Arrays.asList(bta4)), 1, 0, 0);
		Bucket b5 = new Bucket(new HashSet<>(Arrays.asList(bta5)), 1, 0, 1);

		List<Bucket> list = Arrays.asList(b, b1, b2, b3, b4, b5);
		List<Bucket> aggregatedList = Aggregator.generateAggregated(list);
		Assertions.assertThat(aggregatedList).hasSize(4);
		Assertions.assertThat(aggregatedList.get(0).getBucketMetaData()).hasSize(2);
		String[] bucketsNames = new String[aggregatedList.size()];
		for (int i = 0; i < aggregatedList.size(); i++) {
			String x = "";
			for (BucketTerms bt : aggregatedList.get(i).getBucketTerms()) {
				x += bt.getAttributeName() + "|";
			}
			bucketsNames[i] = x;
		}
		Assertions.assertThat(bucketsNames[0]).isEqualTo("a|b|");
		Assertions.assertThat(bucketsNames[1]).isEqualTo("a b|");
		Assertions.assertThat(bucketsNames[2]).isEqualTo("a4|");
		Assertions.assertThat(bucketsNames[3]).isEqualTo("a5|");


	}

}
