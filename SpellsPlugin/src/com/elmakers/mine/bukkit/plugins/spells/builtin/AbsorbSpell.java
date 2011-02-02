package com.elmakers.mine.bukkit.plugins.spells.builtin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;

public class AbsorbSpell extends Spell 
{
	private int defaultAmount = 1;

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
		int amount = defaultAmount;
		
		castMessage(player, "Absorbing some " + target.getType().name().toLowerCase());
		for (int i = 0; i < amount; i++)
		{
			ItemStack itemStack = new ItemStack(target.getType(), 1, (short)0 , target.getData());
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
		defaultAmount = properties.getInteger("spells-absorb-amount", defaultAmount);
	}

	@Override
	public Material getMaterial()
	{
		return Material.BUCKET;
	}
}
