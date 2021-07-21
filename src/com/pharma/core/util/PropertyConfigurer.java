package com.pharma.core.util;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
public class PropertyConfigurer {
	
	
	public static String getPropertyKeyValue(String propFileName ,String propKey) {
		String result = "";
		InputStream inputStream = null;
		try {
			Properties prop = new Properties();
		
			inputStream = PropertyConfigurer.class.getClassLoader().getResourceAsStream("resources/"+propFileName);
 
			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
 
			/**
 			
 			// get the property value and print it out
			String DB_DRIVER = prop.getProperty("DB_DRIVER");
			String DB_CONNECTION = prop.getProperty("DB_CONNECTION");
			String DB_USER = prop.getProperty("DB_USER");
			String DB_PASSWORD = prop.getProperty("DB_PASSWORD");
 
			result = "List = " + DB_DRIVER + ", " + DB_CONNECTION + ", " + DB_USER+ ", " + DB_PASSWORD;
		
			*/
			
			result=prop.getProperty(propKey);
			System.out.println("result ==="+result);
			
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}
	public static String getPropertyKeyValue(String propKey) {
		String result = "";
		InputStream inputStream = null;
		try {
			Properties prop = new Properties();
			String propFileName = "config.properties";
 
			inputStream = PropertyConfigurer.class.getClassLoader().getResourceAsStream("resources/"+propFileName);
 
			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
 
			/**
 			
 			// get the property value and print it out
			String DB_DRIVER = prop.getProperty("DB_DRIVER");
			String DB_CONNECTION = prop.getProperty("DB_CONNECTION");
			String DB_USER = prop.getProperty("DB_USER");
			String DB_PASSWORD = prop.getProperty("DB_PASSWORD");
 
			result = "List = " + DB_DRIVER + ", " + DB_CONNECTION + ", " + DB_USER+ ", " + DB_PASSWORD;
		
			*/
			
			result=prop.getProperty(propKey);
			System.out.println("result ==="+result);
			
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}
	public static void main(String[] args) throws IOException {
		PropertyConfigurer.getPropertyKeyValue("DB_DRIVER");
	}

}
