package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;

public class TreeSpell extends Spell
{
	private TreeType	defaultTreeType = TreeType.BIG_TREE;
	private boolean allowBig = true;
	private boolean allowStandard = true;
	private boolean allowUndoable = true;
	private boolean requireSapling = false;
	
	enum TreeType
	{
		STANDARD_TREE,
		BIG_TREE,
		NORMAL_TREE,
		REDWOOD_TREE,
		BIRCH_TREE;
		
		public String getTreeName()
		{
			switch (this)
			{
				case STANDARD_TREE: return "standard";
				case BIG_TREE: return "big";
				case NORMAL_TREE: return "normal";
				case REDWOOD_TREE: return "redwood";
				case BIRCH_TREE: return "birch";
			}
			return "unknown";
		}
		
		public static TreeType parseString(String t, TreeType defaultTreeType)
		{
			TreeType tree = defaultTreeType;
			if (t.equalsIgnoreCase("standard"))
			{
				tree = TreeType.STANDARD_TREE;
			}
			else if (t.equalsIgnoreCase("big"))
			{
				tree = TreeType.BIG_TREE;
			}
			else if (t.equalsIgnoreCase("normal"))
			{
				tree = TreeType.NORMAL_TREE;
			}
			else if (t.equalsIgnoreCase("redwood"))
			{
				tree = TreeType.REDWOOD_TREE;
			}
			else if (t.equalsIgnoreCase("birch"))
			{
				tree = TreeType.BIRCH_TREE;
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
			if (!allowBig && treeType == TreeType.BIG_TREE) treeType = defaultTreeType;
			if (!allowStandard && treeType == TreeType.STANDARD_TREE) treeType = defaultTreeType;
			if (!allowUndoable && (treeType == TreeType.NORMAL_TREE || treeType == TreeType.BIRCH_TREE || treeType == TreeType.REDWOOD_TREE)) treeType = defaultTreeType;
		}
		
		boolean result = false;
		
		switch (treeType)
		{
			case BIG_TREE:
				result = player.getWorld().generateBigTree(treeLoc);
				break;
			case STANDARD_TREE:
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
}
