package com.searchApplication.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LocationLoader {

	public static Set<String> getLocationsFromFile(String path) throws IOException {
		
		ClassLoader classLoader = LocationLoader.class.getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream("locations.txt");
//		System.out.println("Loaded locations file " + file);
		return new HashSet<String>(Arrays.asList(IOUtils.textLinesAsArray(inputStream)));
	}

	
}
