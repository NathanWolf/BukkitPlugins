package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.Location;

public class DescendSpell extends Spell 
{
	
	@Override
	public boolean onCast(String[] parameters) 
	{
		Location location = findPlaceToStand(player, false);
		if (location != null) 
		{
			plugin.castMessage(player, "Going down!");
			player.teleportTo(location);
			return true;
		} 
		else 
		{		
			// no spot found to ascend
			plugin.castMessage(player, "Nowhere to go down");
			return false;
		}
	}

	@Override
	public String getName() 
	{
		return "descend";
	}

	@Override
	public String getDescription() 
	{
		return "Go down to the nearest safe spot";
	}

	@Override
	public String getCategory() 
	{
		return "movement";
	}
}
