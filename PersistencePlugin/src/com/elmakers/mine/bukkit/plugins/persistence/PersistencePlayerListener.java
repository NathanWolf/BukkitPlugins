package com.elmakers.mine.bukkit.plugins.persistence;

import java.util.List;

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
    	
    	if (command.equalsIgnoreCase("describe"))
    	{
    		if (split.length < 3)
    		{
    			listSchemas(player);
    			return;
    		}
    		
    		String[] entityPath = split[2].split("\\.");
    		if (entityPath.length == 1)
    		{
    			listSchema(player, entityPath[0]);
    		}
    		else
    		{
    			listEntity(player, entityPath[0], entityPath[1]);
    		}
    		return;
    	}
    	
    	player.sendMessage("Unknown /persist command. Type \"/persist help\" for help.");
	}
	
	protected void listSchemas(Player player)
	{
		Persistence persistence = Persistence.getInstance();
		player.sendMessage("Schemas:");
		List<Schema> schemas = persistence.getSchemaList();
		for (Schema schema : schemas)
		{
			String schemaMessage = " " + schema.getName() + " [" + schema.getPersistedClasses().size() + "]";
			player.sendMessage(schemaMessage);
		}
	}
	
	protected void listSchema(Player player, String schemaName)
	{
		Persistence persistence = Persistence.getInstance();
		Schema schema = persistence.getSchema(schemaName);
		if (schema == null)
		{
			player.sendMessage("Unknown schema: " + schemaName);
			return;
		}		
		player.sendMessage("Schema " + schemaName + ":");
		for (PersistedClass persisted : schema.getPersistedClasses())
		{
			String schemaMessage = " " + persisted.getTableName();
			player.sendMessage(schemaMessage);
		}
	}
	
	protected void listEntity(Player player, String schemaName, String entityName)
	{
		Persistence persistence = Persistence.getInstance();
		Schema schema = persistence.getSchema(schemaName);
		if (schema == null)
		{
			player.sendMessage("Unknown schema: " + schemaName);
			return;
		}
		PersistedClass persisted = schema.getPersistedClass(entityName);
		if (persisted == null)
		{
			player.sendMessage("Unknown entity: " + schemaName + "." + entityName);
			return;
		}
		player.sendMessage("Entity " + schemaName + "." + entityName + ":");
		for (PersistedField field : persisted.getPersistedFields())
		{
			String entityMessage = " " + field.getName() + " : " + field.getColumnType();
			player.sendMessage(entityMessage);
		}
	}
	
	protected void printHelp(Player player)
	{
		String helpMessage = 
			"Persistence:\r"
		+	"/persist reload <schema>.<entity> : Reload entities\r"
		+	"/persist describe : List all schemas\r"
		+	"/persist describe <schema> : List entities in a schema\r"
		+	"/persist describe <schema>.<entity> : Describe an entity";
		
		String[] lines = helpMessage.split("\r");
		for (String line : lines)
		{
			player.sendMessage(line);
		}
	}

}
