package com.elmakers.mine.bukkit.plugins.spells.builtin;

import org.bukkit.block.Block;
import org.bukkit.Material;

import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.utilities.BlockList;
import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;
import com.elmakers.mine.bukkit.plugins.spells.utilities.UndoableBlock;

public class TorchSpell extends Spell 
{
	private boolean allowDay = true;
	private boolean allowLightstone = true;

	@Override
	public boolean onCast(String[] parameters) 
	{
		if (getYRotation() > 80 && allowDay)
		{
			castMessage(player, "FLAME ON!");
			setRelativeTime(0);
			return true;
		}
		
		Block target = getTargetBlock();	
		Block face = getLastBlock();
		
		boolean isAir = face.getType() == Material.AIR;
		boolean isAttachmentSlippery = target.getType() == Material.GLASS || target.getType() == Material.ICE || target.getType() == Material.SNOW;
		boolean isWater = face.getType() == Material.STATIONARY_WATER || face.getType() == Material.WATER;
		if 
		(
				face == null
		|| 		(!isAir && !isWater)
		||		(isWater && !allowLightstone)
		||		(isAttachmentSlippery && !allowLightstone)
		)
		{
			player.sendMessage("Can't put a torch there");
			return false;
		}
		
		castMessage(player, "Flame on!");
		BlockList torchBlock = new BlockList();
		UndoableBlock undoBlock = torchBlock.addBlock(face);
		
		if (isWater || isAttachmentSlippery)
		{
			face.setType(Material.GLOWSTONE);
		}
		else
		{
			face.setType(Material.TORCH);
		}
		
		undoBlock.update();
		plugin.addToUndoQueue(player, torchBlock);
		
		return true;
	}

	@Override
	public String getName() 
	{
		return "torch";
	}

	@Override
	public String getDescription() 
	{
		return "Place a torch at your target";
	}

	@Override
	public String getCategory() 
	{
		return "construction";
	}

	@Override
	public void onLoad(PluginProperties properties)
	{
		allowDay = properties.getBoolean("spells-torch-allow-day", allowDay);
		allowLightstone = properties.getBoolean("spells-torch-allow-lightstone", allowLightstone);
	}

	@Override
	public Material getMaterial()
	{
		return Material.TORCH;
	}
}
