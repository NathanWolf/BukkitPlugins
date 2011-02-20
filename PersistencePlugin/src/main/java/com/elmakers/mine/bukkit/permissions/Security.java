package com.elmakers.mine.bukkit.permissions;

import org.bukkit.entity.Player;

public interface Security
{
	public boolean hasPermission(Player player, String node);
}
