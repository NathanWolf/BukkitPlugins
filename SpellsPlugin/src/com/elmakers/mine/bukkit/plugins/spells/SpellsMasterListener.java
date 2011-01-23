package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.elmakers.mine.bukkit.plugins.groups.PlayerPermissions;

public class SpellsMasterListener 
{
	private SpellsPlugin plugin;
	
	public void setPlugin(SpellsPlugin plugin)
	{
		this.plugin = plugin;
	}
	
	/**
     * Commands sent from in game to us.
     *
     * @param player The player who sent the command.
     * @param split The input line split by spaces.
     * @return <code>boolean</code> - True denotes that the command existed, false the command doesn't.
     */
    public void onPlayerCommand(PlayerChatEvent event) 
    {
    	String[] split = event.getMessage().split(" ");
    	String commandString = split[0];
       	
    	PlayerPermissions permissions = plugin.getPermissions(event.getPlayer().getName());
    	
    	if (permissions == null)
    	{
    		return;
    	}
   	
    	if (commandString.equalsIgnoreCase("/spells"))
    	{
    		if (!permissions.isAdministrator() || split.length < 2 || !split[1].equalsIgnoreCase("reload"))
    		{
    			plugin.listSpells(event.getPlayer(), permissions);
    			return;
    		}
    		plugin.load();
    		event.getPlayer().sendMessage("Configuration reloaded.");
    		return;
    	}
    	
    	if (plugin.allowCommandUse())
    	{
    		return;
    	}
    	
    	if (!commandString.equalsIgnoreCase("/cast"))
    	{
    		return;
    	}
   	
    	if (split.length < 2)
    	{
    		plugin.listSpells(event.getPlayer(), permissions);
    		return;
    	}
   
    	String spellName = split[1];
    	
    	SpellVariant spell = plugin.getSpell(spellName);
    	if (spell == null || spellName.equalsIgnoreCase("help") || spellName.equalsIgnoreCase("list") || !permissions.hasPermission(spell.getName()))
    	{
    		plugin.listSpells(event.getPlayer(), permissions);
    		return;
    	}
    	
    	String[] parameters = new String[split.length - 2];
    	for (int i = 2; i < split.length; i++)
    	{
    		parameters[i - 2] = split[i];
    	}
    	
    	spell.getSpell().cast(parameters, event.getPlayer());
    }
    

    /**
     * Called when a player performs an animation, such as the arm swing
     * 
     * @param event Relevant event details
     */
    public void onPlayerAnimation(PlayerAnimationEvent event) 
	{
		if (event.getAnimationType() != PlayerAnimationType.ARM_SWING)
		{
			return;
		}
		
		// Kind of a hack for Wand compatibility, ignore the stick.
		// What we really need is a way to tell what are blocks, or a whitelist.
		ItemStack item = event.getPlayer().getInventory().getItemInHand();
		Material material = Material.AIR;
		byte data = 0;
		if (item != null)
		{
			material = item.getType();
			MaterialData mData = item.getData();
			if (mData != null)
			{
				data = mData.getData();
			}
		}
		if (material.getId() != plugin.getWandTypeId())
		{
			plugin.setCurrentMaterialType(event.getPlayer(), material, data);
		}
    }
	
    /**
     * Called when a player attempts to move location in a world
     *
     * @param event Relevant event details
     */
    public void onPlayerMove(PlayerMoveEvent event) 
    {
    	plugin.cleanup();
    	plugin.onPlayerMove(event);
    }
 
    /**
     * Called when a player uses an item
     * 
     * @param event Relevant event details
     */
    public void onPlayerItem(PlayerItemEvent event) 
    {
    	ItemStack item = event.getPlayer().getInventory().getItemInHand();
    	if (item != null && item.getTypeId() == plugin.getWandTypeId())
    	{
    		plugin.cancel(event.getPlayer());
    	}
    }
    
    public void onPlayerDamage(Player player, EntityEvent event)
    {
    	// TODO!
    }
    
    public void onPlayerQuit(PlayerEvent event)
	{
		plugin.onPlayerQuit(event);
	}
}
