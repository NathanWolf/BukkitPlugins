package com.elmakers.mine.bukkit.plugins.wand;

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

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.utilities.PluginProperties;

public class WandPlugin extends JavaPlugin 
{
	private String propertiesFile = "wand.properties";

	private int wandTypeId = 280;
	private String commandFile = "wand-commands.txt";	
	
	private final Logger log = Logger.getLogger("Minecraft");
	private final HashMap<String, WandPermissions> permissions = new HashMap<String, WandPermissions>();
	private final WandPlayerListener playerListener = new WandPlayerListener();
	private final HashMap<String, PlayerWandList> playerWands = new HashMap<String, PlayerWandList>();
	
	private boolean allCanUse = true;
	private boolean allCanAdminister = true;
	private boolean allCanModify = true;
	
	private PlayerWandList defaultWands = null;
	
	public WandPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File dataFolder, File plugin, ClassLoader cLoader) 
	{
		super(pluginLoader, instance, desc, dataFolder, plugin, cLoader);
	}
	 
	@Override
	public void onEnable() 
	{
		load();
		
		playerListener.setPlugin(this);
		
        PluginManager pm = getServer().getPluginManager();
		
        pm.registerEvent(Type.PLAYER_ANIMATION, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_ITEM, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
        
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
	}

	@Override
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
		
		allCanUse = true;
		allCanAdminister = true;
		allCanModify = true;
		
		for (String user : wandUsers)
		{
			allCanUse = false;
			WandPermissions player = getPermissions(user);
			player.setCanUse(true);
		}
		
		for (String mod : wandMods)
		{
			allCanModify = false;
			WandPermissions player = getPermissions(mod);
			player.setCanModify(true);
		}
		
		for (String admin : wandAdmins)
		{
			allCanAdminister = false;
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
		String[] userSplit = userList.split("");
		
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
}
