package com.elmakers.mine.bukkit.plugins.spells;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.utilities.UndoableBlock;

public class BlockList 
{
	private final List<UndoableBlock> blocks = new ArrayList<UndoableBlock>();
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
		blocks.add(undoBlock);
		return undoBlock;
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
		for (UndoableBlock undo : blocks)
		{
			if (undo.getBlock() == block)
			{
				return true;
			}
		}
		return false;
	}
}
