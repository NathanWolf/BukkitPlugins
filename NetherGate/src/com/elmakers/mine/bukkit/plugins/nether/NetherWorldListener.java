package com.elmakers.mine.bukkit.plugins.nether;

import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldListener;

public class NetherWorldListener extends WorldListener
{
	public NetherWorldListener(NetherManager m)
	{
		manager = m;
	}
	
	@Override
	public void onChunkLoaded(ChunkLoadEvent event)
	{
		manager.onChunkLoaded(event.getChunk());
	}
	
	protected NetherManager manager;
}
