package com.elmakers.mine.bukkit.plugins.persistence.stores;

import java.io.File;
import java.util.Date;

import com.elmakers.mine.bukkit.plugins.persistence.core.DataType;

public class SqlLiteStore extends SqlStore
{
	@Override
	public String getDriverClassName() { return "org.sqlite.JDBC"; }

	@Override
	public String getDriverFileName() { return "sqlite"; }
	
	@Override
	public String getMasterTableName() { return "sqlite_master"; }
	
	@Override
	public String getConnectionString(String schema, String user, String password) 
	{ 
		File sqlLiteFile = new File(dataFolder, schema + ".db");
		return "jdbc:sqlite:" + sqlLiteFile.getAbsolutePath();
	}
	
	@Override
	public String getTypeName(DataType dataType)
	{
		switch (dataType)
		{
			case INTEGER:
				return "INTEGER";
			case BOOLEAN:
				return "INTEGER";
			case DATE:
				return "INTEGER";
			case DOUBLE:
				return "REAL";
			case STRING:
				return "TEXT";
		}
		return null;
	}
	
	@Override
	public Object getFieldValue(Object field, DataType dataType)
	{
		if (field == null) return "null";
		
		if (dataType == DataType.DATE)
		{
			Date d = (Date)field;
			Integer seconds = (int)(d.getTime() / 1000);
			return seconds;
		}
		if (dataType == DataType.BOOLEAN)
		{
			Boolean flag = (Boolean)field;
			Integer intValue = flag ? 1 : 0;
			return intValue;
		}
		return field;
	}
	
	public Object getDataValue(Object storedValue, DataType dataType)
	{
		if (storedValue == null) return null;
		
		if (dataType == DataType.DATE)
		{
			Integer i = (Integer)storedValue;
			Date d = new Date(i * 1000);
			return d;
		}
		if (dataType == DataType.BOOLEAN)
		{
			Integer i = (Integer)storedValue;
			Boolean b = i != 0;
			return b;
		}
		return storedValue;
	}

}
