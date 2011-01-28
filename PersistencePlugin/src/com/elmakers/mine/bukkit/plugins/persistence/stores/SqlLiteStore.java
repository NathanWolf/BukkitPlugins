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

}
