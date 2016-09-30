package com.searchApplication.es.services.response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.bucket.nested.InternalNested;
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import com.searchApplication.entities.LocationAggrigation;
import com.searchApplication.entities.SearchOutput;
import com.searchApplication.entities.Stratum;

public class QueryFilterResponse {

    public static SearchOutput getResponse( SearchResponse tFdocs ) throws Exception
    {
        SearchOutput response = new SearchOutput();
        Map<String, List<String>> stratum = new HashMap<String, List<String>>();
        Map<String, LocationAggrigation> locationList = new HashMap<String, LocationAggrigation>();
        Set<Stratum> stratumList = new TreeSet<Stratum>();
        try
        {
            InternalNested nestedAttributes = tFdocs.getAggregations().get("attributes");
            Terms attTypesTerms = nestedAttributes.getAggregations().get("attTypes");
            for( Terms.Bucket bucket : attTypesTerms.getBuckets() )
            {
                Stratum st = new Stratum();
                st.setStratumName(bucket.getKeyAsText().string());
                Terms levelBuckets = bucket.getAggregations().get("attLevel");
                for( Terms.Bucket levelBucket : levelBuckets.getBuckets() )
                {
                    st.setLevel(levelBucket.getKeyAsText().string());
                    if( levelBucket != null && levelBucket.getAggregations() != null
                            && levelBucket.getAggregations().get("attParent") != null )
                    {
                        Terms attParentTerm = levelBucket.getAggregations().get("attParent");
                        Collection<Bucket> attParentBuckets = attParentTerm.getBuckets();
                        for( Terms.Bucket attParentBucket : attParentBuckets )
                        {
                            st.setParent(attParentBucket.getKeyAsText().string());
                            if( attParentBucket != null && attParentBucket.getAggregations() != null
                                    && attParentBucket.getAggregations().get("attValues") != null )
                            {
                                List<String> stratumValues = new ArrayList<String>();
                                Terms super_Sector_terms = attParentBucket.getAggregations().get("attValues");
                                Collection<Bucket> buckets2 = super_Sector_terms.getBuckets();
                                for( Terms.Bucket bucket2 : buckets2 )
                                {
                                    stratumValues.add(bucket2.getKeyAsText().string());

                                    ReverseNested reverse_nested = bucket2.getAggregations().get("reverseNested");
                                    InternalNested location_terms = reverse_nested.getAggregations().get("locations");
                                    Terms locationType = location_terms.getAggregations().get("locationType");

                                    Collection<Terms.Bucket> buckets5 = locationType.getBuckets();
                                    for( Terms.Bucket bucket5 : buckets5 )
                                    {
                                        LocationAggrigation loc;
                                        if( locationList.get(bucket5.getKeyAsText().string()) != null )
                                        {
                                            loc = locationList.get(bucket5.getKeyAsText().string());
                                        }
                                        else
                                        {
                                            loc = new LocationAggrigation();
                                        }

                                        Terms superregion = bucket5.getAggregations().get("locationName");

                                        Collection<Terms.Bucket> buckets6 = superregion.getBuckets();
                                        Set<String> locationName = new TreeSet<String>();
                                        for( Terms.Bucket bucket6 : buckets6 )
                                        {
                                            String locationNam = bucket6.getKeyAsText().string();
                                            if( locationNam.contains("(") && locationNam.contains(")") )
                                            {
                                                locationNam = locationNam.substring(0, locationNam.indexOf('('));
                                            }
                                            locationName.add(locationNam);
                                        }
                                        if( loc.getLocationName() != null )
                                        {
                                            loc.getLocationName().addAll(locationName);
                                        }
                                        else
                                        {
                                            loc.setLocationName(locationName);
                                        }
                                        locationList.put(bucket5.getKeyAsText().string(), loc);
                                    }

                                }
                                stratum.put(bucket.getKeyAsText().string(), stratumValues);
                                stratumList.add(st);
                            }
                        }
                    }
                }

            }
            response.setStratum(stratum);
            response.setLocations(locationList);
            response.setStratumList(stratumList);
        }
        catch( Exception e )
        {
            throw e;
        }
        return response;
    }

}
