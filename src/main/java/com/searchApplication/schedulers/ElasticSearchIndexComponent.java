//package com.searchApplication.schedulers;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.PrintWriter;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Set;
//import java.util.TreeSet;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import com.searchApplication.es.entities.Attributes;
//import com.searchApplication.es.entities.DatabaseInfo;
//import com.searchApplication.es.entities.Locations;
//import com.searchApplication.es.entities.TimeSeriesData;
//import com.searchApplication.utils.ElasticSearchUtility;
//
//@Component
//@EnableScheduling
//public class ElasticSearchIndexComponent {
//
//	@Autowired
//	private ElasticSearchUtility elasticSearchUtility;
//
//	public ElasticSearchIndexComponent()
//	{
//
//	}
//
//	@Scheduled( fixedRate = 360000000 )
//	public void startApp() throws Exception
//	{
//		try
//		{
//			indexCSVFileData();
//		}
//		catch( Exception e )
//		{
//			throw e;
//		}
//	}
//
//	@SuppressWarnings( "resource" )
//	public void indexCSVFileData() throws Exception
//	{
//		String csvFile = "C:\\Users\\Inksedge\\Downloads\\EuroStat_Property_Series_02Nov2016.csv";
//		BufferedReader br = null;
//		String line = "";
//		String cvsSplitBy = ">>>";
//		PrintWriter per = new PrintWriter("C:\\Users\\Inksedge\\Downloads\\per.txt");
//		long start = System.currentTimeMillis();
//		try
//		{
//
//			br = new BufferedReader(new FileReader(csvFile));
//			int i = 0;
//			List<TimeSeriesData> list = new ArrayList<TimeSeriesData>();
//			while( (line = br.readLine()) != null )
//			{
//				if( i > 2 )
//				{
//					line = line.replace("\",\"", ">>>");
//					line = line.replace("\"", "");
//					//line = line.replace("]", "").replace("[", "");
//
//					String[] description = line.split(cvsSplitBy);
//					if( description.length == 7 )
//					{
//						TimeSeriesData data = convertData(description[0].trim(), description[1].trim(),
//								description[3].replace("]", "").replace("[", "").trim(),
//								description[4].replace("]", "").replace("[", "").trim(), description[5].trim(),
//								description[6].trim());
//						if( data != null && data.getLocations() != null && data.getLocations().size() > 1000 )
//						{
//
//							elasticSearchUtility.addDoc(elasticSearchUtility.getESClient(), "zdaly", "time_series",
//									data);
//
//						}
//						else if( data != null )
//						{
//							list.add(data);
//						}
//					}
//					if( list.size() % 500 == 0 )
//					{
//						elasticSearchUtility.addDocsInBulk(elasticSearchUtility.getESClient(), "zdaly", "time_series",
//								list);
//						list.removeAll(list);
//					}
//					if( i % 100000 == 0 )
//					{
//						long end = System.currentTimeMillis();
//						per.write("For indexing 100k rows it took " + ((end - start) / 1000) + " seconds \n");
//					}
//				}
//				i++;
//			}
//			per.close();
//		}
//		catch( Exception e )
//		{
//			per.write("Ending due to exception");
//			per.close();
//			throw e;
//		}
//	}
//
//	public static TimeSeriesData convertData( String propertyId, String propertyidDB, String attributes,
//			String attributesMetaData, String locations, String locationsMetaData ) throws Exception
//	{
//		TimeSeriesData data = new TimeSeriesData();
//		try
//		{
//			//initialise all the variable inside the result object
//			List<Attributes> attributeList = new ArrayList<Attributes>();
//			DatabaseInfo db = new DatabaseInfo();
//			Set<Locations> locationList = new TreeSet<Locations>();
//			List<String> description = new ArrayList<String>();
//
//			// setting up the db information
//			db.setDb_name(propertyidDB);
//			db.setProperties(new Long(propertyId));
//			data.setDb(db);
//
//			// setting up the attributes information
//			String[] attributesValuesList = attributes.replace("|", ">>>").split(">>>");
//			int attLength = attributesValuesList.length;
//			String[] attributesMetadataList = attributesMetaData.trim().replace("|", ">>>").split(">>>");
//			String[] attributesNameList = new String[attributesMetadataList.length];
//			String[] attributesTypeList = new String[attributesMetadataList.length];
//
//			//forming the attribute name and attribute value list from the given string
//			for( int i = 0; i < attLength; i++ )
//			{
//				attributesNameList[i] = attributesMetadataList[i].substring(
//						attributesMetadataList[i].lastIndexOf("(") + 1, attributesMetadataList[i].lastIndexOf(")"));
//
//				attributesTypeList[i] = attributesMetadataList[i].substring(0,
//						attributesMetadataList[i].lastIndexOf("("));
//			}
//
//			//from each string in the list create a attribute object
//			for( int i = 0; i < attLength; i++ )
//			{
//				Attributes att = new Attributes();
//				att.setAttribute_name(attributesTypeList[i].trim());
//				att.setAttribute_value(attributesValuesList[i].trim());
//				String attLevPar = attributesMetadataList[i].substring(attributesMetadataList[i].lastIndexOf("(") + 1,
//						attributesMetadataList[i].length() - 1);
//				//the attribute level will be the second thing inside the bracket
//				if( attLevPar.split(",")[1] != null )
//				{
//					att.setAttribute_level(
//							new Long(attLevPar.substring(attLevPar.lastIndexOf(",") + 1, attLevPar.length())));
//				}
//				else
//				{
//					att.setAttribute_level((long) 0);
//				}
//				//the attribute parent be the first thing inside the bracket
//				att.setAttribute_parent(attLevPar.split(",")[0].trim());
//
//				attributeList.add(att);
//
//				// add sector subsector and super region 
//				if( att.getAttribute_name().equalsIgnoreCase("sector") )
//				{
//					data.setSector(att.getAttribute_value().trim());
//				}
//				else if( att.getAttribute_name().equalsIgnoreCase("SubSector") )
//				{
//					data.setSub_sector(att.getAttribute_value().trim());
//					att.setAttribute_name("Sub-Sector");
//				}
//				else if( att.getAttribute_name().equalsIgnoreCase("Super_Region") )
//				{
//					data.setSuper_region(att.getAttribute_value().trim());
//				}
//
//				// add description
//				description.add(att.getAttribute_value().trim());
//			}
//			data.setAttributes(attributeList);
//
//			// setting up the locations information
//			String[] locationValuesList = locations.replace("|", ">>>").replace("][", "],[").split(">>>");
//			String[] locationMetaValuesList = locationsMetaData.replace("|", ">>>").replace("]][[", "]],[[")
//					.split(">>>");
//
//			// iterate through all the location details
//			for( int i = 0; i < locationValuesList.length; i++ )
//			{
//				String[] loc = locationValuesList[i].split(",");
//				String[] locMeta = locationMetaValuesList[i].split(",");
//
//				if( locationsMetaData.startsWith("[[") )
//				{
//					//if there is mismatch in location and location meta length throw an error
//					if( locMeta.length != 1 && loc.length != 1 && locMeta.length != loc.length )
//					{
//						return null;
//					}
//					for( int j = 0; j < locMeta.length; j++ )
//					{
//						Locations location = new Locations();
//						location.setLocation_meta("");
//
//						//if this is the starting location then it wont have parent
//						if( j - 1 < 0 )
//						{
//							location.setLocation_parent("NULL");
//						}
//						//anything after first location will have a parent
//						else
//						{
//							if( (loc[j - 1].startsWith("[") && loc[j - 1].endsWith("]"))
//									|| (loc[j].startsWith("[") && (loc[j].endsWith(")") || loc[j].endsWith("]"))) )
//							{
//								location.setLocation_parent("NULL");
//							}
//							else
//							{
//								location.setLocation_parent(loc[j - 1].replace("[", "").replace("]", "").trim());
//							}
//						}
//						//if the location ends with ) which has series id inside ')'
//						if( loc[j].endsWith(")") )
//						{
//							if( locMeta.length == 1 )
//							{
//								location.setLocation_name(
//										locationValuesList[i].substring(0, locationValuesList[i].lastIndexOf("("))
//												.replace("[", "").replace("]", ""));
//								location.setLocation_type(locMeta[j].replace("[", "").replace("]", "").trim());
//							}
//							else
//							{
//								if( j == locMeta.length - 1 )
//								{
//									location.setLocation_name(loc[j].substring(0, loc[j].lastIndexOf("("))
//											.replace("[", "").replace("]", "").trim());
//								}
//								else
//								{
//									location.setLocation_name(loc[j].replace("[", "").replace("]", "").trim());
//								}
//								location.setLocation_type(locMeta[j].replace("[", "").replace("]", "").trim());
//							}
//						}
//						else
//						{
//							if( locMeta.length == 1 )
//							{
//								location.setLocation_name(locationValuesList[i].replace("[", "").replace("]", ""));
//								location.setLocation_type(locMeta[j].replace("[", "").replace("]", "").trim());
//							}
//							else
//							{
//								if( j == locMeta.length - 1 )
//								{
//									location.setLocation_name(loc[j].replace("[", "").replace("]", "").trim());
//								}
//								else
//								{
//									location.setLocation_name(loc[j].replace("[", "").replace("]", "").trim());
//								}
//								location.setLocation_type(locMeta[j].replace("[", "").replace("]", "").trim());
//							}
//						}
//						if( loc[loc.length - 1].endsWith(")") )
//						{
//							String id = "";
//							if( loc[j].endsWith(")") )
//							{
//								id = loc[loc.length - 1]
//										.replace(loc[j].substring(0, loc[j].lastIndexOf("(")).trim(), "").trim();
//							}
//							else
//							{
//								id = loc[loc.length - 1].replace(loc[j].trim(), "").trim();
//							}
//							//id = id.replace("(", "").replace(")", "");
//							id = id.substring(id.lastIndexOf("(") + 1, id.lastIndexOf(")"));
//							if( !id.trim().isEmpty() )
//							{
//								location.setSeries_id(new Long(id));
//								locationList.add(location);
//							}
//						}
//
//					}
//				}
//			}
//
//			data.setLocations(locationList);
//			data.setDescription(description);
//		}
//		catch( Exception e )
//		{
//			e.printStackTrace();
//		}
//		return data;
//	}
//}
