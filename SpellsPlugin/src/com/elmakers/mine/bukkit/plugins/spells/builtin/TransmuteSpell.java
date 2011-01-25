package com.elmakers.mine.bukkit.plugins.spells.builtin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.utilities.BlockList;
import com.elmakers.mine.bukkit.plugins.spells.utilities.UndoableBlock;

public class TransmuteSpell extends Spell
{

	@Override
	public boolean onCast(String[] parameters)
	{	
		BlockList lastAction = plugin.getLastBlockList(player.getName());
		if (lastAction == null)
		{
			sendMessage(player, "Nothing to transmute");
			return false;
		}
		
		ItemStack targetItem = getBuildingMaterial();
		if (targetItem == null)
		{
			sendMessage(player, "Nothing to transmute with");
			return false;
		}
		
		Material material = targetItem.getType();
		byte data = 0;
		MaterialData targetData = targetItem.getData();
		if (targetData != null)
		{
			data = targetData.getData();
		}
		for (UndoableBlock undoBlock : lastAction.getBlocks())
		{
			Block block = undoBlock.getBlock();
			block.setType(material);
			block.setData(data);
		}
		castMessage(player, "You transmute your last structure to " + material.name().toLowerCase());
		
		return true;
	}

	@Override
	public String getName()
	{
		return "transmute";
	}

	@Override
	public String getCategory()
	{
		return "construction";
	}

	@Override
	public String getDescription()
	{
		return "Modify your last construction";
	}

	@Override
	public Material getMaterial()
	{
		return Material.GOLD_INGOT;
	}

}
