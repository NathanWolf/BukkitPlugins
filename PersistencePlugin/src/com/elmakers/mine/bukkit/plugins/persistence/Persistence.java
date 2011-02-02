package com.elmakers.mine.bukkit.plugins.persistence;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.plugins.persistence.core.PersistedClass;
import com.elmakers.mine.bukkit.plugins.persistence.core.Schema;
import com.elmakers.mine.bukkit.plugins.persistence.stores.PersistenceStore;
import com.elmakers.mine.bukkit.plugins.persistence.stores.SqlLiteStore;

/** 
 * The main Persistence interface.
 * 
 * This class is a singleton- use Persistence.getInstance or PersistencePlugin.getPersistence
 * to retrieve the instance.
 * 
 * @author NathanWolf
 */
public class Persistence
{	
	
	/**
	 * Retrieves a Messaging interface for the specified plugin. 
	 * 
	 * Pass in your own plugin instance for access to data-driven in-game message strings and commands.
	 * 
	 * @param plugin The plugin for which to retrieve messages and commands
	 * @return A Messaging instance for sending messages and processing commands
	 */
	public Messaging getMessaging(Plugin plugin)
	{
		return new Messaging(plugin, this);
	}
	
	/**
	 * Populates a list of all instances of a specified type.
	 * 
	 * This is a parameterized function. It will populate a list with object instances for a given type. An example call:
	 * 
	 * List<MyObject> myInstances = new ArrayList<MyObject>();
	 * persistence.getAll(myInstances, MyObject.class);
	 * 
	 * This would populate the myInstances list with any persisted MyObject instances. You must ensure that your List is of a
	 * compatible type with the objects you are retrieving.
	 * 
	 * @param <T> The base type of object. This is an invisible parameter, you don't need to worry about it
	 * @param objects A List (needs not be empty) to populate with object instances
	 * @param objectType The type of object to retrieve
	 */
	public <T> void getAll(List<T> objects, Class<T> objectType)
	{	
		synchronized(cacheLock)
		{
			PersistedClass persistedClass = getPersistedClass(objectType);
			if (persistedClass == null)
			{
				return;
			}
			
			persistedClass.getAll(objects);	
		}
	}
	
	/**
	 * Merge a list of objects into the data store.
	 * 
	 * Use this method to completely replace the stored entity list for a type of entity. Entities not in the "objects" list will
	 * be deleted, new objects will be added, and existing objects merged.
	 * 
	 * This is a parameterized function. It will populate a list with object instances for a given type. An example call:
	 * 
	 * List<MyObject> myInstances = new ArrayList<MyObject>();
	 * ... Fill myInstances with some data
	 * persistence.putAll(myInstances, MyObject.class);
	 * 
	 * This would replace all instances of MyObject with the instances in the myInstances list.
	 * 
	 * TODO: Currently, this method replaces all of the instances directly. This would
	 * invalidate any externally maintained references, so it needs to merge data instead.
	 * 
	 * @param <T> The base type of object. This is an invisible parameter, you don't need to worry about it
	 * @param objects A list of objects to store
	 * @param objectType The type of object to replace instances
	 */
	public <T> void putAll(List<T> objects, Class<T> objectType)
	{
		synchronized(cacheLock)
		{
			PersistedClass persistedClass = getPersistedClass(objectType);
			if (persistedClass == null)
			{
				return;
			}
			
			persistedClass.putAll(objects);	
		}
	}
	
	/**
	 * Retrieve an instance of the specified type.
	 * 
	 * This method retrieves an object instance from the data store, based on the object's id.
	 * The id passed in should match the type of this object's id field- a String for a String id, for instance.
	 * 
	 * If an object with the specified id cannot be found, the method returns null;
	 * 
	 * @param <T> The base type of object. This is an invisible parameter, you don't need to worry about it
	 * @param id The id of the object to lookup
	 * @param objectType The type of object to search for
	 * @return The object instance with the specified id, or null if not found
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(Object id, Class<T> objectType)
	{
		synchronized(cacheLock)
		{
			PersistedClass persistedClass = getPersistedClass(objectType);
			if (persistedClass == null)
			{
				return null;
			}
			
			Object result = persistedClass.get(id);
			if (result == null) return null;
			return (T)result;	
		}
	}
	
	/**
	 * Add an object to the data store.
	 * 
	 * This only adds the object to the cache. At save time, the cached object will trigger a data save.
	 * 
	 * If this is the first instance of this type of object to ever be stored, the schema and tables needed to store this object will be created
	 * at save time. Then, the tables will be populated with this object's data.
	 * 
	 * @param persist The object to persist
	 * @return false if, for some reason, the storage failed.
	 */
	public boolean put(Object persist)
	{
		synchronized(cacheLock)
		{
			PersistedClass persistedClass = getPersistedClass(persist.getClass());
			if (persistedClass == null)
			{
				return false;
			}
			
			persistedClass.put(persist);
		}
		return true;		
	}
	
	
	/**
	 * Force a save of all cached data.
	 * 
	 * This only saves dirty data- unmodified data is not saved back to the database.
	 * Persistence calls save() internally on server shutdown, player login, and player logout. So, calling save is not
	 * mandatory- you only need to use it to force an immediate save.
	 * 
	 */
	public void save()
	{
		for (PersistedClass persistedClass : persistedClasses)
		{
			persistedClass.save();
		}
	}
	
	
	/**
	 * Clear all data.
	 * 
	 * This is currently the method used to clear the cache and reload data, however it is flawed.
	 * It will probably be replaced with a "reload" method eventually.
	 */
	public void clear()
	{
		persistedClasses.clear();
		persistedClassMap.clear();
		schemaMap.clear();
		schemas.clear();
	}
	
