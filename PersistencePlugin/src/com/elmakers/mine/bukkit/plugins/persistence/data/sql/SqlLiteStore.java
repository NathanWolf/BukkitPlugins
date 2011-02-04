package com.elmakers.mine.bukkit.plugins.persistence.data.sql;

import java.io.File;

import com.elmakers.mine.bukkit.plugins.persistence.data.DataType;

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
	
}
