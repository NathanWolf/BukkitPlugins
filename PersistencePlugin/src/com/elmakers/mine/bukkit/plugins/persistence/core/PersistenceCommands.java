package com.elmakers.mine.bukkit.plugins.persistence.core;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.persistence.Messaging;
import com.elmakers.mine.bukkit.plugins.persistence.Persistence;
import com.elmakers.mine.bukkit.plugins.persistence.PersistencePlugin;
import com.elmakers.mine.bukkit.plugins.persistence.dao.CommandData;
import com.elmakers.mine.bukkit.plugins.persistence.dao.Message;

public class PersistenceCommands
{

	public void initialize(PersistencePlugin plugin, Persistence persistence)
	{
		Messaging messaging = persistence.getMessaging(plugin);	
		shortHelpMessage = messaging.getMessage("shortHelpMessage", PersistenceDefaults.shortHelpMessage);
		persistCommand = messaging.getCommand(PersistenceDefaults.persistCommand);
	}

	public boolean process(CommandSender messageOutput, Command cmd, String[] parameters)
	{
		Persistence persistence = Persistence.getInstance();
		
		// Currently only ops can use Persistence commands.
		if (messageOutput instanceof Player)
		{
			Player player = (Player)messageOutput;
			if (!player.isOp()) return false;
		}
		
		if (!persistCommand.checkCommand(cmd)) return false;
    	
    	if (parameters.length == 0)
    	{
    		shortHelpMessage.sendTo(messageOutput, persistCommand.getCommandMatch());
    		return true;
    	}
    	
    	String command = parameters[0];
    	
    	if (command.equalsIgnoreCase("help"))
    	{
    		printHelp(messageOutput);
    		return true;
    	}

    	if (command.equalsIgnoreCase("save"))
    	{
    		persistence.save();
    		messageOutput.sendMessage("Data saved");
    		return true;
    	}
    	
    	if (command.equalsIgnoreCase("reload"))
    	{
    		if (parameters.length < 2)
    		{
    			messageOutput.sendMessage("Use: \"/persist reload <schema>.<entity>");
    			return true;
    		}
    		String[] entityPath = parameters[1].split("\\.");
    		if (entityPath.length < 2)
    		{
    			messageOutput.sendMessage("Use: \"/persist reload <schema>.<entity>");
    			return true;
    		}
    		String schemaName = entityPath[0];
    		String entityName = entityPath[1];	
    		reloadEntity(messageOutput, schemaName, entityName);
    		return true;
    	}
    	
       	if (command.equals("RESET"))
    	{
    		if (parameters.length < 2)
    		{
    			messageOutput.sendMessage("Use: \"/perist RESET <schema>.<entity>");
    			return true;
    		}
    		String[] entityPath = parameters[1].split("\\.");
    		if (entityPath.length < 2)
    		{
    			messageOutput.sendMessage("Use: \"/persist RESET <schema>.<entity>");
    			return true;
    		}
    		String schemaName = entityPath[0];
    		String entityName = entityPath[1];	
    		resetEntity(messageOutput, schemaName, entityName);
    		return true;
    	}
       	
       	if (command.equalsIgnoreCase("reset"))
       	{
       		messageOutput.sendMessage("Use: \"/persist RESET <schema>.<entity>");
       		messageOutput.sendMessage("Be VERY sure!");
       		return true;
       	}
    	
    	if (command.equalsIgnoreCase("describe"))
    	{
    		if (parameters.length < 2)
    		{
    			listSchemas(messageOutput);
    			return true;
    		}
    		
    		String[] entityPath = parameters[1].split("\\.");
    		if (entityPath.length == 1)
    		{
    			describeSchema(messageOutput, entityPath[0]);
    		}
    		else
    		{
    			describeEntity(messageOutput, entityPath[0], entityPath[1]);
    		}
    		return true;
    	}
    	
    	if (command.equalsIgnoreCase("list"))
    	{
    		if (parameters.length < 2)
    		{
    			messageOutput.sendMessage("Use: \"/persist list <schema>.<entity>[.<id>]");
    			return true;
    		}
    		
    		String[] entityPath = parameters[1].split("\\.");
    		if (entityPath.length < 2)
    		{
    			messageOutput.sendMessage("Use: \"/persist list <schema>.<entity>[.<id>]");
    			return true;
    		}
    		
    		String schemaName = entityPath[0];
    		String entityName = entityPath[1];
    		
    		if (entityPath.length == 2)
    		{
    			listEntityIds(messageOutput, schemaName, entityName);
    			return true;
    		}
    		
    		String id = entityPath[2];
    		listEntity(messageOutput, schemaName, entityName, id);
    		
    		return true;
    	}
    	
    	messageOutput.sendMessage("Unknown /persist command. Type \"/persist help\" for help.");
    	return true;
	}
	
