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

		for (Field classField : persistClass.getDeclaredFields())
		{
			Persist persist = classField.getAnnotation(Persist.class);
			if (persist != null)
			{
				PersistedField field = PersistedField.tryCreate(classField, persistClass);		
				if (field == null)
				{
					log.warning("Persistence: Field " + persistClass.getName() + "." + classField.getName() + " is not persistable, type=" + classField.getType().getName());
				}
				else
				{
					addField(field, persist);
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
					log.warning("Persistence: Field " + persistClass.getName() + "." + method.getName() + " is not persistable, type=" + method.getReturnType().getName());
				}
				else
				{
					addField(field, persist);
				}
			}
		}
		
		if (idField == null)
		{
			log.warning("Persistence: class " + persistClass.getName() + ": must specify one id field. Use an auto int if you need.");
			return false;
		}
		
		return (fields.size() > 0);
	}
	
	public boolean addField(PersistedField field, Persist persist)
	{
		if (persist.id())
		{
			if (idField != null)
			{
				log.warning("Persistence: class " + persistClass.getName() + ": can't have more than one id field");
				return false;
			}
			idField = field;
			field.setIsIdField(true);
		}
		
		if (field instanceof PersistedList)
		{
			PersistedList list = (PersistedList)field;
			externalFields.add(list);
			list.setContained(persist.contained());
		}
		else if (field instanceof PersistedReference)
		{
			PersistedReference reference = (PersistedReference)field;
			internalFields.add(reference);
			reference.setContained(persist.contained());
		}
		else
		{
			internalFields.add(field);
		}

		fields.add(field);
		
		return true;
	}
	
	public void bindReferences()
	{
		for (PersistedField field : fields)
		{
			field.bind();
		}
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
		CachedObject cached = cacheMap.get(id);
		if (cached == null) return null;
		return cached.getObject();
	}
	
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
		// TODO: merge...
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
	
	public void clear()
	{
		cacheMap.clear();
		cache.clear();
		loadState = LoadState.UNLOADED;
	}
	
	public void reset()
	{
		store.reset(this);
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
	
	public String getSchema()
	{
		return schema;
	}
	
	public Class<? extends Object> getPersistClass()
	{
		return persistClass;
	}
	
	public PersistedField getIdField()
	{
		return idField;
	}
	
	
	public List<PersistedField> getInternalFields()
	{
		return internalFields;
	}
	
	public List<PersistedList> getExternalFields()
	{
		return externalFields;
	}
	
	public List<PersistedField> getPersistedFields()
	{
		return fields;
	}
	
	public void save()
	{
		if (loadState != LoadState.LOADED) return;
		if (!dirty) return;
		
		for (CachedObject cached : cache)
		{
			if (!cached.isDirty()) continue;
			store.save(this, cached.getObject());
			cached.setSaved();
		}
		
		dirty = false;
	}
	
	/*
	 * Protected members
	 */
		
	protected void checkLoadCache()
	{
		if (loadState == LoadState.UNLOADED && cacheObjects)
		{
			loadState = LoadState.LOADING;
			store.connect(schema);
			store.validateTables(this);
			store.loadAll(this);
			loadState = LoadState.LOADED;
		}
	}
	
	public Object getId(Object o)
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
		Object id = getId(o);
		cacheMap.put(id, cached);
		return cached;
	}
	
	
	/*
	 * Private data
	 */
	
	enum LoadState
	{
		UNLOADED,
		LOADING,
		LOADED,
	}
	
	protected boolean dirty = false;
	protected LoadState loadState = LoadState.UNLOADED;
	
	protected boolean cacheObjects;
	protected Class<? extends Object> persistClass;
	
	protected List<PersistedField> fields = new ArrayList<PersistedField>();
	protected List<PersistedField> internalFields = new ArrayList<PersistedField>();
	protected List<PersistedList> externalFields = new ArrayList<PersistedList>();
	
	protected PersistedField idField;
	
	protected String schema;
	protected String name;
	
	protected PersistenceStore store = null;
	
	protected HashMap<Object, CachedObject> cacheMap = new HashMap<Object, CachedObject>();
	protected List<CachedObject>			cache =  new ArrayList<CachedObject>();
	
	protected Logger log = PersistencePlugin.getLogger();
}
