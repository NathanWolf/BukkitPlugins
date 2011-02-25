package com.elmakers.mine.craftbukkit.persistence.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.elmakers.mine.craftbukkit.persistence.data.DataStore;

/**
 * Describes a schema.
 * 
 * This can be used to retrieve all persisted classes in a schema.
 * 
 * @author NathanWolf
 *
 */
public class Schema
{	
	public Schema(String name, DataStore defaultStore)
	{
		this.name = name;
		this.defaultStore = defaultStore;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public void addPersistedClass(PersistedClass persistedClass)
	{
		persistedClasses.add(persistedClass);
		nameMap.put(persistedClass.getTableName(), persistedClass);
	}
	
	public List<PersistedClass> getPersistedClasses()
	{
		return persistedClasses;
	}
	
	public PersistedClass getPersistedClass(String className)
	{
		return nameMap.get(className);
	}
	
	public DataStore getStore()
	{
		return defaultStore;
	}
	
	public void disconnect()
	{
		if (defaultStore != null)
		{
			defaultStore.disconnect();
		}
	}

	private String									name;
	private DataStore								defaultStore;
	private final List<PersistedClass>				persistedClasses	= new ArrayList<PersistedClass>();
	private final HashMap<String, PersistedClass>	nameMap				= new HashMap<String, PersistedClass>();
}
