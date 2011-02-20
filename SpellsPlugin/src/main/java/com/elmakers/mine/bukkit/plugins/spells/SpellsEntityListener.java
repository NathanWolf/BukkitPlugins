package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;

class SpellsEntityListener extends EntityListener 
{
	private Spells master;
	
	public void setSpells(Spells master)
	{
		this.master = master;
	}
	
	public void handleDamage(EntityEvent event)
	{
		if (Player.class.isInstance(event.getEntity()))
		{
			Player player = (Player)event.getEntity();
			master.onPlayerDamage(player, event);
		}
	}
	
	public void onEntityDamage(EntityDamageEvent event)
	{
		handleDamage(event);
	}
    
    public void onEntityDamageByBlock(EntityDamageByBlockEvent event) 
    {
    	handleDamage(event);
    }

    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) 
    {
    	handleDamage(event);
    }
    
    public void onEntityDamageByProjectile(EntityDamageByProjectileEvent event) 
    {
    	handleDamage(event);
    }
    
    public void onEntityCombust(EntityCombustEvent event) 
    {
    	handleDamage(event);
    }

    public void onEntityExplode(EntityExplodeEvent event) 
    {
    	handleDamage(event);
    }

	public void onEntityDeath(EntityDeathEvent event)
	{
		if (Player.class.isInstance(event.getEntity()))
		{
			Player player = (Player)event.getEntity();
			master.onPlayerDeath(player, event);
		}
	}
}
