package com.elmakers.mine.bukkit.plugins.persistence.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.persistence.PluginUtilities;
import com.elmakers.mine.bukkit.plugins.persistence.Persistence;
import com.elmakers.mine.bukkit.plugins.persistence.PersistencePlugin;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PluginCommand;
import com.elmakers.mine.bukkit.plugins.persistence.dao.Message;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PluginData;

public class PersistenceCommands
{

	public void initialize(PersistencePlugin plugin, Persistence persistence)
	{
		PersistenceDefaults d = new PersistenceDefaults();
		this.persistence = persistence;
		utilities = persistence.getUtilities(plugin);	

		// Initialize Messages
		
		resettingEntityMessage = utilities.getMessage("resettingEntity", d.resettingEntityMessage);
		reloadingEntityMessage = utilities.getMessage("reloadingEntity", d.reloadingEntityMessage);
		entityNotFoundMessage = utilities.getMessage("entityNotFound", d.entityNotFoundMessage);
		entityDisplayMessage = utilities.getMessage("entityDisplay", d.entityDisplayMessage);
		schemaDisplayMessage = utilities.getMessage("schemaDisplay", d.schemaDisplayMessage);
		entityListMessage = utilities.getMessage("entityList", d.entityListMessage);
		schemaListMessage = utilities.getMessage("schemaList", d.schemaListMessage);
		unknownSchemaMessage = utilities.getMessage("unknownSchema", d.unknownSchemaMessage);
		unknownEntityMessage = utilities.getMessage("unknownEntity", d.unknownEntityMessage);
		dataSavedMessage = utilities.getMessage("dataSaved", d.dataSavedMessage);
		pluginListMessage = utilities.getMessage("pluginList", d.pluginListMessage);
		pluginNotFoundMessage = utilities.getMessage("pluginNotFound", d.pluginNotFoundMessage);

		// Iniitialize Commands
		persistCommand = utilities.getGeneralCommand(d.persistCommand[0], d.persistCommand[1], d.persistCommand[2]);
		saveSubCommand = persistCommand.getSubCommand(d.saveSubCommand[0], d.saveSubCommand[1], d.saveSubCommand[2]);
		describeSubCommand = persistCommand.getSubCommand(d.describeSubCommand[0], d.describeSubCommand[1], d.describeSubCommand[2]);
		listSubCommand = persistCommand.getSubCommand(d.listSubCommand[0], d.listSubCommand[1], d.listSubCommand[2]);
		reloadSubCommand = persistCommand.getSubCommand(d.reloadSubCommand[0], d.reloadSubCommand[1], d.reloadSubCommand[2]);
		resetSubCommand = persistCommand.getSubCommand(d.resetSubCommand[0], d.resetSubCommand[1], d.resetSubCommand[2]);
		
		helpCommand = utilities.getGeneralCommand(d.helpCommand[0], d.helpCommand[1], d.helpCommand[2]);
		
		for (String usage : d.helpUsage)
		{
			helpCommand.addUsage(usage);
		}
		
		for (String usage : d.describeUsage)
		{
			describeSubCommand.addUsage(usage);
		}
		
		for (String usage : d.listUsage)
		{
			listSubCommand.addUsage(usage);
		}
		
		saveSubCommand.bind("onSave");
		describeSubCommand.bind("onDescribe");
		listSubCommand.bind("onList");
		reloadSubCommand.bind("onReload");
		resetSubCommand.bind("onReset");
		
		helpCommand.bind("onHelp");
	}
	
	public boolean process(CommandSender messageOutput, Command cmd, String[] parameters)
	{
		// Currently only ops can use Persistence commands.
		// Anyone can use phelp, though.!
		if (messageOutput instanceof Player && !helpCommand.checkCommand(cmd.getName()))
		{
			Player player = (Player)messageOutput;
			if (!player.isOp()) return false;
		}
	
		return utilities.dispatch(this, messageOutput, cmd.getName(), parameters);
	}
		
	public boolean onSave(CommandSender messageOutput, String[] parameters)
	{
   		persistence.save();
   		dataSavedMessage.sendTo(messageOutput);
    	return true;    	
    }
	
	public boolean onHelp(CommandSender messageOutput, String[] parameters)
	{
		List<PluginData> plugins = new ArrayList<PluginData>();
		persistence.getAll(plugins, PluginData.class);

		if (parameters == null || parameters.length == 0)
		{
			pluginListMessage.sendTo(messageOutput);
			for (PluginData plugin : plugins)
			{
				messageOutput.sendMessage(" " + plugin.getId() + " : " + plugin.getDescription());
			}
			return true;
		}
		
		if (parameters[0].equalsIgnoreCase("commands"))
		{
			List<PluginCommand> allCommands = new ArrayList<PluginCommand>();
			for (PluginData plugin : plugins)
			{
				allCommands.addAll(plugin.getCommands());
			}
			
			Collections.sort(allCommands);
			for (PluginCommand command : allCommands)
			{
				command.sendHelp(messageOutput, " ", false, false);
			}
			
			return true;
			
		}
		
		String commandName = parameters[0];
		for (PluginData plugin : plugins)
		{
			List<PluginCommand> commands = plugin.getCommands();
			for (PluginCommand command : commands)
			{
				if (command.checkCommand(commandName))
				{
					command.sendHelp(messageOutput, " ", true, true);
					return true;
				}
			}
		}
		
		String pluginName = parameters[0];
		for (PluginData plugin : plugins)
		{
			if (pluginName.equalsIgnoreCase(plugin.getId()))
			{
				messageOutput.sendMessage(plugin.getId() + " v" + plugin.getVersion() + ":");
				List<PluginCommand> commands = plugin.getCommands();
				for (PluginCommand command : commands)
				{
					command.sendHelp(messageOutput, " ", false, true);
				}
				return true;
			}
		}
		
		pluginNotFoundMessage.sendTo(messageOutput, pluginName);
		
		return true;
	}
	
