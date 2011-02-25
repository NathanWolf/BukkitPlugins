package com.elmakers.mine.craftbukkit.persistence.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.bukkit.Server;

import com.elmakers.mine.bukkit.persistence.EntityInfo;
import com.elmakers.mine.bukkit.persistence.FieldInfo;
import com.elmakers.mine.bukkit.persistence.MigrationInfo;
import com.elmakers.mine.bukkit.persistence.annotation.PersistField;
import com.elmakers.mine.bukkit.persistence.dao.Persisted;
import com.elmakers.mine.bukkit.plugins.persistence.PersistencePlugin;
import com.elmakers.mine.craftbukkit.persistence.data.DataField;
import com.elmakers.mine.craftbukkit.persistence.data.DataRow;
import com.elmakers.mine.craftbukkit.persistence.data.DataStore;
import com.elmakers.mine.craftbukkit.persistence.data.DataTable;
import com.elmakers.mine.craftbukkit.persistence.data.DataType;

/**
 * Represents and manages a single persisted class.
 * 
 * This class binds to a Class, looking for @Persist and @PersistClass
 * tags.
 * 
 * It will keep track of the persisted fields of a class, and handle
 * creating and caching object instances of that class.
 * 
 * @author NathanWolf
 *
 */
public class PersistedClass
{
	public PersistedClass(EntityInfo entityInfo, Server server)
	{
		this.entityInfo = entityInfo;
		this.server = server;
	}
	
	public PersistedClass(PersistedClass copy, PersistedField container)
	{
		this.contained = true;
		this.schema = copy.schema;
		this.container = container;
		this.name = copy.name;
		this.schemaName = copy.schemaName;
		this.entityInfo = copy.entityInfo;
		this.persistClass = copy.persistClass;
		this.server = copy.server;
		
		// TODO: Make sure it's ok to share fields!
		for (PersistedField field : copy.fields)
		{
			// If a field is an id field, we don't care about it in the container, unless the container is also the id field
			// Or, if the container is a list, we have to store the id.
			// TODO: Consider this logic for validity (already faulty, feeling too complex now)
			if (!field.isIdField() || container.isIdField() || container instanceof PersistedList)
			{
				addField(field.clone(), field.getFieldInfo());
			}
		}
	}
	
	public void setMigrationInfo(MigrationInfo migrationInfo)
	{
		this.migrationInfo = migrationInfo;
	}
	
	public MigrationInfo getMigrationInfo()
	{
		return migrationInfo;
	}
	
	public boolean bind(Class<? extends Object> persistClass)
	{
		this.persistClass = persistClass;
		
		cacheObjects = entityInfo.isCached();
		schemaName = entityInfo.getSchema();
		name = entityInfo.getName();
		
		name = name.replace(" ", "_");
		schemaName = schemaName.replace(" ", "_");
		schema = null; // Persistence will assign a schema after binding
		
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
			PersistField persist = classField.getAnnotation(PersistField.class);
			if (persist != null)
			{
				PersistedField field = PersistedField.tryCreate(new FieldInfo(persist), classField, this);		
				if (field == null)
				{
					log.warning("Persistence: Field " + persistClass.getName() + "." + classField.getName() + " is not persistable, type=" + classField.getType().getName());
				}
				else
				{
					addField(field, new FieldInfo(persist));
				}
			}
		}
	
		for (Method method : persistClass.getDeclaredMethods())
		{
			PersistField persist = method.getAnnotation(PersistField.class);
			if (persist != null)
			{
				PersistedField field = PersistedField.tryCreate(new FieldInfo(persist), method, this);
				if (field == null)
				{
					Class<?> type = method.getReturnType();
					if (type == void.class && method.getParameterTypes().length > 0)
					{
						type = method.getParameterTypes()[0];
					}
					log.warning("Persistence: Field " + persistClass.getName() + "." + method.getName() + " is not persistable, type=" +type.getName());
				}
				else
				{
					addField(field, new FieldInfo(persist));
				}
			}
		}
		
