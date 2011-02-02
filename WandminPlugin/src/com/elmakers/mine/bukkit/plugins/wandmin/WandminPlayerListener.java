package com.elmakers.mine.bukkit.plugins.wandmin;

import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerItemEvent;
import org.bukkit.event.player.PlayerListener;

public class WandminPlayerListener extends PlayerListener 
{
	private WandminPlugin plugin;
	
	public void setPlugin(WandminPlugin plugin)
	{
		this.plugin = plugin;
	}
	
	 /**
     * Called when a player plays an animation, such as an arm swing
     * 
     * @param event Relevant event details
     */
	@Override
    public void onPlayerAnimation(PlayerAnimationEvent event) 
	{
		if (event.getAnimationType() == PlayerAnimationType.ARM_SWING)
		{
			if (event.getPlayer().getInventory().getItemInHand().getTypeId() == plugin.getWandTypeId())
			{
				WandPermissions permissions = plugin.getPermissions(event.getPlayer().getName());	
				if (!permissions.canUse())
				{
					return;
				}
				
				PlayerWandList wands = plugin.getPlayerWands(event.getPlayer());
				Wand wand = wands.getCurrentWand();
				if (wand == null)
				{
					return;
				}
				wand.use(plugin, event.getPlayer());
			}
		}
    }
  
    /**
     * Called when a player uses an item
     * 
     * @param event Relevant event details
     */
	@Override
    public void onPlayerItem(PlayerItemEvent event) 
	{
		if (event.getPlayer().getInventory().getItemInHand().getTypeId() == plugin.getWandTypeId())
		{
			WandPermissions permissions = plugin.getPermissions(event.getPlayer().getName());	
			if (!permissions.canUse())
			{
				return;
			}
			
			PlayerWandList wands = plugin.getPlayerWands(event.getPlayer());
			Wand wand = wands.getCurrentWand();
			if (wand == null)
			{
				return;
			}
			wand.nextCommand();
			event.getPlayer().sendMessage(" " + wand.getName() + " : " + wand.getCurrentCommand().getName());
		}
    }
	
    /**
     * Called when a player joins a server
     *
     * @param event Relevant event details
     */
    @Override
    public void onPlayerJoin(PlayerEvent event) 
    {
    	PlayerWandList wands = plugin.getPlayerWands(event.getPlayer().getName());
    	wands.setPlayer(event.getPlayer());
    	plugin.save();
    }

    /**
     * Called when a player leaves a server
     *
     * @param event Relevant event details
     */
    @Override
    public void onPlayerQuit(PlayerEvent event) 
    {
    	PlayerWandList wands = plugin.getPlayerWands(event.getPlayer().getName());
    	wands.setPlayer(null);
    	plugin.save();
    }
}
