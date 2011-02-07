package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerChatEvent;
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
     * Commands sent from in game to us.
     *
     * @param player The player who sent the command.
     * @param split The input line split by spaces.
     * @return <code>boolean</code> - True denotes that the command existed, false the command doesn't.
     */
    @Override
    public void onPlayerCommand(PlayerChatEvent event) 
    {
    	master.onPlayerCommand(event);
    }
    
    /**
     * Called when a player performs an animation, such as the arm swing
     * 
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
