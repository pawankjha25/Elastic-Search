package com.searchApplication.schedulers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.searchApplication.es.entities.Attributes;
import com.searchApplication.es.entities.DatabaseInfo;
import com.searchApplication.es.entities.Locations;
import com.searchApplication.es.entities.TimeSeriesData;
import com.searchApplication.utils.ElasticSearchUtility;

@Component
@EnableScheduling
public class ElasticSearchIndexComponent {

	@Autowired
	private ElasticSearchUtility elasticSearchUtility;

	public ElasticSearchIndexComponent() {

	}

	@Scheduled(fixedRate = 3600000)
	public void startApp() throws Exception {
		try {
			indexCSVFileData();
			// indexOldCSVFileData();
		} catch (Exception e) {
			throw e;
		}
	}

	@SuppressWarnings("resource")
	public void indexOldCSVFileData() throws Exception {
		String csvFile = "/home/girish/zdalydata_sample.csv ";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ">>>";
		try {

			br = new BufferedReader(new FileReader(csvFile));
			int i = 0;
			List<TimeSeriesData> list = new ArrayList<TimeSeriesData>();
			while ((line = br.readLine()) != null) {
				if (i != 0) {
					line = line.replace("\",\"", ">>>");
					line = line.replace("\"", "");

					String[] description = line.split(cvsSplitBy);
					if (description.length == 5) {
						TimeSeriesData data = convertOldData(description[0].trim(), description[1].trim(),
								description[2].trim(), description[3].trim(), description[4].trim());
						if (data.getLocations().size() > 20000) {
							elasticSearchUtility.addDoc(elasticSearchUtility.getESClient(), "zdaly", "time_series",
									data);
						} else {
							list.add(data);
						}
					}
					if (i % 300 == 0) {
						elasticSearchUtility.addDocsInBulk(elasticSearchUtility.getESClient(), "zdaly", "time_series",
								list);
						list.removeAll(list);
					}
				}
				i++;
			}
		} catch (Exception e) {
			throw e;
		}
	}

	public static TimeSeriesData convertOldData(String propertyidDB, String attributes, String attributesMetaData,
			String locations, String locationsMetaData) throws Exception {
		TimeSeriesData data = new TimeSeriesData();
		try {
			List<Attributes> attributeList = new ArrayList<Attributes>();
			DatabaseInfo db = new DatabaseInfo();
			Set<Locations> locationList = new TreeSet<Locations>();
			List<String> description = new ArrayList<String>();

			// setting up the db information
			String[] dbInfo = propertyidDB.split(",");
			db.setDb_name(dbInfo[1].trim());
			db.setProperties(new Long(dbInfo[0].trim()));
			data.setDb(db);

			// setting up the attributes information
			String[] attributesValuesList = attributes.replace("|", ">>>").split(">>>");
			int attLength = attributesValuesList.length;
			String[] attributesMetadataList = attributesMetaData.trim().replace("|", ">>>").split(">>>");
			String[] attributesNameList = new String[attributesMetadataList.length];
			String[] attributesTypeList = new String[attributesMetadataList.length];

			for (int i = 0; i < attLength; i++) {
				attributesNameList[i] = attributesMetadataList[i].substring(attributesMetadataList[i].indexOf("(") + 1,
						attributesMetadataList[i].indexOf(")"));

				attributesTypeList[i] = attributesMetadataList[i].substring(0, attributesMetadataList[i].indexOf("("));
			}

			for (int i = 0; i < attLength; i++) {
				Attributes att = new Attributes();
				att.setAttribute_name(attributesTypeList[i].trim());
				att.setAttribute_value(attributesValuesList[i].trim());
				if (attributesNameList[i].split(",")[1] != null
						&& !attributesNameList[i].split(",")[1].equalsIgnoreCase("NULL")) {
					att.setAttribute_level(new Long(attributesNameList[i].split(",")[1]));
				} else {
					att.setAttribute_level((long) 0);
				}
				att.setAttribute_parent(attributesNameList[i].split(",")[0].trim());

				attributeList.add(att);

				// add sector subsector and super region
				if (att.getAttribute_name().equalsIgnoreCase("sector")) {
					data.setSector(att.getAttribute_value().trim());
				} else if (att.getAttribute_name().equalsIgnoreCase("SubSector")) {
					data.setSub_sector(att.getAttribute_value().trim());
				}
				// add description
				description.add(att.getAttribute_value().trim());
			}
			data.setAttributes(attributeList);

			// setting up the locations information
			String[] locationValuesList = locations.replace("|", ">>>").split(">>>");
			String[] locationMetaValuesList = locationsMetaData.replace("|", ">>>").split(">>>");

			// iterate through all the location details
			for (int i = 0; i < locationValuesList.length; i++) {
				String[] loc = locationValuesList[i].split(",");
				String[] locMeta = locationMetaValuesList[i].split(",");

				if (loc.length == locMeta.length) {
					for (int j = 0; j < loc.length; j++) {
						if (locMeta[j].trim().equalsIgnoreCase("SuperRegion")) {
							data.setSuper_region(loc[j].trim());
						} else {
							Locations location = new Locations();
							location.setLocation_meta("");
							location.setLocation_parent(loc[j - 1].trim());
							if (loc[j].contains("(")) {
								location.setLocation_name(loc[j].substring(0, loc[j].indexOf("(")).trim());
							} else {
								location.setLocation_name(loc[j].trim());
							}
							location.setLocation_type(locMeta[j].trim());
							if (loc[loc.length - 1].contains("(")) {
								String lastLoc = loc[loc.length - 1].substring(0, loc[loc.length - 1].indexOf("("))
										.trim();
								String id = loc[loc.length - 1].replaceFirst(lastLoc, "").replace("(", "")
										.replace(")", "").trim();
								if (!id.trim().isEmpty()) {
									location.setSeries_id(new Long(id));
									locationList.add(location);
								}
							}
						}

					}
				}
			}
			data.setLocations(locationList);
			data.setDescription(description);
		} catch (Exception e) {
			throw e;
		}
		return data;
	}

