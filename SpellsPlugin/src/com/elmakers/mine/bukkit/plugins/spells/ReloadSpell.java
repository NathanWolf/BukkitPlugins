package com.elmakers.mine.bukkit.plugins.spells;

public class ReloadSpell extends Spell {

	@Override
	public boolean onCast(String[] parameters) 
	{
		plugin.load();
		return true;
	}

	@Override
	public String getName() 
	{
		return "reload";
	}

	@Override
	public String getCategory() 
	{
		return "admin";
	}

	@Override
	public String getDescription() 
	{
		return "Reloads the plugin configuration";
	}

}
