package com.elmakers.mine.bukkit.plugins.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class PersistedList extends PersistedField
{
	public PersistedList(Field field)
	{
		super(field);
		findListType();
	}
	
	public PersistedList(Method getter, Method setter)
	{
		super(getter, setter);
		findListType();
	}
	
	public Class<?> getListType()
	{
		return listType;
	}
	
	public DataType getListColumnType()
	{
		return DataType.getTypeFromClass(getListType());
	}

	@SuppressWarnings("unchecked")
	public List<? extends Object> getList(Object o)
	{
		return (List<? extends Object>)get(o);
	}
	
	public String getTableName(PersistedClass persisted)
	{
		String tableName = name;
		tableName = tableName.substring(0, 1).toUpperCase() + tableName.substring(1);
		return persisted.getTableName() + tableName;
	}
	
	public String getIdColumnName(PersistedClass persisted, PersistedField idField)
	{
		String tableName = persisted.getTableName();
		String idFieldName = idField.getColumnName();
		idFieldName = tableName.substring(0, 1).toLowerCase() + tableName.substring(1) 
			+ idFieldName.substring(0, 1).toUpperCase() + idFieldName.substring(1);
		return idFieldName;
	}
	
	public String getDataColumnName()
	{
		return getColumnName();
	}
	
	protected Type getGenericType()
	{
		Type genericType = null;
		if (getter != null)
		{
			genericType = getter.getGenericReturnType();
		}
		else
		{
			genericType = field.getGenericType();
		}
		return genericType;
	}
	
	@Override
	public String getColumnName()
	{
		String columnName = name;
		if (columnName.charAt(columnName.length() - 1) == 's')
		{
			columnName = columnName.substring(0, columnName.length() - 1);
		}
		return columnName;
	}
	
	protected void findListType()
	{
        Type type = getGenericType();  
        
        if (type instanceof ParameterizedType) 
        {  
            ParameterizedType pt = (ParameterizedType)type;
            if (pt.getActualTypeArguments().length > 0)
            {
            	listType = (Class<?>)pt.getActualTypeArguments()[0];
            }
        }  
	}
	
	protected Class<?> listType;
}