		return true;
	}

	/*
	 * TODO: make this check for duplicate fields, and also maybe rename to getPersistedField
	 */
	public PersistedField persistField(String fieldName)
	{
		return persistField(fieldName, new FieldInfo());
	}
	
	public PersistedField persistField(String fieldName, FieldInfo fieldInfo)
	{
		PersistedField persistField = null;
		String getterName = fieldName;
		if (fieldInfo.getGetter() != null && fieldInfo.getGetter().length() > 0)
		{
			getterName = fieldInfo.getGetter();
		}
		Method getter = PersistedField.findGetter(getterName, persistClass);
		
		if (getter != null)
		{
			persistField = PersistedField.tryCreate(fieldInfo, getter, this);		
		}
		else
		{
			if (fieldInfo.getField() != null && fieldInfo.getField().length() > 0)
			{
				fieldName = fieldInfo.getField();
			}
			Field field = PersistedField.findField(fieldName, persistClass);
			if (field != null)
			{
				persistField = PersistedField.tryCreate(fieldInfo, field, this);
			}
		}
		if (persistField != null)
		{
			addField(persistField, fieldInfo);
		}
		return persistField;
	}
	
	
	public boolean addField(PersistedField field, FieldInfo fieldInfo)
	{
		if (fieldInfo.isIdField())
		{
			if (idField != null)
			{
				log.warning("Persistence: class " + persistClass.getName() + ": can't have more than one id field");
				return false;
			}
			idField = field;
		}
		
		if (field instanceof PersistedList)
		{
			PersistedList list = (PersistedList)field;
			if (list.getListDataType() == DataType.LIST)
			{
				log.warning("Persistence: class " + persistClass.getName() + ": lists of lists not supported");
				return false;
			}
			externalFields.add(list);
			if (list.isObject())
			{
				referenceFields.add(list);
			}
		}
		else if (field instanceof PersistedObject)
		{
			PersistedObject reference = (PersistedObject)field;
			internalFields.add(reference);
			if (reference.isObject())
			{
				referenceFields.add(reference);
			}
		}
		else
		{
			if (fieldInfo.isContained())
			{
				log.warning("Persistence: class " + persistClass.getName() + ": only List and Object fields may be contained");
				return false;
			}
			if (fieldInfo.isAutogenerated())
			{
				if (!fieldInfo.isIdField())
				{
					log.warning("Persistence: class " + persistClass.getName() + ": only id fields may be autogenerated");
				}
				else if (!field.getType().isAssignableFrom(Integer.class) && !field.getType().isAssignableFrom(int.class))
				{
					log.warning("Persistence: class " + persistClass.getName() + ": only integer fields may be autogenerated");
				}
			}
			internalFields.add(field);
		}
		
		field.setContainer(container);

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
	
	public boolean validate()
	{
		if (idField == null && !isContainedClass())
		{
			log.warning("Persistence: class " + persistClass.getName() + ": must specify one id field. Use an auto int if you need.");
			return false;
		}
		
		if (fields.size() == 0)
		{
			log.warning("Persistence: class " + persistClass.getName() + ": has no persisted fields.");
			return false;			
		}
		
		for (PersistedReference reference : referenceFields)
		{
			if (reference.getReferenceType() == null)
			{
				log.warning("Persistence: class " + persistClass.getName() + ", field " + reference.getName() + " references a non-persistable class: " + reference.getType().getName());
				return false;	
			}
		}
	
		return true;
	}
	
	public boolean isContainedClass()
	{
		return entityInfo.isContained();
	}
	
	public boolean hasContainer()
	{
		return container != null;
	}
	
	public void put(Object o)
	{
		checkLoadCache();
				
		Object id = getId(o);
		CachedObject co = cacheMap.get(id);
		if (co == null)
		{
			co = addToCache(o);
		}
		
		// TODO: merge
		co.setCached(cacheObjects);
		co.setObject(o);
		dirty = true;
	}
	
	public void remove(Object o)
	{
		Object id = getId(o);
		removeFromCache(id);
		dirty = true;
	}
	
	protected Object getById(PersistedField idField, Map<Object, CachedObject> fromCache, Object id)
	{
		if (idField == null || id == null) return null;
		if (id.getClass().isAssignableFrom(idField.getType()))
		{
			CachedObject cached = fromCache.get(id);
			if (cached != null)
			{
				return cached.getObject();
			}
		}
		else
		{
			// Try to do some fancy casting.
			// This is mainly here to avoid the Integer/int problem.
			
			DataField requestId = new DataField(id);
			id = requestId.getValue(idField.getType());
				
			CachedObject cached = fromCache.get(id);
			if (cached != null)
			{
				return cached.getObject();
			}
		}
		
		return null;
	}

	public Object get(Object id, Object defaultValue)
	{
		checkLoadCache();
		Object result = get(id);
		if (result != null)
		{
			return result;
		}
		
		if (defaultValue == null) return null;
		
		CachedObject cached = addToCache(defaultValue);
		return cached.getObject();
	}
	
	public Object get(Object id)
	{
		checkLoadCache();
		PersistedField idField = getIdField();
		if (idField == null) return null;
		
		Object result = getById(idField, cacheMap, id);
		if (result == null)
		{
			idField = idField.getConcreteField();
			result = getById(idField, concreteIdMap, id);
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public <T> void getAll(List<T> objects)
	{
		checkLoadCache();
		for (CachedObject cachedObject : cacheMap.values())
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
	
	public void clear()
	{
		cacheMap.clear();
		concreteIdMap.clear();
		loadState = LoadState.UNLOADED;
	}
	
	public void reset()
	{
		reset(getDefaultStore());
	}
	
	public DataStore getDefaultStore()
	{
		if (schema == null) return null;
		return schema.getStore();
	}
	
	public void reset(DataStore store)
	{
		if (!store.connect()) return;
		
		DataTable resetTable = getClassTable(); 
		store.drop(resetTable.getName());
		
		// Reset any list sub-tables
		for (PersistedList list : externalFields)
		{
			DataTable listTable = getListTable(list);
			store.drop(listTable.getName());
		}
		
		maxId = 1;
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
	
	public String getName()
	{
		return name;
	}
	
	public String getSchemaName()
	{
		return schemaName;
	}
	
	public Schema getSchema()
	{
		return schema;
	}
	
	public void setSchema(Schema schema)
	{
		this.schema = schema;
	}
	
	public Class<? extends Object> getType()
	{
		return persistClass;
	}
	
	public PersistedField getIdField()
	{
		return idField;
	}
	
	public List<PersistedField> getPersistedFields()
	{
		return fields;
	}
	
	public void save()
	{
		save(getDefaultStore());
	}
	
	public void save(DataStore store)
	{
		if (loadState != LoadState.LOADED) return;
		if (!dirty) return;
		
		// Drop removed objects
		Collection<CachedObject> removedList = removedMap.values();
		if (removedList.size() > 0)
		{
			DataTable clearTable = getClassTable();
			populate(clearTable, removedList);			
			store.clear(clearTable);
			
			removedMap.clear();
		}
		
		// Save dirty objects
		List<CachedObject> dirtyObjects = new ArrayList<CachedObject>();
		for (CachedObject cached : cacheMap.values())
		{
			if (cached.isDirty())
			{
				dirtyObjects.add(cached);
			}
		}
		
		save(dirtyObjects, store);
		dirty = false;
	}
	
	protected void populate(DataTable dataTable, Collection<CachedObject> instances)
	{
		for (CachedObject instance : instances)
		{
			DataRow instanceRow = new DataRow(dataTable);
			populate(instanceRow, instance.getObject());
			dataTable.addRow(instanceRow);	
		}
	}
	
	public void populate(DataRow row, Object instance)
	{
		for (PersistedField field : internalFields)
		{
			field.save(row, instance);
		}	
	}
	
	public void save(List<CachedObject> instances)
	{
		save(instances, getDefaultStore());
	}
	
	public void save(List<CachedObject> instances, DataStore store)
	{
		if (!store.connect()) return;
		
		// Save main class data
		DataTable classTable = getClassTable();
		populate(classTable, instances);
		store.save(classTable);
		
		// Save list data
		for (PersistedList list : externalFields)
		{
			DataTable listTable = getListTable(list);
			List<Object> instanceIds = new ArrayList<Object>();
			
			for (CachedObject instance : instances)
			{
				Object id = getIdData(instance.getObject());
				instanceIds.add(id);			
				list.save(listTable, instance.getObject());
			}
			
			// First, delete removed items
			store.clearIds(listTable, instanceIds);
			
			// Save new list data
			store.save(listTable);
		}	
		
		for (CachedObject cached : instances)
		{
			cached.setSaved();
		}
	}
	
	/*
	 * Protected members
	 */
	
	protected DataTable getClassTable()
	{
		DataTable classTable = new DataTable(getTableName());
		return classTable;
	}
	
	protected DataTable getListTable(PersistedList list)
	{
		DataTable listTable = new DataTable(list.getTableName());
		return listTable;
	}
	
	protected void checkLoadCache()
	{
		checkLoadCache(getDefaultStore());
	}
	
	protected void checkLoadCache(DataStore store)
	{
		if (loadState == LoadState.UNLOADED && cacheObjects)
		{
			loadState = LoadState.LOADING;
			try
			{
				if (store.connect())
				{
					validateTables(store);
					loadCache();
					loadState = LoadState.LOADED;
				}
			}
			catch(Exception e)
			{
				log.severe("Persistence: Exception loading cache for " + name);
				clear();
				e.printStackTrace();
				return;
			}
		}
	}
	
	protected void validateTables(DataStore store)
	{
		if (!store.connect())
		{
			return;
		}
		DataTable classTable = getClassTable();
		
		classTable.createHeader();
		populateHeader(classTable);
		
		store.migrateEntity(classTable, this);
		
		// Validate any list sub-tables
		for (PersistedList list : externalFields)
		{
			DataTable listTable = getListTable(list);
			listTable.createHeader();
			list.populateHeader(listTable);
			store.migrateEntity(listTable, this);
		}		
	}
	
	public String getContainedIdName()
	{
		String idName = getTableName();
		if (idField != null)
		{
			idName = PersistedField.getContainedName(idName, idField.getDataName());
		}
		return idName;
	}
	
	public String getContainedIdName(PersistedField container)
	{
		String idName = container.getDataName();
		if (idField != null)
		{
			idName = PersistedField.getContainedName(idName, idField.getDataName());
		}
		return idName;
	}
	
	public void populateHeader(DataTable table)
	{
		for (PersistedField field : internalFields)
		{
			field.populateHeader(table);
		}
	}
	
	protected void loadCache()
	{
		loadCache(getDefaultStore());
	}
	
	protected void loadCache(DataStore store)
	{
		if (!store.connect()) return;
		
		DataTable classTable = getClassTable();
		store.load(classTable);
		
		// Begin deferred referencing, to prevent the problem of DAO's referencing unloaded DAOs.
		// DAOs will be loaded recursively as needed,
		// and then all deferred references will be resolved afterward.
		PersistedObject.beginDefer();
		
		for (DataRow row : classTable.getRows())
		{
			Object newInstance = createInstance(row);
			if (newInstance == null)
			{
				int bar = 0;
				bar++;
			}
			if (newInstance != null)
			{
				if (idField.isAutogenerated())
				{
					int id = (Integer)idField.get(newInstance);
					if (id > maxId) maxId = id;
				}
				
				// Check for objects with objects as ids
				// These will have null ids at the moment, since the
				// id object's class load is deferred.
				
				// So, in this case, we need to get the concrete id 
				// from the data store to cache this instance so we 
				// can look it up later when we bind its instances
				
				Object id = getId(newInstance);
				Object concreteId = null;
				if (id == null)
				{
					DataField idData = row.get(idField.getDataName());
					concreteId = idData.getValue();
				}
				
				addToCache(newInstance, concreteId);
			}
		}
		
		// Bind deferred references, to handle DAOs referencing other DAOs, even of the
		// Same type. 
		// DAOs will be loaded recursively as needed, and then references bound when everything has been
		// resolved.
		PersistedObject.endDefer();

		// Defer load lists of entities
		PersistedList.beginDefer();
		
		// Load list data
		if (externalFields.size() > 0)
		{
			List<Object> instances = new ArrayList<Object>();
			for (CachedObject cached : cacheMap.values())
			{
				instances.add(cached.getObject());
			}
			for (PersistedList list : externalFields)
			{
				DataTable listTable = getListTable(list);
				store.load(listTable);
				list.load(listTable, instances);
			}
		}
		
		// Load any reference lists
		PersistedList.endDefer();
	}
	
	public void load(DataRow row, Object o)
	{
        for (PersistedField field : internalFields)
        {
        	if (field.isReadOnly()) continue;
        	
    		try
    		{
    			field.load(row, o);
    		}
			catch (Exception ex)
			{
				log.warning("Persistence error getting field " + field.getName() + " for " + getTableName());
				ex.printStackTrace();
	        }
        }
	}
	
	protected Object createInstance(DataRow row)
	{
		Object newObject = null;
		
		try
		{
			newObject = persistClass.newInstance();
			updatePersisted(newObject);
			load(row, newObject);
		}
		catch (IllegalAccessException ex)
		{
			newObject = null;
			log.warning("Persistence error creating instance of " + getTableName() + ": " + ex.getMessage());		
		}
		catch (InstantiationException ex)
		{
			newObject = null;
			log.warning("Persistence error creating instance of " + getTableName() + ": " + ex.getMessage());		
		}
		
		return newObject;
	}

	/**
	 * Whew! Ok, took me a while to figure out  I needed this...
	 * 
	 * getIdData will recurse down objects-as-id reference chains. This is for persisting
	 * in the data store.
	 * 
	 * getId will return the actual id value, which is how data is cached internally.
	 * 
	 * A VERY important distinction! You look up object-as-id objects using their id instance,
	 * not the id of their id (of that id's id, etc...)
	 */
	public Object getIdData(Object o)
	{
		Object value = null;
		PersistedField field = idField;
		if (field != null)
		{
			if (field instanceof PersistedReference)
			{
				PersistedReference ref = (PersistedReference)field;
				Object refId = idField.get(o);
				if (ref.getReferenceType() != null)
				{
					return ref.getReferenceType().getIdData(refId);
				}
			}
			else
			{
				value = idField.get(o);				
			}
		}
		return value;
	}
	
	public PersistedField getConcreteIdField()
	{
		if (idField == null)
		{
			return null;
		}
		return idField.getConcreteField();
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
		return addToCache(o, null);
	}
	
	protected boolean updatePersisted(Object o)
	{
		if (o instanceof Persisted)
		{
			Persisted persisted = (Persisted)o;
			persisted.setPersistedClass(this);
			return true;
		}
		
		return false;
	}
	
	protected CachedObject addToCache(Object o, Object concreteId)
	{
		// First, make sure any Persisted class data is up to date
		updatePersisted(o);
		
		// Handle autogen ids
		// 0 (and lower) are "magic numbers" signifying that there is no auto
		// id set, and we need to generate one.
		boolean autogenerate = idField.isAutogenerated();
		Object id = null;
		if (!autogenerate)
		{
			id = getId(o);
			if (id != null)
			{
				// Check to see if this object has already been removed, if so
				// un-remove it
				CachedObject removedObject = removedMap.get(id);
				if (removedObject != null)
				{
					removedMap.remove(id);
				}
			}
			if (concreteId == null)
			{
				concreteId = getIdData(o);
			}
		}
		else
		{
			// TODO: This is not the place for this- find a better place to set up the auto id!
			// Addendum: maybe? I don't like the magic number thing, but cache add _is_ kind
			// of the place for this..
			
			id = getId(o);
			Long intValue = null;
			
			// Think I'm going to recommend, but not _enforce_ using a Long for an auto int id...
			boolean usingLong = id instanceof Long;
			
			if (usingLong)
			{
				intValue = (Long)id;
			}
			else
			{
				intValue = (Long)DataType.convertValue(id, Long.class);
			}
			if (intValue == null || intValue <= 0)
			{
				intValue = (Long)maxId++;
				if (usingLong)
				{
					id = intValue;
				}
				else
				{
					id = DataType.convertValue(intValue, idField.getType());
				}
			}
			concreteId = id;
			idField.set(o, id);
		}

		if (id == null && concreteId == null)
		{
			return null;
		}

		CachedObject cached = new CachedObject(o);
		if (id != null)
		{
			cacheMap.put(id, cached);
		}
		if (concreteId != null)
		{
			concreteIdMap.put(concreteId, cached);
		}
		
		return cached;
	}
	
	protected void removeFromCache(Object id)
	{
		CachedObject co = cacheMap.get(id);
		if (co == null)
		{
			return;
		}
		
		if (cacheMap.containsKey(id))
		{
			cacheMap.remove(id);
		}
		if (concreteIdMap.containsKey(id))
		{
			concreteIdMap.remove(id);
		}
		removedMap.put(id, co);
	}
	
	public Server getServer()
	{
		return server;
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
	
	protected boolean						dirty				= false;
	protected LoadState						loadState			= LoadState.UNLOADED;

	protected boolean						cacheObjects		= false;
	protected long							maxId				= 1;

	protected Map<Object, CachedObject>		cacheMap			= new ConcurrentHashMap<Object, CachedObject>();
	protected Map<Object, CachedObject>		concreteIdMap		= new ConcurrentHashMap<Object, CachedObject>();
	protected Map<Object, CachedObject>		removedMap			= new ConcurrentHashMap<Object, CachedObject>();

	protected Class<? extends Object>		persistClass		= null;
	protected Server						server				= null;

	// TODO: Make sure these are ok non-concurrent? Should never be writing to these after startup!
	protected List<PersistedField>			fields				= new ArrayList<PersistedField>();
	protected List<PersistedField>			internalFields		= new ArrayList<PersistedField>();
	protected List<PersistedList>			externalFields		= new ArrayList<PersistedList>();
	protected List<PersistedReference>		referenceFields		= new ArrayList<PersistedReference>();

	protected PersistedField 				idField 			= null;
	protected PersistedField				container 			= null;
	protected boolean						contained			= false;

	protected EntityInfo					entityInfo			= null;
	protected MigrationInfo					migrationInfo		= null;

	protected Schema						schema	 			= null;
	protected String						schemaName 			= null;
	protected String						name 				= null;

	protected static Logger					log					= PersistencePlugin.getLogger();
}
