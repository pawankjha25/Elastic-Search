package com.searchApplication.utils;

import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
//import org.springframework.data.cassandra.core.CassandraOperations;
//import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.context.annotation.Configuration;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;

@Configuration
public class ZdalyCassandraConnection
{

	private static final Logger LOG = LoggerFactory.getLogger(ZdalyCassandraConnection.class);

	private static Cluster cluster;
	private static Session session;
	private static final String cassandraIP = "ec2-54-70-111-205.us-west-2.compute.amazonaws.com";
	private static final int cassandraPORT = 9042;

	@Bean
	public Session initCassandraSession() throws UnknownHostException
	{
		if (session == null)
		{
			LOG.debug("Inside session == null");
			cluster = Cluster.builder().withPort(cassandraPORT).addContactPoints(cassandraIP).build();
			LOG.debug(" Cluster Initialized " + cluster.getClusterName());
			final Metadata metadata = cluster.getMetadata();
			for (final Host host : metadata.getAllHosts())
			{
				LOG.debug("Datacenter: {}  Host: {}  Rack: {}", host.getDatacenter(), host.getAddress(), host.getRack());
			}
			session = cluster.connect("test");
			LOG.debug("Connected to Cassandra Keyspace test");
		}
		return session;
	}

	public static Session getCassandraSession()
	{
		return session;
	}
}