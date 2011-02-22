package com.elmakers.mine.bukkit.persistence.dao;

import org.bukkit.Location;

import com.elmakers.mine.bukkit.persistence.annotation.PersistClass;
import com.elmakers.mine.bukkit.persistence.annotation.PersistField;

@PersistClass(schema="global", name="orientation", contained=true)
public class Orientation extends Persisted
{
	public Orientation()
	{
		
	}
	
	public Orientation(Location location)
	{
		yaw = location.getYaw();
		pitch = location.getPitch();
	}
	
	public Orientation(float yaw, float pitch)
	{
		this.yaw = yaw;
		this.pitch = pitch;
	}
	
	
	@PersistField
	public float getPitch()
	{
		return pitch;
	}
	
	public void setPitch(float pitch)
	{
		this.pitch = pitch;
	}
	
	@PersistField
	public float getYaw()
	{
		return yaw;
	}
	
	public void setYaw(float yaw)
	{
		this.yaw = yaw;
	}
	
	protected float pitch;
	protected float yaw;
}
