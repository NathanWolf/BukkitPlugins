package com.elmakers.mine.bukkit.plugins.spells.builtin;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.utilities.BlockList;
import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;

public class FillSpell extends Spell 
{
	private int maxDimension = 128;
	private int maxVolume = 512;
	private final HashMap<String, Block> playerTargets = new HashMap<String, Block>();
	
	public FillSpell()
	{
		addVariant("paint", Material.PAINTING, getCategory(), "Fill a single block", "with single");
	}
	
	@Override
	public boolean onCast(String[] parameters) 
	{
		Block targetBlock = getTargetBlock();
		Material material = plugin.finishMaterialUse(player);
		boolean overrideMaterial = false;
		boolean singleBlock = false;
		byte data = plugin.getMaterialData(player);
	
		for (int i = 0; i < parameters.length; i++)
		{
			if (parameters[i].equalsIgnoreCase("with"))
			{
				overrideMaterial = true;
			}
			
			if (parameters[i].equalsIgnoreCase("single"))
			{
				singleBlock = true;
			}
		}
		
		if (targetBlock == null) 
		{
			castMessage(player, "No target");
			return false;
		}
		
		if (overrideMaterial)
		{
			ItemStack buildWith = getBuildingMaterial(!singleBlock);
			if (buildWith != null)
			{
				material = buildWith.getType();
				MaterialData targetData = buildWith.getData();
				if (targetData != null)
				{
					data = targetData.getData();
				}
			}
			else
			{
				overrideMaterial = false;
			}
		}
		
		if (singleBlock)
		{
			BlockList filledBlocks = new BlockList();
			
			filledBlocks.addBlock(targetBlock);
			targetBlock.setType(material);
			targetBlock.setData(data);
			
			castMessage(player, "Painting with " + material.name().toLowerCase());
			plugin.addToUndoQueue(player, filledBlocks);
			return true;
		}
		
		Block target = getTarget();
		
		if (target != null)
		{			
			int deltax = targetBlock.getX() - target.getX();
			int deltay = targetBlock.getY() - target.getY();
			int deltaz = targetBlock.getZ() - target.getZ();
			
			int absx = Math.abs(deltax);
			int absy = Math.abs(deltay);
			int absz = Math.abs(deltaz);
		
			if (maxDimension > 0 && (absx > maxDimension || absy > maxDimension || absz > maxDimension))
			{
				player.sendMessage("Dimension is too big!");
				return false;
			}

			if (maxVolume > 0 && absx * absy * absz > maxVolume)
			{
				player.sendMessage("Volume is too big!");
				return false;
			}
			
			int dx = (int)Math.signum(deltax);
			int dy = (int)Math.signum(deltay);
			int dz = (int)Math.signum(deltaz);
			
			absx++;
			absy++;
			absz++;
			
			BlockList filledBlocks = new BlockList();
			castMessage(player, "Filling " + absx + "x" + absy + "x" + absz + " area with " + material.name().toLowerCase());
			int x = target.getX();
			int y = target.getY();
			int z = target.getZ();
			for (int ix = 0; ix < absx; ix++)
			{
				for (int iy = 0; iy < absy; iy++)
				{
					for (int iz = 0; iz < absz; iz++)
					{
						Block block = getBlockAt(x + ix * dx, y + iy * dy, z + iz * dz);
						filledBlocks.addBlock(block);
						block.setType(material);
						block.setData(data);
					}
				}
			}
			plugin.addToUndoQueue(player, filledBlocks);
			
			setTarget(null);
			return true;
		}
		else
		{
			target = targetBlock;
			setTarget(target);
			plugin.startMaterialUse(player, target.getType(), target.getData());
			if (!overrideMaterial)
			{
				material = target.getType();
			}
			player.sendMessage("Cast again to fill with " + material.name().toLowerCase());
			return true;
		}
	}
	
	protected Block getTarget()
	{
		return playerTargets.get(player.getName());
	}
	
	protected void setTarget(Block target)
	{
		playerTargets.put(player.getName(), target);
	}
	
	@Override
	public void onCancel()
	{
		Block target = getTarget();
		if (target != null)
		{
			player.sendMessage("Cancelled fill");
			setTarget(null);
		}
	}

	@Override
	public String getName() 
	{
		return "fill";
	}

	@Override
	public String getDescription() 
	{
		return "Fills a selected area (2 clicks)";
	}

	@Override
	public String getCategory() 
	{
		return "construction";
	}

	@Override
	public void onLoad(PluginProperties properties)
	{
		maxDimension = properties.getInteger("spells-fill-max-dimension", maxDimension);
		maxVolume = properties.getInteger("spells-fill-max-volume", maxVolume);
	}

	@Override
	public Material getMaterial()
	{
		return Material.GOLD_SPADE;
	}
}
