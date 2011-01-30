package com.elmakers.mine.bukkit.plugins.classes.dao;

import java.util.Date;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.persistence.annotations.Persist;
import com.elmakers.mine.bukkit.plugins.persistence.annotations.PersistClass;

@PersistClass(name = "users", schema = "classes") 
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
	
	@Persist(id=true)
	public String getId()
	{
		return id;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	@Persist
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	@Persist
	public void setSuperUser(boolean su)
	{
		this.superUser = su;
	}
	
	public boolean isSuperUser()
	{
		return superUser;
	}
		
	@Persist
	public Date getFirstLogin()
	{
		return firstLogin;
	}

	public void setFirstLogin(Date firstLogin)
	{
		this.firstLogin = firstLogin;
	}

	@Persist
	public Date getLastLogin()
	{
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin)
	{
		this.lastLogin = lastLogin;
	}

	@Persist
	public Date getLastDisconnect()
	{
		return lastDisconnect;
	}

	public void setLastDisconnect(Date lastDisconnect)
	{
		this.lastDisconnect = lastDisconnect;
	}

	@Persist
	public boolean isOnline()
	{
		return online;
	}

	public void setOnline(boolean online)
	{
		this.online = online;
	}
	
	private String 		name;
	private String 		id;
	private boolean 	superUser;
	private Date		firstLogin;
	private Date		lastLogin;
	private	Date		lastDisconnect;
	private boolean		online;

}
