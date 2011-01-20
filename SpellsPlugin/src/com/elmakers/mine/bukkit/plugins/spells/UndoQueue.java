package com.elmakers.mine.bukkit.plugins.spells;

import java.util.LinkedList;

public class UndoQueue
{
	private final LinkedList<BlockList> blockQueue = new LinkedList<BlockList>();
	private int maxSize = 0;
	
	public void add(BlockList blocks)
	{
		if (maxSize > 0 && blockQueue.size() > maxSize)
		{
			blockQueue.removeFirst();
		}
		blockQueue.add(blocks);
	}
	
	public boolean undo()
	{
		if (blockQueue.size() == 0) return false;
		
		BlockList blocks = blockQueue.removeLast();
		blocks.undo();
		return true;
	}
	
	public void setMaxSize(int size)
	{
		maxSize = size;
	}
}
