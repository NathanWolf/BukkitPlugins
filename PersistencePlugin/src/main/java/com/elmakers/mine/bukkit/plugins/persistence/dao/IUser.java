package com.elmakers.mine.bukkit.plugins.persistence.dao;

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
public interface IUser
{
	public boolean isSet(String key);
}
