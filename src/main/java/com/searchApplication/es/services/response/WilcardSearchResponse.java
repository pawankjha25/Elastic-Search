package com.searchApplication.es.services.response;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.bucket.nested.InternalNested;
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import com.google.gson.Gson;
import com.searchApplication.es.entities.WildCardSearchResponse;
import com.searchApplication.es.entities.WildCardSearchResponseList;
import com.searchApplication.es.services.impl.StringCompareUtil;

public class WilcardSearchResponse {

    public static WildCardSearchResponseList getResults( SearchResponse tFdocs, String queryText ) throws Exception
    {
        WildCardSearchResponseList response = new WildCardSearchResponseList();
        response.setSearchString(queryText);
        int length = queryText.split(" ").length;
        try
        {
            Set<WildCardSearchResponse> searchResponse = new TreeSet<WildCardSearchResponse>();

            InternalNested attributes = tFdocs.getAggregations().get("attributes");
            Terms attributes_filter = attributes.getAggregations().get("attributesValues");
            Collection<Terms.Bucket> buckets1 = attributes_filter.getBuckets();

            for( Terms.Bucket bucket1 : buckets1 )
            {
                Terms attributes_type = bucket1.getAggregations().get("attributeValuesRaw");
                Collection<Terms.Bucket> buckets2 = attributes_type.getBuckets();
                for( Terms.Bucket bucket2 : buckets2 )
                {
                    Terms attributes_value = bucket2.getAggregations().get("attributeName");
                    Collection<Terms.Bucket> buckets3 = attributes_value.getBuckets();
                    for( Terms.Bucket bucket3 : buckets3 )
                    {
                        ReverseNested reverse_nested = bucket3.getAggregations().get("reverseNested");
                        Terms sector_terms = reverse_nested.getAggregations().get("sector");
                        Collection<Terms.Bucket> buckets4 = sector_terms.getBuckets();
                        for( Terms.Bucket bucket4 : buckets4 )
                        {
                            Terms subSector_terms = bucket4.getAggregations().get("subsector");
                            Collection<Terms.Bucket> buckets5 = subSector_terms.getBuckets();
                            for( Terms.Bucket bucket5 : buckets5 )
                            {
                                Terms superregion = bucket5.getAggregations().get("superregion");
                                Collection<Terms.Bucket> buckets6 = superregion.getBuckets();
                                for( Terms.Bucket bucket6 : buckets6 )
                                {
                                    WildCardSearchResponse wsr = new WildCardSearchResponse();
                                    wsr.setSector(bucket4.getKeyAsString());
                                    wsr.setSubSector(bucket5.getKeyAsString());
                                    wsr.setSuperRegion(bucket6.getKeyAsString());
                                    wsr.setSuggestionString(bucket2.getKeyAsString());
                                    wsr.setStratumName(bucket3.getKeyAsString());
                                    //level1 bucket which has all the results matching the search word
                                    searchResponse.add(wsr);
                                }
                            }
                        }
                    }
                }

            }
            Set<WildCardSearchResponse> exactMatchResult = new TreeSet<WildCardSearchResponse>();
            Set<WildCardSearchResponse> containsResult = new TreeSet<WildCardSearchResponse>();
            Map<Double, WildCardSearchResponse> highPercentMatch = new HashMap<Double, WildCardSearchResponse>();
            Map<Double, WildCardSearchResponse> mediumPercentMatch = new HashMap<Double, WildCardSearchResponse>();
            Map<Double, WildCardSearchResponse> lowPercentMatch = new HashMap<Double, WildCardSearchResponse>();

            String filteredQueryText = "";
            for( String sM : queryText.split(" ") )
            {
                if( sM.length() > 2 )
                {
                    if( filteredQueryText == "" )
                    {
                        filteredQueryText = filteredQueryText + sM;
                    }
                    else
                    {
                        filteredQueryText = filteredQueryText + " " + sM;
                    }
                }
            }
            for( WildCardSearchResponse result : searchResponse )
            {
                //for one word search and exact match
                if( length == 1 )
                {
                    //exact match
                    if( result.getSuggestionString().toLowerCase().equals(queryText.toLowerCase()) )
                    {
                        exactMatchResult.add(result);
                    }
                    else if( result.getSuggestionString().toLowerCase().trim()
                            .contains(queryText.toLowerCase().trim()) )
                    {
                        containsResult.add(result);
                    }

                }
                //for multiple word search
                else
                {
                    //exact match
                    boolean match = true;
                    if( result.getSuggestionString().toLowerCase().trim()
                            .equals(filteredQueryText.toLowerCase().trim()) )
                    {
                        exactMatchResult.add(result);
                    }
                    for( String s : filteredQueryText.split(" ") )
                    {
                        if( !result.getSuggestionString().toLowerCase().trim().contains(s.toLowerCase().trim()) )
                        {
                            match = false;
                        }
                        if( !match )
                        {
                            break;
                        }

                    }
                    if( match )
                    {
                        containsResult.add(result);
                    }

                }
                if( exactMatchResult.isEmpty() )
                {
                    if( !containsResult.isEmpty() )
                    {
                        for( WildCardSearchResponse containsRes : containsResult )
                        {
                            double sMatch = StringCompareUtil.similarity(
                                    containsRes.getSuggestionString().toLowerCase(), queryText.toLowerCase());
                            if( sMatch > .50 )
                            {
                                if( highPercentMatch.get(sMatch) == null )
                                {
                                    highPercentMatch.put(sMatch, containsRes);
                                }
                                else
                                {
                                    highPercentMatch.put(sMatch + new Random().nextDouble() - 1, containsRes);
                                }
                            }
                            else if( sMatch > .30 )
                            {
                                mediumPercentMatch.put(sMatch, containsRes);
                                if( mediumPercentMatch.get(sMatch) == null )
                                {
                                    mediumPercentMatch.put(sMatch, containsRes);
                                }
                                else
                                {
                                    mediumPercentMatch.put(sMatch + new Random().nextDouble() - 1, containsRes);
                                }
                            }
                            else if( sMatch > .15 )
                            {
                                if( lowPercentMatch.get(sMatch) == null )
                                {
                                    lowPercentMatch.put(sMatch, containsRes);
                                }
                                else
                                {
                                    lowPercentMatch.put(sMatch + new Random().nextDouble() - 1, containsRes);
                                }
                            }
                            else
                            {

                            }
                        }
                    }
                }
            }

            Map<Double, WildCardSearchResponse> searchResponseSort = new HashMap<Double, WildCardSearchResponse>();

            //add the buckets in the results
            if( !exactMatchResult.isEmpty() )
            {
                System.out.println("Exact Match");
                response.setSearchResponse(exactMatchResult);
            }
            else if( !highPercentMatch.isEmpty() )
            {
                System.out.println("High Match");
                Set<WildCardSearchResponse> highMatchResult = sortMap(highPercentMatch);
                System.out.println(new Gson().toJson(highMatchResult));
                response.setSearchResponse(highMatchResult);
            }
            else if( !mediumPercentMatch.isEmpty() )
            {
                System.out.println("Medium Match");
                Set<WildCardSearchResponse> medMatchResult = sortMap(mediumPercentMatch);
                response.setSearchResponse(medMatchResult);
            }
            else if( !lowPercentMatch.isEmpty() )
            {
                System.out.println("Low Match");
                Set<WildCardSearchResponse> lowMatchResult = sortMap(lowPercentMatch);
                response.setSearchResponse(lowMatchResult);
            }
            else
            {
                System.out.println("Search Match");
                for( String s : filteredQueryText.split(" ") )
                {
                    for( WildCardSearchResponse sort : searchResponse )
                    {
                        if( sort.getSuggestionString().trim().toLowerCase().startsWith(s.trim().toLowerCase()) )
                        {
                            double sMatch = StringCompareUtil.similarity(sort.getSuggestionString().toLowerCase(),
                                    queryText.toLowerCase());
                            searchResponseSort.put(sMatch, sort);
                        }

                    }
                    break;

                }
                response.setSearchResponse(sortMap(searchResponseSort));
            }
        }
        catch( Exception e )
        {
            throw e;
        }
        return response;
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
