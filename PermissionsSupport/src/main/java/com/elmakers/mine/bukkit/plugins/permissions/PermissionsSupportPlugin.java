package com.elmakers.mine.bukkit.plugins.permissions;

import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.permission.PermissionManager;
import com.elmakers.mine.bukkit.plugins.persistence.PersistencePlugin;
import com.elmakers.mine.craftbukkit.persistence.Persistence;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class PermissionsSupportPlugin extends JavaPlugin
{
	public void onDisable()
	{
	}

	public void onEnable()
	{
		try
		{
			initialize();
			PluginDescriptionFile pdfFile = this.getDescription();
	        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
		}
		catch(Throwable e)
		{
			PluginDescriptionFile pdfFile = this.getDescription();
	        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " failed to initialize");	
	        e.printStackTrace();
		}	
	}
	
	public void initialize()
	{
		Plugin checkForPersistence = this.getServer().getPluginManager().getPlugin("Persistence");
	    if(checkForPersistence != null) 
	    {
	    	PersistencePlugin plugin = (PersistencePlugin)checkForPersistence;
	    	persistence = plugin.getPersistence();
	    	permissions = plugin.getPermissions();
	    } 
	    else 
	    {
	    	log.warning("The PermissionsSupport plugin depends on Persistence");
	    	this.getServer().getPluginManager().disablePlugin(this);
	    	return;
	    }
	    
	    // Permissions backward compatibility
	    bindToPermissions();
	    
	}
	
	public void bindToPermissions()
	{
		Plugin checkForPermissions = this.getServer().getPluginManager().getPlugin("Permissions");
	    if (checkForPermissions != null) 
	    {
	    	getServer().getPluginManager().enablePlugin(checkForPermissions);
	    	PermissionHandler handler = ((Permissions)checkForPermissions).getHandler();
	    	proxy = new PermissionsProxy(handler);
	    	permissions.addHandler(proxy);
	    	log.info("PermissionsSupport: Found Permissions, using it for permissions.");
	    }
	    else
	    {
	    	log.info("PermissionsSupport: Feeling pretty useless!");
	    	log.info("PermissionsSupport: Either install Permissions.jar, or remove PermissionsSupport.jar");
	    }
	}

	protected Persistence persistence = null;
	protected PermissionManager permissions = null;
	protected PermissionsProxy proxy = null;
	protected static final Logger log = Persistence.getLogger();
}
