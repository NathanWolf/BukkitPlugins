package com.elmakers.mine.bukkit.plugins.crowd.dao;

import java.util.List;

import com.elmakers.mine.bukkit.persistence.annotation.PersistClass;
import com.elmakers.mine.bukkit.persistence.annotation.PersistField;
import com.elmakers.mine.bukkit.persistence.dao.WorldData;

@PersistClass(schema="crowd", name="world")
public class ControlledWorld
{
	public ControlledWorld()
	{
		
	}
	
	public ControlledWorld(WorldData world)
	{
		this.id = world;
	}
	
	@PersistField(id=true)
	public WorldData getId()
	{
		return id;
	}

	public void setId(WorldData id)
	{
		this.id = id;
	}

	@PersistField(contained=true)
	public List<ControlRule> getRules()
	{
		return rules;
	}

	public void setRules(List<ControlRule> rules)
	{
		this.rules = rules;
	}
	
	protected WorldData			id;
	protected List<ControlRule>	rules;
}
	