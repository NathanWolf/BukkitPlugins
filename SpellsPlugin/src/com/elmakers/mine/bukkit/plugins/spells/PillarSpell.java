package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class PillarSpell extends Spell 
{
	int MAX_SEARCH_DISTANCE = 255;
	
	@Override
	public boolean onCast(String[] parameters) 
	{
		Block attachBlock = getTargetBlock();
		if (attachBlock == null)
		{
			player.sendMessage("No target");
			return false;
		}	

		BlockFace direction = BlockFace.UP;	
		if (parameters.length > 0 && parameters[0].equalsIgnoreCase("down"))
		{
			direction = BlockFace.DOWN;
		}
		
		Block targetBlock = attachBlock.getFace(direction);
		int distance = 0;
		
		while (targetBlock.getType() != Material.AIR && distance <= MAX_SEARCH_DISTANCE)
		{
			distance++;
			attachBlock = targetBlock;
			targetBlock = attachBlock.getFace(direction);
		}
		if (targetBlock.getType() != Material.AIR)
		{
			player.sendMessage("Can't pillar any further");
			return false;
		}
		setBlockAt(attachBlock.getTypeId(), targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
		player.sendMessage("Creating a pillar of " + attachBlock.getType().name().toLowerCase());
		//player.sendMessage("Facing " + playerRot + " : " + direction.name() + ", " + distance + " spaces to " + attachBlock.getType().name());
		
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
		return "build";
	}
}
