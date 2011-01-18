package com.elmakers.mine.bukkit.plugins.groups;

import java.util.ArrayList;
import java.util.List;

public class Group 
{
	private final List<String> commands = new ArrayList<String>();
	private String name;
	private String description;
	private boolean administrator = false;
	
	public boolean parse(String line)
	{
		commands.clear();
		administrator = false;
		
		String[] pieces = line.split(",");
		if (pieces.length < 1) return false;
		
		setName(pieces[0]);
		if (name.length() < 1) return false;
		
		for (int i = 1; i < pieces.length; i++)
		{
			String command = pieces[i];
			if (command.length() > 0)
			{
				commands.add(command);
			}
		}
		
		return true;
	}
	
	public List<String> getCommands()
	{
		return commands;
	}
	
	public String getName() 
	{
		return name;
	}
	
	public void setName(String name) 
	{
		this.name = name;

		administrator =  (name.equalsIgnoreCase("admin") || name.equalsIgnoreCase("admins"));
	}
	
	public String getDescription() 
	{
		return description;
	}
	
	public void setDescription(String description) 
	{
		this.description = description;
	}
	
	public boolean isAdministrator()
	{
		return administrator;
	}

}
