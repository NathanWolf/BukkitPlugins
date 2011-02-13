package com.elmakers.mine.bukkit.plugins.nether;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

public class NetherEntityListener extends EntityListener
{
	public NetherEntityListener(NetherManager manager)
	{
		this.manager = manager;
	}

	@Override
	public void onEntityDeath(EntityDeathEvent event)
	{
		if (Player.class.isInstance(event.getEntity()))
		{
			Player player = (Player)event.getEntity();
			manager.onPlayerDeath(player, event);
		}
	}
	
	protected NetherManager manager;

}
