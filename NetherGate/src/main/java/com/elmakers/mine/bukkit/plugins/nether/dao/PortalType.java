package com.elmakers.mine.bukkit.plugins.nether.dao;

public enum PortalType
{
	NONE,
	PLATFORM,
	PORTAL,
	PORTAL_AND_PLATFORM,
	PORTAL_AND_FRAME,
	PORTAL_FRAME_AND_PLATFORM;
	
	public boolean hasFrame()
	{
		return (this == PORTAL_AND_FRAME || this == PORTAL_FRAME_AND_PLATFORM);
	}
	
	public boolean hasPlatform()
	{
		return (this == PLATFORM || this == PORTAL_AND_PLATFORM|| this == PORTAL_FRAME_AND_PLATFORM);
	}
	
	public boolean hasPortal()
	{
		return (this != PortalType.NONE && this != PortalType.PLATFORM);
	}
	
	public boolean isTracked()
	{
		return this == PORTAL_FRAME_AND_PLATFORM;
	}
	
	public static PortalType getPortalType(boolean buildPortal, boolean buildFrame, boolean buildPlatform)
	{
		if (buildPortal)
		{
			if (buildFrame)
			{
				if (buildPlatform)
				{
					return PORTAL_FRAME_AND_PLATFORM;
				}
				else
				{
					return PORTAL_AND_FRAME;
				}
			}
			else
			{
				if (buildPlatform)
				{
					return PORTAL_AND_PLATFORM;
				}
				else
				{
					return PORTAL;
				}
			}
		}
		else if (buildPlatform)
		{
			return PLATFORM;
		}
		
		return NONE;
	}
}
