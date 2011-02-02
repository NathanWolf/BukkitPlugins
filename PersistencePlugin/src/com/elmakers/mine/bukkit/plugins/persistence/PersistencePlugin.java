package com.elmakers.mine.bukkit.plugins.persistence;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.plugins.persistence.core.PersistenceCommands;
import com.elmakers.mine.bukkit.plugins.persistence.core.PersistenceListener;

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
		if (sender instanceof Player)
		{
			return handler.process((Player)sender, cmd, args);
		}
		
		// TODO: Support server commands, etc.
		return false;
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
		initialize();
		
		PluginManager pm = getServer().getPluginManager();
		
		pm.registerEvent(Type.PLAYER_QUIT, listener, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_JOIN, listener, Priority.Normal, this);

		PluginDescriptionFile pdfFile = this.getDescription();
        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
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
		handler.initialize(this, getPersistence());
		listener.initialize(getPersistence());
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
