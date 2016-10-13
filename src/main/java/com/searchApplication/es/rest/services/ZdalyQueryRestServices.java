package com.searchApplication.es.rest.services;

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
import com.searchApplication.entities.FilterRequest;
import com.searchApplication.entities.QueryResultsList;
import com.searchApplication.entities.SearchOutput;
import com.searchApplication.entities.TransactionResponse;
import com.searchApplication.es.entities.BucketResponseList;
import com.searchApplication.es.entities.PerformanceBucket;
import com.searchApplication.es.interfaces.ZdalyQueryServices;
import com.searchApplication.es.services.impl.PerformanceTest;

@Path( "/zdaly" )
@RestController
public class ZdalyQueryRestServices {

	@Autowired
	private ZdalyQueryServices zdalyQueryServices;

	@Autowired
	private PerformanceTest performanceTest;

	@GET
	@Produces( MediaType.APPLICATION_JSON )
	@Path( "/wilcard-search/{queryString}" )
	public TransactionResponse produceBucekts(
			@NotNull( message = "Cannot be null" ) @PathParam( "queryString" ) String queryString ) throws Exception
	{

		TransactionResponse transactionResponse = new TransactionResponse();
		transactionResponse.setStatus("200");
		transactionResponse.setResponseMessage("Successfull");
		transactionResponse.setResponseType("Object");
		try
		{
			long startTime = System.currentTimeMillis();
			BucketResponseList results = zdalyQueryServices.produceBuckets(queryString);

			if( results != null )
			{
				transactionResponse.setResponseEntity(results);
			}
			else
			{
				transactionResponse.setResponseMessage("No results found");
			}
			long endTime = System.currentTimeMillis();

			System.out.println("Service took - " + (endTime - startTime) + " milliseconds to execute");
		}
		catch( Exception e )
		{
			e.printStackTrace();
			throw e;
		}
		return transactionResponse;
	}

	@GET
	@Produces( MediaType.APPLICATION_JSON )
	@Path( "/match-query/{queryString}" )
	public TransactionResponse matchQuery(
			@NotNull( message = "Cannot be null" ) @PathParam( "queryString" ) String queryString ) throws Exception
	{

		TransactionResponse transactionResponse = new TransactionResponse();
		transactionResponse.setStatus("200");
		transactionResponse.setResponseMessage("Successfull");
		transactionResponse.setResponseType("Object");
		try
		{
			long startTime = System.currentTimeMillis();
			SearchOutput results = zdalyQueryServices.matchQuery(queryString);
			if( results != null )
			{
				transactionResponse.setResponseEntity(results);
			}
			else
			{
				transactionResponse.setResponseMessage("No results found");
			}
			long endTime = System.currentTimeMillis();

			System.out.println("Service took - " + (endTime - startTime) + " milliseconds to execute");
		}
		catch( Exception e )
		{
			e.printStackTrace();
			throw e;
		}
		return transactionResponse;
	}

	@POST
	@Consumes( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON } )
	@Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON } )
	@Path( "/query-with-filters" )
	public TransactionResponse queryWithFilters( FilterRequest filterRequest ) throws Exception
	{
		TransactionResponse transactionResponse = new TransactionResponse();
		transactionResponse.setStatus("200");
		transactionResponse.setResponseMessage("Successfull");
		transactionResponse.setResponseType("Object");
		try
		{
			long startTime = System.currentTimeMillis();
			SearchOutput results = zdalyQueryServices.queryWithFilters(filterRequest);
			if( results != null )
			{
				transactionResponse.setResponseEntity(results);
			}
			else
			{
				transactionResponse.setResponseMessage("No results found");
			}
			long endTime = System.currentTimeMillis();

			System.out.println("Service took - " + (endTime - startTime) + " milliseconds to execute");

		}
		catch( Exception e )
		{
			e.printStackTrace();
			throw e;
		}
		return transactionResponse;
	}

	@POST
	@Consumes( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON } )
	@Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON } )
	@Path( "/query-results" )
	public TransactionResponse queryResults( FilterRequest filterRequest ) throws Exception
	{
		TransactionResponse transactionResponse = new TransactionResponse();
		transactionResponse.setStatus("200");
		transactionResponse.setResponseMessage("Successfull");
		transactionResponse.setResponseType("Object");
		try
		{
			long startTime = System.currentTimeMillis();
			QueryResultsList results = zdalyQueryServices.queryResults(filterRequest);
			if( results != null )
			{
				transactionResponse.setResponseEntity(results);
			}
			else
			{
				transactionResponse.setResponseMessage("No results found");
			}
			long endTime = System.currentTimeMillis();

			System.out.println("Service took - " + (endTime - startTime) + " milliseconds to execute");

		}
		catch( Exception e )
		{
			e.printStackTrace();
			throw e;
		}
		return transactionResponse;
	}

	@GET
	@Produces( MediaType.APPLICATION_JSON )
	@Path( "/performance" )
	public TransactionResponse performanceCheck() throws Exception
	{

		TransactionResponse transactionResponse = new TransactionResponse();
		transactionResponse.setStatus("200");
		transactionResponse.setResponseMessage("Successfull");
		transactionResponse.setResponseType("Object");
		try
		{
			long startTime = System.currentTimeMillis();
			String[] queries = new String[] { "corn", "corn production", "corn production illinois", "apple",
					"apple production illinois", "apple production contract illinois", "Livestock and meat", "Vineries",
					"Crop statistics (from 2000 onwards)", "Crop statistics (1955 - 1999)",
					"Selling prices of sugar beet (unit value)", "Selling prices of soft wheat",
					"Selling prices of sheep", "Selling prices of raw cow's milk", "Selling prices of pigs (light)",
					"Selling prices of piglets", "Selling prices of oats", "Selling prices of maize",
					"Selling prices of main crop potatoes", "Selling prices of fresh eggs",
					"Selling prices of chickens (live 1st choice)", "Selling prices of calves",
					"Selling prices of barley",
					"Selling prices of crop products (absolute prices) - monthly - old codes - data from 1969 to 2006",
					"Selling prices of crop products (absolute prices) - annual price (from 2000 onwards)",
					"Selling prices of crop products (absolute prices) - annual - old codes - data from 1969 to 2005",
					"Selling prices of animal products (absolute prices) - monthly - old code - data from 1969 to 2006",
					"Selling prices of animal products (absolute prices) - annual price (from 2000 onwards)",
					"Selling prices of animal products (absolute prices) - annual - old codes - data from 1969 to 2005",
					"Purchase prices of the means of agricultural production (absolute prices) - monthly - old codes - data from 1969 to 2006",
					"Purchase prices of the means of agricultural production (absolute prices) - annual price (from 2000 onwards)",
					"Purchase prices of the means of agricultural production (absolute prices) - annual - old codes - data from 1969 to 2005",
					"Land prices and rents", "Purchase price indices, total means of agricultural production",
					"Producer price indices, total agricultural production" };
			List<PerformanceBucket> results = performanceTest.performanceCheck(queries, true);
			if( results != null && !results.isEmpty() )
			{
				transactionResponse.setResponseEntity(results);
			}
			else
			{
				transactionResponse.setResponseMessage("No results found");
			}
			long endTime = System.currentTimeMillis();

			System.out.println("Service took - " + (endTime - startTime) + " milliseconds to execute");
		}
		catch( Exception e )
		{
			e.printStackTrace();
			throw e;
		}
		return transactionResponse;
	}
}
