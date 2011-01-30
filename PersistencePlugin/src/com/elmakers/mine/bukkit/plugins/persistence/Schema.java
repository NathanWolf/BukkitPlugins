package com.elmakers.mine.bukkit.plugins.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Schema
{	
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
	
	private String name;
	private final List<PersistedClass> persistedClasses = new ArrayList<PersistedClass>();
	private final HashMap<String, PersistedClass> nameMap = new HashMap<String, PersistedClass>();
}
