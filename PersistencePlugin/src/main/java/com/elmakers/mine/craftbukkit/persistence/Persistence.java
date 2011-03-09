package com.elmakers.mine.craftbukkit.persistence;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockVector;

import com.elmakers.mine.bukkit.persistence.EntityInfo;
import com.elmakers.mine.bukkit.persistence.FieldInfo;
import com.elmakers.mine.bukkit.persistence.MigrationInfo;
import com.elmakers.mine.bukkit.persistence.annotation.Migrate;
import com.elmakers.mine.bukkit.persistence.annotation.PersistClass;
import com.elmakers.mine.bukkit.persistence.dao.CommandSenderData;
import com.elmakers.mine.bukkit.persistence.exception.InvalidPersistedClassException;
import com.elmakers.mine.bukkit.plugins.persistence.PersistencePlugin;
import com.elmakers.mine.bukkit.utilities.PluginUtilities;
import com.elmakers.mine.craftbukkit.persistence.core.PersistedClass;
import com.elmakers.mine.craftbukkit.persistence.core.Schema;
import com.elmakers.mine.craftbukkit.persistence.data.DataStore;
import com.elmakers.mine.craftbukkit.persistence.data.sql.SqlLiteStore;

/** 
 * The main Persistence interface.
 * 
 * This class is a singleton- use Persistence.getInstance or PersistencePlugin.getPersistence
 * to retrieve the instance.
 * 
 * @author NathanWolf
 */
public class Persistence implements com.elmakers.mine.bukkit.persistence.Persistence
{	
	/**
	 *  Persistence is a singleton, so we hide the constructor.
	 *  
	 *  Use PersistencePlugin.getInstance to retrieve a reference to Persistence safely.
	 *  
	 *  @see PersistencePlugin#getPersistence()
	 *  @see Persistence#getInstance()
	 */
	protected Persistence()
	{
		this.server = PersistencePlugin.getInstance().getServer();
	}
	
	/**
	 * Retrieves a PluginUtilities interface for the specified plugin. 
	 * 
	 * Pass in your own plugin instance for access to data-driven in-game message strings and commands,
	 * and other useful utilities.
	 * 
	 * @param plugin The plugin for which to retrieve messages and commands
	 * @return A PluginUtilities instance for sending messages and processing commands
	 */
	public PluginUtilities getUtilities(Plugin plugin)
	{
		PluginUtilities utilities = new PluginUtilities(plugin, this);
		// TODO: This should be temporary...
		utilities.loadPermissions(PersistencePlugin.getInstance().getPermissions());
		return utilities;
	}
	
