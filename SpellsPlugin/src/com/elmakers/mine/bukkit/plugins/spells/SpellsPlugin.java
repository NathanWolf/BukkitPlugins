package com.elmakers.mine.bukkit.plugins.spells;

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
import org.dynmap.DynmapPlugin;

import com.elmakers.mine.bukkit.plugins.nether.NetherGatePlugin;

public class SpellsPlugin extends JavaPlugin
{
	/*
	 * Constructor
	 */
	public SpellsPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File dataFolder, File plugin, ClassLoader cLoader) 
	{
		super(pluginLoader, instance, desc, dataFolder, plugin, cLoader);
	}
	
	/*
	 * Public API
	 */
	public Spells getSpells()
	{
		return spells;
	}

	/*
	 * Plugin interface
	 */
	
	public void onEnable() 
	{
		bindDynmapPlugin();
		bindNetherGatePlugin();
		
		spells.initialize(this);
		
		playerListener.setSpells(spells);
		entityListener.setSpells(spells);
		
        PluginManager pm = getServer().getPluginManager();
		
        pm.registerEvent(Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_ITEM, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_ANIMATION, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
        
        pm.registerEvent(Type.ENTITY_DEATH, entityListener, Priority.Normal, this);
        
        /*
        pm.registerEvent(Type.ENTITY_DAMAGED, entityListener, Priority.Normal, this);
        pm.registerEvent(Type.ENTITY_COMBUST, entityListener, Priority.Normal, this);
        pm.registerEvent(Type.ENTITY_DAMAGEDBY_BLOCK, entityListener, Priority.Normal, this);
        pm.registerEvent(Type.ENTITY_DAMAGEDBY_ENTITY, entityListener, Priority.Normal, this);
        pm.registerEvent(Type.ENTITY_DAMAGEDBY_PROJECTILE, entityListener, Priority.Normal, this);
        pm.registerEvent(Type.ENTITY_EXPLODE, entityListener, Priority.Normal, this);
        */
        
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
	}

	public void onDisable() 
	{
		spells.clear();
	}
	
	protected void bindDynmapPlugin() 
	{
		Plugin checkForMap = this.getServer().getPluginManager().getPlugin("dynmap");

	    if (checkForMap != null) 
	    {
	    	try
	    	{
	    		if (checkForMap.getClass().getMethod("getMapManager") != null)
	    		{
	    			spells.setDynmap((DynmapPlugin)checkForMap);
	    			log.info("Spells: Found dynmap plugin, binding to it");
	    		}
	    	}
	    	catch(NoSuchMethodException ex)
	    	{
	    		log.info("Spells: Found dynmap, but need a newer version");
	    	}
	    }
	}
	
	protected void bindNetherGatePlugin() 
	{
		Plugin checkForNether = this.getServer().getPluginManager().getPlugin("NetherGate");

	    if (checkForNether != null) 
	    {
	    	log.info("Spells: found NetherGate! Thanks for using my plugins :)");
	    	NetherGatePlugin plugin = (NetherGatePlugin)checkForNether;
	    	spells.setNether(plugin.getManager());
	    }
	}

	/*
	 * Private data
	 */
	private final Spells spells = new Spells();
	private final Logger log = Logger.getLogger("Minecraft");
	private final SpellsPlayerListener playerListener = new SpellsPlayerListener();
	private final SpellsEntityListener entityListener = new SpellsEntityListener();
	
}
