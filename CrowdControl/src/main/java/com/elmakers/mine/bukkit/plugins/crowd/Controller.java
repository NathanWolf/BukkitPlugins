package com.elmakers.mine.bukkit.plugins.crowd;

import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Cow;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;

import com.elmakers.mine.bukkit.plugins.crowd.dao.ControlRule;
import com.elmakers.mine.bukkit.plugins.crowd.dao.ControlledWorld;
import com.elmakers.mine.craftbukkit.persistence.Persistence;

public class Controller
{
	public void initialize(Server server)
	{
		this.server = server;
	}
	
	public static boolean isEntityType(CreatureType type, LivingEntity entity)
	{
		if (entity instanceof Player) return false;

		switch(type)
		{
			case SHEEP: return (entity instanceof Sheep);
			case COW: return (entity instanceof Cow);
			case PIG: return (entity instanceof Pig);
			case CREEPER: return (entity instanceof Creeper);
			case PIG_ZOMBIE: return (entity instanceof PigZombie);
			case SKELETON: return (entity instanceof Skeleton);
			case SPIDER: return (entity instanceof Spider);
			case SQUID: return (entity instanceof Squid);
			case ZOMBIE: return (entity instanceof Zombie);
			case GHAST: return (entity instanceof Ghast);
			case SLIME: return (entity instanceof Slime);
			case GIANT_ZOMBIE: return (entity instanceof Giant);
		}

		return false;
	}
	
	protected LivingEntity spawn(Location location, CreatureType type)
	{
		return server.spawn(location, type);
	}
	
	public void controlSpawnEvent(ControlledWorld world, CreatureSpawnEvent event)
	{
		List<ControlRule> rules = world.getRules();
		if (rules == null) return;
		
		Entity baseEntity = event.getEntity();
		if (!(baseEntity instanceof LivingEntity)) return;
		
		LivingEntity entity = (LivingEntity)baseEntity;
		
		for (ControlRule rule : rules)
		{
			if (isEntityType(rule.getCreatureType(), entity))
			{
				float percent = rule.getPercentChance();
				if (percent >= rand.nextFloat())
				{
					if (debugLog)
					{
						log.info("CrowdControl: controlling a " + rule.getCreatureType().getName());
					}
					
					CreatureType replaceWith = rule.getReplaceWith();
					if (replaceWith != null)
					{
						spawn(entity.getLocation(), replaceWith);
						if (debugLog)
						{
							log.info("CrowdControl: spawned a " + replaceWith.getName());
						}
					}
					
					entity.setHealth(0);
					event.setCancelled(true);
				}
			}
		}
	}
	
	public int nuke(World world, CreatureType entityType, boolean nukeAll)
	{
		int killCount = 0;
		List<LivingEntity> entities = world.getLivingEntities();
		for (LivingEntity entity : entities)
		{
			if (nukeAll || isEntityType(entityType, entity))
			{
				entity.setHealth(0);
				killCount++;
			}
		}
		
		return killCount;
	}
	
	private Server server;
	private final Random rand = new Random();
	private static boolean debugLog = false;
	private static final Logger log = Persistence.getLogger();
}
