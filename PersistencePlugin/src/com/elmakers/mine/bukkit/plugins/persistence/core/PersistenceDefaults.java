package com.elmakers.mine.bukkit.plugins.persistence.core;

class PersistenceDefaults
{
	// Commands
	public static final String persistCommand = "persist";
	
	// Text
	public static final String helpHeader = "Persistence:";
	
	public static final String subCommands[] = 
	{
		"save", 
		"describe", 
		"describe <schema>", 
		"describe <schema>.<entity>",
		"list <schema>.<entity>",
		"list <schema>.<entity>.<id>",
		"reload <schema>.<entity>",
		"RESET <schema>.<entity>"
	};
	
	public static final String subCommandHelp[] = 
	{
		"Save cached entities",
		"List all schemas",
		"List entities in a schema",
		"Describe an entit",
		"List all entity ids",
		"List an entity",
		"Reload entities",
		"DROP entity table"
	};
	
	public static final String shortHelpMessage = "Use \"%s help\" for help.";
	public static final String resettingEntityMessage = "RESETTING entity: %s.%s";
}
