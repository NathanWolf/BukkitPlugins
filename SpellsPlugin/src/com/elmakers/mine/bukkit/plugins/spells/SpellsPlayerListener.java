package com.elmakers.mine.bukkit.plugins.spells;
import org.bukkit.event.player.PlayerChatEvent;
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
    	
    	spell.initialize(plugin, event.getPlayer());
    	spell.onCast(parameters);
    }
    
}
