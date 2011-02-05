package com.elmakers.mine.bukkit.plugins.persistence.dao;

import com.elmakers.mine.bukkit.plugins.persistence.annotation.Persist;
import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistClass;

@PersistClass(schema="global", name="area", contained=true)
public class BoundingBox
{
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
