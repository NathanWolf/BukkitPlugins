package com.elmakers.mine.bukkit.plugins.spells;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SpellsPlugin extends JavaPlugin 
{
	private static final Logger log = Logger.getLogger("Minecraft");
	static final SpellsPlayerListener playerListener = new SpellsPlayerListener();
	static final HashMap<String, Spell> spells = new HashMap<String, Spell>();

	@Override
	public void onInitialize()
	{
		// Create all of our spells
		addSpell(new HealSpell());
		addSpell(new BlinkSpell());
		addSpell(new AscendSpell());
		addSpell(new DescendSpell());
		addSpell(new TorchSpell());
	}
	
	private void addSpell(Spell spell)
	{
		spells.put(spell.getName(), spell);
	}
	
	@Override
	public void onEnable() 
	{
		playerListener.setPlugin(this);
		
        PluginManager pm = getServer().getPluginManager();
		
        pm.registerEvent(Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
        
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
	}

	@Override
	public void onDisable() 
	{
	}
	
	public void listSpells(Player player)
	{
		player.sendMessage("Use: /cast <spell>: ");
		for (Spell spell : spells.values())
		{
			player.sendMessage(" " + spell.getName() + " : " + spell.getDescription());
		}
	}
	
	public Spell getSpell(String name)
	{
		return spells.get(name);
	}
}
