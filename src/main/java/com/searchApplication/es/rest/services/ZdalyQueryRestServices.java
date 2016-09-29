package com.searchApplication.es.rest.services;

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
import com.searchApplication.es.entities.WildCardSearchResponseList;
import com.searchApplication.es.interfaces.ZdalyQueryServices;

@Path( "/zdaly" )
@RestController
public class ZdalyQueryRestServices {

	@Autowired
	private ZdalyQueryServices zdalyQueryServices;

	@GET
	@Produces( MediaType.APPLICATION_JSON )
	@Path( "/wilcard-search/{queryString}" )
	public TransactionResponse wildCardQuery(
			@NotNull( message = "Cannot be null" ) @PathParam( "queryString" ) String queryString ) throws Exception
	{

		TransactionResponse transactionResponse = new TransactionResponse();
		transactionResponse.setStatus("200");
		transactionResponse.setResponseMessage("Successfull");
		transactionResponse.setResponseType("Object");
		try
		{
			long startTime = System.currentTimeMillis();
			WildCardSearchResponseList results = zdalyQueryServices.wildcardQuery(queryString);

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
}
