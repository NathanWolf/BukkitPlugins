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
import com.elmakers.mine.bukkit.plugins.persistence.dao.PermissionType;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PlayerData;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PlayerGroup;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PluginCommand;
import com.elmakers.mine.bukkit.plugins.persistence.dao.Message;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PluginData;
import com.elmakers.mine.bukkit.plugins.persistence.dao.ProfileData;

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
		commandListMessage = utilities.getMessage("commandList", d.commandListMessage);
		pluginNotFoundMessage = utilities.getMessage("pluginNotFound", d.pluginNotFoundMessage);
		suEnabledMessage = utilities.getMessage("suEnabled", d.suEnabledMessage);
		suDisabledMessage = utilities.getMessage("suDisabled", d.suDisabledMessage);
		addedPlayerToGroupMessage = utilities.getMessage("addedPlayerToGroup", d.addedPlayerToGroupMessage);
		removedPlayerFromGroupMessage = utilities.getMessage("removedPlayerFromGroup", d.removedPlayerFromGroupMessage);
		createdGroupMessage = utilities.getMessage("createdGroup", d.createdGroupMessage);
		denyAccessMessage = utilities.getMessage("denyAccess", d.denyAccessMessage);
		grantAccessMessage = utilities.getMessage("grantAccess", d.grantAccessMessage);
		groupExistsMessage = utilities.getMessage("groupExistss", d.groupExistsMessage);
		playerNotFoundMessage = utilities.getMessage("playerNotFound", d.playerNotFoundMessage);
		groupNotFoundMessage = utilities.getMessage("groupNotFound", d.groupNotFoundMessage);
		unknownProfileMessage = utilities.getMessage("unknownProfile", d.unknownProfileMessage);
		
		// Initialize Commands
		persistCommand = utilities.getGeneralCommand(d.persistCommand[0], d.persistCommand[1], d.persistCommand[2], PermissionType.ADMINS_ONLY);
		saveSubCommand = persistCommand.getSubCommand(d.saveSubCommand[0], d.saveSubCommand[1], d.saveSubCommand[2], PermissionType.ADMINS_ONLY);
		describeSubCommand = persistCommand.getSubCommand(d.describeSubCommand[0], d.describeSubCommand[1], d.describeSubCommand[2], PermissionType.ADMINS_ONLY);
		listSubCommand = persistCommand.getSubCommand(d.listSubCommand[0], d.listSubCommand[1], d.listSubCommand[2], PermissionType.ADMINS_ONLY);
		reloadSubCommand = persistCommand.getSubCommand(d.reloadSubCommand[0], d.reloadSubCommand[1], d.reloadSubCommand[2], PermissionType.ADMINS_ONLY);
		resetSubCommand = persistCommand.getSubCommand(d.resetSubCommand[0], d.resetSubCommand[1], d.resetSubCommand[2], PermissionType.ADMINS_ONLY);
		
		groupCommand = utilities.getGeneralCommand(d.groupCommand[0], d.groupCommand[1], d.groupCommand[2], PermissionType.ADMINS_ONLY);
		groupCreateCommand = groupCommand.getSubCommand(d.groupCreateCommand[0], d.groupCreateCommand[1], d.groupCreateCommand[2], PermissionType.ADMINS_ONLY);
		groupAddCommand = groupCommand.getSubCommand(d.groupAddCommand[0], d.groupAddCommand[1], d.groupAddCommand[2], PermissionType.ADMINS_ONLY);
		groupRemoveCommand = groupCommand.getSubCommand(d.groupRemoveCommand[0], d.groupRemoveCommand[1], d.groupRemoveCommand[2], PermissionType.ADMINS_ONLY);
		
		denyCommand = utilities.getGeneralCommand(d.denyCommand[0], d.denyCommand[1], d.denyCommand[2], PermissionType.ADMINS_ONLY);
		denyPlayerCommand = denyCommand.getSubCommand(d.denyPlayerCommand[0], d.denyPlayerCommand[1], d.denyPlayerCommand[2], PermissionType.ADMINS_ONLY);
		denyGroupCommand = denyCommand.getSubCommand(d.denyGroupCommand[0], d.denyGroupCommand[1], d.denyGroupCommand[2], PermissionType.ADMINS_ONLY);	
		
		grantCommand = utilities.getGeneralCommand(d.grantCommand[0], d.grantCommand[1], d.grantCommand[2], PermissionType.ADMINS_ONLY);
		grantPlayerCommand = grantCommand.getSubCommand(d.grantPlayerCommand[0], d.grantPlayerCommand[1], d.grantPlayerCommand[2], PermissionType.ADMINS_ONLY);
		grantGroupCommand = grantCommand.getSubCommand(d.grantGroupCommand[0], d.grantGroupCommand[1], d.grantGroupCommand[2], PermissionType.ADMINS_ONLY);
		
		helpCommand = utilities.getGeneralCommand(d.helpCommand[0], d.helpCommand[1], d.helpCommand[2]);
		
		// Player commands
		// TODO - not sure this is going to work right when switching back and forth between built-in and bukkit permissions .. ?
		PermissionType suType = Persistence.allowOpSU() ? PermissionType.OPS_ONLY : PermissionType.ADMINS_ONLY;
		suCommand = utilities.getPlayerCommand(d.suCommand[0], d.suCommand[1], d.suCommand[2], suType);
		
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
		suCommand.bind("onSU");
		
		groupCreateCommand.bind("onCreateGroup");
		groupAddCommand.bind("onAddToGroup");
		groupRemoveCommand.bind("onRemoveFromGroup");
		denyPlayerCommand.bind("onDenyPlayer");
		denyGroupCommand.bind("onGrantPlayer");
		grantPlayerCommand.bind("onDenyGroup");
		grantGroupCommand.bind("onGrantGroup");
		
		helpCommand.bind("onHelp");
	}
	
	public boolean process(CommandSender messageOutput, Command cmd, String[] parameters)
	{
		return utilities.dispatch(this, messageOutput, cmd.getName(), parameters);
	}
	
	public boolean onCreateGroup(CommandSender messageOutput, String[] parameters)
	{
		if (parameters.length == 0)
		{
			return false;
		}
		
		String groupName = parameters[0];
		
		// First check for existing
		PlayerGroup group = persistence.get(groupName, PlayerGroup.class);
		if (group != null)
		{
			groupExistsMessage.sendTo(messageOutput, groupName);
			return true;
		}
		
		group = new PlayerGroup(groupName);
		persistence.put(group);
		createdGroupMessage.sendTo(messageOutput, groupName);
		
		return true;
	}
	
	public boolean onAddToGroup(CommandSender messageOutput, String[] parameters)
	{
		if (parameters.length < 2)
		{
			return false;
		}
		
		String playerName = parameters[0];
		String groupName = parameters[1];
		
		// First check for group
		PlayerGroup group = persistence.get(groupName, PlayerGroup.class);
		if (group == null)
		{
			groupNotFoundMessage.sendTo(messageOutput, groupName);
			return true;
		}
		
		// Check for player
		PlayerData playerData = persistence.get(playerName, PlayerData.class);
		if (playerData == null)
		{
			playerNotFoundMessage.sendTo(messageOutput, playerName);
			return true;
		}
		
		playerData.addToGroup(group);
		persistence.put(playerData);
		addedPlayerToGroupMessage.sendTo(messageOutput, playerName, groupName);
		
		return true;
	}
	
	// TODO: Less copy+paste! In a hurry....
	public boolean onRemoveFromGroup(CommandSender messageOutput, String[] parameters)
	{
		if (parameters.length < 2)
		{
			return false;
		}
		
		String playerName = parameters[0];
		String groupName = parameters[1];
		
		// First check for group
		PlayerGroup group = persistence.get(groupName, PlayerGroup.class);
		if (group == null)
		{
			groupNotFoundMessage.sendTo(messageOutput, group);
			return true;
		}
		
		// Check for player
		PlayerData playerData = persistence.get(playerName, PlayerData.class);
		if (playerData == null)
		{
			playerNotFoundMessage.sendTo(messageOutput, group);
			return true;
		}
		
		playerData.removeFromGroup(group);
		persistence.put(playerData);
		removedPlayerFromGroupMessage.sendTo(messageOutput, playerName, groupName);
		
		return true;
	}
	
	public boolean onDenyPlayer(CommandSender messageOutput, String[] parameters)
	{
		if (parameters.length < 2)
		{
			return false;
		}
		
		String playerName = parameters[0];
		String profileName = parameters[1];
		
		// First check for permission profile
		ProfileData profileData = persistence.get(profileName, ProfileData.class);
		if (profileData == null)
		{
			unknownProfileMessage.sendTo(messageOutput, profileName);
			return true;
		}
		
		// Check for player
		PlayerData playerData = persistence.get(playerName, PlayerData.class);
		if (playerData == null)
		{
			playerNotFoundMessage.sendTo(messageOutput, playerName);
			return true;
		}
		
		playerData.denyPermission(profileData);
		persistence.put(playerData);
		denyAccessMessage.sendTo(messageOutput, profileName, "player", playerName);
		
		return true;
	}
	
	public boolean onGrantPlayer(CommandSender messageOutput, String[] parameters)
	{
		if (parameters.length < 2)
		{
			return false;
		}
		
		String playerName = parameters[0];
		String profileName = parameters[1];
		
		// First check for permission profile
		ProfileData profileData = persistence.get(profileName, ProfileData.class);
		if (profileData == null)
		{
			unknownProfileMessage.sendTo(messageOutput, profileName);
			return true;
		}
		
		// Check for player
		PlayerData playerData = persistence.get(playerName, PlayerData.class);
		if (playerData == null)
		{
			playerNotFoundMessage.sendTo(messageOutput, playerName);
			return true;
		}
		
		playerData.grantPermission(profileData);
		persistence.put(playerData);
		grantAccessMessage.sendTo(messageOutput, profileName, "player", playerName);
		
		return true;
	}
	
	public boolean onDenyGroup(CommandSender messageOutput, String[] parameters)
	{
		if (parameters.length < 2)
		{
			return false;
		}
		
		String groupName = parameters[0];
		String profileName = parameters[1];
		
		// First check for permission profile
		ProfileData profileData = persistence.get(profileName, ProfileData.class);
		if (profileData == null)
		{
			unknownProfileMessage.sendTo(messageOutput, profileName);
			return true;
		}
		
		// Check for group
		PlayerGroup group = persistence.get(groupName, PlayerGroup.class);
		if (group == null)
		{
			groupNotFoundMessage.sendTo(messageOutput, groupName);
			return true;
		}
		
		group.denyPermission(profileData);
		persistence.put(group);
		denyAccessMessage.sendTo(messageOutput, profileName, "group", groupName);
			
		return true;
	}
	
	public boolean onGrantGroup(CommandSender messageOutput, String[] parameters)
	{
		if (parameters.length < 2)
		{
			return false;
		}
		
		String groupName = parameters[0];
		String profileName = parameters[1];
		
		// First check for permission profile
		ProfileData profileData = persistence.get(profileName, ProfileData.class);
		if (profileData == null)
		{
			unknownProfileMessage.sendTo(messageOutput, profileName);
			return true;
		}
		
		// Check for group
		PlayerGroup group = persistence.get(groupName, PlayerGroup.class);
		if (group == null)
		{
			groupNotFoundMessage.sendTo(messageOutput, groupName);
			return true;
		}
		
		group.grantPermission(profileData);
		persistence.put(group);
		grantAccessMessage.sendTo(messageOutput, profileName, "group", groupName);
		
		return true;
	}

	public boolean onSU(Player player, String[] parameters)
	{
		PlayerData playerData = persistence.get(player.getName(), PlayerData.class);
		if (playerData == null) return false;
		
		if (playerData.isSuperUser())
		{
			suDisabledMessage.sendTo(player);
			playerData.setSuperUser(false);
		}
		else
		{
			suEnabledMessage.sendTo(player);
			playerData.setSuperUser(true);
		}
		
		persistence.put(playerData);
		
		// This is an important change.
		persistence.save();
		
		return true;
	}
	
	public boolean onSave(CommandSender messageOutput, String[] parameters)
	{
   		persistence.save();
   		dataSavedMessage.sendTo(messageOutput);
    	return true;    	
    }
	
	public boolean onHelpPlugins(CommandSender messageOutput, String[] parameters)
	{
		List<PluginData> plugins = new ArrayList<PluginData>();
		persistence.getAll(plugins, PluginData.class);

		pluginListMessage.sendTo(messageOutput);
				
		for (PluginData plugin : plugins)
		{
			messageOutput.sendMessage(" " + plugin.getId() + " : " + plugin.getDescription());
		}
		return true;
	}
	
	public boolean onHelp(CommandSender messageOutput, String[] parameters)
	{
		List<PluginData> plugins = new ArrayList<PluginData>();
		persistence.getAll(plugins, PluginData.class);

		if (parameters == null || parameters.length == 0)
		{
			commandListMessage.sendTo(messageOutput);
			List<PluginCommand> allCommands = new ArrayList<PluginCommand>();
			for (PluginData plugin : plugins)
			{
				List<PluginCommand> pluginCommands = plugin.getCommands();
				for (PluginCommand command : pluginCommands)
				{
					if (command.checkPermission(messageOutput))
					{
						allCommands.add(command);
					}
				}
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
				if (command.checkCommand(messageOutput, commandName) && command.checkPermission(messageOutput))
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
		
		String[] entityPath = parameters[0].split("\\.");
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
			return onDescribe(messageOutput, parameters);
		}
		
		String[] entityPath = parameters[0].split("\\.");
		if (entityPath.length < 2)
		{
			return onDescribe(messageOutput, parameters);
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
			PersistedClass refType = field.getReferenceType();
			if (refType != null)
			{
				data = refType.getIdData(data);
			}
			
			String dataField = "null";
			if (data != null)
			{
				dataField = data.toString();
			}
			
			if (refType != null)
			{
				dataField = refType.getSchema() + "." + refType.getTableName() + "." + dataField;
			}
			
			String row = fieldName + " = " + dataField;
			rows.add(row);
		}
		
		entityDisplayMessage.sendTo(messageOutput, schemaName, entityName);
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
		
		entityListMessage.sendTo(messageOutput, schemaName, entityName, entities.size());
		
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
			String thisId = persisted.getIdData(entity).toString();
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
	
	protected PluginCommand getSUCommand()
	{
		return suCommand;
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
	private PluginCommand suCommand;
	private PluginCommand groupCommand;
	private PluginCommand groupCreateCommand;
	private PluginCommand groupRemoveCommand;
	private PluginCommand groupAddCommand;
	private PluginCommand denyCommand;
	private PluginCommand denyPlayerCommand;
	private PluginCommand denyGroupCommand;
	private PluginCommand grantCommand;
	private PluginCommand grantPlayerCommand;
	private PluginCommand grantGroupCommand;

	private Message groupExistsMessage;
	private Message addedPlayerToGroupMessage;
	private Message removedPlayerFromGroupMessage;
	private Message createdGroupMessage;
	private Message denyAccessMessage;
	private Message grantAccessMessage;
	private Message playerNotFoundMessage;
	private Message unknownProfileMessage;
	private Message groupNotFoundMessage;
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
	private Message commandListMessage;
	private Message pluginNotFoundMessage;
	private Message suEnabledMessage;
	private Message suDisabledMessage;
	
	private PluginUtilities utilities;
	private Persistence persistence;

}
