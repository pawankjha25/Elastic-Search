package com.searchApplication.es.services.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import com.google.gson.Gson;
import com.searchApplication.entities.SearchObjectFields;
import com.searchApplication.es.entities.WildCardSearchResponse;
import com.searchApplication.es.services.impl.StringCompareUtil;

public class BucketingSearchResponse {

    public static Set<WildCardSearchResponse> getResults( SearchResponse tFdocs, String queryText ) throws Exception
    {
        Set<WildCardSearchResponse> sortedRows = new LinkedHashSet<WildCardSearchResponse>();
        try
        {
            int queryLength = queryText.split(" ").length;
            SearchHit[] hits = tFdocs.getHits().getHits();
            if( tFdocs != null )
            {
                Map<Double, WildCardSearchResponse> rank1Rows = new HashMap<Double, WildCardSearchResponse>();
                Map<Double, WildCardSearchResponse> rank2Rows = new HashMap<Double, WildCardSearchResponse>();
                Map<Double, WildCardSearchResponse> rank3Rows = new HashMap<Double, WildCardSearchResponse>();

                hits = tFdocs.getHits().getHits();
                for( SearchHit d : hits )
                {
                    Map<String, Object> res = null;
                    res = d.getSource();

                    WildCardSearchResponse row = new WildCardSearchResponse();
                    List<String> description = new ArrayList<String>();

                    //for each row get the following fields
                    for( String key : res.keySet() )
                    {
                        if( res.get(key) != null && res.get(key).toString() != null )
                        {
                            if( SearchObjectFields.DESCRIPTION.equals(key) )
                            {
                                description.addAll(new Gson().fromJson(new Gson().toJson(res.get(key)), List.class));
                            }
                            else if( SearchObjectFields.SECTOR.equals(key) )
                            {
                                row.setSector(res.get(key).toString());
                            }
                            else if( SearchObjectFields.SUB_SECTOR.equals(key) )
                            {
                                row.setSubSector(res.get(key).toString());
                            }
                            else if( SearchObjectFields.SUPER_REGION.equals(key) )
                            {
                                row.setSuperRegion(res.get(key).toString());
                            }
                        }
                    }
                    //for each row get the perfect matching words
                    Set<String> perfectMatch = new TreeSet<String>();

                    //for each row get the partial match words
                    Set<String> partialMatch = new TreeSet<String>();

                    //have a bucket for the diff types of matches
                    Set<String> rank1Bucket = new TreeSet<String>();
                    Set<String> rank2Bucket = new TreeSet<String>();
                    Set<String> rank3Bucket = new TreeSet<String>();

                    for( String words : description )
                    {
                        for( String word : words.toLowerCase().split(" ") )
                        {
                            word = word.replaceAll("[-+.^:,]", "");
                            for( String query : queryText.split(" ") )
                            {
                                //perfect match
                                if( query.equals(word) )
                                {
                                    perfectMatch.add(word);
                                    rank1Bucket.add(words);
                                    rank2Bucket.add(words);
                                }
                                //partial match
                                else if( word.contains(query) )
                                {
                                    partialMatch.add(word);
                                    rank3Bucket.add(words);
                                }
                            }

                        }

                    }

                    //ranking the row

                    //check if it is a rank1 row or rank 2 row

                    if( perfectMatch.size() > 0 )
                    {
                        //rank one row should have all the query words in perfect match set
                        if( queryLength == perfectMatch.size() )
                        {
                            String bucket = "";

                            for( String ss : rank1Bucket )
                            {

                                if( bucket == "" )
                                {
                                    bucket = ss;
                                }
                                else
                                {
                                    bucket = bucket + " | " + ss;
                                }
                            }
                            if( bucket != null && !bucket.isEmpty() )
                            {
                                double sMatch = StringCompareUtil.similarity(bucket.toLowerCase(),
                                        queryText.toLowerCase());
                                row.setSuggestionString(bucket);
                                rank1Rows.put(sMatch, row);
                            }
                        }
                        //for rank two atleast any one perfect match has to be der
                        else
                        {
                            String bucket = "";
                            for( String s : rank3Bucket )
                            {
                                if( bucket == "" )
                                {
                                    bucket = s;
                                }
                                else
                                {
                                    bucket = bucket + " | " + s;
                                }
                            }
                            if( bucket != null && !bucket.isEmpty() )
                            {
                                row.setSuggestionString(bucket);
                                double sMatch = StringCompareUtil.similarity(bucket.toLowerCase(),
                                        queryText.toLowerCase());
                                rank2Rows.put(sMatch, row);
                            }
                        }
                    }
                    //check for rank3 row
                    else if( partialMatch.size() > 0 )
                    {
                        //it will be a rank 3 row
                        String bucket = "";
                        for( String s : rank3Bucket )
                        {
                            if( bucket == "" )
                            {
                                bucket = s;
                            }
                            else
                            {
                                bucket = bucket + " | " + s;
                            }
                        }
                        if( bucket != null && !bucket.isEmpty() )
                        {
                            double sMatch = StringCompareUtil.similarity(bucket.toLowerCase(), queryText.toLowerCase());
                            row.setSuggestionString(bucket);
                            rank3Rows.put(sMatch, row);
                        }
                    }
                }
                sortedRows.addAll(sortMap(rank1Rows));
                sortedRows.addAll(sortMap(rank2Rows));
                sortedRows.addAll(sortMap(rank3Rows));
            }
        }
        catch( Exception e )
        {
            throw e;
        }
        return sortedRows;
    }

    private static Set<WildCardSearchResponse> sortMap( Map<Double, WildCardSearchResponse> highPercentMatch )
            throws Exception
    {
        Set<WildCardSearchResponse> res = new LinkedHashSet<WildCardSearchResponse>();
        try
        {
            TreeSet<Double> sortedMatch = new TreeSet<Double>();
            for( Double map : highPercentMatch.keySet() )
            {
                sortedMatch.add(map);
            }
            sortedMatch = (TreeSet<Double>) sortedMatch.descendingSet();
            for( Double sorted : sortedMatch )
            {
                res.add(highPercentMatch.get(sorted));
            }

        }
        catch( Exception e )
        {
            throw e;
        }
        return res;
    }

}
