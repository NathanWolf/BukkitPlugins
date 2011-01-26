package com.elmakers.mine.bukkit.plugins.spells.builtin;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.plugins.spells.Spell;

public class UndoSpell extends Spell
{
	public UndoSpell()
	{
		addVariant("revert", Material.REDSTONE, getCategory(), "Deconstruct your target", "target");
	}
	
	@Override
	public boolean onCast(String[] parameters)
	{
		String undoPlayer = player.getName();
		
		for (int i = 0; i < parameters.length; i++)
		{
			if (parameters[i].equalsIgnoreCase("player") && i < parameters.length - 1)
			{
				undoPlayer = parameters[++i];
			}
			else if (parameters[i].equalsIgnoreCase("target"))
			{
				targetThrough(Material.GLASS);
				Block target = getTargetBlock();
				if (target == null)
				{
					castMessage(player, "No target");
					return false;
				}
				
				boolean undone = plugin.undo(undoPlayer, target);
				if (undone)
				{
					castMessage(player, "You revert your construction");
				}
				else
				{
					castMessage(player, "That is not your construction");
				}
				return undone;
			}
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
