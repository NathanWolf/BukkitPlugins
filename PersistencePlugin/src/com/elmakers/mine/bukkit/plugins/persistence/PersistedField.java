package com.elmakers.mine.bukkit.plugins.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PersistedField
{
	public PersistedField(Method getter, Method setter)
	{
		this.getter = getter;
		this.setter = setter;
		this.field = null;
	}
	
	public PersistedField(Field field)
	{
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
	
	private Method getter;
	private Method setter;
	private Field field;
}
