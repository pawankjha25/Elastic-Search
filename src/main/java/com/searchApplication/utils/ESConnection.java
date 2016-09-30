package com.searchApplication.utils;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class ESConnection {

	private Client client;

	public ESConnection(String clusterLocation, String clusterName) {
		Settings settings = ImmutableSettings.settingsBuilder().put("client.transport.sniff", true)
				.put("cluster.name", clusterName).build();
		this.client = new TransportClient(settings)
				.addTransportAddress(new InetSocketTransportAddress(clusterLocation, 9300));

	}

	public ESConnection(Client client) {
		this.client = client;
	}

	public Client getClient() {
		return client;
	}

}
