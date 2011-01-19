package com.elmakers.mine.bukkit.utilities;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;

public class UndoableBlock
{	
	private Block block;
	private final byte originalData;
	private final Material originalMaterial;
	
	public UndoableBlock(Block b)
	{
		block = b;
		originalData = b.getData();
		originalMaterial = b.getType();
	}
	
	public Block getBlock()
	{
		return block;
	}
	
	public void update()
	{
		CraftWorld world = (CraftWorld)block.getWorld();
		world.updateBlock(block.getX(), block.getY(), block.getZ());
	}
	
	public void undo()
	{
		CraftWorld world = (CraftWorld)block.getWorld();
	
		block = world.getBlockAt(block.getX(), block.getY(), block.getZ());
		if (block.getType() != originalMaterial || block.getData() != originalData)
		{
			block.setType(originalMaterial);
			block.setData(originalData);
			update();
		}
	}
}