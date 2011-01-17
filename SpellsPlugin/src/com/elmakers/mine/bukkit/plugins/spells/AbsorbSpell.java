package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class AbsorbSpell extends Spell 
{
	int DEFAULT_AMOUNT = 16;

	@Override
	public boolean onCast(String[] parameters) 
	{
		Block target = getTargetBlock();
		
		if (target == null) 
		{
			player.sendMessage("No target");
			return false;
		}
		int amount = DEFAULT_AMOUNT;
		player.sendMessage("Absorbing some " + target.getType().name().toLowerCase());
		player.getWorld().dropItem(player.getLocation(), new ItemStack(target.getType(), amount));
		return true;
	}

	@Override
	public String getName() 
	{
		return "absorb";
	}

	@Override
	public String getDescription() 
	{
		return "Give yourself some of your target";
	}

}
