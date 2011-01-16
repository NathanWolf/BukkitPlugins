package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.Location;

public class AscendSpell extends Spell 
{

	@Override
	public boolean onCast(String[] parameters) 
	{
		Location location = findPlaceToStand(player, true);
		if (location != null) 
		{
			player.sendMessage("Going up!");
			player.teleportTo(location);
			return true;
		} 
		else 
		{		
			// no spot found to ascend
			player.sendMessage("Nowhere to go up");
			return false;
		}
	}

	@Override
	public String getName() 
	{
		return "ascend";
	}

	@Override
	public String getDescription() 
	{
		return "Go up to the nearest safe spot";
	}

}
