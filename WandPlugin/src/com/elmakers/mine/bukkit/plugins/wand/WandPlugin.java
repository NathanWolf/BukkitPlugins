package com.elmakers.mine.bukkit.plugins.wand;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class WandPlugin extends JavaPlugin 
{
	
	@Override
	public void onInitialize()
	{
		
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
