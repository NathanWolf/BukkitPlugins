package com.elmakers.mine.bukkit.plugins.nether;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;


import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.gameplay.BlockRequestListener;
import com.elmakers.mine.bukkit.gameplay.dao.BoundingBox;
import com.elmakers.mine.bukkit.persistence.dao.LocationData;
import com.elmakers.mine.bukkit.persistence.dao.PlayerData;
import com.elmakers.mine.bukkit.persistence.dao.WorldData;
import com.elmakers.mine.bukkit.plugins.nether.dao.NetherWorld;
import com.elmakers.mine.bukkit.plugins.nether.dao.Portal;
import com.elmakers.mine.bukkit.plugins.nether.dao.PortalArea;
import com.elmakers.mine.bukkit.plugins.nether.dao.NetherPlayer;
import com.elmakers.mine.bukkit.plugins.nether.dao.PortalType;
import com.elmakers.mine.bukkit.plugins.nether.dao.NetherPlayer.TeleportState;

import com.elmakers.mine.bukkit.utilities.PluginUtilities;
import com.elmakers.mine.craftbukkit.persistence.Persistence;

public class NetherManager
{
	public NetherWorld getCurrentWorld(World current)
	{
		if (current == null) return null;
		NetherWorld currentWorld = getWorldData(current);
		if (currentWorld == null)
		{
			currentWorld = createWorld(current.getName(), current.getEnvironment(), null);
		}
		
		return currentWorld;
	}
	
	public NetherWorld createWorld(String name, Environment defaultType, World currentWorld)
	{
		NetherWorld currentWorldData = null;
		if (currentWorld != null)
		{
			currentWorldData = getWorldData(currentWorld);
		}

		WorldData world = utilities.getWorld(server, name, defaultType);
		if (world == null) return null;
		
		NetherWorld worldData = getWorldData(world);
		if (worldData == null)
		{
			worldData = new NetherWorld(world);
			switch (defaultType)
			{
				case NETHER:
					worldData.setScale(8);
					break;
				default:
					worldData.setScale(1);
			}
			persistence.put(worldData);
			persistence.put(worldData.getTargetOffset());
			persistence.put(worldData.getCenterOffset());
			
			if (currentWorldData != null)
			{
				worldData.autoBind(currentWorldData);
			}
		}

		return worldData;
	}
	
	protected NetherWorld getWorldData(World world)
	{
		WorldData worldData = utilities.getWorld(server, world);
		return getWorldData(worldData);
	}
	
	protected NetherWorld getWorldData(WorldData worldData)
	{
		if (worldData == null) return null;
		return persistence.get(worldData, NetherWorld.class);
	}
	
	public NetherWorld getDefaultNether(World currentWorld)
	{
		NetherWorld nether = persistence.get("nether", NetherWorld.class);
		if (nether != null)
		{
			return nether;
		}
				
		nether = createWorld("nether", Environment.NETHER, currentWorld);
		persistence.put(nether);
	
		return nether;	
	}
	
	public NetherWorld getNextWorld(World currentWorld)
	{
		NetherWorld thisWorldData = getCurrentWorld(currentWorld);
		if (thisWorldData == null) return null;
		
		NetherWorld targetWorld = thisWorldData.getTargetWorld();
		if (targetWorld == null)
		{
			List<NetherWorld> allWorlds = new ArrayList<NetherWorld>();
			persistence.getAll(allWorlds, NetherWorld.class);
			
			// Only auto-bind the first world
			// TODO - Persistence: a way to get entity could without getting a whole list!
			if (allWorlds.size() == 1)
			{
				targetWorld = getDefaultNether(currentWorld);
				if (targetWorld != null)
				{
					thisWorldData.setTargetWorld(targetWorld);
					thisWorldData.autoBind(thisWorldData);
				}
			}
		}
		
		return targetWorld;
	}
	
	public NetherPlayer getPlayerData(Player player)
	{
		PlayerData playerData = utilities.getPlayer(player);
		if (playerData == null) return null;
		
		NetherPlayer tpPlayer = persistence.get(playerData, NetherPlayer.class);
		if (tpPlayer == null)
		{
			tpPlayer = new NetherPlayer(playerData);
			tpPlayer.setState(TeleportState.NONE);
			persistence.put(tpPlayer);
		}
		
		return tpPlayer;
	}
	
