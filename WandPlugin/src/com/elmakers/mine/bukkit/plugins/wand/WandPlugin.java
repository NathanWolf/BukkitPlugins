package com.elmakers.mine.bukkit.plugins.wand;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class WandPlugin extends JavaPlugin 
{
	private String commandFile = "wand-commands.txt";	
	private static final Logger log = Logger.getLogger("Minecraft");
	static final WandPlayerListener playerListener = new WandPlayerListener();
	static final HashMap<String, PlayerWandList> playerWands = new HashMap<String, PlayerWandList>();
	
	@Override
	public void onInitialize()
	{
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
	
	public PlayerWandList getPlayerWands(Player player)
	{
		return getPlayerWands(player.getName());
	}
	
	public PlayerWandList getPlayerWands(String playerName)
	{
		PlayerWandList wands = playerWands.get(playerName);
		
		if (wands == null)
		{
			wands = new PlayerWandList();
			playerWands.put(playerName, wands);
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
				
				String[] splitWands = line.split(";");
				
				if (splitWands.length < 1) 
				{
					log.log(Level.SEVERE, "Malformed line (" + line + ") in " + commandFile);
					continue;
				}

				String playerInfo = splitWands[0];
				String[] playerSplit = playerInfo.split(":");
				if (playerSplit.length < 1) 
				{
					log.log(Level.SEVERE, "Malformed line (" + line + ") in " + commandFile);
					continue;
				}
				
				String playerName = playerSplit[0];
				String selectedWand = "";
				if (playerSplit.length > 1)
				{
					selectedWand = playerSplit[1];
				}
				PlayerWandList wands = getPlayerWands(playerName);
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
			}
			scanner.close();
		} 
		catch (Exception e) 
		{
			log.log(Level.SEVERE, "Exception while reading " + commandFile, e);
		}
	}
	
}
