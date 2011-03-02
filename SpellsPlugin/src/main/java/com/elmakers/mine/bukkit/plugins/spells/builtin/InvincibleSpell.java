package com.elmakers.mine.bukkit.plugins.spells.builtin;

import org.bukkit.Material;

import com.elmakers.mine.bukkit.plugins.spells.Spell;

public class InvincibleSpell extends Spell 
{
	@Override
	public boolean onCast(String[] parameters) 
	{
		boolean invincible = !spells.isInvincible(player);
		spells.setInvincible(player, invincible);
		if (invincible)
		{
			castMessage(player, "You feel invincible!");
		}
		else
		{
			castMessage(player, "You feel ... normal.");
		}
		return true;
	}

	@Override
	public String getName() 
	{
		return "invincible";
	}

	@Override
	public String getCategory() 
	{
		return "help";
	}

	@Override
	public String getDescription() 
	{
		return "Makes you impervious to damage";
	}

	@Override
	public Material getMaterial()
	{
		return Material.GOLDEN_APPLE;
	}

}
