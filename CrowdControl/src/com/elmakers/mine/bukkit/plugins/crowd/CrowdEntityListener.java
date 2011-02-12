package com.elmakers.mine.bukkit.plugins.crowd;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;

public class CrowdEntityListener extends EntityListener
{
	@Override
	public void onEntityTarget(EntityTargetEvent event)
	{
		Entity entity = event.getEntity();
		if (entity instanceof Ghast)
		{
			Ghast ghast = (Ghast)entity;
			
			// DIE MOFO, DIE!!!
			ghast.setHealth(0);
		}
	}

}
