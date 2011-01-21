package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.util.Vector;

public class ArrowSpell extends Spell
{
	
	private final float ARROW_SPEED = 1.5f;
	private final float ARROW_SPREAD = 0.0f;

	@Override
	public boolean onCast(String[] parameters)
	{
		Location location = getSpawnLocation();
		Vector velocity = getAimVector();
		Arrow arrow = player.getWorld().spawnArrow(location, velocity, ARROW_SPEED, ARROW_SPREAD);
		if (arrow == null)
		{
			castMessage(player, "Your arrow fizzled");
		}
		else
		{
			castMessage(player, "You fire a magical arrow");
		}
		return arrow != null;
	}

	@Override
	public String getName()
	{
		return "arrow";
	}

	@Override
	public String getCategory()
	{
		return "combat";
	}

	@Override
	public String getDescription()
	{
		return "Throws a magic arrow";
	}

}
