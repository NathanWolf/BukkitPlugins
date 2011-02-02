package com.elmakers.mine.bukkit.plugins.persistence.core;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;

import com.elmakers.mine.bukkit.plugins.persistence.Persistence;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PlayerData;

public class PersistenceListener extends PlayerListener
{
	public void initialize(Persistence persistence)
	{
		this.persistence = persistence;
	}
	
	@Override
	public void onPlayerJoin(PlayerEvent event)
	{
		Player player = event.getPlayer();
		String playerName = player.getName();
		PlayerData playerData = persistence.get(playerName, PlayerData.class);
		if (playerData == null)
		{
			playerData = new PlayerData(player);
		}
		playerData.update(player);
		persistence.put(playerData);
		persistence.save();
	}

	@Override
	public void onPlayerQuit(PlayerEvent event)
	{
		Player player = event.getPlayer();
		String playerName = player.getName();
		PlayerData playerData = persistence.get(playerName, PlayerData.class);
		if (playerData != null)
		{
			playerData.disconnect(player);
			persistence.put(playerData);
			persistence.save();
		}
	}
	
	private Persistence persistence;
}
