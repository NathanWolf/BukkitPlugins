package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class ExtendSpell extends Spell 
{
	int MAX_SEARCH_DISTANCE = 16;
	
	@Override
	public boolean onCast(String[] parameters) 
	{
		Block playerBlock = getPlayerBlock();
		if (playerBlock == null) 
		{
			// no spot found to ascend
			player.sendMessage("Nowhere to extend");
			return false;
		}		

		float playerRot = getPlayerRotation();
	
		BlockFace direction = BlockFace.NORTH;
		if (playerRot <= 45 || playerRot > 315)
		{
			direction = BlockFace.WEST;
		}
		else if (playerRot > 45 && playerRot <= 135)
		{
			direction = BlockFace.NORTH;
		}
		else if (playerRot > 135 && playerRot <= 225)
		{
			direction = BlockFace.EAST;
		}
		else if (playerRot > 225 && playerRot <= 315)
		{
			direction = BlockFace.SOUTH;
		}
		Block attachBlock = playerBlock;
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
			player.sendMessage("Can't extend any further");
			return false;
		}
		setBlockAt(attachBlock.getTypeId(), targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
		player.sendMessage("You extend your target");
		//player.sendMessage("Facing " + playerRot + " : " + direction.name() + ", " + distance + " spaces to " + attachBlock.getType().name());
		
		return true;
	}

	@Override
	public String getName() 
	{
		return "extend";
	}

	@Override
	public String getDescription() 
	{
		return "Extends your target outward";
	}

	@Override
	public String getCategory() 
	{
		return "build";
	}
}
