package com.elmakers.mine.bukkit.plugins.nether;

import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.LivingEntity;

import com.elmakers.mine.bukkit.plugins.gameplay.EntityType;

public class Nuke
{
	public int nuke(World world, EntityType entityType)
	{
		int killCount = 0;
		List<LivingEntity> entities = world.getLivingEntities();
		for (LivingEntity entity : entities)
		{
			if (entityType.isEntityType(entity))
			{
				entity.setHealth(0);
				killCount++;
			}
		}
		
		return killCount;
	}
}
