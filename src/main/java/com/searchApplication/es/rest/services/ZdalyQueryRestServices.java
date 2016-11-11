package com.searchApplication.es.rest.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.searchApplication.entities.CassandraFilterRequest;
import com.searchApplication.entities.FilterRequest;
import com.searchApplication.entities.QueryResultsList;
import com.searchApplication.entities.SearchOutput;
import com.searchApplication.entities.TimeSeriesEntity;
import com.searchApplication.entities.TransactionResponse;
import com.searchApplication.es.entities.BucketResponseList;
import com.searchApplication.es.interfaces.ZdalyQueryServices;
import com.searchApplication.utils.ZdalyCassandraConnection;

@Path("/zdaly")
@RestController
@PropertySource("sample-key.properties")
public class ZdalyQueryRestServices
{
	@Value("${zDaly.salt}")
	private String salt;

	@Autowired
	private ZdalyQueryServices zdalyQueryServices;
	static final Logger LOG = LoggerFactory.getLogger(ZdalyQueryRestServices.class);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/wilcard-search/{queryString}")
	public TransactionResponse produceBucekts(@NotNull(message = "Cannot be null") @PathParam("queryString") String queryString) throws Exception
	{

		TransactionResponse transactionResponse = new TransactionResponse();
		transactionResponse.setStatus("200");
		transactionResponse.setResponseMessage("Successfull");
		transactionResponse.setResponseType("Object");
		try
		{
			long startTime = System.currentTimeMillis();
			BucketResponseList results = zdalyQueryServices.produceBuckets(queryString);

			if (results != null)
			{
				transactionResponse.setResponseEntity(results);
			} else
			{
				transactionResponse.setResponseMessage("No results found");
			}
			long endTime = System.currentTimeMillis();

			System.out.println("Service took - " + (endTime - startTime) + " milliseconds to execute");
		} catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		return transactionResponse;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/match-query/{queryString}")
	public TransactionResponse matchQuery(@NotNull(message = "Cannot be null") @PathParam("queryString") String queryString) throws Exception
	{

		TransactionResponse transactionResponse = new TransactionResponse();
		transactionResponse.setStatus("200");
		transactionResponse.setResponseMessage("Successfull");
		transactionResponse.setResponseType("Object");
		try
		{
			long startTime = System.currentTimeMillis();
			SearchOutput results = zdalyQueryServices.matchQuery(queryString);
			if (results != null)
			{
				transactionResponse.setResponseEntity(results);
			} else
			{
				transactionResponse.setResponseMessage("No results found");
			}
			long endTime = System.currentTimeMillis();

			System.out.println("Service took - " + (endTime - startTime) + " milliseconds to execute");
		} catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		return transactionResponse;
	}

	@POST
	@Consumes(
	{ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces(
	{ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("/query-with-filters")
	public TransactionResponse queryWithFilters(FilterRequest filterRequest) throws Exception
	{
		TransactionResponse transactionResponse = new TransactionResponse();
		transactionResponse.setStatus("200");
		transactionResponse.setResponseMessage("Successfull");
		transactionResponse.setResponseType("Object");
		try
		{
			long startTime = System.currentTimeMillis();
			SearchOutput results = zdalyQueryServices.queryWithFilters(filterRequest);
			if (results != null)
			{
				transactionResponse.setResponseEntity(results);
			} else
			{
				transactionResponse.setResponseMessage("No results found");
			}
			long endTime = System.currentTimeMillis();

			System.out.println("Service took - " + (endTime - startTime) + " milliseconds to execute");

		} catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		return transactionResponse;
	}

	@POST
	@Consumes(
	{ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces(
	{ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("/query-results")
	public TransactionResponse queryResults(FilterRequest filterRequest) throws Exception
	{
		TransactionResponse transactionResponse = new TransactionResponse();
		transactionResponse.setStatus("200");
		transactionResponse.setResponseMessage("Successfull");
		transactionResponse.setResponseType("Object");
		try
		{
			long startTime = System.currentTimeMillis();
			QueryResultsList results = zdalyQueryServices.queryResults(filterRequest);
			if (results != null)
			{
				transactionResponse.setResponseEntity(results);
			} else
			{
				transactionResponse.setResponseMessage("No results found");
			}
			long endTime = System.currentTimeMillis();

			System.out.println("Service took - " + (endTime - startTime) + " milliseconds to execute");

		} catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		return transactionResponse;
	}

	@POST
	@Consumes(
	{ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces(
	{ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("/get-time-series-data")
	public TransactionResponse getTimeSeriesData(@RequestBody List<CassandraFilterRequest> requests) throws Exception
	{
		long starttime = System.currentTimeMillis();
		List<TimeSeriesEntity> list = new ArrayList<>();
		TransactionResponse transactionResponse = new TransactionResponse();
		transactionResponse.setStatus(HttpStatus.OK.toString());
		transactionResponse.setResponseMessage("Successfull");
		transactionResponse.setResponseType("Object");
		try
		{
			for (CassandraFilterRequest request : requests)
			{
				LOG.info(request.toString());
				String db_name = request.getDbName();
				String fromDate = request.getFromDate();
				String toDate = request.getToDate();
				BigInteger series_id = new BigInteger(request.getSeriesId());
				Session session = ZdalyCassandraConnection.getCassandraSession();
				StringBuffer sql = new StringBuffer("select * from time_series_data where series_id = ? and db_name= ? and period='d'  ");
				/*if (fromDate != null)
				{
					sql.append("  and dttm >= " + "\'" + fromDate + "\'");
				}
				if (toDate != null)
				{
					sql.append("  and dttm < " + "\'" + toDate + "\'");
				}*/
				LOG.debug(sql.toString());
				// String series_id = HashUtil.encode(request.getSeriesId(),
				// this.salt);
				ResultSet rs = session.execute(sql.toString(), series_id, db_name);
				Iterator<Row> itr = rs.iterator();
				while (itr.hasNext())
				{
					Row row = itr.next();
					//list.add(new TimeSeriesEntity(row.getVarint(0), row.getString(1), row.getDouble(5), row.getString(4)));
					
					list.add(new TimeSeriesEntity(row.getVarint(1), row.getString(0), row.getDouble(5), row.getString(3)));
				}
			}
			transactionResponse.setResponseEntity(list);
		} catch (Exception exp)
		{
			handleException(exp);
			transactionResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.toString());
			transactionResponse.setResponseMessage("Error : " + exp.getMessage());
		}
		long end = System.currentTimeMillis();
		LOG.debug(" requests : " + requests.size() + " Time taken : " + (end - starttime) + "ms");
		return transactionResponse;
	}

	public void handleException(Exception exp) throws IOException
	{
		StringWriter sw = new StringWriter();
		PrintWriter prnt = new PrintWriter(sw);
		exp.printStackTrace(prnt);
		LOG.error(sw.toString());
		sw.close();
	}
}