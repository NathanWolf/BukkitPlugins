package com.elmakers.mine.bukkit.plugins.nether;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockVector;

import com.elmakers.mine.bukkit.plugins.nether.dao.NetherWorld;
import com.elmakers.mine.bukkit.plugins.nether.dao.PortalArea;
import com.elmakers.mine.bukkit.plugins.persistence.PluginUtilities;
import com.elmakers.mine.bukkit.plugins.persistence.Persistence;
import com.elmakers.mine.bukkit.plugins.persistence.PersistencePlugin;
import com.elmakers.mine.bukkit.plugins.persistence.dao.Message;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PermissionType;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PluginCommand;
import com.elmakers.mine.bukkit.plugins.persistence.dao.WorldData;

public class NetherGatePlugin extends JavaPlugin
{

	public NetherGatePlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder,
			File plugin, ClassLoader cLoader)
	{
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
	}
	
	public NetherManager getManager()
	{
		return manager;
	}

	@Override
	public void onDisable()
	{
		
	}

	@Override
	public void onEnable()
	{
		try
		{
			initialize();
			PluginDescriptionFile pdfFile = this.getDescription();
	        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
		}
		catch(Throwable e)
		{
			PluginDescriptionFile pdfFile = this.getDescription();
	        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " failed to initialize");	
	        e.printStackTrace();
		}
		
		// Hook up event listeners
		PluginManager pm = getServer().getPluginManager();
		
        pm.registerEvent(Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.CHUNK_LOADED, worldListener, Priority.Normal, this);
  }
	
	public void initialize()
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
	    	return;
	    }
	    
	    utilities = persistence.getUtilities(this);
	    manager.initialize(getServer(), persistence, utilities);
	    
		netherCommand = utilities.getPlayerCommand("nether", "Manage portal areas and worlds", "nether <command>", PermissionType.ADMINS_ONLY);
		createCommand = netherCommand.getSubCommand("create", "Create a portal area or world", "create <world | area> <name>", PermissionType.ADMINS_ONLY);
		worldCommand = createCommand.getSubCommand("world", "Create a new world", "world <name>", PermissionType.ADMINS_ONLY);
		areaCommand = createCommand.getSubCommand("area", "Create a new PortalArea underground", "area <name>", PermissionType.ADMINS_ONLY);
		kitCommand = netherCommand.getSubCommand("kit", "Give yourself a portal kit", "kit", PermissionType.ADMINS_ONLY);
		goCommand = netherCommand.getSubCommand("go", "TP to an area or world", "go [area | world]", PermissionType.ADMINS_ONLY);
		
		areaCommand.bind("onCreateArea");
		worldCommand.bind("onCreateWorld");
		goCommand.bind("onGo");
		kitCommand.bind("onKit");
		
		
		creationFailedMessage = utilities.getMessage("creationFailed", "Nether creation failed- is there enough room below you?");
		creationSuccessMessage = utilities.getMessage("creationSuccess", "Created new Nether area");
		netherExistsMessage = utilities.getMessage("netherExist", "A Nether area already exists here");
		giveKitMessage = utilities.getMessage("giveKit", "Happy portaling!");
		worldCreateMessage = utilities.getMessage("worldCreated", "World %s created");
		worldCreateFailedMessage = utilities.getMessage("worldCreateFailed", "World creation failed");
		goFailedMessage = utilities.getMessage("goFailed", "Failed teleport");
		goSuccessMessage = utilities.getMessage("goSuccess", "Going to world %s");
	}
	
	public boolean onGo(Player player, String[] parameters)
	{
		WorldData targetWorld = manager.go(player, parameters);
			
		if (targetWorld == null)
		{
			goFailedMessage.sendTo(player);
		}
		else
		{	
			goSuccessMessage.sendTo(player, targetWorld.getName());
		}
			
		return true;
	}
	
	public boolean onCreateWorld(Player player, String[] parameters)
	{
		// First, make sure this world is registered!
		World currentWorld = player.getWorld();
		
		if (parameters.length < 0)
		{
			worldCommand.sendHelp(player, "Use: ", true, true);
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

		NetherWorld world = manager.createWorld(getServer(), worldName, worldType, currentWorld);
		if (world == null)
		{
			worldCreateFailedMessage.sendTo(player);
		}
		else
		{
			worldCreateMessage.sendTo(player, world.getWorld().getName());
		}
		
		return true;
	}
	
	public boolean onKit(Player player, String[] parameters)
	{
		PlayerInventory inventory = player.getInventory();
		
		// Give a bit of obsidian
		ItemStack itemStack = new ItemStack(Material.OBSIDIAN, 32);
		inventory.addItem(itemStack);
		
		// And a flint and steel, if they don't have one
		if (!inventory.contains(Material.FLINT_AND_STEEL))
		{
			itemStack = new ItemStack(Material.FLINT_AND_STEEL, 1);
			player.getInventory().addItem(itemStack);
		}
		
		// And a diamond pickaxe (for destroying), if they don't have one
		if (!inventory.contains(Material.DIAMOND_PICKAXE))
		{
			itemStack = new ItemStack(Material.DIAMOND_PICKAXE, 1);
			player.getInventory().addItem(itemStack);
		}
		
		return true;
	}
	
	public boolean onCreateArea(Player player, String[] parameters)
	{
		// Check for an existing Nether area
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
		
		return true;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		// Currently only ops can use nether
		// TODO: Implement permissions
		if (sender instanceof Player)
		{
			Player player = (Player)sender;
			if (!player.isOp()) return true;
		}
		
		return utilities.dispatch(this, sender, cmd.getName(), args);
	}

	protected PluginCommand netherCommand;
	protected PluginCommand createCommand;
	protected PluginCommand worldCommand;
	protected PluginCommand areaCommand;
	protected PluginCommand goCommand;
	protected PluginCommand kitCommand;
	
	protected Message creationFailedMessage;
	protected Message creationSuccessMessage;
	protected Message netherExistsMessage;
	protected Message giveKitMessage;
	protected Message worldCreateMessage;
	protected Message worldCreateFailedMessage;
	protected Message goFailedMessage;
	protected Message goSuccessMessage;
	
	protected NetherManager manager = new NetherManager();
	protected NetherPlayerListener playerListener = new NetherPlayerListener(manager);
	protected NetherWorldListener worldListener = new NetherWorldListener(manager);
	
	protected Persistence persistence = null;
	protected PluginUtilities utilities = null;

	protected static final Logger log = Logger.getLogger("Minecraft");
}
