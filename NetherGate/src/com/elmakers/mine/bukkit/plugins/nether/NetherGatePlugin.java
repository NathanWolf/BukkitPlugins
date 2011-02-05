package com.elmakers.mine.bukkit.plugins.nether;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.plugins.persistence.Messaging;
import com.elmakers.mine.bukkit.plugins.persistence.Persistence;
import com.elmakers.mine.bukkit.plugins.persistence.PersistencePlugin;
import com.elmakers.mine.bukkit.plugins.persistence.dao.Message;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PluginCommand;

public class NetherGatePlugin extends JavaPlugin
{

	public NetherGatePlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder,
			File plugin, ClassLoader cLoader)
	{
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
	}

	@Override
	public void onDisable()
	{
		
	}

	@Override
	public void onEnable()
	{
		try
		{
			initialize();
			PluginDescriptionFile pdfFile = this.getDescription();
	        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
		}
		catch(Throwable e)
		{
			PluginDescriptionFile pdfFile = this.getDescription();
	        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " failed to initialize");	
	        e.printStackTrace();
		}
	}
	
	public void initialize()
	{
		Plugin checkForPersistence = this.getServer().getPluginManager().getPlugin("Persistence");
	    if(checkForPersistence != null) 
	    {
	    	PersistencePlugin plugin = (PersistencePlugin)checkForPersistence;
	    	persistence = plugin.getPersistence();
	    } 
	    else 
	    {
	    	log.warning("The NetherGate plugin depends on Persistence");
	    	this.getServer().getPluginManager().disablePlugin(this);
	    	return;
	    }
	    
	    messaging = persistence.getMessaging(this);
	    nether.initialize(persistence, messaging);
	    
		netherCommand = messaging.getPlayerCommand("nether", "Manage Nether areas", "use phelp nether for help");
		createCommand = netherCommand.getSubCommand("create", "Create a new Nether area undergroup", "create");
		
		createCommand.bind("onCreate");
		netherCommand.bind("onNether");
		
		creationFailedMessage = messaging.getMessage("creationFailed", "Nether creation failed- is there enough room below you?");
		creationSuccessMessage = messaging.getMessage("creationSuccess", "Created new Nether area");
		netherExistsMessage = messaging.getMessage("netherExist", "A Nether area already exists here");
	}
	
	public boolean onNether(Player player, String[] parameters)
	{
		// Currently only ops can use nether
		// TODO: Implement permissions
		if (!player.isOp()) return false;
		
		netherCommand.sendHelp(player, "Use : ", true, true);
		
		return true;
	}
	
	public boolean onCreate(Player player, String[] parameters)
	{
		// Currently only ops can use nether
		// TODO: Implement permissions
		if (!player.isOp()) return false;
		
		// TODO: Check for existing Nether areas.
		
		if (!nether.create(player))
		{
			creationFailedMessage.sendTo(player);
		}
		else
		{
			creationSuccessMessage.sendTo(player);
		}
		
		return true;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		return messaging.dispatch(this, sender, cmd.getName(), args);
	}

	protected PluginCommand netherCommand;
	protected PluginCommand createCommand;
	
	protected NetherManager nether = new NetherManager();
	protected static final Logger log = Logger.getLogger("Minecraft");
	protected Persistence persistence = null;
	protected Messaging messaging = null;
	
	protected Message creationFailedMessage;
	protected Message creationSuccessMessage;
	protected Message netherExistsMessage;
}
