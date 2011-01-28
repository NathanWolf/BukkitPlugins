package com.elmakers.mine.bukkit.plugins.classes.dao;

import com.elmakers.mine.bukkit.plugins.persistence.annotations.Persist;
import com.elmakers.mine.bukkit.plugins.persistence.annotations.PersistClass;

@PersistClass(name = "player", schema = "classes") 
public class PlayerDAO
{
	@Persist(id=true)
	public String getPlayerName()
	{
		return playerName;
	}
	
	public void setPlayerName(String pName)
	{
		this.playerName = pName;
	}
	
	private String playerName;
}
