package com.elmakers.mine.bukkit.plugins.nether;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.minecraft.server.WorldServer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockVector;

import com.elmakers.mine.bukkit.persistence.dao.LocationData;
import com.elmakers.mine.bukkit.persistence.dao.Message;
import com.elmakers.mine.bukkit.persistence.dao.PermissionType;
import com.elmakers.mine.bukkit.persistence.dao.PluginCommand;
import com.elmakers.mine.bukkit.persistence.dao.WorldData;
import com.elmakers.mine.bukkit.plugins.nether.dao.NetherPlayer;
import com.elmakers.mine.bukkit.plugins.nether.dao.NetherWorld;
import com.elmakers.mine.bukkit.plugins.persistence.PersistencePlugin;
import com.elmakers.mine.bukkit.utilities.PluginUtilities;
import com.elmakers.mine.craftbukkit.persistence.Persistence;

public class NetherGatePlugin extends JavaPlugin
{
	public NetherManager getManager()
	{
		return manager;
	}

	public void onDisable()
	{
		
	}

	public void onEnable()
	{
		try
		{
			if (!initialize())
			{
				throw new Exception("Initialization returned false");
			}
			PluginDescriptionFile pdfFile = this.getDescription();
	        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
		}
		catch(Throwable e)
		{
			PluginDescriptionFile pdfFile = this.getDescription();
	        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " failed to initialize");	
	        e.printStackTrace();
	        return;
		}
		
		// Hook up event listeners
		PluginManager pm = getServer().getPluginManager();
		
