package com.elmakers.mine.bukkit.plugins.gameplay;

import org.bukkit.entity.Cow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Zombie;

public enum EntityType
{
	CHICKEN,
	SHEEP,
	COW,
	PIG,
	CREEPER,
	PIGZOMBIE,
	SKELETON,
	SPIDER,
	SQUID,
	ZOMBIE,
	GHAST,
	GIANT,
	//FISH,
	//SLIME,
	ALL,
	UNKNOWN;
	
	public String getName()
	{
		if (this == ALL)
		{
			return "whatever";
		}
		
		return this.name().toLowerCase();
	}
	
	public static EntityType parseString(String s)
	{
		return parseString(s, UNKNOWN);
	}
	
	public static EntityType parseString(String s, EntityType defaultFamiliarType)
	{
		EntityType foundType = defaultFamiliarType;
		for (EntityType t : EntityType.values())
		{
			if (t.name().equalsIgnoreCase(s))
			{
				foundType = t;
			}
		}
		return foundType;
	}
	
	public boolean isEntityType(LivingEntity entity)
	{
		if (entity instanceof Player) return false;
		
		switch(this)
		{
			case ALL:
				return true;
			case SHEEP: return (entity instanceof Sheep);
			case COW: return (entity instanceof Cow);
			case PIG: return (entity instanceof Pig);
			case CREEPER: return (entity instanceof Creeper);
			case PIGZOMBIE: return (entity instanceof PigZombie);
			case SKELETON: return (entity instanceof Skeleton);
			case SPIDER: return (entity instanceof Spider);
			case SQUID: return (entity instanceof Squid);
			case ZOMBIE: return (entity instanceof Zombie);
			case GHAST: return (entity instanceof Ghast);
			case GIANT: return (entity instanceof Giant);
		}
		
		return false;
	}
	
};