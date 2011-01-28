package com.elmakers.mine.bukkit.plugins.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.elmakers.mine.bukkit.plugins.persistence.annotations.Persist;
import com.elmakers.mine.bukkit.plugins.persistence.annotations.PersistClass;
import com.elmakers.mine.bukkit.plugins.persistence.stores.PersistenceStore;

public class PersistedClass
{
	public boolean bind(Class<? extends Object> persistClass)
	{
		this.persistClass = persistClass;
		
		/*
		 * Set up persisted class
		 */
		PersistClass classSettings = persistClass.getAnnotation(PersistClass.class);
		
		if (classSettings == null)
		{
			// TODO : Log error
			return false;
		}		

		cacheObjects = classSettings.cache();
		schema = classSettings.schema();
		name = classSettings.name();
		
		name = name.replace(" ", "_");
		schema = schema.replace(" ", "_");
		
		store = Persistence.getInstance().getStore(schema);
		
		if (!cacheObjects)
		{
			// TODO: support non-cached objects
			return false;
		}
		
		/*
		 * Find fields, getters and setters
		 */
		
		idField = null;
		orderByField = null;

		for (Field field : persistClass.getDeclaredFields())
		{
			Persist persist = field.getAnnotation(Persist.class);
			if (persist != null)
			{
				PersistedField pField = new PersistedField(field);
				fields.add(pField);
				if (persist.id())
				{
					idField = pField;
				}
				if (persist.order())
				{
					orderByField = pField;
				}
			}
		}
	
		for (Method method : persistClass.getDeclaredMethods())
		{
			Persist persist = method.getAnnotation(Persist.class);
			if (persist != null)
			{
				Method setter = null;
				Method getter = null;
				String fieldName = getNameFromMethod(method);
				if (fieldName.length() == 0)
				{
					// TODO : log error?
					continue;
				}
				
				if (isSetter(method))
				{
					setter = method;
					getter = findGetter(fieldName, persistClass);
				}
				else if (isGetter(method))
				{
					getter = method;
					setter = findSetter(fieldName, persistClass);
				}
				
				if (getter == null || setter == null)
				{
					// TODO: Log error!
					continue;
				}
				
				PersistedField pField = new PersistedField(getter, setter);
				fields.add(pField);
				if (persist.id())
				{
					idField = pField;
				}
				if (persist.order())
				{
					orderByField = pField;
				}
			}
		}
		
		return (fields.size() > 0);
	}
	
	public void put(Object o)
	{
		checkLoadCache();
		Object idField = getId(o);
		CachedObject co = cacheMap.get(idField);
		if (co == null)
		{
			co = addToCache(o);
		}	
		co.setCached(cacheObjects);
		co.setObject(o);
		dirty = true;
	}

	public Object get(Object id)
	{
		checkLoadCache();
		return cacheMap.get(id).getObject();
	}
	
	/*
	public void getAll(List<? extends Object> objects)
	{
	}
	*/
	
	@SuppressWarnings("unchecked")
	public <T> void getAll(List<T> objects)
	{
		checkLoadCache();
		for (CachedObject cachedObject : cache)
		{
			Object object = cachedObject.getObject();
			if (persistClass.isAssignableFrom(object.getClass()))
			{
				objects.add((T)object);
			}
		}
	}
	
	public void putAll(List<? extends Object> objects)
	{
		checkLoadCache();
		// TODO: merge
	}

	public Object get(Object id, Object defaultValue)
	{
		checkLoadCache();
		CachedObject cached = cacheMap.get(id);
		if (cached == null)
		{
			cached = addToCache(defaultValue);
			
		}
		return cached.getObject();
	}
	
	public boolean isDirty()
	{
		return dirty;
	}
	
	public int getFieldCount()
	{
		return fields.size();
	}
	
	public String getTableName()
	{
		return name;
	}
	
	/*
	 * Protected members
	 */
		
	protected void checkLoadCache()
	{
		if (!loaded && cacheObjects)
		{
			store.connect(schema);
			store.validateTable(this);
			loaded = true;
		}
	}
	
	protected boolean isGetter(Method method)
	{
		String methodName = method.getName();
		return methodName.substring(0, 3).equals("get");
	}
	
	protected boolean isSetter(Method method)
	{
		String methodName = method.getName();
		return methodName.substring(0, 3).equals("get");
	}
	
	protected String getNameFromMethod(Method method)
	{
		String methodName = method.getName();
		return methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
	}
	
	protected Method findSetter(String name, Class<? extends Object> c)
	{
		Method setter = null;
		try
		{
			setter = c.getMethod("set" + name, Object.class);
		}
		catch (NoSuchMethodException e)
		{
			setter = null;
		}
		return setter;
	}
	
	protected Method findGetter(String name, Class<?> c)
	{
		Method getter = null;
		try
		{
			getter = c.getMethod("get" + name);
		}
		catch (NoSuchMethodException e)
		{
			getter = null;
		}
		return getter;
	}
	
	protected Object getId(Object o)
	{
		Object value = null;
		if (idField != null)
		{
			value = idField.get(o);
		}
		return value;
	}
	
	protected CachedObject addToCache(Object o)
	{
		CachedObject cached = new CachedObject(o);
		cache.add(cached);
		cacheMap.put(o, cached);
		return cached;
	}
	
	
	/*
	 * Private data
	 */
	
	protected boolean dirty = false;
	protected boolean loaded = false;
	
	protected boolean cacheObjects;
	protected Class<? extends Object> persistClass;
	
	protected List<PersistedField> fields = new ArrayList<PersistedField>();
	protected PersistedField idField;
	protected PersistedField orderByField;
	
	protected String schema;
	protected String name;
	
	protected PersistenceStore store = null;
	
	protected HashMap<Object, CachedObject> cacheMap = new HashMap<Object, CachedObject>();
	protected List<CachedObject>			cache =  new ArrayList<CachedObject>();
}
