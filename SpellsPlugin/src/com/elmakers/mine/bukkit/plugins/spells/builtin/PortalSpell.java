package com.elmakers.mine.bukkit.plugins.spells.builtin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.utilities.BlockList;

public class PortalSpell extends Spell
{
	private int				defaultSearchDistance	= 32;
	
	@Override
	public boolean onCast(String[] parameters)
	{
		Block target = getTargetBlock();
		if (target == null)
		{
			castMessage(player, "No target");
			return false;
		}
		if (defaultSearchDistance > 0 && getDistance(player, target) > defaultSearchDistance)
		{
			castMessage(player, "Can't create a portal that far away");
			return false;
		}
		
		Material blockType = target.getType();
		Block portalBase = target.getFace(BlockFace.UP);
		blockType = portalBase.getType();
		if (blockType != Material.AIR)
		{
			portalBase = getFaceBlock();
		}
		
		blockType = portalBase.getType();
		if (blockType != Material.AIR)
		{
			castMessage(player, "Can't create a portal there");
			return false;
		
		}
		
		BlockList portalBlocks = new BlockList();
		portalBlocks.setTimeToLive(10000);

		for (int y = 0; y < 5; y++)
		{
			setBlock(portalBlocks, portalBase, 0, y, 0, Material.OBSIDIAN);
			setBlock(portalBlocks, portalBase, 0, y, 3, Material.OBSIDIAN);
		}
		
		for (int z = 1; z < 3; z++)
		{
			setBlock(portalBlocks, portalBase, 0, 4, z, Material.OBSIDIAN);
			setBlock(portalBlocks, portalBase, 0, 0, z, Material.OBSIDIAN);
		}
		
		for (int z = 0; z < 2; z++)
		{
			for (int y = 0; y < 4; y++)
			{
				setBlock(portalBlocks, portalBase, 0, y, z, Material.PORTAL);
			}
		}
	
		spells.scheduleCleanup(portalBlocks);
		
		return true;
	}
		
	protected void setBlock(BlockList blocks, Block baseBlock, int x, int y, int z, Material material)
	{
		Block block = baseBlock.getRelative(x, y, z);
		if (block.getType() == Material.AIR)
		{
			blocks.addBlock(block);
			block.setType(material);
		}		
	}

	@Override
	protected String getName()
	{
		return "portal";
	}

	@Override
	public String getCategory()
	{
		return "nether";
	}

	@Override
	public String getDescription()
	{
		return "Create a temporary portal";
	}

	@Override
	public Material getMaterial()
	{
		return Material.FLINT_AND_STEEL;
	}

}
