package com.sample.bukkit.plugins.sample;

class SampleDefaults
{
	public final String[] setCommand = {"set", "Base set command - use 'set nickname'", null};
	public final String[] setNickNameCommand = {"nickname", "Set a player's nickname", "<player> <nickname>"};
	public final String[] showCommand = {"show", "Base show command - use 'show nickname'", null};
	public final String[] showNickNameCommand = {"nickname", "Display a player's nickname", "<player>"};

	public final String changedNicknameMessage = "Set nickname for %s to %s";
	public final String showNicknameMessage = "%s's nickname is %s";
	public final String unknownPlayerMessage = "Player %s is unknown";
	public final String noNicknameMessage = "Player %s has no nickname";
	
}
