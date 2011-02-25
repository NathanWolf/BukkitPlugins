package com.elmakers.mine.craftbukkit.persistence.data.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.elmakers.mine.craftbukkit.persistence.data.DataRow;
import com.elmakers.mine.craftbukkit.persistence.data.DataTable;
import com.elmakers.mine.craftbukkit.persistence.data.DataType;

public class SqlDataRow extends DataRow
{
	/**
	 * Create a DataRow based on a SQL ResultSet
	 * 
	 * This constructor is used to create a DataRow for reading from a store.
	 * 
	 * @param row The SQL ResultSet to read from
	 */
	public SqlDataRow(DataTable table, ResultSet row)
	{
		super(table);
		
		try
		{
			ResultSetMetaData rowInfo = row.getMetaData();
			int columnCount = rowInfo.getColumnCount();
			for (int i = 1; i <= columnCount; i++)
			{
				int sqlType = rowInfo.getColumnType(i);
				String columnName = rowInfo.getColumnName(i);
				DataType dataType = DataType.getTypeFromSqlType(sqlType);
				SqlDataField field = new SqlDataField(row, i, columnName, dataType);
				add(field);
			}
		}
		catch (SQLException ex)
		{
			log.warning("Persistence: error creating SQLDataRow: " + ex.getMessage());
		}
		
	}
}
