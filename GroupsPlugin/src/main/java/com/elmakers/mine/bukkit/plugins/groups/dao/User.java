package com.elmakers.mine.bukkit.plugins.groups.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.groups.dao.Group;
import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistField;
import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistClass;
import com.elmakers.mine.bukkit.plugins.persistence.dao.IProfile;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PlayerData;
import com.nijikokun.bukkit.Permissions.Permissions;

/**
 * Encapsulate a User that can be part of a Group
 * 
 * @author NathanWolf
 *
 */
@PersistClass(name = "user", schema = "groups") 
public class User implements IProfile
{
	/**
	 * The default constructor, used by Persistence to create new instances.
	 */
	public User()
	{
	}
	
	/**
	 * Create a new instance based on an existing player.
	 * 
	 * @param player the player this user will wrap
	 */
	public User(PlayerData player)
	{
		this.id = player;
	}

	public void removeFromGroup(Group group)
	{
		if (groupMap == null || groups == null) return;
		
		Group storedGroup = groupMap.get(group.getId());
		if (group != null)
		{
			groups.remove(storedGroup);
			groupMap.remove(storedGroup.getId());
		}
	}
	
	public void addToGroup(Group group)
	{
		if (groupMap == null)
		{
			groupMap = new HashMap<String, Group>();
		}
		if (groups == null)
		{
			groups = new ArrayList<Group>();
		}
		
		if (groupMap.get(group.getId()) == null)
		{
			groupMap.put(group.getId(), group);
			groups.add(group);
		}
	}

	public void grantPermission(ProfileData profile)
	{
		if (grantMap == null)
		{
			grantMap = new HashMap<String, ProfileData>();
		}
		if (grant == null)
		{
			grant = new ArrayList<ProfileData>();
		}
		
		if (grantMap.get(profile.getId()) == null)
		{
			grantMap.put(profile.getId(), profile);
			grant.add(profile);
		}
		
		// Now, make sure to remove from the deny map also
		// This is more for inherited permissions, we don't
		// want to block ourselves here.
		if (denyMap != null)
		{
			ProfileData denyProfile = denyMap.get(profile.getId());
			if (denyProfile != null)
			{
				denyMap.remove(denyProfile.getId());
				if (deny != null)
				{
					deny.remove(denyProfile);
				}
			}
		}
	}
	
	public void denyPermission(ProfileData profile)
	{
		if (denyMap == null)
		{
			denyMap = new HashMap<String, ProfileData>();
		}
		if (deny == null)
		{
			deny = new ArrayList<ProfileData>();
		}
		
		if (denyMap.get(profile.getId()) == null)
		{
			denyMap.put(profile.getId(), profile);
			deny.add(profile);
		}
		
		// Remove from the allow map if present, since we'd block it anyway.
		if (grantMap != null)
		{
			ProfileData allowProfile = grantMap.get(profile.getId());
			if (allowProfile != null)
			{
				grantMap.remove(allowProfile.getId());
				if (deny != null)
				{
					grant.remove(allowProfile);
				}
			}
		}
	}
	
	public boolean isSet(String key)
	{
		// Check for deny first
		if (deny != null)
		{
			for (ProfileData profile : deny)
			{
				if (profile.isSet(key))
				{
					return false;
				}
			}
		}
			
		// Check grant
		if (grant != null)
		{
			for (ProfileData profile : grant)
			{
				if (profile.isSet(key))
				{
					return true;
				}
			}
		}
		
		// Check groups
		if (groups != null)
		{
			for (Group group : groups)
			{
				if (group.isSet(key))
				{
					return true;
				}
			}		
		}
		
		// Permissions backwards compatibility
		if (permissions != null)
		{
			Player player = id.getPlayer();
			if (player == null) return false;
			
			return Permissions.Security.has(player, key);
		}
		
		return false;
	}
	
	@PersistField(id=true)
	public PlayerData getId()
	{
		return id;
	}
	
	public void setId(PlayerData id)
	{
		this.id = id;
	}
	
	@PersistField
	public List<Group> getGroups()
	{
		return groups;
	}

	public void setGroups(List<Group> groups)
	{
		this.groups = groups;
		
		groupMap = new HashMap<String, Group>();
		for (Group group : groups)
		{
			groupMap.put(group.getId(), group);
		}
	}

	@PersistField
	public List<ProfileData> getGrant()
	{
		return grant;
	}

	public void setGrant(List<ProfileData> grant)
	{
		this.grant = grant;
		
		grantMap = new HashMap<String, ProfileData>();
		for (ProfileData profile : grant)
		{
			grantMap.put(profile.getId(), profile);
		}
	}

	@PersistField
	public List<ProfileData> getDeny()
	{
		return deny;
	}

	public void setDeny(List<ProfileData> deny)
	{
		this.deny = deny;
		
		denyMap = new HashMap<String, ProfileData>();
		for (ProfileData profile : deny)
		{
			denyMap.put(profile.getId(), profile);
		}
	}

	private PlayerData			id;
	private List<Group>	groups;
	private List<ProfileData>	grant;
	private List<ProfileData>	deny;
	
	// Transient
	private HashMap<String, Group> groupMap;
	private HashMap<String, ProfileData> grantMap;
	private HashMap<String, ProfileData> denyMap;
	
	
	// Permissions backwards-compatibility
	public static void setPermissions(Permissions permissions)
	{
		User.permissions = permissions;
	}
	
	protected static Permissions permissions = null;
}
