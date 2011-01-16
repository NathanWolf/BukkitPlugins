package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.Player;

public class HealSpell extends Spell 
{

	@Override
	public boolean cast(SpellsPlugin plugin, Player player) 
	{
		player.sendMessage("You heal yourself");
		player.setHealth(20);
		return true;
	}

	@Override
	public String getName() 
	{
		return "heal";
	}

	@Override
	public String getDescription() 
	{
		return "Heal yourself";
	}

}
