package com.elmakers.mine.bukkit.plugins.persistence;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import com.elmakers.mine.bukkit.plugins.persistence.stores.PersistenceStore;
import com.elmakers.mine.bukkit.plugins.persistence.stores.SqlLiteStore;

/*
 * This class is a singleton.
 */
public class Persistence
{
	/*
	 * public API
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
	
	public void save()
	{
		for (PersistedClass persistedClass : persistedClasses)
		{
			persistedClass.save();
		}
	}
	
	public void clear()
	{
		persistedClasses.clear();
		persistedClassMap.clear();
	}
	
	/*
	 * Internal functions - do not call
	 */
	
	public void initialize(File dataFolder)
	{
		this.dataFolder = dataFolder;
		dataFolder.mkdirs();

		// TODO : load configuration, sql connection params, etc?
	}
	
	public void disconnect()
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
	
	/*
	 * Protected members
	 */
	
	protected PersistenceStore createStore()
	{
		// Only SqlLite supported for now!
		// TODO : Support Mysql? Flatfile?
		SqlLiteStore store = new SqlLiteStore();
		store.setDataFolder(dataFolder);
		return store;
	}
	
	protected PersistedClass getPersistedClass(Class<? extends Object> persistType)
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
			persistedClasses.add(persistedClass);
			persistedClassMap.put(persistType, persistedClass);
			
			// Deferred bind refernces- to avoid circular reference issues
			persistedClass.bindReferences();
		}
		return persistedClass;
	}
	
	/*
	 * private data
	 */
	
	private File dataFolder = null;
	
	private final HashMap<Class<? extends Object>, PersistedClass> persistedClassMap = new HashMap<Class<? extends Object>, PersistedClass>(); 
	private final List<PersistedClass> persistedClasses = new ArrayList<PersistedClass>(); 
	private final Logger log = Logger.getLogger("Minecraft");
	private final HashMap<String, PersistenceStore> schemaStores = new HashMap<String, PersistenceStore>();
	private final List<PersistenceStore> stores = new ArrayList<PersistenceStore>();
	
	private static final Object dataLock = new Object();
	private static final Object instanceLock = new Object();
	private static final Object cacheLock = new Object();
	
	private static Persistence instance = null;
}
