package com.elmakers.mine.bukkit.plugins.wand;

import java.io.File;

import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class WandPlugin extends JavaPlugin 
{

	public WandPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File plugin, ClassLoader cLoader) 
	{
        super(pluginLoader, instance, desc, plugin, cLoader);
    }
	
	@Override
	public void onEnable() 
	{

        PluginManager pm = getServer().getPluginManager();
		// TODO: Register our events        

        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
	}

	@Override
	public void onDisable() 
	{
		System.out.println("Goodbye world!");

	}

}
