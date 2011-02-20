package com.elmakers.mine.bukkit.plugins.wandmin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.plugins.wandmin.utilities.PluginProperties;

public class WandminPlugin extends JavaPlugin 
{
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] parameters)
	{
		// Only works in-game, for now.
		if (!(sender instanceof Player)) return false;
	
		String commandString = command.getName();
    	if (!commandString.equalsIgnoreCase("wandmin"))
    	{
    		return false;
    	}
  	
		Player player = (Player)sender;		
		WandPermissions permissions = getPermissions(player.getName());

		if (!permissions.canUse())
		{
			return true;
		}
	
    	PlayerWandList wands = getPlayerWands(player);
    	
    	if (parameters.length < 1)
    	{
    		showHelp(wands);
    		return true;
    	}

    	String wandCommand = parameters[0];
    	if (wandCommand.equalsIgnoreCase("help"))
    	{
    		showHelp(wands);
    		return true;
    	}
    	
    	if (wandCommand.equalsIgnoreCase("reload") && permissions.canAdminister())
    	{
    		load();
    		player.sendMessage("Wands reloaded");
    		return true;
    	}

    	if (wandCommand.equalsIgnoreCase("next"))
    	{
    		Wand wand = wands.getCurrentWand();
    		if (wand == null)
    		{
    			player.sendMessage("Create a wand first");
    			return true;
    		}
    		wands.nextWand();
    		wand = wands.getCurrentWand();
    		player.sendMessage(" " + wand.getName() + " : " + wand.getCurrentCommand().getName());
    		return true;
    	}

    	if (wandCommand.equalsIgnoreCase("wands"))
    	{
    		player.sendMessage("You have " + wands.getWands().size() + " wands:");
    		for (Wand wand : wands.getWands())
    		{
    			String prefix = " ";
    			if (wand == wands.getCurrentWand())
    			{
    				prefix = "*";
    			}
    			String wandMessage = prefix + wand.getName();
    			String wandDescription = wand.getDescription();
    			if (wandDescription != null && wandDescription.length() > 0)
    			{
    				wandMessage = wandMessage + " : " + wandDescription;
    			}
    			player.sendMessage(wandMessage);
    		}
    		return true;
    	}
    	
    	if (wandCommand.equalsIgnoreCase("spells") || wandCommand.equalsIgnoreCase("commands"))
    	{
    		Wand wand = wands.getCurrentWand();
    		if (wand == null)
    		{
    			player.sendMessage("Create a wand first");
    			return true;
    		}
    		player.sendMessage("You have " + wand.getCommands().size() + " spells on your " + wand.getName() + " wand:");
    		player.sendMessage(wand.getName());
    		for (WandCommand wandCommandPart : wand.getCommands())
    		{
    			String prefix = " ";
    			if (wandCommandPart == wand.getCurrentCommand())
    			{
    				prefix = "*";
    			}
    			String commandMessage = prefix + wandCommandPart.getName();
    			String commandDescription = wandCommandPart.getDescription();
    			if (commandDescription != null && commandDescription.length() > 0)
    			{
    				commandMessage = commandMessage + " : " + commandDescription;
    			}
    			player.sendMessage(commandMessage);
    		}
    		return true;
    	}
    	
    	// All mod stuff from here
    	if (!permissions.canModify())
    	{
    		showHelp(wands);
    		return true;
    	}
    	
    	// One param
    	if (parameters.length < 2)
		{
			showHelp(wands);
			return true;
		}
    	
    	String castCommand = "";
    	for (int i = 1; i < parameters.length; i++) 
    	{
    		castCommand += parameters[i];
			if (i != parameters.length - 1)
			{
				castCommand += " ";
			}
		}
    	
    	if (wandCommand.equalsIgnoreCase("create"))
    	{
    		String wandName = parameters[1];
    		wands.addWand(wandName);
    		player.sendMessage("Added wand '" + wandName + "'");
    		return true;
    	}
    	
    	if (wandCommand.equalsIgnoreCase("destroy"))
    	{
    		String wandName = parameters[1];
    		wands.removeWand(wandName);
    		player.sendMessage("Removed wand '" + wandName + "'");
    		return true;
    	}

    	// Needs a wand
    	Wand wand = wands.getCurrentWand();
    	if (wand == null)
    	{
    		player.sendMessage("Create a wand first");
    		return true;
    	}
    	
    	if (wandCommand.equalsIgnoreCase("bind"))
    	{
    		wand.addCommand(castCommand);
    		player.sendMessage("Bound wand '" + wand.getName() + "' to '" + castCommand + "'");
    		return true;
    	}

    	if (wandCommand.equalsIgnoreCase("unbind"))
    	{
    		wand.removeCommand(castCommand);
    		player.sendMessage("Unbound wand '" + wand.getName() + "' from '" + castCommand + "'");
    		return true;
    	}
		
		return true;
	}
	
	
	private void showHelp(PlayerWandList wands)
	{
		Player player = wands.getPlayer();
		WandPermissions permissions = getPermissions(player.getName());
		// TODO - pull this from plugin description
		player.sendMessage("Usage: /wandmin [command] [parameters]");
		player.sendMessage(" commands : List the bound commands");
		player.sendMessage(" spells : List the bound commands");
		player.sendMessage(" wands : List all of your wands");
		player.sendMessage(" next : Switch to the next wand");

		if (permissions.canModify())
		{
			player.sendMessage(" create [name] : Create a magic wand");
			player.sendMessage(" bind [command] : Bind a command to your wand");
			player.sendMessage(" unbind [command] : Unbind a command from your wand");
			player.sendMessage(" destroy [name] : Destroy one of your wands");
		}
		
		if (permissions.canAdminister())
		{
			player.sendMessage(" reload : Reload the configuration");
		}
		
	}

	public void onEnable() 
	{
		load();
		
		playerListener.setPlugin(this);
		
        PluginManager pm = getServer().getPluginManager();
		
        pm.registerEvent(Type.PLAYER_ANIMATION, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_ITEM, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
        
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
	}

	public void onDisable() 
	{
		save();
	}
	
	public void loadProperties()
	{
		PluginProperties properties = new PluginProperties(propertiesFile);
		properties.load();
		
		// Get and set all properties
		commandFile = properties.getString("wand-commands-file", commandFile);
		wandTypeId = properties.getInteger("wand-type-id", wandTypeId);
		String wandDefault = properties.getString("wand-default", "");
		String wandUsers = properties.getString("wand-users", "");
		String wandMods = properties.getString("wand-mods", "");
		String wandAdmins = properties.getString("wand-admins", "");
		
		parsePermissions(wandUsers, wandMods, wandAdmins);
		defaultWands = parseWands("DEFAULT:" + wandDefault);
		
		properties.save();
	}
	
	protected void parsePermissions(String wandUserString, String wandModString, String wandAdminString)
	{
		permissions.clear();
		
		List<String> wandUsers = parseUserList(wandUserString);
		List<String> wandMods = parseUserList(wandModString);
		List<String> wandAdmins = parseUserList(wandAdminString);
		
		allCanUse = false;
		allCanAdminister = false;
		allCanModify = false;
		
		for (String user : wandUsers)
		{
			WandPermissions player = getPermissions(user);
			player.setCanUse(true);
		}
		
		for (String mod : wandMods)
		{
			WandPermissions player = getPermissions(mod);
			player.setCanModify(true);
		}
		
		for (String admin : wandAdmins)
		{
			WandPermissions player = getPermissions(admin);
			player.setCanAdminister(true);
		}
	}
	
	protected List<String> parseUserList(String userList)
	{	
		List<String> users = new ArrayList<String>();
		if (userList == null || userList.length() == 0)
		{
			return users;
		}
		String[] userSplit = userList.split(",");
		
		for (int i = 0; i < userSplit.length; i++)
		{
			String userName = userSplit[i];
			if (userName == null || userName.length() == 0)
			{
				continue;
			}
			users.add(userName);
		}
		return users;
	}
	
	public PlayerWandList getPlayerWands(Player player)
	{
		PlayerWandList list = getPlayerWands(player.getName());
		if (list != null)
		{
			list.setPlayer(player);
		}
		return list;
	}
	
	
	public WandPermissions getPermissions(String playerName)
	{
		WandPermissions player = permissions.get(playerName);

		if (player == null)
		{
			player = new WandPermissions();
			player.setCanAdminister(allCanAdminister);
			player.setCanModify(allCanModify);
			player.setCanUse(allCanUse);
			permissions.put(playerName, player);
		}

		Player loggedIn = getServer().getPlayer(playerName);
		if (loggedIn != null && loggedIn.isOp())
		{
			player.setCanAdminister(true);
		}

		return player;
	}	
	
	public PlayerWandList getPlayerWands(String playerName)
	{
		PlayerWandList wands = playerWands.get(playerName);
		
		if (wands == null)
		{
			wands = new PlayerWandList();
			playerWands.put(playerName, wands);
		}
		
		if (wands.isEmpty() && defaultWands != null)
		{
			defaultWands.copyTo(wands);
		}
		
		return wands;
	}

    public void save() 
    {
		BufferedWriter writer = null;
		try 
		{
			log.info("Saving " + commandFile);
			writer = new BufferedWriter(new FileWriter(commandFile));
			writer.write("# " + commandFile);
			writer.newLine();
			for (String playerName : playerWands.keySet()) 
			{
				String playerLine = playerName + ":";
				PlayerWandList wands = playerWands.get(playerName);
				if (wands.getCurrentWand() != null)
				{
					playerLine += wands.getCurrentWand().getName() + ";";
				
					for (Wand wand : wands.getWands())
					{
						String wandLine = wand.getName() + ":";
						if (wand.getCurrentCommand() != null)
						{
							wandLine += wand.getCurrentCommand().getName() + ":";
							for (WandCommand command : wand.getCommands())
							{
								wandLine += command.getName() + ":";
							}
						}
						playerLine += wandLine + ";";
					}
				}
				writer.write(playerLine.toString());
				writer.newLine();
			}
		} 
		catch (Exception e) 
		{
			log.log(Level.SEVERE, "Exception while creating " + commandFile, e);
		} 
		finally 
		{
			try 
			{
				if (writer != null) 
				{
					writer.close();
				}
			} 
			catch (IOException e) 
			{
				log.log(Level.SEVERE, "Exception while closing " + commandFile, e);
			}
		}
	}

	public void load() 
	{	
		loadProperties();
		if (!new File(commandFile).exists())
		{
			log.info("File does not exist " + commandFile);
			return;
		}
		playerWands.clear();
		try 
		{
			log.info("Loading " + commandFile);
			Scanner scanner = new Scanner(new File(commandFile));
			while (scanner.hasNextLine()) 
			{
				String line = scanner.nextLine();
				if (line.startsWith("#") || line.equals(""))
					continue;
				
				PlayerWandList parsedWands = parseWands(line);
				if (parsedWands != null)
				{
					PlayerWandList userWands = getPlayerWands(parsedWands.getPlayerName());
					parsedWands.copyTo(userWands);
				}
			}
			scanner.close();
		} 
		catch (Exception e) 
		{
			log.log(Level.SEVERE, "Exception while reading " + commandFile, e);
		}
	}
	
	public PlayerWandList parseWands(String wandString)
	{
		PlayerWandList wands = new PlayerWandList();
		String[] splitWands = wandString.split(";");
		
		if (splitWands.length < 1) 
		{
			return null;
		}

		String playerInfo = splitWands[0];
		String[] playerSplit = playerInfo.split(":");
		if (playerSplit.length < 1) 
		{
			return null;
		}
		
		String playerName = playerSplit[0];
		wands.setPlayerName(playerName);
		
		String selectedWand = "";
		if (playerSplit.length > 1)
		{
			selectedWand = playerSplit[1];
		}
		for (int wandIndex = 1; wandIndex < splitWands.length; wandIndex++)
		{
			if (splitWands[wandIndex].length() <= 0) continue;
			String[] wandSplit = splitWands[wandIndex].split(":");
			if (wandSplit.length < 1) continue;
			Wand wand = wands.addWand(wandSplit[0]);
			if (wandSplit.length < 2) continue;
			String selectedCommand = wandSplit[1];
			for (int commandIndex = 2; commandIndex < wandSplit.length; commandIndex++)
			{
				String command = wandSplit[commandIndex];
				wand.addCommand(command);
			}
			wand.selectCommand(selectedCommand);
		}
		wands.selectWand(selectedWand);
		
		return wands;
	}
	
	public int getWandTypeId()
	{
		return wandTypeId;
	}
	
	public void bindPersistencePlugin() 
	{
		/*
		Plugin checkForPersistence = this.getServer().getPluginManager().getPlugin("Persistence");
	    if(checkForPersistence != null) 
	    {
	    	PersistencePlugin plugin = (PersistencePlugin)checkForPersistence;
	    	persistence = plugin.getPersistence();
	    } 
	    else 
	    {
	    	log.warning("The Wandmin plugin depends on Persistence - please install it!");
	    	this.getServer().getPluginManager().disablePlugin(this);
	    }
	    */
	}
	
	private final String propertiesFile = "wandmin.properties";

	private int wandTypeId = 280;
	private String commandFile = "wand-commands.txt";
	
	private final Logger log = Logger.getLogger("Minecraft");
	private final HashMap<String, WandPermissions> permissions = new HashMap<String, WandPermissions>();
	private final WandminPlayerListener playerListener = new WandminPlayerListener();
	private final HashMap<String, PlayerWandList> playerWands = new HashMap<String, PlayerWandList>();
	
	private boolean allCanUse = true;
	private boolean allCanAdminister = true;
	private boolean allCanModify = true;
	
	private PlayerWandList defaultWands = null;

}
