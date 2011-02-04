package com.elmakers.mine.bukkit.plugins.persistence.dao;

import java.util.List;

import org.bukkit.command.Command;

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
	public CommandData(PluginData plugin, String command)
	{
		this.plugin = plugin;
		this.command = command;
	}
	
	/**
	 * Add a command to this command as a sub-command.
	 * 
	 * Sub-commands are activated using parameters. So:
	 *
	 * /persist list global.player.NathanWolf
	 * 
	 * Consists of the main Command "persist", one sub-command "list",
	 * and one parameter "global.player.NathanWolf".
	 * 
	 * @param command The command to add as a sub-command of this one
	 */
	public void addSubCommand(CommandData command)
	{
		children.add(command);
		command.setParent(this);
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
	
	@Persist(id=true, auto=true)
	public int getId()
	{
		return id;
	}
	
	public void setId(int id)
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
	public void setPlugin(PluginData plugin)
	{
		this.plugin = plugin;
	}

	public PluginData getPlugin()
	{
		return plugin;
	}

	@Persist
	public void setParent(CommandData parent)
	{
		this.parent = parent;
	}

	public CommandData getParent()
	{
		return parent;
	}
	
	@Persist
	public void setChildren(List<CommandData> children)
	{
		this.children = children;
	}

	public List<CommandData> getChildren()
	{
		return children;
	}

	private int					id;
	private String				command;
	private PluginData			plugin;
	private CommandData			parent;
	private List<CommandData>	children;
}
