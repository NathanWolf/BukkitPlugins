package com.elmakers.mine.bukkit.plugins.spells;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.utilities.BlockList;
import com.elmakers.mine.bukkit.utilities.PluginProperties;
import com.elmakers.mine.bukkit.utilities.UndoableBlock;

public class AlterSpell extends Spell
{
	static final String DEFAULT_ADJUSTABLES = "6,8,9,10,11,17,18,35,50,52,53,54,55,58,59,60,61,62,63,64,65,66,67,69,71,75,76,77,81,83,85,86";
	static final String DEFAULT_RECURSABLES = "17,18,59";
	
	private List<Material> adjustableMaterials = new ArrayList<Material>();
	private int recurseDistance = 16;
	private List<Material> recursableMaterials = new ArrayList<Material>();
	
	@Override
	public boolean onCast(String[] parameters)
	{
		Block targetBlock = getTargetBlock();
		if (targetBlock == null) 
		{
			castMessage(player, "No target");
			return false;
		}
		if (!adjustableMaterials.contains(targetBlock.getType()))
		{
			player.sendMessage("Can't adjust " + targetBlock.getType().name().toLowerCase());
			return false;
		}
		
		BlockList undoList = new BlockList();
		byte originalData = targetBlock.getData();
		byte data = originalData;
		data = (byte)((data + 1) % 16);
		boolean recursive = recursableMaterials.contains(targetBlock.getType());
		
		adjust(targetBlock, data, undoList, recursive, 0);
		
		plugin.addToUndoQueue(player, undoList);
		
		castMessage(player, "Adjusting " + targetBlock.getType().name().toLowerCase() + " from " + originalData + " to " + data);
		
		return true;
	}
	
	protected void adjust(Block block, byte dataValue, BlockList adjustedBlocks, boolean recursive, int rDepth)
	{
		UndoableBlock undoBlock = adjustedBlocks.addBlock(block);
		block.setData(dataValue);
		undoBlock.update();
		
		if (recursive && rDepth < recurseDistance)
		{
			Material targetMaterial = block.getType();
			tryAdjust(block.getFace(BlockFace.NORTH), dataValue,targetMaterial, adjustedBlocks, rDepth + 1);
			tryAdjust(block.getFace(BlockFace.WEST), dataValue,targetMaterial, adjustedBlocks, rDepth + 1);
			tryAdjust(block.getFace(BlockFace.SOUTH), dataValue,targetMaterial, adjustedBlocks, rDepth + 1);
			tryAdjust(block.getFace(BlockFace.EAST), dataValue,targetMaterial, adjustedBlocks, rDepth + 1);
			tryAdjust(block.getFace(BlockFace.UP), dataValue,targetMaterial, adjustedBlocks, rDepth + 1);
			tryAdjust(block.getFace(BlockFace.DOWN), dataValue,targetMaterial, adjustedBlocks, rDepth + 1);			
		}
	}
	
	protected void tryAdjust(Block target, byte dataValue, Material targetMaterial, BlockList adjustedBlocks, int rDepth)
	{
		if (target.getType() != targetMaterial || adjustedBlocks.contains(target))
		{
			return;
		}
		
		adjust(target, dataValue, adjustedBlocks, true, rDepth);
	}
	
	@Override
	public String getName()
	{
		return "alter";
	}

	@Override
	public String getCategory()
	{
		return "construction";
	}

	@Override
	public String getDescription()
	{
		return "Alter certain objects, such as stairs or wool";
	}
	
	@Override
	public void onLoad(PluginProperties properties)
	{
		adjustableMaterials = properties.getMaterials("spells-adjustable", DEFAULT_ADJUSTABLES);
		recursableMaterials = properties.getMaterials("spells-recursable", DEFAULT_RECURSABLES);
	}

}
