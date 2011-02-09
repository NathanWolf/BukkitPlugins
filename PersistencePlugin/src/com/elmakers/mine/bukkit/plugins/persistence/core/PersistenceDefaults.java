package com.elmakers.mine.bukkit.plugins.persistence.core;

class PersistenceDefaults
{
	// Defaults
	public final String[] persistCommand = {"persist", "Manage Persistence", "persist <sub-command> <parameters>"};
	public final String[] saveSubCommand = {"save", "Save cached data", "save"};
	public final String[] describeSubCommand = {"describe", "Describe entities and schema", "describe"};
	public final String[] listSubCommand = {"list", "List entities or data", "list <schema>.<entity>"};
	public final String[] reloadSubCommand = {"reload", "Reload an entity", "reload"};
	public final String[] resetSubCommand = {"RESET", "DROP an entity table", "RESET <schema>.<entity>"};
	public final String[] suCommand = {"su", "Enable full permission access", "su"};
	public final String[] helpCommand = {"phelp", "Get help on Persistence plugins", "phelp"};

	public final String[] describeUsage = {"describe <schema>", "describe <schema>.<entity>"};
	public final String[] listUsage = {"list <schema>.<entity>.<id>"};
	
	public final String[] helpUsage = {"phelp <plugin>", "phelp <plugin>.<command>"};
	
	public final String dataSavedMessage = "Data saved.";
	public final String resettingEntityMessage = "RESETTING entity: %s.%s";
	public final String reloadingEntityMessage = "Reloading entity: %s.%s";
	public final String entityNotFoundMessage = "Can't find entity: %s.%s with %s=%s";
	public final String entityDisplayMessage = "Entity %s.%s:";
	public final String entityListMessage = "%s, %s : %i entities:";
	public final String schemaListMessage = "Schemas:";
	public final String schemaDisplayMessage = "Schema %s:";
	public final String unknownSchemaMessage = "Unknown schema: %s";
	public final String unknownEntityMessage = "Unknown entity: %s";
	public final String pluginListMessage = "Use: phelp commands for list of commands\r     phelp <plugin | command> for detailed help";
	public final String pluginNotFoundMessage = "Plugin %s not found";
	public final String suEnabledMessage = "Full access enabled. Use /su again to revert to normal user.";
	public final String suDisabledMessage = "Normal access restored.";
}
