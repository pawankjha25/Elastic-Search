package com.searchApplication.utils;

import java.net.UnknownHostException;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.data.cassandra.core.CassandraOperations;
//import org.springframework.data.cassandra.core.CassandraTemplate;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class ZdalyCassandraConnection {

	private static final Logger LOG = LoggerFactory.getLogger(ZdalyCassandraConnection.class);

	private static Cluster cluster;
	private static Session session;

	public static Session getCassandraSession() throws UnknownHostException {
		if (session == null) {
			cluster = Cluster.builder().withPort(9042)
					.addContactPoints("ec2-54-70-111-205.us-west-2.compute.amazonaws.com").build();
			final Metadata metadata = cluster.getMetadata();
			System.out.printf("Connected to cluster: %s\n", metadata.getClusterName());
			for (final Host host : metadata.getAllHosts()) {
				System.out.printf("Datacenter: %s; Host: %s; Rack: %s\n", host.getDatacenter(), host.getAddress(),
						host.getRack());
			}
			session = cluster.connect("test");
		}
		return session;

	}
}