package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.Player;

public abstract class Spell 
{
	public abstract boolean cast(SpellsPlugin plugin, Player player);
	public abstract String getName();
	public abstract String getDescription();
}
