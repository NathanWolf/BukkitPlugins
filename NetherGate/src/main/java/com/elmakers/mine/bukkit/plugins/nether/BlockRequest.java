package com.elmakers.mine.bukkit.plugins.nether;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.gameplay.BlockRequestListener;
import com.elmakers.mine.bukkit.gameplay.dao.BoundingBox;
import com.elmakers.mine.bukkit.plugins.nether.dao.NetherWorld;

public class BlockRequest
{
	
	public BlockRequest(NetherManager manager, BlockVector center, int radius, BlockRequestListener listener)
	{
		this.manager = manager;
		this.center = center;
		this.radius = radius;
		this.requestor = listener;
	}

	public NetherWorld getWorld()
	{
		return world;
	}
	
	public void setWorld(NetherWorld world)
	{
		this.world = world;
	}
	
	public BlockVector getCenter()
	{
		return center;
	}
	
	public void translate(NetherWorld fromWorld)
	{
		BlockVector centerPos = getCenter();
		Vector translated = (Vector)manager.mapLocation(fromWorld, world, centerPos);
		
		center = new BlockVector(translated);
	}

	protected BoundingBox getArea()
	{
		int minX = center.getBlockX() - radius;
		int minY = center.getBlockY() - radius;
		int minZ = center.getBlockZ() - radius;
		int maxX = center.getBlockX() + radius;
		int maxY = center.getBlockY() + radius;
		int maxZ = center.getBlockZ() + radius;

		return new BoundingBox(new BlockVector(minX, minY, minZ), new BlockVector(maxX, maxY, maxZ));
	}	
	
	public void dispatch()
	{
		if (requestor == null) return;
		
		World targetWorld = world.getWorld().getWorld();
		Location location = new Location(targetWorld, center.getBlockX(), center.getBlockY(), center.getBlockZ());
		Location targetLocation = manager.findPlaceToStand(location);
		if (targetLocation != null)
		{
			center = new BlockVector(targetLocation.getBlockX(), targetLocation.getBlockY(), targetLocation.getBlockZ());
		}
		
		center = new BlockVector(center.getBlockX(), center.getBlockY() + radius, center.getBlockZ());
		List<Block> blocks = new ArrayList<Block>();
		BoundingBox area = getArea();
		area.getBlocks(targetWorld, blocks);
		requestor.onBlockListLoaded(blocks);
	}
		
	protected NetherManager		manager;
	protected NetherWorld		world;
	protected BlockVector		center;
	protected int				radius;
	protected BlockRequestListener requestor;
}