	public boolean onReload(CommandSender messageOutput, String[] parameters)
	{
		if (parameters.length < 1)
		{
			reloadSubCommand.sendShortHelp(messageOutput);
			return true;
		}
		String[] entityPath = parameters[1].split("\\.");
		if (entityPath.length < 2)
		{
			reloadSubCommand.sendShortHelp(messageOutput);
			return true;
		}
		String schemaName = entityPath[0];
		String entityName = entityPath[1];	
		reloadEntity(messageOutput, schemaName, entityName);
		return true;
	}
    	
	public boolean onReset(CommandSender messageOutput, String[] parameters)
	{
		if (parameters.length < 1)
		{
			resetSubCommand.sendShortHelp(messageOutput);
			return true;
		}
		String[] entityPath = parameters[1].split("\\.");
		if (entityPath.length < 1)
		{
			resetSubCommand.sendShortHelp(messageOutput);
			return true;
		}
		String schemaName = entityPath[0];
		String entityName = entityPath[1];	
		resetEntity(messageOutput, schemaName, entityName);
		return true;
	}
 	
	public boolean onDescribe(CommandSender messageOutput, String[] parameters)
	{
		if (parameters.length < 1)
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

	public boolean onList(CommandSender messageOutput, String[] parameters)
	{
		if (parameters.length < 1)
		{
			listSubCommand.sendShortHelp(messageOutput);
			return true;
		}
		
		String[] entityPath = parameters[1].split("\\.");
		if (entityPath.length < 2)
		{
			listSubCommand.sendShortHelp(messageOutput);
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
	
	protected void listEntity(CommandSender messageOutput, String schemaName, String entityName, String id)
	{
		PersistedClass persisted = getEntity(messageOutput, schemaName, entityName);
		if (persisted == null) return;
		
		Object instance = persisted.get(id);
		
		if (instance == null)
		{
			entityNotFoundMessage.sendTo(messageOutput, schemaName, entityName, persisted.getIdField().getName(), id);
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
		
		entityListMessage.sendTo(messageOutput, schemaName, entityName);
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
		schemaListMessage.sendTo(messageOutput);
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
			unknownSchemaMessage.sendTo(messageOutput, schemaName);
			return null;
		}
		PersistedClass persisted = schema.getPersistedClass(entityName);
		if (persisted == null)
		{
			unknownEntityMessage.sendTo(messageOutput, schemaName, entityName);
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
			unknownSchemaMessage.sendTo(messageOutput, schemaName);
			return;
		}		
		schemaDisplayMessage.sendTo(messageOutput, schemaName);
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
		
		entityDisplayMessage.sendTo(messageOutput, schemaName, entityName);
		for (PersistedField field : persisted.getPersistedFields())
		{
			String entityMessage = " " + field.getName() + " : " + field.getDataType();
			messageOutput.sendMessage(entityMessage);
		}
	}
	
	protected void reloadEntity(CommandSender messageOutput, String schemaName, String entityName)
	{
		PersistedClass persisted = getEntity(messageOutput, schemaName, entityName);
		if (persisted == null) return;
		
		reloadingEntityMessage.sendTo(messageOutput, schemaName, entityName);
		persisted.clear();
	}
	
	protected void resetEntity(CommandSender messageOutput, String schemaName, String entityName)
	{
		PersistedClass persisted = getEntity(messageOutput, schemaName, entityName);
		if (persisted == null) return;
		
		resettingEntityMessage.sendTo(messageOutput, schemaName, entityName);
		persisted.reset();
	}
	
	private int maxLineCount = 10;
	private int maxColumnWidth = 10;
	private int maxLineLength = 50;
	private int maxIdCount = 50;
	
	private PluginCommand persistCommand;	
	private PluginCommand saveSubCommand;
	private PluginCommand describeSubCommand;
	private PluginCommand listSubCommand;
	private PluginCommand reloadSubCommand;
	private PluginCommand resetSubCommand;
	private PluginCommand helpCommand;

	private Message resettingEntityMessage;
	private Message reloadingEntityMessage;
	private Message entityNotFoundMessage;
	private Message entityDisplayMessage;
	private Message entityListMessage;
	private Message schemaDisplayMessage;
	private Message schemaListMessage;
	private Message unknownSchemaMessage;
	private Message unknownEntityMessage;
	private Message dataSavedMessage;
	private Message pluginListMessage;
	private Message pluginNotFoundMessage;
	
	private PluginUtilities utilities;
	private Persistence persistence;

}
