package com.elmakers.mine.bukkit.plugins.permissions;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.permission.PermissionHandler;

public class PermissionsProxy implements PermissionHandler
{
	public PermissionsProxy(com.nijiko.permissions.PermissionHandler handler)
	{
		this.handler = handler;
	}
	
	public boolean isSet(Player player, String permissionNode)
	{
		if (handler != null)
		{
			return handler.has(player, permissionNode);
		}
		
		return false;
	}

	protected com.nijiko.permissions.PermissionHandler handler = null;
}
