package com.searchApplication;

import java.util.Set;

import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.searchApplication.utils.ElasticSearchUtility;
import com.searchApplication.utils.LocationLoader;

@Configuration
@EnableAutoConfiguration
@ComponentScan("com.searchApplication.*")
@EnableScheduling
public class App
{

	@Autowired
	private Environment env;
	public static Set<String> LOCATIONS;

	public static void main(String[] args)
	{
		SpringApplication.run(App.class, args);
	}

	@Bean
	public ServletRegistrationBean jerseyServlet()
	{
		ServletRegistrationBean registration = new ServletRegistrationBean(new ServletContainer(), "/rest/*");
		registration.addInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS, JerseyConfig.class.getName());
		ElasticSearchUtility.getInstance(env);
		try
		{
			LOCATIONS = LocationLoader.getLocationsFromFile("src/main/resources/locations");
		} catch (Exception e)
		{
			System.out.println("failed to load locations");
		}
		return registration;
	}
}