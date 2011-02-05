package com.elmakers.mine.bukkit.plugins.persistence.core;

class PersistenceDefaults
{
	// TODO
	public static final String helpHeader = "Persistence:";
	
	// Defaults
	public final String[] persistCommand = {"persist", "Manage Persistence", "persist <sub-command> <parameters>"};
	public final String[] saveSubCommand = {"save", "Save cached data", "save"};
	public final String[] describeSubCommand = {"describe", "Describe entities and schema", "describe"};
	public final String[] listSubCommand = {"list", "List entities or data", "list <schema>.<entity>"};
	public final String[] reloadSubCommand = {"reload", "Reload an entity", "reload"};
	public final String[] resetSubCommand = {"RESET", "DROP an entity table", "RESET <schema>.<entity>"};
	public final String[] helpCommand = {"phelp", "Get help on Persistence plugins", "phelp"};

	public final String[] describeUsage = {"describe <schema>", "describe <schema>.<entity>"};
	public final String[] listUsage = {"list <schema>.<entity>.<id>"};
	
	public final String[] helpUsage = {"phelp <plugin>", "phelp <plugin>.<command>"};
	
	public final String resettingEntityMessage = "RESETTING entity: %s.%s";
}
