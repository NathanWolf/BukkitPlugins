package com.elmakers.mine.bukkit.plugins.nether.dao;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

import com.elmakers.mine.bukkit.persistence.annotation.PersistClass;
import com.elmakers.mine.bukkit.persistence.annotation.PersistField;
import com.elmakers.mine.bukkit.persistence.dao.LocationData;
import com.elmakers.mine.bukkit.persistence.dao.PlayerData;


@PersistClass(schema="nether", name="player")
public class NetherPlayer
{
	public NetherPlayer()
	{
		
	}	
	
	public NetherPlayer(PlayerData player)
	{
		this.player = player;
		update(player.getPlayer());
	}
	
	public void update(Player player)
	{	
		if (player == null) return;
		
		Location loc = player.getLocation();
		if (home == null)
		{
			home = new LocationData(loc);
		}
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
	
	@PersistField(contained=true)
	public LocationData getHome()
	{
		return home;
	}

	public void setHome(LocationData home)
	{
		this.home = home;
	}

	@PersistField(contained=true)
	public BlockVector getLastLocation()
	{
		return lastLocation;
	}

	public void setLastLocation(BlockVector lastLocation)
	{
		this.lastLocation = lastLocation;
	}
	
	// Transient state data
	public TeleportState getState()
	{
		return state;
	}
	
	public void setState(TeleportState state)
	{
		if (state == TeleportState.TELEPORTING || state == TeleportState.TELEPORTED)
		{
			shieldTimer = System.currentTimeMillis() + defaultShieldInterval;
		}
		this.state = state;
	}

	public BlockVector getTargetLocation()
	{
		return targetLocation;
	}
	
	public void setTargetLocation(BlockVector targetLocation)
	{
		this.targetLocation = targetLocation;
	}
	
	public NetherWorld getTargetWorld()
	{
		return targetWorld;
	}

	public void setTargetWorld(NetherWorld targetWorld)
	{
		this.targetWorld = targetWorld;
	}
	
	public NetherWorld getSourceWorld()
	{
		return sourceWorld;
	}

	public void setSourceWorld(NetherWorld sourceWorld)
	{
		this.sourceWorld = sourceWorld;
	}

	public PortalArea getTargetArea()
	{
		return targetArea;
	}

	public void setTargetArea(PortalArea targetArea)
	{
		this.targetArea = targetArea;
	}

	public PortalArea getSourceArea()
	{
		return sourceArea;
	}

	public void setSourceArea(PortalArea sourceArea)
	{
		this.sourceArea = sourceArea;
	}
	
	public Portal getTargetPortal()
	{
		return targetPortal;
	}

	public void setTargetPortal(Portal targetPortal)
	{
		this.targetPortal = targetPortal;
	}

	public Portal getSourcePortal()
	{
		return sourcePortal;
	}

	public void setSourcePortal(Portal sourcePortal)
	{
		this.sourcePortal = sourcePortal;
	}
	
	public boolean areShieldsUp()
	{
		if (shieldTimer == null) return false;
	
		if (System.currentTimeMillis() > shieldTimer)
		{
			shieldTimer = null;
		}
		
		return true;
	}

	protected LocationData		home;
	protected BlockVector		lastLocation;
	
	// Transient
	protected PlayerData 		player;
	protected TeleportState 	state;
	protected NetherWorld		targetWorld;
	protected NetherWorld		sourceWorld;
	protected PortalArea		targetArea;
	protected PortalArea		sourceArea;
	protected Portal			targetPortal;
	protected Portal			sourcePortal;
	protected BlockVector		targetLocation;
	protected Long				shieldTimer;
	
	static final long defaultShieldInterval = 10000;
}
