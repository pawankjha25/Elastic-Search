package com.searchApplication.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
	
	public static String[] textLinesAsArray(File file) throws IOException {
		List<String> sb = new LinkedList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(file));) {
			String line;
			while ((line = br.readLine()) != null) {
				sb.add(line);
			}
			return sb.toArray(new String[sb.size()]);
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
	
	public static String[] textLinesAsArray(InputStream inputStream) throws IOException {
		List<String> sb = new LinkedList<String>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
			String line;
			while ((line = br.readLine()) != null) {
				sb.add(line);
			}
			return sb.toArray(new String[sb.size()]);
		}
		
	}
}
