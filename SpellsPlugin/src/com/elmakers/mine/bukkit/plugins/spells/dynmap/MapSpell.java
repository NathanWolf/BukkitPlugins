package com.elmakers.mine.bukkit.plugins.spells.dynmap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.dynmap.DynmapPlugin;
import org.dynmap.MapManager;

import com.elmakers.mine.bukkit.plugins.spells.Spell;

public class MapSpell extends Spell
{

	@Override
	public boolean onCast(String[] parameters)
	{
		DynmapPlugin dynmap = plugin.getDynmapPlugin();
		if (dynmap == null) return false;
		MapManager mgr = dynmap.getMapManager();
		if (mgr == null) return false;
		
		Block targetBlock = getTargetBlock();
		if (targetBlock == null)
		{
			mgr.touch(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
			castMessage(player, "You remap the area around yourself");
		}
		else
		{
			mgr.touch(targetBlock.getLocation().getBlockX(), targetBlock.getLocation().getBlockY(), targetBlock.getLocation().getBlockZ());
			castMessage(player, "You remap the area around your target");
		}
		
		return true;
	}

	@Override
	protected String getName()
	{
		return "map";
	}

	@Override
	public String getCategory()
	{
		return "exploration";
	}

	@Override
	public String getDescription()
	{
		return "Map the tile containing the target block";
	}

	@Override
	public Material getMaterial()
	{
		return Material.PAPER;
	}

}
