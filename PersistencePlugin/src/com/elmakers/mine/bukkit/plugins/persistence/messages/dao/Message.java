package com.elmakers.mine.bukkit.plugins.persistence.messages.dao;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.persistence.annotations.Persist;
import com.elmakers.mine.bukkit.plugins.persistence.annotations.PersistClass;

@PersistClass(schema="global", name="message")
public class Message
{
	public void sendTo(Player player, Object ... parameters)
	{
		if (!enabled) return;
		String[] lines = getLines(parameters);
		for (String line : lines)
		{
			player.sendMessage(line);
		}
	}
	
	public String[] getLines(Object ... parameters)
	{
		String baseMessage = String.format(message, parameters);
		return baseMessage.split("\r");
	}
	
	public Message()
	{
		
	}
	
	public Message(String id, String message)
	{
		this.id = id;
		this.message = message;
	}
	
	public String toString()
	{
		return message;
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
	public String getMessage()
	{
		return message;
	}
	
	public void setMessage(String message)
	{
		this.message = message;
	}
	
	@Persist
	public boolean isEnabled()
	{
		return enabled;
	}
	
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}
	
	public void setPluginId(String pluginId)
	{
		this.pluginId = pluginId;
	}

	public String getPluginId()
	{
		return pluginId;
	}

	private String id;
	private String message;
	private String pluginId;
	private boolean enabled = true;
}
