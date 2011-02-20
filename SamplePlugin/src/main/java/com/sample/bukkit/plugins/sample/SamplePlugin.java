package com.sample.bukkit.plugins.sample;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.plugins.persistence.Persistence;
import com.elmakers.mine.bukkit.plugins.persistence.PersistencePlugin;
import com.elmakers.mine.bukkit.plugins.persistence.PluginUtilities;
import com.elmakers.mine.bukkit.plugins.persistence.dao.Message;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PermissionType;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PlayerData;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PluginCommand;
import com.sample.bukkits.plugins.sample.dao.SamplePlayerData;

public class SamplePlugin extends JavaPlugin
{

	/* Process player quit and join messages.
	 * 
	 * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		return utilities.dispatch(this, sender, cmd.getName(), args);
	}
	
	/**
	 * Default constructor as required by JavaPlugin.
	 * 
	 * You don't ever really need to do anything with this, but it has to be here.
	 * 
	 * Most, if not all, of this data is available to you directly via the JavaPlugin base class.
	 * 
	 * @param pluginLoader The pluginLoader
	 * @param instance The instance
	 * @param desc The desc
	 * @param folder The folder
	 * @param plugin The plugin
	 * @param cLoader The classLoader
	 */
	public SamplePlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader)
	{
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
	}

	/* Called when you plugin is disabled
	 * @see org.bukkit.plugin.Plugin#onDisable()
	 */
	public void onDisable()
	{
		// TODO Auto-generated method stub
	}

	/* Called when your plugin is enabled
	 * @see org.bukkit.plugin.Plugin#onEnable()
	 */
	public void onEnable()
	{
		if (initialize())
		{
			PluginDescriptionFile pdfFile = this.getDescription();
	        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
		}
		else
		{
			PluginDescriptionFile pdfFile = this.getDescription();
	        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " failed to initialize");
		}	
	}
	
	/**
	 * Initialize this plugin, bind to Persistence and initialize command handling and messaging.
	 * 
	 * @return true if successful - on false, this plugin should be disabled.
	 */
	public boolean initialize()
	{
		try
		{
			Plugin checkForPersistence = this.getServer().getPluginManager().getPlugin("Persistence");
			if (checkForPersistence != null)
			{
				PersistencePlugin plugin = (PersistencePlugin) checkForPersistence;
				persistence = plugin.getPersistence();
			}
			else
			{
				log.warning("This plugin depends on Persistence");
				this.getServer().getPluginManager().disablePlugin(this);
				return false;
			}

			SampleDefaults d = new SampleDefaults();
			utilities = persistence.getUtilities(this);

			// Messages
			changedNicknameMessage = utilities.getMessage("changedNickname", d.changedNicknameMessage);
			showNicknameMessage = utilities.getMessage("showNickname", d.showNicknameMessage);
			unknownPlayerMessage = utilities.getMessage("unknownPlayer", d.unknownPlayerMessage);
			noNicknameMessage = utilities.getMessage("noNickname", d.noNicknameMessage);

			// Commands
			setCommand = utilities.getGeneralCommand(d.setCommand[0], d.setCommand[1], d.setCommand[2], PermissionType.ADMINS_ONLY);
			setNickNameCommand = setCommand.getSubCommand(d.setNickNameCommand[0], d.setNickNameCommand[1], d.setNickNameCommand[2], PermissionType.ADMINS_ONLY);

			showCommand = utilities.getGeneralCommand(d.showCommand[0], d.showCommand[1], d.showCommand[2]);
			showNickNameCommand = showCommand.getSubCommand(d.showNickNameCommand[0], d.showNickNameCommand[1], d.showNickNameCommand[2]);

			// Bind commands to handler methods
			showNickNameCommand.bind("onShowNickname");
			setNickNameCommand.bind("onSetNickname");
		} 
		catch(Throwable ex)
		{
			log.log(Level.SEVERE, "Error intializing plugin: ", ex);
			return false;
		}
		
		return true;
	}
		
	/**
	 * Find some user data, and create it if it does not exist
	 * 
	 * @param playerName The player to find data for
	 * @return A SamplePlayerData for this player, or null if the player is unknown.
	 */
	public SamplePlayerData createData(String playerName)
	{
		SamplePlayerData sampleData = null;

		// Check for playerData
		PlayerData playerData = persistence.get(playerName, PlayerData.class);
		if (playerData == null)
		{
			return null;
		}
		
		SamplePlayerData samplePlayer = persistence.get(playerData, SamplePlayerData.class);
		if (samplePlayer == null)
		{
			samplePlayer = new SamplePlayerData(playerData);
			persistence.put(samplePlayer);
		}

		return sampleData;
	}

	public boolean onShowNickname(CommandSender messageOutput, String[] parameters)
	{
		if (parameters.length == 0)
		{
			return false;
		}
		
		String playerName = parameters[0];
		
		SamplePlayerData player = createData(playerName);
		if (player == null)
		{
			unknownPlayerMessage.sendTo(messageOutput, playerName);
			return true;
		}
		
		String nickname = player.getNickname();
		if (nickname == null || nickname.length() == 0)
		{
			noNicknameMessage.sendTo(messageOutput, playerName);
		}
		else
		{
			showNicknameMessage.sendTo(messageOutput, nickname);
		}
		
		return true;
	}
	
	
	public boolean onSetNickname(CommandSender messageOutput, String[] parameters)
	{
		if (parameters.length < 2)
		{
			return false;
		}
		
		String playerName = parameters[0];
		String nickname = parameters[1];
		
		SamplePlayerData player = createData(playerName);
		if (player == null)
		{
			unknownPlayerMessage.sendTo(messageOutput, playerName);
			return true;
		}
		
		player.setNickname(nickname);
		persistence.put(player);
		changedNicknameMessage.sendTo(messageOutput, playerName, nickname);
		
		return true;
	}
	
	protected Persistence persistence = null;
	protected PluginUtilities utilities = null;
	
	private PluginCommand setCommand;
	private PluginCommand setNickNameCommand;
	private PluginCommand showCommand;
	private PluginCommand showNickNameCommand;
	private Message changedNicknameMessage;
	private Message showNicknameMessage;
	private Message unknownPlayerMessage;
	private Message noNicknameMessage;

	protected static final Logger log = Persistence.getLogger();
}
