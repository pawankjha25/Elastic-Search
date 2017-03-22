package com.searchApplication.es.search.aggs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fest.assertions.api.Assertions;
import org.junit.Test;

import com.searchApplication.es.entities.DBData;
import com.searchApplication.es.entities.Row;
import com.searchApplication.es.entities.RowAttributes;

public class SectorBreakDownAggregationTest extends com.searchApplication.es.search.bucketing.SearchESTest {

	public void createTestIndex() throws IOException {
		createIndex(TEST_INDEX_NAME, TYPE_NAME, DEFAULT_ARTICLE_MAPPING);
	}

	@Test
	public void test() throws IOException {
		createTestIndex();

		Row r = new Row();
		r.setDescription(Arrays.asList("corn", "production", "x"));
		DBData db = new DBData();
		db.setDb_name("db");
		db.setProperty(1);
		r.setDb(db);
		r.setSector("sector");
		r.setSub_sector("sub_sector");
		r.setSuper_region("region");
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
		r3.setSuper_region("region");

		index(r3, 4);

		Row r4 = createAtrributeFromList("soccer|transfer data");
		r4.setSector("sector");
		r4.setSub_sector("subSector1");
		r4.setSuper_region("region1");

		index(r4, 5);

		Row r5 = createAtrributeFromList("soccer|transfer data");
		r4.setSector("sector");
		r4.setSub_sector("subSector1");
		r4.setSuper_region("region2");

		index(r5, 6);
		
		List<InsdustriInfo> info = SectorBreakDownAggregation.getSectors(client(), TEST_INDEX_NAME);
		Assertions.assertThat(info).hasSize(2);
		Assertions.assertThat(info.get(0).getRegions()).hasSize(2);
		Assertions.assertThat(info.get(0).getRegions().get(0).getSubsectors()).hasSize(2);
		Assertions.assertThat(info.get(0).getRegions().get(1).getSubsectors()).hasSize(1);

		Assertions.assertThat(info.get(1).getRegions()).hasSize(1);
		Assertions.assertThat(info.get(1).getRegions().get(0).getSubsectors()).hasSize(1);


	}

	private Row createAtrributeFromList(String attributes) {
		Row r = new Row();
		String[] attNames = attributes.split("\\|");
		r.setDescription(Arrays.asList(attNames));
		List<RowAttributes> atts = new ArrayList<RowAttributes>();
		for (String name : attNames) {
			RowAttributes a = new RowAttributes(name, "a", "a", "a", 0);
			atts.add(a);
		}
		r.setAttributes(atts);
		return r;
	}

}
