package com.searchApplication.es.services.impl;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Resource;
import org.elasticsearch.client.Client;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import com.searchApplication.es.entities.PerformanceBucket;
import com.searchApplication.es.search.bucketing.AttributeBucketer;
import com.searchApplication.utils.ElasticSearchUtility;

@Service
public class PerformanceTest {

	@Resource
	private Environment env;

	public static Client client = null;

	public PerformanceTest()
	{
		PerformanceTest.client = ElasticSearchUtility.addClient();
	}

	public List<PerformanceBucket> performanceCheck( String[] queryText, boolean copyToFile )
	{
		int[] sizes = new int[] { 1000, 500, 200, 100 };
		List<PerformanceBucket> list = new ArrayList<>();
		try
		{
			PrintWriter pw = new PrintWriter(new File("/ebs/apps/test.csv"));
			StringBuilder sb = new StringBuilder();
			sb.append("Search String");
			sb.append(',');
			sb.append("fetchSize");
			sb.append(',');
			sb.append("timeTakenToHitES");
			sb.append(',');
			sb.append("timeTakenToGetEsResults");
			sb.append(',');
			sb.append("timeTakenToProcessBuckets");
			sb.append(',');
			sb.append("timeTakenToSortBuckets");
			sb.append(',');
			sb.append("totalTimeTaken");
			sb.append('\n');
			pw.write(sb.toString());

			for( Integer size : sizes )
			{
				pw.write("\n\n");
				for( String query : queryText )
				{

					PerformanceBucket b = AttributeBucketer.createBucketListPerformanceCheck(client,
							env.getProperty("es.index_name"), env.getProperty("es.search_object"), query, 1,
							size.intValue());
					StringBuilder sbr = new StringBuilder();
					sbr.append(b.getQueryString().replaceAll(",", ""));
					sbr.append(',');
					sbr.append(b.getFetchSize());
					sbr.append(',');
					sbr.append(b.getTimeTakenToHitES() + " ms");
					sbr.append(',');
					sbr.append(b.getTimeTakenToGetEsResults() + " ms");
					sbr.append(',');
					sbr.append(b.getTimeTakenToProcessBuckets() + " ms");
					sbr.append(',');
					sbr.append(b.getTimeTakenToSortBuckets() + " ms");
					sbr.append(',');
					sbr.append(((float) (b.getTotalTimeTaken() / 1000)) + " s");
					sbr.append('\n');
					pw.write(sbr.toString());
					list.add(b);
				}
			}
			pw.close();
			Collections.sort(list);
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		return list;

	}

}
