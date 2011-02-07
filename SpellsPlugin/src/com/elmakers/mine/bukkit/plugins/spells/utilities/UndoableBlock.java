package com.elmakers.mine.bukkit.plugins.spells.utilities;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class UndoableBlock
{	
	private World world;
	private int x;
	private int y;
	private int z;
	private byte originalData;
	private Material originalMaterial;
	private Material[] originalSideMaterials = new Material[4];
	private Material originalTopMaterial;
	private byte[] originalSideData = new byte[4];
	private byte originalTopData;	
	public static final BlockFace[] SIDES = new BlockFace[] {BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST};
	
	
	public byte getOriginalData()
	{
		return originalData;
	}
	
	public Material getOriginalMaterial()
	{
		return originalMaterial;
	}
	
	public Material getOriginalSideMaterial(int side)
	{
		if (side > 3 || side < 0) return Material.AIR;
		return originalSideMaterials[side];
	}
	
	public Material getOriginalTopMaterial()
	{
		return originalTopMaterial;
	}
	
	public UndoableBlock(UndoableBlock ub)
	{
		world = ub.world;
		x = ub.x;
		y = ub.y;
		z = ub.z;
		originalData = ub.originalData;
		originalMaterial = ub.originalMaterial;
		originalTopMaterial = ub.originalTopMaterial;
		originalTopData = ub.originalTopData;
		for (int i = 0; i < 4; i++)
		{
			originalSideMaterials[i] = ub.originalSideMaterials[i];
			originalSideData[i] = ub.originalSideData[i];
		}
	}
	
	public void setFromBottom(UndoableBlock bottom)
	{
		originalMaterial = bottom.originalTopMaterial;
		originalData = bottom.originalTopData;
	}
	
	public void setFromSide(UndoableBlock neighbor, int side)
	{
		if (side > 3 || side < 0) return;
		originalMaterial = neighbor.originalSideMaterials[side];
		originalData = neighbor.originalSideData[side];
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
		
		Block topBlock =  b.getFace(BlockFace.UP);
		originalTopMaterial = topBlock.getType();
		originalTopData = topBlock.getData();
		for (int i = 0; i < 4; i++)
		{
			Block side = b.getFace(SIDES[i]);
			originalSideData[i] = side.getData();
			originalSideMaterials[i] = side.getType();
		}
	}

	public boolean undo()
	{
		Block block = world.getBlockAt(x, y, z);
		if (!world.isChunkLoaded(x, z)) return false;
		
		if (block.getType() != originalMaterial || block.getData() != originalData)
		{
			block.setType(originalMaterial);
			block.setData(originalData);
		}
		
		return true;
	}
}