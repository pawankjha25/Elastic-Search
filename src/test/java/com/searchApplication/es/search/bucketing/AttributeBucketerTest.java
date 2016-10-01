package com.searchApplication.es.search.bucketing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Test;

import com.searchApplication.es.entities.BucketResponseList;
import com.searchApplication.es.entities.DBData;
import com.searchApplication.es.entities.Row;
import com.searchApplication.es.entities.RowAttributes;

public class AttributeBucketerTest extends SearchESTest {

	@Test
	public void testWithMeta() throws IOException {
		createSpoozIndex();
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

		List<Bucket> buckets = AttributeBucketer.createBucketList(client(), TEST_INDEX_NAME, TYPE_NAME, "corn", 1);
		org.junit.Assert.assertEquals(1, buckets.size());
		org.junit.Assert.assertEquals(2, buckets.get(0).getBucketMetaData().size());

	}

	@Test
	public void testProduceResponseBuckets() throws IOException {
		createSpoozIndex();

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

		BucketResponseList buckets = AttributeBucketer.generateBuckets(client(), TEST_INDEX_NAME, TYPE_NAME, "corn", 1);
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
		createSpoozIndex();

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
				"corn production", 1);

		Assertions.assertThat(buckets.get(0).getBucketTerms()).containsOnly("corn production");
		Assertions.assertThat(buckets.get(1).getBucketTerms()).containsOnly("production", "corn");
		Assertions.assertThat(buckets.get(2).getBucketTerms()).containsOnly("corn", "production planning");
		Assertions.assertThat(buckets.get(3).getBucketTerms()).containsOnly("yellow corn");
		Assertions.assertThat(buckets.get(4).getBucketTerms()).containsOnly("iron production");
		Assertions.assertThat(buckets.get(5).getBucketTerms()).containsOnly("popcorn production");
		Assertions.assertThat(buckets.get(6).getBucketTerms()).containsOnly("production planning");

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
		createSpoozIndex();

		index(createAtrributeFromList("wheat|production|x"), 1);

		index(createAtrributeFromList("production|wheat|x"), 2);

		index(createAtrributeFromList("mining wheat|iron production"), 3);

		index(createAtrributeFromList("wheat|wheat production"), 4);

		index(createAtrributeFromList("soccer|wheat production"), 5);

		index(createAtrributeFromList("x|wheat"), 6);

		List<Bucket> buckets = AttributeBucketer.createBucketList(client(), TEST_INDEX_NAME, TYPE_NAME,
				"wheat production", 1);
		Assertions.assertThat(buckets.get(0).getBucketTerms()).containsOnly("wheat", "wheat production");
		Assertions.assertThat(buckets.get(1).getBucketTerms()).containsOnly("wheat production");

		Assertions.assertThat(buckets.get(2).getBucketTerms()).containsOnly("wheat", "production");

		Assertions.assertThat(buckets.get(3).getBucketTerms()).containsOnly("mining wheat", "iron production");

		Assertions.assertThat(buckets.get(4).getBucketTerms()).containsOnly("wheat");


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

}
