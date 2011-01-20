package com.elmakers.mine.bukkit.plugins.spells;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.utilities.PluginProperties;
import com.elmakers.mine.bukkit.utilities.UndoableBlock;

public class AlterSpell extends Spell
{
	static final String DEFAULT_ADJUSTABLES = "6,8,9,10,11,17,18,35,50,53,54,55,58,59,60,61,62,63,64,67,69,71,75,76,77,81,83";
	
	private List<Material> adjustableMaterials = new ArrayList<Material>();
	
	@Override
	public boolean onCast(String[] parameters)
	{
		Block targetBlock = getTargetBlock();
		if (targetBlock == null) 
		{
			player.sendMessage("No target");
			return false;
		}
		if (!adjustableMaterials.contains(targetBlock.getType()))
		{
			player.sendMessage("Can't adjust " + targetBlock.getType().name().toLowerCase());
			return false;
		}
		
		BlockList undoList = new BlockList();
		
		UndoableBlock undoBlock = undoList.addBlock(targetBlock);
		byte originalData = targetBlock.getData();
		byte data = originalData;
		data = (byte)((data + 1) % 16);
		targetBlock.setData(data);
		undoBlock.update();
		
		plugin.addToUndoQueue(player, undoList);
		
		player.sendMessage("Adjusting " + targetBlock.getType().name().toLowerCase() + " from " + originalData + " to " + data);
		
		return true;
	}

	@Override
	public String getName()
	{
		return "alter";
	}

	@Override
	public String getCategory()
	{
		return "build";
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
	}

}
