package com.searchApplication.utils;

import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
//import org.springframework.data.cassandra.core.CassandraOperations;
//import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;

@Configuration
@PropertySources(
{ @PropertySource("classpath:database.properties") })
public class ZdalyCassandraConnection
{
	private static final Logger LOG = LoggerFactory.getLogger(ZdalyCassandraConnection.class);
	private static Cluster cluster;
	private static Session session;
	@Value("${zDaly.hostName}")
	private String cassandraIP;
	@Value("${zDaly.cassandra.port}")
	private int cassandraPORT;
	@Value("${zDaly.cassandra.keyspace}")
	private String keyspace; 
	@Value("${zDaly.cassandra.connectionRetry}")
	private int connectionRetry;
	
	@Bean
	public Session initCassandraSession() throws UnknownHostException
	{
		if (session == null)
		{
			System.out.println( " cassandraIP " + cassandraIP);
			// if connection fails try every 5 mins
			long retryMillis = connectionRetry * 60 * 1000;
			LOG.debug("Inside session == null");
			cluster = Cluster.builder().withPort(cassandraPORT).withReconnectionPolicy(new ConstantReconnectionPolicy(retryMillis)).addContactPoints(cassandraIP).build();
			LOG.debug(" Cluster Initialized " + cluster.getClusterName());
			final Metadata metadata = cluster.getMetadata();
			for (final Host host : metadata.getAllHosts())
			{
				LOG.debug("Datacenter: {}  Host: {}  Rack: {}", host.getDatacenter(), host.getAddress(), host.getRack());
			}
			session = cluster.connect(keyspace);
			LOG.debug("Connected to Cassandra Keyspace zDaly");
		}
		return session;
	}

	public static Session getCassandraSession()
	{
		return session;
	}

	public String getCassandraIP()
	{
		return cassandraIP;
	}

	public void setCassandraIP(String cassandraIP)
	{
		this.cassandraIP = cassandraIP;
	}

	public int getCassandraPORT()
	{
		return cassandraPORT;
	}

	public void setCassandraPORT(int cassandraPORT)
	{
		this.cassandraPORT = cassandraPORT;
	}

	public String getKeyspace()
	{
		return keyspace;
	}

	public void setKeyspace(String keyspace)
	{
		this.keyspace = keyspace;
	}

	public int getConnectionRetry()
	{
		return connectionRetry;
	}

	public void setConnectionRetry(int connectionRetry)
	{
		this.connectionRetry = connectionRetry;
	}
}