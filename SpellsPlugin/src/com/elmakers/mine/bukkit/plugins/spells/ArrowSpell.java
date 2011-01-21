package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Arrow;

public class ArrowSpell extends Spell
{
	@Override
	public boolean onCast(String[] parameters)
	{
		CraftPlayer cp = (CraftPlayer)player;
		Arrow arrow = cp.shootArrow();
		if (arrow == null)
		{
			castMessage(player, "Your arrow fizzled");
		}
		else
		{
			castMessage(player, "You fire a magical arrow");
		}
		return arrow != null;
	}

	@Override
	public String getName()
	{
		return "arrow";
	}

	@Override
	public String getCategory()
	{
		return "combat";
	}

	@Override
	public String getDescription()
	{
		return "Throws a magic arrow";
	}

}
