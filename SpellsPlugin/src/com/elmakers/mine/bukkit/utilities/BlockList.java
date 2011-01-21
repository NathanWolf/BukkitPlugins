package com.elmakers.mine.bukkit.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.block.Block;


public class BlockList 
{
	private final List<UndoableBlock> blocks = new ArrayList<UndoableBlock>();
	private final HashMap<Long, UndoableBlock> blockLookup = new HashMap<Long, UndoableBlock>();
	private int timeToLive = 0;
	private int timeRemaining = 0;
	private int passesRemaining = 1;

	public BlockList()
	{
	}
	
	public BlockList(BlockList other)
	{
		blocks.addAll(other.blocks);
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
		UndoableBlock undoBlock = new UndoableBlock(block);
		UndoableBlock searchBlock = blockLookup.get(undoBlock.getHash());
		
		if (searchBlock == null)
		{
			searchBlock = undoBlock;
			blocks.add(undoBlock);
			blockLookup.put(undoBlock.getHash(), undoBlock);
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
		UndoableBlock undoBlock = new UndoableBlock(block);
		return (blockLookup.get(undoBlock.getHash()) != null);
	}
}
