package com.elmakers.mine.bukkit.plugins.nether.listener;

import java.util.List;

import org.bukkit.block.Block;

public interface BlockRequestListener
{
	public void onBlockListLoaded(List<Block> blocks);
}
