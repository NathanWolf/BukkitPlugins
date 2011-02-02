package com.elmakers.mine.bukkit.plugins.persistence;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

public class PersistencePlugin extends JavaPlugin
{
	/*
	 * Public API
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

	public Persistence getPersistence()
	{
		persistence = Persistence.getInstance();
		return persistence;
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
		initialize();
		
		PluginDescriptionFile pdfFile = this.getDescription();
        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
	}
	
	/*
	 * Helper functions
	 */
	
	public static Logger getLogger()
	{
		return log;
	}
	
	
	public static PersistencePlugin getInstance()
	{
		return pluginInstance;
	}	
	
	/*
	 * Internal functions
	 */
	
	public void initialize()
	{
		handler.initialize(this, getPersistence());
	}
	
	/*
	 * Private data
	 */
	
	private static PersistencePlugin pluginInstance = null;
	private final PersistenceCommands handler = new PersistenceCommands();
	private Persistence persistence = null;
	private static final Logger log = Logger.getLogger("Minecraft");
	
}
