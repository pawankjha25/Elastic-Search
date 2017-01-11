package com.searchApplication.es.filtering;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.searchApplication.entities.FilterRequest;
import com.searchApplication.entities.LocationAggrigation;
import com.searchApplication.es.entities.Attributes;
import com.searchApplication.es.entities.DatabaseInfo;
import com.searchApplication.es.entities.Locations;
import com.searchApplication.es.entities.TimeSeriesData;
import com.searchApplication.es.services.impl.Filtering;
import com.searchApplication.es.services.impl.Results;

public class FilteringAttributesAndLocTest extends SearchESTest {

	@Before
	public void index() throws Exception
	{
		createTestIndex();
		TimeSeriesData r = new TimeSeriesData();
		r.setDescription(Arrays.asList("corn", "production", "all production", "agriculture", "crops"));
		DatabaseInfo db = new DatabaseInfo();
		db.setDb_name("agriculture_value");
		db.setProperties((long) 1);
		r.setDb(db);
		r.setSector("agriculture");
		r.setSub_sector("crops");
		r.setSuper_region("UNITED STATES");
		Attributes a1 = new Attributes("corn", "Stratum1", "null", new Long(0));
		Attributes a2 = new Attributes("production", "Stratum2", "Stratum1", new Long(1));
		Attributes a3 = new Attributes("all production", "Stratum3", "Stratum2", new Long(2));
		Attributes a4 = new Attributes("agriculture", "Sector", "null", new Long(1));
		Attributes a5 = new Attributes("crops", "Sub-Sector", "Sector", new Long(2));
		List<Attributes> atts = Arrays.asList(a1, a2, a3, a4, a5);
		r.setAttributes(atts);

		Set<Locations> locations = new TreeSet<>();
		Locations l1 = new Locations("1", "UNITED STATES", "Country", "", "UNITED STATES");
		locations.add(l1);
		Locations l2 = new Locations("2", "UNITED STATES", "Country", "", "UNITED STATES");
		locations.add(l2);
		Locations l3 = new Locations("3", "ALABAMA", "State", "", "UNITED STATES");
		locations.add(l3);
		Locations l4 = new Locations("3", "UNITED STATES", "Country", "", "UNITED STATES");
		locations.add(l4);
		Locations l5 = new Locations("3", "ALABAMA", "State", "", "UNITED STATES");
		locations.add(l5);
		Locations l6 = new Locations("3", "COUNTY1", "County", "", "ALABAMA");
		locations.add(l6);

		r.setLocations(locations);

		//adding 5 similar rows 
		index(r, r.getDb().getDb_name() + ":" + r.getDb().getProperties());

		db.setProperties((long) 2);
		index(r, r.getDb().getDb_name() + ":" + r.getDb().getProperties());

		db.setProperties((long) 3);
		index(r, r.getDb().getDb_name() + ":" + r.getDb().getProperties());

		db.setProperties((long) 4);
		index(r, r.getDb().getDb_name() + ":" + r.getDb().getProperties());

		db.setProperties((long) 5);
		index(r, r.getDb().getDb_name() + ":" + r.getDb().getProperties());

		//adding different attribute and locations
		r = new TimeSeriesData();
		r.setDescription(Arrays.asList("meat", "production", "meat production", "meat", "animals"));

		db = new DatabaseInfo();
		db.setDb_name("Animals");
		db.setProperties((long) 1);

		r.setDb(db);
		r.setSector("meat");
		r.setSub_sector("animals");
		r.setSuper_region("UNITED STATES");
		a1 = new Attributes("meat", "Stratum1", "null", new Long(0));
		a2 = new Attributes("production", "Stratum2", "Stratum1", new Long(1));
		a3 = new Attributes("meat production", "Stratum3", "Stratum2", new Long(2));
		a4 = new Attributes("meat", "Sector", "null", new Long(1));
		a5 = new Attributes("animals", "Sub-Sector", "Sector", new Long(2));
		atts = Arrays.asList(a1, a2, a3, a4, a5);
		r.setAttributes(atts);

		locations = new TreeSet<>();
		l1 = new Locations("4", "UNITED STATES", "Country", "", "UNITED STATES");
		locations.add(l1);
		l2 = new Locations("5", "UNITED STATES", "Country", "", "UNITED STATES");
		locations.add(l2);
		l3 = new Locations("5", "ALABAMA", "State", "", "UNITED STATES");
		locations.add(l3);
		l4 = new Locations("6", "UNITED STATES", "Country", "", "UNITED STATES");
		locations.add(l4);
		l5 = new Locations("6", "ALABAMA", "State", "", "UNITED STATES");
		locations.add(l5);
		l6 = new Locations("6", "COUNTY1", "County", "", "ALABAMA");
		locations.add(l6);

		r.setLocations(locations);

		index(r, r.getDb().getDb_name() + ":" + r.getDb().getProperties());

	}

