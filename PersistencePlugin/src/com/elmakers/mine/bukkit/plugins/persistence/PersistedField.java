package com.elmakers.mine.bukkit.plugins.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PersistedField
{
	public PersistedField(Method getter, Method setter)
	{
		this.name = getNameFromMethod(setter);
		this.getter = getter;
		this.setter = setter;
		this.field = null;
	}
	
	public PersistedField(Field field)
	{
		this.name = field.getName();
		this.field = field;
		this.getter = null;
		this.setter = null;
	}
	
	public boolean set(Object o, Object value)
	{
		if (setter != null)
		{
			try
			{
				setter.invoke(o, value);
			}
			catch(InvocationTargetException e)
			{
				return false;
			}
			catch(IllegalAccessException e)
			{
				return false;
			}
		}
		
		if (field != null)
		{
			try
			{
				field.set(o, value);
			}
			catch(IllegalAccessException e)
			{
				return false;
			}
		}
		return true;
	}
	
	public Object get(Object o)
	{
		Object result = null;
		if (getter != null)
		{
			try
			{
				result = getter.invoke(o);
			}
			catch(InvocationTargetException e)
			{
				result = null;
			}
			catch(IllegalAccessException e)
			{
				result = null;
			}
		}
		
		if (result == null && field != null)
		{
			try
			{
				result = field.get(o);
			}
			catch(IllegalAccessException e)
			{
				result = null;
			}
		}
		
		return result;
	}
	
	public DataType getColumnType()
	{
		Class<?> fieldType = getType();
		return DataType.getTypeFromClass(fieldType);
	}
	
	public Class<?> getType()
	{
		if (getter != null)
		{
			return getter.getReturnType();
		}
		if (field != null)
		{
			return field.getType();
		}
		return null;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getColumnName()
	{
		return name;
	}
	
	public boolean isIdField()
	{
		return idField;
	}
	
	public void setIsIdField(boolean isId)
	{
		idField = isId;
	}
	
	public static PersistedField tryCreate(Field field, Class<?> persistClass)
	{
		DataType dataType = DataType.getTypeFromClass(field.getType());
		PersistedField pField = null;

		if (dataType == DataType.OBJECT)
		{
			pField = new PersistedReference(field);
		}
		else if (dataType != DataType.NULL)
		{
			pField = new PersistedField(field);
		}
		return pField;
	}
	
	public static PersistedField tryCreate(Method getterOrSetter, Class<?> persistClass)
	{
		Method setter = null;
		Method getter = null;
		String fieldName = getNameFromMethod(getterOrSetter);
		
		if (fieldName.length() == 0)
		{
			return null;
		}
		
		if (isSetter(getterOrSetter))
		{
			setter = getterOrSetter;
			getter = findGetter(setter, persistClass);
		}
		else if (isGetter(getterOrSetter))
		{
			getter = getterOrSetter;
			setter = findSetter(getter, persistClass);
		}
		if (setter == null || getter == null)
		{
			return null;
		}
		
		PersistedField pField = null;
		DataType dataType = DataType.getTypeFromClass(getter.getReturnType());
		
		if (dataType == DataType.OBJECT)
		{
			pField = new PersistedReference(getter, setter);
		}
		else if (dataType != DataType.NULL)
		{
			pField = new PersistedField(getter, setter);
		}

		return pField;
	}
	
	public static boolean isGetter(Method method)
	{
		String methodName = method.getName();
		boolean hasGet = methodName.substring(0, 3).equals("get");
		boolean hasIs = methodName.substring(0, 2).equals("is");
		
		return hasIs || hasGet;
	}
	
	public static boolean isSetter(Method method)
	{
		String methodName = method.getName();
		return methodName.substring(0, 3).equals("set");
	}
	
	public static String getNameFromMethod(Method method)
	{
		String methodName = method.getName();
		return methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
	}
	
	public static Method findSetter(Method getter, Class<? extends Object> c)
	{
		Method setter = null;
		String methodName = getter.getName();
		String name = "s" + methodName.substring(1);
		if (methodName.substring(0, 2).equals("is"))
		{
			name = "set" + methodName.substring(2);
		}
		try
		{
			setter = c.getMethod(name, getter.getReturnType());
		}
		catch (NoSuchMethodException e)
		{
			setter = null;
		}
		return setter;
	}
	
	public static Method findGetter(Method setter, Class<?> c)
	{
		Method getter = null;
		String name = "g" + setter.getName().substring(1);
		try
		{
			getter = c.getMethod(name);
		}
		catch (NoSuchMethodException e)
		{
			getter = null;
		}
		if (getter == null)
		{
			name = "is" + setter.getName().substring(3);
			try
			{
				getter = c.getMethod(name);
			}
			catch (NoSuchMethodException e)
			{
				getter = null;
			}
		}
		return getter;
	}
	
	public void bind()
	{
		
	}
	
	private Method		getter;
	private Method		setter;
	private Field		field;
	protected String	name;
	protected boolean	idField	= false;
}
