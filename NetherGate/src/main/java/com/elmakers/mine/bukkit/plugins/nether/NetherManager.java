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
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.gameplay.BoundingBox;
import com.elmakers.mine.bukkit.plugins.nether.dao.NetherWorld;
import com.elmakers.mine.bukkit.plugins.nether.dao.PortalArea;
import com.elmakers.mine.bukkit.plugins.nether.dao.NetherPlayer;
import com.elmakers.mine.bukkit.plugins.nether.dao.NetherPlayer.TeleportState;

import com.elmakers.mine.bukkit.plugins.persistence.BlockRequestListener;
import com.elmakers.mine.bukkit.plugins.persistence.PluginUtilities;
import com.elmakers.mine.bukkit.plugins.persistence.Persistence;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PlayerData;
import com.elmakers.mine.bukkit.plugins.persistence.dao.WorldData;

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
					//worldData.setScale(8); // hrm..
					worldData.setScale(0);
					break;
				default:
					worldData.setScale(0); // TODO: Fix all this!
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
		World world = targetWorld.getWorld().getWorld(server);
		Chunk chunk = world.getChunkAt(targetLocation.getBlockX(), targetLocation.getBlockZ());
		
		if (world.isChunkLoaded(chunk))
		{
			request.dispatch();
		}
		else
		{
			BlockRequestList requesting = requestMap.get(chunk);
			if (requesting == null)
			{
				requesting = new BlockRequestList();
				requestMap.put(chunk, requesting);
			}
			
			requesting.add(request);
			world.loadChunk(chunk);
		}
		
		return true;
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
		
		needsPlatform.put(Material.WATER, true);
		needsPlatform.put(Material.STATIONARY_WATER, true);
		needsPlatform.put(Material.LAVA, true);
		needsPlatform.put(Material.STATIONARY_LAVA, true);
		
		parseMaterials(DEFAULT_DESTRUCTIBLES, destructible);
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
			
		nether.setOwner(playerData);
		nether.setRatio(ratio);
		
		nether.create(player.getWorld());
		addToMap(nether);
		
		netherAreas.add(nether);
		persistence.put(nether);
		
		return true;
	}
	
	protected void addToMap(PortalArea nether)
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
	
	protected void load(World w)
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
	
	/*
	 * Player teleportation
	 */
	
	protected boolean teleportPlayer(Player player, WorldData targetWorldData, Location targetLocation)
	{
		if (targetWorldData == null) return false;
		
		NetherWorld targetWorld = getWorldData(targetWorldData);
		if (targetWorld == null) return false;
		
		return teleportPlayer(player, targetWorld, targetLocation);
	}
	
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

	protected boolean teleportPlayer(Player player, NetherWorld targetWorld, Location targetLocation)
	{
		if (targetWorld == null) return false;
		
		NetherPlayer tpPlayer = getPlayerData(player);
		if (tpPlayer == null) return false;
		
		if (!persistence.hasPermission(player, "NetherGate.portal.use")) 
		{
			cancelTeleport(tpPlayer);
			return false;
		}
		
		// Register the current world first, in case it's not already.
		NetherWorld currentWorld = getCurrentWorld(player.getWorld());
		if (currentWorld == null) return false;
			
		BlockVector target = new BlockVector(targetLocation.getBlockX(), targetLocation.getBlockY(), targetLocation.getBlockZ());
		
		tpPlayer.setTargetLocation(target);
		tpPlayer.setTargetWorld(targetWorld);
		tpPlayer.setSourceWorld(currentWorld);
		tpPlayer.setSourceArea(null);
		tpPlayer.setTargetArea(null);
		tpPlayer.setSourcePortal(null);
		tpPlayer.setTargetPortal(null);
		
		// Set home world, if none is set
		if (tpPlayer.getHomeWorld() == null)
		{
			tpPlayer.setHomeWorld(currentWorld);
			persistence.put(tpPlayer);
		}
			
		World world = targetWorld.getWorld().getWorld(server);
		Chunk chunk = world.getChunkAt(targetLocation.getBlockX(), targetLocation.getBlockZ());
		
		if (world.isChunkLoaded(chunk))
		{
			finishTeleport(tpPlayer, world);
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
	
	protected void startTeleport(Player player)
	{
		startTeleport(player, getNextWorld(player.getWorld()));
	}
	
	protected boolean startTeleport(Player player, NetherWorld targetWorld)
	{
		return teleportPlayer(player, targetWorld, player.getLocation());
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
		startTeleport(player);
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
	
	public WorldData go(Player player, String worldName)
	{
		// First make sure this world is registered!
		getCurrentWorld(player.getWorld());
		NetherWorld targetWorld = getWorld(worldName, player.getWorld());
		if (targetWorld == null)
		{
			return null;
		}
		
		Location location = player.getLocation();
		if (!teleportPlayer(player, targetWorld, location))
		{
			targetWorld = null;
		}
	
		return targetWorld.getWorld();
	}
	
	public void onChunkLoaded(Chunk chunk)
	{
		PlayerList players = teleporting.get(chunk);
		if (players != null)
		{
			for (NetherPlayer tp : players)
			{
				finishTeleport(tp, chunk.getWorld());
			}
			teleporting.put(chunk, null);
		}
		
		BlockRequestList blockRequests = requestMap.get(chunk);
		if (blockRequests != null)
		{
			for (BlockRequest request : blockRequests)
			{
				request.dispatch();
			}
			requestMap.put(chunk, null);
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
			
			Location targetLocation = new Location
			(
					world, 
					loc.getBlockX(), 
					loc.getBlockY(), 
					loc.getBlockZ(),
					currentLocation.getYaw(),
					currentLocation.getPitch()
			);
			
			Persistence.getLogger().info("From: " + currentLocation.getBlockX() + ", " + currentLocation.getBlockY() + ", " + currentLocation.getBlockZ()
					+ " to " + targetLocation.getBlockX() + ", " + targetLocation.getBlockY() + ", " + targetLocation.getBlockZ());
			
			
			teleportTo(player, targetLocation);
			playerData.update(player);
		}
		playerData.setState(TeleportState.TELEPORTED);
		persistence.put(playerData);
	}	
	
	protected void teleportTo(Player player, Location location)
	{
		if (location == null) return;
		
		// Look up first, then down
		Location targetLocation = findPlaceToStand(location);
		if (targetLocation == null)
		{
			Persistence.getLogger().info("Couldn't find a place for " + player.getName() + " to stand - sorry for the fall!");
			targetLocation = location;
		}
		else
		{
			// Go one up- findPlaceToStand returns a block, not a place to tp to.
			targetLocation.setY(targetLocation.getBlockY() + 1);
		}
		
		Block standingBlock = location.getWorld().getBlockAt(targetLocation.getBlockX(), targetLocation.getBlockY(), targetLocation.getBlockZ());
		standingBlock = standingBlock.getFace(BlockFace.DOWN);
		
		if (persistence.hasPermission(player, "NetherGate.portal.create"))
		{
			boolean buildPortal = persistence.hasPermission(player, "NetherGate.portal.create.portal");
			boolean buildPlatform = persistence.hasPermission(player, "NetherGate.portal.create.platform");
			
			if (buildPortal)
			{
				buildPortal(standingBlock, BlockFace.NORTH, true, false, false, null);	
			}
			else if (buildPlatform)
			{
				buildPlatform(standingBlock, null);
			}
		}
		
		player.teleportTo(targetLocation);
	}
	

	protected Location findPlaceToStand(Location startLocation)
	{
		World world = startLocation.getWorld();

		// get player position, start from top
		int x = startLocation.getBlockX();
		int y = 125;
		int z = startLocation.getBlockZ();		
		
		// First, try built-in!
		CraftWorld cWorld = (CraftWorld)world;
		int highestY = cWorld.getHighestBlockYAt(x, z);
		
		if (highestY > 0 && highestY < 128)
		{
			Block block = world.getBlockAt(x, highestY, z);
			Block oneUp = block.getFace(BlockFace.UP);
			Block twoUp = oneUp.getFace(BlockFace.UP);
			if 
			(
				isOkToStandOn(block.getType())
			&&	isOkToStandIn(oneUp.getType())
			&& 	isOkToStandIn(twoUp.getType())
			)
			{
				return block.getLocation();
			}
		}

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
		
		for (;y > 5; y--)
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
			
	
			blocks[2] = blocks[1];				
			blocks[1] = blocks[0];
			blocks[0] = blocks[0].getFace(BlockFace.DOWN);
	
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
		while (y > 5)
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
	
	public void buildPortal(Block centerBlock, BlockFace facing, boolean platform, boolean frame, List<Block> blockList)
	{
		buildPortal(centerBlock, facing, platform, frame, true, blockList);
	}
	
	public void buildPortal(Block centerBlock, BlockFace facing, boolean platform, boolean frame, boolean portal, List<Block> blockList)
	{
		clearPortalArea(centerBlock, facing, blockList);
	
		if (frame)
		{
			//buildFrame(centerBlock, facing);
		}
		
		if (platform)
		{
			buildPlatform(centerBlock, blockList);
		}
		
		if (portal)
		{
			buildPortalBlocks(centerBlock, facing);
		}
	}
	
	protected void buildPortalBlocks(Block centerBlock, BlockFace facing)
	{
		disablePhysics();
		BoundingBox container = new BoundingBox(centerBlock.getX() - 1, centerBlock.getY() + 1, centerBlock.getZ() - 1,
				centerBlock.getX() + 1, centerBlock.getY() + 4, centerBlock.getZ());
		
		container.fill(centerBlock.getWorld(), Material.PORTAL, destructible);
		disablePhysics();
	}
	
	protected void buildFrame(Block centerBlock, BlockFace facing)
	{
		disablePhysics();
		BoundingBox container = new BoundingBox(centerBlock.getX() - 2, centerBlock.getY() + 1, centerBlock.getZ() - 1,
				centerBlock.getX() + 2, centerBlock.getY() + 5, centerBlock.getZ());
		
		container.fill(centerBlock.getWorld(), Material.OBSIDIAN, destructible);
		disablePhysics();
	}
	
	protected void clearPortalArea(Block centerBlock, BlockFace facing, List<Block> blockList)
	{
		BoundingBox container = new BoundingBox(centerBlock.getX() - 3, centerBlock.getY() + 1, centerBlock.getZ() - 3,
				centerBlock.getX() + 2, centerBlock.getY() + 5, centerBlock.getZ() + 2);
		
		container.fill(centerBlock.getWorld(), Material.AIR, destructible, blockList);
	}
	
	protected void buildPlatform(Block centerBlock, List<Block> blockList)
	{
		BoundingBox platform = new BoundingBox(centerBlock.getX() - 3, centerBlock.getY(), centerBlock.getZ() - 3,
				centerBlock.getX() + 2, centerBlock.getY() + 1, centerBlock.getZ() + 2);
		
		platform.fill(centerBlock.getWorld(), Material.OBSIDIAN, needsPlatform, blockList);
	}
	
	
	protected boolean isOkToStandIn(Material mat)
	{
		return (mat == Material.AIR || mat == Material.PORTAL);
	}

	protected boolean isOkToStandOn(Material mat)
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
	
	protected void disablePhysics()
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
		
		NetherWorld homeWorld = playerData.getHomeWorld();
		if (homeWorld != null)
		{
			BlockVector spawn = homeWorld.getWorld().getSpawn();
			World home = homeWorld.getWorld().getWorld(server);
			Location spawnPoint = new Location
			(
					home,
					spawn.getX(),
					spawn.getY(),
					spawn.getZ(),
					player.getLocation().getYaw(),
					player.getLocation().getPitch()
			);
			teleportPlayer(player, homeWorld, spawnPoint);
		}
	}
	
	public static BlockVector					origin					= new BlockVector(0, 0, 0);

	protected static final String				DEFAULT_DESTRUCTIBLES	= "0,1,2,3,4,10,11,12,13,14,15,16,21,51,56,78,79,82,87,88,89";

	protected HashMap<Material, Boolean>		destructible			= new HashMap<Material, Boolean>();
	protected HashMap<Material, Boolean>		needsPlatform			= new HashMap<Material, Boolean>();
	protected HashMap<Chunk, PlayerList>		teleporting				= new HashMap<Chunk, PlayerList>();
	protected HashMap<Chunk, NetherList>		netherMap				= new HashMap<Chunk, NetherList>();
	protected HashMap<Chunk, BlockRequestList>	requestMap				= new HashMap<Chunk, BlockRequestList>();
	protected List<PortalArea>					netherAreas				= new ArrayList<PortalArea>();
	protected World								world;
	protected Server							server;
	protected Persistence						persistence;
	protected PluginUtilities					utilities;
	protected long								disabledPhysics			= 0;
	
}