	protected void listEntity(CommandSender messageOutput, String schemaName, String entityName, String id)
	{
		PersistedClass persisted = getEntity(messageOutput, schemaName, entityName);
		if (persisted == null) return;
		
		Object instance = persisted.get(id);
		
		if (instance == null)
		{
			messageOutput.sendMessage("Can't find entity " + schemaName + "." + entityName + ", " + persisted.getIdField().getName() + "=" + id);
			return;
		}
		
		List<String> rows = new ArrayList<String>();
		for (PersistedField field : persisted.getPersistedFields())
		{
			String fieldName = field.getName();
			fieldName = padColumn(fieldName);
			
			
			Object data = field.get(instance);
			String dataField = "null";
			if (data != null)
			{
				dataField = data.toString();
			}
			
			String row = fieldName + " = " + dataField;
			rows.add(row);
		}
		
		messageOutput.sendMessage("Entity " + schemaName + "." + entityName + ":");
		for (String row : rows)
		{
			messageOutput.sendMessage(row);
		}
	}
	
	protected void listEntities(CommandSender messageOutput, String schemaName, String entityName)
	{
		PersistedClass persisted = getEntity(messageOutput, schemaName, entityName);
		if (persisted == null) return;
		
		String heading = "";
		List<String> rows = new ArrayList<String>();
		for (PersistedField field : persisted.getPersistedFields())
		{
			String fieldName = field.getName();
			fieldName = padColumn(fieldName);
			
			String newHeading = heading +  fieldName + " | ";
			if (newHeading.length() > maxLineLength)
			{
				break;
			}
			heading = newHeading;
		}
		
		int lineCount = 0;
		List<Object> entities = new ArrayList<Object>();
		persisted.getAll(entities);
		
		for (Object entity : entities)
		{
			String row = "";
			for (PersistedField field : persisted.getPersistedFields())
			{
				Object data = field.get(entity);
				String dataField = "null";
				if (data != null)
				{
					dataField = data.toString();
				}
				dataField = padColumn(dataField);
				String newRow = row + dataField + " | ";
				if (newRow.length() > maxLineLength)
				{
					break;
				}
				row = newRow;
			}
			
			rows.add(row);
			
			if (lineCount >= maxLineCount)
			{
				break;
			}
		}
		
		messageOutput.sendMessage(heading);
		for (String row : rows)
		{
			messageOutput.sendMessage(row);
		}
		
	}
	
	protected String padColumn(String column)
	{
		return String.format("%1$-" + maxColumnWidth + "s", column);
	}
	
	protected void listEntityIds(CommandSender messageOutput, String schemaName, String entityName)
	{
		PersistedClass persisted = getEntity(messageOutput, schemaName, entityName);
		if (persisted == null) return;
		
		List<Object> entities = new ArrayList<Object>();
		persisted.getAll(entities);
		
		messageOutput.sendMessage(schemaName + "." + entityName + ", " + entities.size() + " entities:");
		
		int idCount = 0;
		List<String> idLines = new ArrayList<String>();
		String currentLine = "";
		for (Object entity : entities)
		{
			boolean firstInLine = currentLine.length() == 0;
			
			if (!firstInLine)
			{
				currentLine += ", ";
			}
			else
			{
				currentLine = " ";
			}
			String thisId = persisted.getId(entity).toString();
			String newLine = currentLine + thisId;
			if (newLine.length() > maxLineLength)
			{
				idLines.add(currentLine);
				currentLine = " " + thisId;
			}
			else
			{
				currentLine = newLine;
			}
			idCount++;
			if (idCount >= maxIdCount)
			{
				currentLine += "...";
			}
		}
		if (currentLine.length() > 0)
		{
			idLines.add(currentLine);
		}
		for (String idLine : idLines)
		{
			messageOutput.sendMessage(idLine);
		}
	}
	
