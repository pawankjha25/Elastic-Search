package com.searchApplication.es.rest.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.context.annotation.PropertySources;
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
import com.searchApplication.entities.SeriesIdStatistics;
import com.searchApplication.entities.TimeSeriesEntity;
import com.searchApplication.entities.TransactionResponse;
import com.searchApplication.es.entities.BucketResponseList;
import com.searchApplication.es.interfaces.ZdalyQueryServices;
import com.searchApplication.utils.ZdalyCassandraConnection;

import zdaly.etl.util.HashUtil;

@Path("/zdaly")
@RestController
@PropertySources({ @PropertySource("classpath:database.properties") })
public class ZdalyQueryRestServices {
	@Value("${zDaly.cassandra.salt}")
	private String salt;

	@Value("${zDaly.cassandra.encrypt}")
	private boolean encrypted;

	@Autowired
	private ZdalyQueryServices zdalyQueryServices;
	static final Logger LOG = LoggerFactory.getLogger(ZdalyQueryRestServices.class);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/wilcard-search/{queryString}")
	public TransactionResponse produceBucekts(@NotNull(message = "Cannot be null") @PathParam("queryString") String queryString) throws Exception {

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
	public TransactionResponse matchQuery(@NotNull(message = "Cannot be null") @PathParam("queryString") String queryString) throws Exception {

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

	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("/get-time-series-data")
	public TransactionResponse getTimeSeriesData(@RequestBody List<CassandraFilterRequest> requests) throws Exception {
		long starttime = System.currentTimeMillis();
		List<TimeSeriesEntity> list = new ArrayList<>();
		TransactionResponse transactionResponse = new TransactionResponse();
		transactionResponse.setStatus(HttpStatus.OK.toString());
		transactionResponse.setResponseMessage("Successfull");
		transactionResponse.setResponseType("Object");
		try {
			Session session = ZdalyCassandraConnection.getCassandraSession();
			for (CassandraFilterRequest request : requests) {
				LOG.info(request.toString());

				String db_name = request.getDbName();
				String casDbName = db_name;
				if (encrypted) {
					casDbName = HashUtil.encode(db_name, salt);
				}
				String fromDate = request.getFromDate();
				String toDate = request.getToDate();
				String period = request.getPeriod();
				String series_id = request.getSeriesId();
				Map<String, Object> valueMap = new LinkedHashMap<>();
				StringBuilder sql = new StringBuilder("select series_id, db_name, date,value from time_series_data ");
				sql.append("where db_name= ? ");
				valueMap.put("db_name", casDbName);

				if (series_id != null) {
					sql.append("and series_id = ?");
					valueMap.put("series_id", series_id);
				}
				if (period != null) {
					sql.append("and period = ?");
					valueMap.put("period", period);
				}
				if (fromDate != null) {
					sql.append("and dttm >= ?");
					valueMap.put("dttm", fromDate);
				}
				if (toDate != null) {
					sql.append("and dttm < ?");
					valueMap.put("dttm", toDate);
				}
				LOG.debug(sql.toString());

				ResultSet rs = session.execute(sql.toString(), valueMap.values().toArray());
				Iterator<Row> itr = rs.iterator();
				while (itr.hasNext()) {
					Row row = itr.next();
					list.add(new TimeSeriesEntity(row.getString(0), row.getString(1), row.getDecimal(3), row.getString(2)));
				}
			}
			transactionResponse.setResponseEntity(list);
		} catch (Exception exp) {
			handleException(exp);
			transactionResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.toString());
			transactionResponse.setResponseMessage("Error : " + exp.getMessage());
		}
		long end = System.currentTimeMillis();
		LOG.debug(" requests : " + requests.size() + " Time taken : " + (end - starttime) + "ms");
		return transactionResponse;
	}

	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("/get-time-series-stat-data")
	public TransactionResponse getTimeSeriesStatData(@RequestBody List<CassandraFilterRequest> requests) throws Exception {
		long starttime = System.currentTimeMillis();
		List<SeriesIdStatistics> list = new ArrayList<>();
		TransactionResponse transactionResponse = new TransactionResponse();
		transactionResponse.setStatus(HttpStatus.OK.toString());
		transactionResponse.setResponseMessage("Successfull");
		transactionResponse.setResponseType("Series Id Statistics ");
		try {
			Session session = ZdalyCassandraConnection.getCassandraSession();
			ResultSet rs =null;
			for (CassandraFilterRequest request : requests) {
				LOG.info(request.toString());
				String seriesId = request.getSeriesId();
				String tableName = request.getDbName();
				if(tableName==null || tableName.length()==0)
				{
					throw new Exception("DB Name can't be NULL or empty");
				}
				Map<String, Object> valueMap = new LinkedHashMap<>();
				StringBuilder sql = new StringBuilder("select table_name,series_id,start_date,end_date,row_count from time_series_data_stat ");
				sql.append("where table_name = ?");
				valueMap.put("tableName", tableName);
				if (seriesId != null) {
					sql.append("and series_id = ?");
					valueMap.put("seriesId", seriesId);
				}
				LOG.debug(sql.toString());

				rs = session.execute(sql.toString(), valueMap.values().toArray());
				Iterator<Row> itr = rs.iterator();
				while (itr.hasNext()) {
					Row row = itr.next();
					list.add(new SeriesIdStatistics(row.getString("series_id"),row.getString("table_name"), row.getTimestamp("start_date"), row.getTimestamp("end_date"), row.getInt("row_count")));
				}
			}
			transactionResponse.setResponseEntity(list);
		} catch (Exception exp) {
			handleException(exp);
			transactionResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.toString());
			transactionResponse.setResponseMessage("Error : " + exp.getMessage());
		}
		long end = System.currentTimeMillis();
		LOG.debug(" requests : " + requests.size() + " Time taken : " + (end - starttime) + "ms");
		return transactionResponse;
	}

	public void handleException(Exception exp) throws IOException {
		StringWriter sw = new StringWriter();
		PrintWriter prnt = new PrintWriter(sw);
		exp.printStackTrace(prnt);
		LOG.error(sw.toString());
		sw.close();
	}
}