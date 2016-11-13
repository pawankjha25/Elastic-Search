package com.searchApplication.utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LocationLoader {

	public static Set<String> getLocationsFromFile(String path) throws IOException {
		
		ClassLoader classLoader = LocationLoader.class.getClassLoader();
		File file = new File(classLoader.getResource(path).getFile());
		return new HashSet<String>(Arrays.asList(IOUtils.textLinesAsArray(file)));
	}

	
}
