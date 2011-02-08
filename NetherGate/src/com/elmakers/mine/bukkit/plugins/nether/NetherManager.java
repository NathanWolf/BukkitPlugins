package com.elmakers.mine.bukkit.plugins.nether;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

import com.elmakers.mine.bukkit.plugins.nether.dao.PortalArea;
import com.elmakers.mine.bukkit.plugins.nether.dao.TeleportingPlayer;
import com.elmakers.mine.bukkit.plugins.nether.dao.TeleportingPlayer.TeleportState;
import com.elmakers.mine.bukkit.plugins.persistence.PluginUtilities;
import com.elmakers.mine.bukkit.plugins.persistence.Persistence;
import com.elmakers.mine.bukkit.plugins.persistence.dao.BoundingBox;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PlayerData;
import com.elmakers.mine.bukkit.plugins.persistence.dao.WorldData;

public class NetherManager
{
	
	public void initialize(Server server, Persistence persistence, PluginUtilities utilities)
	{
		this.server = server;
		this.utilities = utilities;
		this.persistence = persistence;
	}
	
	public boolean create(Player player)
	{
		Location location = player.getLocation();
		PortalArea nether = new PortalArea();
		
		int minX = location.getBlockX() - PortalArea.defaultSize / 2;
		int maxX = location.getBlockX() + PortalArea.defaultSize / 2;
		int minZ = location.getBlockZ() - PortalArea.defaultSize / 2;
		int maxZ = location.getBlockZ() + PortalArea.defaultSize / 2;
		int minY = PortalArea.defaultFloor + PortalArea.getFloorPadding();
		int maxY = minY + PortalArea.minHeight;
		
		int limitY = location.getBlockY() - PortalArea.getCeilingPadding();
		
		if (maxY > limitY)
		{
			return false;
		}
		
		while (maxY < limitY && maxY - minY < PortalArea.maxHeight)
		{
			maxY++;
		}
		
		BoundingBox area = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
		nether.setInternalArea(area);
		
		int ratio = PortalArea.defaultRatio;
			
		nether.setOwner(persistence.get(player.getName(), PlayerData.class));
		nether.setRatio(ratio);
		
		nether.create(player.getWorld());
		addToMap(nether);
		
		netherAreas.add(nether);
		persistence.put(nether);
		
		return true;
	}
	
	public void addToMap(PortalArea nether)
	{
		BlockVector location = nether.getInternalArea().getCenter();
		Chunk chunk = world.getChunkAt(location.getBlockX(), location.getBlockZ());
		NetherList list = netherMap.get(chunk);
		if (list == null)
		{
			list = new NetherList();
			netherMap.put(chunk, list);
		}
		list.add(nether);
	}
	
	public void load(World w)
	{
		if (world != null)
		{
			return;
		}
		world = w;
		if (world == null) return;
		
		persistence.getAll(netherAreas, PortalArea.class);
		for (PortalArea nether : netherAreas)
		{
			addToMap(nether);
		}
	}
	
	public PortalArea getNether(BlockVector position)
	{
		if (world == null || position == null) return null;
		
		Chunk chunk = world.getChunkAt(position.getBlockX(), position.getBlockZ());
		NetherList list = netherMap.get(chunk);
		if (list == null) return null;
		
		for (PortalArea nether : list)
		{
			if (nether.getExternalArea().contains(position))
			{
				return nether;
			}
		}
		
		return null;
	}
	
	public TeleportingPlayer getPlayerData(Player player)
	{
		PlayerData playerData = persistence.get(player.getName(), PlayerData.class);
		if (playerData == null) return null;
		
		TeleportingPlayer tpPlayer = persistence.get(playerData, TeleportingPlayer.class);
		if (tpPlayer == null)
		{
			tpPlayer = new TeleportingPlayer(playerData, TeleportState.TELEPORTING);
			persistence.put(tpPlayer);
		}
		
		return tpPlayer;
	}
	
	public boolean teleportPlayer(Player player, WorldData targetWorld, Location targetLocation)
	{
		TeleportingPlayer tpPlayer = getPlayerData(player);
		if (tpPlayer == null) return false;
		
		BlockVector target = new BlockVector(targetLocation.getBlockX(), targetLocation.getBlockY(), targetLocation.getBlockZ());

		tpPlayer.setTargetLocation(target);
		tpPlayer.setTargetWorld(targetWorld);
		
		World world = targetWorld.getWorld(server);
		Chunk chunk = world.getChunkAt(targetLocation.getBlockX(), targetLocation.getBlockZ());
		if (world.isChunkLoaded(chunk))
		{
			targetLocation.setWorld(targetWorld.getWorld(server));
			teleportTo(player, targetLocation);
			tpPlayer.setState(TeleportState.TELEPORTED);
		}
		else
		{
			PlayerList players = teleporting.get(chunk);
			if (players == null)
			{
				players = new PlayerList();
				teleporting.put(chunk, players);
			}
			
			players.add(tpPlayer);
			world.loadChunk(chunk);
		}
		
		return true;
	}
	
