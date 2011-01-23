package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;

public class TreeSpell extends Spell
{
	private TreeType	defaultTreeType = TreeType.BIG;
	private boolean allowBig = true;
	private boolean allowStandard = true;
	private boolean allowUndoable = true;
	private boolean requireSapling = false;
	
	enum TreeType
	{
		STANDARD,
		BIG,
		NORMAL,
		REDWOOD,
		BIRCH;
		
		public String getTreeName()
		{
			return name().toLowerCase();
		}
		
		public static TreeType parseString(String s, TreeType defaultTreeType)
		{
			TreeType tree = defaultTreeType;
			for (TreeType t : TreeType.values())
			{
				if (t.name().equalsIgnoreCase(s))
				{
					tree = t;
				}
			}
			return tree;
		}
	};

	@Override
	public boolean onCast(String[] parameters)
	{
		Block target = getTargetBlock();

		if (target == null)
		{
			castMessage(player, "No target");
			return false;
		}
		
		if (requireSapling && target.getType() != Material.SAPLING)
		{
			castMessage(player, "Plant a sapling first");
			return false;
		}

		Location treeLoc = new Location(player.getWorld(), target.getX(), target.getY() + 1, target.getZ(), 0, 0);
		TreeType treeType = defaultTreeType;
		if (parameters.length > 0)
		{
			treeType = TreeType.parseString(parameters[0], defaultTreeType);
			if (!allowBig && treeType == TreeType.BIG) treeType = defaultTreeType;
			if (!allowStandard && treeType == TreeType.STANDARD) treeType = defaultTreeType;
			if (!allowUndoable && (treeType == TreeType.NORMAL || treeType == TreeType.BIRCH || treeType == TreeType.REDWOOD)) treeType = defaultTreeType;
		}
		
		boolean result = false;
		
		switch (treeType)
		{
			case BIG:
				result = player.getWorld().generateBigTree(treeLoc);
				break;
			case STANDARD:
				result = player.getWorld().generateTree(treeLoc);
				break;
		}
		
		if (result)
		{
			castMessage(player, "You grow a " + treeType.getTreeName() + " tree");
		}
		else
		{
			castMessage(player, "Your tree didn't grow");
		}
		return result;
	}

	@Override
	public String getName()
	{
		return "tree";
	}

	@Override
	public String getCategory()
	{
		return "farming";
	}

	@Override
	public String getDescription()
	{
		return "Creates a tree, or a big tree";
	}

	@Override
	public void onLoad(PluginProperties properties)
	{
		defaultTreeType = TreeType.parseString(properties.getString("spells-tree-default", defaultTreeType.getTreeName()), defaultTreeType);
		allowBig = properties.getBoolean("spells-tree-allow-big", allowBig);
		allowStandard = properties.getBoolean("spells-tree-allow-standard", allowStandard);
		allowUndoable = properties.getBoolean("spells-tree-allow-undoable", allowUndoable);
		requireSapling = properties.getBoolean("spells-tree-require-sapling", requireSapling);
	}

	@Override
	public Material getMaterial()
	{
		return Material.SAPLING;
	}
}
