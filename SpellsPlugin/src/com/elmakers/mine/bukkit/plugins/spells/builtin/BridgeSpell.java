package com.elmakers.mine.bukkit.plugins.spells.builtin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.utilities.BlockList;

public class BridgeSpell extends Spell 
{
	int MAX_SEARCH_DISTANCE = 16;
	
	@Override
	public boolean onCast(String[] parameters) 
	{
		Block playerBlock = getPlayerBlock();
		if (playerBlock == null) 
		{
			// no spot found to bridge
			player.sendMessage("You need to be standing on something");
			return false;
		}
		
		BlockFace direction = getPlayerFacing();
		Block attachBlock = playerBlock;
		Block targetBlock = attachBlock.getFace(direction);
		
		Material material = targetBlock.getType();
		byte data = targetBlock.getData();
		
		ItemStack buildWith = getBuildingMaterial();
		if (buildWith != null)
		{
			material = buildWith.getType();
			MaterialData targetData = buildWith.getData();
			if (targetData != null)
			{
				data = targetData.getData();
			}
		}
		
		int distance = 0;
		while (isTargetable(targetBlock.getType()) && distance <= MAX_SEARCH_DISTANCE)
		{
			distance++;
			attachBlock = targetBlock;
			targetBlock = attachBlock.getFace(direction);
		}
		if (isTargetable(targetBlock.getType()))
		{
			player.sendMessage("Can't bridge any further");
			return false;
		}
		BlockList bridgeBlocks = new BlockList();
		bridgeBlocks.addBlock(targetBlock);
		targetBlock.setType(material);
		targetBlock.setData(data);
		
		castMessage(player, "A bridge extends!");
		spells.addToUndoQueue(player, bridgeBlocks);
		
		//castMessage(player, "Facing " + playerRot + " : " + direction.name() + ", " + distance + " spaces to " + attachBlock.getType().name());
		
		return true;
	}

	@Override
	public String getName() 
	{
		return "bridge";
	}

	@Override
	public String getDescription() 
	{
		return "Extends the ground underneath you";
	}

	@Override
	public String getCategory() 
	{
		return "construction";
	}

	@Override
	public Material getMaterial()
	{
		return Material.GOLD_HOE;
	}
}
