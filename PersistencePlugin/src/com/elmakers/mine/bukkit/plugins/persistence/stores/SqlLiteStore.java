package com.elmakers.mine.bukkit.plugins.persistence.stores;

import java.io.File;

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
			case DOUBLE:
				return "REAL";
			case STRING:
				return "TEXT";
			case DATE:
				return "INTEGER";
		}
		return null;
	}
	
	@Override
	public String getFieldValue(Object field, SqlType dataType)
	{
		if (dataType == SqlType.STRING)
		{
			return "'" + field.toString() + "'";
		}
		return field.toString();
	}

}
