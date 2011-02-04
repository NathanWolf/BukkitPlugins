package com.elmakers.mine.bukkit.plugins.persistence.dao;

import org.bukkit.command.Command;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import com.elmakers.mine.bukkit.plugins.persistence.annotation.Persist;
import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistClass;

/**
 * A data class for encapsulating and storing a Command object.
 * 
 * @author NathanWolf
 *
 */
@PersistClass(name="command", schema="global")
public class CommandData
{
	/**
	 * The default constructor, used by Persistence to create instances.
	 */
	public CommandData()
	{
		
	}
	
	/**
	 * A constructor used to create new CommandData objects manually.
	 * 
	 * This may change in the future.
	 * 
	 * @param plugin The plugin that is registering this command
	 * @param id The command id
	 * @param command The command string, or alias
	 */
	public CommandData(Plugin plugin, String id, String command)
	{
		PluginDescriptionFile pdfFile = plugin.getDescription();
		this.pluginId = pdfFile.getName();
		this.id = id;
		this.command = command;
	}
	
	/**
	 * Check to see if this command matches a given command string.
	 * 
	 * Will eventually check permissions, look for sub-commands, and other 
	 * things. 
	 * 
	 * @param cmd The command string to check
	 * @return Whether or not the command succeeded
	 */
	public boolean checkCommand(Command cmd)
	{
		// TODO: permissions check, sub-commands, etc
		return command.equalsIgnoreCase(cmd.getName());
	}
	
	/**
	 * Get the command string that would be seen in a player message.
	 * 
	 * Currently, this is used only to generate help text- it will probably be
	 * removed in the future.
	 * 
	 * @return The command name prefixed with a slash
	 */
	public String getCommandMatch()
	{
		return "/" + command;
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
	public String getCommand()
	{
		return command;
	}
	
	public void setCommand(String command)
	{
		this.command = command;
	}

	@Persist
	public void setPluginId(String pluginId)
	{
		this.pluginId = pluginId;
	}

	public String getPluginId()
	{
		return pluginId;
	}

	private String id;
	private String command;
	private String pluginId;
}
