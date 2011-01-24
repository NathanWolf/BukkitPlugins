package com.elmakers.mine.bukkit.plugins.spells.builtin;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;

public class BlinkSpell extends Spell
{
	private int maxRange = 0;
	private boolean allowAscend = true;
	private boolean allowDescend = true;
	private boolean allowPassthrough = true;
	
	public String getName()
	{
		return "blink";
	}
	
	public String getDescription()
	{
		return "Teleport to your target";
	}
	
	@Override
	public boolean onCast(String[] parameters)
	{
		if (yRotation < -80 && allowDescend)
		{
			Location location = findPlaceToStand(player.getLocation(), false);
			if (location != null) 
			{
				castMessage(player, "Blink down!");
				player.teleportTo(location);
				return true;
			}
		}
		
		if (yRotation > 80 && allowAscend)
		{
			Location location = findPlaceToStand(player.getLocation(), true);
			if (location != null) 
			{
				castMessage(player, "Blink up!");
				player.teleportTo(location);
				return true;
			}
		}
		
		if (allowPassthrough)
		{
			Block firstBlock = getNextBlock();
			if (firstBlock.getType() != Material.AIR)
			{
				reverseTargeting = true;
				targetThrough(Material.AIR);
			}
			else
			{
				targetThrough(Material.GLASS);
			}
		}
		
		Block target = getTargetBlock();
		Block face = getLastBlock();
		
		if (target == null) 
		{
			castMessage(player, "Nowhere to blink to");
			return false;
		}
		if (maxRange > 0 && getDistance(player,target) > maxRange) 
		{
			sendMessage(player, "Can't blink that far");
			return false;
		}
		
		World world = player.getWorld();
		
		// Don't drop the player too far, and make sure there is somewhere to stand
    	Block destination = face;
    	if (reverseTargeting)
    	{
    		destination = target;
    	}
    	Block groundBlock = destination.getFace(BlockFace.DOWN);
    	while (!isOkToStandOn(groundBlock.getType()))
    	{
    		destination = groundBlock;
    		groundBlock = destination.getFace(BlockFace.DOWN);
    	}
    	
		Block oneUp = destination.getFace(BlockFace.UP);
		Block twoUp = oneUp.getFace(BlockFace.UP);
		if (!isOkToStandIn(oneUp.getType()) || !isOkToStandIn(twoUp.getType()))
		{
			sendMessage(player, "You can't fit in there!");
			return false;
		}
		castMessage(player, "Blink!");
		player.teleportTo
		(
			new org.bukkit.Location
			(
				world,
				destination.getX() + 0.5,
				destination.getY(),
				destination.getZ() + 0.5,
				player.getLocation().getYaw(),
				player.getLocation().getPitch()
			)
		);
		return true;
	}
	
	@Override
	public String getCategory() 
	{
		return "movement";
	}

	@Override
	public void onLoad(PluginProperties properties)
	{
		maxRange = properties.getInteger("spells-blink-range", maxRange);
		allowAscend = properties.getBoolean("spells-blink-allow-ascend", allowAscend);
		allowDescend = properties.getBoolean("spells-blink-allow-ascend", allowDescend);
		allowPassthrough = properties.getBoolean("spells-blink-allow-ascend", allowPassthrough);
	}

	@Override
	public Material getMaterial()
	{
		return Material.FEATHER;
	}
}
