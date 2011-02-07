package com.elmakers.mine.bukkit.plugins.persistence.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.persistence.Persistence;
import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistField;
import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistClass;

/**
 * A data class for encapsulating and storing a Command object.
 * 
 * @author NathanWolf
 *
 */
@PersistClass(name="command", schema="global")
public class PluginCommand implements Comparable<PluginCommand>
{	
	/**
	 * The default constructor, used by Persistence to create instances.
	 */
	public PluginCommand()
	{
		
	}
	
	/**
	 * A constructor used to create new CommandData objects manually.
	 * 
	 * This may change in the future.
	 * 
	 * @param plugin The plugin that is registering this command
	 * @param command The command string, or alias
	 * @param tooltip The tooltip for this command
	 * @param usage The usage instructions for this command, more can be added later
	 * @param sender The sender that receives this command, more can be added later
	 */
	public PluginCommand(PluginData plugin, String command, String tooltip, String usage, CommandSenderData sender)
	{
		this.plugin = plugin;
		this.command = command;
		this.tooltip = tooltip;
		addUsage(usage);
		addSender(sender);
	}
	
	/**
	 * Set up automatic command binding for this command.
	 * 
	 * If you dispatch commands with messaging.dispatch, this command will automatically call
	 * the given method on the listener class if executed.
	 * 
	 * For Player commands, the signature should be:
	 * 
	 * public boolean onMyCommand(Player player, String[] parameters)
	 * {
	 * }
	 * 
	 * For General commands, a CommandSender should be used in place of Player.
	 * 
	 * @param methodName
	 * @see com.elmakers.mine.bukkit.plugins.persistence.Messaging#dispatch(Object, CommandSender, String, String[])
	 */
	public void bind(String methodName)
	{
		callbackMethod = methodName;
	}
	
	/**
	 * Use this to add an additional usage (example) string to this command.
	 * 
	 * @param use The usage string
	 */
	public void addUsage(String use)
	{
		if (use == null || use.length() <= 0) return;
		
		if (usage == null)
		{
			usage = new ArrayList<String>();
		}
		if (!usage.contains(use))
		{
			usage.add(use);
		}
	}
	
	/**
	 * Use this to add an additional command sender that is able to receive this type of message.
	 * 
	 * @param sender the command sender to add
	 */
	public void addSender(CommandSenderData sender)
	{
		if (sender == null) return;
		
		if (senders == null)
		{
			senders = new ArrayList<CommandSenderData>();
		}
		if (!senders.contains(sender))
		{
			senders.add(sender);
		}
	}
	
