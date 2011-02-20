package com.elmakers.mine.bukkit.plugins.crowd;

import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityListener;

import com.elmakers.mine.bukkit.plugins.crowd.dao.ControlledWorld;
import com.elmakers.mine.craftbukkit.persistence.Persistence;

public class CrowdEntityListener extends EntityListener
{
	public void initialize(Persistence persistence, Controller controller)
	{
		this.persistence = persistence;
		this.controller = controller;
	}
	
	@Override
	public void onCreatureSpawn(CreatureSpawnEvent event)
	{
		ControlledWorld worldData = persistence.get(event.getLocation().getWorld().getName(), ControlledWorld.class);
		if (worldData == null) return;
		
		controller.controlSpawnEvent(worldData, event);
	}
	
	protected Persistence persistence;
	protected Controller controller;
}
