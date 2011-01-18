package com.elmakers.mine.bukkit.plugins.groups;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Permissions 
{
	private final HashMap<String, PlayerPermissions> players = new HashMap<String, PlayerPermissions>();
	private final Logger log = Logger.getLogger("Minecraft");
	private final HashMap<String, Group> groups = new HashMap<String, Group>();
	
	public void load(String permissionsFile)
	{
		if (!new File(permissionsFile).exists())
		{
			writeDefault(permissionsFile);
			return;
		}
		
		try 
		{
			log.info("Loading " + permissionsFile);
			Scanner scanner = new Scanner(new File(permissionsFile));
			while (scanner.hasNextLine()) 
			{
				String line = scanner.nextLine();
				if (line.startsWith("#") || line.equals(""))
					continue;
				
				String[] pieces = line.split("=");
				if (pieces.length < 2)
					continue;
				
				if (pieces[0].equalsIgnoreCase("player") || pieces[0].equalsIgnoreCase("user"))
				{
					PlayerPermissions player = new PlayerPermissions();
					if (player.parse(pieces[1], this))
					{
						players.put(player.getPlayerName(), player);
					}
				}
				
				if (pieces[0].equalsIgnoreCase("group") || pieces[0].equalsIgnoreCase("class"))
				{
					Group group = new Group();
					if (group.parse(pieces[1]))
					{
						groups.put(group.getName(), group);
					}
				}
			}
			scanner.close();
		} 
		catch (Exception e) 
		{
			log.log(Level.SEVERE, "Exception while reading " + permissionsFile, e);
		}
	}
	
	protected void writeDefault(String fileName)
	{
		log.info("Creating default file: " + fileName);
		BufferedWriter writer = null;
		try 
		{
			log.info("Saving " + fileName);
			writer = new BufferedWriter(new FileWriter(fileName));
			writer.write("# " + fileName);
			writer.newLine();
			writer.write("# Format (groups before users!): ");
			writer.newLine();
			writer.write("# group=groupName:command1,command2,command3");
			writer.newLine();
			writer.write("# user=userName:group1,group2,group3");
			writer.newLine();	
			writer.write("# 'admins' is a special group that has access to any command - put yourself in it!");
			writer.newLine();	
			writer.write("# Note that player/class can be used in place of user/group.");
			writer.newLine();	
			writer.write("player=YOURNAMEHERE:admins");
			writer.newLine();
		} 
		catch (Exception e) 
		{
			log.log(Level.SEVERE, "Exception while creating " + fileName, e);
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
				log.log(Level.SEVERE, "Exception while closing " + fileName, e);
			}
		}
	}
	
	public Group getGroup(String groupName)
	{
		Group group = groups.get(groupName);
		if (group == null)
		{
			group = new Group();
			group.setName(groupName);
		}
		return group;
	}
	
	public PlayerPermissions getPlayerPermissions(String playerName)
	{
		return players.get(playerName);
	}
}
