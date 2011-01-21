package com.elmakers.mine.bukkit.plugins.spells.utilities;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;

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
	
	public UndoableBlock(Block b)
	{
		world = b.getWorld();
		x = b.getX();
		y = b.getY();
		z = b.getZ();
		originalData = b.getData();
		originalMaterial = b.getType();
	}
	
	public void update()
	{
		CraftWorld craftWorld = (CraftWorld)world;
		craftWorld.updateBlock(x, y, z);
	}
	
	public void undo()
	{
		Block block = world.getBlockAt(x, y, z);
		if (block.getType() != originalMaterial || block.getData() != originalData)
		{
			block.setType(originalMaterial);
			block.setData(originalData);
			update();
		}
	}
}