	/**
	 * Get or create a sub-command of this command.
	 * 
	 * @param subCommandName The sub-command name
	 * @param defaultTooltip The default tooltip
	 * @param defaultUsage The default usage string
	 * @return A new command object
	 */
	public PluginCommand getSubCommand(String subCommandName, String defaultTooltip, String defaultUsage)
	{
		PluginCommand child = childMap.get(subCommandName);
		if (child == null)
		{
			child = new PluginCommand(plugin, subCommandName, defaultTooltip, defaultUsage, null);
			persistence.put(child);
			addSubCommand(child);
		}
		
		return child;
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
	protected void addSubCommand(PluginCommand command)
	{
		if (children == null)
		{
			children = new ArrayList<PluginCommand>();
		}
		children.add(command);
		command.setParent(this);
		if (senders != null)
		{
			for (CommandSenderData sender : senders)
			{
				command.addSender(sender);
			}
		}
	}
	
	/**
	 * Check to see if this command matches a given command string.
	 * 
	 * Will eventually check permissions, look for sub-commands, and other 
	 * things. 
	 * 
	 * @param commandString The command string to check
	 * @return Whether or not the command succeeded
	 */
	public boolean checkCommand(String commandString)
	{
		// TODO: permissions check, sub-commands, etc
		return command.equals(commandString) || command.equals(commandString.toLowerCase());
	}
	
	/**
	 * Use to send a short informational help message
	 * 
	 * This can be used when the player has mis-entered parameters or some other exceptional case.
	 * 
	 * @param sender The CommandSender to reply to
	 */
	public void sendShortHelp(CommandSender sender)
	{
		sendHelp(sender, "Use: ", false, false);
	}
	
	/**
	 * Use this to display a help message for this command to the given sender.
	 * 
	 * CommandSender may be a player, server console, etc.
	 * 
	 * @param sender The CommandSender (e.g. Player) to display help to
	 * @param prefix A prefix, such as "Use: " to put in front of the first line
	 * @param showUsage Whether or not to show detailed usage information
	 */
	public void sendHelp(CommandSender sender, String prefix, boolean showUsage, boolean showSubCommands)
	{
		boolean useSlash = sender instanceof Player;
		String slash = useSlash ? "/" : "";
		String currentIndent = getIndent("");
		String message = currentIndent + slash + " " + getPath() + " : "  + tooltip;
		sender.sendMessage(prefix + message);
		currentIndent += indent;
		
		if (showUsage && usage != null)
		{
			for (String exampleUse : usage)
			{
				sender.sendMessage(currentIndent + exampleUse);
			}
		}
		
		if (showSubCommands && children != null)
		{
			for (PluginCommand child : children)
			{
				child.sendHelp(sender, "", showUsage, showSubCommands);
			}
		}
	}
		
	public int compareTo(PluginCommand compare)
	{
		return command.compareTo(compare.getCommand());
	}

	protected String getIndent(String begin)
	{
		if (parent != null)
		{
			begin = indent + parent.getIndent(begin);
		}
		return begin;
	}
	
	protected String getPath()
	{
		String path = command;
		if (parent != null)
		{
			path = parent.getPath() + " " + path;
		}
		return path;
	}
	
	@PersistField(id=true, auto=true)
	public int getId()
	{
		return id;
	}
	
	public void setId(int id)
	{
		this.id = id;
	}
	
	@PersistField
	public String getCommand()
	{
		return command;
	}
	
	public void setCommand(String command)
	{
		this.command = command;
	}

	@PersistField
	public void setPlugin(PluginData plugin)
	{
		this.plugin = plugin;
	}

	public PluginData getPlugin()
	{
		return plugin;
	}

	@PersistField
	public void setParent(PluginCommand parent)
	{
		this.parent = parent;
	}

	public PluginCommand getParent()
	{
		return parent;
	}
	
	@PersistField
	public void setChildren(List<PluginCommand> children)
	{
		this.children = children;
		
		// Create child map
		childMap.clear();
		if (children != null)
		{
			for (PluginCommand child : children)
			{
				childMap.put(child.getCommand(), child);
			}
		}
	}

	public List<PluginCommand> getChildren()
	{
		return children;
	}

	@PersistField
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	@PersistField
	public void setUsage(List<String> usage)
	{
		this.usage = usage;
	}

	public List<String> getUsage()
	{
		return usage;
	}

	@PersistField
	public void setTooltip(String tooltip)
	{
		this.tooltip = tooltip;
	}

	public String getTooltip()
	{
		return tooltip;
	}

	@PersistField
	public void setSenders(List<CommandSenderData> senders)
	{
		this.senders = senders;
	}

	public List<CommandSenderData> getSenders()
	{
		return senders;
	}

	public String getCallbackMethod()
	{
		return callbackMethod;
	}
	
	public void setPersistence(Persistence persistence)
	{
		this.persistence = persistence;
	}

	private Persistence			persistence;
	private String				callbackMethod;
	private int					id;
	private boolean				enabled = true;
	private String				command;
	private String				tooltip;
	private List<String>		usage;
	private PluginData			plugin;
	private PluginCommand		parent;
	private List<PluginCommand>	children;
	private List<CommandSenderData> senders;
	private HashMap<String, PluginCommand> childMap = new HashMap<String, PluginCommand>();
	private static final String indent = "  ";
}
