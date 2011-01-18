package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utilities.PluginProperties;

public class AbsorbSpell extends Spell 
{
	int defaultAmount = 16;

	@Override
	public boolean onCast(String[] parameters) 
	{
		Block target = getTargetBlock();
		
		if (target == null) 
		{
			player.sendMessage("No target");
			return false;
		}
		int amount = defaultAmount;
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

	@Override
	public String getCategory() 
	{
		return "build";
	}
	
	@Override
	public void load(PluginProperties properties)
	{
		defaultAmount = properties.getInteger("spells-absorb-amount", defaultAmount);
	}
}
