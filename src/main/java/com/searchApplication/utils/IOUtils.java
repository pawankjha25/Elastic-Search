package com.searchApplication.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class IOUtils {


	public static String textLines(String path) throws IOException {
		StringBuffer sb = new StringBuffer();
		try (BufferedReader br = new BufferedReader(new FileReader(path));) {
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(" "+line);
			}
			return sb.toString();
		}
		
	}
	
	
	public static String[] textLinesAsArray(String path) throws IOException {
		List<String> sb = new LinkedList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(path));) {
			String line;
			while ((line = br.readLine()) != null) {
				sb.add(line);
			}
			return sb.toArray(new String[sb.size()]);
		}
		
	}
}
