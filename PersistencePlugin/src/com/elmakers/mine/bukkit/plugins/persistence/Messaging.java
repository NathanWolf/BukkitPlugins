package com.elmakers.mine.bukkit.plugins.persistence;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import com.elmakers.mine.bukkit.plugins.persistence.dao.CommandSenderData;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PluginCommand;
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
	 * @param requestingPlugin The plugin requesting the messaging interface
	 * @param persistence The Persistence reference to use for retrieving data
	 */
	public Messaging(Plugin requestingPlugin, Persistence persistence)
	{
		this.persistence = persistence;
		
		// Retreive or create the plugin data record for this plugin.
		PluginDescriptionFile pdfFile = requestingPlugin.getDescription();
		String pluginId = pdfFile.getName();
		plugin = persistence.get(pluginId, PluginData.class);
		if (plugin == null)
		{
			plugin = new PluginData(requestingPlugin);
			persistence.put(plugin);
		}
		
		playerSender = persistence.get("player", CommandSenderData.class);
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
		if (messages == null)
		{
			messages = new ArrayList<Message>();
		}

		// First, look for a root command by this name
		Message message = messageMap.get(id);
		if (message == null)
		{
			message = new Message(id, defaultString);
			persistence.put(message);
			messages.add(message);
			messageMap.put(id, message);
		}
		
		return message;
	}
	
	/**
	 * Retrieve a player command description based on id. 
	 * 
	 * A command description can be used to easily process commands, including
	 * commands with sub-commands.
	 * 
	 * This method automatically creates a player-specific (in-game) command.
	 * 
	 * @param commandName The command id to retrieve or create
	 * @param defaultTooltip The default tooltip to use if this is a new command
	 * @param defaultUsage The default usage string, more can be added
	 * @return A command descriptor
	 */
	public PluginCommand getPlayerCommand(String commandName, String defaultTooltip, String defaultUsage)
	{
		return getCommand(commandName, defaultTooltip, defaultUsage, playerSender);
	}
	
	/**
	 * Retrieve a general command description based on id. 
	 * 
	 * A command description can be used to easily process commands, including
	 * commands with sub-commands.
	 * 
	 * This method automatically creates a general command that will be passed
	 * a CommandSender for use as a server or in-game command.
	 * 
	 * @param commandName The command id to retrieve or create
	 * @param defaultTooltip The default tooltip to use if this is a new command
	 * @param defaultUsage The default usage string, more can be added
	 * @return A command descriptor
	 */
	public PluginCommand getGeneralCommand(String commandName, String defaultTooltip, String defaultUsage)
	{
		return getCommand(commandName, defaultTooltip, defaultUsage, null);
	}
	
	/**
	 * Retrieve a command description based on id, for a given sender
	 * 
	 * A command description can be used to easily process commands, including
	 * commands with sub-commands.
	 * 
	 * @param commandName The command id to retrieve or create
	 * @param defaultTooltip The default tooltip to use if this is a new command
	 * @param defaultUsage The default usage string, more can be added
	 * @param sender The sender that will issue this command
	 * @return A command descriptor
	 */
	public PluginCommand getCommand(String commandName, String defaultTooltip, String defaultUsage, CommandSenderData sender)
	{
		// First, look for a root command by this name
		List<PluginCommand> commands = plugin.getCommands();
		if (commands == null)
		{
			commands = new ArrayList<PluginCommand>();
			plugin.setCommands(commands);
		}
		for (PluginCommand  command : commands)
		{
			if (command.getCommand().equalsIgnoreCase(commandName))
			{
				return command;
			}
		}
		
		// Create a new un-parented command
		PluginCommand command = new PluginCommand(plugin, commandName, defaultTooltip, defaultUsage, sender);
		command.setPersistence(persistence);
		persistence.put(command);
		plugin.addCommand(command);
		return command;
	}

	/**
	 * Dispatch any automatically bound command handlers.
	 * 
	 * Any commands registered with this plugin that around bound() to a command handler will be automatically called.
	 * 
	 * For Player commands, the signature should be:
	 * 
	 * public boolean onMyCommand(Player player, String[] parameters)
	 * {
	 * }
	 * 
	 * For General commands, a CommandSender should be used in place of Player.
	 * 
	 * @param listener The class that will handle the command callback
	 * @param sender The sender of this command
	 * @param baseCommand The base command issues
	 * @param baseParameters Any parameters (or sub-commands) passed to the base command 
	 * @see PluginCommand#bind(String)
	 */
	public boolean dispatch(Object listener, CommandSender sender, String baseCommand, String[] baseParameters)
	{
		List<PluginCommand> baseCommands = plugin.getCommands();
		if (baseCommands == null) return false;
		
		for (PluginCommand command : baseCommands)
		{
			boolean success = dispatch(listener, sender, command, baseCommand, baseParameters);
			if (success) return true;
		}
		return false;
	}
	
	protected boolean dispatch(Object listener, CommandSender sender, PluginCommand command, String commandString, String[] parameters)
	{
		if (command.checkCommand(commandString))
		{
			boolean handledByChild = false;
			if (parameters != null && parameters.length > 0)
			{
				String[] childParameters = new String[parameters.length - 1];
				for (int i = 0; i < childParameters.length; i++)
				{
					childParameters[i] = parameters[i + 1];
				}
				String childCommand = parameters[0];
				
				List<PluginCommand> subCommands = command.getChildren();
				if (subCommands != null)
				{
					for (PluginCommand child : subCommands)
					{
						handledByChild = dispatch(listener, sender, child, childCommand, childParameters);
						if (handledByChild)
						{
							return true;
						}
					}
				}
			}
			
			// Not handled by a sub-child, so handle it ourselves.
			String callbackName = command.getCallbackMethod();
			if (callbackName == null || callbackName.length() <= 0) return false;
			
			try
			{
				List<CommandSenderData> senders = command.getSenders();
				
				if (senders != null)
				{
					for (CommandSenderData senderData : senders)
					{
						Class<?> senderType = senderData.getType();
						if (senderType == null) continue;
						try
						{
							Method customHandler;
							customHandler = listener.getClass().getMethod(callbackName, senderType, String[].class);
							return (Boolean)customHandler.invoke(listener, senderType.cast(sender), parameters);
						}
						catch (NoSuchMethodException e)
						{
						}					
					}
				}

				Method genericHandler;
				genericHandler = listener.getClass().getMethod(callbackName, CommandSender.class, String[].class);
				return (Boolean)genericHandler.invoke(listener, sender, parameters);
			}
			catch (NoSuchMethodException ex)
			{
				log.warning("Persistence: Can't find callback method " + callbackName + " of " + listener.getClass().getName());
			}					
			catch (SecurityException ex)
			{
				log.warning("Persistence: Can't access callback method " + callbackName + " of " + listener.getClass().getName() + ", make sure it's public");
			}
			catch (IllegalArgumentException ex)
			{
				log.warning("Persistence: Can't find callback method " + callbackName + " of " + listener.getClass().getName() + " with the correct signature, please consult the docs.");
			}
			catch (IllegalAccessException ex)
			{
				log.warning("Persistence: Can't access callback method " + callbackName + " of " + listener.getClass().getName());
			}
			catch (InvocationTargetException ex)
			{
				log.severe("Persistence: Error invoking callback method " + callbackName + " of " + listener.getClass().getName());
				ex.printStackTrace();
			}
		}
		
		return false;
	}

	private Persistence persistence;
	private PluginData plugin;
	private CommandSenderData playerSender;
	private static final Logger log = Persistence.getLogger();
	private List<Message> messages;
	private HashMap<String, Message> messageMap = new HashMap<String, Message>();
}
