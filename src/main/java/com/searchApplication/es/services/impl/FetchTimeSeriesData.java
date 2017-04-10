package com.searchApplication.es.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.searchApplication.entities.CassandraFilterRequest;
import com.searchApplication.entities.TimeSeriesDataRequest;
import com.searchApplication.entities.TimeSeriesEntity;
import com.searchApplication.entities.TimeSeriesTable;
import com.searchApplication.utils.ZdalyCassandraConnection;

import zdaly.etl.util.HashUtil;

@Service("fetchTimeSeriesData")
public class FetchTimeSeriesData {

	private Session session = null;

	@Value("${zDaly.cassandra.keyspace}")
	private String keyspace;

	@Value("${zDaly.cassandra.salt}")
	private String salt;

	public List<TimeSeriesEntity> getData(List<CassandraFilterRequest> requests) {

		List<TimeSeriesEntity> list = new ArrayList<>();

		try {
			if (session == null) {
				session = ZdalyCassandraConnection.getCassandraSession();
			}
			Map<String, TimeSeriesDataRequest> reqList = formMapRequest(requests);

			for (String req : reqList.keySet()) {

				Statement get = getCassandrQuery(reqList.get(req));
				System.out.println(get.toString());

				ResultSet rs = session.execute(get);
				Iterator<Row> itr = rs.iterator();
				while (itr.hasNext()) {
					Row row = itr.next();
					list.add(new TimeSeriesEntity(row.getString(TimeSeriesTable.SERIES_ID),
							row.getString(TimeSeriesTable.TABLE_NAME), row.getDecimal("value"), row.getString("date"),
							row.getString("period"), row.getString("extended")));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return list;
	}

	private Map<String, TimeSeriesDataRequest> formMapRequest(List<CassandraFilterRequest> requests) {

		Map<String, TimeSeriesDataRequest> res = new HashMap<>();

		try {

			for (CassandraFilterRequest req : requests) {

				if (req.getDbName() != null && req.getTableName() != null) {

					String key = req.getDbName() + ":" + req.getTableName();
					TimeSeriesDataRequest data;

					if (res.get(key) == null) {

						data = new TimeSeriesDataRequest();
						data.setDbName(req.getDbName());
						data.setTableName(req.getTableName());
						data.setFromDate(req.getFromDate());
						data.setToDate(req.getToDate());

						List<String> seriesIds = new ArrayList<>();
						seriesIds.add(req.getSeriesId());
						data.setSeriesId(seriesIds);

					} else {

						data = res.get(key);
						data.getSeriesId().add(req.getSeriesId());

					}
					res.put(key, data);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return res;
	}

	private Statement getCassandrQuery(TimeSeriesDataRequest timeSeriesDataRequest) {

		Statement get = null;

		try {
			if (timeSeriesDataRequest.getDbName() != null && timeSeriesDataRequest.getTableName() != null) {

				String dbName = HashUtil.encode(timeSeriesDataRequest.getDbName().toLowerCase(), salt);
				String tableName = HashUtil.encode(timeSeriesDataRequest.getTableName(), salt);

				if (timeSeriesDataRequest.getPeriod() == null && timeSeriesDataRequest.getFromDate() == null
						&& timeSeriesDataRequest.getToDate() == null) {

					get = QueryBuilder.select().all().from(keyspace, "time_series_data").allowFiltering()
							.where(QueryBuilder.eq(TimeSeriesTable.DB_NAME, dbName))
							.and(QueryBuilder.eq(TimeSeriesTable.TABLE_NAME, tableName))
							.and(QueryBuilder.in(TimeSeriesTable.SERIES_ID, timeSeriesDataRequest.getSeriesId()));

				} else if (timeSeriesDataRequest.getPeriod() != null && timeSeriesDataRequest.getFromDate() == null
						&& timeSeriesDataRequest.getToDate() == null) {

					get = QueryBuilder.select().all().from(keyspace, "time_series_data").allowFiltering()
							.where(QueryBuilder.eq(TimeSeriesTable.DB_NAME, dbName))
							.and(QueryBuilder.eq(TimeSeriesTable.TABLE_NAME, tableName))
							.and(QueryBuilder.eq(TimeSeriesTable.PERIOD, timeSeriesDataRequest.getPeriod()))
							.and(QueryBuilder.in(TimeSeriesTable.SERIES_ID, timeSeriesDataRequest.getSeriesId()));

				} else if (timeSeriesDataRequest.getPeriod() == null && timeSeriesDataRequest.getFromDate() != null
						&& timeSeriesDataRequest.getToDate() != null) {

					get = QueryBuilder.select().all().from(keyspace, "time_series_data").allowFiltering()
							.where(QueryBuilder.eq(TimeSeriesTable.DB_NAME, dbName))
							.and(QueryBuilder.eq(TimeSeriesTable.TABLE_NAME, tableName))
							.and(QueryBuilder.eq(TimeSeriesTable.FROM_DATE, timeSeriesDataRequest.getFromDate()))
							.and(QueryBuilder.eq(TimeSeriesTable.TO_DATE, timeSeriesDataRequest.getToDate()))
							.and(QueryBuilder.in(TimeSeriesTable.SERIES_ID, timeSeriesDataRequest.getSeriesId()));

				} else {

					get = QueryBuilder.select().all().from(keyspace, "time_series_data").allowFiltering()
							.where(QueryBuilder.eq(TimeSeriesTable.DB_NAME, dbName))
							.and(QueryBuilder.eq(TimeSeriesTable.TABLE_NAME, tableName))
							.and(QueryBuilder.eq(TimeSeriesTable.FROM_DATE, timeSeriesDataRequest.getFromDate()))
							.and(QueryBuilder.eq(TimeSeriesTable.TO_DATE, timeSeriesDataRequest.getToDate()))
							.and(QueryBuilder.eq(TimeSeriesTable.PERIOD, timeSeriesDataRequest.getPeriod()))
							.and(QueryBuilder.in(TimeSeriesTable.SERIES_ID, timeSeriesDataRequest.getSeriesId()));
				}
			}

		} catch (Exception e) {
			throw e;
		}
		return get;
	}

}
