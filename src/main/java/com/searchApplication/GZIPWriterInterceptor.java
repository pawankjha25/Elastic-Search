package com.searchApplication;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.searchApplication.es.rest.services.ZdalyQueryRestServices;

@Provider
@Compress
public class GZIPWriterInterceptor implements WriterInterceptor {

	private static final Logger LOG = LoggerFactory.getLogger(ZdalyQueryRestServices.class);

	@Override
	public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {

		MultivaluedMap<String, Object> headers = context.getHeaders();
		headers.add("Content-Encoding", "gzip");

		LOG.info("Compressing the data");

		final OutputStream outputStream = context.getOutputStream();
		context.setOutputStream(new GZIPOutputStream(outputStream));
		context.proceed();
	}
}