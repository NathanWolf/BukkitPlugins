package com.elmakers.mine.bukkit.plugins.groups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerPermissions 
{
	private final List<Group> groups = new ArrayList<Group>();
	private final HashMap<String, Boolean> commandPermissions = new HashMap<String, Boolean>();
	private String playerName;
	private boolean administrator = false;
	
	public boolean parse(String line, Permissions permissions)
	{
		administrator = false;
		groups.clear();
		
		String[] pieces = line.split(",");
		if (pieces.length < 1) return false;
		
		playerName = pieces[0];
		if (playerName.length() < 1) return false;
		
		for (int i = 1; i < pieces.length; i++)
		{
			String groupName = pieces[i];
			Group group = permissions.getGroup(groupName);
			if (group != null)
			{
				groups.add(group);
			}
		}
		
		constructPermissions();
		
		return true;
	}
	
	public void constructPermissions()
	{
		commandPermissions.clear();
		
		for (Group group : groups)
		{
			if (group.isAdministrator())
			{
				administrator = true;
			}
			List<String> commands = group.getCommands();
			for (String command : commands)
			{
				commandPermissions.put(command, true);
			}
		}
	}
	
	public boolean hasPermission(String command)
	{
		if (administrator)
		{
			return true;
		}
		
		Boolean permission = commandPermissions.get(command);
		return permission != null && permission == true;
	}
	
	public String getPlayerName()
	{
		return playerName;
	}
	
	public boolean isAdministrator()
	{
		return administrator;
	}
	
}
