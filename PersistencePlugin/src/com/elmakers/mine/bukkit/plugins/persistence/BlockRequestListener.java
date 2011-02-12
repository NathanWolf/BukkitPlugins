package com.elmakers.mine.bukkit.plugins.persistence;

import java.util.List;

import org.bukkit.block.Block;

public interface BlockRequestListener
{
	public void onBlockListLoaded(List<Block> blocks);
}
