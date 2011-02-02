package com.elmakers.mine.bukkit.plugins.persistence.stores;

import java.util.logging.Logger;

import com.elmakers.mine.bukkit.plugins.persistence.Persistence;
import com.elmakers.mine.bukkit.plugins.persistence.PersistencePlugin;
import com.elmakers.mine.bukkit.plugins.persistence.core.PersistedClass;

public abstract class PersistenceStore
{
	public abstract boolean connect(String schema);
	public abstract void disconnect();
	
	public abstract void validateTables(PersistedClass persisted);
	
	public abstract void reset(PersistedClass persisted);
	
	public abstract boolean loadAll(PersistedClass persisted);
	public abstract boolean save(PersistedClass persisted, Object o);
	
	public void initialize(Persistence p)
	{
		persistence = p;
	}
	
	protected Persistence persistence = null;
	protected Logger log = PersistencePlugin.getLogger();
}
