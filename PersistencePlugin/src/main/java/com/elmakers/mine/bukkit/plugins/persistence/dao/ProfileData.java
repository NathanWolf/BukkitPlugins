package com.elmakers.mine.bukkit.plugins.persistence.dao;

import org.bukkit.permission.PermissionProfile;

import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistClass;
import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistField;

@PersistClass(schema="global", name="profile")
public class ProfileData 
{
	public ProfileData()
	{
		
	}
	
	public ProfileData(String id)
	{
		this.id = id;
	}

	public void setProfile(PermissionProfile profile)
	{
		this.profile = profile;
	}

	public <T> T get(final String key)
	{
		if (this.profile == null)
		{
			return null;
		}

		return this.profile.get(key);
	}

	public boolean isSet(final String key)
	{
		if (this.profile == null)
		{
			return false;
		}

		return this.isSet(key);
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

	String id;
	
	// transient
	PermissionProfile profile;
}
