package com.elmakers.mine.bukkit.plugins.persistence;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;

import com.elmakers.mine.bukkit.persistence.dao.PlayerData;
import com.elmakers.mine.craftbukkit.persistence.Persistence;

public class PersistenceListener extends PlayerListener
{
	public void initialize(Persistence persistence, PersistenceCommands commands)
	{
		this.persistence = persistence;
		this.commands = commands;
	}
	
	@Override
	public void onPlayerJoin(PlayerEvent event)
	{
		PersistencePlugin.getInstance().getPermissions().initializePermissions();
		
		Player player = event.getPlayer();
		String playerName = player.getName();
		PlayerData playerData = persistence.get(playerName, PlayerData.class);
		if (playerData == null)
		{
			playerData = new PlayerData(player);
		}
		playerData.login(player);
		if (!commands.getSUCommand().checkPermission(player))
		{
			playerData.setSuperUser(false);
		}
		persistence.put(playerData);
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
		}
	}
	
	private Persistence persistence;
	private PersistenceCommands commands;
}
