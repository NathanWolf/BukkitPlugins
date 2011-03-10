package com.elmakers.mine.bukkit.plugins.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.permission.GroupManager;
import com.elmakers.mine.bukkit.permission.PermissionManager;
import com.elmakers.mine.bukkit.persistence.dao.PlayerData;
import com.elmakers.mine.bukkit.utilities.PluginUtilities;
import com.elmakers.mine.craftbukkit.persistence.Persistence;

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
	
	/**
	 * Default constructor! Hooray!
	 */
	public PersistencePlugin()
	{
		pluginInstance = this;
	}
	
	/* Process player quit and join messages.
	 * 
	 * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] parameters)
	{
		if (listeners == null)
		{
			listeners = new ArrayList<Object>();
			listeners.add(handler);
			listeners.add(getPermissions());
		}
		return utilities.dispatch(listeners, sender, cmd.getName(), parameters);
	}

	/**
	 * Retrieve the singleton Persistence instance.
	 * 
	 * Use this function to get a reference to Persistence, which you can use to access the Persistence API.
	 * 
	 * @see com.elmakers.mine.craftbukkit.persistence.Persistence
	 * 
	 * @return The singleton instance of Persistence
	 */
	public Persistence getPersistence()
	{
		if (persistence == null)
		{
			persistence = Persistence.getInstance();
		}
		return persistence;
	}

	/*
	 * Plugin interface
	 */

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
		// Initialize permissions, if it hasn't been already
		getPermissions();
			
		handler.initialize(this, getPersistence(), getUtilities());
		listener.initialize(getPersistence(), handler);
		
		PluginManager pm = getServer().getPluginManager();
		
		pm.registerEvent(Type.PLAYER_QUIT, listener, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_JOIN, listener, Priority.Normal, this);
	}
	
	public PluginUtilities getUtilities()
	{
		if (utilities == null)
		{
			utilities = getPersistence().getUtilities(this);
		}
		
		return utilities;
	}
	
	public PermissionManager getPermissions()
	{
		if (permissions == null)
		{
			// TODO: This is messy, group manager relies on plugin utilities,
			// which needs a permission manager.
			// Hopefully all temporary!
			permissions = new GroupManager(getServer(), getPersistence(), getDataFolder());
			permissions.initialize();
			PlayerData.setPermissionHandler(permissions);
		}
		return permissions;
	}
	
	/*
	 * Private data
	 */

	private static PersistencePlugin	pluginInstance	= null;
	private final PersistenceListener	listener		= new PersistenceListener();
	private final PersistenceCommands	handler			= new PersistenceCommands();
	private Persistence					persistence		= null;
	private GroupManager				permissions		= null;
	private PluginUtilities				utilities		= null;
	private List<Object>				listeners 		= null;
	private static final Logger			log				= Logger.getLogger("Minecraft");
	
}
