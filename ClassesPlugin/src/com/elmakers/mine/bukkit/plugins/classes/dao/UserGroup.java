package com.elmakers.mine.bukkit.plugins.classes.dao;

import com.elmakers.mine.bukkit.plugins.persistence.annotation.Persist;
import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistClass;

@PersistClass(name = "group", schema = "classes") 
public class UserGroup
{
	private String 		id;
	private String 		name;
	private String 		description;
	private UserGroup 	parent;
	
	@Persist(id=true)
	public String getId()
	{
		return id;
	}
	public void setId(String id)
	{
		this.id = id;
	}
	@Persist
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	@Persist
	public String getDescription()
	{
		return description;
	}
	public void setDescription(String description)
	{
		this.description = description;
	}
	@Persist
	public UserGroup getParent()
	{
		return parent;
	}
	public void setParent(UserGroup parent)
	{
		this.parent = parent;
	}
}
