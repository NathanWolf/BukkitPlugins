package com.elmakers.mine.bukkit.persistence.exception;

import com.elmakers.mine.craftbukkit.persistence.core.PersistedClass;
import com.elmakers.mine.craftbukkit.persistence.data.DataField;
import com.elmakers.mine.craftbukkit.persistence.data.DataRow;
import com.elmakers.mine.craftbukkit.persistence.data.DataTable;

public class InvalidDataException extends Exception
{
	public DataField getDataField()
	{
		return dataField;
	}

	public DataTable getDataTable()
	{
		return dataTable;
	}

	public DataRow getDataRow()
	{
		return dataRow;
	}

	public InvalidDataException(DataTable dataTable)
	{
		this.dataTable = dataTable;
	}

	public InvalidDataException(DataTable dataTable, String message)
	{
		super(message);
		this.dataTable = dataTable;
	}

	public InvalidDataException(DataTable dataTable, DataRow dataRow)
	{
		this.dataTable = dataTable;
		this.dataRow = dataRow;
	}

	public InvalidDataException(DataTable dataTable, DataRow dataRow, String message)
	{
		super(message);
		this.dataTable = dataTable;
		this.dataRow = dataRow;
	}

	public InvalidDataException(DataTable dataTable, DataRow dataRow, DataField dataField)
	{
		this.dataTable = dataTable;
		this.dataRow = dataRow;
		this.dataField = dataField;
	}

	public InvalidDataException(DataTable dataTable, DataRow dataRow, DataField dataField, String message)
	{
		super(message);
		this.dataTable = dataTable;
		this.dataRow = dataRow;
		this.dataField = dataField;
	}
	
	public InvalidDataException(PersistedClass persistedClass, Throwable cause)
	{
		super(cause);
		this.persistedClass = persistedClass;
	}
	
	public InvalidDataException(PersistedClass persistedClass, String message)
	{
		super(message);
		this.persistedClass = persistedClass;
	}
	
	public InvalidDataException(PersistedClass persistedClass, DataTable dataTable, DataRow dataRow, Throwable cause)
	{
		super(cause);
		this.persistedClass = persistedClass;
		this.dataTable = dataTable;
		this.dataRow = dataRow;
	}

	public InvalidDataException(DataTable dataTable, DataRow dataRow, Throwable cause)
	{
		super(cause);
		this.dataTable = dataTable;
		this.dataRow = dataRow;
	}

	PersistedClass				persistedClass		= null;
	DataField					dataField			= null;
	DataTable					dataTable			= null;
	DataRow						dataRow				= null;

	/**
	 * Need to support Serializable via Exception
	 */
	private static final long	serialVersionUID	= 1L;
}
