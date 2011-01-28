package com.elmakers.mine.bukkit.plugins.persistence.stores;

import java.io.File;
import java.sql.Connection;
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
			Class.forName("org.sqlite.JDBC");
			
			// Create or connect to the database
			File sqlLiteFile = new File(dataFolder, schema + ".db");
			
			connection = DriverManager.getConnection("jdbc:sqlite:" + sqlLiteFile.getAbsolutePath());
		}
		catch(SQLException e)
		{
			connection = null;
			log.severe("Permissions: error connecting to sqllite db: " + e.getMessage());
		}
		catch (ClassNotFoundException e)
		{
			log.severe("Permissions: Can't find sqllite drivers");
			connection = null;
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
