package com.elmakers.mine.bukkit.plugins.persistence.dao;

import org.bukkit.entity.Player;

/**
 * This should be temporary, I'm hoping once I get persisted inheretence
 * working, this won't be needed.
 * 
 * Right now, it's here to support routing permission requests through
 * Persistence in a generic way, since all my plugins (and other devs' plugins)
 * rely on Persistence for that right now.
 * 
 * @author nathan
 *
 */
public interface IProfile
{
	
	/**
	 * This is the interface for permissions profiles
	 * 
	 * @param key The key to check for
	 * @return True if this profile has this permissions
	 */
	public boolean isSet(String key);
	
	
	/**
	 * This is for backwards compatibility with Permissions,
	 * and will assigne each use a single "virtual" profile.
	 * 
	 * @param key The key to check for
	 * @param player The player requesting permissions
	 * @return True if this player has this permissions
	 */
	public boolean isSet(String key, Player player);
}
