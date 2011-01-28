package com.elmakers.mine.bukkit.plugins.persistence.stores;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.elmakers.mine.bukkit.plugins.persistence.PersistedClass;

public class SqlLiteStore extends PersistenceStore
{
	Connection connection = null;

	@Override
	public boolean connect(String schema)
	{
		try 
		{
			// Check to see if the driver is loaded
			String jdbcClass = "org.sqlite.JDBC";
			try
			{
				Class.forName(jdbcClass);
			}
			catch (ClassNotFoundException e)
			{
				log.info("Persistence: Loading sqlite drivers from plugins folder");
				String fileName = "sqlitejdbc";
				
				File dataPath = dataFolder.getAbsoluteFile();
				File pluginsPath = new File(dataPath.getParent());
				File cbPath = new File(pluginsPath.getParent());
				File sqlLiteFile = new File(cbPath, fileName + ".jar");
	            if (!sqlLiteFile.exists()) 
	            {
	                log.severe("Persistence: Failed to find sqllite driver: " + fileName + ".jar");
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
	                log.severe("Persistence: Exception while loading sqllite drivers");
	                ex.printStackTrace();
	                return false;
	            }
	            catch (IllegalAccessException ex) 
	            {
	                log.severe("Persistence: Exception while loading sqllite drivers");
	                ex.printStackTrace();
	                return false;
	            }
	            catch (InstantiationException ex) 
	            {
	                log.severe("Persistence: Exception while loading sqllite drivers");
	                ex.printStackTrace();
	                return false;
	            }
				catch (ClassNotFoundException e1)
				{
					log.severe("Persistence: JDBC class not found in sqllite jar");
				}
			}			
			// Create or connect to the database
			File sqlLiteFile = new File(dataFolder, schema + ".db");
			
			connection = DriverManager.getConnection("jdbc:sqlite:" + sqlLiteFile.getAbsolutePath());
		}
		catch(SQLException e)
		{
			connection = null;
			log.severe("Permissions: error connecting to sqllite db: " + e.getMessage());
		}
		
		return isConnected();
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
	
	public void setDataFolder(File dataFolder)
	{
		this.dataFolder = dataFolder;
	}
	
	private File dataFolder;
	
}
