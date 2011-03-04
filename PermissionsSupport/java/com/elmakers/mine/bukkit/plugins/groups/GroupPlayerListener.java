package com.elmakers.mine.bukkit.plugins.groups;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;

import com.elmakers.mine.bukkit.groups.dao.User;
import com.elmakers.mine.bukkit.persistence.dao.PlayerData;
import com.elmakers.mine.craftbukkit.persistence.Persistence;

public class GroupPlayerListener extends PlayerListener
{

	public void initialize(Persistence persistence)
	{
		this.persistence = persistence;
	}
	
	// I think this is only here for Permissions support..
	@Override
	public void onPlayerJoin(PlayerEvent event)
	{
		Player player = event.getPlayer();
		String playerName = player.getName();
		PlayerData playerData = persistence.get(playerName, PlayerData.class);
		if (playerData == null)
		{
			playerData = new PlayerData(player);
			persistence.put(playerData);
		}
		
		User user = persistence.get(playerName, User.class);
		if (user == null)
		{
			user = new User(playerData);
			persistence.put(user);
		}
		
		playerData.setProfile(user);
	}
	
	protected Persistence persistence;
}
