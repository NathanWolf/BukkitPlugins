package com.elmakers.mine.bukkit.plugins.spells.builtin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.utilities.BlockList;
import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;

public class MineSpell extends Spell
{
	static final String		DEFAULT_MINEABLE	= "14,15,16,56,73,74";
	static final String		DEFAULT_MINED	= "14,15,263,264,331,331";
	
	private List<Material>	mineableMaterials	= new ArrayList<Material>();
	private List<Material>	minedMaterials	= new ArrayList<Material>();
	private int maxRecursion = 16;
	
	@Override
	public boolean onCast(String[] parameters)
	{
		Block target = getTargetBlock();
		if (target == null)
		{
			castMessage(player, "No target");
			return false;
		}
		if (!isMineable(target))
		{
			sendMessage(player, "Can't mine " + target.getType().name().toLowerCase());
			return false;
		}
		
		BlockList minedBlocks = new BlockList();
		Material mineMaterial = target.getType();
		mine(target, mineMaterial, minedBlocks);
		
		World world = player.getWorld();
		
		int index = mineableMaterials.indexOf(mineMaterial);
		mineMaterial = minedMaterials.get(index);
		
		Location itemDrop = new Location(world, target.getX(), target.getY(), target.getZ(), 0, 0);
		player.getWorld().dropItemNaturally(itemDrop, new ItemStack(mineMaterial, minedBlocks.getCount()));
		
		// This isn't undoable, since we can't pick the items back up!
		// So, don't add it to the undo queue.
		castMessage(player, "Mined " + minedBlocks.getCount() + " blocks of " + mineMaterial.name().toLowerCase());
		
		return true;
	}
	
	protected void mine(Block block, Material fillMaterial, BlockList minedBlocks)
	{		
		mine(block, fillMaterial, minedBlocks, 0);
	}
	
	protected void mine(Block block, Material fillMaterial, BlockList minedBlocks, int rDepth)
	{
		minedBlocks.addBlock(block);
		block.setType(Material.AIR);
		
		if (rDepth < maxRecursion)
		{
			tryMine(block.getFace(BlockFace.NORTH), fillMaterial, minedBlocks, rDepth + 1);
			tryMine(block.getFace(BlockFace.WEST), fillMaterial, minedBlocks, rDepth + 1);
			tryMine(block.getFace(BlockFace.SOUTH), fillMaterial, minedBlocks, rDepth + 1);
			tryMine(block.getFace(BlockFace.EAST), fillMaterial, minedBlocks, rDepth + 1);
			tryMine(block.getFace(BlockFace.UP), fillMaterial, minedBlocks, rDepth + 1);
			tryMine(block.getFace(BlockFace.DOWN), fillMaterial, minedBlocks, rDepth + 1);
		}
	}
	
	protected void tryMine(Block target, Material fillMaterial, BlockList minedBlocks, int rDepth)
	{
		if (target.getType() != fillMaterial || minedBlocks.contains(target))
		{
			return;
		}
		
		mine(target, fillMaterial, minedBlocks, rDepth);
	}

	@Override
	public String getName()
	{
		return "mine";
	}

	@Override
	public String getCategory()
	{
		return "mining";
	}

	@Override
	public String getDescription()
	{
		return "Mines and drops the targeted resources";
	}
	
	public boolean isMineable(Block block)
	{
		if (block.getType() == Material.AIR)
			return false;

		return mineableMaterials.contains(block.getType());
	}

	@Override
	public void onLoad(PluginProperties properties)
	{
		mineableMaterials = properties.getMaterials("spells-mine-mineable", DEFAULT_MINEABLE);
		minedMaterials = properties.getMaterials("spells-mine-mined", DEFAULT_MINED);
		maxRecursion = properties.getInteger("spells-mine-recursion", maxRecursion);
	}

	@Override
	public Material getMaterial()
	{
		return Material.GOLD_PICKAXE;
	}
}
