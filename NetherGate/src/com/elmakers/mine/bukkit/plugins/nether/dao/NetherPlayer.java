package com.elmakers.mine.bukkit.plugins.nether.dao;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

import com.elmakers.mine.bukkit.plugins.persistence.dao.PlayerData;

import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistField;
import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistClass;

@PersistClass(schema="nether", name="player")
public class NetherPlayer
{
	public NetherPlayer()
	{
		
	}	
	
	public NetherPlayer(PlayerData player)
	{
		this.player = player;
	}
	
	public void update(Player player)
	{	
		Location loc = player.getLocation();
		lastLocation = new BlockVector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
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
	public BlockVector getTargetLocation()
	{
		return targetLocation;
	}
	
	public void setTargetLocation(BlockVector targetLocation)
	{
		this.targetLocation = targetLocation;
	}
	
	@PersistField
	public NetherWorld getHomeWorld()
	{
		return homeWorld;
	}

	public void setHomeWorld(NetherWorld homeWorld)
	{
		this.homeWorld = homeWorld;
	}
	
	@PersistField
	public BlockVector getHome()
	{
		return home;
	}

	public void setHome(BlockVector home)
	{
		this.home = home;
	}

	@PersistField
	public NetherWorld getTargetWorld()
	{
		return targetWorld;
	}

	public void setTargetWorld(NetherWorld targetWorld)
	{
		this.targetWorld = targetWorld;
	}
	
	@PersistField
	public BlockVector getLastLocation()
	{
		return lastLocation;
	}

	public void setLastLocation(BlockVector lastLocation)
	{
		this.lastLocation = lastLocation;
	}
	
	@PersistField
	public NetherWorld getSourceWorld()
	{
		return sourceWorld;
	}

	public void setSourceWorld(NetherWorld sourceWorld)
	{
		this.sourceWorld = sourceWorld;
	}

	@PersistField
	public PortalArea getTargetArea()
	{
		return targetArea;
	}

	public void setTargetArea(PortalArea targetArea)
	{
		this.targetArea = targetArea;
	}

	@PersistField
	public PortalArea getSourceArea()
	{
		return sourceArea;
	}

	public void setSourceArea(PortalArea sourceArea)
	{
		this.sourceArea = sourceArea;
	}
	
	@PersistField
	public Portal getTargetPortal()
	{
		return targetPortal;
	}

	public void setTargetPortal(Portal targetPortal)
	{
		this.targetPortal = targetPortal;
	}

	@PersistField
	public Portal getSourcePortal()
	{
		return sourcePortal;
	}

	public void setSourcePortal(Portal sourcePortal)
	{
		this.sourcePortal = sourcePortal;
	}

	protected PlayerData 		player;
	protected NetherWorld		homeWorld;
	protected BlockVector		home;
	protected BlockVector		lastLocation;
	protected TeleportState 	state;
	protected NetherWorld		targetWorld;
	protected NetherWorld		sourceWorld;
	protected PortalArea		targetArea;
	protected PortalArea		sourceArea;
	protected Portal			targetPortal;
	protected Portal			sourcePortal;
	protected BlockVector		targetLocation;
}
