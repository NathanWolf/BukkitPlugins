package com.elmakers.mine.bukkit.plugins.persistence.stores;

import java.util.logging.Logger;

import com.elmakers.mine.bukkit.plugins.persistence.PersistedClass;
import com.elmakers.mine.bukkit.plugins.persistence.Persistence;
import com.elmakers.mine.bukkit.plugins.persistence.PersistencePlugin;

public abstract class PersistenceStore
{
	public abstract boolean connect(String schema);
	public abstract void disconnect();
	
	public abstract void validateTable(PersistedClass persisted);
	
	public abstract boolean loadAll(PersistedClass persisted);
	public abstract boolean saveAll(PersistedClass persisted);
	
	public void initialize(Persistence p)
	{
		persistence = p;
	}
	
	protected Persistence persistence = null;
	protected Logger log = PersistencePlugin.getLogger();
}
