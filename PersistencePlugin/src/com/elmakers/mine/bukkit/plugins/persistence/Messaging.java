package com.elmakers.mine.bukkit.plugins.persistence;

import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.plugins.persistence.dao.CommandData;
import com.elmakers.mine.bukkit.plugins.persistence.dao.Message;

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
		this.plugin = plugin;
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
	public CommandData getCommand(String id)
	{
		CommandData command = persistence.get(id, CommandData.class);
		if (command == null)
		{
			command = new CommandData(plugin, id, id);
			persistence.put(command);
		}
		return command;
	}
	
	private Persistence persistence;
	private Plugin plugin;
}
