package com.elmakers.mine.bukkit.plugins.persistence.messages.dao;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import com.elmakers.mine.bukkit.plugins.persistence.annotations.PersistClass;
import com.elmakers.mine.bukkit.plugins.persistence.annotations.Persist;

@PersistClass(name="command", schema="global")
public class Command
{
	public Command()
	{
		
	}
	
	public Command(Plugin plugin, String id, String command)
	{
		PluginDescriptionFile pdfFile = plugin.getDescription();
		this.pluginId = pdfFile.getName();
		this.id = id;
		this.command = command;
	}
	
	public String[] checkCommand(String consoleMessage)
	{
		String[] consolePieces = consoleMessage.split(" ");
		String commandCheck = getCommandMatch();
		if (consolePieces == null || consolePieces.length < 0 || !commandCheck.equalsIgnoreCase(consolePieces[0]))
		{
			return null;
		}
		
		String[] parameters = new String[consolePieces.length - 1];
		for (int i = 1; i < consolePieces.length; i++)
		{
			parameters[i - 1] = consolePieces[i];
		}
		return parameters;
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
