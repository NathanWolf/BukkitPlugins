package com.elmakers.mine.bukkit.plugins.nether.dao;

import java.sql.Date;

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

	@Persist
	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

	@Persist
	public boolean isUpdatePending()
	{
		return updatePending;
	}

	public void setUpdatePending(boolean updatePending)
	{
		this.updatePending = updatePending;
	}

	@Persist
	public Date getLastUsed()
	{
		return lastUsed;
	}

	public void setLastUsed(Date lastUsed)
	{
		this.lastUsed = lastUsed;
	}
	
	protected int			id;
	protected boolean		active = false;
	protected boolean		updatePending = false;
	protected Date			lastUsed;
	protected Nether		nether;
	protected Portal		target;
	protected BoundingBox	area;
	protected PlayerData	owner;
}
