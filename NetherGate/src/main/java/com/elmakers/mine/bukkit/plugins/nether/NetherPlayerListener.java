package com.elmakers.mine.bukkit.plugins.nether;

import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;

public class NetherPlayerListener extends PlayerListener
{
	@Override
	public void onPlayerLogin(PlayerLoginEvent event)
	{
		if (event.getResult() == Result.ALLOWED)
		{
			manager.loadWorlds();
		}
	}

	public NetherPlayerListener(NetherManager manager)
	{
		this.manager = manager;
	} 

	@Override
	public void onPlayerMove(PlayerMoveEvent event)
	{
		manager.onPlayerMove(event.getPlayer());
	}

	protected NetherManager	manager;
}
