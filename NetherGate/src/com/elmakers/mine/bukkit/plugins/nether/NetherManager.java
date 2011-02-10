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
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.nether.dao.NetherWorld;
import com.elmakers.mine.bukkit.plugins.nether.dao.PortalArea;
import com.elmakers.mine.bukkit.plugins.nether.dao.NetherPlayer;
import com.elmakers.mine.bukkit.plugins.nether.dao.NetherPlayer.TeleportState;
import com.elmakers.mine.bukkit.plugins.persistence.PluginUtilities;
import com.elmakers.mine.bukkit.plugins.persistence.Persistence;
import com.elmakers.mine.bukkit.plugins.persistence.dao.BoundingBox;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PlayerData;
import com.elmakers.mine.bukkit.plugins.persistence.dao.WorldData;

public class NetherManager
{
	public HashMap<Material, Boolean> destructible = new HashMap<Material, Boolean>();
	public HashMap<Material, Boolean> needsPlatform = new HashMap<Material, Boolean>();
	
	static final String		DEFAULT_DESTRUCTIBLES	= "1,2,3,10,11,12,13,87,88";
	
	public static void parseMaterials(String csvList, HashMap<Material, Boolean> materials)
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

	public NetherWorld createWorld(Server server, String name, Environment defaultType, World currentWorld)
	{
		NetherWorld current = getWorldData(currentWorld);
		if (current == null) return null;
		
		WorldData world = utilities.getWorld(server, name, defaultType);
		NetherWorld worldData = persistence.get(world, NetherWorld.class);
		if (worldData == null)
		{
			worldData = new NetherWorld(world);
			worldData.autoBind(current);
			persistence.put(current);
			persistence.put(worldData);
			persistence.put(worldData.getTargetOffset());
			persistence.put(worldData.getCenterOffset());
		}
		
		return worldData;
	}
	
	public NetherWorld getWorldData(World world)
	{
		WorldData worldData = utilities.getWorld(server, world);
		return getWorldData(worldData);
	}
	
	public NetherWorld getWorldData(WorldData worldData)
	{
		if (worldData == null) return null;
		
		NetherWorld netherData = persistence.get(worldData, NetherWorld.class);
		if (netherData == null)
		{
			netherData = new NetherWorld(worldData);
			persistence.put(netherData);
		}
		return netherData;
	}
	
