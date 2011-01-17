package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.block.Block;
import org.bukkit.Material;

public class TorchSpell extends Spell 
{

	@Override
	public boolean onCast(String[] parameters) 
	{
		Block face = getLastBlock();
		if (face == null || face.getType() != Material.AIR)
		{
			player.sendMessage("Can't put a torch there");
			return false;
		}
		
		player.sendMessage("Flame on!");
		
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
		return "build";
	}
}
