package ru.simsonic.rscCommonsLibrary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionMySQL
{
	private static final Pattern PATTERN_CONNECTION = Pattern.compile("jdbc:mysql://(?:[\\w:\\-\\.]+)/([\\w\\-]+)");
	public  static class ConnectionParams
	{
		public String nodename;
		public String database;
		public String username;
		public String password;
		public String prefixes;
	}
	protected String RememberName;
	protected String RememberURL;
	protected String RememberUser;
	protected String RememberPass;
	private volatile Connection connection;
	private volatile Statement  statement;
	public ConnectionMySQL()
	{
	}
	public synchronized void initialize(ConnectionParams cp)
	{
		initialize(cp.nodename, cp.database, cp.username, cp.password, cp.prefixes);
	}
	public synchronized void initialize(String name, String database, String username, String password, String prefixes)
	{
		RememberName = (name != null) ? name : "unnamed";
		RememberURL  = "jdbc:mysql://" + database;
		RememberUser = username;
		RememberPass = password;
		Matcher match = PATTERN_CONNECTION.matcher(RememberURL);
		clearQueryTemplate();
		if(match.find())
			setupQueryTemplate("{DATABASE}", match.group(1));
		setupQueryTemplate("{PREFIX}", prefixes);
	}
	protected String loadResourceSQLT(String name) throws SQLException
	{
		final StringBuilder result = new StringBuilder();
		try
		{
			final InputStream stream = this.getClass().getClassLoader().getResourceAsStream("sqlt/" + name + ".sqlt");
			final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF8"));
			for(String line = reader.readLine(); line != null; line = reader.readLine())
				result.append(line).append("\n");
		} catch(IOException | NullPointerException ex) {
			throw new SQLException(ex);
		}
		return result.toString();
	}
	private final HashMap<String, String> replaceInQueries = new HashMap<>();
	public synchronized void setupQueryTemplate(String from, String to)
	{
		if(to == null)
			to = "";
		if(from != null)
			if(!"".equals(from))
				replaceInQueries.put(from, to);
	}
	public synchronized void clearQueryTemplate()
	{
		replaceInQueries.clear();
	}
	private String queryExplicitation(String query)
	{
		for(Entry<String, String> pairs : replaceInQueries.entrySet())
			query = query.replace(pairs.getKey(), pairs.getValue());
		return query;
	}
	public synchronized boolean isConnected() throws SQLException
	{
		try
		{
			if(connection != null)
				if(!connection.isValid(0))
					disconnect();
			if(connection == null)
				return connect();
			if(connection != null)
				return connection.isValid(0);
		} catch(SQLException ex) {
			throw ex;
		}
		return false;
	}
	public synchronized boolean connect() throws SQLException
	{
		if(RememberURL == null || RememberUser == null || RememberPass == null)
			return false;
		if("".equals(RememberURL) || "".equals(RememberUser) || "".equals(RememberPass))
			return false;
		try
		{ 
			Class.forName("com.mysql.jdbc.Driver");
			String FixedURL = RememberURL + "?allowMultiQueries=true&autoReConnect=true";
			connection = DriverManager.getConnection(FixedURL, RememberUser, RememberPass);
			statement  = connection.createStatement();
			return true;
		} catch(SQLException ex) {
			disconnect();
			throw ex;
		} catch(ClassNotFoundException | NullPointerException ex) {
			disconnect();
			throw new SQLException(ex);
		}
	}
	public synchronized void disconnect()
	{
		try
		{
			if(statement != null)
				statement.close();
		} catch(SQLException ex) {
		} finally {
			statement = null;
		}
		try
		{
			if(connection != null)
				connection.close();
		} catch(SQLException ex) {
		} finally {
			connection = null;
		}
	}
	public synchronized ResultSet executeQueryT(String templateResource) throws SQLException
	{
		return executeQuery(loadResourceSQLT(templateResource));
	}
	public synchronized boolean executeUpdateT(String templateResource) throws SQLException
	{
		return executeUpdate(loadResourceSQLT(templateResource));
	}
	public synchronized ResultSet executeQuery(String query) throws SQLException
	{
		final String threadName = Thread.currentThread().getName();
		Thread.currentThread().setName("rscAPI:SQL-read (" + threadName + ")");
		try
		{
			if(isConnected())
			{
				final ResultSet result = statement.executeQuery(queryExplicitation(query));
				return result;
			}
		} catch(SQLException ex) {
			throw ex;
		} finally {
			Thread.currentThread().setName(threadName);
		}
		return null;
	}
	public synchronized boolean executeUpdate(String query) throws SQLException
	{
		final String threadName = Thread.currentThread().getName();
		Thread.currentThread().setName("rscAPI:SQL-write (" + threadName + ")");
		try
		{
			if(isConnected())
			{
				statement.execute(queryExplicitation(query));
				return true;
			}
		} catch(SQLException ex) {
			throw ex;
		} finally {
			Thread.currentThread().setName(threadName);
		}
		return false;
	}
}
