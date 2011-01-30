package com.elmakers.mine.bukkit.plugins.classes;

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

import com.elmakers.mine.bukkit.plugins.classes.dao.UserGroup;
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
		
		playerListener.setPersistence(persistence);
		
		UserGroup testGroup = new UserGroup();
		persistence.put(testGroup);
		persistence.save();
		
		PluginManager pm = getServer().getPluginManager();
			
		pm.registerEvent(Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
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
	private final ClassesPlayerListener playerListener = new ClassesPlayerListener();
	private Persistence persistence = null;
}
