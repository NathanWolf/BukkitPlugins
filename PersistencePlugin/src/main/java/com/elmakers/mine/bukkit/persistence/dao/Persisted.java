package com.elmakers.mine.bukkit.persistence.dao;

import com.elmakers.mine.craftbukkit.persistence.core.PersistedClass;


/**
 * This is an (optional) common base class for all persisted classes.
 * 
 * It implements Cloneable for you, and also provides easy access to
 * the PersistedClass that manages its instances.
 * 
 * @author NathanWolf
 */
public class Persisted implements Cloneable
{

    /**
     * Generate a hash id based on the hash id of this 
     * object's concrete (data) id.
     *
     * @return an auto-generated hash code
     */
    @Override
    public int hashCode() 
    {
        if (persistedClass == null) return 0;
        Object id = persistedClass.getIdData(this);
        if (id == null) return 0;
        
        return id.hashCode();
    }

    /**
     * Automatically generate a copy of this instance based on its
     * persistend data.
     *
     * @return a copy of this persisted object
     */
    @Override
    public Persisted clone() 
    {
    	// TODO!
        return null;
    }

	public void setPersistedClass(PersistedClass owningClass)
	{
		this.persistedClass = owningClass;
	}

	protected PersistedClass	persistedClass = null;
}
