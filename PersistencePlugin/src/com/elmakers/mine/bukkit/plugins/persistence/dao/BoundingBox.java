package com.elmakers.mine.bukkit.plugins.persistence.dao;

import com.elmakers.mine.bukkit.plugins.persistence.annotation.Persist;
import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistClass;

@PersistClass(schema="global", name="area", contained=true)
public class BoundingBox
{
	public BoundingBox()
	{
		
	}
	
	public BoundingBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
	{
		min = new Position(Math.min(minX, maxX), Math.min(minY, maxY), Math.min(minZ, maxZ));
		max = new Position(Math.max(minX, maxX), Math.max(minY, maxY), Math.max(minZ, maxZ));
	}
	
	
	@Persist(contained=true)
	public Position getMin()
	{
		return min;
	}
	
	public void setMin(Position min)
	{
		this.min = min;
	}
	
	@Persist(contained=true)
	public Position getMax()
	{
		return max;
	}
	
	public void setMax(Position max)
	{
		this.max = max;
	}
	
	protected Position min;
	protected Position max;
}
