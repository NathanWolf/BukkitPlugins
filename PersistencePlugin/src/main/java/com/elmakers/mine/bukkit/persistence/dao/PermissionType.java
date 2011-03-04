package com.elmakers.mine.bukkit.persistence.dao;

// Enums don't need annotations to be persistable
public enum PermissionType
{
	DEFAULT,
	ALLOW_ALL,
	OPS_ONLY,
	PLAYER_ONLY
};