	protected void listSchemas(CommandSender messageOutput)
	{
		Persistence persistence = Persistence.getInstance();
		messageOutput.sendMessage("Schemas:");
		List<Schema> schemas = persistence.getSchemaList();
		for (Schema schema : schemas)
		{
			String schemaMessage = " " + schema.getName() + " [" + schema.getPersistedClasses().size() + "]";
			messageOutput.sendMessage(schemaMessage);
		}
	}
	
	protected PersistedClass getEntity(CommandSender messageOutput, String schemaName, String entityName)
	{
		Persistence persistence = Persistence.getInstance();
		Schema schema = persistence.getSchema(schemaName);
		if (schema == null)
		{
			messageOutput.sendMessage("Unknown schema: " + schemaName);
			return null;
		}
		PersistedClass persisted = schema.getPersistedClass(entityName);
		if (persisted == null)
		{
			messageOutput.sendMessage("Unknown entity: " + schemaName + "." + entityName);
			return null;
		}
		return persisted;
	}
	
	protected void describeSchema(CommandSender messageOutput, String schemaName)
	{
		Persistence persistence = Persistence.getInstance();
		Schema schema = persistence.getSchema(schemaName);
		if (schema == null)
		{
			messageOutput.sendMessage("Unknown schema: " + schemaName);
			return;
		}		
		messageOutput.sendMessage("Schema " + schemaName + ":");
		for (PersistedClass persisted : schema.getPersistedClasses())
		{
			String schemaMessage = " " + persisted.getTableName();
			messageOutput.sendMessage(schemaMessage);
		}
	}

	protected void describeEntity(CommandSender messageOutput, String schemaName, String entityName)
	{
		PersistedClass persisted = getEntity(messageOutput, schemaName, entityName);
		if (persisted == null) return;
		
		messageOutput.sendMessage("Entity " + schemaName + "." + entityName + ":");
		for (PersistedField field : persisted.getPersistedFields())
		{
			String entityMessage = " " + field.getName() + " : " + field.getColumnType();
			messageOutput.sendMessage(entityMessage);
		}
	}
	
	protected void reloadEntity(CommandSender messageOutput, String schemaName, String entityName)
	{
		PersistedClass persisted = getEntity(messageOutput, schemaName, entityName);
		if (persisted == null) return;
		
		messageOutput.sendMessage("Reloading entity: " + schemaName + "." + entityName);
		persisted.clear();
	}
	
	protected void resetEntity(CommandSender messageOutput, String schemaName, String entityName)
	{
		PersistedClass persisted = getEntity(messageOutput, schemaName, entityName);
		if (persisted == null) return;
		
		resettingEntityMessage.sendTo(messageOutput, schemaName, entityName);
		persisted.reset();
	}
	
	protected void printHelp(CommandSender messageOutput)
	{
		// TODO: Get this dynamically from the CommandData store
		messageOutput.sendMessage(PersistenceDefaults.helpHeader);
		for (int i = 0; i < PersistenceDefaults.subCommands.length; i++)
		{
			String helpLine = PersistenceDefaults.subCommands[i] + " : "
				+ PersistenceDefaults.subCommandHelp[i];
			
			messageOutput.sendMessage(helpLine);
		}
	}
	
	private int maxLineCount = 10;
	private int maxColumnWidth = 10;
	private int maxLineLength = 50;
	private int maxIdCount = 50;
	
	private CommandData persistCommand;	
	private Message shortHelpMessage;
	private Message resettingEntityMessage;

}
