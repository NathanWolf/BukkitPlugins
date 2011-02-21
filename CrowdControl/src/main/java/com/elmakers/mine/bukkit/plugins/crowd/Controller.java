package com.elmakers.mine.bukkit.plugins.crowd;

import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import net.minecraft.server.EntityChicken;
import net.minecraft.server.EntityCow;
import net.minecraft.server.EntityCreeper;
import net.minecraft.server.EntityGhast;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityPig;
import net.minecraft.server.EntityPigZombie;
import net.minecraft.server.EntitySheep;
import net.minecraft.server.EntitySkeleton;
import net.minecraft.server.EntitySlime;
import net.minecraft.server.EntitySpider;
import net.minecraft.server.EntitySquid;
import net.minecraft.server.EntityZombie;
import net.minecraft.server.WorldServer;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Cow;
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

import com.elmakers.mine.bukkit.borrowed.CreatureType;
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
			case GIANT: return (entity instanceof Giant);
		}

		return false;
	}
	
	protected EntityLiving spawn(Location location, CreatureType type)
	{
		EntityLiving e = null;
		CraftWorld craftWorld = (CraftWorld)location.getWorld();
		WorldServer world = craftWorld.getHandle();

		switch (type)
		{
			case SHEEP: e = new EntitySheep(world); break;
			case PIG: e = new EntityPig(world); break;
			case CHICKEN: e = new EntityChicken(world); break;
			case COW: e = new EntityCow(world); break;
			case CREEPER: e = new EntityCreeper(world); break;
			case PIG_ZOMBIE: e = new EntityPigZombie(world); break;
			case SKELETON: e = new EntitySkeleton(world); break;
			case SPIDER: e = new EntitySpider(world); break;
			case SQUID: e = new EntitySquid(world); break;
			case GHAST: e = new EntityGhast(world); break;
			case ZOMBIE: e = new EntityZombie(world); break;
			//case GIANT_ZOMBIE: e = new EntityGiantZombie(world); break;
			case SLIME: e = new EntitySlime(world); break;
			//case FISH: e = new EntityFish(world); break;
		}

		if (e != null)
		{
			e.c(location.getX(), location.getY(), location.getZ(), location.getYaw(), 0.0F);
	        world.a(e);
		}
		return e;
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
	
	public int nuke(ControlledWorld targetWorld, CreatureType entityType, boolean nukeAll)
	{
		int killCount = 0;
		World world = targetWorld.getId().getWorld(server);
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