        pm.registerEvent(Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_LOGIN, playerListener, Priority.Monitor, this);
        pm.registerEvent(Type.CHUNK_LOADED, worldListener, Priority.Normal, this);
        pm.registerEvent(Type.BLOCK_PHYSICS, physicsListener, Priority.Normal, this);
        pm.registerEvent(Type.ENTITY_DAMAGED, entityListener, Priority.Normal, this);
    }
	
	public boolean initialize()
	{
		Plugin checkForPersistence = this.getServer().getPluginManager().getPlugin("Persistence");
	    if(checkForPersistence != null) 
	    {
	    	PersistencePlugin plugin = (PersistencePlugin)checkForPersistence;
	    	persistence = plugin.getPersistence();
	    } 
	    else 
	    {
	    	log.warning("The NetherGate plugin depends on Persistence");
	    	this.getServer().getPluginManager().disablePlugin(this);
	    	return false;
	    }
	    
	    utilities = persistence.getUtilities(this);
	    manager.initialize(getServer(), persistence, utilities);
	    
		netherCommand = utilities.getGeneralCommand("nether", "Manage portal areas and worlds", null, PermissionType.ADMINS_ONLY);
		createCommand = netherCommand.getSubCommand("create", "Create a portal area or world", null, PermissionType.ADMINS_ONLY);
		worldCommand = createCommand.getSubCommand("world", "Create a new world", "<name>", PermissionType.ADMINS_ONLY);
		areaCommand = createCommand.getSubCommand("area", "Create a new PortalArea underground", "<name>", PermissionType.ADMINS_ONLY);
		kitCommand = netherCommand.getSubCommand("kit", "Give yourself a portal kit", null, PermissionType.ADMINS_ONLY);
		goCommand = netherCommand.getSubCommand("go", "TP to an area or world", "[name]", PermissionType.ADMINS_ONLY);
		deleteCommand = netherCommand.getSubCommand("delete", "Delete an area or world", null, PermissionType.ADMINS_ONLY);
		deleteWorldCommand = deleteCommand.getSubCommand("world", "Delete an world", "<name>", PermissionType.ADMINS_ONLY);
		targetCommand = netherCommand.getSubCommand("target", "Re-target worlds or areas", null, PermissionType.ADMINS_ONLY);
		targetWorldCommand = targetCommand.getSubCommand("world", "Re-target a world", "<from> <to>", PermissionType.ADMINS_ONLY);
		scaleCommand = netherCommand.getSubCommand("scale", "Re-scale an area or world", "<world | area> <name> <scale>", PermissionType.ADMINS_ONLY); 
		scaleWorldCommand = scaleCommand.getSubCommand("world", "Re-scale a world", "<name> <scale>", PermissionType.ADMINS_ONLY); 
		centerCommand = netherCommand.getSubCommand("center", "Re-center an area or world", "<world | area> <name> <X> <Y> <Z>", PermissionType.ADMINS_ONLY); 
		centerWorldCommand = centerCommand.getSubCommand("world", "Re-center a world", "<name> <X> <Y> <Z>", PermissionType.ADMINS_ONLY); 
		listCommand = netherCommand.getSubCommand("list", "List worlds, areas and portals", null, PermissionType.ADMINS_ONLY);
		listWorldsCommand = listCommand.getSubCommand("worlds", "List all known worlds", null, PermissionType.ADMINS_ONLY);

		setHomeCommand = netherCommand.getSubCommand("sethome", "Set your home world and location", null); 
		goHomeCommand = netherCommand.getSubCommand("home", "Go to your home world and location", null);
		compassCommand = netherCommand.getSubCommand("compass", "Get your current location", null);
		
		spawnCommand = netherCommand.getSubCommand("spawn", "Return you to spawn", null);

		// This one is a little weird, and bears specific permissions testing.
		// I want "/spawn" to be publicly available, but "spawn set" to be admin-only. Should work.
		setSpawnCommand = spawnCommand.getSubCommand("set", "Set the current world's spawn point", null, PermissionType.ADMINS_ONLY);
		cleanSpawnCommand = spawnCommand.getSubCommand("clean", "Get rid of any lava in the spawn area", "<world>", PermissionType.ADMINS_ONLY);
		
		areaCommand.bind("onCreateArea");
		worldCommand.bind("onCreateWorld");
		goCommand.bind("onGo");
		kitCommand.bind("onKit");
		deleteWorldCommand.bind("onDeleteWorld");
		targetWorldCommand.bind("onTargetWorld");
		scaleWorldCommand.bind("onScaleWorld");
		setSpawnCommand.bind("onSetSpawn");
		goHomeCommand.bind("onGoHome");
		setHomeCommand.bind("onSetHome");
		centerWorldCommand.bind("onCenterWorld");
		listWorldsCommand.bind("onListWorlds");
		compassCommand.bind("onCompass");
		spawnCommand.bind("onGoSpawn");
		cleanSpawnCommand.bind("onCleanSpawn");
		
		creationFailedMessage = utilities.getMessage("creationFailed", "Nether creation failed- is there enough room below you?");
		creationSuccessMessage = utilities.getMessage("creationSuccess", "Created new Nether area");
		netherExistsMessage = utilities.getMessage("netherExist", "A Nether area already exists here");
		giveKitMessage = utilities.getMessage("giveKit", "Happy portaling!");
		worldCreateMessage = utilities.getMessage("worldCreated", "World %s created");
		worldCreateFailedMessage = utilities.getMessage("worldCreateFailed", "World creation failed");
		goFailedMessage = utilities.getMessage("goFailed", "Failed teleport");
		goSuccessMessage = utilities.getMessage("goSuccess", "Going to world %s");
		retargtedWorldMessage = utilities.getMessage("retargedWorld", "Retargeted world %s to %s");
		deletedWorldMessage = utilities.getMessage("deletedWorld", "Deleted world %s");
		noWorldMessage = utilities.getMessage("noWorld", "Can't find world %s");
		scaledWorldMessage = utilities.getMessage("scaleWorld", "Re-scaled world %s to %.2f");
		invalidNumberMessage = utilities.getMessage("invalidNumber", "'%s' is not a number");
		disableScaleMessage = utilities.getMessage("disableScale", "Disabling scaling for world %s");
		spawnSetMessage = utilities.getMessage("setSpawn", "The spawn for world %s now set to (%d, %d, %d)");
		spawnSetFailedMessage = utilities.getMessage("setSpawnfailed", "Couldn't set the spawn, sorry!");
		homeSetMessage = utilities.getMessage("setHome", "Set your home to (%d, %d, %d) in %s");
		homeSetFailedMessage = utilities.getMessage("setSpawnfailed", "Couldn't set your home, sorry!");
		goHomeFailedMessage = utilities.getMessage("goHomeFailed", "Couldn't go home, sorry!");
		goHomeSuccessMessage = utilities.getMessage("goHome", "Going home!");
		noHomeMessage = utilities.getMessage("nohome", "Use sethome to set your home");
		centeredWorldMessage = utilities.getMessage("centerWorld", "World %s centered around (%d,%d,%d)");
		listWorldMessage = utilities.getMessage("listWorlds", "%s (%s) : %dx -> %s");
		compassMessage = utilities.getMessage("compass", "%d,%d,%d in %s");
		playerCommandMessage = utilities.getMessage("playerCommand", "Hm- not too sure about that, server boy!");
		spawnGoMessage = utilities.getMessage("goSpawn", "Returning you to spawn in %s");
		spawnGoFailedMessage = utilities.getMessage("goSpawnFailed", "Failed to go to spawn");
		cleanSpawnMessage = utilities.getMessage("createSpawn", "Cleaned up %d lava blocks in %s");
		cleanSpawnFailedMessage = utilities.getMessage("createSpawnFailed", "Failed to clean spawn in %s");
		requiresWorldMessage = utilities.getMessage("requiresWorld", "The world parameter is required");
		
		return true;
	}
	
	public boolean onDeleteWorld(CommandSender sender, String[] parameters)
	{
		if (parameters.length < 1)
		{
			return false;
		}
		
		NetherWorld worldData = null;
		String worldName = parameters[0];
		
		WorldData world = persistence.get(worldName, WorldData.class);
		if (world != null)
		{
			worldData = manager.getWorldData(world);
		}
		
		if (worldData == null)
		{
			noWorldMessage.sendTo(sender, worldName);
			return true;
		}
		
		List<NetherWorld> allWorlds = new ArrayList<NetherWorld>();
		persistence.getAll(allWorlds, NetherWorld.class);
		
		// Re-target any worlds targeting this one to themselves
		for (NetherWorld checkWorld : allWorlds)
		{
			if (checkWorld.getTargetWorld() == worldData)
			{
				checkWorld.setTargetWorld(checkWorld);
				persistence.put(checkWorld);
			}
		}
		
		persistence.remove(worldData);
		
		deletedWorldMessage.sendTo(sender, worldName);
		
		return true;
	}
	
	public boolean onCompass(CommandSender sender, String[] parameters)
	{
		if (!(sender instanceof Player))
		{
			playerCommandMessage.sendTo(sender);
			return true;
		}
		Player player = (Player)sender;
		NetherWorld currentWorld = manager.getCurrentWorld(player.getWorld());
		if (currentWorld == null)
		{
			noWorldMessage.sendTo(player, "unknown");
		}
		Location playerLocation = player.getLocation();
		int x = playerLocation.getBlockX();
		int y = playerLocation.getBlockY();
		int z = playerLocation.getBlockZ();
		compassMessage.sendTo(player, x, y, z, currentWorld.getWorld().getName());
		return true;
	}
	
	// "%s (%s) : %dx -> %s"
	public boolean onListWorlds(CommandSender sender, String[] parameters)
	{
		manager.loadWorlds();
		
		List<NetherWorld> allWorlds = new ArrayList<NetherWorld>();
		persistence.getAll(allWorlds, NetherWorld.class);
		
		for (NetherWorld checkWorld : allWorlds)
		{
			NetherWorld targetWorld = checkWorld.getTargetWorld();
			double scale = checkWorld.getScale();
			String worldName = "(unknown)";
			String targetName = "(unknown)";
			String envType = "(unknown)";
			if (checkWorld != null && checkWorld.getWorld() != null)
			{
				worldName = checkWorld.getWorld().getName();
				envType = checkWorld.getWorld().getEnvironmentType() == Environment.NETHER ? "nether" : "normal";
			}
			if (targetWorld != null && targetWorld.getWorld() != null)
			{
				targetName = targetWorld.getWorld().getName();
			}
			listWorldMessage.sendTo(sender, worldName, envType, (int)scale, targetName);
		}
		
		return true;
	}
	
	public boolean onScaleWorld(CommandSender sender, String[] parameters)
	{
		if (parameters.length < 2)
		{
			return false;
		}
		
		NetherWorld worldData = null;
		String worldName = parameters[0];
		
		WorldData world = persistence.get(worldName, WorldData.class);
		if (world != null)
		{
			worldData = manager.getWorldData(world);
		}
		
		if (worldData == null)
		{
			noWorldMessage.sendTo(sender, worldName);
			return true;
		}
		
		double scale = 0;
		String scaleText = parameters[1];
		try
		{
			scale = Double.parseDouble(scaleText);
		}
		catch(Throwable ex)
		{
			invalidNumberMessage.sendTo(sender, scaleText);
			return true;
		}
		
		if (scale <= 0.01)
		{
			scale = 0;
			disableScaleMessage.sendTo(sender, worldName);
		}
				
		worldData.setScale(scale);
		persistence.put(worldData);
		
		if (scale != 0)
		{
			scaledWorldMessage.sendTo(sender, worldName, scale);
		}
		
		return true;
	}
	
	public boolean onCenterWorld(CommandSender sender, String[] parameters)
	{
		if (parameters.length < 4)
		{
			return false;
		}
		
		NetherWorld worldData = null;
		String worldName = parameters[0];
		
		WorldData world = persistence.get(worldName, WorldData.class);
		if (world != null)
		{
			worldData = manager.getWorldData(world);
		}
		
		if (worldData == null)
		{
			noWorldMessage.sendTo(sender, worldName);
			return true;
		}
		
		int x = 0;
		int y = 0;
		int z = 0;
		String currentCheck = parameters[1];
		try
		{
			x = Integer.parseInt(currentCheck);
			currentCheck = parameters[2];
			y = Integer.parseInt(currentCheck);
			currentCheck = parameters[3];
			z = Integer.parseInt(currentCheck);
		}
		catch(Throwable ex)
		{
			invalidNumberMessage.sendTo(sender, currentCheck);
			return true;
		}
		
		BlockVector newCenter = new BlockVector(x, y, z);
		worldData.setCenterOffset(newCenter);
		persistence.put(worldData);
	
		centeredWorldMessage.sendTo(sender, worldName, x, y, z);
		
		return true;
	}
	
	public boolean onTargetWorld(CommandSender sender, String[] parameters)
	{
		if (parameters.length < 2)
		{
			return false;
		}
		
		NetherWorld fromWorld = null;
		NetherWorld toWorld = null;
		String fromWorldName = parameters[0];
		String toWorldName = parameters[1];
		
		WorldData world = persistence.get(fromWorldName, WorldData.class);
		if (world != null)
		{
			fromWorld = manager.getWorldData(world);
		}
		
		if (fromWorld == null)
		{
			noWorldMessage.sendTo(sender, fromWorldName);
			return true;
		}
		
		world = persistence.get(toWorldName, WorldData.class);
		if (world != null)
		{
			toWorld = manager.getWorldData(world);
		}
		
		if (toWorld == null)
		{
			noWorldMessage.sendTo(sender, toWorldName);
			return true;
		}
				
		fromWorld.setTargetWorld(toWorld);
		persistence.put(fromWorld);
		
		retargtedWorldMessage.sendTo(sender, fromWorldName, toWorldName);
		
		return true;
	}
	
	public boolean onSetSpawn(CommandSender sender, String[] parameters)
	{
		if (!(sender instanceof Player))
		{
			playerCommandMessage.sendTo(sender);
			return true;
		}
		Player player = (Player)sender;
		String worldName = null;
		NetherWorld targetWorld = null;
		if (parameters.length > 0)
		{
			worldName = parameters[0];
			targetWorld = manager.getWorld(worldName, player.getWorld());
		}
		else
		{
			targetWorld = manager.getCurrentWorld(player.getWorld());
		}
		
		if (targetWorld == null)
		{
			if (worldName != null)
			{
				noWorldMessage.sendTo(player, worldName);	
			}
			else
			{
				spawnSetFailedMessage.sendTo(player);
				return true;
			}
		}
		
		WorldData worldData = targetWorld.getWorld();
		if (worldData == null)
		{
			spawnSetFailedMessage.sendTo(player);
			return true;
		}
		
		World world = worldData.getWorld();
		if (world == null)
		{
			spawnSetFailedMessage.sendTo(player);
			return true;
		}
		
		worldData.update(world);
		persistence.put(worldData);
	
		
		CraftWorld cWorld = (CraftWorld)world;
		int x = player.getLocation().getBlockX();
		int y = player.getLocation().getBlockY();
		int z = player.getLocation().getBlockZ();
		

		WorldServer wServer = cWorld.getHandle();
		wServer.q.a(x, y, z);
		
		spawnSetMessage.sendTo(player, worldData.getName(), x, y, z);
		
		return true;
	}
	
	public boolean onSetHome(CommandSender sender, String[] parameters)
	{
		if (!(sender instanceof Player))
		{
			playerCommandMessage.sendTo(sender);
			return true;
		}
		Player player = (Player)sender;
		NetherWorld currentWorld = manager.getCurrentWorld(player.getWorld());
		
		if (currentWorld == null || currentWorld.getWorld() == null)
		{
			homeSetFailedMessage.sendTo(player);
			return true;
		}
		
		NetherPlayer playerData = manager.getPlayerData(player);
		if (playerData == null)
		{
			homeSetFailedMessage.sendTo(player);
			return true;
		}
		
		int x = player.getLocation().getBlockX();
		int y = player.getLocation().getBlockY();
		int z = player.getLocation().getBlockZ();

		LocationData location = new LocationData(player.getLocation());
		playerData.setHome(location);
		persistence.put(playerData);
		
		homeSetMessage.sendTo(player, x, y, z, currentWorld.getWorld().getName());
	
		return true;
	}
	
	public boolean onGoHome(CommandSender sender, String[] parameters)
	{
		if (!(sender instanceof Player))
		{
			playerCommandMessage.sendTo(sender);
			return true;
		}
		Player player = (Player)sender;
		NetherPlayer playerData = manager.getPlayerData(player);
		if (playerData == null)
		{
			goHomeFailedMessage.sendTo(player);
			return true;
		}
		
		LocationData home = playerData.getHome();
		if (home == null)
		{
			noHomeMessage.sendTo(player);
			return true;
		}
		
		NetherWorld homeWorld = manager.getWorldData(home.getWorld());
		if (homeWorld == null)
		{
			noHomeMessage.sendTo(player);
			return true;
		}
		
		Location location = home.getLocation();
		if (!manager.startTeleport(player, homeWorld, location))
		{
			goHomeFailedMessage.sendTo(player);
		}
		else
		{	
			goHomeSuccessMessage.sendTo(player);
		}
			
		return true;
	}
	
	public boolean onGoSpawn(CommandSender sender, String[] parameters)
	{
		if (!(sender instanceof Player))
		{
			playerCommandMessage.sendTo(sender);
			return true;
		}
		Player player = (Player)sender;
		NetherPlayer playerData = manager.getPlayerData(player);
		if (playerData == null)
		{
			spawnGoFailedMessage.sendTo(player);
			return true;
		}
		
		NetherWorld currentWorld = manager.getCurrentWorld(player.getWorld());
		if (currentWorld == null || currentWorld.getWorld() == null || currentWorld.getWorld().getSpawn() == null)
		{
			spawnGoFailedMessage.sendTo(player);
			return true;
		}
		BlockVector spawn = currentWorld.getWorld().getSpawn();
		if (!manager.startTeleport(player, currentWorld, new Location(player.getWorld(), spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ())))
		{
			spawnGoFailedMessage.sendTo(player);
		}
		else
		{	
			spawnGoMessage.sendTo(player, player.getWorld().getName());
		}
			
		return true;
	}
	
	public boolean onCleanSpawn(CommandSender sender, String[] parameters)
	{
		manager.loadWorlds();
		
		World targetWorld = null;
		String worldName = "unknown";
		if (parameters.length > 0)
		{
			worldName = parameters[0];
			WorldData requestedWorld = persistence.get(worldName, WorldData.class);
			if (requestedWorld != null)
			{
				targetWorld = requestedWorld.getWorld();
			}
		}
		else
		{
			if (!(sender instanceof Player))
			{
				requiresWorldMessage.sendTo(sender);
				return true;
			}
			Player player = (Player)sender;
			targetWorld = player.getWorld();
			worldName = targetWorld.getName();
		}
		
		if (targetWorld == null)
		{
			noWorldMessage.sendTo(sender, worldName);
			return true;
		}
		
		Location spawn = targetWorld.getSpawnLocation();
		if (spawn == null)
		{
			cleanSpawnFailedMessage.sendTo(sender, worldName);
		}
			
		int blocksCleaned = 0;
		Block spawnBlock = targetWorld.getBlockAt(spawn);

		for (int dx = -8; dx < 8; dx++)
		{
			for (int dz = -8; dz < 8; dz++)
			{
				int dy = -1;
				Block current = spawnBlock.getRelative(dx, dy, dz);
				// Go down until we hit something solid
				while (current.getType() == Material.AIR && dy > -64)
				{
					current = current.getFace(BlockFace.DOWN);
					dy--;
				}
				
				// Make any ground-level lava blocks obsidian
				if (isLava(current.getType()))
				{
					current.setType(Material.OBSIDIAN);
					blocksCleaned++;
				}
				
				// Go up and look for more lava!
				// Note that spilling may occur- you may still have to do manual cleanup!
				while (dy < 120)
				{
					current = current.getFace(BlockFace.UP);
					if (isLava(current.getType()))
					{
						current.setType(Material.AIR);
						blocksCleaned++;
					}
					dy++;
				}
			}
		}
		
		cleanSpawnMessage.sendTo(sender, blocksCleaned, worldName);
		
		return true;
	}
	
	public static boolean isLava(Material mat)
	{
		return (mat == Material.LAVA || mat == Material.STATIONARY_LAVA);
	}
	
	public boolean onGo(CommandSender sender, String[] parameters)
	{
		if (!(sender instanceof Player))
		{
			playerCommandMessage.sendTo(sender);
			return true;
		}
		Player player = (Player)sender;
		String worldName = null;
		if (parameters.length > 0)
		{
			worldName = parameters[0];
		}
		WorldData targetWorld = manager.go(player, worldName);
			
		if (targetWorld == null)
		{
			if (worldName != null)
			{
				noWorldMessage.sendTo(player, worldName);	
			}
			else
			{
				goFailedMessage.sendTo(player);
			}
		}
		else
		{	
			goSuccessMessage.sendTo(player, targetWorld.getName());
		}
			
		return true;
	}
	
	public boolean onCreateWorld(CommandSender sender, String[] parameters)
	{
		World currentWorld = null;
		if (sender instanceof Player)
		{
			Player player = (Player)sender;
			currentWorld = player.getWorld();
		}
		if (parameters.length < 0)
		{
			worldCommand.sendHelp(sender, "Use: ", true, true);
			return true;
		}
		
		String worldName = parameters[0];
		Environment worldType = Environment.NETHER;
			
		for (int i = 1; i < parameters.length; i++)
		{
			if (parameters[i].equalsIgnoreCase("normal"))
			{
				worldType = Environment.NORMAL;
			}
		}

		NetherWorld world = manager.createWorld(worldName, worldType, currentWorld);
		if (world == null)
		{
			worldCreateFailedMessage.sendTo(sender);
		}
		else
		{
			worldCreateMessage.sendTo(sender, world.getWorld().getName());
		}
		
		return true;
	}
	
	public boolean onKit(CommandSender sender, String[] parameters)
	{
		if (!(sender instanceof Player))
		{
			playerCommandMessage.sendTo(sender);
			return true;
		}
		Player player = (Player)sender;
		PlayerInventory inventory = player.getInventory();
		
		// Give a bit of obsidian
		// Try to play nice with Spells by putting the materials
		// on the right, if possible
		ItemStack itemStack = new ItemStack(Material.OBSIDIAN, 32);
		ItemStack[] items = inventory.getContents();
		boolean inActive = false;
		for (int i = 8; i >= 0; i--)
		{
			if (items[i] == null || items[i].getType() == Material.AIR)
			{
				inventory.setItem(i, itemStack);
				inActive = true;
				break;
			}
		}
		
		if (!inActive)
		{
			inventory.addItem(itemStack);
		}
		
		// And a flint and steel, if they don't have one
		if (!inventory.contains(Material.FLINT_AND_STEEL))
		{
			ItemStack flintItem = new ItemStack(Material.FLINT_AND_STEEL, 1);
			player.getInventory().addItem(flintItem);
		}
		
		// And a diamond pickaxe (for destroying), if they don't have one
		if (!inventory.contains(Material.DIAMOND_PICKAXE))
		{
			// Try to play nice with Spells by putting the materials
			// on the right, if possible
			ItemStack pickAxeItem = new ItemStack(Material.DIAMOND_PICKAXE, 1);
			inActive = false;
			for (int i = 8; i >= 0; i--)
			{
				if (items[i] == null || items[i].getType() == Material.AIR)
				{
					inventory.setItem(i, pickAxeItem);
					inActive = true;
					break;
				}
			}
			
			if (!inActive)
			{
				inventory.addItem(pickAxeItem);
			}
		}
		
		return true;
	}
	
	public boolean onCreateArea(CommandSender sender, String[] parameters)
	{
		// Check for an existing Nether area
		/*
		Location location = player.getLocation();
		PortalArea nether = manager.getNether(new BlockVector(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
		if (nether != null)
		{
			netherExistsMessage.sendTo(player);
			return true;
		}
		
		if (!manager.createArea(player))
		{
			creationFailedMessage.sendTo(player);
		}
		else
		{
			creationSuccessMessage.sendTo(player);
		}
		*/
		sender.sendMessage("Not implemented yet!");
		
		return true;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		return utilities.dispatch(this, sender, cmd.getName(), args);
	}

	protected PluginCommand netherCommand;
	protected PluginCommand createCommand;
	protected PluginCommand worldCommand;
	protected PluginCommand areaCommand;
	protected PluginCommand goCommand;
	protected PluginCommand kitCommand;
	protected PluginCommand targetCommand;
	protected PluginCommand targetWorldCommand;
	protected PluginCommand deleteCommand;
	protected PluginCommand deleteWorldCommand;
	protected PluginCommand scaleCommand;
	protected PluginCommand scaleWorldCommand;
	protected PluginCommand setSpawnCommand;
	protected PluginCommand spawnCommand;
	protected PluginCommand setHomeCommand;
	protected PluginCommand goHomeCommand;
	protected PluginCommand centerWorldCommand;
	protected PluginCommand centerCommand;
	protected PluginCommand listWorldsCommand;
	protected PluginCommand listCommand;
	protected PluginCommand compassCommand;
	protected PluginCommand cleanSpawnCommand;
	
	protected Message creationFailedMessage;
	protected Message creationSuccessMessage;
	protected Message netherExistsMessage;
	protected Message giveKitMessage;
	protected Message worldCreateMessage;
	protected Message worldCreateFailedMessage;
	protected Message goFailedMessage;
	protected Message goSuccessMessage;
	protected Message retargtedWorldMessage;
	protected Message deletedWorldMessage;
	protected Message noWorldMessage;
	protected Message scaledWorldMessage;
	protected Message centeredWorldMessage;
	protected Message invalidNumberMessage;
	protected Message disableScaleMessage;
	protected Message spawnGoMessage;
	protected Message spawnGoFailedMessage;
	protected Message spawnSetMessage;
	protected Message spawnSetFailedMessage;
	protected Message homeSetMessage;
	protected Message homeSetFailedMessage;
	protected Message goHomeFailedMessage;
	protected Message goHomeSuccessMessage;
	protected Message noHomeMessage;
	protected Message listWorldMessage;
	protected Message compassMessage;
	protected Message playerCommandMessage;
	protected Message cleanSpawnMessage;
	protected Message cleanSpawnFailedMessage;
	protected Message requiresWorldMessage;
	
	protected NetherManager manager = new NetherManager();
	protected NetherPlayerListener playerListener = new NetherPlayerListener(manager);
	protected NetherWorldListener worldListener = new NetherWorldListener(manager);
	protected NetherBlockListener physicsListener = new NetherBlockListener(manager);
	protected NetherEntityListener entityListener = new NetherEntityListener(manager);
	
	protected Persistence persistence = null;
	protected PluginUtilities utilities = null;

	protected static final Logger log = Persistence.getLogger();
}
