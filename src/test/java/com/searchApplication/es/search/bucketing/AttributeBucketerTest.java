package com.searchApplication.es.search.bucketing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import com.searchApplication.es.entities.BucketResponseList;
import com.searchApplication.es.entities.DBData;
import com.searchApplication.es.entities.LocationData;
import com.searchApplication.es.entities.Row;
import com.searchApplication.es.entities.RowAttributes;

public class AttributeBucketerTest extends SearchESTest {

	private static Set<String> LOC = new HashSet<String>(Arrays.asList("illinois", "rice", "united states"));

	@Test
	public void testWithMeta() throws IOException {
		createTestIndex();
		Row r = new Row();
		r.setDescription(Arrays.asList("corn", "production", "x"));
		DBData db = new DBData();
		db.setDb_name("db");
		db.setProperty(1);
		r.setDb(db);
		r.setSector("sector");
		r.setSub_sector("sub_sector");
		r.setSuper_region("super");
		RowAttributes a1 = new RowAttributes("corn", "agro", "", "null", 0);
		RowAttributes a2 = new RowAttributes("priduction", "prd", "", "agro", 1);
		RowAttributes a3 = new RowAttributes("x", "x", "prd", "", 2);
		List<RowAttributes> atts = Arrays.asList(a1, a2, a3);
		r.setAttributes(atts);
		index(r, 1);

		Row r1 = r;
		index(r1, 2);

		Row r2 = r;
		r2.setSector("sector 1");
		index(r2, 3);

		List<Bucket> buckets = AttributeBucketer.createBucketList(client(), TEST_INDEX_NAME, TYPE_NAME, "corn", 1, 1000,
				LOC, false);
		org.junit.Assert.assertEquals(1, buckets.size());
		org.junit.Assert.assertEquals(2, buckets.get(0).getBucketMetaData().size());

	}

	@Test
	public void testProduceResponseBuckets() throws IOException {
		createTestIndex();

		Row r = new Row();
		r.setDescription(Arrays.asList("corn", "production", "x"));
		DBData db = new DBData();
		db.setDb_name("db");
		db.setProperty(1);
		r.setDb(db);
		r.setSector("sector");
		r.setSub_sector("sub_sector");
		r.setSuper_region("super");
		RowAttributes a1 = new RowAttributes("corn", "agro", "a", "null", 0);
		RowAttributes a2 = new RowAttributes("priduction", "prd", "a", "agro", 1);
		RowAttributes a3 = new RowAttributes("x", "x", "prd", "a", 2);
		List<RowAttributes> atts = Arrays.asList(a1, a2, a3);
		r.setAttributes(atts);
		index(r, 1);

		Row r1 = r;
		index(r1, 2);

		Row r2 = r;
		r2.setSector("sector 1");
		index(r2, 3);

		Row r3 = createAtrributeFromList("corn|corn production");
		r3.setSector("sector");
		r3.setSub_sector("subSector");
		r3.setSuper_region("superRegion");

		index(r3, 4);

		Row r4 = createAtrributeFromList("soccer|transfer data");
		r4.setSector("sector");
		r4.setSub_sector("subSector");
		r4.setSuper_region("superRegion");

		index(r4, 5);

		BucketResponseList buckets = AttributeBucketer.generateBuckets(client(), TEST_INDEX_NAME, TYPE_NAME, "corn", 1,
				1000, LOC, false);
	}

	/*
	 * row_1 (attributes): production planning|corn|x
	 * 
	 * 
	 * row_2: corn|production|x
	 * 
	 * row_3: iron production|mining
	 * 
	 * 
	 * row_4: popcorn|popcorn production
	 * 
	 * row_5: soccer|transfer data
	 * 
	 * row_6 : a|corn production|x
	 * 
	 * 
	 * row_7: yellow corn|a |x
	 * 
	 * Row 8 : a|production planning|iron
	 * 
	 * row_x1 : a|production planning|metal
	 * 
	 * row_x3 : a|production planning |
	 * 
	 * copper row_x4: a|production planning|gold
	 * 
	 * expected buckets:
	 * 
	 * 1st bucket: corn production
	 * 
	 * corn|production
	 * 
	 * production planning|corn
	 * 
	 * yellow corn
	 * 
	 * production planning iron production
	 * 
	 * popcorn|popcorn production (due to the 2 levensthein popcorn is not a
	 * match)
	 */

