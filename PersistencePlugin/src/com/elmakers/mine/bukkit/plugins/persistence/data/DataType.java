package com.elmakers.mine.bukkit.plugins.persistence.data;

import java.util.Date;
import java.util.List;

import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistClass;

public enum DataType
{
	INTEGER,
	BOOLEAN,
	DOUBLE,
	STRING,
	DATE,
	OBJECT,
	LIST,
	NULL;
	
	public String toString()
	{
		return this.name().toLowerCase();
	}
	
	public static DataType getTypeFromClass(Class<?> fieldType)
	{
		DataType sqlType = NULL;
		
		if (fieldType.isAssignableFrom(List.class))
		{
			sqlType = DataType.LIST;
		}
		else if (fieldType.isAssignableFrom(Date.class))
		{
			sqlType = DataType.DATE;
		}
		else if (fieldType.isAssignableFrom(Boolean.class))
		{
			sqlType = DataType.BOOLEAN;
		}
		else if (fieldType.isAssignableFrom(Integer.class))
		{
			sqlType = DataType.INTEGER;
		}
		else if (fieldType.isAssignableFrom(Double.class))
		{
			sqlType = DataType.DOUBLE;
		}
		else if (fieldType.isAssignableFrom(Float.class))
		{
			sqlType = DataType.DOUBLE;
		}
		else if (fieldType.isAssignableFrom(String.class))
		{
			sqlType = DataType.STRING;
		}
		else if (fieldType.isAssignableFrom(boolean.class))
		{
			sqlType = DataType.BOOLEAN;
		}
		else if (fieldType.isAssignableFrom(int.class))
		{
			sqlType = DataType.INTEGER;
		}
		else if (fieldType.isAssignableFrom(double.class))
		{
			sqlType = DataType.DOUBLE;
		}
		else if (fieldType.isAssignableFrom(float.class))
		{
			sqlType = DataType.DOUBLE;
		}
		else
		{
			// Don't get the PersistedClass here, or you might cause recursion issues with circular dependencies.
			// Instead, manually look for the annotation.
			PersistClass classSettings = fieldType.getAnnotation(PersistClass.class);
			if (classSettings != null)
			{
				sqlType = DataType.OBJECT;
			}
		}
		return sqlType;
	}
	
	public static Object convertFrom(Object field, DataType dataType)
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
	
	public static Object convertTo(Object field, DataType dataType)
	{
		if (field == null) return null;
		
		if (dataType == DataType.DATE)
		{
			Integer i = (Integer)field;
			Date d = new Date(i * 1000);
			return d;
		}
		if (dataType == DataType.BOOLEAN)
		{
			Integer i = (Integer)field;
			Boolean b = i != 0;
			return b;
		}
		return field;
	}
}
