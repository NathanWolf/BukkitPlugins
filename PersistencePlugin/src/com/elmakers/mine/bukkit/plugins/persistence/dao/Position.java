package com.elmakers.mine.bukkit.plugins.persistence.dao;

import org.bukkit.Location;

import com.elmakers.mine.bukkit.plugins.persistence.annotation.Persist;
import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistClass;

@PersistClass(schema="global", name="position", contained=true)
public class Position
{
	public Position()
	{
		
	}
	
	public Position(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Position(Location location)
	{
		x = location.getBlockX();
		y = location.getBlockY();
		z = location.getBlockZ();
	}
	
	@Persist
	public int getX()
	{
		return x;
	}
	
	public void setX(int x)
	{
		this.x = x;
	}
	
	@Persist
	public int getY()
	{
		return y;
	}
	
	public void setY(int y)
	{
		this.y = y;
	}
	
	@Persist
	public int getZ()
	{
		return z;
	}
	
	public void setZ(int z)
	{
		this.z = z;
	}
	
	protected int x;
	protected int y;
	protected int z;
}
