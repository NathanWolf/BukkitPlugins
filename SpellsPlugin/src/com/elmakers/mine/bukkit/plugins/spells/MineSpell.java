package com.elmakers.mine.bukkit.plugins.spells;

public class MineSpell extends Spell
{

	@Override
	public boolean onCast(String[] parameters)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName()
	{
		return "mine";
	}

	@Override
	public String getCategory()
	{
		return "Mining";
	}

	@Override
	public String getDescription()
	{
		return "Mines and drops the targeted resources";
	}

}