	public boolean requestBlockList(World currentWorld, String worldName, BlockVector center, int radius, BlockRequestListener listener)
	{
		NetherWorld targetWorld = getWorld(worldName, currentWorld);
		if (targetWorld == null)
		{
			return false;
		}
		
		NetherWorld worldData = getWorldData(currentWorld);
		if (worldData == null)
		{
			return false;
		}
		
		BlockRequest request = new BlockRequest(this, center, radius, listener);
		request.setWorld(targetWorld);
		request.translate(worldData);
		
		BlockVector targetLocation = request.getCenter();
		World world = targetWorld.getWorld().getWorld();
		Chunk chunk = world.getChunkAt(targetLocation.getBlockX(), targetLocation.getBlockZ());
		
		/*if (world.isChunkLoaded(chunk))
		{
			request.dispatch();
		}
		else
		{*/
			BlockRequestList requesting = requestMap.get(getChunkId(chunk));
			if (requesting == null)
			{
				requesting = new BlockRequestList();
				requestMap.put(getChunkId(chunk), requesting);
			}
			
			requesting.add(request);
			world.loadChunk(chunk);
	//	}
		
		return true;
	}
	
	protected BlockVector getChunkId(Chunk chunk)
	{
		return new BlockVector(chunk.getX(), 0, chunk.getZ());
	}
	
	protected static void parseMaterials(String csvList, HashMap<Material, Boolean> materials)
	{
		String[] matIds = csvList.split(",");
		for (String matId : matIds)
		{
			try
			{
				int typeId = Integer.parseInt(matId.trim());
				materials.put(Material.getMaterial(typeId), true);
			}
			catch (NumberFormatException ex)
			{
				
			}
		}
	}
	
	public void initialize(Server server, Persistence persistence, PluginUtilities utilities)
	{
		this.server = server;
		this.utilities = utilities;
		this.persistence = persistence;
		
		load();
	}
	
	/*
	 * Basic player/world access
	 */

	
	/*
	 * PortalArea
	 */
	public boolean createArea(Player player)
	{
		NetherPlayer playerData = getPlayerData(player);
		
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
			
		nether.setCreator(playerData);
		nether.setRatio(ratio);
		
		nether.create(player.getWorld());
		addToMap(nether);
		
		netherAreas.add(nether);
		persistence.put(nether);
		
		return true;
	}
	
	protected void addToMap(PortalArea nether)
	{
		/*
		BlockVector location = nether.getInternalArea().getCenter();
		Chunk chunk = world.getChunkAt(location.getBlockX(), location.getBlockZ());
		NetherList list = netherMap.get(getChunkId(chunk));
		if (list == null)
		{
			list = new NetherList();
			netherMap.put(getChunkId(chunk), list);
		}
		list.add(nether);
		*/
	}
	
	protected void load()
	{
		persistence.getAll(netherAreas, PortalArea.class);
		for (PortalArea nether : netherAreas)
		{
			addToMap(nether);
		}
	}
	
	public PortalArea getNether(BlockVector position)
	{
		/*
		Chunk chunk = world.getChunkAt(position.getBlockX(), position.getBlockZ());
		NetherList list = netherMap.get(getChunkId(chunk));
		if (list == null) return null;
		
		for (PortalArea nether : list)
		{
			if (nether.getExternalArea().contains(position))
			{
				return nether;
			}
		}
		*/
		return null;
	}
	
	/*
	 * Player teleportation
	 */
	
	public BlockVector mapLocation(NetherWorld from, NetherWorld to, BlockVector target)
	{
		double fromScale = from.getScale();
		double toScale = to.getScale();
		
		if (fromScale == 0 || toScale == 0) return target;
		
		int originalY = target.getBlockY();
		Vector transformed = new Vector(target.getBlockX(), target.getBlockY(), target.getBlockZ());
		
		// First, offset to center on local spawn (making sure there is one set)
		
		BlockVector fromSpawn = from.getWorld().getSpawn();
		if (fromSpawn != null)
		{
			transformed.subtract(fromSpawn);
		}
		
		// Apply additional offset
		BlockVector fromOffset = from.getCenterOffset();
		if (fromOffset != null)
		{
			transformed.subtract(fromOffset);
		}
		
		// Scale
		if (fromScale != 0 && toScale != 0)
		{
			transformed.multiply(fromScale / toScale);
		}
		
		// Unwind
		
		BlockVector toOffset = to.getCenterOffset();
		if (toOffset != null)
		{
			transformed.add(toOffset);
		}
		
		BlockVector toSpawn = to.getWorld().getSpawn();
		if (toSpawn != null)
		{
			transformed.add(toSpawn);
		}
		
		transformed.setY(originalY);
		
		return new BlockVector(transformed);
	}


