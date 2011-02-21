package com.elmakers.mine.bukkit.plugins.crowd.dao;

import com.elmakers.mine.bukkit.borrowed.CreatureType;
import com.elmakers.mine.bukkit.persistence.annotation.PersistClass;
import com.elmakers.mine.bukkit.persistence.annotation.PersistField;

@PersistClass(schema="crowd", name="rule")
public class ControlRule
{
	public ControlRule()
	{
		
	}
	
	public ControlRule(int order, CreatureType mobType)
	{
		this.rank = order;
		this.creatureType = mobType;
	}
	
	@PersistField(id=true)
	public int getRank()
	{
		return rank;
	}
	
	public void setRank(int rank)
	{
		this.rank = rank;
	}
	
	@PersistField
	public CreatureType getCreatureType()
	{
		return creatureType;
	}
	
	public void setCreatureType(CreatureType creatureType)
	{
		this.creatureType = creatureType;
	}
	
	@PersistField
	public float getPercentChance()
	{
		return percentChance;
	}
	
	public void setPercentChance(float percentChance)
	{
		this.percentChance = percentChance;
	}
	
	@PersistField
	public CreatureType getReplaceWith()
	{
		return replaceWith;
	}
	
	public void setReplaceWith(CreatureType replaceWith)
	{
		this.replaceWith = replaceWith;
	}

	protected int			rank;
	protected CreatureType	creatureType;
	protected float			percentChance;
	protected CreatureType	replaceWith;
}
