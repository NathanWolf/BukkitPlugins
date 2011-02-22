package com.elmakers.mine.bukkit.plugins.nether;

import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

public class NetherPlayerListener extends PlayerListener
{
	public NetherPlayerListener(NetherManager m)
	{
		manager = m;
	}

	@Override
	public void onPlayerMove(PlayerMoveEvent event)
	{
		manager.onPlayerMove(event.getPlayer());
	}
	
	protected NetherManager manager;
}
