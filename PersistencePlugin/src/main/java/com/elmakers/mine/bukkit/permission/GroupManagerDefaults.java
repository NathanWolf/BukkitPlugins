package com.elmakers.mine.bukkit.permission;

class GroupManagerDefaults
{
	public final String[] groupCommand = {"group", "Manage player groups", null};
	public final String[] groupCreateCommand = {"create", "Create a player group", "<groupname>"};
	public final String[] groupAddCommand = {"add", "Add a player to a group", "<player> <group>"};
	public final String[] groupRemoveCommand = {"remove", "Remove a player from a group", "<player> <group>"};
	public final String[] denyCommand = {"deny", "Deny players and groups access", null};
	public final String[] denyPlayerCommand = {"player", "Deny a player's access", "<player> <permission>"};
	public final String[] denyGroupCommand = {"group", "Deny a group's accesse", "<group> <permission>"};
	public final String[] grantCommand = {"grant", "Grant players and groups access", null};
	public final String[] grantPlayerCommand = {"player", "Grant a player access", "<player> <permission>"};
	public final String[] grantGroupCommand = {"group", "Grant a group access", "<group> <permission>"};

	public final String addedPlayerToGroupMessage = "Added %s to %s";
	public final String removedPlayerFromGroupMessage = "Removed %s from %s";
	public final String createdGroupMessage = "Created group %s";
	public final String denyAccessMessage = "Access to %s denied to %s %s";
	public final String grantAccessMessage = "Access to %s granted to %s %s";
	public final String playerNotFoundMessage = "Player %s is unknown";
	public final String groupNotFoundMessage = "Group %s is unknown";
	public final String unknownProfileMessage = "Permission profile %s is unknown";
	public final String groupExistsMessage = "Group %s already exists";
}
