package com.elmakers.mine.bukkit.plugins.persistence.dao;

import com.elmakers.mine.bukkit.plugins.persistence.annotation.Persist;
import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistClass;

@PersistClass(schema="global", name="position", contained=true)
public class PositionData
{
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
