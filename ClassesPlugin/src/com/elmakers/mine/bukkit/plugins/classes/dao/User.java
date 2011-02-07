package com.elmakers.mine.bukkit.plugins.classes.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistField;
import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistClass;

@PersistClass(name = "player", schema = "classes") 
public class User
{
	public User()
	{
	}
	
	public User(Player loggedIn)
	{
		id = loggedIn.getName();
		lastDisconnect = null;
		
		// This will eventually be a setting that ops can toggle on and off
		// It is on by default for ops, but will not turn back on automatically
		// if disabled. This allows ops to play "mostly" as a normal user.
		superUser = loggedIn.isOp();
		
		update(loggedIn);
	}
	
	public void update(Player player)
	{		
		Date now = new Date();
		
		lastLogin = now;
		firstLogin = now;
		name = player.getDisplayName();
		online = player.isOnline();
	}
	
	public void disconnect(Player player)
	{
		online = false;
		lastDisconnect = new Date();
	}
	
	public void addToGroup(UserGroup group)
	{
		groups.add(group);
	}
	
	public void removeFromGroup(UserGroup group)
	{
		groups.remove(group);
	}
	
	@PersistField(id=true)
	public String getId()
	{
		return id;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	@PersistField
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	@PersistField
	public void setSuperUser(boolean su)
	{
		this.superUser = su;
	}
	
	public boolean isSuperUser()
	{
		return superUser;
	}
		
	@PersistField
	public Date getFirstLogin()
	{
		return firstLogin;
	}

	public void setFirstLogin(Date firstLogin)
	{
		this.firstLogin = firstLogin;
	}

	@PersistField
	public Date getLastLogin()
	{
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin)
	{
		this.lastLogin = lastLogin;
	}

	@PersistField
	public Date getLastDisconnect()
	{
		return lastDisconnect;
	}

	public void setLastDisconnect(Date lastDisconnect)
	{
		this.lastDisconnect = lastDisconnect;
	}

	@PersistField
	public boolean isOnline()
	{
		return online;
	}

	public void setOnline(boolean online)
	{
		this.online = online;
	}
	
	@PersistField
	public void setGroups(List<UserGroup> groups)
	{
		this.groups = groups;
	}

	public List<UserGroup> getGroups()
	{
		return groups;
	}

	private String 		name;
	private String 		id;
	private boolean 	superUser;
	private Date		firstLogin;
	private Date		lastLogin;
	private	Date		lastDisconnect;
	private boolean		online;
	private List<UserGroup> groups = new ArrayList<UserGroup>();

}
