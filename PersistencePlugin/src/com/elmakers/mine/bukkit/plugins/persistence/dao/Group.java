package com.elmakers.mine.bukkit.plugins.persistence.dao;

import com.elmakers.mine.bukkit.plugins.persistence.annotations.Persist;
import com.elmakers.mine.bukkit.plugins.persistence.annotations.PersistClass;

/**
 * Represents a group, which can be assigned permissions and contain players and other groups.
 * 
 * @author nathan
 *
 */
@PersistClass(name = "group", schema = "global") 
public class Group
{
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
	public Group getParent()
	{
		return parent;
	}
	
	public void setParent(Group parent)
	{
		this.parent = parent;
	}
	
	private String 		id;
	private String 		name;
	private String 		description;
	private Group 	parent;
}
