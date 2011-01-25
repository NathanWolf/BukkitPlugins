package com.elmakers.mine.bukkit.plugins.spells.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.block.Block;


public class BlockList 
{
	private final List<UndoableBlock> blocks = new ArrayList<UndoableBlock>();
	private final HashMap<Block, UndoableBlock> blockLookup = new HashMap<Block, UndoableBlock>();
	private int timeToLive = 0;
	private int timeRemaining = 0;
	private int passesRemaining = 1;

	public BlockList()
	{
	}
	
	public BlockList(BlockList other)
	{
		for (UndoableBlock block : other.blocks)
		{
			UndoableBlock newBlock = new UndoableBlock(block);
			blocks.add(newBlock);
			blockLookup.put(block.getBlock(), newBlock);
		}
		timeToLive = other.timeToLive;
	}
	
	public int getCount()
	{
		return blocks.size();
	}
		
	public void setTimeToLive(int ttl)
	{
		timeToLive = ttl;
		timeRemaining = ttl;
	}
	
	public void setRepetitions(int repeat)
	{
		passesRemaining = repeat;
	}
	
	public boolean age(int t)
	{
		boolean triggered = false;
		timeRemaining -= t;
		if (isExpired())
		{
			passesRemaining--;
			if (passesRemaining > 0)
			{
				timeRemaining = timeToLive;
			}
			triggered = true;
		}
		return triggered;
	}
	
	public boolean isExpired()
	{
		return timeRemaining <= 0;
	}
	
	public UndoableBlock addBlock(Block block)
	{
		UndoableBlock searchBlock = blockLookup.get(block);
		
		if (searchBlock == null)
		{
			searchBlock = new UndoableBlock(block);
			blocks.add(searchBlock);
			blockLookup.put(block, searchBlock);
		}
		return searchBlock;
	}
	
	public void undo()
	{
		for (UndoableBlock block : blocks)
		{
			block.undo();
		}
	}

	public boolean contains(Block block)
	{
		return (blockLookup.get(block) != null);
	}
	
	public List<UndoableBlock> getBlocks()
	{
		return blocks;
	}
}
