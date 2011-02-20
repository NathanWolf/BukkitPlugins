package com.elmakers.mine.bukkit.plugins.crowd;

import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.plugins.gameplay.EntityType;
import com.elmakers.mine.bukkit.plugins.persistence.Persistence;
import com.elmakers.mine.bukkit.plugins.persistence.PersistencePlugin;
import com.elmakers.mine.bukkit.plugins.persistence.PluginUtilities;
import com.elmakers.mine.bukkit.plugins.persistence.dao.Message;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PermissionType;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PluginCommand;
import com.elmakers.mine.bukkit.plugins.persistence.dao.WorldData;

public class CrowdControlPlugin extends JavaPlugin
{

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		return utilities.dispatch(this, sender, cmd.getName(), args);
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
		
        pm.registerEvent(Type.CREATURE_SPAWN, listener, Priority.Normal, this);
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
	    crowdControlCommand = crowdCommand.getSubCommand("control", "Control a mob type", "[mob] <chance> <world>", PermissionType.ADMINS_ONLY);
	    nukeCommand = crowdCommand.getSubCommand("nuke", "Kill all ghasts (or whatever)", "[all | mobtype] [world]", PermissionType.ADMINS_ONLY);
		
	    crowdControlCommand.bind("onControlCrowd");
	    nukeCommand.bind("onNuke");
		
		crowdEnabledMessage = utilities.getMessage("crowdEnabled", "Mob %s enabled in world %s");
		crowdDisabledMessage = utilities.getMessage("crowdDisable", "Mob %s disabled in world %s");
		noWorldMessage = utilities.getMessage("noWorld", "Can't find world %s");
		killedEntitiesMessage = utilities.getMessage("killedEntities", "Nuked %d of those darn %ss!");
		noEntityMessage = utilities.getMessage("noEntities", "You are currently %s-free. Congrats!");
		killFailedMessage = utilities.getMessage("killFailed", "Sorry, couldn't kill any &ss!");
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
	
	public boolean onNuke(Player player, String[] parameters)
	{
		String worldName = null;
		WorldData targetWorld = null;
		EntityType targetType = EntityType.GHAST;
		
		if (parameters.length > 0)
		{
			targetType = EntityType.parseString(parameters[0]);
			if (targetType == EntityType.UNKNOWN)
			{
				targetType = EntityType.GHAST;
			}
		}
		
		if (parameters.length > 1)
		{
			worldName = parameters[0];
			targetWorld = persistence.get(worldName, WorldData.class);
		}
		else
		{
			targetWorld = utilities.getWorld(getServer(), player.getWorld());
		}
		
		if (targetWorld == null)
		{
			if (worldName != null)
			{
				noWorldMessage.sendTo(player, worldName);	
			}
			else
			{
				killFailedMessage.sendTo(player, targetType.getName());
				return true;
			}
		}
		
		World world = targetWorld.getWorld(getServer());
		if (world == null)
		{
			killFailedMessage.sendTo(player, targetType.getName());
			return true;
		}
		
		Nuke nuke = new Nuke();
		int killCount = nuke.nuke(world, targetType);
		
		if (killCount > 0)
		{
			killedEntitiesMessage.sendTo(player, killCount, targetType.getName());
		}
		else
		{
			noEntityMessage.sendTo(player, targetType.getName());
		}
		
		return true;
	}
	
	protected final CrowdEntityListener listener = new CrowdEntityListener();
	
	protected PluginCommand crowdCommand;
	protected PluginCommand crowdControlCommand;
	protected PluginCommand nukeCommand;

	protected Message killedEntitiesMessage;
	protected Message killFailedMessage;
	protected Message noEntityMessage;
	protected Message crowdEnabledMessage;
	protected Message crowdDisabledMessage;
	protected Message noWorldMessage;
	
	protected Persistence persistence = null;
	protected PluginUtilities utilities = null;

	protected static final Logger log = Logger.getLogger("Minecraft");


}
