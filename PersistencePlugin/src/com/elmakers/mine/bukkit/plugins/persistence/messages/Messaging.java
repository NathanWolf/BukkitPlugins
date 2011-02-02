package com.elmakers.mine.bukkit.plugins.persistence.messages;

import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.plugins.persistence.Persistence;
import com.elmakers.mine.bukkit.plugins.persistence.messages.dao.CommandData;
import com.elmakers.mine.bukkit.plugins.persistence.messages.dao.Message;

public class Messaging
{
	public Messaging(Plugin plugin, Persistence persistence)
	{
		this.persistence = persistence;
		this.plugin = plugin;
	}
	
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
