package com.elmakers.mine.bukkit.plugins.crowd;

class CrowdControlDefaults
{
	public final String[] crowdCommand = {"crowd", "Manage creature spawning", null};
	public final String[] crowdControlCommand = {"control", "Control creature spawning", "<type> [percent] [replace] [world]"};
	public final String[] crowdReleaseCommand = {"release", "Release a creature from control", "<type> [world]"};
	public final String[] nukeCommand = {"nuke", "Kill all creatures of a specific type", "[type | all] [world]"};
	public final String[] listCommand = {"list", "List rules or creatures, or both", "rules | population]"};
	public final String[] listRulesCommand = {"rules", "List all rules applying to a world", "[type] [world]"};
	public final String[] listPopulationCommand = {"population", "List the current population", "[type] [world]"};

	public final String killedEntitiesMessage = "Nuked %d %ss in %s!";
	public final String killFailedMessage = "Sorry, couldn't kill any &ss in %s!";
	public final String noEntityMessage = "You are currently %s-free in %s.";
	public final String unknownEntityMessage = "Creature type %s is unknown";
	public final String crowdChanceDisableMessage = "Disabled spawning of %s at %d%% in %s";
	public final String crowdChanceReplaceMessage = "Will replace %s with %s at %d%% in %s";
	public final String crowdDisableMessage = "Disabled spawning of %s in %s";
	public final String crowdReplaceMessage = "Will replace %s with %s in %s";
	public final String crowdReleasedMessage = "No longer controlling %ss in %s";
	public final String notControllingMessage = "Not currently controlling %ss in %s";
	public final String noWorldMessage = "World %s is unknown";
	public final String listMobRulesMessage = "Current rules for %s in world %s:";
	public final String listWorldRulesMessage = "Current rules in world %s:";
	public final String listPopulationMessage = "Population in world %s:";
	public final String populationMessage = " %d %ss in %s";
	public final String rulesMessage = " %s -> %s at %d%% in %s";
	public final String listMobPopulationMessage = "%s population in world %s is %d";
}
