package com.elmakers.mine.bukkit.plugins.spells.builtin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.utilities.BlockList;
import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;

public class ConstructSpell extends Spell
{
	static final String		DEFAULT_DESTRUCTIBLES	= "1,2,3,8,9,10,11,12,13";

	private List<Material>	destructibleMaterials	= new ArrayList<Material>();
	private ConstructionType defaultConstructionType = ConstructionType.SPHERE;
	private int				defaultRadius			= 2;
	private int				maxRadius				= 32;
	private int				defaultSearchDistance	= 32;
	
	public ConstructSpell()
	{
		addVariant("shell", Material.BOWL, getCategory(), "Create a large shell using your selected material", "shell 12");
		addVariant("superblob", Material.CLAY_BRICK, getCategory(), "Create a large sphere at your target", "sphere 11");
	}
	
	enum ConstructionType
	{
		SPHERE,
		CUBOID,
		SHELL;
		
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
		
		Material material = target.getType();
		byte data = target.getData();
		
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
		
		switch (conType)
		{
			case SPHERE: constructSphere(target, radius, material, data); break;
			case SHELL: constructShell(target, radius, material, data); break;
			default : return false;
		}
		
		
		return true;
	}
	
	public void constructSphere(Block target, int radius, Material material, byte data)
	{
		fillSphere(target, radius, material, data, true);
	}
	
	public void constructShell(Block target, int radius, Material material, byte data)
	{
		fillSphere(target, radius, material, data, false);
	}
	
	public void fillSphere(Block target, int radius, Material material, byte data, boolean fill)
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
					int position = checkSpherePosition(x - midX, y - midY, z - midZ, radius);
					if 
					(
						(fill && position <= 0)
					||	(!fill && position <= 0 && getDistance(x - midX, y - midY, z - midZ) >= radius - 2)
					)
					{
						constructBlock(x, y, z, target, radius, material, data, constructedBlocks);
						constructBlock(diameterOffset - x, y, z, target, radius, material, data, constructedBlocks);
						constructBlock(x, diameterOffset - y, z, target, radius, material, data, constructedBlocks);
						constructBlock(x, y, diameterOffset - z, target, radius, material, data, constructedBlocks);
						constructBlock(diameterOffset - x, diameterOffset - y, z, target, radius, material, data, constructedBlocks);
						constructBlock(x, diameterOffset - y, diameterOffset - z, target, radius, material, data, constructedBlocks);
						constructBlock(diameterOffset - x, y, diameterOffset - z, target, radius, material, data, constructedBlocks);
						constructBlock(diameterOffset - x, diameterOffset - y, diameterOffset - z, target, radius, material, data, constructedBlocks);
					}
				}
			}
		}

		plugin.addToUndoQueue(player, constructedBlocks);
		castMessage(player, "Constructed " + constructedBlocks.getCount() + "blocks");
	}
	
	public int getDistance(int x, int y, int z)
	{
		return (int)(Math.sqrt(x * x + y * y + z * z) + 0.5);
	}
	
	public int checkSpherePosition(int x, int y, int z, int R)
	{
		return (x * x) + (y * y) + (z * z) - (R * R);
	}

	public void constructBlock(int dx, int dy, int dz, Block centerPoint, int radius, Material material, byte data, BlockList constructedBlocks)
	{
		int x = centerPoint.getX() + dx - radius;
		int y = centerPoint.getY() + dy - radius;
		int z = centerPoint.getZ() + dz - radius;
		Block block = player.getWorld().getBlockAt(x, y, z);
		if (!isDestructible(block))
		{
			return;
		}
		constructedBlocks.addBlock(block);
		block.setType(material);
		block.setData(data);
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
		return "blob";
	}

	@Override
	public String getCategory()
	{
		return "construction";
	}

	@Override
	public String getDescription()
	{
		return "Add some blocks to your target";
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

	@Override
	public Material getMaterial()
	{
		return Material.CLAY_BALL;
	}

}
