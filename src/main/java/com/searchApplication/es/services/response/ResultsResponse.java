package com.searchApplication.es.services.response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.bucket.nested.InternalNested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import com.searchApplication.entities.QueryResults;
import com.searchApplication.entities.QueryResultsList;
import com.searchApplication.es.entities.Data;

public class ResultsResponse {

    public static QueryResultsList getResults( SearchResponse tFdocs ) throws Exception
    {
        QueryResultsList response = new QueryResultsList();
        Set<QueryResults> results = new TreeSet<QueryResults>();
        try
        {

            InternalNested attributes = tFdocs.getAggregations().get("attributes");
            Terms attTypes = attributes.getAggregations().get("attTypes");
            for( Terms.Bucket bucket4 : attTypes.getBuckets() )
            {
                if( bucket4.getKeyAsText().string().equals("Details") )
                {
                    if( bucket4 != null && bucket4.getAggregations() != null
                            && bucket4.getAggregations().get("attributesValues") != null )
                    {
                        Terms attValues = bucket4.getAggregations().get("attributesValues");
                        Collection<Bucket> buckets2 = attValues.getBuckets();
                        for( Terms.Bucket bucket5 : buckets2 )
                        {
                            if( bucket5.getKeyAsText().string() != null && !bucket5.getKeyAsText().string().equals("null") )
                            {
                                QueryResults qr = new QueryResults();
                                List<Data> data = new ArrayList<Data>();
                                Data d = new Data();
                                List<Long> seriesId = new ArrayList<Long>();
                                d.setDetails(bucket5.getKeyAsText().string());

                                InternalNested database = bucket5.getAggregations().get("database");
                                Terms db_name = database.getAggregations().get("dbname");

                                for( Terms.Bucket bucket : db_name.getBuckets() )
                                {
                                    qr.setDbName(bucket.getKeyAsText().string());
                                    if( bucket != null && bucket.getAggregations() != null
                                            && bucket.getAggregations().get("dbproperties") != null )
                                    {
                                        Terms db_properties = bucket.getAggregations().get("dbproperties");
                                        Collection<Bucket> buckets1 = db_properties.getBuckets();
                                        for( Terms.Bucket bucket1 : buckets1 )
                                        {
                                            qr.setPropertyId(new Long(bucket1.getKeyAsText().string()));
                                            InternalNested sectorTerms = bucket1.getAggregations().get("locations");
                                            Terms buckets = sectorTerms.getAggregations().get("locationid");

                                            for( Terms.Bucket bucket3 : buckets.getBuckets() )
                                            {
                                                seriesId.add(new Long(bucket3.getKeyAsText().string()));
                                            }
                                            d.setSeriesId(seriesId);
                                        }
                                    }
                                }
                                data.add(d);
                                qr.setData(data);
                                results.add(qr);
                            }
                        }
                    }
                }
            }
            response.setResults(results);
        }
        catch( Exception e )
        {
            throw e;
        }
        return response;
    }

}
