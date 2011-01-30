package com.elmakers.mine.bukkit.plugins.persistence;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;

public class PersistencePlayerListener extends PlayerListener
{

	@Override
	public void onPlayerCommand(PlayerChatEvent event)
	{
		Player player = event.getPlayer();
		
		// Currently only ops can use Persistence commands.
		if (!player.isOp()) return;
		
		String[] split = event.getMessage().split(" ");
    	String commandString = split[0];
    	
    	if (!commandString.equalsIgnoreCase("/persist")) return;
    	
    	event.setCancelled(true);
    	
    	if (split.length == 1)
    	{
    		player.sendMessage("Use \"/persist help\" for help.");
    		return;
    	}
    	
    	String command = split[1];
    	
    	if (command.equalsIgnoreCase("help"))
    	{
    		printHelp(player);
    		return;
    	}
	}
	
	protected void printHelp(Player player)
	{
		String helpMessage = 
			"Persistence:\r"
		+	"/persist reload <schema>.<entity> : Reload entities\r"
		+	"/persist describe <schema> : List entities in a schema\r"
		+	"/persist describe <schema>.<entity> : Describe an entity";
		
		String[] lines = helpMessage.split("\r");
		for (String line : lines)
		{
			player.sendMessage(line);
		}
	}

}
