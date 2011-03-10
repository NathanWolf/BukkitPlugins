package com.elmakers.mine.bukkit.plugins.crowd;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.borrowed.CreatureType;
import com.elmakers.mine.bukkit.persistence.dao.Message;
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
	    controller.initialize();
	    
	    crowdCommand = utilities.getGeneralCommand(d.crowdCommand[0], d.crowdCommand[1], d.crowdCommand[2]);
	    crowdControlCommand = crowdCommand.getSubCommand(d.crowdControlCommand[0], d.crowdControlCommand[1], d.crowdControlCommand[2]);
	    crowdReplaceCommand = crowdCommand.getSubCommand(d.crowdReplaceCommand[0], d.crowdReplaceCommand[1], d.crowdReplaceCommand[2]);
	    crowdReleaseCommand = crowdCommand.getSubCommand(d.crowdReleaseCommand[0], d.crowdReleaseCommand[1], d.crowdReleaseCommand[2]);
	    nukeCommand = crowdCommand.getSubCommand(d.nukeCommand[0], d.nukeCommand[1], d.nukeCommand[2]);
	    listCommand = crowdCommand.getSubCommand(d.listCommand[0], d.listCommand[1], d.listCommand[2]);
		listRulesCommand = listCommand.getSubCommand(d.listRulesCommand[0], d.listRulesCommand[1], d.listRulesCommand[2]);
	    listPopulationCommand = listCommand.getSubCommand(d.listPopulationCommand[0], d.listPopulationCommand[1], d.listPopulationCommand[2]);
		
	    crowdControlCommand.bind("onControlCrowd");
	    crowdReplaceCommand.bind("onReplaceCrowd");
	    crowdReleaseCommand.bind("onReleaseCrowd");
	    nukeCommand.bind("onNuke");
	    listRulesCommand.bind("onListRules");
	    listPopulationCommand.bind("onListPopulation");
		
	    notControllingMessage = utilities.getMessage("notControlling", d.notControllingMessage);
	   
	    crowdChanceDisableMessage = utilities.getMessage("crowdChangeDisable", d.crowdChanceDisableMessage);
		crowdChanceReplaceMessage = utilities.getMessage("crowdChanceReplace", d.crowdChanceReplaceMessage);
		crowdDisableMessage = utilities.getMessage("crowdDisable", d.crowdDisableMessage);
		crowdReplaceMessage = utilities.getMessage("crowdReplace", d.crowdReplaceMessage);
		crowdReleasedMessage = utilities.getMessage("crowdDisable", d.crowdReleasedMessage);
		noWorldMessage = utilities.getMessage("noWorld", d.noWorldMessage);
		killedEntitiesMessage = utilities.getMessage("killedEntities", d.killedEntitiesMessage);
		noEntityMessage = utilities.getMessage("noEntities", d.noEntityMessage);
		unknownEntityMessage = utilities.getMessage("unknownEntity", d.unknownEntityMessage);
		killFailedMessage = utilities.getMessage("killFailed", d.killFailedMessage);
		listMobRulesMessage = utilities.getMessage("listMobRules", d.listMobRulesMessage);
		listWorldRulesMessage = utilities.getMessage("listWorldRules", d.listWorldRulesMessage);
		listPopulationMessage = utilities.getMessage("listPopulation", d.listPopulationMessage);
		listMobPopulationMessage = utilities.getMessage("listMobPopulation", d.listMobPopulationMessage);
		populationMessage = utilities.getMessage("population", d.populationMessage);
		rulesMessage = utilities.getMessage("rules", d.rulesMessage);
	}
	
	/**
	 * Yeah, this is just going to be whatever shows up in the list first...
	 * 
	 * @return the default world, as best as I can tell, or null if there are no registered worlds (shouldn't happen....)
	 */
	public ControlledWorld getDefaultWorld()
	{
		List<WorldData> allWorlds = new ArrayList<WorldData>();
		persistence.getAll(allWorlds, WorldData.class);
		if (allWorlds.size() == 0) return null;
		WorldData defaultWorldData = allWorlds.get(0);
		
		return getWorldData(defaultWorldData);
	}
	
	public ControlledWorld getWorldData(String worldName)
	{
		WorldData existingWorld = persistence.get(worldName, WorldData.class);
		if (existingWorld == null) return null;
		
		return getWorldData(existingWorld);
	}
	
	public WorldData getGlobalWorldData(World world)
	{
		if (world == null) return null;
		WorldData worldData = utilities.getWorld(getServer(), world);
		
		return worldData;
	}
	
	public ControlledWorld getWorldData(World world)
	{
		return getWorldData(getGlobalWorldData(world));
	}
	
	public ControlledWorld getWorldData(WorldData worldData)
	{
		if (worldData == null) return null;
		
		ControlledWorld controlled = persistence.get(worldData, ControlledWorld.class);
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
	
	class WorldSearchResults
	{
		public ControlledWorld world;
		public String worldName;
		
		public WorldSearchResults(ControlledWorld world, String worldName)
		{
			this.world = world;
			this.worldName = worldName;
		}
	}
	
	protected WorldSearchResults findWorld(CommandSender sender, String[] parameters, int worldParamIndex)
	{
		ControlledWorld world = null;
		String worldName = "unknown";
		if (parameters.length > worldParamIndex)
		{
			worldName = parameters[worldParamIndex];
			world = getWorldData(worldName);
		}
		else if (sender instanceof Player)
		{
			Player player = ((Player)sender);
			World playerWorld = player.getWorld();
			world = getWorldData(playerWorld);
			worldName = playerWorld.getName();
		}
		else
		{
			world = getDefaultWorld();
			if (world != null && world.getId() != null)
			{
				worldName = world.getId().getName();
			}
		}
		
		return new WorldSearchResults(world, worldName);
	}
	
	// <type> [percent] [world]
	public boolean onControlCrowd(CommandSender sender, String[] parameters)
	{
		if (parameters.length < 1)
		{
			return false;
		}
		
		String mobName = parameters[0];
		CreatureType mobType = getCreatureType(mobName);
		if (mobType == null)
		{
			unknownEntityMessage.sendTo(sender, mobName);
			return true;
		}
		
		float percent = 1;
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
		
		WorldSearchResults search = findWorld(sender, parameters, 2);
		ControlledWorld world = search.world;
		if (world == null)
		{
			noWorldMessage.sendTo(sender, search.worldName);
			return true;
		}
			
		List<ControlRule> rules = world.getRules();
		if (rules == null)
		{
			rules = new ArrayList<ControlRule>();
		}
		
		int currentRank = 1;
		for (ControlRule rule : rules)
		{
			currentRank = rule.getRank() + 1;
		}
	
		ControlRule newRule = new ControlRule(currentRank, mobType);
		newRule.setPercentChance(percent);
		rules.add(newRule);
		world.setRules(rules);
		persistence.put(world);
		
		if (percent >= 1)
		{
			crowdDisableMessage.sendTo(sender, mobType.getName(), search.worldName);
		}
		else
		{
			crowdChanceDisableMessage.sendTo(sender, mobType.getName(), (int)(percent * 100), search.worldName);
		}
	
		return true;
	}
	
	// <type> <replace> [percent]  [world]
	public boolean onReplaceCrowd(CommandSender sender, String[] parameters)
	{
		if (parameters.length < 2)
		{
			return false;
		}
		
		String mobName = parameters[0];
		CreatureType mobType = getCreatureType(mobName);
		if (mobType == null)
		{
			unknownEntityMessage.sendTo(sender, mobName);
			return true;
		}
		
		String replaceName = parameters[1];
		CreatureType targetType = getCreatureType(replaceName);
		if (targetType == null)
		{
			unknownEntityMessage.sendTo(sender, replaceName);
			return true;
		}
		
		float percent = 1;
		if (parameters.length > 2)
		{
			try
			{
				percent = Float.parseFloat(parameters[2]);
				if (percent > 1)
				{
					percent = (float)Integer.parseInt(parameters[2]) / 100;
				}
			} 
			catch(NumberFormatException ex)
			{
			}
		}
		
		WorldSearchResults search = findWorld(sender, parameters, 3);
		ControlledWorld world = search.world;
		if (world == null)
		{
			noWorldMessage.sendTo(sender, search.worldName);
			return true;
		}
			
		List<ControlRule> rules = world.getRules();
		if (rules == null)
		{
			rules = new ArrayList<ControlRule>();
		}
		
		int currentRank = 1;
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
	
		if (percent >= 1)
		{
			crowdReplaceMessage.sendTo(sender, mobType.getName(), targetType.getName(), search.worldName);
		}
		else
		{
			crowdChanceReplaceMessage.sendTo(sender, mobType.getName(), targetType.getName(), (int)(percent * 100), search.worldName);
		}
		
		return true;
	}

	// <mob> <world>
	public boolean onReleaseCrowd(CommandSender sender, String[] parameters)
	{
		if (parameters.length < 1)
		{
			return false;
		}
		
		CreatureType mobType = getCreatureType(parameters[0]);
		
		if (mobType == null)
		{
			unknownEntityMessage.sendTo(sender, parameters[0]);
			return true;
		}
		
		WorldSearchResults search = findWorld(sender, parameters, 1);
		ControlledWorld world = search.world;
		if (world == null)
		{
			noWorldMessage.sendTo(sender, search.worldName);
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
			crowdReleasedMessage.sendTo(sender, mobType.getName(), search.worldName);
		}
		else
		{
			notControllingMessage.sendTo(sender, mobType.getName(), search.worldName);
		}
		
		return true;
	}
	
	protected void listRules(CommandSender sender, ControlledWorld world, CreatureType mobType)
	{
		if (world == null) return;
		
		List<ControlRule> rules = world.getRules();
		if (rules != null)
		{
			for (ControlRule rule : rules)
			{
				if (rule != null && (mobType == null || rule.getCreatureType() == mobType))
				{
					String replaceType = "Nothing";
					if (rule.getReplaceWith() != null)
					{
						replaceType = rule.getReplaceWith().getName();
					}
					rulesMessage.sendTo(sender, rule.getCreatureType().getName(), replaceType, (int)(100 * rule.getPercentChance()), world.getId().getName());
				}
			}
		}
	}
	
	// <type> <world>
	public boolean onListRules(CommandSender sender, String[] parameters)
	{
		CreatureType mobType = null;
		if (parameters.length > 0)
		{
			mobType = getCreatureType(parameters[0]);
		}
		WorldSearchResults search = findWorld(sender, parameters, 1);
		ControlledWorld world = search.world;
		
		if (world == null)
		{
			noWorldMessage.sendTo(sender, search.worldName);
			return true;
		}
		
		if (mobType == null)
		{
			listWorldRulesMessage.sendTo(sender, search.worldName);
		}
		else
		{
			listMobRulesMessage.sendTo(sender, mobType.getName(), search.worldName);
		}
		listRules(sender, world, mobType);
		
		return true;
	}
	
	protected void listPopulation(CommandSender sender, ControlledWorld worldData, CreatureType mobType, boolean alwaysPrint)
	{
		if (worldData == null || worldData.getId() == null) return;
		
		World world = worldData.getId().getWorld();
		int entityCount = 0;
		List<LivingEntity> entities = world.getLivingEntities();
		for (LivingEntity entity : entities)
		{
			if (Controller.isEntityType(mobType, entity))
			{
				entityCount++;
			}
		}
		if (entityCount > 0)
		{
			populationMessage.sendTo(sender, entityCount, mobType.getName(), world.getName());
		}
		else if (alwaysPrint)
		{
			noEntityMessage.sendTo(sender, mobType.getName());
		}
	}
	
	// <type> <world>
	public boolean onListPopulation(CommandSender sender, String[] parameters)
	{
		CreatureType mobType = null;
		
		if (parameters.length > 0)
		{
			mobType = getCreatureType(parameters[0]);
		}
		WorldSearchResults search = findWorld(sender, parameters, 1);
		ControlledWorld world = search.world;
		
		if (world == null)
		{
			noWorldMessage.sendTo(sender, search.worldName);
			return true;
		}
		
		if (mobType == null)
		{
			listPopulationMessage.sendTo(sender, search.worldName);
			for (CreatureType creatureType : CreatureType.values())
			{
				listPopulation(sender, world, creatureType, false);
			}
		}
		else
		{
			listMobPopulationMessage.sendTo(sender, mobType.getName(), search.worldName);
			listPopulation(sender, world, mobType, true);
		}
		
		return true;
	}
	
	// <type> <world>
	public boolean onNuke(CommandSender sender, String[] parameters)
	{
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
		
		WorldSearchResults search = findWorld(sender, parameters, 1);
		ControlledWorld targetWorld = search.world;
		if (targetWorld == null)
		{
			noWorldMessage.sendTo(sender, search.worldName);
			return true;
		}
	
		int killCount = controller.nuke(targetWorld, targetType, nukeAll);
		
		if (killCount > 0)
		{
			killedEntitiesMessage.sendTo(sender, killCount, targetType.getName(), search.worldName);
		}
		else
		{
			noEntityMessage.sendTo(sender, targetType.getName(), search.worldName);
		}
		
		return true;
	}
	
	protected final CrowdEntityListener listener = new CrowdEntityListener();
	protected final Controller controller = new Controller();
	
	protected PluginCommand crowdCommand;
	protected PluginCommand crowdControlCommand;
	protected PluginCommand crowdReplaceCommand;
	protected PluginCommand crowdReleaseCommand;
	protected PluginCommand nukeCommand;
	protected PluginCommand listRulesCommand;
	protected PluginCommand listCommand;
	protected PluginCommand listPopulationCommand;

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
	protected Message listMobRulesMessage;
	protected Message listWorldRulesMessage;
	protected Message listPopulationMessage;
	protected Message listMobPopulationMessage;
	protected Message populationMessage;
	protected Message rulesMessage;
	
	protected Persistence persistence = null;
	protected PluginUtilities utilities = null;

	protected static final Logger log = Logger.getLogger("Minecraft");


}
