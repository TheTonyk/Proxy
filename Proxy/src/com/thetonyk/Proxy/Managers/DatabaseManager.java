package com.thetonyk.Proxy.Managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbcp2.BasicDataSource;

import com.thetonyk.Proxy.Main;

public class DatabaseManager {
	
	private static BasicDataSource data;
	
	private static String HOST = Main.getConfiguration().getString("SQLHost", "localhost");
	private static String DB = Main.getConfiguration().getString("SQLDatabase", Main.name.toLowerCase().replaceAll(" ", ""));
	private static String USER = Main.getConfiguration().getString("SQLUser", "root");
	private static String PASS = Main.getConfiguration().getString("SQLPass", "");
	
	private static BasicDataSource createConnection() {
		
		BasicDataSource newData = new BasicDataSource();
		newData.setUrl("jdbc:mysql://" + HOST + "/" + DB);
		newData.setUsername(USER);
		newData.setPassword(PASS);
		newData.setMaxIdle(20);
		newData.setMinIdle(1);
		newData.setMaxOpenPreparedStatements(200);
		
		return newData;
		
	}
	
	public static Connection getConnection() throws SQLException {
		
		if (data == null) data = createConnection();
		
		return data.getConnection();
		
	}
	
	public static void close() throws SQLException {
		
		data.close();
		data = null;
		
	}
	
	public synchronized static void updateQuery (String query) throws SQLException {
		
		try (Connection connection = getConnection(); 
		PreparedStatement prepared = connection.prepareStatement(query)) {
			
			prepared.executeUpdate();
			
		}
		
	}
	
	public static boolean exist(String query) throws SQLException {
		
		try (Connection connection = getConnection();
		Statement statement = connection.createStatement();
		ResultSet exist = statement.executeQuery(query)) {
			
			return exist.next();
			
		}
		
	}
	
	public static boolean exist(String table, String column, String value1, int value2) throws SQLException {
		
		return exist("SELECT " + column + " FROM " + table + " WHERE " + column + " = '" + (value1 != null ? value1 : value2) + "';");
		
	}

}
