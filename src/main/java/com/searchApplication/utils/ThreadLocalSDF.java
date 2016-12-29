package com.searchApplication.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadLocalSDF {

	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadLocalSDF.class);

	private static final ThreadLocal<SimpleDateFormat> dateFormatHolder = new ThreadLocal<SimpleDateFormat>() {
		private static final String DATE_FORMAT = "dd MMM, yyyy";

		/*
		 * initialValue() is called
		 */
		@Override
		protected SimpleDateFormat initialValue() {
			LOGGER.debug("Creating SimpleDateFormat for Thread : " + Thread.currentThread().getName());
			return new SimpleDateFormat(DATE_FORMAT);
		}
	};

	/*
	 * Every time there is a call for DateFormat, ThreadLocal will return
	 * calling Thread's copy of SimpleDateFormat
	 */
	public static DateFormat getDateFormatter() {
		return dateFormatHolder.get();
	}
}
