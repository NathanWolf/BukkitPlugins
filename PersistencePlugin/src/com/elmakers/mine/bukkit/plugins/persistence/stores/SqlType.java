package com.elmakers.mine.bukkit.plugins.persistence.stores;

import java.util.Date;

public enum SqlType
{
	INTEGER,
	BOOLEAN,
	DOUBLE,
	STRING,
	DATE,
	OBJECT,
	NULL;
	
	public static SqlType getTypeFromClass(Class<?> fieldType)
	{
		SqlType sqlType = NULL;
		
		if (fieldType.isAssignableFrom(Date.class))
		{
			sqlType = SqlType.DATE;
		}
		else if (fieldType.isAssignableFrom(Boolean.class))
		{
			sqlType = SqlType.BOOLEAN;
		}
		else if (fieldType.isAssignableFrom(Integer.class))
		{
			sqlType = SqlType.INTEGER;
		}
		else if (fieldType.isAssignableFrom(Double.class))
		{
			sqlType = SqlType.DOUBLE;
		}
		else if (fieldType.isAssignableFrom(Float.class))
		{
			sqlType = SqlType.DOUBLE;
		}
		else if (fieldType.isAssignableFrom(String.class))
		{
			sqlType = SqlType.STRING;
		}
		else if (fieldType.isAssignableFrom(boolean.class))
		{
			sqlType = SqlType.BOOLEAN;
		}
		else if (fieldType.isAssignableFrom(int.class))
		{
			sqlType = SqlType.INTEGER;
		}
		else if (fieldType.isAssignableFrom(double.class))
		{
			sqlType = SqlType.DOUBLE;
		}
		else if (fieldType.isAssignableFrom(float.class))
		{
			sqlType = SqlType.DOUBLE;
		}
		return sqlType;
	}
}
