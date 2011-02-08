package com.elmakers.mine.bukkit.plugins.nether.dao;

import org.bukkit.util.BlockVector;

import com.elmakers.mine.bukkit.plugins.persistence.dao.PlayerData;
import com.elmakers.mine.bukkit.plugins.persistence.dao.WorldData;

import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistField;
import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistClass;

@PersistClass(schema="nether", name="teleporting")
public class TeleportingPlayer
{
	public TeleportingPlayer()
	{
		
	}
	
	public TeleportingPlayer(PlayerData player, TeleportState state)
	{
		this.player = player;
		this.state = state;
	}
	
	public enum TeleportState
	{
		NONE,
		TELEPORTING,
		TELEPORTED
	}
	
	@PersistField(id=true)
	public PlayerData getPlayer()
	{
		return player;
	}
	
	public void setPlayer(PlayerData player)
	{
		this.player = player;
	}
	
	@PersistField
	public TeleportState getState()
	{
		return state;
	}
	
	public void setState(TeleportState state)
	{
		this.state = state;
	}
	
	@PersistField
	public WorldData getTargetWorld()
	{
		return targetWorld;
	}
	
	public void setTargetWorld(WorldData targetWorld)
	{
		this.targetWorld = targetWorld;
	}
	
	@PersistField
	public BlockVector getTargetLocation()
	{
		return targetLocation;
	}
	
	public void setTargetLocation(BlockVector targetLocation)
	{
		this.targetLocation = targetLocation;
	}
	
	public PlayerData 		player;
	public TeleportState 	state;
	public WorldData		targetWorld;
	public BlockVector		targetLocation;
}