	@Test
	public void testMatches() throws Exception {
		createTestIndex();

		index(createAtrributeFromList("production planning|corn|x"), 1);

		index(createAtrributeFromList("iron production|mining"), 2);

		index(createAtrributeFromList("popcorn|popcorn production"), 3);

		index(createAtrributeFromList("soccer|transfer data"), 4);

		index(createAtrributeFromList("a|corn production|x"), 5);

		index(createAtrributeFromList("corn|production|x"), 6);

		index(createAtrributeFromList("yellow corn|a|x"), 7);

		index(createAtrributeFromList("a|production planning"), 9);
		index(createAtrributeFromList("a|production planning|metal"), 10);

		List<Bucket> buckets = AttributeBucketer.createBucketList(client(), TEST_INDEX_NAME, TYPE_NAME,
				"corn production", 1, 1000, LOC, false);
		System.out.println(buckets);
		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(0).getBucketTerms()))
				.containsOnly("corn production");
		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(1).getBucketTerms()))
				.containsOnly("corn", "production");

		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(2).getBucketTerms()))
				.containsOnly("corn", "production planning");

		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(3).getBucketTerms()))
				.containsOnly("production planning");

		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(4).getBucketTerms()))
				.containsOnly("yellow corn");

		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(5).getBucketTerms()))
				.containsOnly("iron production");

		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(3).getBucketTerms()))
				.containsOnly("production planning");

	}

	@Test
	public void testAggregated() throws Exception {
		createTestIndex();

		index(createAtrributeFromList("production planning|corn|x"), 1);

		index(createAtrributeFromList("iron production|mining"), 2);

		index(createAtrributeFromList("popcorn|popcorn production"), 3);

		index(createAtrributeFromList("soccer|transfer data"), 4);

		index(createAtrributeFromList("a|corn production|x"), 5);

		index(createAtrributeFromList("corn|corn production|x"), 6);

		index(createAtrributeFromList("yellow corn|a|x"), 7);

		index(createAtrributeFromList("a|corn production|production planning"), 9);
		index(createAtrributeFromList("a|production planning|metal"), 10);

		List<Bucket> buckets = AttributeBucketer.createBucketList(client(), TEST_INDEX_NAME, TYPE_NAME,
				"corn production", 1, 1000, LOC, false);
		buckets = Aggregator.generateAggregated(buckets);

		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(0).getBucketTerms()))
				.containsOnly("corn production");
		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(1).getBucketTerms()))
				.containsOnly("corn", "production planning");
		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(2).getBucketTerms()))
				.containsOnly("yellow corn");

		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(4).getBucketTerms()))
				.containsOnly("popcorn production");

		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(3).getBucketTerms()))
				.containsOnly("iron production");
		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(5).getBucketTerms()))
				.containsOnly("production planning");

	}

	/*
	 * row_1 wheat|production|x
	 * 
	 * row_2: production|wheat|x
	 * 
	 * row_3: iron production|mining wheat
	 * 
	 * row_4: wheat|wheat production
	 * 
	 * row_5: soccer|wheat production
	 * 
	 * expected
	 * 
	 * wheat, wheat production wheat production wheat, production minning wheat,
	 * iron production wheat
	 */

	@Test
	public void testMatch2() throws Exception {
		createTestIndex();

		index(createAtrributeFromList("wheat|production|x"), 1);

		index(createAtrributeFromList("production|wheat|x"), 2);

		index(createAtrributeFromList("mining wheat|iron production"), 3);

		index(createAtrributeFromList("wheat|wheat production"), 4);

		index(createAtrributeFromList("soccer|wheat production"), 5);

		index(createAtrributeFromList("x|wheat"), 6);

		List<Bucket> buckets = AttributeBucketer.createBucketList(client(), TEST_INDEX_NAME, TYPE_NAME,
				"wheat production", 10, 1000, LOC, false);

		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(0).getBucketTerms()))
				.containsExactly("wheat", "production");

		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(1).getBucketTerms()))
				.containsExactly("wheat production");

		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(2).getBucketTerms()))
				.containsExactly("wheat production", "wheat");

		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(3).getBucketTerms()))
				.containsExactly("mining wheat", "iron production");

		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(4).getBucketTerms()))
				.containsExactly("wheat");

	}

	private Row createAtrributeFromList(String attributes) {
		Row r = new Row();
		String[] attNames = attributes.split("\\|");
		r.setSector("sector");
		r.setSub_sector("sub_sector");
		r.setSuper_region("super");
		r.setDescription(Arrays.asList(attNames));
		List<RowAttributes> atts = new ArrayList<RowAttributes>();
		for (String name : attNames) {
			RowAttributes a = new RowAttributes(name, "a", "a", "a", 0);
			atts.add(a);
		}
		r.setAttributes(atts);
		return r;
	}

	@After
	public void close() {
		client().admin().indices().prepareClose(TEST_INDEX_NAME).get();

	}

	@Test
	public void testLocations() throws IOException {

		createTestIndex();
		LocationData loc = new LocationData();
		loc.setLocation_name("ILLINOIS");
		loc.setLocation_type("state");

		LocationData loc1 = new LocationData();
		loc1.setLocation_name("united states");
		loc1.setLocation_type("country");

		Row r3 = createAtrributeFromList("corn|corn production");
		r3.setSector("sector");
		r3.setSub_sector("subSector");
		r3.setSuper_region("superRegion");
		r3.setLocations(Arrays.asList(loc, loc1));

		Row r4 = createAtrributeFromList("soccer|transfer data");
		r4.setSector("sector");
		r4.setSub_sector("subSector");
		r4.setSuper_region("superRegion");

		r4.setLocations(Arrays.asList(loc1));

		Row r5 = createAtrributeFromList("corn|corn production");
		r5.setSector("sector");
		r5.setSub_sector("subSector");
		r5.setSuper_region("superRegion");

		index(r3, 3);
		index(r4, 4);
		index(r5, 5);

		List<Bucket> buckets = AttributeBucketer.createBucketList(client(), TEST_INDEX_NAME, TYPE_NAME,
				"corn production illinois", 1, 1000, LOC, false);

		Assertions.assertThat(buckets).hasSize(1);
		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(0).getBucketTerms()))
				.containsOnly("corn", "corn production", "ILLINOIS_LOC");

	}

	@Test
	public void testLocationsOnly() throws IOException {
		createTestIndex();
		LocationData loc = new LocationData();
		loc.setLocation_name("ILLINOIS");
		loc.setLocation_type("state");

		LocationData loc1 = new LocationData();
		loc1.setLocation_name("UNITED STATES");
		loc1.setLocation_type("country");

		Row r3 = createAtrributeFromList("corn|corn production");
		r3.setSector("sector");
		r3.setSub_sector("subSector");
		r3.setSuper_region("superRegion");
		r3.setLocations(Arrays.asList(loc));

		Row r4 = createAtrributeFromList("soccer|transfer data");
		r4.setSector("sector");
		r4.setSub_sector("subSector");
		r4.setSuper_region("superRegion");

		r4.setLocations(Arrays.asList(loc1));

		Row r5 = createAtrributeFromList("corn|corn x");
		r5.setSector("sector");
		r5.setSub_sector("subSector");
		r5.setSuper_region("superRegion");

		index(r3, 3);
		index(r4, 4);
		index(r5, 5);

		List<Bucket> buckets = AttributeBucketer.createBucketList(client(), TEST_INDEX_NAME, TYPE_NAME, "united states",
				1, 1000, LOC, false);

		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(0).getBucketTerms()))
				.containsOnly("UNITED STATES_LOC");

		buckets = AttributeBucketer.createBucketList(client(), TEST_INDEX_NAME, TYPE_NAME, "illinois", 1, 1000,
				LOC, false);

		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(0).getBucketTerms()))
				.containsOnly("ILLINOIS_LOC");

	}

	@Test
	public void testMatchesMulti() throws Exception {
		createTestIndex();

		index(createAtrributeFromList("production planning|corn|x"), 1);

		index(createAtrributeFromList("iron production|mining"), 2);

		index(createAtrributeFromList("popcorn|popcorn production"), 3);

		index(createAtrributeFromList("soccer|transfer data"), 4);

		index(createAtrributeFromList("a|corn production|x"), 5);

		index(createAtrributeFromList("corn|production|x"), 6);

		index(createAtrributeFromList("yellow corn|a|x"), 7);

		index(createAtrributeFromList("a|production planning"), 9);
		index(createAtrributeFromList("a|production planning|metal"), 10);

		List<Bucket> buckets = AttributeBucketer.createBucketList(client(), TEST_INDEX_NAME, TYPE_NAME, "corn", 1, 1000,
				LOC, false);
		System.out.println(buckets);
		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(0).getBucketTerms()))
				.containsOnly("corn");
		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(1).getBucketTerms()))
				.containsOnly("yellow corn");
		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(2).getBucketTerms()))
				.containsOnly("corn production");

	}

	@Test
	public void testLocationAttributeMatchSingleQueryWord() throws IOException {
		createTestIndex();
		LocationData loc = new LocationData();
		loc.setLocation_name("ILLINOIS");
		loc.setLocation_type("state");

		LocationData loc1 = new LocationData();
		loc1.setLocation_name("RICE STATES");
		loc1.setLocation_type("country");

		Row r4 = createAtrributeFromList("corn|corn production");
		r4.setSector("sector");
		r4.setSub_sector("subSector");
		r4.setSuper_region("superRegion");
		r4.setLocations(Arrays.asList(loc1));

		Row r3 = createAtrributeFromList("rice");
		r3.setSector("sector");
		r3.setSub_sector("subSector");
		r3.setSuper_region("superRegion");
		r3.setLocations(Arrays.asList(loc));

		index(r3, 3);

		index(r4, 4);

		List<Bucket> buckets = AttributeBucketer.createBucketList(client(), TEST_INDEX_NAME, TYPE_NAME, "rice", 1, 1000,
				LOC, false);

		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(0).getBucketTerms()))
				.containsOnly("rice");

	}

	@Test
	public void testLocationAttributeMatchMultiQueryWords() throws IOException {
		createTestIndex();
		LocationData loc = new LocationData();
		loc.setLocation_name("ILLINOIS");
		loc.setLocation_type("state");

		LocationData loc1 = new LocationData();
		loc1.setLocation_name("RICE STATES");
		loc1.setLocation_type("country");

		Row r4 = createAtrributeFromList("corn|corn production");
		r4.setSector("sector");
		r4.setSub_sector("subSector");
		r4.setSuper_region("superRegion");
		r4.setLocations(Arrays.asList(loc1));

		Row r3 = createAtrributeFromList("rice");
		r3.setSector("sector");
		r3.setSub_sector("subSector");
		r3.setSuper_region("superRegion");
		r3.setLocations(Arrays.asList(loc));

		index(r3, 3);

		index(r4, 4);

		List<Bucket> buckets = AttributeBucketer.createBucketList(client(), TEST_INDEX_NAME, TYPE_NAME,
				"rice production", 1, 1000, LOC, false);

		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(0).getBucketTerms()))
				.containsOnly("rice");

	}

	@Test
	public void testLocationAttributeMatchMultiQueryWordsLocation() throws IOException {
		createTestIndex();
		LocationData loc = new LocationData();
		loc.setLocation_name("ILLINOIS");
		loc.setLocation_type("state");

		LocationData loc1 = new LocationData();
		loc1.setLocation_name("RICE STATES");
		loc1.setLocation_type("country");

		Row r4 = createAtrributeFromList("corn|corn production");
		r4.setSector("sector");
		r4.setSub_sector("subSector");
		r4.setSuper_region("superRegion");
		r4.setLocations(Arrays.asList(loc1));

		Row r3 = createAtrributeFromList("rice");
		r3.setSector("sector");
		r3.setSub_sector("subSector");
		r3.setSuper_region("superRegion");
		r3.setLocations(Arrays.asList(loc));

		Row r5 = createAtrributeFromList("rice production");
		r5.setSector("sector");
		r5.setSub_sector("subSector");
		r5.setSuper_region("superRegion");
		r5.setLocations(Arrays.asList(loc1));

		index(r3, 3);

		index(r4, 4);

		index(r5, 5);

		List<Bucket> buckets = AttributeBucketer.createBucketList(client(), TEST_INDEX_NAME, TYPE_NAME,
				"rice production illinois", 1, 1000, LOC, false);
		System.out.println(buckets);
		Assertions.assertThat(BucketTerms.createdQuerySortedBucket(buckets.get(0).getBucketTerms()))
				.containsOnly("rice", "ILLINOIS_LOC");

	}

}
