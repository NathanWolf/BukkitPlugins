package com.elmakers.mine.bukkit.plugins.nether;

import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

public class NetherPlayerListener extends PlayerListener
{
	public NetherPlayerListener(NetherManager m)
	{
		manager = m;
	}

	@Override
	public void onPlayerJoin(PlayerEvent event)
	{
		manager.load(event.getPlayer().getWorld());
	}
	
	@Override
	public void onPlayerMove(PlayerMoveEvent event)
	{
		// TODO Auto-generated method stub
		super.onPlayerMove(event);
	}
	
	protected NetherManager manager;
}
