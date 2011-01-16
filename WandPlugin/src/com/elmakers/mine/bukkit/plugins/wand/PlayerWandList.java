package com.elmakers.mine.bukkit.plugins.wand;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

public class PlayerWandList 
{
	private final List<Wand>	wands = new ArrayList<Wand>();
	private Wand				currentWand;
	private Player				player;
	
	public void setPlayer(Player player)
	{
		this.player = player;
	}
	
	public Player getPlayer()
	{
		return player;
	}
	
	public Wand getCurrentWand()
	{
		return currentWand;
	}
	
	public final List<Wand> getWands()
	{
		return wands;
	}
	
	public Wand addWand(String name)
	{
		Wand newWand = new Wand();
		newWand.setName(name);
		wands.add(newWand);
		currentWand = newWand;
		return newWand;
	}
	
	public boolean removeWand(String name)
	{
		Wand foundWand = null;
		for (Wand lookWand : wands)
		{
			if (lookWand.getName().equalsIgnoreCase(name))
			{
				foundWand = lookWand;
				wands.remove(foundWand);
				break;
			}
		}
		return (foundWand != null);
	}
	
	public void selectWand(String name)
	{
		if (wands.size() > 0)
		{
			currentWand = wands.get(0);
		}
		for (Wand lookWand : wands)
		{
			if (lookWand.getName().equalsIgnoreCase(name))
			{
				currentWand = lookWand;
				break;
			}
		}
	}
	
	public void nextWand()
	{
		if (currentWand != null)
		{
			int index = wands.indexOf(currentWand);
			index = (index + 1) % wands.size();
			currentWand = wands.get(index);
		}
	}
}