	protected void onPlayerMove(Player player)
	{
		NetherPlayer playerData = getPlayerData(player);
		if (playerData == null) return;
		
		Location currentLoc = player.getLocation();
		BlockVector lastLoc = playerData.getLastLocation();
		if 
		(
			lastLoc != null
		&&	currentLoc.getBlockX() == lastLoc.getBlockX()
		&&	currentLoc.getBlockY() == lastLoc.getBlockY()
		&&	currentLoc.getBlockZ() == lastLoc.getBlockZ()
		)
		{
			return;
		}
		
		playerData.update(player);
		
		persistence.put(playerData);
		
		Block block = player.getWorld().getBlockAt(currentLoc.getBlockX(), currentLoc.getBlockY(), currentLoc.getBlockZ());
		if (block == null) return;
		
		block = block.getFace (BlockFace.UP);
		if (block == null) return;
		
		if (block.getType() != Material.PORTAL)
		{
			if (playerData.getState() != NetherPlayer.TeleportState.NONE)
			{
				playerData.setState(NetherPlayer.TeleportState.NONE);
			}
			return;
		}
		
		if (playerData.getState() != NetherPlayer.TeleportState.NONE) return;
		startAutoPortal(player);
	}
	
	public NetherWorld getWorld(String worldName, World currentWorld)
	{
		// First, make sure the current world is loaded!
		getCurrentWorld(currentWorld);
		
		NetherWorld targetWorld = null;
		if (worldName != null && worldName.length() > 0)
		{
			WorldData world = persistence.get(worldName, WorldData.class);
			if (world != null)
			{
				targetWorld = getWorldData(world);
			}
		}
		else
		{
			targetWorld = getNextWorld(currentWorld);
		}
		
		return targetWorld;
	}

	public WorldData go(Player player, String worldName, PortalType autoPortal)
	{
		// First make sure this world is registered!
		getCurrentWorld(player.getWorld());
		NetherWorld targetWorld = getWorld(worldName, player.getWorld());
		if (targetWorld == null)
		{
			return null;
		}
		
		Location location = player.getLocation();
		if (!startTeleport(player, targetWorld, location, autoPortal))
		{
			targetWorld = null;
			return null;
		}
	
		return targetWorld.getWorld();
	}
	
	public WorldData go(Player player, String worldName)
	{
		return go(player, worldName, PortalType.NONE);
	}
	
	protected boolean startAutoPortal(Player player)
	{
		return startTeleport(player, getNextWorld(player.getWorld()), player.getLocation(), PortalType.PORTAL_FRAME_AND_PLATFORM);
	}
	
	protected boolean startTeleport(Player player, NetherWorld targetWorld, Location targetLocation)
	{
		return startTeleport(player, targetWorld, targetLocation, PortalType.NONE);
	}
	
