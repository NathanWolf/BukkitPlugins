package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerItemEvent;
import org.bukkit.event.player.PlayerListener;

public class SpellsPlayerListener extends PlayerListener 
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
    @Override
    public void onPlayerCommand(PlayerChatEvent event) 
    {
    	String[] split = event.getMessage().split(" ");
    	String commandString = split[0];
    	
    	if (!commandString.equalsIgnoreCase("/cast"))
    	{
    		return;
    	}
    	
    	if (split.length < 2)
    	{
    		plugin.listSpells(event.getPlayer());
    		return;
    	}

    	// No params
   
    	String spellName = split[1];
    	Spell spell = plugin.getSpell(spellName);
    	if (spell == null || spellName.equalsIgnoreCase("help") || spellName.equalsIgnoreCase("list"))
    	{
    		plugin.listSpells(event.getPlayer());
    		return;
    	}
    	
    	String[] parameters = new String[split.length - 1];
    	for (int i = 1; i < split.length; i++)
    	{
    		parameters[i - 1] = split[i];
    	}
    	
    	spell.cast(parameters, plugin, event.getPlayer());
    }
    

    /**
     * Called when a player uses an item
     * 
     * @param event Relevant event details
     */
	@Override
    public void onPlayerItem(PlayerItemEvent event) 
	{
		// Kind of a hack for Wand compatibility, ignore the stick.
		// What we really need is a way to tell what are blocks, or a whitelist.
		Material material = event.getPlayer().getInventory().getItemInHand().getType();
		if (material != Material.STICK)
		{
			plugin.setCurrentMaterialType(event.getPlayer(), material);
		}
    }
    
}
