package com.searchApplication.es.indexing;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.searchApplication.es.entities.Attributes;
import com.searchApplication.es.entities.DatabaseInfo;
import com.searchApplication.es.entities.Locations;
import com.searchApplication.es.entities.TimeSeriesData;

public class CSVParser {

	private static final String SPLITER = ">>>";

	public static TimeSeriesData parseLine(String line) throws Exception {

		line = line.replaceAll("\",\"", SPLITER);
		line = line.replaceAll("\"", "");
		line = line.replaceAll("\\]", "").replaceAll("\\[", "");

		String[] description = line.split(SPLITER);
		if (description.length == 8) {
			TimeSeriesData data = convertData(description[0].trim(), description[1].trim(), description[2].trim(),
					description[3].trim(), description[4].trim(), description[7].trim());
			return data;
		} else {
			return null;
		}
	}

	public static TimeSeriesData getAttriuteData(String attributes, String attributesMetaData, TimeSeriesData data) {
		List<Attributes> attributeList = new ArrayList<Attributes>();
		List<String> description = new ArrayList<String>();

		String[] attributesValuesList = attributes.replace("|", SPLITER).split(SPLITER);
		int attLength = attributesValuesList.length;
		String[] attributesMetadataList = attributesMetaData.trim().replace("|", SPLITER).split(SPLITER);
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
		data.setDescription(description);
		data.setAttributes(attributeList);
		return data;

	}

	public static TimeSeriesData processLocations(String locations, String locationsMetaData, TimeSeriesData data) {

		Set<Locations> locationList = new TreeSet<Locations>();
		// setting up the locations information
		String[] locationValuesList = locations.replace("|", SPLITER).split(SPLITER);
		String[] locationMetaValuesList = locationsMetaData.replace("|", SPLITER).split(SPLITER);
		System.out.println(locationValuesList.length);

		// iterate through all the location details
		for (int i = 0; i < locationValuesList.length; i++) {
			String[] loc = locationValuesList[i].split(",");
			String[] locMeta = locationMetaValuesList[i].split(",");

			Locations location = new Locations();
			if (loc.length == locMeta.length) {
				for (int j = 0; j < loc.length; j++) {
					if (locMeta[j].trim().equalsIgnoreCase("Super Region")) {
						data.setSuper_region(loc[j].trim());
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
							String lastLoc = loc[loc.length - 1].substring(0, loc[loc.length - 1].indexOf("(")).trim();
							String id = loc[loc.length - 1].replaceFirst(lastLoc, "").replace("(", "").replace(")", "")
									.trim();
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
		return data;
	}

	public static TimeSeriesData convertData(String propertyidDB, String attributes, String attributesMetaData,
			String locations, String locationsMetaData, String propertyId) throws Exception {
		System.out.println("------------------");
		TimeSeriesData data = new TimeSeriesData();
		try {
			DatabaseInfo db = new DatabaseInfo();

			// setting up the db information
			db.setDb_name(propertyidDB);
			db.setProperties(new Long(propertyId));
			data.setDb(db);
			data = getAttriuteData(attributes, attributesMetaData, data);
			// setting up the attributes information

		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
}