	protected boolean startTeleport(Player player, NetherWorld targetWorld, Location targetLocation, PortalType autoPortal)
	{
		if (targetWorld == null) return false;
		
		NetherPlayer playerData = getPlayerData(player);
		if (playerData == null) return false;
		
		if (!utilities.getSecurity().hasPermission(player, "NetherGate.portal.use")) 
		{
			cancelTeleport(playerData);
			return false;
		}
		
		// Register the current world first, in case it's not already.
		NetherWorld currentWorld = getCurrentWorld(player.getWorld());
		if (currentWorld == null) return false;
		
		// Look for existing Portal
		BlockVector target = new BlockVector(targetLocation.getBlockX(), targetLocation.getBlockY(), targetLocation.getBlockZ());
		Portal sourcePortal = currentWorld.findPortalAt(target);
		Portal targetPortal = null;
		if (sourcePortal != null)
		{
			sourcePortal.initialize(this);
			targetPortal = sourcePortal.getTarget();
		}
		
		if (targetPortal != null && targetPortal.getLocation() != null)
		{
			targetPortal.initialize(this);
			target = targetPortal.getLocation().getPosition();
			NetherWorld targetPortalWorld = getWorldData(targetPortal.getLocation().getWorld());
			if (targetPortalWorld != null)
			{
				targetWorld = targetPortalWorld;
			}
		}
		else
		{
			targetPortal = null;
			if (sourcePortal != null)
			{
				sourcePortal.setTarget(null);
			}
		}
		
		// Check for auto-portal, register this one if binding
		if (autoPortal != null && autoPortal.isTracked())
		{
			 if (sourcePortal == null)
			 {
				 sourcePortal = new Portal(player, targetLocation, autoPortal, this);
				 persistence.put(sourcePortal);
				 currentWorld.addPortal(sourcePortal);
			 }
		}
		else
		{
			// ignore source portal if not tracking- just use portal loc instead
			sourcePortal = null;
		}
		
		playerData.setTargetLocation(target);
		playerData.setTargetWorld(targetWorld);
		playerData.setSourceWorld(currentWorld);
		playerData.setSourcePortal(sourcePortal);
		playerData.setTargetPortal(targetPortal);

		// for later!
		playerData.setSourceArea(null);
		playerData.setTargetArea(null);
			
		WorldData targetWorldData = targetWorld.getWorld();
		if (targetWorldData == null)
		{
			cancelTeleport(playerData);
			return false;
		}
		World world = targetWorldData.getWorld();
		Chunk chunk = world.getChunkAt(targetLocation.getBlockX(), targetLocation.getBlockZ());
		
		if (world.isChunkLoaded(chunk))
		{
			finishTeleport(playerData, world);
		}
		else
		{
			PlayerList players = teleporting.get(getChunkId(chunk));
			if (players == null)
			{
				players = new PlayerList();
				teleporting.put(getChunkId(chunk), players);
			}
			
			players.add(playerData);
			world.loadChunk(chunk);
		}
		
		return true;
	}
	
	public void onChunkLoaded(Chunk chunk)
	{
		PlayerList players = teleporting.get(getChunkId(chunk));
		if (players != null)
		{
			for (NetherPlayer tp : players)
			{
				finishTeleport(tp, chunk.getWorld());
			}
			teleporting.put(getChunkId(chunk), null);
		}
		
		BlockRequestList blockRequests = requestMap.get(getChunkId(chunk));
		if (blockRequests != null)
		{
			for (BlockRequest request : blockRequests)
			{
				request.dispatch();
			}
			requestMap.put(getChunkId(chunk), null);
		}
	}
	
	protected void cancelTeleport(NetherPlayer playerData)
	{
		playerData.setState(TeleportState.NONE);
		persistence.put(playerData);
	}
	