	public void startTeleport(Player player)
	{
		startTeleport(player, getNextWorld(player.getWorld()));
	}
	
	public WorldData getNextWorld(World currentWorld)
	{
		WorldData thisWorld = utilities.getWorld(server, currentWorld);
		WorldData targetWorld = thisWorld.getTargetWorld();
		
		// Auto-create a default nether world if this is the only one
		if (targetWorld == null || targetWorld == thisWorld)
		{
			targetWorld = utilities.getWorld(server, "nether", Environment.NETHER);
		}
		
		return targetWorld;
		
	}
	
	public boolean startTeleport(Player player, WorldData targetWorld)
	{
		return teleportPlayer(player, targetWorld, player.getLocation());
	}
	
	public void onPlayerMove(Player player)
	{
		TeleportingPlayer playerData = getPlayerData(player);
		if (playerData == null) return;
		
		Location currentLoc = player.getLocation();
		BlockVector lastLoc = playerData.getPlayer().getPosition();
		if 
		(
			currentLoc.getBlockX() == lastLoc.getBlockX()
		&&	currentLoc.getBlockY() == lastLoc.getBlockY()
		&&	currentLoc.getBlockZ() == lastLoc.getBlockZ()
		)
		{
			return;
		}
		
		playerData.getPlayer().update(player);
		persistence.put(playerData.getPlayer());
		
		Block block = player.getWorld().getBlockAt(currentLoc.getBlockX(), currentLoc.getBlockY(), currentLoc.getBlockZ());
		if (block == null) return;
		block = block.getFace (BlockFace.UP);
		if (block == null) return;
		
		if (block.getType() != Material.PORTAL)
		{
			if (playerData.getState() != TeleportingPlayer.TeleportState.NONE)
			{
				playerData.setState(TeleportingPlayer.TeleportState.NONE);
				persistence.put(playerData);
			}
			return;
		}
		
		if (playerData.getState() != TeleportingPlayer.TeleportState.NONE) return;
		startTeleport(player);
	}
	
	public void onChunkLoaded(Chunk chunk)
	{
		PlayerList players = teleporting.get(chunk);
		if (players != null)
		{
			for (TeleportingPlayer tp : players)
			{
				Player player = server.getPlayer(tp.getPlayer().getName());
				if (player != null)
				{
					BlockVector loc = tp.getTargetLocation();
					Location targetLocation = new Location(chunk.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
					teleportTo(player, targetLocation);
					tp.setState(TeleportState.TELEPORTED);
					persistence.put(tp);
				}
			}
			teleporting.put(chunk, null);
		}
	}
	
	public void teleportTo(Player player, Location location)
	{
		if (location == null) return;
		
		Location targetLocation = findPlaceToStand(location, true);
		if (targetLocation == null)
		{
			targetLocation = findPlaceToStand(location, false);
		}
		if (targetLocation == null)
		{
			targetLocation = location;
		}
		player.teleportTo(targetLocation);
	}
	
	public Location findPlaceToStand(Location startLocation, boolean goUp)
	{
		World world = startLocation.getWorld();
		int step;
		if (goUp)
		{
			step = 1;
		}
		else
		{
			step = -1;
		}

		// get player position
		int x = (int) Math.round(startLocation.getX() - 0.5);
		int y = (int) Math.round(startLocation.getY() + step + step);
		int z = (int) Math.round(startLocation.getZ() - 0.5);

		// search for a spot to stand
		while (2 < y && y < 127)
		{
			Block block = world.getBlockAt(x, y, z);
			Block blockOneUp = world.getBlockAt(x, y + 1, z);
			Block blockTwoUp = world.getBlockAt(x, y + 2, z);
			if 
			(
				isOkToStandOn(block.getType())
			&&	isOkToStandIn(blockOneUp.getType())
			&& 	isOkToStandIn(blockTwoUp.getType())
			)
			{
				// spot found - return location
				return new Location(world, (double) x + 0.5, (double) y + 1, (double) z + 0.5, startLocation.getYaw(),
						startLocation.getPitch());
			}
			y += step;
		}

		// no spot found
		return null;
	}
	
	public boolean isOkToStandIn(Material mat)
	{
		return (mat == Material.AIR || mat == Material.WATER || mat == Material.STATIONARY_WATER);
	}

	public boolean isOkToStandOn(Material mat)
	{
		return (mat != Material.AIR && mat != Material.LAVA && mat != Material.STATIONARY_LAVA);
	}

	protected HashMap<Chunk, PlayerList>	teleporting	= new HashMap<Chunk, PlayerList>();
	protected HashMap<Chunk, NetherList>	netherMap	= new HashMap<Chunk, NetherList>();
	protected List<PortalArea>				netherAreas	= new ArrayList<PortalArea>();
	protected World							world;
	protected Server						server;
	protected Persistence					persistence;
	protected PluginUtilities				utilities;
}
