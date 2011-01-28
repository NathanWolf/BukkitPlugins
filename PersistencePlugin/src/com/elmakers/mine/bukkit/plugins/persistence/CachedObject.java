package com.elmakers.mine.bukkit.plugins.persistence;

public class CachedObject
{
	private Object object;
	private boolean cached;
	private long cacheTime;
	
	public CachedObject(Object o)
	{
		object = o;
		cached = true;
		cacheTime = System.currentTimeMillis();
	}
	
	public void setCached(boolean c)
	{
		cached = c;
	}
	
	public Object getObject()
	{
		return object;
	}
	
	public boolean isCached()
	{
		return cached;
	}
	
	public long getCacheTime()
	{
		return cacheTime;
	}
	
}
