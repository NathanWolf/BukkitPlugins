package com.elmakers.mine.bukkit.plugins.persistence.annotation;

public class FieldInfo
{
	public boolean isId()
	{
		return id;
	}
	public void setId(boolean id)
	{
		this.id = id;
	}
	
	public boolean isAuto()
	{
		return auto;
	}
	
	public void setAuto(boolean auto)
	{
		this.auto = auto;
	}
	
	public boolean isContained()
	{
		return contained;
	}
	
	public void setContained(boolean contained)
	{
		this.contained = contained;
	}
	
	public boolean isReadonly()
	{
		return readonly;
	}
	
	public void setReadonly(boolean readonly)
	{
		this.readonly = readonly;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	private boolean id;
	private boolean auto;
	boolean contained;
	boolean readonly;
	String name;
}
