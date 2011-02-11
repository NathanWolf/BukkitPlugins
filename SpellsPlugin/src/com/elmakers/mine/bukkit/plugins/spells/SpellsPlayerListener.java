package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerItemEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

class SpellsPlayerListener extends PlayerListener 
{
	private Spells master;
	
	public void setSpells(Spells master)
	{
		this.master = master;
	}
    
    /**
     * Called when a player performs an animation, such as the arm swing
     * w
     * @param event Relevant event details
     */
    
	@Override
    public void onPlayerAnimation(PlayerAnimationEvent event) 
	{
		master.onPlayerAnimation(event);
    }
	
    /**
     * Called when a player attempts to move location in a world
     *
     * @param event Relevant event details
     */
    public void onPlayerMove(PlayerMoveEvent event) 
    {
    	master.onPlayerMove(event);
    }
 
    /**
     * Called when a player uses an item
     * 
     * @param event Relevant event details
     */
    public void onPlayerItem(PlayerItemEvent event) 
    {
    	master.onPlayerItem(event);
    }

	@Override
	public void onPlayerQuit(PlayerEvent event)
	{
		master.onPlayerQuit(event);
	}
}
