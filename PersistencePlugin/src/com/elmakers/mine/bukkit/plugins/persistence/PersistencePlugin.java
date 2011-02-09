package com.elmakers.mine.bukkit.plugins.persistence;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.plugins.persistence.core.PersistenceCommands;
import com.elmakers.mine.bukkit.plugins.persistence.core.PersistenceListener;

import com.nijikokun.bukkit.Permissions.Permissions;

/** 
 * The JavaPlugin interface for Persistence- binds Persistence to Bukkit.
 * 
 * @author NathanWolf
 *
 */
public class PersistencePlugin extends JavaPlugin
{
	/*
	 * Public API
	 */
	
	/* Process player quit and join messages.
	 * 
	 * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		return handler.process(sender, cmd, args);
	}

	/**
	 * Retrieve the singleton Persistence instance.
	 * 
	 * Use this function to get a reference to Persistence, which you can use to access the Persistence API.
	 * 
	 * @see com.elmakers.mine.bukkit.plugins.persistence.Persistence
	 * 
	 * @return The singleton instance of Persistence
	 */
	public Persistence getPersistence()
	{
		persistence = Persistence.getInstance();
		return persistence;
	}

	/*
	 * Plugin interface
	 */
	
	/**
	 * The default JavaPlugin constructor
	 * 
	 * @param pluginLoader The plugin loader instance
	 * @param instance The server instance
	 * @param desc The plugin description file
	 * @param folder The folder to use for data storage
	 * @param plugin The plugin file
	 * @param cLoader The class loader
	 */
	public PersistencePlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder,
			File plugin, ClassLoader cLoader)
	{
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
		
		pluginInstance = this;
	}

	/* Shut down Persistence, save data, clear cache
	 * 
	 * @see org.bukkit.plugin.Plugin#onDisable()
	 */
	public void onDisable()
	{
		if (persistence != null)
		{
			persistence.save();
			persistence.clear();
			persistence.disconnect();
		}
	}

	/* Start up Persistence, bind event handlers
	 * 
	 * @see org.bukkit.plugin.Plugin#onEnable()
	 */
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
	}
	
	/*
	 * Helper functions
	 */
	
	/**
	 * Retrieve the Logger used by Persistence.
	 * 
	 * Currently, this is just the Minecraft server logger.
	 * 
	 * @return The Persistence logger
	 */
	public static Logger getLogger()
	{
		return log;
	}
	
	
	/**
	 * Retrieve the instance of this plugin that was created by Bukkit.
	 * 
	 * Used internally.
	 * 
	 * @return The instance of this plugin.
	 */
	public static PersistencePlugin getInstance()
	{
		return pluginInstance;
	}	
	
	protected void initialize()
	{
		// We use persistence internally, so go ahead and initialize it.
		persistence = getPersistence();
		
		handler.initialize(this, persistence);
		listener.initialize(persistence);
		
		PluginManager pm = getServer().getPluginManager();
		
		pm.registerEvent(Type.PLAYER_QUIT, listener, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_JOIN, listener, Priority.Normal, this);
		
		bindToPermissions();
	}
	
	public void bindToPermissions()
	{
		Plugin checkForPermissions = this.getServer().getPluginManager().getPlugin("Permissions");
	    if (checkForPermissions != null) 
	    {
	    	persistence.setPermissions((Permissions)checkForPermissions);
	    	log.info("Persistence: Found Permissions, using it for permissions.");
	    } 

	}
	
	/*
	 * Private data
	 */
	
	private static PersistencePlugin pluginInstance = null;
	private final PersistenceListener listener = new PersistenceListener();
	private final PersistenceCommands handler = new PersistenceCommands();
	private Persistence persistence = null;
	private static final Logger log = Logger.getLogger("Minecraft");
	
}