	protected void finishTeleport(NetherPlayer playerData, World world)
	{
		Player player = server.getPlayer(playerData.getPlayer().getName());
		if (player != null)
		{
			Location currentLocation = player.getLocation();
			BlockVector loc = playerData.getTargetLocation();
			loc = mapLocation(playerData.getSourceWorld(), playerData.getTargetWorld(), loc);
			
			Location location = new Location
			(
					world, 
					loc.getBlockX(), 
					loc.getBlockY(), 
					loc.getBlockZ(),
					currentLocation.getYaw(),
					currentLocation.getPitch()
			);
					
			// Find a good place to stand
			Location targetLocation = findPlaceToStand(location);
			if (targetLocation == null)
			{
				if (debugLogging)
				{
					log.info("NG: Couldn't find a place for " + player.getName() + " to stand - sorry for the fall!");
				}
				targetLocation = location;
			}
			else
			{
				// Go one up- findPlaceToStand returns a block, not a place to tp to.
				targetLocation.setY(targetLocation.getBlockY() + 1);
			}
			
			if (debugLogging)
			{
				String formatMessage =  "NG: TP'ing Player %s from (%d, %d, %d) to (%d, %d, %d)";
				String message = String.format
				(
					formatMessage, 
					player.getName(), 
					currentLocation.getBlockX(), 
					currentLocation.getBlockY(), 
					currentLocation.getBlockZ(), 
					targetLocation.getBlockX(), 
					targetLocation.getBlockY(), 
					targetLocation.getBlockZ()
				);
				log.info(message);
			}
			// Get player permissions
			boolean buildPortal = utilities.getSecurity().hasPermission(player, NetherPermissions.autoCreatePortal);
			boolean buildPlatform = utilities.getSecurity().hasPermission(player, NetherPermissions.autoCreatePlatform);
			boolean fillAir = utilities.getSecurity().hasPermission(player, NetherPermissions.fillAir);			
			
			// Check for portal connections
			Portal sourcePortal = playerData.getSourcePortal();
			NetherWorld worldData = getWorldData(world);
			if (sourcePortal != null && sourcePortal.getType().isTracked() && worldData != null)
			{
				sourcePortal.initialize(this);
				// Auto-bind if not bound, and you have permission to create at least the portal
				Portal targetPortal = sourcePortal.getTarget();
				if (targetPortal == null && buildPortal)
				{
					targetPortal = new Portal(player, targetLocation, sourcePortal.getType(), this);
					persistence.put(targetPortal);
					worldData.addPortal(targetPortal);
					if (debugLogging)
					{
						String formatMessage =  "NG: Building a full portal at %d, %d, %d and %s filling with air";
						String message = String.format(formatMessage, targetLocation.getBlockX(), targetLocation.getBlockY(), targetLocation.getBlockZ(), fillAir ? "" : "not");
						log.info(message);
					}
					targetPortal.build(fillAir);
					sourcePortal.setTarget(targetPortal);
				}
				else
				{
					// Use portal coordinates if available!
					targetPortal.initialize(this);
					targetLocation = targetPortal.getLocation().getLocation();
				}
			}
			else if(fillAir || buildPlatform)
			{
				Portal tempPortal =  new Portal(player, targetLocation, PortalType.PLATFORM, this);
				tempPortal.build(fillAir);
			
				if (debugLogging)
				{
					if (fillAir)
					{
						String formatMessage =  "NG: Clearing area around %d, %d, %d";
						String message = String.format(formatMessage, targetLocation.getBlockX(), targetLocation.getBlockY(), targetLocation.getBlockZ());
						log.info(message);
					}

					if (buildPlatform)
					{
						String formatMessage =  "NG: Building a platform at %d, %d, %d";
						String message = String.format(formatMessage, targetLocation.getBlockX(), targetLocation.getBlockY(), targetLocation.getBlockZ());
						log.info(message);
					}
				}
			}
			
			// Go up one block so the player is inside the portal
			targetLocation.setY(targetLocation.getY() + 1);
			player.teleportTo(targetLocation);
			
			playerData.update(player);
		}
		playerData.setState(TeleportState.TELEPORTED);
		persistence.put(playerData);
	}	
	
	public void createTemporaryPortal(Player player, Block centerBlock)
	{
		Portal tempPortal = new Portal(player, centerBlock.getLocation(), PortalType.PORTAL, this);
		tempPortal.build(false);
		if (debugLogging)
		{
			String formatMessage =  "NG: Buiding temporary portal at (%d, %d, %d)";
			String message = String.format
			(
				formatMessage, 
				player.getName(), 
				centerBlock.getX(), 
				centerBlock.getY(), 
				centerBlock.getZ()
			);
			log.info(message);
		}

	}
	
