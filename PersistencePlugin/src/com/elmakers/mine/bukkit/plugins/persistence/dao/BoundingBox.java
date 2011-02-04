package com.elmakers.mine.bukkit.plugins.persistence.dao;

import com.elmakers.mine.bukkit.plugins.persistence.annotation.Persist;
import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistClass;

@PersistClass(schema="global", name="area", contained=true)
public class BoundingBox
{
	@Persist(contained=true)
	public PositionData getMin()
	{
		return min;
	}
	
	public void setMin(PositionData min)
	{
		this.min = min;
	}
	
	@Persist(contained=true)
	public PositionData getMax()
	{
		return max;
	}
	
	public void setMax(PositionData max)
	{
		this.max = max;
	}
	
	protected PositionData min;
	protected PositionData max;
}
