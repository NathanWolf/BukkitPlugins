package com.elmakers.mine.bukkit.plugins.nether.dao;

import java.util.Date;

import com.elmakers.mine.bukkit.persistence.annotation.PersistClass;
import com.elmakers.mine.bukkit.persistence.annotation.PersistField;
import com.elmakers.mine.bukkit.persistence.dao.LocationData;

@PersistClass(schema="nether", name="portal")
public class Portal
{
	@PersistField(id=true, auto=true)
	public int getId()
	{
		return id;
	}
	
	public void setId(int id)
	{
		this.id = id;
	}
	
	@PersistField
	public PortalArea getContainer()
	{
		return container;
	}
	
	public void setContainer(PortalArea container)
	{
		this.container = container;
	}
	
	@PersistField
	public Portal getTarget()
	{
		return target;
	}
	
	public void setTarget(Portal target)
	{
		this.target = target;
	}

	@PersistField
	public NetherPlayer getCreator()
	{
		return creator;
	}

	public void setCreator(NetherPlayer creator)
	{
		this.creator = creator;
	}

	@PersistField
	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

	@PersistField
	public boolean isUpdatePending()
	{
		return updatePending;
	}

	public void setUpdatePending(boolean updatePending)
	{
		this.updatePending = updatePending;
	}

	@PersistField
	public Date getLastUsed()
	{
		return lastUsed;
	}

	public void setLastUsed(Date lastUsed)
	{
		this.lastUsed = lastUsed;
	}
	
	@PersistField
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@PersistField(contained=true)
	public LocationData getLocation()
	{
		return location;
	}

	public void setLocation(LocationData location)
	{
		this.location = location;
	}

	protected int			id;
	protected LocationData 	location;
	protected PortalArea	container;
	protected String		name;
	protected boolean		active			= false;
	protected boolean		updatePending	= false;
	protected Date			lastUsed;
	protected Portal		target;
	protected NetherPlayer	creator;
}
