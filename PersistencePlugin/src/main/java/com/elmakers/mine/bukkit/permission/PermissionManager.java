package com.elmakers.mine.bukkit.permission;

import com.elmakers.mine.craftbukkit.permission.PermissionDescriptionNode;
import com.elmakers.mine.craftbukkit.permission.RootPermissionDescription;

public interface PermissionManager
{
	public void addHandler(PermissionHandler handler);
	public RootPermissionDescription getPermissionRoot(final String path);
	public PermissionDescriptionNode getPermissionPath(final String path);
}
