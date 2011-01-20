package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.block.Block;
import org.bukkit.Material;

import com.elmakers.mine.bukkit.utilities.PluginProperties;

public class TorchSpell extends Spell 
{
	private boolean allowDay = true;

	@Override
	public boolean onCast(String[] parameters) 
	{
		if (yRotation > 80 && allowDay)
		{
			plugin.castMessage(player, "FLAME ON!");
			setRelativeTime(0);
			return true;
		}
		
		Block face = getLastBlock();
		if (face == null || face.getType() != Material.AIR)
		{
			player.sendMessage("Can't put a torch there");
			return false;
		}
		
		plugin.castMessage(player, "Flame on!");
		
		setFaceBlock(50);
		return true;
	}

	@Override
	public String getName() 
	{
		return "torch";
	}

	@Override
	public String getDescription() 
	{
		return "Place a torch at your target";
	}

	@Override
	public String getCategory() 
	{
		return "construction";
	}

	@Override
	public void onLoad(PluginProperties properties)
	{
		allowDay = properties.getBoolean("spells-torch-allow-day", allowDay);
	}
}
