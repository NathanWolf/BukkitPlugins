package com.elmakers.mine.bukkit.plugins.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

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
			log.warning("Persistence: class " + persistClass.getName() + " does not have the @PersistClass annotation.");
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
			log.warning("Persistence: class " + persistClass.getName() + ": non-cached objects no supported, yet.");
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
					pField.setIsIdField(true);
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
				PersistedField field = PersistedField.tryCreate(method, persistClass);
				
				if (field == null)
				{
					log.warning("Persistence: class " + persistClass.getName() + ": getter/setter " + method.getName() + " failed to bind - missing matching getter/setter?");
					continue;
				}
				
				fields.add(field);
				if (persist.id())
				{
					idField = field;
					field.setIsIdField(true);
				}
				if (persist.order())
				{
					orderByField = field;
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
	
	public Class<? extends Object> getPersistClass()
	{
		return persistClass;
	}
	
	public List<PersistedField> getPersistedFields()
	{
		List<PersistedField> fieldsCopy = new ArrayList<PersistedField>();
		
		fieldsCopy.addAll(fields);
		return fieldsCopy;
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
			store.loadAll(this);
			loaded = true;
		}
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
	
	protected Logger log = PersistencePlugin.getLogger();
}
