package com.elmakers.mine.bukkit.plugins.persistence.stores;

import java.util.logging.Logger;

import com.elmakers.mine.bukkit.plugins.persistence.PersistedClass;
import com.elmakers.mine.bukkit.plugins.persistence.Persistence;

public abstract class PersistenceStore
{
	public abstract boolean connect(String schema);
	public abstract void disconnect();
	
	public abstract boolean load(PersistedClass persisted);
	public abstract boolean save(PersistedClass persisted);
	
	public void initialize(Persistence p)
	{
		persistence = p;
	}
	
	protected Persistence persistence = null;
	protected Logger log = Logger.getLogger("Minecraft");
}
