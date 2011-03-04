package com.elmakers.mine.bukkit.persistence.exception;

import com.elmakers.mine.bukkit.persistence.EntityInfo;
import com.elmakers.mine.craftbukkit.persistence.core.PersistedClass;

public class InvalidPersistedClassException extends Exception
{
	public PersistedClass getPersistedClass()
	{
		return persistedClass;
	}

	public EntityInfo getEntityInfo()
	{
		return entityInfo;
	}

	public Class<? extends Object> getPersistedType()
	{
		return persistedType;
	}

	public InvalidPersistedClassException(PersistedClass persistedClass)
	{
		this.persistedClass = persistedClass;
		if (persistedClass != null)
		{
			this.entityInfo = persistedClass.getEntityInfo();
			this.persistedType = persistedClass.getType();
		}
	}
	
	public InvalidPersistedClassException(Class<? extends Object> persistedType)
	{
		this.persistedType = persistedType;
	}
	
	public InvalidPersistedClassException(Class<? extends Object> persistedType, String message)
	{
		super(message);
		this.persistedType = persistedType;
	}
	
	public InvalidPersistedClassException(EntityInfo entityInfo)
	{
		this.entityInfo = entityInfo;
	}
	
	public InvalidPersistedClassException(PersistedClass persistedClass, String message)
	{
		super(message);
		this.persistedClass = persistedClass;
		if (persistedClass != null)
		{
			this.entityInfo = persistedClass.getEntityInfo();
			this.persistedType = persistedClass.getType();
		}
	}
	
	public InvalidPersistedClassException(EntityInfo entityInfo, String message)
	{
		super(message);
		this.entityInfo = entityInfo;
	}

	private PersistedClass			persistedClass		= null;
	private EntityInfo				entityInfo			= null;
	private Class<? extends Object>	persistedType		= null;				

	/**
	 * Need to support Serializable via Exception
	 */
	private static final long	serialVersionUID	= 1L;
}
