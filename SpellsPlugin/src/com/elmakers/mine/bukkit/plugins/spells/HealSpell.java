package com.elmakers.mine.bukkit.plugins.spells;

public class HealSpell extends Spell 
{
	@Override
	public boolean onCast(String[] parameters) 
	{
		plugin.castMessage(player, "You heal yourself");
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

	@Override
	public String getCategory() 
	{
		return "help";
	}
}
