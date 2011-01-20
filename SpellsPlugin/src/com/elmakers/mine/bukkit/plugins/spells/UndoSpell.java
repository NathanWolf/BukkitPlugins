package com.elmakers.mine.bukkit.plugins.spells;

import com.elmakers.mine.bukkit.plugins.groups.PlayerPermissions;

public class UndoSpell extends Spell
{

	@Override
	public boolean onCast(String[] parameters)
	{
		String undoPlayer = player.getName();
		PlayerPermissions permissions = plugin.getPermissions(player.getName());
		if (parameters.length > 0 && permissions.isAdministrator())
		{
			undoPlayer = parameters[0];
		}
			
		boolean undone = plugin.undo(undoPlayer);
		if (undone)
		{
			castMessage(player, "Time reverses...");
		}
		else
		{
			player.sendMessage("There is nothing to undo");
		}
		return undone;
	}

	@Override
	public String getName()
	{
		return "rewind";
	}

	@Override
	public String getCategory()
	{
		return "construction";
	}

	@Override
	public String getDescription()
	{
		return "Undoes your last action";
	}

}