	protected Location findPlaceToStand(Location startLocation)
	{
		World world = startLocation.getWorld();
		boolean goUp = world.getEnvironment() == Environment.NETHER;

		// get player position, start from top
		int minY = 3;
		int maxY = 126;
		
		int x = startLocation.getBlockX();
		int y = goUp ? minY : maxY;
		int dy = goUp ? 1 : -1;
		int z = startLocation.getBlockZ();		

		// search for a spot to stand
		Block[] blocks = new Block[3];
		Material[] mats = new Material[3];

		blocks[0] = world.getBlockAt(x, y, z);
		blocks[1] = blocks[0].getFace(BlockFace.UP);
		blocks[2] = blocks[1].getFace(BlockFace.UP);
		
		for (int i = 0; i < blocks.length; i++)
		{
			mats[i] = blocks[i].getType();
		}
		
		for (;y >= minY && y <= maxY; y += dy)
		{
			if 
			(
				isOkToStandOn(mats[0])
			&&	isOkToStandIn(mats[1])
			&& 	isOkToStandIn(mats[2])
			)
			{
				// spot found - return location
				return new Location(world, x, y, z, startLocation.getYaw(), startLocation.getPitch());
			}
			
			if (goUp)
			{
				blocks[1] = blocks[2];				
				blocks[0] = blocks[1];
				blocks[2] = blocks[2].getFace(BlockFace.UP);
			}
			else
			{
				blocks[2] = blocks[1];				
				blocks[1] = blocks[0];
				blocks[0] = blocks[0].getFace(BlockFace.DOWN);
			}
	
			for (int i = 0; i < blocks.length; i++)
			{
				mats[i] = blocks[i].getType();
			}
		}

		// no spot found
		return tryToLand(startLocation);
	}
	
	protected Location tryToLand(Location startLocation)
	{
		World world = startLocation.getWorld();
		Block currentBlock = world.getBlockAt(startLocation.getBlockX(), startLocation.getBlockY(), startLocation.getBlockZ());
		int y = currentBlock.getY();
		while (y > 2)
		{
			if (isOkToStandOn(currentBlock.getType()))
			{
				return new Location(world, currentBlock.getX(), currentBlock.getY(), currentBlock.getZ(), startLocation.getYaw(), startLocation.getPitch()); 
			}
			y--;
		}
		
		// Uh-oh! Good luck...
		return null;
	}
	
	public boolean isOkToStandIn(Material mat)
	{
		return (mat == Material.AIR || mat == Material.PORTAL);
	}

	public boolean isOkToStandOn(Material mat)
	{
		return (mat != Material.AIR);
	}
	
	public boolean allowPhysics(Block block)
	{
		if (disabledPhysics > 0)
		{
			if (System.currentTimeMillis() > disabledPhysics)
			{
				disabledPhysics = 0;
				return true;
			}
			
			return (block.getType() != Material.PORTAL);
		}
		
		return true;
	}
	
	public void disablePhysics()
	{
		disabledPhysics = System.currentTimeMillis() + 5000;
	}
	
	public Server getServer()
	{
		return server;
	}
	
	public void onPlayerDeath(Player player, EntityDeathEvent event)
	{
		NetherPlayer playerData = getPlayerData(player);
		if (playerData == null)
		{
			return;
		}
		
		WorldData homeWorld = null;	
		LocationData homeLocation = playerData.getHome();
		if (homeLocation != null)
		{
			homeWorld = homeLocation.getWorldData();
		}
		if (homeWorld != null)
		{
			NetherWorld netherHome = persistence.get(homeWorld, NetherWorld.class);
			if (netherHome != null)
			{
				BlockVector spawn = homeWorld.getSpawn();
				World home = homeWorld.getWorld();
				Location spawnPoint = new Location
				(
						home,
						spawn.getX(),
						spawn.getY(),
						spawn.getZ(),
						player.getLocation().getYaw(),
						player.getLocation().getPitch()
				);
				startTeleport(player, netherHome, spawnPoint);
			}
		}
	}
	
	public void loadWorlds()
	{
		// Make sure all worlds are loaded before a player joins!
		List<WorldData> allWorlds = new ArrayList<WorldData>();
		persistence.getAll(allWorlds, WorldData.class);
		for (WorldData loadWorld : allWorlds)
		{
			loadWorld.getWorld();
		}
	}
	
	public static BlockVector							origin					= new BlockVector(0, 0, 0);

	protected static boolean 							debugLogging			= true;
	protected static final Logger 						log						= Persistence.getLogger();
	
	protected HashMap<BlockVector, PlayerList>			teleporting				= new HashMap<BlockVector, PlayerList>();
	protected HashMap<BlockVector, BlockRequestList>	requestMap				= new HashMap<BlockVector, BlockRequestList>();
	protected List<PortalArea>							netherAreas				= new ArrayList<PortalArea>();
	protected Server									server;
	protected Persistence								persistence;
	protected PluginUtilities							utilities;
	protected long										disabledPhysics			= 0;
	
}
