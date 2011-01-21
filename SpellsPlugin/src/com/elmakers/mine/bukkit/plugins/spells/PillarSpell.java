package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.utilities.BlockList;
import com.elmakers.mine.bukkit.utilities.UndoableBlock;

public class PillarSpell extends Spell 
{
	int MAX_SEARCH_DISTANCE = 255;
	
	@Override
	public boolean onCast(String[] parameters) 
	{
		Block attachBlock = getTargetBlock();
		if (attachBlock == null)
		{
			castMessage(player, "No target");
			return false;
		}	

		BlockFace direction = BlockFace.UP;	
		if (parameters.length > 0 && parameters[0].equalsIgnoreCase("down"))
		{
			direction = BlockFace.DOWN;
		}
		
		Block targetBlock = attachBlock.getFace(direction);
		int distance = 0;
		
		while (isTargetable(targetBlock.getType()) && distance <= MAX_SEARCH_DISTANCE)
		{
			distance++;
			attachBlock = targetBlock;
			targetBlock = attachBlock.getFace(direction);
		}
		if (isTargetable(targetBlock.getType()))
		{
			player.sendMessage("Can't pillar any further");
			return false;
		}
		
		BlockList pillarBlocks = new BlockList();
		Block pillar = getBlockAt(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
		UndoableBlock undoPillar = pillarBlocks.addBlock(pillar);
		pillar.setType(attachBlock.getType());
		undoPillar.update();
		
		castMessage(player, "Creating a pillar of " + attachBlock.getType().name().toLowerCase());
		plugin.addToUndoQueue(player, pillarBlocks);
		//castMessage(player, "Facing " + playerRot + " : " + direction.name() + ", " + distance + " spaces to " + attachBlock.getType().name());
		
		return true;
	}

	@Override
	public String getName() 
	{
		return "pillar";
	}

	@Override
	public String getDescription() 
	{
		return "Raises a pillar up (or down)";
	}

	@Override
	public String getCategory() 
	{
		return "construction";
	}
}
