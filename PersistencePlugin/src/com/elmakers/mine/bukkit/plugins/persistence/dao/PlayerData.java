package com.elmakers.mine.bukkit.plugins.persistence.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.persistence.annotation.Persist;
import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistClass;

/**
 * Encapsulate a player in a persitable class.
 * 
 * You can use this class in your own data objects to reference a player, instead of using playerName.
 * 
 * The player name is used an id, so it is still what will ultimately get persisted to your data table.
 * 
 * @author NathanWolf
 *
 */
@PersistClass(name = "player", schema = "global") 
public class PlayerData
{
	/**
	 * The default constructor, used by Persistence to create new instances.
	 */
	public PlayerData()
	{
	}
	
	/**
	 * Create a new instance based on a logged in Player.
	 * 
	 * Sets the first login time to now, and sets the id from the player name.
	 * 
	 * If the player is an Op, "superUser" is set to true by default, though they will have the
	 * ability to turn this on and off.
	 * 
	 * @param loggedIn the player this data will represent
	 */
	public PlayerData(Player loggedIn)
	{
		id = loggedIn.getName();
		firstLogin = new Date();
		lastDisconnect = null;
		
		// This will eventually be a setting that ops can toggle on and off
		// It is on by default for ops, but will not turn back on automatically
		// if disabled. This allows ops to play "mostly" as a normal user.
		superUser = loggedIn.isOp();
		
		update(loggedIn);
	}
	
	/**
	 * Update data based on a logged-in player.
	 * 
	 * Will update online status, display name, and last login time.
	 * 
	 * @param player The player to use when updating this data.
	 */
	public void update(Player player)
	{		
		lastLogin = new Date();
		name = player.getDisplayName();
		online = player.isOnline();
	}
	
	/**
	 * Update this data based on a player disconnecting.
	 * 
	 * Will update online status and last disconnect time.
	 * 
	 * @param player The player that logged out.
	 */
	public void disconnect(Player player)
	{
		online = false;
		lastDisconnect = new Date();
	}
	
	/**
	 * Add this player to the specified Group.
	 * 
	 * @param group The Group to add this player to.
	 */ 
	public void addToGroup(Group group)
	{
		if (groups == null)
		{
			groups = new ArrayList<Group>();
		}
		groups.add(group);
	}
	
	/**
	 * Remove this player from the specified Group.
	 * 
	 * @param group The Group to remove this player from.
	 */
	public void removeFromGroup(Group group)
	{
		if (groups == null) return;
		
		groups.remove(group);
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
	
	@Persist
	public void setGroups(List<Group> groups)
	{
		this.groups = groups;
	}

	public List<Group> getGroups()
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
	private List<Group> groups;

}
