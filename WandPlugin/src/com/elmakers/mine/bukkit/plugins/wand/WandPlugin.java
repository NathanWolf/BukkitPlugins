package com.elmakers.mine.bukkit.plugins.wand;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.plugins.spells.SpellsPlugin;

public class WandPlugin extends JavaPlugin 
{
	private final Wands wands = new Wands();
	private final Logger log = Logger.getLogger("Minecraft");
	private final WandPlayerListener playerListener = new WandPlayerListener();
	
	public WandPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File dataFolder, File plugin, ClassLoader cLoader) 
	{
		super(pluginLoader, instance, desc, dataFolder, plugin, cLoader);
	}
	 
	@Override
	public void onEnable() 
	{
		wands.initialize(this);
		
		bindSpellsPlugin();
		
		playerListener.setWands(wands);
		
        PluginManager pm = getServer().getPluginManager();
		
        pm.registerEvent(Type.PLAYER_ANIMATION, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_ITEM, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
        
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
	}

	@Override
	public void onDisable() 
	{
		wands.save();
	}
	
	public void bindSpellsPlugin() 
	{
		Plugin checkForSpells = this.getServer().getPluginManager().getPlugin("Spells");
	    if(checkForSpells != null) 
	    {
	    	SpellsPlugin plugin = (SpellsPlugin)checkForSpells;
	    	wands.setSpells(plugin.getSpells());
	    } 
	    else 
	    {
	    	log.warning("The Wand plugin depends on Spells v0.50 or higher - please install it!");
	    	this.getServer().getPluginManager().disablePlugin(this);
	    }
	}
	
}
