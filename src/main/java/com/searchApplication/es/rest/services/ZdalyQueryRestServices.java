package com.searchApplication.es.rest.services;

import java.math.BigInteger;
import java.net.UnknownHostException;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
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
public class ZdalyQueryRestServices {

	@Autowired
	private ZdalyQueryServices zdalyQueryServices;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/wilcard-search/{queryString}")
	public TransactionResponse produceBucekts(
			@NotNull(message = "Cannot be null") @PathParam("queryString") String queryString) throws Exception {

		TransactionResponse transactionResponse = new TransactionResponse();
		transactionResponse.setStatus("200");
		transactionResponse.setResponseMessage("Successfull");
		transactionResponse.setResponseType("Object");
		try {
			long startTime = System.currentTimeMillis();
			BucketResponseList results = zdalyQueryServices.produceBuckets(queryString);

			if (results != null) {
				transactionResponse.setResponseEntity(results);
			} else {
				transactionResponse.setResponseMessage("No results found");
			}
			long endTime = System.currentTimeMillis();

			System.out.println("Service took - " + (endTime - startTime) + " milliseconds to execute");
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return transactionResponse;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/match-query/{queryString}")
	public TransactionResponse matchQuery(
			@NotNull(message = "Cannot be null") @PathParam("queryString") String queryString) throws Exception {

		TransactionResponse transactionResponse = new TransactionResponse();
		transactionResponse.setStatus("200");
		transactionResponse.setResponseMessage("Successfull");
		transactionResponse.setResponseType("Object");
		try {
			long startTime = System.currentTimeMillis();
			SearchOutput results = zdalyQueryServices.matchQuery(queryString);
			if (results != null) {
				transactionResponse.setResponseEntity(results);
			} else {
				transactionResponse.setResponseMessage("No results found");
			}
			long endTime = System.currentTimeMillis();

			System.out.println("Service took - " + (endTime - startTime) + " milliseconds to execute");
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return transactionResponse;
	}

	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("/query-with-filters")
	public TransactionResponse queryWithFilters(FilterRequest filterRequest) throws Exception {
		TransactionResponse transactionResponse = new TransactionResponse();
		transactionResponse.setStatus("200");
		transactionResponse.setResponseMessage("Successfull");
		transactionResponse.setResponseType("Object");
		try {
			long startTime = System.currentTimeMillis();
			SearchOutput results = zdalyQueryServices.queryWithFilters(filterRequest);
			if (results != null) {
				transactionResponse.setResponseEntity(results);
			} else {
				transactionResponse.setResponseMessage("No results found");
			}
			long endTime = System.currentTimeMillis();

			System.out.println("Service took - " + (endTime - startTime) + " milliseconds to execute");

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return transactionResponse;
	}

	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("/query-results")
	public TransactionResponse queryResults(FilterRequest filterRequest) throws Exception {
		TransactionResponse transactionResponse = new TransactionResponse();
		transactionResponse.setStatus("200");
		transactionResponse.setResponseMessage("Successfull");
		transactionResponse.setResponseType("Object");
		try {
			long startTime = System.currentTimeMillis();
			QueryResultsList results = zdalyQueryServices.queryResults(filterRequest);
			if (results != null) {
				transactionResponse.setResponseEntity(results);
			} else {
				transactionResponse.setResponseMessage("No results found");
			}
			long endTime = System.currentTimeMillis();

			System.out.println("Service took - " + (endTime - startTime) + " milliseconds to execute");

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return transactionResponse;
	}

	@GET
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("/get-time-series-data")
	public List<TimeSeriesEntity> getTimeSeriesData() throws UnknownHostException {
		List<TimeSeriesEntity> list = new ArrayList<>();
		Session session = ZdalyCassandraConnection.getCassandraSession();
		ResultSet rs = session.execute("select * from time_series_data");
		Iterator<Row> itr = rs.iterator();
		int i = 0;
		while (itr.hasNext() && i < 10) {
			i++;
			Row row = itr.next();
			
			System.out.println(row.getVarint(0));
			System.out.println(row.getString(1));
			System.out.println(row.getDouble(5));
			list.add(new TimeSeriesEntity(row.getVarint(0), row.getString(1), row.getDouble(5)));

		}
		return list;
	}

}
