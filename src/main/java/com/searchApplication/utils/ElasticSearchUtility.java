package com.searchApplication.utils;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest;
import org.elasticsearch.action.admin.indices.close.CloseIndexResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.searchApplication.es.entities.TimeSeriesData;

@Service
public class ElasticSearchUtility {

	public static Environment env;
	private Gson gson = new Gson();

	private static Logger logger = LoggerFactory.getLogger(ElasticSearchUtility.class);

	private static Client c = null;
	public static XContentBuilder mapping = null;

	public ElasticSearchUtility() {
	}

	public static void getInstance(Environment env) {
		System.out.println(env);
		ElasticSearchUtility.env = env;
		addClient();

	}

	public static Client addClient() {
		try {
			//
			Settings settings = Settings.settingsBuilder().put("cluster.name", env.getProperty("es.cluster_name"))
					.put("number_of_shards", env.getProperty("es.num_shards"))
					.put("number_of_replicas", env.getProperty("es.num_replicas")).build();
			c = TransportClient.builder().settings(settings).build().addTransportAddress(
					new InetSocketTransportAddress(InetAddress.getByName(env.getProperty("es.host")), 9300));
			// Settings settings =
			// ImmutableSettings.builder().put("cluster.name",
			// env.getProperty("es.cluster_name"))
			// .put("number_of_replicas",
			// env.getProperty("es.num_replicas")).build();
			// c = new TransportClient(settings).addTransportAddress(
			// new
			// InetSocketTransportAddress(InetAddress.getByName(env.getProperty("es.host")),
			// 9300));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return c;
	}

	/**
	 * Get the client of a node from the elastic search server
	 * 
	 * @return
	 * @throws Exception
	 */
	public Client getESClient() throws Exception {
		Client client = null;
		try {
			client = c;
		} catch (Exception e) {
			logger.error("Error encountered while getting elasticsearch server client", e);
		}
		return client;
	}

	/**
	 * Close the client
	 * 
	 * @throws Exception
	 */
	public void closeClient() throws Exception {
		try {
			c.close();
		} catch (Exception e) {
			logger.error("Error encountered while closing elasticsearch server client", e);
		}
	}

	/**
	 * Creating the index in the elastic search server
	 * 
	 * @param c
	 * @return
	 * @throws Exception
	 */
	public ActionFuture<CreateIndexResponse> createIndex(Client c) throws Exception {
		ActionFuture<CreateIndexResponse> res = null;
		try {
			CreateIndexRequest req = new CreateIndexRequest(env.getProperty("es.index_name"));
			res = c.admin().indices().create(req);
		} catch (Exception e) {
			logger.error("Error encountered while creating index in elasticsearch server", e);
		}
		return res;
	}

	/**
	 * add the emails as documents in to the elastic search server (indexing
	 * emails)
	 * 
	 * @param indexName
	 * @param type
	 * @param o
	 * @return
	 * @throws Exception
	 */
	public BulkResponse addDocsInBulk(Client client, String indexName, String type, List<TimeSeriesData> o)
			throws Exception {
		BulkResponse bulkResponse = null;
		BulkRequestBuilder request = c.prepareBulk();

		try {
			BulkProcessor bulkProcessor = BulkProcessor.builder(client, new BulkProcessor.Listener() {

				@Override
				public void beforeBulk(long executionId, BulkRequest request) {
					logger.info("Going to execute new bulk composed of {} actions", request.numberOfActions());
				}

				@Override
				public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
					logger.info("Executed bulk composed of {} actions", request.numberOfActions());
				}

				@Override
				public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
					logger.warn("Error executing bulk", failure);
				}
			}).setBulkActions(o.size()).setConcurrentRequests(1000).build();

			for (TimeSeriesData ob : o) {
				String id = ob.getDb().getDb_name() + "-" + ob.getDb().getProperties();
				request.add(client.prepareIndex(indexName, type).setId(id).setSource(gson.toJson(ob)));
				bulkProcessor.add(new IndexRequest(indexName, type, id).source(gson.toJson(ob)));
			}
			bulkProcessor.awaitClose(10, TimeUnit.MINUTES);
		} catch (Exception e) {
			throw e;
		}
		return bulkResponse;
	}

	public IndexResponse addDoc(Client c, String indexName, String type, TimeSeriesData o) throws Exception {
		Gson gson = new Gson();
		IndexResponse res = new IndexResponse();
		try {
			String id = o.getDb().getDb_name() + "-" + o.getDb().getProperties();
			res = c.prepareIndex(indexName, type, id).setSource(gson.toJson(o)).execute().actionGet();
			System.out.println("Indexing a document");
		} catch (Exception e) {
			throw e;
		}
		return res;
	}

	/**
	 * Delete a document from the ES server
	 * 
	 * @param indexName
	 * @param type
	 * @param docId
	 * @return
	 * @throws Exception
	 */
	public ActionFuture<DeleteResponse> deleteDoc(String indexName, String type, String docId) throws Exception {
		DeleteRequest req = new DeleteRequest(indexName, type, docId);
		ActionFuture<DeleteResponse> res = null;
		try {
			res = c.delete(req);
		} catch (Exception e) {
			throw e;
		}
		return res;
	}

	public void openIndex(String indexName) throws Exception {
		getESClient().admin().indices().open(new OpenIndexRequest(indexName));
	}

	public ActionFuture<OpenIndexResponse> openAllIndices() throws Exception {
		ActionFuture<OpenIndexResponse> res = getESClient().admin().indices().open(new OpenIndexRequest("_all"));
		return res;
	}

	public void closeIndex(String indexName) throws Exception {
		getESClient().admin().indices().close(new CloseIndexRequest(indexName));
	}

	public ActionFuture<CloseIndexResponse> closeAllIndices() throws Exception {
		ActionFuture<CloseIndexResponse> res = getESClient().admin().indices().close(new CloseIndexRequest("_all"));
		return res;
	}

}
