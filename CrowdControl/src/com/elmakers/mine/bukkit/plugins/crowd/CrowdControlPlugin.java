package com.elmakers.mine.bukkit.plugins.crowd;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.plugins.persistence.Persistence;
import com.elmakers.mine.bukkit.plugins.persistence.PersistencePlugin;
import com.elmakers.mine.bukkit.plugins.persistence.PluginUtilities;
import com.elmakers.mine.bukkit.plugins.persistence.dao.Message;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PermissionType;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PluginCommand;

public class CrowdControlPlugin extends JavaPlugin
{

	public CrowdControlPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder,
			File plugin, ClassLoader cLoader)
	{
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
	}

	public void onDisable()
	{
	}

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
		
        pm.registerEvent(Type.ENTITY_TARGET, listener, Priority.Normal, this);
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
	    
	    crowdCommand = utilities.getPlayerCommand("crowd", "Manage mob spawning", null, PermissionType.ADMINS_ONLY);
	    crowdEnableCommand = crowdCommand.getSubCommand("enable", "Enable a mob type", "[mob] <world>", PermissionType.ADMINS_ONLY);
	    crowdDisableCommand = crowdCommand.getSubCommand("disable", "Disable a mob type", "[mob] <world>", PermissionType.ADMINS_ONLY);
	
	    crowdEnableCommand.bind("onEnableCrowd");
	    crowdEnableCommand.bind("onDisableCrowd");
		
		crowdEnabledMessage = utilities.getMessage("crowdEnabled", "Mob %s enabled in world %s");
		crowdDisabledMessage = utilities.getMessage("crowdDisable", "Mob %s disabled in world %s");
	}
	
	public boolean onEnableCrowd(Player player, String[] parameters)
	{
		player.sendMessage("Not implemented yet!");
		return true;
	}

	public boolean onDisableCrowd(Player player, String[] parameters)
	{
		player.sendMessage("Not implemented yet!");
		return true;
	}
	
	protected final CrowdEntityListener listener = new CrowdEntityListener();
	
	protected PluginCommand crowdCommand;
	protected PluginCommand crowdEnableCommand;
	protected PluginCommand crowdDisableCommand;
	
	protected Message crowdEnabledMessage;
	protected Message crowdDisabledMessage;
	
	protected Persistence persistence = null;
	protected PluginUtilities utilities = null;

	protected static final Logger log = Logger.getLogger("Minecraft");


}