	/**
	 * Retrieve the Logger that Persistence uses for debug messages and errors.
	 * 
	 * Currently, this is hard-coded to the Minecraft server logger.
	 * 
	 * @return A Logger that can be used for errors or debugging.
	 */
	public static Logger getLogger()
	{
		return log;
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
		PersistedClass persistedClass = null;
		try
		{
			persistedClass = getPersistedClass(objectType);
		}
		catch (InvalidPersistedClassException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (persistedClass == null)
		{
			return;
		}
		
		persistedClass.getAll(objects);	
	}
	
	/**
	 * Remove an object from the cache (and data store on save)
	 * 
	 * @param removeObject The object to remove
	 */
	public void remove(Object removeObject)
	{
		PersistedClass persistedClass = null;
		try
		{
			persistedClass = getPersistedClass(removeObject.getClass());
		}
		catch (InvalidPersistedClassException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (persistedClass == null)
		{
			return;
		}
		
		persistedClass.remove(removeObject);
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
		PersistedClass persistedClass = null;
		try
		{
			persistedClass = getPersistedClass(objectType);
		}
		catch (InvalidPersistedClassException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (persistedClass == null)
		{
			return;
		}
		
		persistedClass.putAll(objects);	
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
		PersistedClass persistedClass = null;
		try
		{
			persistedClass = getPersistedClass(objectType);
		}
		catch (InvalidPersistedClassException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (persistedClass == null)
		{
			return null;
		}
		
		Object result = persistedClass.get(id);
		if (result == null) return null;
		return (T)result;	
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
		if (persist == null) return false;
		
		PersistedClass persistedClass = null;
		try
		{
			persistedClass = getPersistedClass(persist.getClass());
		}
		catch (InvalidPersistedClassException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (persistedClass == null)
		{
			return false;
		}
		
		persistedClass.put(persist);

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
		for (PersistedClass persistedClass : persistedClassMap.values())
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
		persistedClassMap.clear();
		schemaMap.clear();
	}
	
	/**
	 * Return the singleton instance for Persistence.
	 * 
	 * Will create the instance if it does not exist, but most likely the PersistencePlugin has done this already.
	 * 
	 * It is more adviseable to use PersistencePlugin.getPersistence() so that Persistence gets initialized properly
	 * 
	 * @return the Persistence singleton instance
	 * @see PersistencePlugin#getPersistence()
	 */
	public static Persistence getInstance()
	{
		synchronized(instanceLock)
		{
			if (instance == null)
			{
				instance = new Persistence();
				instance.initialize
				(
					PersistencePlugin.getInstance().getDataFolder(),
					PersistencePlugin.getInstance().getServer()
				);
			}
		}
		return instance;
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
		schemaList.addAll(schemaMap.values());
		return schemaList;
	}
	
	/**
	 * Retrieve or create a persisted class, using the annotations built into the class.
	 * 
	 * @param persistClass The annotated Class to persist
	 * @return The persisted class definition, or null if failure
	 */
	public PersistedClass getPersistedClass(Class<? extends Object> persistClass)
		throws InvalidPersistedClassException
	{		
		/*
		 * Look for Class annotations
		 */
		
		// TODO: Lookup from schema/name map ... hm... uh, how to do this without the annotation?
		// I guess pass in one, and then other persisted classes can request data from their own schema...
		PersistedClass persistedClass = persistedClassMap.get(persistClass);
		if (persistedClass == null)
		{
			PersistClass entityAnnotation = persistClass.getAnnotation(PersistClass.class);
			Migrate migrationAnnotation = persistClass.getAnnotation(Migrate.class);
			
			if (entityAnnotation == null)
			{
				throw new InvalidPersistedClassException(persistClass, "Class does not have the @PersistClass annotation");
			}

			persistedClass = getPersistedClass(persistClass, new EntityInfo(entityAnnotation));
			
			if (migrationAnnotation != null)
			{
				persistedClass.setMigrationInfo(new MigrationInfo(persistedClass, migrationAnnotation));
			}
		
		}

		return persistedClass;
	}
	
	protected PersistedClass createPersistedClass(Class<? extends Object> persistType, EntityInfo entityInfo) throws InvalidPersistedClassException
	{
		PersistedClass persistedClass = new PersistedClass(entityInfo, server);
		if (!persistedClass.bind(persistType))
		{
			return null;
		}
		String schemaName = persistedClass.getSchemaName();
		Schema schema = getSchema(schemaName);
		if (schema == null)
		{
			schema = createSchema(schemaName);
		}
		schema.addPersistedClass(persistedClass);
		persistedClass.setSchema(schema);
		
		persistedClassMap.put(persistType, persistedClass);
		
		// Deferred bind refernces- to avoid circular reference issues
		persistedClass.bindReferences();
		
		return persistedClass;
	}
	
	protected Schema createSchema(String schemaName)
	{
		Schema schema = schemaMap.get(schemaName);
		if (schema == null)
		{
			schemaName = schemaName.toLowerCase();
			DataStore store = createStore();
			store.initialize(schemaName, this);
			schema = new Schema(schemaName, store);
			schemaMap.put(schemaName, schema);
		}
		return schema;
	}
	
	/**
	 * Retrieve or create a persisted class definition for a given class type.
	 * 
	 * This can be used to create a persisted class based on a existing class.
	 * 
	 * @param persistType The Class to persist
	 * @param entityInfo Information on how to persist this class 
	 * @return The persisted class definition, or null if failure
	 */
	public PersistedClass getPersistedClass(Class<? extends Object> persistType, EntityInfo entityInfo)
	{	
		PersistedClass persistedClass = persistedClassMap.get(persistType);
		if (persistedClass == null)
		{
			// Lock now, to create an atomic checkCreate for class:
			synchronized(classCreateLock)
			{
				persistedClass = persistedClassMap.get(persistType);
				if (persistedClass == null)
				{
					try
					{
						persistedClass = createPersistedClass(persistType, entityInfo);
					}
					catch (InvalidPersistedClassException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return persistedClass;
	}
	
	/*
	 * Protected members
	 */
	
	protected DataStore createStore()
	{
		// Only SqlLite supported for now!
		// TODO : Support MySQL
		SqlLiteStore store = new SqlLiteStore();
		store.setDataFolder(dataFolder);
		return store;
	}
	
	protected void initialize(File dataFolder, Server server)
	{
		this.dataFolder = dataFolder;
		dataFolder.mkdirs();

		updateGlobalData();
		
		// TODO : load configuration, sql connection params, etc?
	}
	
	protected void updateGlobalData()
	{
		// Update CommandSenders
		updateCommandSender("player" , Player.class);
		
		// Create BlockVector class
		EntityInfo vectorInfo = new EntityInfo("global", "vector");
		FieldInfo vectorId = new FieldInfo("id");
		FieldInfo fieldX = new FieldInfo("x");
		FieldInfo fieldY = new FieldInfo("y");
		FieldInfo fieldZ = new FieldInfo("z");
		
		// Make the hash code the id, make it readonly, and override its storage name
		vectorId.setIdField(true);
		vectorId.setReadOnly(true);
	
		// Bind each field- this is a little awkward right now, due to the
		// assymmetry (lack of setBlockX type setters).
		fieldX.setGetter("getBlockX");
		fieldY.setGetter("getBlockY");
		fieldZ.setGetter("getBlockZ");
		fieldX.setSetter("setX");
		fieldY.setSetter("setY");
		fieldZ.setSetter("setZ");
		
		// Create the class definition
		PersistedClass persistVector = getPersistedClass(BlockVector.class, vectorInfo);
		try
		{
			persistVector.persistField("hashCode", vectorId);
	
			persistVector.persistField("x", fieldX);
			persistVector.persistField("y", fieldY);
			persistVector.persistField("z", fieldZ);
			
			persistVector.validate();
		}
		catch (InvalidPersistedClassException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO: Materials (? .. currently in Gameplay!)
	}
		

	protected CommandSenderData updateCommandSender(String senderId, Class<?> senderClass)
	{
		CommandSenderData sender = get(senderId, CommandSenderData.class);
		if (sender == null)
		{
			sender = new CommandSenderData(senderId, senderClass);
			put(sender);
		}		
		return sender;
	}
	
	public void disconnect()
	{
		for (Schema schema : schemaMap.values())
		{
			schema.disconnect();
		}
	}
	
	public static boolean getOpsCanSU()
	{
		return allowOpsSUAccess;
	}

	public static void setOpsCanSU(boolean allow)
	{
		allowOpsSUAccess = allow;
	}
	
	/*
	 * private data
	 */
	
	private File dataFolder = null;
	
	private static boolean allowOpsSUAccess = true;
	
	private static final Logger log = Logger.getLogger("Minecraft");
	
	private final Map<Class<? extends Object>, PersistedClass> persistedClassMap = new ConcurrentHashMap<Class<? extends Object>, PersistedClass>(); 
	private final Map<String, Schema> schemaMap = new ConcurrentHashMap<String, Schema>();
	
	private static Persistence instance = null;
	private Server server;
	
	// Locks for manual synchronization
	
	// Make sure that we don't create a persisted class twice at the same time
	private static final Object classCreateLock = new Object();
	
	// Make sure we don't create more than one instance of Persistence
	private static final Object instanceLock = new Object();
	
}
