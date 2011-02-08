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
		
		String[] pieces = line.split(":");
		if (pieces.length < 2) return false;
		
		playerName = pieces[0];
		if (playerName.length() < 1) return false;
		
		playerName = playerName.toLowerCase();
		
		String[] groupString = pieces[1].split(",");
		
		for (int i = 0; i < groupString.length; i++)
		{
			String groupName = groupString[i];
			Group group = permissions.getGroup(groupName);
			addToGroup(group);
		}
		
		return true;
	}
	
	public void addToGroup(Group group)
	{
		if (group == null) return;
		
		if (!groups.contains(group))
		{
			groups.add(group);
		}
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
	
	public void setIsOp()
	{
		administrator = true;
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
