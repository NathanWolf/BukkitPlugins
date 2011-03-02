package com.elmakers.mine.bukkit.plugins.nether;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

import com.elmakers.mine.bukkit.plugins.nether.dao.NetherPlayer;

public class NetherEntityListener extends EntityListener
{
	@Override
	public void onEntityDamage(EntityDamageEvent event)
	{
		if (event.getEntity() instanceof Player)
		{
			Player player = (Player)event.getEntity();
			NetherPlayer playerData = manager.getPlayerData(player);
			if (playerData != null && playerData.areShieldsUp())
			{
				event.setCancelled(true);
			}
		}
	}

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
