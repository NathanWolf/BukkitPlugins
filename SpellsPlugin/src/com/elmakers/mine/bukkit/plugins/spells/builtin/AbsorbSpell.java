package com.elmakers.mine.bukkit.plugins.spells.builtin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;

public class AbsorbSpell extends Spell 
{
	@Override
	public boolean onCast(String[] parameters) 
	{
		if (!isUnderwater())
		{
			noTargetThrough(Material.STATIONARY_WATER);
			noTargetThrough(Material.WATER);
		}
		Block target = getTargetBlock();
		
		if (target == null) 
		{
			castMessage(player, "No target");
			return false;
		}
		int amount = 1;
		
		castMessage(player, "Absorbing some " + target.getType().name().toLowerCase());
		ItemStack itemStack = new ItemStack(target.getType(), amount, (short)0 , target.getData());
		boolean active = false;
		for (int i = 8; i >= 0; i--)
		{
			ItemStack current = player.getInventory().getItem(i);
			if (current == null || current.getType() == Material.AIR)
			{
				player.getInventory().setItem(i, itemStack);
				active = true;
				break;
			}
		}
		
		if (!active)
		{
			player.getInventory().addItem(itemStack);
		}
		
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
		return "construction";
	}
	
	@Override
	public void onLoad(PluginProperties properties)
	{
		//defaultAmount = properties.getInteger("spells-absorb-amount", defaultAmount);
	}

	@Override
	public Material getMaterial()
	{
		return Material.BUCKET;
	}
}