	public NetherWorld getNextWorld(World currentWorld)
	{
		NetherWorld thisWorldData = getWorldData(currentWorld);
		if (thisWorldData == null) return null;
		
		NetherWorld targetWorld = thisWorldData.getTargetWorld();
		
		// Auto-create a default nether world if this is the only one
		if (targetWorld == null || targetWorld == thisWorldData)
		{
			targetWorld = createWorld(server, "nether", Environment.NETHER, currentWorld);
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
	
	/*
	 * Player teleportation
	 */
	
	public boolean teleportPlayer(Player player, WorldData targetWorldData, Location targetLocation)
	{
		NetherWorld targetWorld = getWorldData(targetWorldData);
		if (targetWorld == null) return false;
		
		return teleportPlayer(player, targetWorld, targetLocation);
	}
	
	public BlockVector mapLocation(NetherWorld from, NetherWorld to, BlockVector target)
	{
		int originalY = target.getBlockY();
		Vector transformed = target;
		
		// First, offset to center on local spawn (making sure there is one set)
		/*
		BlockVector fromSpawn = from.getWorld().getSpawn();
		if (fromSpawn != null)
		{
			transformed.subtract(fromSpawn);
		}
		
		// Apply additional offset
		transformed.subtract(from.getCenterOffset());
		*/
		// Scale
		double fromScale = from.getScale();
		double toScale = to.getScale();
		if (fromScale != 0 && toScale != 0)
		{
			transformed.multiply(fromScale / toScale);
		}
		
		// Unwind
		/*
		transformed.add(to.getCenterOffset());
		
		BlockVector toSpawn = to.getWorld().getSpawn();
		if (toSpawn != null)
		{
			transformed.add(toSpawn);
		}
		*/
		transformed.setY(originalY);
		
		return new BlockVector(transformed);
	}

	public boolean teleportPlayer(Player player, NetherWorld targetWorld, Location targetLocation)
	{
		NetherPlayer tpPlayer = getPlayerData(player);
		if (tpPlayer == null) return false;
		
		// Register the current world first, in case it's not already.
		NetherWorld currentWorld = getWorldData(player.getWorld());
		if (currentWorld == null) return false;
			
		BlockVector target = new BlockVector(targetLocation.getBlockX(), targetLocation.getBlockY(), targetLocation.getBlockZ());
		
		tpPlayer.setTargetLocation(target);
		tpPlayer.setTargetWorld(targetWorld);
		tpPlayer.setSourceWorld(currentWorld);
		tpPlayer.setSourceArea(null);
		tpPlayer.setTargetArea(null);
		tpPlayer.setSourcePortal(null);
		tpPlayer.setTargetPortal(null);
		
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
	
	public void startTeleport(Player player)
	{
		startTeleport(player, getNextWorld(player.getWorld()));
	}
	
	public boolean startTeleport(Player player, NetherWorld targetWorld)
	{
		return teleportPlayer(player, targetWorld, player.getLocation());
	}
	
	public void onPlayerMove(Player player)
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
	
	public WorldData go(Player player, String[] parameters)
	{
		NetherWorld targetWorld = null;
		if (parameters.length > 0)
		{
			WorldData world = persistence.get(parameters[0], WorldData.class);
			if (world != null)
			{
				targetWorld = getWorldData(world);
			}
		}
		else
		{
			World currentWorld = player.getWorld();
			WorldData thisWorld = utilities.getWorld(server, currentWorld);
			if (thisWorld != null)
			{
				NetherWorld thisWorldData = getWorldData(thisWorld);
				if (thisWorldData != null)
				{
					targetWorld = thisWorldData.getTargetWorld();
				}
				
				// Auto-create a default nether world if this is the only one
				if (targetWorld == null || targetWorld == thisWorldData)
				{
					targetWorld = createWorld(server, "nether", Environment.NETHER, currentWorld);
					targetWorld.setScale(8);
				}
			}
		}
		
		if (targetWorld != null)
		{
			Location location = player.getLocation();
			if (!teleportPlayer(player, targetWorld, location))
			{
				targetWorld = null;
			}
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
			teleportTo(player, targetLocation);
			playerData.update(player);
		}
		playerData.setState(TeleportState.TELEPORTED);
		persistence.put(playerData);
	}	
	
	public void teleportTo(Player player, Location location)
	{
		if (location == null) return;
		
		// Look up first, then down
		Location targetLocation = findPlaceToStand(location, true);
		if (targetLocation == null)
		{
			targetLocation = findPlaceToStand(location, false);
		}
		if (targetLocation == null)
		{
			targetLocation = location;
		}
		
		// Make sure we're not left hanging, look for ground below
		// THis is kind of "belt and suspenders"- may remove it later when findPlacetoStand is more reliable.
		Block standingBlock = location.getWorld().getBlockAt(targetLocation.getBlockX(), targetLocation.getBlockY(), targetLocation.getBlockZ());
		standingBlock = standingBlock.getFace(BlockFace.DOWN);
		Material standingMaterial = standingBlock.getType();
		int startY = standingBlock.getY();
		int maxSearch = 16;
		int i = 0;
		while ( i < maxSearch && standingMaterial == Material.AIR)
		{
			int y = startY - i;
			if (y < 4) break;
			standingBlock = standingBlock.getFace(BlockFace.DOWN);
			standingMaterial = standingBlock.getType();
			i++;
		}
		
		buildPortal(standingBlock, BlockFace.NORTH, true, true);
		
		player.teleportTo(targetLocation);
	}
	
	protected void buildPortal(Block centerBlock, BlockFace facing, boolean platform, boolean frame)
	{
		clearPortalArea(centerBlock, facing);
		
		if (platform)
		{
			buildPlatform(centerBlock);
		}
		
		buildPortalBlocks(centerBlock, facing);
		
	}
	
	protected void buildPortalBlocks(Block centerBlock, BlockFace facing)
	{
		disablePhysics();
		BoundingBox container = new BoundingBox(centerBlock.getX() - 2, centerBlock.getY() + 2, centerBlock.getZ() - 2,
				centerBlock.getX(), centerBlock.getY() + 4, centerBlock.getZ());
		
		container.fill(centerBlock.getWorld(), Material.PORTAL, destructible);
		disablePhysics();
	}
	
	protected void clearPortalArea(Block centerBlock, BlockFace facing)
	{
		BoundingBox container = new BoundingBox(centerBlock.getX() - 3, centerBlock.getY() + 1, centerBlock.getZ() - 3,
				centerBlock.getX() + 1, centerBlock.getY() + 5, centerBlock.getZ() + 1);
		
		container.fill(centerBlock.getWorld(), Material.AIR, destructible);
	}
	
	protected void buildPlatform(Block centerBlock)
	{
		BoundingBox platform = new BoundingBox(centerBlock.getX() - 3, centerBlock.getY(), centerBlock.getZ() - 3,
				centerBlock.getX() + 2, centerBlock.getY() + 1, centerBlock.getZ() + 2);
		
		platform.fill(centerBlock.getWorld(), Material.OBSIDIAN, needsPlatform);
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
		int x = startLocation.getBlockX();
		int y = startLocation.getBlockY() - 1;
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
		
		while (y > 5 && y < 125)
		{
			if 
			(
				isOkToStandOn(mats[0])
			&&	isOkToStandIn(mats[1])
			&& 	isOkToStandIn(mats[2])
			)
			{
				// spot found - return location
				return new Location(world, x, y + 1, z, startLocation.getYaw(), startLocation.getPitch());
			}
			
			if (goUp)
			{
				blocks[0] = blocks[1];
				blocks[1] = blocks[2];
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
			y += step;
		}

		// no spot found
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
			
			return false;
		}
		return (block.getType() != Material.PORTAL);
	}
	
	public void disablePhysics()
	{
		disabledPhysics = System.currentTimeMillis() + 5000;
	}
	
	public static BlockVector origin = new BlockVector(0, 0, 0);

	protected HashMap<Chunk, PlayerList>	teleporting	= new HashMap<Chunk, PlayerList>();
	protected HashMap<Chunk, NetherList>	netherMap	= new HashMap<Chunk, NetherList>();
	protected List<PortalArea>				netherAreas	= new ArrayList<PortalArea>();
	protected World							world;
	protected Server						server;
	protected Persistence					persistence;
	protected PluginUtilities				utilities;
	protected long							disabledPhysics = 0;
}