	@Test
	public void filteringTest() throws Exception
	{
		//first request for corn
		FilterRequest request = new FilterRequest();
		request.setSearchText("corn");
		request.setLocation(true);
		Map<String, List<String>> filters = new HashMap<>();
		filters.put("Stratum2", Arrays.asList("production"));
		request.setFilters(filters);

		//check if it has considered all 5 rows
		org.junit.Assert.assertEquals(new Long(5),
				Filtering.getFilteringResults(request, "time_series", "time_series", client()).getTotalRows());

		org.junit.Assert.assertEquals("corn",
				Filtering.getFilteringResults(request, "time_series", "time_series", client()).getStratum()
						.get("Stratum1").get(0));

		org.junit.Assert.assertEquals("production",
				Filtering.getFilteringResults(request, "time_series", "time_series", client()).getStratum()
						.get("Stratum2").get(0));

		//-------------------------------------------------------------------------

		//second request for animals
		request = new FilterRequest();
		request.setSearchText("animals");
		request.setLocation(true);
		Map<String, List<String>> filters1 = new HashMap<>();
		filters1.put("Stratum2", Arrays.asList("production"));
		request.setFilters(filters1);

		//check if it has considered only 1 row
		org.junit.Assert.assertEquals(new Long(1),
				Filtering.getFilteringResults(request, "time_series", "time_series", client()).getTotalRows());

		org.junit.Assert.assertEquals("animals",
				Filtering.getFilteringResults(request, "time_series", "time_series", client()).getStratum()
						.get("Sub-Sector").get(0));

		org.junit.Assert.assertEquals("production",
				Filtering.getFilteringResults(request, "time_series", "time_series", client()).getStratum()
						.get("Stratum2").get(0));

		//-------------------------------------------------------------------------

		//third request for animals and wrong filter
		request = new FilterRequest();
		request.setSearchText("animals|");
		request.setLocation(true);
		Map<String, List<String>> filters2 = new HashMap<>();
		filters2.put("Stratum2", Arrays.asList("production xyz"));
		request.setFilters(filters2);

		//since production xyz does not exist total rows matched will be zero
		org.junit.Assert.assertEquals(new Long(0),
				Filtering.getFilteringResults(request, "time_series", "time_series", client()).getTotalRows());

		//-------------------------------------------------------------------------

		//fourth request for corn and location ALABAMA 
		request = new FilterRequest();
		request.setSearchText("animals|");
		request.setLocation(true);

		Map<String, Set<String>> locFilter = new HashMap<>();
		Set<String> countryList = new TreeSet<>();
		//country and his parent
		countryList.add("UNITED STATES:UNITED STATES");

		Set<String> stateList = new TreeSet<>();
		//state and its parent
		stateList.add("UNITED STATES:ALABAMA");

		locFilter.put("Country", countryList);
		locFilter.put("State", stateList);
		request.setLocations(locFilter);

		Map<String, Set<LocationAggrigation>> locations = Filtering
				.getFilteringResults(request, "time_series", "time_series", client()).getLocations();
		for( String key : locations.keySet() )
		{
			if( key.equals("Country") )
			{
				Set<LocationAggrigation> stateValue = locations.get("Country");
				for( LocationAggrigation loc : stateValue )
				{
					org.junit.Assert.assertEquals("UNITED STATES", loc.getLocationParent());
					org.junit.Assert.assertEquals(true, loc.getLocations().contains("UNITED STATES"));
					break;
				}
			}
			else if( key.equals("State") )
			{
				Set<LocationAggrigation> stateValue = locations.get("State");
				for( LocationAggrigation loc : stateValue )
				{
					org.junit.Assert.assertEquals("UNITED STATES", loc.getLocationParent());
					org.junit.Assert.assertEquals(true, loc.getLocations().contains("ALABAMA"));
					break;
				}
			}
			else if( key.equals("County") )
			{
				Set<LocationAggrigation> stateValue = locations.get("County");
				for( LocationAggrigation loc : stateValue )
				{
					org.junit.Assert.assertEquals("ALABAMA", loc.getLocationParent());
					org.junit.Assert.assertEquals(true, loc.getLocations().contains("COUNTY1"));
					break;
				}
			}
		}

		//-------------------------------------------------------------------------

		//fifth request for corn and location ALABAMA and production filter
		request = new FilterRequest();
		request.setSearchText("animals|");
		request.setLocation(true);

		Map<String, List<String>> filters3 = new HashMap<>();
		filters3.put("Stratum2", Arrays.asList("production"));
		request.setFilters(filters3);

		request.setLocations(locFilter);

		locations = Filtering.getFilteringResults(request, "time_series", "time_series", client()).getLocations();
		for( String key : locations.keySet() )
		{
			if( key.equals("Country") )
			{
				Set<LocationAggrigation> stateValue = locations.get("Country");
				for( LocationAggrigation loc : stateValue )
				{
					org.junit.Assert.assertEquals("UNITED STATES", loc.getLocationParent());
					org.junit.Assert.assertEquals(true, loc.getLocations().contains("UNITED STATES"));
					break;
				}
			}
			else if( key.equals("State") )
			{
				Set<LocationAggrigation> stateValue = locations.get("State");
				for( LocationAggrigation loc : stateValue )
				{
					org.junit.Assert.assertEquals("UNITED STATES", loc.getLocationParent());
					org.junit.Assert.assertEquals(true, loc.getLocations().contains("ALABAMA"));
					break;
				}
			}
			else if( key.equals("County") )
			{
				Set<LocationAggrigation> stateValue = locations.get("County");
				for( LocationAggrigation loc : stateValue )
				{
					org.junit.Assert.assertEquals("ALABAMA", loc.getLocationParent());
					org.junit.Assert.assertEquals(true, loc.getLocations().contains("COUNTY1"));
					break;
				}
			}
		}

		//-------------------------------------------------------------------------

		//fifth request for corn and location Bangalore (WRONG LOCATION) and production filter
		request = new FilterRequest();
		request.setSearchText("animals|");
		request.setLocation(true);

		locFilter = new HashMap<>();
		countryList = new TreeSet<>();
		//country and his parent
		countryList.add("UNITED STATES:UNITED STATES");

		stateList = new TreeSet<>();
		//state and its parent
		stateList.add("UNITED STATES:BANGALORE");

		locFilter.put("Country", countryList);
		locFilter.put("State", stateList);
		request.setLocations(locFilter);

		org.junit.Assert.assertEquals(new Long(0),
				Filtering.getFilteringResults(request, "time_series", "time_series", client()).getTotalRows());
	}

