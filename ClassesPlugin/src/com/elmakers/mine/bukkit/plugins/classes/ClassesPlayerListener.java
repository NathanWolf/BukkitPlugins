package com.elmakers.mine.bukkit.plugins.classes;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;

import com.elmakers.mine.bukkit.plugins.classes.dao.User;
import com.elmakers.mine.bukkit.plugins.persistence.Persistence;

public class ClassesPlayerListener extends PlayerListener
{
	public void setPersistence(Persistence p)
	{
		persistence = p;
	}

	@Override
	public void onPlayerJoin(PlayerEvent event)
	{
		Player player = event.getPlayer();
		String playerName = player.getName();
		User playerData = persistence.get(playerName, User.class);
		if (playerData == null)
		{
			playerData = new User(player);
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
		User playerData = persistence.get(playerName, User.class);
		if (playerData != null)
		{
			playerData.disconnect(player);
			persistence.put(playerData);
			persistence.save();
		}
	}
	
	private Persistence persistence;

}
