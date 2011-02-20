package com.elmakers.mine.bukkit.plugins.crowd;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityListener;

public class CrowdEntityListener extends EntityListener
{
	@Override
	public void onCreatureSpawn(CreatureSpawnEvent event)
	{
		Entity entity = event.getEntity();
		if (entity instanceof Ghast)
		{
			Ghast ghast = (Ghast)entity;
			
			// DIE MOFO, DIE!!!
			ghast.setHealth(0);
			
			event.setCancelled(true);
		}
	}
}
