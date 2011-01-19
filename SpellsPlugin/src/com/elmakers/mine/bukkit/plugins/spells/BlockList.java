package com.elmakers.mine.bukkit.plugins.spells;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.Material;

public class BlockList 
{
	public BlockList(Player player)
	{
		this.world = (CraftWorld)player.getWorld();
	}
	
	public BlockList(BlockList other)
	{
		blocks.addAll(other.blocks);
		timeToLive = other.timeToLive;
		world = other.world;
	}
	
	private final CraftWorld world;
	private final List<UndoableBlock> blocks = new ArrayList<UndoableBlock>();
	private int timeToLive = 0;
	
	public void setTimeToLive(int ttl)
	{
		timeToLive = ttl;
	}
	
	public void age(int t)
	{
		timeToLive -= t;
	}
	
	public boolean isExpired()
	{
		return timeToLive <= 0;
	}
	
	public void addBlock(Block block)
	{
		blocks.add(new UndoableBlock(block));
	}
	
	public void undo()
	{
		for (UndoableBlock block : blocks)
		{
			if (block.undo())
			{
				world.updateBlock(block.getBlock().getX(), block.getBlock().getY(), block.getBlock().getZ());
			}
		}
	}

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
		
		public boolean undo()
		{
			if (block.getType() == originalMaterial && block.getData() == originalData)
			{
				return false;
			}
			block.setType(originalMaterial);
			block.setData(originalData);
			return true;
		}
	}

}
