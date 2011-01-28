package com.elmakers.mine.bukkit.plugins.persistence.stores;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.elmakers.mine.bukkit.plugins.persistence.PersistedClass;
import com.elmakers.mine.bukkit.plugins.persistence.PersistencePlugin;
import com.elmakers.mine.bukkit.plugins.persistence.PersistedField;

public abstract class SqlStore extends PersistenceStore
{
	public abstract String getDriverClassName();
	public abstract String getDriverFileName();
	public abstract String getMasterTableName();
	public abstract String getConnectionString(String schema, String user, String password);
	
	public boolean onConnect()
	{
		return true;
	}
	
	@Override
	public boolean connect(String schema)
	{
		try 
		{
			// Check to see if the driver is loaded
			String jdbcClass = getDriverClassName();
			try
			{
				Class.forName(jdbcClass);
			}
			catch (ClassNotFoundException e)
			{
				log.info("Persistence: Loading sqlite drivers from plugins folder");
				String fileName = getDriverFileName();
				
				File dataPath = dataFolder.getAbsoluteFile();
				File pluginsPath = new File(dataPath.getParent());
				File cbPath = new File(pluginsPath.getParent());
				File sqlLiteFile = new File(cbPath, fileName + ".jar");
	            if (!sqlLiteFile.exists()) 
	            {
	                log.severe("Persistence: Failed to find sql driver: " + fileName + ".jar");
	                return false;
	            }
	            
	            try 
	            {
	            	URL u = new URL("jar:file:" + sqlLiteFile.getAbsolutePath() + "!/");
	        		URLClassLoader ucl = new URLClassLoader(new URL[] { u });
	        		Driver d = (Driver)Class.forName(jdbcClass, true, ucl).newInstance();
	        		DriverManager.registerDriver(new DriverShim(d));
	            } 
	            catch (MalformedURLException ex) 
	            {
	                log.severe("Persistence: Exception while loading sql drivers");
	                ex.printStackTrace();
	                return false;
	            }
	            catch (IllegalAccessException ex) 
	            {
	                log.severe("Persistence: Exception while loading sql drivers");
	                ex.printStackTrace();
	                return false;
	            }
	            catch (InstantiationException ex) 
	            {
	                log.severe("Persistence: Exception while loading sql drivers");
	                ex.printStackTrace();
	                return false;
	            }
				catch (ClassNotFoundException e1)
				{
					log.severe("Persistence: JDBC class not found in sql jar");
				}
			}			
			// Create or connect to the database
			
			// TODO: user, password
			String user = "";
			String password = "";
					
			connection = DriverManager.getConnection(getConnectionString(schema, user, password));
		}
		catch(SQLException e)
		{
			connection = null;
			log.severe("Permissions: error connecting to sqllite db: " + e.getMessage());
		}
		
		return isConnected() && onConnect();
	}

	@Override
	public void disconnect()
	{
		if (connection != null)
		{
			try
			{
				connection.close();
			}
			catch (SQLException e)
			{
				
			}
		}
		connection = null;
	}

	@Override
	public void validateTable(PersistedClass persisted)
	{
		String tableName = persisted.getTableName();
		String checkQuery = "SELECT name FROM " + getMasterTableName() + " WHERE type='table' AND name='" + tableName + "'";
		boolean tableExists = false;
		try
		{
			PreparedStatement ps = connection.prepareStatement(checkQuery);
			ResultSet rs = ps.executeQuery();
			tableExists = !rs.isClosed() && rs.first();
		}
		catch (SQLException ex)
		{
		}
		if (!tableExists)
		{
			String createStatement = "CREATE TABLE " + tableName + "(";
			
			//TODO...
		}
	}

	@Override
	public boolean load(PersistedClass persisted)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean save(PersistedClass persisted)
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean isConnected()
	{
		boolean isClosed = true;
		try
		{
			isClosed = connection == null || connection.isClosed();
		}
		catch (SQLException e)
		{
			isClosed = true;
		}
		return (connection != null && !isClosed);
	}
	
	protected SqlType getSqlType(PersistedField field)
	{
		SqlType sqlType = SqlType.NULL;
		
		Class<?> fieldType = field.getType();
		if (fieldType.isAssignableFrom(Integer.class))
		{
			sqlType = SqlType.INTEGER;
		}
		else if (fieldType.isAssignableFrom(Double.class))
		{
			sqlType = SqlType.DOUBLE;
		}
		else if (fieldType.isAssignableFrom(Float.class))
		{
			sqlType = SqlType.DOUBLE;
		}
		else if (fieldType.isAssignableFrom(String.class))
		{
			sqlType = SqlType.STRING;
		}
		else
		{
			log.warning("Persistence: field: " + field.getType().getName() + " not a supported type. Object refences not supported, yet.");
			sqlType = SqlType.NULL;
		}
		
		return sqlType;
	}

	public void setDataFolder(File dataFolder)
	{
		this.dataFolder = dataFolder;
	}
	
	protected File dataFolder;
	protected Connection connection = null;
}