	@SuppressWarnings("resource")
	public void indexCSVFileData() throws Exception {
		String csvFile = "/ebs/apps/QuickStats_Attributes_Data.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ">>>";
		try {

			br = new BufferedReader(new FileReader(csvFile));
			int i = 0;
			List<TimeSeriesData> list = new ArrayList<TimeSeriesData>();
			while ((line = br.readLine()) != null) {
				if (i > 83) {
					line = line.replace("\",\"", ">>>");
					line = line.replace("\"", "");
					line = line.replace("]", "").replace("[", "");

					String[] description = line.split(cvsSplitBy);
					if (description.length == 8) {
						TimeSeriesData data = convertData(description[0].trim(), description[1].trim(),
								description[2].trim(), description[3].trim(), description[4].trim(),
								description[7].trim());
						if (data != null && data.getLocations() != null && data.getLocations().size() > 1000) {

							elasticSearchUtility.addDoc(elasticSearchUtility.getESClient(), "zdaly", "time_series",
									data);

						} else {

							list.add(data);
						}
					}
					if (i % 500 == 0) {
						elasticSearchUtility.addDocsInBulk(elasticSearchUtility.getESClient(), "zdaly", "time_series",
								list);
						list.removeAll(list);
					}
				}
				i++;
			}
		} catch (Exception e) {
			throw e;
		}
	}

	public static TimeSeriesData convertData(String propertyidDB, String attributes, String attributesMetaData,
			String locations, String locationsMetaData, String propertyId) throws Exception {
		TimeSeriesData data = new TimeSeriesData();
		try {
			List<Attributes> attributeList = new ArrayList<Attributes>();
			DatabaseInfo db = new DatabaseInfo();
			Set<Locations> locationList = new TreeSet<Locations>();
			List<String> description = new ArrayList<String>();

			// setting up the db information
			db.setDb_name(propertyidDB);
			db.setProperties(new Long(propertyId));
			data.setDb(db);

			// setting up the attributes information
			String[] attributesValuesList = attributes.replace("|", ">>>").split(">>>");
			int attLength = attributesValuesList.length;
			String[] attributesMetadataList = attributesMetaData.trim().replace("|", ">>>").split(">>>");
			String[] attributesNameList = new String[attributesMetadataList.length];
			String[] attributesTypeList = new String[attributesMetadataList.length];

			for (int i = 0; i < attLength; i++) {
				attributesNameList[i] = attributesMetadataList[i].substring(attributesMetadataList[i].indexOf("(") + 1,
						attributesMetadataList[i].indexOf(")"));

				attributesTypeList[i] = attributesMetadataList[i].substring(0, attributesMetadataList[i].indexOf("("));
			}

			for (int i = 0; i < attLength; i++) {
				Attributes att = new Attributes();
				att.setAttribute_name(attributesTypeList[i].trim());
				att.setAttribute_value(attributesValuesList[i].trim());
				if (attributesNameList[i].split(",")[1] != null
						&& !attributesNameList[i].split(",")[1].equalsIgnoreCase("NULL")) {
					att.setAttribute_level(new Long(attributesNameList[i].split(",")[1]));
				} else {
					att.setAttribute_level((long) 0);
				}
				att.setAttribute_parent(attributesNameList[i].split(",")[0].trim());

				attributeList.add(att);

				// add sector subsector and super region
				if (att.getAttribute_name().equalsIgnoreCase("sector")) {
					data.setSector(att.getAttribute_value().trim());
				} else if (att.getAttribute_name().equalsIgnoreCase("Sub-Sector")) {
					data.setSub_sector(att.getAttribute_value().trim());
				}
				// add description
				description.add(att.getAttribute_value().trim());
			}
			data.setAttributes(attributeList);

			// setting up the locations information
			String[] locationValuesList = locations.replace("|", ">>>").split(">>>");
			String[] locationMetaValuesList = locationsMetaData.replace("|", ">>>").split(">>>");

			// iterate through all the location details
			String superRegion = "";
			for (int i = 0; i < locationValuesList.length; i++) {
				String[] loc = locationValuesList[i].split(",");
				String[] locMeta = locationMetaValuesList[i].split(",");

				Locations location = new Locations();
				if (loc.length == locMeta.length) {
					for (int j = 0; j < loc.length; j++) {
						if (locMeta[j].trim().equalsIgnoreCase("Super Region")) {
							if (loc[j].trim() != null)
								superRegion = loc[j].trim();
						} else {
							location.setLocation_meta("");
							location.setLocation_parent(loc[j - 1].trim());
							if (loc[j].contains("(")) {
								location.setLocation_name(loc[j].substring(0, loc[j].indexOf("(")).trim());
							} else {
								location.setLocation_name(loc[j].trim());
							}
							location.setLocation_type(locMeta[j].trim());
							if (loc[loc.length - 1].contains("(")) {
								String lastLoc = loc[loc.length - 1].substring(0, loc[loc.length - 1].indexOf("("))
										.trim();
								String id = loc[loc.length - 1].replaceFirst(lastLoc, "").replace("(", "")
										.replace(")", "").trim();
								if (!id.trim().isEmpty()) {
									location.setSeries_id(new Long(id));
									locationList.add(location);
								}
							}
						}

					}
				}
			}
			data.setSuper_region(superRegion);
			data.setLocations(locationList);
			data.setDescription(description);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
}
