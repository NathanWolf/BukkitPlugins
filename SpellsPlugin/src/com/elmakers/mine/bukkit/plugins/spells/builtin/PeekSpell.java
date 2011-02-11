package com.elmakers.mine.bukkit.plugins.spells.builtin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;

import com.elmakers.mine.bukkit.plugins.nether.NetherManager;
import com.elmakers.mine.bukkit.plugins.nether.listener.BlockRequestListener;
import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.utilities.BlockList;
import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;

public class PeekSpell extends Spell implements BlockRequestListener
{
	static final String		DEFAULT_PEEKABLES		= "1,2,3,10,11,12,13";

	private List<Material>	peekableMaterials		= new ArrayList<Material>();
	private int				defaultRadius			= 3;
	private int				maxRadius				= 32;
	private int				defaultSearchDistance	= 32;
	private boolean			fillAir					= false;
	
	private int				radius					= defaultRadius;
	private Block			targetBlock				= null;
	private NetherManager	nether					= null;
	
	public PeekSpell(NetherManager nether)
	{
		this.nether = nether;
		if (nether != null)
		{
			addVariant("window", Material.MOB_SPAWNER, getCategory(), "Create a window into another world", "5 world");
		}
	}

	@Override
	public boolean onCast(String[] parameters)
	{
		targetThrough(Material.GLASS);
		Block target = getTargetBlock();
		if (target == null)
		{
			castMessage(player, "No target");
			return false;
		}
		if (defaultSearchDistance > 0 && getDistance(player, target) > defaultSearchDistance)
		{
			castMessage(player, "Can't peek that far away");
			return false;
		}

		boolean window = false;
		String worldName = null;
		radius = defaultRadius;
		
		for (int i = 0; i < parameters.length; i++)
		{
			String parameter = parameters[i];
			if (parameter.equalsIgnoreCase("world"))
			{
				window = true;
				if (i < parameters.length - 1)
				{
					worldName = parameters[i + 1];
				}
			}
			// Try for number
			try
			{
				int r = Integer.parseInt(parameter);
				radius = r;
				if (radius > maxRadius && maxRadius > 0)
				{
					radius = maxRadius;
				}
			} 
			catch(NumberFormatException ex)
			{
			}
		}
		
		if (window)
		{
			if (targetBlock == null)
			{
				targetBlock = target;
				
				nether.requestBlockList(player.getWorld(), worldName, new BlockVector(target.getX(), target.getY(), target.getZ()), radius, this);
			}
			else
			{
				sendMessage(player, "You must wait for your previous window");
				return false;
			}
		}
		else
		{		
			BlockList peekedBlocks = peek(target, radius, null);
			if (peekedBlocks == null)
			{
				sendMessage(player, "Peek failed");
				return false;
			}
			spells.scheduleCleanup(peekedBlocks);
			castMessage(player, "Peeked through  " + peekedBlocks.getCount() + "blocks");
		}

		return true;
	}
	
	protected BlockList peek(Block target, int radius, List<Block> blocks)
	{
		BlockList peekedBlocks = new BlockList();
		int diameter = radius * 2;
		int midX = (diameter - 1) / 2;
		int midY = (diameter - 1) / 2;
		int midZ = (diameter - 1) / 2;
		int diameterOffset = diameter - 1;
		
		// Sanity check
		if (blocks.size() != diameter * diameter * diameter)
		{
			return null;
		}
		
		for (int x = 0; x < diameter; ++x)
		{
			for (int y = 0; y < diameter; ++y)
			{
				for (int z = 0; z < diameter; ++z)
				{
					if (checkPosition(x - midX, y - midY, z - midZ, radius) <= 0)
					{
						Material mat = Material.GLASS;
						if (blocks != null)
						{
							mat = blocks.get(x + (y * diameter) + z * diameter * diameter).getType();
						}
						
						peekBlock(x, y, z, target, radius, peekedBlocks, mat);
						peekBlock(diameterOffset - x, y, z, target, radius, peekedBlocks, mat);
						peekBlock(x, diameterOffset - y, z, target, radius, peekedBlocks, mat);
						peekBlock(x, y, diameterOffset - z, target, radius, peekedBlocks, mat);
						peekBlock(diameterOffset - x, diameterOffset - y, z, target, radius, peekedBlocks, mat);
						peekBlock(x, diameterOffset - y, diameterOffset - z, target, radius, peekedBlocks, mat);
						peekBlock(diameterOffset - x, y, diameterOffset - z, target, radius, peekedBlocks, mat);
						peekBlock(diameterOffset - x, diameterOffset - y, diameterOffset - z, target, radius, peekedBlocks, mat);
					}
				}
			}
			
		}
		
		peekedBlocks.setTimeToLive(8000);
		
		return peekedBlocks;
	}
	

	public void onBlockListLoaded(List<Block> blocks)
	{
		fillAir = true;
		BlockList peekedBlocks = peek(targetBlock, radius, blocks);
		fillAir = false;
		targetBlock = null;
		if (peekedBlocks == null)
		{
			return;
		}
		spells.scheduleCleanup(peekedBlocks);
	
		castMessage(player, "Windowed through  " + peekedBlocks.getCount() + "blocks");
	}
	

	public int checkPosition(int x, int y, int z, int R)
	{
		return (x * x) + (y * y) + (z * z) - (R * R);
	}

	public void peekBlock(int dx, int dy, int dz, Block centerPoint, int radius, BlockList blocks, Material mat)
	{
		int x = centerPoint.getX() + dx - radius;
		int y = centerPoint.getY() + dy - radius;
		int z = centerPoint.getZ() + dz - radius;
		Block block = player.getWorld().getBlockAt(x, y, z);
		if (!isPeekable(block))
		{
			return;
		}
		blocks.addBlock(block);
		block.setType(mat);
	}

	public boolean isPeekable(Block block)
	{
		if (block.getType() == Material.AIR)
			return fillAir;
		
		if (block.getType() == Material.GLASS)
			return false;
		
		return peekableMaterials.contains(block.getType());
	}
	
	@Override
	public void onLoad(PluginProperties properties)
	{
		peekableMaterials = properties.getMaterials("spells-peek-peekable", DEFAULT_PEEKABLES);
		defaultRadius = properties.getInteger("spells-peek-radius", defaultRadius);
		maxRadius = properties.getInteger("spells-peek-max-radius", maxRadius);
		defaultSearchDistance = properties.getInteger("spells-peek-search-distance", defaultSearchDistance);
	}

	@Override
	protected String getName()
	{
		return "peek";
	}

	@Override
	public String getCategory()
	{
		return "exploration";
	}

	@Override
	public String getDescription()
	{
		return "Temporarily glass your target surface";
	}

	@Override
	public Material getMaterial()
	{
		return Material.SUGAR_CANE;
	}

}
