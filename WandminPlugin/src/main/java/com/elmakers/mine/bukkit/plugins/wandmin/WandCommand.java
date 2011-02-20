package com.elmakers.mine.bukkit.plugins.wandmin;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Type;
import org.bukkit.event.player.PlayerChatEvent;

public class WandCommand 
{
	private String command;
	private String name;
	private String description;
	
	public void copyTo(WandCommand other)
	{
		other.command = command;
		other.name = name;
		other.description = description;
	}
	
	public void use(WandminPlugin plugin, Player player)
	{
		CraftServer server = (CraftServer)plugin.getServer();
		
		// Try new way first, then old way.
		if (!server.dispatchCommand(player, command))
		{
			server.getPluginManager().callEvent(new PlayerChatEvent(Type.PLAYER_COMMAND, player, "/" + command));
		}
	}
	
	public String getCommand()
	{
		return command;
	}
	
	public String getDescription()
	{
		return description;
	}

	public String getName()
	{
		return name;
	}
	
	public void setCommand(String command)
	{
		this.command = command;
		if (this.name == null)
		{
			this.name = command;
		}
	}
}
