package com.searchApplication.entities;

public class TransactionResponse {

	private String status;
	private String responseMessage;
	private String responseType; //ENTITY OR LIST
	private Object responseEntity;

	public TransactionResponse( String statusCode, String responseMessage, String responseType, Object responseEntity )
	{
		super();
		this.status = statusCode;
		this.responseMessage = responseMessage;
		this.responseType = responseType;
		this.responseEntity = responseEntity;
	}

	public TransactionResponse()
	{
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus( String statusCode )
	{
		this.status = statusCode;
	}

	public String getResponseMessage()
	{
		return responseMessage;
	}

	public void setResponseMessage( String responseMessage )
	{
		this.responseMessage = responseMessage;
	}

	public String getResponseType()
	{
		return responseType;
	}

	public void setResponseType( String responseType )
	{
		this.responseType = responseType;
	}

	public Object getResponseEntity()
	{
		return responseEntity;
	}

	public void setResponseEntity( Object responseEntity )
	{
		this.responseEntity = responseEntity;
	}
}
