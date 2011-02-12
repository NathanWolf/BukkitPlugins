package com.elmakers.mine.bukkit.plugins.nether.dao;

import org.bukkit.util.BlockVector;

import com.elmakers.mine.bukkit.plugins.nether.NetherManager;
import com.elmakers.mine.bukkit.plugins.persistence.Persistence;
import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistClass;
import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistField;
import com.elmakers.mine.bukkit.plugins.persistence.dao.WorldData;

@PersistClass(schema="nether", name="world")
public class NetherWorld
{
	public NetherWorld()
	{
		
	}
	
	public NetherWorld(WorldData world)
	{
		this.world = world;
		targetArea = null;
		
		targetOffset = NetherManager.origin;
		centerOffset = NetherManager.origin;	
	}
	
	public void autoBind(NetherWorld currentWorld)
	{
		if (targetWorld != null) return;
		
		Persistence persistence = Persistence.getInstance();
		
		if (currentWorld.targetWorld == null)
		{
			currentWorld.targetWorld = this;
			targetWorld = currentWorld;
		}
		else
		{
			targetWorld = currentWorld.targetWorld;
			currentWorld.targetWorld = this;
		}
		
		// Save changes to current world target
		persistence.put(currentWorld);
	}
	
	@PersistField(id=true)
	public WorldData getWorld()
	{
		return world;
	}
	
	public void setWorld(WorldData world)
	{
		this.world = world;
	}
	
	@PersistField
	public PortalArea getTargetArea()
	{
		return targetArea;
	}

	public void setTargetArea(PortalArea targetArea)
	{
		this.targetArea = targetArea;
	}

	@PersistField
	public NetherWorld getTargetWorld()
	{
		return targetWorld;
	}
	
	public void setTargetWorld(NetherWorld targetWorld)
	{
		this.targetWorld = targetWorld;
	}
	
	@PersistField
	public BlockVector getTargetOffset()
	{
		return targetOffset;
	}
	
	public void setTargetOffset(BlockVector targetOffset)
	{
		this.targetOffset = targetOffset;
	}
	
	@PersistField
	public BlockVector getCenterOffset()
	{
		return centerOffset;
	}
	
	public void setCenterOffset(BlockVector centerOffset)
	{
		this.centerOffset = centerOffset;
	}
	
	@PersistField
	public double getScale()
	{
		return scale;
	}
	
	public void setScale(double scale)
	{
		this.scale = scale;
	}
	
	protected WorldData 	world;
	protected NetherWorld	targetWorld;
	protected PortalArea	targetArea;
	protected BlockVector	targetOffset;
	protected BlockVector	centerOffset;
	protected double		scale;
}
