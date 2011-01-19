package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;

import com.elmakers.mine.bukkit.utilities.PluginProperties;

public class CushionSpell extends Spell
{
	private int cushionWidth = 5;
	private int cushionHeight = 4;
	
	@Override
	public boolean onCast(String[] parameters) 
	{
		World world = player.getWorld();
    	CraftWorld craftWorld = (CraftWorld)world;
  		Block targetFace = getFaceBlock();
		if (targetFace == null)
		{
			player.sendMessage("No target");
			return false;
		}
		
		player.sendMessage("Creating cushion");
		
		BlockList blocks = new BlockList(player);
		blocks.setTimeToLive(10000);
		for (int dx = -cushionWidth  / 2 ; dx < cushionWidth / 2; dx++)
		{
			for (int dz = -cushionWidth  / 2 ; dz < cushionWidth / 2; dz++)
			{
				for (int dy = 0; dy < cushionHeight; dy++)
				{
					int x = targetFace.getX() + dx;
					int y = targetFace.getY() + dy;
					int z = targetFace.getZ() + dz;
					Block block = craftWorld.getBlockAt(x, y, z);
					if (block.getType() == Material.AIR)
					{
						blocks.addBlock(block);
						block.setType(Material.STATIONARY_WATER);
						craftWorld.updateBlock(block.getX(), block.getY(), block.getZ());
					}
				}
			}
		}
	
		plugin.scheduleCleanup(blocks);
	
		// Schedule an additional later cleanup, to cleanup water spillage
		BlockList delayedCleanup = new BlockList(blocks);
		delayedCleanup.setTimeToLive(15000);
		
		return true;
	}

	@Override
	public String getName() 
	{
		return "cushion";
	}

	@Override
	public String getDescription() 
	{
		return "Create a safety bubble";
	}

	@Override
	public String getCategory() 
	{
		return "help";
	}
	
	@Override
	public void load(PluginProperties properties)
	{
		cushionWidth = properties.getInteger("spells-cushion-width", cushionWidth);
		cushionHeight = properties.getInteger("spells-cushion-height", cushionHeight);
	}
}
