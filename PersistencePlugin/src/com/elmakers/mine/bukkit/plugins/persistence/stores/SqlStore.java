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
import java.util.ArrayList;
import java.util.List;

import com.elmakers.mine.bukkit.plugins.persistence.DataType;
import com.elmakers.mine.bukkit.plugins.persistence.PersistedClass;
import com.elmakers.mine.bukkit.plugins.persistence.PersistedField;
import com.elmakers.mine.bukkit.plugins.persistence.PersistedList;
import com.elmakers.mine.bukkit.plugins.persistence.PersistedReference;
import com.elmakers.mine.bukkit.plugins.persistence.PersistencePlugin;

public abstract class SqlStore extends PersistenceStore
{
	public abstract String getDriverClassName();
	public abstract String getDriverFileName();
	public abstract String getMasterTableName();
	public abstract String getConnectionString(String schema, String user, String password);
	public abstract String getTypeName(DataType dataType);
	public abstract Object getFieldValue(Object field, DataType dataType);
	public abstract Object getDataValue(Object storedValue, DataType dataType);
	
	public boolean onConnect()
	{
		return true;
	}
	
	@Override
	public boolean connect(String schema)
	{
		this.schema = schema;
		
		// Try to load drivers if necessary
		if (!driversLoaded)
		{
			// Check to see if the driver is loaded
			String jdbcClass = getDriverClassName();
			try
			{
				Class.forName(jdbcClass);
				driversLoaded = true;
			}
			catch (ClassNotFoundException e)
			{
				driversLoaded = false;
			}
			if (!driversLoaded)
			{
				log.info("Persistence: Loading sqlite drivers from CraftBukkit folder");
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
	            	ClassLoader parentLoader = PersistencePlugin.class.getClassLoader();
	        		URLClassLoader ucl = new URLClassLoader(new URL[] { u }, parentLoader);
	        		Driver d = (Driver)Class.forName(jdbcClass, true, ucl).newInstance();
	        		DriverManager.registerDriver(new PersistenceJDBCDriver(d));
	        		driversLoaded = true;
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
				catch(SQLException e)
				{
					connection = null;
					log.severe("Permissions: SQL errors loading sqllite drivers: " + e.getMessage());
				}
			}
		}
		// Create or connect to the database
		
		// TODO: user, password
		String user = "";
		String password = "";
				
		try
		{
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
	public void reset(PersistedClass persisted)
	{
		String tableName = persisted.getTableName();
		if (tableExists(tableName))
		{
			String dropQuery = "DROP TABLE \"" + tableName + "\"";
			try
			{
				PreparedStatement ps = connection.prepareStatement(dropQuery);
				ps.execute();
			}
			catch (SQLException ex)
			{
				ex.printStackTrace();
				return;
			}
			log.info("Dropped table " + schema + "." + tableName);
		}
		return;
	}

	public boolean tableExists(String tableName)
	{
		String checkQuery = "SELECT name FROM \"" + getMasterTableName() + "\" WHERE type='table' AND name='" + tableName + "'";
		boolean tableExists = false;
		try
		{
			PreparedStatement ps = connection.prepareStatement(checkQuery);
			ResultSet rs = ps.executeQuery();
			tableExists = rs.next();
			rs.close();
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
			return false;
		}
		return tableExists;
	}
	
	@Override
	public void validateTables(PersistedClass persisted)
	{
		String tableName = persisted.getTableName();
		if (!tableExists(tableName))
		{
			List<PersistedField> fields = persisted.getPersistedFields();
			String createStatement = "CREATE TABLE \"" + tableName + "\" (";
			int fieldCount = 0;
			for (PersistedField field : fields)
			{
				if (field instanceof PersistedList)
				{
					continue;
				}
				DataType fieldType = field.getColumnType();
				String fieldName = field.getColumnName();
				if (fieldCount != 0)
				{
					createStatement += ",";
				}
				fieldCount++;
				createStatement += "\"" + fieldName + "\" " + getTypeName(fieldType);
				if (field.isIdField())
				{
					createStatement += " PRIMARY KEY";
				}
			}
			createStatement += ")";
			
			if (fieldCount == 0)
			{
				log.warning("Persistence: class " + tableName + " has no fields");
				return;
			}
			
			log.info("Persistence: Created table " + schema + "." + tableName);
			try
			{
				PreparedStatement ps = connection.prepareStatement(createStatement);
				ps.execute();
			}
			catch (SQLException ex)
			{
				ex.printStackTrace();
			}
		}
		else
		{
			// TODO: validate schema, add columns if necessary
		}
		
		for (PersistedField field : persisted.getPersistedFields())
		{
			if (!(field instanceof PersistedList)) continue;
			
			PersistedList listField = (PersistedList)field;
			DataType idFieldType = persisted.getIdField().getColumnType();
			
			String valueField = listField.getDataColumnName();
			String joinTableName = listField.getTableName(persisted);
			
			PersistedField idField = persisted.getIdField();
			String idFieldName = listField.getIdColumnName(persisted, idField);
			String joinFieldList = "\"" + idFieldName + "\", \"" + valueField + "\"";
			
			DataType contentsDataType = listField.getListColumnType();
			
			// Check for list sub-tables
			// Validate table schema for join table
			if (!tableExists(joinTableName))
			{	
				String createSql = "CREATE TABLE \"" + joinTableName + "\" (\""
					+	idFieldName + "\" " + getTypeName(idFieldType) + ", \"" + valueField + "\" " + getTypeName(contentsDataType)
					+ ", PRIMARY KEY (" + joinFieldList + "))";
				
				try
				{
					PreparedStatement ps = connection.prepareStatement(createSql);
					ps.execute();
				}
				catch (SQLException ex)
				{
					ex.printStackTrace();
					log.info(createSql);
				}
			}
		}
		
	}
	
	@Override
	public boolean save(PersistedClass persisted, Object o)
	{
		String tableName = persisted.getTableName();
		String fieldList = "";
		String valueList = "";
		int fieldCount = 0;
		List<PersistedField> fields = persisted.getPersistedFields();
		for (PersistedField field : fields)
		{
			if (field instanceof PersistedList)
			{
				continue;
			}
			if (fieldCount != 0)
			{
				fieldList += ", ";
				valueList += ", ";
			}
			fieldCount++;
			fieldList += "\"" + field.getColumnName() + "\"";
			valueList += "?";
		}
		if (fieldCount == 0)
		{
			log.warning("Persistence: class " + tableName + " has no fields");
			return false;
		}
		
		String updateSql = "INSERT OR REPLACE INTO \"" + tableName + "\" (" + fieldList + ") VALUES (" + valueList + ")";
		try
		{
			PreparedStatement updateStatement = connection.prepareStatement(updateSql);
			
			int index = 1;
			for (PersistedField field : fields)
            {
				if (field instanceof PersistedList)
				{
					continue;
				}
				Object value = field.getColumnData(o);
				
				if (value != null)
				{
					updateStatement.setObject(index, getFieldValue(value, field.getColumnType()));
				}
				else
				{
					updateStatement.setNull(index, java.sql.Types.NULL);
				}
				index++;
            }
			
			updateStatement.execute();
		}
		catch (SQLException ex)
		{
			log.warning("Persistence: Error updating table " + tableName + ": " + ex.getMessage());
			log.info(updateSql);
			return false;
		}	
		
		// Update list data
		for (PersistedField field : fields)
        {
			if (!(field instanceof PersistedList))
			{
				continue;
			}
			PersistedList listField = (PersistedList)field;
			DataType contentsDataType = listField.getListColumnType();
			
			if (contentsDataType == DataType.NULL)
			{
				log.warning("Error persisting list " + listField.getName() + " in " + persisted.getTableName());
				continue;
			}
			
			if (contentsDataType == DataType.LIST)
			{
				log.warning("Lists of lists not supported:" + listField.getName() + " in " + persisted.getTableName());
				continue;
			}

			// Construct table and column names
			String valueField = listField.getDataColumnName();
			String joinTableName = listField.getTableName(persisted);
			
			PersistedField idField = persisted.getIdField();
			String idFieldName = listField.getIdColumnName(persisted, idField);
			String joinFieldList = "\"" + idFieldName + "\", \"" + valueField + "\"";
			
			List<Object> objectValues = listField.getListData(o);
			if (objectValues.size() > 0)
			{
				String listDelete = "DELETE FROM \"" + joinTableName + "\" WHERE \"" + idFieldName + "\" = ? "
					+ " AND \"" + valueField + "\" NOT IN (";
				
				try
				{
					boolean firstItem = true;
					for (@SuppressWarnings("unused") Object listItem : objectValues)
					{
						if (!firstItem)
						{
							listDelete += ", ";
						}
						firstItem = false;
						listDelete += "?";
					}
					listDelete += ")";
					
					PreparedStatement deleteStatement = connection.prepareStatement(listDelete);
					deleteStatement.setObject(1, getFieldValue(idField.getColumnData(o), idField.getColumnType()));
					int index = 2;
					for (Object listItem : objectValues)
					{
						if (listItem != null)
						{
							deleteStatement.setObject(index, getFieldValue(listItem, contentsDataType));
						}
						else
						{
							deleteStatement.setNull(index, java.sql.Types.NULL);
						}
						index++;
					}
					
					deleteStatement.execute();
					
				}
				catch (SQLException ex)
				{
					log.warning("Persistence: Error deleting from table " + tableName + ": " + ex.getMessage());
					log.info(listDelete);
					return false;
				}	
				
				// Save list data
				for (Object listItem : objectValues)
				{
					String itemUpdateSql = "INSERT OR REPLACE INTO \"" + joinTableName + "\" (" + joinFieldList + ") VALUES (?, ?)";
					
					try
					{
						PreparedStatement listUpdateStatement = connection.prepareStatement(itemUpdateSql);
						listUpdateStatement.setObject(1, getFieldValue(idField.getColumnData(o), idField.getColumnType()));
						listUpdateStatement.setObject(2, getFieldValue(listItem, contentsDataType));
						listUpdateStatement.execute();
					}
					catch (SQLException ex)
					{
						log.warning("Persistence: Error updating list " + joinTableName + ": " + ex.getMessage());
						log.info(itemUpdateSql);
						return false;
					}	
				}
			}
			else
			{
				String listDelete = "DELETE FROM \"" + joinTableName + "\"";
				
				try
				{
					PreparedStatement deleteStatement = connection.prepareStatement(listDelete);
					deleteStatement.execute();
				}
				catch (SQLException ex)
				{
					log.warning("Persistence: Error deleting list " + joinTableName + ": " + ex.getMessage());
					log.info(listDelete);
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public boolean loadAll(PersistedClass persisted)
	{
		String tableName = persisted.getTableName();
		String selectQuery = "SELECT ";
		int fieldCount = 0;
		int listCount = 0;
		List<PersistedField> fields = persisted.getPersistedFields();
		List<PersistedList> listFields = new ArrayList<PersistedList>();
		
		for (PersistedField field : fields)
		{
			if (field instanceof PersistedList)
			{
				listFields.add((PersistedList)field);
				listCount++;
				continue;
			}
			if (fieldCount != 0)
			{
				selectQuery += ", ";
			}
			fieldCount++;
			selectQuery += "\"" + field.getColumnName() + "\"";
		}
		
		if (fieldCount == 0 && listCount == 0)
		{
			log.warning("Persistence: class " + tableName + " has no fields");
			return false;
		}
		selectQuery +=  " FROM \"" + tableName + "\";";
		
		try
		{
			// Begin deferred referencing, to prevent the problem of DAO's referencing unloaded DAOs.
			// DAOs will be loaded recursively as needed,
			// and then all deferred references will be resolved afterward.
			PersistedReference.beginDefer();
				
			List<Object> loadedObjects = new ArrayList<Object>();
			PreparedStatement ps = connection.prepareStatement(selectQuery);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				Object newObject = createInstance(rs, persisted);
				if (newObject != null)
				{
					persisted.put(newObject);
					loadedObjects.add(newObject);
				}
			}
			rs.close();
			
			// Bind deferred references, to handle DAOs referencing other DAOs, even of the
			// Same type. 
			// DAOs will be loaded recursively as needed, and then references bound when everything has been
			// resolved.
			PersistedReference.endDefer();
			
			// Populate lists
			// Defer load lists of entities
			PersistedList.beginDefer();
			for (Object o : loadedObjects)
			{
				for (PersistedList listField : listFields)
				{
					DataType contentsDataType = listField.getListColumnType();
					
					if (contentsDataType == DataType.NULL)
					{
						log.warning("Error loading list " + listField.getName() + " in " + persisted.getTableName());
						continue;
					}
					
					if (contentsDataType == DataType.LIST)
					{
						log.warning("Lists of lists not supported:" + listField.getName() + " in " + persisted.getTableName());
						continue;
					}
									
					// Construct table and column names
					String valueField = listField.getDataColumnName();
					String joinTableName = listField.getTableName(persisted);
					
					PersistedField idField = persisted.getIdField();
					String idFieldName = listField.getIdColumnName(persisted, idField);
					
					// Fetch list data
					String listSelectSql = "SELECT \"" + valueField + "\" FROM \"" + joinTableName + "\" WHERE \"" + idFieldName + "\" = ?";
					
					PreparedStatement listSelect = connection.prepareStatement(listSelectSql);
					listSelect.setObject(1, getFieldValue(idField.getColumnData(o), idField.getColumnType()));
					
					rs = listSelect.executeQuery();
					List<Object> objectList = new ArrayList<Object>();
					while (rs.next())
					{
						Object value = rs.getObject(valueField);
						objectList.add(value);
					}
					rs.close();
					
					listField.setList(o, objectList);
				}
			}
			
			// Done loading lists, finally defer-load any entities from lists
			PersistedList.endDefer();
		}
		catch (SQLException ex)
		{
			log.warning("Persistence: Error selecting from table " + tableName + ": " + ex.getMessage());
			return false;
		}
		
		return true;
	}
	
	protected Object createInstance(ResultSet rs, PersistedClass persisted)
	{
		Object newObject = null;
		
		try
		{
			newObject = persisted.getPersistClass().newInstance();
			List<PersistedField> fields = persisted.getPersistedFields();
	        for (PersistedField field : fields)
	        {
	        	// Lists get populated in a second step
	        	if (field instanceof PersistedList) continue;
	        	
        		Object value = rs.getObject(field.getColumnName());
        		DataType sqlType = field.getColumnType();
        		field.setColumnData(newObject, getDataValue(value, sqlType));
	        }
		}
		catch (Exception e)
		{
			newObject = null;
			log.warning("Persistence error getting fields for " + persisted.getTableName() + ": " + e.getMessage());
		}

		return newObject;
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
	
	protected File dataFolder = null;
	protected Connection connection = null;
	protected String schema = null;
	protected static boolean driversLoaded = false;
}
