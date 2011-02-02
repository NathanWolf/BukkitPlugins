package com.elmakers.mine.bukkit.plugins.persistence.messages.dao;

import org.bukkit.command.Command;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import com.elmakers.mine.bukkit.plugins.persistence.annotations.PersistClass;
import com.elmakers.mine.bukkit.plugins.persistence.annotations.Persist;

@PersistClass(name="command", schema="global")
public class CommandData
{
	public CommandData()
	{
		
	}
	
	public CommandData(Plugin plugin, String id, String command)
	{
		PluginDescriptionFile pdfFile = plugin.getDescription();
		this.pluginId = pdfFile.getName();
		this.id = id;
		this.command = command;
	}
	
	public boolean checkCommand(Command cmd)
	{
		// TODO: permissions check, etc
		return command.equalsIgnoreCase(cmd.getName());
	}
	
	public String getCommandMatch()
	{
		return "/" + command;
	}
	
	@Persist(id=true)
	public String getId()
	{
		return id;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	@Persist
	public String getCommand()
	{
		return command;
	}
	
	public void setCommand(String command)
	{
		this.command = command;
	}

	@Persist
	public void setPluginId(String pluginId)
	{
		this.pluginId = pluginId;
	}

	public String getPluginId()
	{
		return pluginId;
	}

	private String id;
	private String command;
	private String pluginId;
}
