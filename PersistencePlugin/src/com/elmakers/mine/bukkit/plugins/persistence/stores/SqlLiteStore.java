package com.elmakers.mine.bukkit.plugins.persistence.stores;

import java.io.File;
import java.util.Date;

public class SqlLiteStore extends SqlStore
{
	@Override
	public String getDriverClassName() { return "org.sqlite.JDBC"; }

	@Override
	public String getDriverFileName() { return "sqlitejdbc"; }
	
	@Override
	public String getMasterTableName() { return "sqlite_master"; }
	
	@Override
	public String getConnectionString(String schema, String user, String password) 
	{ 
		File sqlLiteFile = new File(dataFolder, schema + ".db");
		return "jdbc:sqlite:" + sqlLiteFile.getAbsolutePath();
	}
	
	@Override
	public String getTypeName(SqlType dataType)
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
	public Object getFieldValue(Object field, SqlType dataType)
	{
		if (field == null) return "null";
		
		if (dataType == SqlType.DATE)
		{
			Date d = (Date)field;
			Integer seconds = (int)(d.getTime() / 1000);
			return seconds;
		}
		if (dataType == SqlType.BOOLEAN)
		{
			Boolean flag = (Boolean)field;
			Integer intValue = flag ? 1 : 0;
			return intValue;
		}
		return field;
	}
	
	public Object getDataValue(Object storedValue, SqlType dataType)
	{
		if (storedValue == null) return null;
		
		if (dataType == SqlType.DATE)
		{
			Integer i = (Integer)storedValue;
			Date d = new Date(i * 1000);
			return d;
		}
		if (dataType == SqlType.BOOLEAN)
		{
			Integer i = (Integer)storedValue;
			Boolean b = i != 0;
			return b;
		}
		return storedValue;
	}

}