	/**
	 * Return the singleton instance for Persistence.
	 * 
	 * Will create the instance if it does not exist, but most likely the PersistencePlugin has done this already.
	 * It is more adviseable to use PersistencePlugin.getPersistence().
	 * 
	 * @return the Persistence singleton instance
	 */
	public static Persistence getInstance()
	{
		synchronized(instanceLock)
		{
			if (instance == null)
			{
				instance = new Persistence();
				instance.initialize(PersistencePlugin.getInstance().getDataFolder());
			}
		}
		return instance;
	}
	
	/**
	 * Retrieve or create the persistence store for a particular schema.
	 * 
	 * Each schema gets its own persistent connection and database schema.
	 * 
	 * This is an internal function that doesn't necessarily need to be called.
	 * 
	 * @param schema The schema name to retrieve
	 * @return The data store for the given schema
	 */
	public PersistenceStore getStore(String schema)
	{
		PersistenceStore store = null;
		synchronized(dataLock)
		{
			schema = schema.toLowerCase();
			store = schemaStores.get(schema);
			if (store == null)
			{
				store = createStore();
				store.initialize(this);
				schemaStores.put(schema, store);
				stores.add(store);
			}
		}
		return store;
	}
	
	/**
	 * Retrieve a Schema definition, with a list of PersistedClasses.
	 * 
	 * This function is used for inspecting schemas and entities.
	 * 
	 * @param schemaName The schema to retrieve
	 * @return A Schema definition class, containing entity classes
	 */
	public Schema getSchema(String schemaName)
	{
		return schemaMap.get(schemaName);
	}
	
	/**
	 * Retrieve a list of definitions for all known schemas.
	 * 
	 * This function is used for inspecting schemas and entities.
	 * 
	 * @return The list of schemas
	 */
	public List<Schema> getSchemaList()
	{
		List<Schema> schemaList = new ArrayList<Schema>();
		schemaList.addAll(schemas);
		return schemaList;
	}
	
	/**
	 * Retrieve or create a persisted class definition for a given class type.
	 * 
	 * This is an internal function that doesn't necessarily need to be called.
	 * 
	 * @param persistType
	 * @return The persisted class definition, or null if failure
	 */
	public PersistedClass getPersistedClass(Class<? extends Object> persistType)
	{	
		PersistedClass persistedClass = persistedClassMap.get(persistType);
		if (persistedClass == null)
		{
			persistedClass = new PersistedClass();
			if (!persistedClass.bind(persistType))
			{
				log.warning("No fields in class '" + persistType.getName() + "', Did you use @Persist?");
				return null;
			}
			String schemaName = persistedClass.getSchema();
			Schema schema = schemaMap.get(schemaName);
			if (schema == null)
			{
				schema = new Schema();
				schema.setName(schemaName);
				schemas.add(schema);
				schemaMap.put(schemaName, schema);
			}
			schema.addPersistedClass(persistedClass);
			persistedClasses.add(persistedClass);
			persistedClassMap.put(persistType, persistedClass);
			
			// Deferred bind refernces- to avoid circular reference issues
			persistedClass.bindReferences();
		}
		return persistedClass;
	}
	
	/*
	 * Protected members
	 */
	
	protected PersistenceStore createStore()
	{
		// Only SqlLite supported for now!
		// TODO : Support MySQL
		SqlLiteStore store = new SqlLiteStore();
		store.setDataFolder(dataFolder);
		return store;
	}
	
	protected void initialize(File dataFolder)
	{
		this.dataFolder = dataFolder;
		dataFolder.mkdirs();

		// TODO : load configuration, sql connection params, etc?
	}
	
	protected void disconnect()
	{
		synchronized(dataLock)
		{
			for (PersistenceStore store : stores)
			{
				store.disconnect();
			}
			stores.clear();
			schemaStores.clear();
		}
	}
	
	/*
	 * private data
	 */
	
	private File dataFolder = null;
	
	private final HashMap<Class<? extends Object>, PersistedClass> persistedClassMap = new HashMap<Class<? extends Object>, PersistedClass>(); 
	private final List<PersistedClass> persistedClasses = new ArrayList<PersistedClass>(); 
	private final List<Schema> schemas = new ArrayList<Schema>();
	private final HashMap<String, Schema> schemaMap = new HashMap<String, Schema>();

	private final Logger log = Logger.getLogger("Minecraft");
	private final HashMap<String, PersistenceStore> schemaStores = new HashMap<String, PersistenceStore>();
	private final List<PersistenceStore> stores = new ArrayList<PersistenceStore>();
	
	private static final Object dataLock = new Object();
	private static final Object instanceLock = new Object();
	private static final Object cacheLock = new Object();
	
	private static Persistence instance = null;
}
