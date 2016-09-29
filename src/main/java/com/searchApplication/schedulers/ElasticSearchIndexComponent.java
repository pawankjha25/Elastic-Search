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

    public ElasticSearchIndexComponent()
    {

    }

    @Scheduled( fixedRate = 3600000 )
    public void startApp() throws Exception
    {
        try
        {
            indexCSVFileData();
        }
        catch( Exception e )
        {
            throw e;
        }
    }

    @SuppressWarnings( "resource" )
    public void indexCSVFileData() throws Exception
    {
        String csvFile = "/home/girish/zdalydata_sample.csv ";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ">>>";
        try
        {

            br = new BufferedReader(new FileReader(csvFile));
            int i = 0;
            List<TimeSeriesData> list = new ArrayList<TimeSeriesData>();
            while( (line = br.readLine()) != null )
            {
                if( i != 0 )
                {
                    line = line.replace("\",\"", ">>>");
                    line = line.replace("\"", "");

                    String[] description = line.split(cvsSplitBy);
                    if( description.length == 5 )
                    {
                        TimeSeriesData data = convertData(description[0].trim(), description[1].trim(),
                                description[2].trim(), description[3].trim(), description[4].trim());
                        list.add(data);
                    }
                    if( i % 1000 == 0 )
                    {
                        elasticSearchUtility.addDocsInBulk(elasticSearchUtility.getESClient(), "zdaly", "time_series",
                                list);
                        System.out.println(i);
                        list.removeAll(list);
                    }
                }
                i++;
            }
        }
        catch( Exception e )
        {
            throw e;
        }
    }

    public static TimeSeriesData convertData( String propertyidDB, String attributes, String attributesMetaData,
            String locations, String locationsMetaData ) throws Exception
    {
        TimeSeriesData data = new TimeSeriesData();
        try
        {
            List<Attributes> attributeList = new ArrayList<Attributes>();
            DatabaseInfo db = new DatabaseInfo();
            Set<Locations> locationList = new TreeSet<Locations>();
            List<String> description = new ArrayList<String>();

            //setting up the db information
            String[] dbInfo = propertyidDB.split(",");
            db.setDb_name(dbInfo[1]);
            db.setProperties(new Long(dbInfo[0]));
            data.setDb(db);

            //setting up the attributes information
            String[] attributesValuesList = attributes.replace("|", ">>>").split(">>>");
            int attLength = attributesValuesList.length;
            String[] attributesMetadataList = attributesMetaData.trim().replace("|", ">>>").split(">>>");
            String[] attributesNameList = new String[attributesMetadataList.length];
            String[] attributesTypeList = new String[attributesMetadataList.length];

            for( int i = 0; i < attLength; i++ )
            {
                attributesNameList[i] = attributesMetadataList[i].substring(attributesMetadataList[i].indexOf("(") + 1,
                        attributesMetadataList[i].indexOf(")"));

                attributesTypeList[i] = attributesMetadataList[i].substring(0, attributesMetadataList[i].indexOf("("));
            }

            for( int i = 0; i < attLength; i++ )
            {
                Attributes att = new Attributes();
                att.setAttribute_name(attributesTypeList[i].trim());
                att.setAttribute_value(attributesValuesList[i].trim());
                if( attributesNameList[i].split(",")[1] != null
                        && !attributesNameList[i].split(",")[1].equalsIgnoreCase("NULL") )
                {
                    att.setAttribute_level(new Long(attributesNameList[i].split(",")[1]));
                }
                else
                {
                    att.setAttribute_level((long) 0);
                }
                att.setAttribute_parent(attributesNameList[i].split(",")[0]);

                attributeList.add(att);

                //add sector subsector and super region
                if( att.getAttribute_name().equalsIgnoreCase("sector") )
                {
                    data.setSector(att.getAttribute_value());
                }
                else if( att.getAttribute_name().equalsIgnoreCase("SubSector") )
                {
                    data.setSub_sector(att.getAttribute_value());
                }
                //add description
                description.add(att.getAttribute_value());
            }
            data.setAttributes(attributeList);

            //setting up the locations information
            String[] locationValuesList = locations.replace("|", ">>>").split(">>>");
            String[] locationMetaValuesList = locationsMetaData.replace("|", ">>>").split(">>>");

            for( int i = 0; i < locationValuesList.length; i++ )
            {
                String[] loc = locationValuesList[i].split(",");
                String[] locMeta = locationMetaValuesList[i].split(",");

                if( loc.length >= 4 )
                {
                    for( int j = 0; j < loc.length; j++ )
                    {
                        if( locMeta[j].trim().equalsIgnoreCase("SuperRegion") )
                        {
                            data.setSuper_region(loc[j].trim());
                        }
                        else
                        {
                            Locations location = new Locations();
                            if( loc[j].contains("(") )
                            {
                                location.setLocation_name(loc[j].substring(0, loc[j].indexOf("(")).trim());
                            }
                            else
                            {
                                location.setLocation_name(loc[j]);
                            }
                            location.setLocation_type(locMeta[j]);
                            if( loc[3].contains("(") )
                            {
                                String state = loc[3].substring(0, loc[3].indexOf("(")).trim();
                                String id = loc[3].replaceFirst(state, "").replace("(", "").replace(")", "").trim();
                                if( !id.trim().isEmpty() )
                                {
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
        }
        catch( Exception e )
        {
            throw e;
        }
        return data;
    }
}
