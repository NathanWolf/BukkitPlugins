package com.elmakers.mine.bukkit.plugins.nether;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.plugins.persistence.Persistence;
import com.elmakers.mine.bukkit.plugins.persistence.PersistencePlugin;

public class NetherGatePlugin extends JavaPlugin
{

	public NetherGatePlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder,
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
		Plugin checkForPersistence = this.getServer().getPluginManager().getPlugin("Persistence");
	    if(checkForPersistence != null) 
	    {
	    	PersistencePlugin plugin = (PersistencePlugin)checkForPersistence;
	    	persistence = plugin.getPersistence();
	    } 
	    else 
	    {
	    	log.warning("The NetherGate plugin depends on Persistence");
	    	this.getServer().getPluginManager().disablePlugin(this);
	    	return;
	    }
	    
		PluginDescriptionFile pdfFile = this.getDescription();
        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		return false;
	}

	protected NetherManager nether = new NetherManager();
	private static final Logger log = Logger.getLogger("Minecraft");
	protected Persistence persistence = null;
}
