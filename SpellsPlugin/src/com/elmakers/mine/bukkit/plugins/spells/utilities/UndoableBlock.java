package com.elmakers.mine.bukkit.plugins.spells.utilities;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class UndoableBlock
{	
	private World world;
	private int x;
	private int y;
	private int z;
	private final byte originalData;
	private final Material originalMaterial;
	
	public long getHash()
	{
		long hash = (long)x | ((long)z << 24) | (long)y << 48;
		return hash;
 	}
	
	public UndoableBlock(UndoableBlock ub)
	{
		world = ub.world;
		Block b = world.getBlockAt(x, y, z);
		x = b.getX();
		y = b.getY();
		z = b.getZ();
		originalData = b.getData();
		originalMaterial = b.getType();
	}
	
	public Block getBlock()
	{
		return world.getBlockAt(x, y, z);
	}
	
	public UndoableBlock(Block b)
	{
		world = b.getWorld();
		x = b.getX();
		y = b.getY();
		z = b.getZ();
		originalData = b.getData();
		originalMaterial = b.getType();
	}

	public void undo()
	{
		Block block = world.getBlockAt(x, y, z);
		if (block.getType() != originalMaterial || block.getData() != originalData)
		{
			block.setType(originalMaterial);
			block.setData(originalData);
		}
	}
}