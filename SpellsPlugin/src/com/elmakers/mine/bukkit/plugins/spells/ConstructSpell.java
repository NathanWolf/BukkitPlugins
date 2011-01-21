package com.elmakers.mine.bukkit.plugins.spells;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.plugins.spells.utilities.BlockList;
import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;
import com.elmakers.mine.bukkit.plugins.spells.utilities.UndoableBlock;

public class ConstructSpell extends Spell
{
	static final String		DEFAULT_DESTRUCTIBLES	= "1,2,3,10,11,12,13";

	private List<Material>	destructibleMaterials	= new ArrayList<Material>();
	private ConstructionType defaultConstructionType = ConstructionType.SPHERE;
	private int				defaultRadius			= 2;
	private int				maxRadius				= 32;
	private int				defaultSearchDistance	= 32;
	
	enum ConstructionType
	{
		SPHERE,
		CUBOID;
		
		public static ConstructionType parseString(String s, ConstructionType defaultType)
		{
			ConstructionType construct = defaultType;
			for (ConstructionType t : ConstructionType.values())
			{
				if (t.name().equalsIgnoreCase(s))
				{
					construct = t;
				}
			}
			return construct;
		}
	};
	
	@Override
	public boolean onCast(String[] parameters)
	{
		targetThrough(Material.GLASS);
		Block target = getTargetBlock();
		if (target == null)
		{
			initializeTargeting(player);
			noTargetThrough(Material.GLASS);
			target = getTargetBlock();
			if (target == null)
			{
				castMessage(player, "No target");
				return false;
			}
		}
		
		ConstructionType conType = defaultConstructionType;
		if (parameters.length > 0)
		{
			conType = ConstructionType.parseString(parameters[0], conType);
		}
		
		int radius = defaultRadius;
		if (parameters.length > 1)
		{
			try
			{
				radius = Integer.parseInt(parameters[1]);
				if (radius > maxRadius && maxRadius > 0)
				{
					radius = maxRadius;
				}
			} 
			catch(NumberFormatException ex)
			{
				radius = defaultRadius;
			}
		}
		
		// For now, there is only sphere!
		constructSphere(target, radius);
		
		return true;
	}
	
	public void constructSphere(Block target, int radius)
	{
		BlockList constructedBlocks = new BlockList();
		int diameter = radius * 2;
		int midX = (diameter - 1) / 2;
		int midY = (diameter - 1) / 2;
		int midZ = (diameter - 1) / 2;
		int diameterOffset = diameter - 1;

		for (int x = 0; x < radius; ++x)
		{
			for (int y = 0; y < radius; ++y)
			{
				for (int z = 0; z < radius; ++z)
				{
					if (checkPosition(x - midX, y - midY, z - midZ, radius) <= 0)
					{
						constructBlock(x, y, z, target, radius, constructedBlocks);
						constructBlock(diameterOffset - x, y, z, target, radius, constructedBlocks);
						constructBlock(x, diameterOffset - y, z, target, radius, constructedBlocks);
						constructBlock(x, y, diameterOffset - z, target, radius, constructedBlocks);
						constructBlock(diameterOffset - x, diameterOffset - y, z, target, radius, constructedBlocks);
						constructBlock(x, diameterOffset - y, diameterOffset - z, target, radius, constructedBlocks);
						constructBlock(diameterOffset - x, y, diameterOffset - z, target, radius, constructedBlocks);
						constructBlock(diameterOffset - x, diameterOffset - y, diameterOffset - z, target, radius, constructedBlocks);
					}
				}
			}
		}

		plugin.addToUndoQueue(player, constructedBlocks);
		castMessage(player, "Constructed " + constructedBlocks.getCount() + "blocks");
	}
	
	public int checkPosition(int x, int y, int z, int R)
	{
		return (x * x) + (y * y) + (z * z) - (R * R);
	}

	public void constructBlock(int dx, int dy, int dz, Block centerPoint, int radius, BlockList constructedBlocks)
	{
		int x = centerPoint.getX() + dx - radius;
		int y = centerPoint.getY() + dy - radius;
		int z = centerPoint.getZ() + dz - radius;
		Block block = player.getWorld().getBlockAt(x, y, z);
		if (!isDestructible(block))
		{
			return;
		}
		UndoableBlock undoBlock = constructedBlocks.addBlock(block);
		block.setType(centerPoint.getType());
		undoBlock.update();
	}

	public boolean isDestructible(Block block)
	{
		if (block.getType() == Material.AIR)
			return true;

		return destructibleMaterials.contains(block.getType());
	}

	@Override
	public String getName()
	{
		return "construct";
	}

	@Override
	public String getCategory()
	{
		return "construction";
	}

	@Override
	public String getDescription()
	{
		return "Construct a shape your target";
	}
	
	@Override
	public void onLoad(PluginProperties properties)
	{
		destructibleMaterials = properties.getMaterials("spells-construct-destructible", DEFAULT_DESTRUCTIBLES);
		defaultConstructionType = ConstructionType.parseString(properties.getString("spells-construct-default", ""), defaultConstructionType);
		defaultRadius = properties.getInteger("spells-construct-radius", defaultRadius);
		maxRadius = properties.getInteger("spells-construct-max-radius", maxRadius);
		defaultSearchDistance = properties.getInteger("spells-constructs-search-distance", defaultSearchDistance);
	}

}
