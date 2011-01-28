package com.elmakers.mine.bukkit.plugins.classes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.plugins.classes.dao.PlayerDAO;
import com.elmakers.mine.bukkit.plugins.persistence.Persistence;
import com.elmakers.mine.bukkit.plugins.persistence.PersistencePlugin;

public class ClassesPlugin extends JavaPlugin
{

	public ClassesPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder,
			File plugin, ClassLoader cLoader)
	{
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
	}

	@Override
	public void onDisable()
	{
		
	}

	@Override
	public void onEnable()
	{
		if (!bindPersistence()) return;
		
		List<PlayerDAO> players = new ArrayList<PlayerDAO>();
		persistence.getAll(players, PlayerDAO.class);
	}
	
	public boolean bindPersistence() 
	{
		Plugin checkForPlugin = this.getServer().getPluginManager().getPlugin("Persistence");
	    if (checkForPlugin != null) 
	    {
	    	PersistencePlugin plugin = (PersistencePlugin)checkForPlugin;
	    	persistence = plugin.getPersistence();
	    } 
	    else 
	    {
	    	log.warning("The Classes plugin depends on Persistence - please install it!");
	    	this.getServer().getPluginManager().disablePlugin(this);
	    	return false;
	    }
	    
	    return true;
	}
	
	private final Logger log = Logger.getLogger("Minecraft");
	private Persistence persistence = null;
}
