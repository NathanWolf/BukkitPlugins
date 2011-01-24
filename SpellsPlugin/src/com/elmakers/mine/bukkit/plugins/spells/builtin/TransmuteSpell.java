package com.elmakers.mine.bukkit.plugins.spells.builtin;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.SpellEventType;
import com.elmakers.mine.bukkit.plugins.spells.utilities.BlockList;
import com.elmakers.mine.bukkit.plugins.spells.utilities.UndoableBlock;

public class TransmuteSpell extends Spell
{
	private HashMap<String, Boolean> transmuting = new HashMap<String, Boolean>();

	@Override
	public boolean onCast(String[] parameters)
	{
		if (isTransmuting(player))
		{
			setTransmuting(player, false);
			sendMessage(player, "Transmute cancelled");
			checkListener();
			return false;
		}
		
		BlockList lastAction = plugin.getLastBlockList(player.getName());
		if (lastAction == null)
		{
			sendMessage(player, "Nothing to transmute");
			return false;
		}
		
		sendMessage(player, "You concentrate on your last construction");
		setTransmuting(player, true);
		// Hmmm.. kinda hacky :(
		plugin.startMaterialUse(player, Material.STICK, (byte)0);
		checkListener();
		return true;
	}
	
	public void onCancel()
	{
		if (isTransmuting(player))
		{
			setTransmuting(player, false);
			checkListener();
			sendMessage(player, "Transmute cancelled");
		}
	}

	public void onMaterialChoose(Player player)
	{
		if (!isTransmuting(player)) return;
		setTransmuting(player, false);
		checkListener();
		
		BlockList lastAction = plugin.getLastBlockList(player.getName());
		if (lastAction == null) return;
		
		Material material = plugin.finishMaterialUse(player);
		byte data = plugin.getMaterialData(player);
		for (UndoableBlock undoBlock : lastAction.getBlocks())
		{
			Block block = undoBlock.getBlock();
			block.setType(material);
			block.setData(data);
			undoBlock.update();
		}
		
		castMessage(player, "You transmute your last structure to " + material.name().toLowerCase());
	}
	
	protected void checkListener()
	{
		boolean anyoneTransmuting = false;
		for (Boolean is : transmuting.values())
		{
			if (is != null && is)
			{
				anyoneTransmuting = true;
				break;
			}
		}
		if (anyoneTransmuting)
		{
			plugin.registerEvent(SpellEventType.MATERIAL_CHANGE, this);
		}
		else
		{
			plugin.unregisterEvent(SpellEventType.MATERIAL_CHANGE, this);
		}
	}
	
	protected boolean isTransmuting(Player player)
	{
		Boolean is = transmuting.get(player.getName());
		return (is != null && is);
	}
	
	protected void setTransmuting(Player player, boolean is)
	{
		transmuting.put(player.getName(), is);
	}

	@Override
	public String getName()
	{
		return "transmute";
	}

	@Override
	public String getCategory()
	{
		return "construction";
	}

	@Override
	public String getDescription()
	{
		return "Modify the material of the last thing you built";
	}

	@Override
	public Material getMaterial()
	{
		return Material.GOLD_INGOT;
	}

}
