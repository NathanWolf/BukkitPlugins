package com.elmakers.mine.craftbukkit.persistence.data;

import java.util.Date;
import java.util.List;

public enum DataType
{
	BYTE,
	INTEGER,
	LONG,
	BOOLEAN,
	FLOAT,
	DOUBLE,
	STRING,
	DATE,
	ENUMERATION,
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
		
		if (fieldType.isEnum())
		{
			sqlType = DataType.ENUMERATION;
		}
		else if (List.class.isAssignableFrom(fieldType))
		{
			sqlType = DataType.LIST;
		}
		else if (Enum.class.isAssignableFrom(fieldType))
		{
			sqlType = DataType.ENUMERATION;
		} 
		else if (Date.class.isAssignableFrom(fieldType))
		{
			sqlType = DataType.DATE;
		}
		else if (Boolean.class.isAssignableFrom(fieldType))
		{
			sqlType = DataType.BOOLEAN;
		}
		else if (Integer.class.isAssignableFrom(fieldType))
		{
			sqlType = DataType.INTEGER;
		}
		else if (Double.class.isAssignableFrom(fieldType))
		{
			sqlType = DataType.DOUBLE;
		}
		else if (Float.class.isAssignableFrom(fieldType))
		{
			sqlType = DataType.FLOAT;
		}
		else if (Long.class.isAssignableFrom(fieldType))
		{
			sqlType = DataType.LONG;
		}
		else if (Byte.class.isAssignableFrom(fieldType))
		{
			sqlType = DataType.BYTE;
		}
		else if (String.class.isAssignableFrom(fieldType))
		{
			sqlType = DataType.STRING;
		}
		else if (boolean.class.isAssignableFrom(fieldType))
		{
			sqlType = DataType.BOOLEAN;
		}
		else if (int.class.isAssignableFrom(fieldType))
		{
			sqlType = DataType.INTEGER;
		}
		else if (long.class.isAssignableFrom(fieldType))
		{
			sqlType = DataType.LONG;
		}
		else if (double.class.isAssignableFrom(fieldType))
		{
			sqlType = DataType.DOUBLE;
		}
		else if (float.class.isAssignableFrom(fieldType))
		{
			sqlType = DataType.FLOAT;
		}
		else if (byte.class.isAssignableFrom(fieldType))
		{
			sqlType = DataType.BYTE;
		}
		else
		{
			// Don't get the PersistedClass here, or you might cause recursion issues with circular dependencies.
			// Also, don't look for an annotation, since it might not be used. 
			// We make sure the object reference is valid in validate().
			sqlType = DataType.OBJECT;
		}
		return sqlType;
	}
	
	// TODO: SqlDataType class?
	public static int getSqlType(DataType dataType)
	{
		switch(dataType)
		{
			case BOOLEAN: return java.sql.Types.BOOLEAN;
			case DATE: return java.sql.Types.DATE;
			case DOUBLE: return java.sql.Types.DOUBLE;
			case FLOAT: return java.sql.Types.FLOAT;
			case INTEGER: return java.sql.Types.INTEGER;
			case BYTE: return java.sql.Types.TINYINT;
			case STRING: return java.sql.Types.VARCHAR;
		}
		
		return java.sql.Types.NULL;
	}
	

	/**
	 * Convert a SQL data type to a DataType.
	 * 
	 * Only converts supported DataTypes.
	 * 
	 * TODO: Move this to a SqlData type class, allow
	 * stores to override type conversion
	 * 
	 * @param sqlType The java.sql.Type value to convert
	 * @return A DataType value, or DataType.NULL if invalid
	 */
	public static DataType getTypeFromSqlType(int sqlType)
	{
		DataType dataType = DataType.NULL;
		
		switch(sqlType)
		{
			case java.sql.Types.BOOLEAN:
				dataType = DataType.BOOLEAN;
				break;
			case java.sql.Types.BIT:
				dataType = DataType.BOOLEAN;
				break;	
			case java.sql.Types.TINYINT:
				dataType = DataType.BYTE;
				break;	
			case java.sql.Types.DATE:
				dataType = DataType.DATE;
				break;
			case java.sql.Types.DECIMAL:
				dataType = DataType.DOUBLE;
				break;	
			case java.sql.Types.DOUBLE:
				dataType = DataType.DOUBLE;
				break;	
			case java.sql.Types.FLOAT:
				dataType = DataType.DOUBLE;
				break;	
			case java.sql.Types.INTEGER:
				dataType = DataType.INTEGER;
				break;	
			case java.sql.Types.JAVA_OBJECT:
				dataType = DataType.OBJECT;
				break;	
			case java.sql.Types.LONGNVARCHAR:
				dataType = DataType.STRING;
				break;	
			case java.sql.Types.LONGVARCHAR:
				dataType = DataType.STRING;
				break;	
			case java.sql.Types.NCHAR:
				dataType = DataType.STRING;
				break;	
			case java.sql.Types.NULL:
				dataType = DataType.NULL;
				break;	
			case java.sql.Types.NUMERIC:
				dataType = DataType.INTEGER;
				break;	
			case java.sql.Types.REAL:
				dataType = DataType.DOUBLE;
				break;	
			case java.sql.Types.ROWID:
				dataType = DataType.INTEGER;
				break;	
			case java.sql.Types.SMALLINT:
				dataType = DataType.INTEGER;
				break;	
			case java.sql.Types.TIME:
				dataType = DataType.DATE;
				break;	
			case java.sql.Types.TIMESTAMP:
				dataType = DataType.DATE;
				break;	
			case java.sql.Types.VARCHAR:
				dataType = DataType.STRING;
				break;	
		}
		
		return dataType;
	}
	
	public static boolean isPrimitive(Class<?> targetClass)
	{
		if (targetClass == null) return false;
		
		return
		(
			boolean.class.isAssignableFrom(targetClass)
		||	float.class.isAssignableFrom(targetClass)
		||	int.class.isAssignableFrom(targetClass)
		||	byte.class.isAssignableFrom(targetClass)
		||	long.class.isAssignableFrom(targetClass)
		||	double.class.isAssignableFrom(targetClass)
		);
	}
	
	public static Object convertValue(Object value, Class<?> targetClass)
	{
		if (value == null) 
		{
			return null;
		}
		
		Class<?> valueClass = value.getClass();
		
		if (targetClass.isAssignableFrom(valueClass))
		{
			return value;
		}
		
		if (targetClass.isEnum() && (int.class.isAssignableFrom(valueClass) || Integer.class.isAssignableFrom(valueClass)))
		{
			int ordinal = (Integer)value;
			return targetClass.getEnumConstants()[ordinal];
		}
		
		// Handle dates- converting to int for now, should be encapsulated in a DataStore-specific way, but
		// _not_ by moving this whole function to DataStore, since it's static.
		if (Date.class.isAssignableFrom(targetClass))
		{
			if (Integer.class.isAssignableFrom(valueClass) || int.class.isAssignableFrom(valueClass))
			{
				Integer intDate = (Integer)value;
				Date d = new Date(intDate * 1000);
				return d;
			}
			
			if (Long.class.isAssignableFrom(valueClass) || long.class.isAssignableFrom(valueClass))
			{
				Long intDate = (Long)value;
				Date d = new Date(intDate * 1000);
				return d;
			}
		}
		
		// Handle bools- again, assume we can't handle them and change to ints.
		if (boolean.class.isAssignableFrom(targetClass) || Boolean.class.isAssignableFrom(targetClass))
		{
			if (Boolean.class.isAssignableFrom(valueClass) || boolean.class.isAssignableFrom(valueClass))
			{
				return value;	
			}
			if (Integer.class.isAssignableFrom(valueClass) || int.class.isAssignableFrom(valueClass))
			{
				Integer intBoolean = (Integer)value;
				Boolean b = intBoolean != 0;
				return b;	
			}
		}
		
		// Handle conversion to byte
		if (byte.class.isAssignableFrom(targetClass) || Byte.class.isAssignableFrom(targetClass))
		{
			if (Byte.class.isAssignableFrom(valueClass) || byte.class.isAssignableFrom(valueClass))
			{
				return value;	
			}
			if (Long.class.isAssignableFrom(valueClass) || long.class.isAssignableFrom(valueClass))
			{
				return (Byte)(byte)(long)(Long)value;	
			}
			if (Float.class.isAssignableFrom(valueClass) || float.class.isAssignableFrom(valueClass))
			{
				return (Byte)(byte)(float)(Float)value;	
			}
			if (Double.class.isAssignableFrom(valueClass) || double.class.isAssignableFrom(valueClass))
			{
				return (Byte)(byte)(double)(Double)value;	
			}
			if (Integer.class.isAssignableFrom(valueClass) || int.class.isAssignableFrom(valueClass))
			{
				return (Byte)(byte)(int)(Integer)value;	
			}
		}
		
		// Handle conversion to int
		if (int.class.isAssignableFrom(targetClass) || Integer.class.isAssignableFrom(targetClass))
		{
			if (Integer.class.isAssignableFrom(valueClass) || int.class.isAssignableFrom(valueClass))
			{
				return value;	
			}
			if (Long.class.isAssignableFrom(valueClass) || long.class.isAssignableFrom(valueClass))
			{
				return (Integer)(int)(long)(Long)value;	
			}
			if (Float.class.isAssignableFrom(valueClass) || float.class.isAssignableFrom(valueClass))
			{
				return (Integer)(int)(float)(Float)value;	
			}
			if (Double.class.isAssignableFrom(valueClass) || double.class.isAssignableFrom(valueClass))
			{
				return (Integer)(int)(double)(Double)value;	
			}
			if (Byte.class.isAssignableFrom(valueClass) || byte.class.isAssignableFrom(valueClass))
			{
				return (Integer)(int)(byte)(Byte)value;	
			}
		}
		
		// Conversion to long
		if (long.class.isAssignableFrom(targetClass) || Long.class.isAssignableFrom(targetClass))
		{
			if (Long.class.isAssignableFrom(valueClass) || long.class.isAssignableFrom(valueClass))
			{
				return value;
			}
			if (Float.class.isAssignableFrom(valueClass) || float.class.isAssignableFrom(valueClass))
			{
				return (Long)(long)(float)(Float)value;	
			}
			if (Integer.class.isAssignableFrom(valueClass) || int.class.isAssignableFrom(valueClass))
			{
				return (Long)(long)(int)(Integer)value;	
			}
			if (Double.class.isAssignableFrom(valueClass) || double.class.isAssignableFrom(valueClass))
			{
				return (Long)(long)(double)(Double)value;	
			}
			if (Byte.class.isAssignableFrom(valueClass) || byte.class.isAssignableFrom(valueClass))
			{
				return (Long)(long)(byte)(Byte)value;	
			}
		}
		
		// Conversion to flaot
		if (float.class.isAssignableFrom(targetClass) || Float.class.isAssignableFrom(targetClass))
		{
			if (Float.class.isAssignableFrom(valueClass) || float.class.isAssignableFrom(valueClass))
			{
				return value;
			}
			if (Long.class.isAssignableFrom(valueClass) || long.class.isAssignableFrom(valueClass))
			{
				return (Float)(float)(long)(Long)value;	
			}
			if (Integer.class.isAssignableFrom(valueClass) || int.class.isAssignableFrom(valueClass))
			{
				return (Float)(float)(Integer)value;	
			}
			if (Double.class.isAssignableFrom(valueClass) || double.class.isAssignableFrom(valueClass))
			{
				return (Float)(float)(double)(Double)value;	
			}
			if (Byte.class.isAssignableFrom(valueClass) || byte.class.isAssignableFrom(valueClass))
			{
				return (Float)(float)(byte)(Byte)value;	
			}
		}
		
		// Conversion to double
		if (double.class.isAssignableFrom(targetClass) || Double.class.isAssignableFrom(targetClass))
		{
			if (Double.class.isAssignableFrom(valueClass) || double.class.isAssignableFrom(valueClass))
			{
				return value;
			}
			if (Long.class.isAssignableFrom(valueClass) || long.class.isAssignableFrom(valueClass))
			{
				return (Integer)(int)(long)(Long)value;	
			}
			if (Integer.class.isAssignableFrom(valueClass) || int.class.isAssignableFrom(valueClass))
			{
				return (Double)(double)(Integer)value;	
			}
			if (Float.class.isAssignableFrom(valueClass) || float.class.isAssignableFrom(valueClass))
			{
				return (Double)(double)(float)(Float)value;	
			}
			if (Byte.class.isAssignableFrom(valueClass) || byte.class.isAssignableFrom(valueClass))
			{
				return (Double)(double)(byte)(Byte)value;	
			}
		}
				
		return value;
	}
	
}
