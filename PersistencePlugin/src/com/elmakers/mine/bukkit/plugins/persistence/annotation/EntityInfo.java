package com.elmakers.mine.bukkit.plugins.persistence.annotation;


public class EntityInfo
{
	public EntityInfo()
	{
		
	}
	
	public EntityInfo(PersistClass defaults)
	{
		schema = defaults.schema();
		name = defaults.name();
		contained = defaults.contained();
		cache = defaults.cache();
	}
	
	public String getSchema()
	{
		return schema;
	}

	public void setSchema(String schema)
	{
		this.schema = schema;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public boolean getContained()
	{
		return contained;
	}

	public void setContained(boolean contained)
	{
		this.contained = contained;
	}

	public boolean getCache()
	{
		return cache;
	}

	public void setCache(boolean cache)
	{
		this.cache = cache;
	}

	private String schema;
	private String name;
	private boolean contained;
	private boolean cache;
}
