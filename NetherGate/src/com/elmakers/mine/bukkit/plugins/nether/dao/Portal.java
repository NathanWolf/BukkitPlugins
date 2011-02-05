package com.elmakers.mine.bukkit.plugins.nether.dao;

import com.elmakers.mine.bukkit.plugins.persistence.annotation.Persist;
import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistClass;
import com.elmakers.mine.bukkit.plugins.persistence.dao.BoundingBox;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PlayerData;

@PersistClass(schema="nether", name="portal")
public class Portal
{
	@Persist(id=true, auto=true)
	public int getId()
	{
		return id;
	}
	
	public void setId(int id)
	{
		this.id = id;
	}
	
	@Persist
	public Nether getNether()
	{
		return nether;
	}
	
	public void setNether(Nether nether)
	{
		this.nether = nether;
	}
	
	@Persist
	public Portal getTarget()
	{
		return target;
	}
	
	public void setTarget(Portal target)
	{
		this.target = target;
	}
	
	@Persist(contained=true)
	public BoundingBox getArea()
	{
		return area;
	}
	
	public void setArea(BoundingBox area)
	{
		this.area = area;
	}

	@Persist
	public PlayerData getOwner()
	{
		return owner;
	}

	public void setOwner(PlayerData owner)
	{
		this.owner = owner;
	}

	protected int			id;
	protected Nether		nether;
	protected Portal		target;
	protected BoundingBox	area;
	protected PlayerData	owner;
}
