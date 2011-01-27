package com.elmakers.mine.bukkit.plugins.persistence;

import java.io.File;

import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

public class PersistencePlugin extends JavaPlugin
{

	/*
	 * Plugin interface
	 */
	
	public PersistencePlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder,
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
	}
	
	/*
	 * Private data
	 */
	
	private final Persistence 	persistence = new Persistence();

}
