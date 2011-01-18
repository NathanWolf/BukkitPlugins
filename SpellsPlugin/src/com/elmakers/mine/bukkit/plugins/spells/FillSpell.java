package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.utilities.PluginProperties;

public class FillSpell extends Spell 
{
	int maxDimension = 128;
	int maxVolume = 512;
	Block target = null;
	
	@Override
	public boolean onCast(String[] parameters) 
	{
		Block targetBlock = getTargetBlock();
		Material material = plugin.finishMaterialUse(player);
		if (targetBlock == null) 
		{
			player.sendMessage("No target");
			target = null;
			return false;
		}
		
		if (target != null)
		{			
			int deltax = targetBlock.getX() - target.getX();
			int deltay = targetBlock.getY() - target.getY();
			int deltaz = targetBlock.getZ() - target.getZ();
			
			int absx = Math.abs(deltax);
			int absy = Math.abs(deltay);
			int absz = Math.abs(deltaz);
		
			if (absx > maxDimension || absy > maxDimension || absz > maxDimension)
			{
				player.sendMessage("Dimension is too big!");
				target = null;
				return false;
			}

			if (absx * absy * absz > maxVolume)
			{
				player.sendMessage("Volume is too big!");
				target = null;
				return false;
			}
			
			int dx = (int)Math.signum(deltax);
			int dy = (int)Math.signum(deltay);
			int dz = (int)Math.signum(deltaz);
			
			absx++;
			absy++;
			absz++;
			
			player.sendMessage("Filling " + absx + "x" + absy + "x" + absz + " area with " + material.name().toLowerCase());
			int x = target.getX();
			int y = target.getY();
			int z = target.getZ();
			for (int ix = 0; ix < absx; ix++)
			{
				for (int iy = 0; iy < absy; iy++)
				{
					for (int iz = 0; iz < absz; iz++)
					{
						setBlockAt(material.getId(), x + ix * dx, y + iy * dy, z + iz * dz);
					}
				}
			}
			
			target = null;
			return true;
		}
		else
		{
			target = targetBlock;
			plugin.startMaterialUse(player, target.getType());
			player.sendMessage("Cast again to fill with " + target.getType().name().toLowerCase());
			return true;
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
		return "build";
	}

	@Override
	public void load(PluginProperties properties)
	{
		maxDimension = properties.getInteger("spells-fill-max-dimension", maxDimension);
		maxVolume = properties.getInteger("spells-fill-max-volume", maxVolume);
	}
}
