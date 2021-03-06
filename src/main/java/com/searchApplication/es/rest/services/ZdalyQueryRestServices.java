package com.searchApplication.es.rest.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.searchApplication.Compress;
import com.searchApplication.entities.CassandraFilterRequest;
import com.searchApplication.entities.FilterRequest;
import com.searchApplication.entities.QueryResultsList;
import com.searchApplication.entities.SearchOutput;
import com.searchApplication.entities.SeriesIdStatistics;
import com.searchApplication.entities.TimeSeriesEntity;
import com.searchApplication.entities.TransactionResponse;
import com.searchApplication.es.entities.BucketResponseList;
import com.searchApplication.es.interfaces.ZdalyQueryServices;
import com.searchApplication.es.search.aggs.InsdustriInfo;
import com.searchApplication.es.services.impl.FetchTimeSeriesData;
import com.searchApplication.utils.ThreadLocalSDF;
import com.searchApplication.utils.ZdalyCassandraConnection;

import zdaly.etl.util.HashUtil;

@Path("/zdaly")
@RestController
@PropertySources({ @PropertySource("classpath:database.properties") })
public class ZdalyQueryRestServices {
	@Value("${zDaly.cassandra.salt}")
	private String salt;

	@Value("${zDaly.cassandra.read.consistency.level}")
	private String readConsistencyLevel;

	@Value("${zDaly.cassandra.encrypt}")
	private boolean encrypted;

	@Value("${update.cache.api}")
	private boolean updateCacheApi;

	@Autowired
	private ZdalyQueryServices zdalyQueryServices;

	@Autowired
	private FetchTimeSeriesData fetchTimeSeriesData;

	// Re-use prepared statement to optimize performance.
	private Map<String, PreparedStatement> preparedStatements = new ConcurrentHashMap<>(); // Thread
																							// safety
																							// is
																							// required.

	static final Logger LOG = LoggerFactory.getLogger(ZdalyQueryRestServices.class);

	@GET
	@Compress
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/industry-info")
	public TransactionResponse getIndustryInfo() throws Exception {
		TransactionResponse transactionResponse = new TransactionResponse();

		try {
			List<InsdustriInfo> results = zdalyQueryServices.getIndustryInfo();
			transactionResponse.setStatus("200");
			transactionResponse.setResponseMessage("Successfull");
			transactionResponse.setResponseType("Object");
			if (results != null) {
				transactionResponse.setResponseEntity(results);
			} else {
				transactionResponse.setResponseMessage("No results found");
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return transactionResponse;
	}

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
			BucketResponseList results = zdalyQueryServices.produceBuckets(queryString, false);

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
	@Path("/wildcard-search-with-cache/{queryString}/{updateCache}")
	public TransactionResponse produceBucektsWithCache(
			@NotNull(message = "Cannot be null") @PathParam("queryString") String queryString,
			@NotNull(message = "Cannot be null") @PathParam("updateCache") String updateCache) throws Exception {

		TransactionResponse transactionResponse = new TransactionResponse();
		transactionResponse.setStatus("200");
		transactionResponse.setResponseMessage("Successfull");
		transactionResponse.setResponseType("Object");
		if(!updateCacheApi) {
			transactionResponse.setStatus("403");
			transactionResponse.setResponseMessage("Method not allowed");
			return transactionResponse;
		}
		try {
			long startTime = System.currentTimeMillis();
			boolean update = false;
			if("true".equalsIgnoreCase(updateCache)) {
				update = true;
			}
			BucketResponseList results = zdalyQueryServices.produceBuckets(queryString, update);

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
	@Compress
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
	@Compress
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
	@Compress
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
			list = fetchTimeSeriesData.getData(requests);

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

	private ConsistencyLevel getReadConsistencyLevel() {
		return ConsistencyLevel.valueOf(readConsistencyLevel.toUpperCase());
	}

	@POST
	@Compress
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("/get-time-series-stat-data")
	public TransactionResponse getTimeSeriesStatData(@RequestBody List<CassandraFilterRequest> requests)
			throws Exception {
		long starttime = System.currentTimeMillis();
		List<SeriesIdStatistics> list = new ArrayList<>();
		TransactionResponse transactionResponse = new TransactionResponse();
		transactionResponse.setStatus(HttpStatus.OK.toString());
		transactionResponse.setResponseMessage("Successfull");
		transactionResponse.setResponseType("Series Id Statistics ");
		DateFormat sdf = ThreadLocalSDF.getDateFormatter();
		try {
			Session session = ZdalyCassandraConnection.getCassandraSession();
			ResultSet rs = null;
			for (CassandraFilterRequest request : requests) {
				LOG.info(request.toString());
				String seriesId = request.getSeriesId();
				String dbName = request.getDbName();
				String tableName = request.getTableName();
				if (tableName == null || tableName.length() == 0) {
					transactionResponse.setResponseMessage("DB Name is null/Empty for one or more cases ");
				}
				String casDbName = dbName;

				String casTableName = tableName;
				if (encrypted) {
					casDbName = HashUtil.encode(dbName, salt);
					casTableName = HashUtil.encode(tableName, salt);
				}
				Map<String, Object> valueMap = new LinkedHashMap<>();
				StringBuilder sql = new StringBuilder(
						"select table_name,series_id,start_date,end_date,row_count from time_series_data_stat ");
				sql.append("where db_name = ? ");
				valueMap.put("dbName", casDbName);
				if (tableName != null) {
					sql.append("and table_name = ? ");
					valueMap.put("table_name", casTableName);
				}
				if (seriesId != null) {
					sql.append("and series_id = ? ");
					valueMap.put("seriesId", seriesId);
				}
				LOG.debug(sql.toString());
				rs = session.execute(sql.toString(), valueMap.values().toArray());
				Iterator<Row> itr = rs.iterator();
				while (itr.hasNext()) {
					Row row = itr.next();
					list.add(new SeriesIdStatistics(row.getString("series_id"), row.getString("table_name"),
							sdf.format(row.getTimestamp("start_date")), sdf.format(row.getTimestamp("end_date")),
							row.getVarint("row_count")));
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

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/health")
	public String healthCheckUp() throws Exception {
		return zdalyQueryServices.health();
	}
}