package com.elmakers.mine.bukkit.plugins.persistence;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PersistencePlugin extends JavaPlugin
{
	/*
	 * Public API
	 */
	
	public Persistence getPersistence()
	{
		return Persistence.getInstance();
	}
	
	public static PersistencePlugin getInstance()
	{
		return pluginInstance;
	}

	/*
	 * Plugin interface
	 */
	
	public PersistencePlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder,
			File plugin, ClassLoader cLoader)
	{
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
		
		pluginInstance = this;
	}

	public void onDisable()
	{
		if (persistence != null)
		{
			persistence.save();
			persistence.clear();
			persistence.disconnect();
		}
	}

	public void onEnable()
	{
		PluginManager pm = getServer().getPluginManager();
		
        pm.registerEvent(Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
        
		PluginDescriptionFile pdfFile = this.getDescription();
        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
	}
	
	public static Logger getLogger()
	{
		return log;
	}
	
	/*
	 * Private data
	 */
	
	private static PersistencePlugin pluginInstance = null;
	private final PersistencePlayerListener playerListener = new PersistencePlayerListener();
	private Persistence persistence = null;
	private static final Logger log = Logger.getLogger("Minecraft");
	
}
