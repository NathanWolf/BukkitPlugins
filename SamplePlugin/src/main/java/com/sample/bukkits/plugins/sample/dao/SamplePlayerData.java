package com.sample.bukkits.plugins.sample.dao;

import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistClass;
import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistField;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PlayerData;

/**
 * Encapsulate a User that can be part of a Group
 * 
 * @author NathanWolf
 *
 */
@PersistClass(name = "player", schema = "sample") 
public class SamplePlayerData
{
	/**
	 * The default constructor, used by Persistence to create new instances.
	 */
	public SamplePlayerData()
	{
	}
	
	/**
	 * Create a new instance based on an existing player.
	 * 
	 * @param player the player this user will wrap
	 */
	public SamplePlayerData(PlayerData player)
	{
		this.id = player;
	}

	/**
	 * Get the player associated with this data
	 * 
	 * @return The PlayerData representing this player
	 */
	@PersistField(id=true)
	public PlayerData getId()
	{
		return id;
	}
	
	public void setId(PlayerData id)
	{
		this.id = id;
	}

	@PersistField
	public void setNickname(String nickname)
	{
		this.nickname = nickname;
	}

	public String getNickname()
	{
		return nickname;
	}

	private PlayerData	id;
	private String		nickname;
}
