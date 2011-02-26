package com.elmakers.mine.bukkit.plugins.spells.builtin;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.gameplay.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;

public class DisintegrateSpell extends Spell
{
	private int				defaultSearchDistance	= 32;

	@Override
	public boolean onCast(String[] parameters)
	{
		Block target = getTargetBlock();
		if (target == null)
		{
			castMessage(player, "No target");
			return false;
		}
		if (defaultSearchDistance > 0 && getDistance(player, target) > defaultSearchDistance)
		{
			castMessage(player, "Can't blast that far away");
			return false;
		}
		
		BlockList disintigrated = new BlockList();
		disintigrated.add(target);
		
		if (isUnderwater())
		{
			target.setType(Material.STATIONARY_WATER);
		}
		else
		{
			target.setType(Material.AIR);
		}
		
		spells.addToUndoQueue(player, disintigrated);
		castMessage(player, "ZAP!");
		
		return true;
	}

	@Override
	protected String getName()
	{
		return "disintegrate";
	}

	@Override
	public String getCategory()
	{
		return "mining";
	}

	@Override
	public String getDescription()
	{
		return "Destroy the target block";
	}

	@Override
	public Material getMaterial()
	{
		return Material.BONE;
	}

	@Override
	public void onLoad(PluginProperties properties)
	{
		defaultSearchDistance = properties.getInteger("spells-disintegrate-search-distance", defaultSearchDistance);
	}
}
