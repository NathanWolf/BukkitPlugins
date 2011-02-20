package com.elmakers.mine.craftbukkit.permissions;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.persistence.dao.PlayerData;
import com.elmakers.mine.craftbukkit.persistence.Persistence;

public class Security implements com.elmakers.mine.bukkit.permissions.Security
{
	public boolean hasPermission(Player player, String node)
	{
		if (player == null) return false;
		
		// Check for su status- this can be toggled by ops with the /su command
		PlayerData playerData = Persistence.getInstance().get(player.getName(), PlayerData.class);
		
		if (playerData == null) return false;
		
		if (playerData.isSuperUser()) return true;
		
		return playerData.isSet(node);	
	}
}
