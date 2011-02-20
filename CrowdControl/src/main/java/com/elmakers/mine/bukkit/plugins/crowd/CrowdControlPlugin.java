package com.elmakers.mine.bukkit.plugins.crowd;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.persistence.dao.Message;
import com.elmakers.mine.bukkit.persistence.dao.PermissionType;
import com.elmakers.mine.bukkit.persistence.dao.PluginCommand;
import com.elmakers.mine.bukkit.persistence.dao.WorldData;
import com.elmakers.mine.bukkit.plugins.crowd.dao.ControlRule;
import com.elmakers.mine.bukkit.plugins.crowd.dao.ControlledWorld;
import com.elmakers.mine.bukkit.plugins.persistence.PersistencePlugin;
import com.elmakers.mine.bukkit.utilities.PluginUtilities;
import com.elmakers.mine.craftbukkit.persistence.Persistence;

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
	    
	    CrowdControlDefaults d = new CrowdControlDefaults();
	    utilities = persistence.getUtilities(this);
	    listener.initialize(persistence, controller);
	    controller.initialize(getServer());
	    
	    crowdCommand = utilities.getPlayerCommand(d.crowdCommand[0], d.crowdCommand[1], d.crowdCommand[2], PermissionType.ADMINS_ONLY);
	    crowdControlCommand = crowdCommand.getSubCommand(d.crowdControlCommand[0], d.crowdControlCommand[1], d.crowdControlCommand[2], PermissionType.ADMINS_ONLY);
	    crowdReleaseCommand = crowdCommand.getSubCommand(d.crowdReleaseCommand[0], d.crowdReleaseCommand[1], d.crowdReleaseCommand[2], PermissionType.ADMINS_ONLY);
	    nukeCommand = crowdCommand.getSubCommand(d.nukeCommand[0], d.nukeCommand[1], d.nukeCommand[2], PermissionType.ADMINS_ONLY);
		
	    crowdControlCommand.bind("onControlCrowd");
	    crowdReleaseCommand.bind("onReleaseCrowd");
	    nukeCommand.bind("onNuke");
		
	    notControllingMessage = utilities.getMessage("notControlling", d.notControllingMessage);
	   
	    crowdChanceDisableMessage = utilities.getMessage("crowdChangeDisable", d.crowdChanceDisableMessage);
		crowdChanceReplaceMessage = utilities.getMessage("crowdChanceReplace", d.crowdChanceReplaceMessage);
		crowdDisableMessage = utilities.getMessage("crowdDisable", d.crowdDisableMessage);
		crowdReplaceMessage = utilities.getMessage("crowdReplace", d.crowdReplaceMessage);
		crowdReleasedMessage = utilities.getMessage("crowdDisable", d.crowdReleasedMessage);
		noWorldMessage = utilities.getMessage("noWorld", d.noWorldMessage);
		killedEntitiesMessage = utilities.getMessage("killedEntities", d.killedEntitiesMessage);
		noEntityMessage = utilities.getMessage("noEntities", d.noEntityMessage);
		killFailedMessage = utilities.getMessage("killFailed", d.killFailedMessage);
	}
	
	public ControlledWorld getWorldData(World world)
	{
		if (world == null) return null;
		WorldData worldData = utilities.getWorld(getServer(), world);
		
		if (worldData == null) return null;
		
		ControlledWorld controlled = persistence.get(world, ControlledWorld.class);
		if (controlled == null)
		{
			controlled = new ControlledWorld(worldData);
			persistence.put(controlled);
		}
		
		return controlled;
	}
	
	public CreatureType getCreatureType(String name)
	{
		for (CreatureType ct: CreatureType.values())
		{
			if (ct.getName().equalsIgnoreCase(name)) return ct;
		}
		
		return CreatureType.fromName(name);
	}
	
	public boolean onControlCrowd(Player player, String[] parameters)
	{
		if (parameters.length < 1)
		{
			return false;
		}
		
		CreatureType mobType = getCreatureType(parameters[0]);
		
		if (mobType == null)
		{
			unknownEntityMessage.sendTo(player, parameters[0]);
			return true;
		}
		
		ControlledWorld world = getWorldData(player.getWorld());
		if (world == null)
		{
			noWorldMessage.sendTo(player, player.getWorld().getName());
			return true;
		}
		
		float percent = 1;
		CreatureType targetType = null;
		int currentRank = 1;
		
		if (parameters.length > 1)
		{
			try
			{
				percent = Float.parseFloat(parameters[1]);
				if (percent > 1)
				{
					percent = (float)Integer.parseInt(parameters[1]) / 100;
				}
			} 
			catch(NumberFormatException ex)
			{
			}
		}
		
		if (parameters.length > 2)
		{
			targetType = getCreatureType(parameters[2]);
		}
		
		List<ControlRule> rules = world.getRules();
		if (rules == null)
		{
			rules = new ArrayList<ControlRule>();
		}
		
		for (ControlRule rule : rules)
		{
			currentRank = rule.getRank() + 1;
		}
	
		ControlRule newRule = new ControlRule(currentRank, mobType);
		newRule.setPercentChance(percent);
		newRule.setReplaceWith(targetType);
		rules.add(newRule);
		world.setRules(rules);
		persistence.put(world);
		if (targetType == null)
		{
			if (percent >= 1)
			{
				crowdDisableMessage.sendTo(player, mobType.getName());
			}
			else
			{
				crowdChanceDisableMessage.sendTo(player, mobType.getName(), (int)(percent * 100));
			}
		}
		else
		{
			if (percent >= 1)
			{
				crowdReplaceMessage.sendTo(player, mobType.getName(), targetType.getName());
			}
			else
			{
				crowdChanceReplaceMessage.sendTo(player, mobType.getName(), targetType.getName(), (int)(percent * 100));
			}
		}
		
		return true;
	}

	public boolean onReleaseCrowd(Player player, String[] parameters)
	{
		if (parameters.length < 1)
		{
			return false;
		}
		
		CreatureType mobType = getCreatureType(parameters[0]);
		
		if (mobType == null)
		{
			unknownEntityMessage.sendTo(player, parameters[0]);
			return true;
		}
		
		ControlledWorld world = getWorldData(player.getWorld());
		if (world == null)
		{
			noWorldMessage.sendTo(player, player.getWorld().getName());
			return true;
		}
		
		boolean modified = false;
		List<ControlRule> rules = world.getRules();
		List<ControlRule> newRules = new ArrayList<ControlRule>();
		if (rules != null)
		{
			for (ControlRule rule : rules)
			{
				if (rule != null && rule.getCreatureType() != mobType)
				{
					newRules.add(rule);
				}
				else
				{
					modified = true;
				}
			}
		}
		
		if (modified)
		{
			world.setRules(newRules);
			persistence.put(world);
			crowdReleasedMessage.sendTo(player, mobType.getName());
		}
		else
		{
			notControllingMessage.sendTo(player, mobType.getName());
		}
		
		return true;
	}
	
	public boolean onNuke(Player player, String[] parameters)
	{
		String worldName = null;
		WorldData targetWorld = null;
		CreatureType targetType = CreatureType.GHAST;
		boolean nukeAll = false;
		
		if (parameters.length > 0)
		{
			if (parameters[0].equalsIgnoreCase("all"))
			{
				nukeAll = true;
			}
			else
			{
				targetType = getCreatureType(parameters[0]);
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
		
		int killCount = controller.nuke(world, targetType, nukeAll);
		
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
	protected final Controller controller = new Controller();
	
	protected PluginCommand crowdCommand;
	protected PluginCommand crowdControlCommand;
	protected PluginCommand crowdReleaseCommand;
	protected PluginCommand nukeCommand;

	protected Message killedEntitiesMessage;
	protected Message killFailedMessage;
	protected Message noEntityMessage;
	protected Message unknownEntityMessage;
	
	protected Message crowdChanceDisableMessage;
	protected Message crowdChanceReplaceMessage;
	protected Message crowdDisableMessage;
	protected Message crowdReplaceMessage;
	protected Message crowdReleasedMessage;
	protected Message noWorldMessage;
	protected Message notControllingMessage;
	
	protected Persistence persistence = null;
	protected PluginUtilities utilities = null;

	protected static final Logger log = Logger.getLogger("Minecraft");


}
