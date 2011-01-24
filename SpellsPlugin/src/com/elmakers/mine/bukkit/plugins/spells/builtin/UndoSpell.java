package com.elmakers.mine.bukkit.plugins.spells.builtin;

import org.bukkit.Material;

import com.elmakers.mine.bukkit.plugins.groups.PlayerPermissions;
import com.elmakers.mine.bukkit.plugins.spells.Spell;

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
			castMessage(player, "There is nothing to undo");
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

	@Override
	public Material getMaterial()
	{
		return Material.WATCH;
	}

}
