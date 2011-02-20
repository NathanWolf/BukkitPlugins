package com.elmakers.mine.bukkit.plugins.crowd;

class CrowdControlDefaults
{
	public final String[] crowdCommand = {"crowd", "Manage creature spawning", null};
	public final String[] crowdControlCommand = {"control", "Control creature spawning", "<type> [percent] [replace]"};
	public final String[] crowdReleaseCommand = {"release", "Release a creature from control", "<type>"};
	public final String[] nukeCommand = {"nuke", "Kill all creatures of a specific type", "[type | all] [world]"};

	public final String killedEntitiesMessage = "Nuked %d %ss!";
	public final String killFailedMessage = "Sorry, couldn't kill any &ss!";
	public final String noEntityMessage = "You are currently %s-free.";
	public final String unknownEntityMessage = "Creature type %s is unknown";
	public final String crowdChanceDisableMessage = "Disabled spawning of %s at %d%%";
	public final String crowdChanceReplaceMessage = "Will replace %s with %s at %d%%";
	public final String crowdDisableMessage = "Disabled spawning of %s";
	public final String crowdReplaceMessage = "Will replace %s with %s";
	public final String crowdReleasedMessage = "No longer controlling %ss";
	public final String notControllingMessage = "Not currently controlling %ss";
	public final String noWorldMessage = "World %s is unknown";
}