	@Test
	public void resultsTest() throws Exception
	{

		//first request for corn
		FilterRequest request = new FilterRequest();
		request.setSearchText("corn");
		request.setLocation(true);
		Map<String, List<String>> filters = new HashMap<>();
		filters.put("Stratum2", Arrays.asList("production"));
		request.setFilters(filters);
		request.setStratumName("*");

		//5row and 3 series id in each will accounts to 15 series ID
		org.junit.Assert.assertEquals(15,
				Results.getResults(request, "time_series", "time_series", client()).getResults().size());

		//-------------------------------------------------------------------------

		//second request for corn with location limit
		request.setStratumName("*10");

		//5row and 3 series id in each will accounts to 15 series ID
		org.junit.Assert.assertEquals(10,
				Results.getResults(request, "time_series", "time_series", client()).getResults().size());

		//-------------------------------------------------------------------------

		//third request for animals
		request = new FilterRequest();
		request.setSearchText("animals");
		request.setLocation(true);
		Map<String, List<String>> filters1 = new HashMap<>();
		filters1.put("Stratum2", Arrays.asList("production"));
		request.setFilters(filters1);
		request.setStratumName("*");

		//1row and 3 series id in each will accounts to 3 series ID
		org.junit.Assert.assertEquals(3,
				Results.getResults(request, "time_series", "time_series", client()).getResults().size());
	}

	@After
	public void close()
	{
		client().admin().indices().prepareClose(TEST_INDEX_NAME).get();

	}

}
