package com.elmakers.mine.bukkit.plugins.persistence;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import com.elmakers.mine.bukkit.plugins.persistence.dao.CommandData;
import com.elmakers.mine.bukkit.plugins.persistence.dao.Message;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PluginData;

/** 
 * An interface for displaying data-driven messages and processing data-driven commands.
 * 
 * @author NathanWolf
 *
 */
public class Messaging
{
	/**
	 * Messaging constructor. Use to create an instance of Messaging for your plugin.
	 * 
	 * This can also be done via persistence.getMessaging(plugin)
	 * 
	 * @param plugin The plugin requesting the messaging interface
	 * @param persistence The Persistence reference to use for retrieving data
	 */
	public Messaging(Plugin plugin, Persistence persistence)
	{
		this.persistence = persistence;
		
		// Retreive or create the plugin data record for this plugin.
		PluginDescriptionFile pdfFile = plugin.getDescription();
		String pluginId = pdfFile.getName();
		pluginData = persistence.get(pluginId, PluginData.class);
		if (pluginData == null)
		{
			pluginData = new PluginData(plugin);
			persistence.put(pluginData);
		}
	}
	
	/**
	 * Get a message based on id, or create one using a default.
	 * 
	 * @param id The message id
	 * @param defaultString The default string to use if no value exists
	 * @return The stored message, or defaultString if none exists
	 */
	public Message getMessage(String id, String defaultString)
	{
		Message message = persistence.get(id, Message.class);
		if (message == null)
		{
			message = new Message(id, defaultString);
			persistence.put(message);
		}
		return message;
	}
	
	/**
	 * Retrieve a command description based on id. 
	 * 
	 * A command description can be used to easily process commands, including
	 * commands with sub-commands.
	 * 
	 * @param id The command id to retrieve.
	 * @return A command descriptor
	 */
	public CommandData getCommand(String commandName)
	{
		// First, look for a root command by this name
		List<CommandData> allCommands = new ArrayList<CommandData>();
		persistence.getAll(allCommands, CommandData.class);
		
		for (CommandData  command : allCommands)
		{
			if (command.getCommand().equalsIgnoreCase(commandName))
			{
				return command;
			}
		}
		
		CommandData command = persistence.get(commandName, CommandData.class);
		if (command == null)
		{
			command = new CommandData(pluginData, commandName);
			persistence.put(command);
		}
		return command;
	}
	
	public CommandData getSubCommand(CommandData parent, String id)
	{
		CommandData child = null;
		
		
		return child;
	}
	
	private Persistence persistence;
	private PluginData pluginData;
}
