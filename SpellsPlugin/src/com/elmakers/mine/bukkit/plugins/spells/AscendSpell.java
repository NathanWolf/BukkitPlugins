package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.Location;
import org.bukkit.Material;

public class AscendSpell extends Spell 
{

	@Override
	public boolean onCast(String[] parameters) 
	{
		Location location = findPlaceToStand(player.getLocation(), true);
		if (location != null) 
		{
			castMessage(player, "Going up!");
			player.teleportTo(location);
			return true;
		} 
		else 
		{		
			// no spot found to ascend
			castMessage(player, "Nowhere to go up");
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

	@Override
	public String getCategory() 
	{
		return "movement";
	}

	@Override
	public Material getMaterial()
	{
		return Material.RED_MUSHROOM;
	}
}
