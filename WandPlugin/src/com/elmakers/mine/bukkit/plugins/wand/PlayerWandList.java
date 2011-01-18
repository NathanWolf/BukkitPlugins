package com.elmakers.mine.bukkit.plugins.wand;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

public class PlayerWandList 
{
	private final List<Wand>	wands = new ArrayList<Wand>();
	private Wand				currentWand;
	private Player				player;
	private String				playerName;
	
	public void copyTo(PlayerWandList other)
	{
		other.clear();
		for (Wand wand : wands)
		{
			Wand newWand = other.addWand(wand.getName());
			wand.copyTo(newWand);
		}
		if (currentWand != null)
		{
			other.selectWand(currentWand.getName());
		}
	}
	
	public void clear()
	{
		currentWand = null;
		wands.clear();
	}
	
	public boolean isEmpty()
	{
		return wands.isEmpty();
	}
	
	public void setPlayer(Player player)
	{
		this.player = player;
		if (player != null)
		{
			this.playerName = player.getName();
		}
	}
	
	public Player getPlayer()
	{
		return player;
	}
	
	public void setPlayerName(String playerName)
	{
		this.playerName = playerName;
	}
	
	public String getPlayerName()
	{
		return playerName;
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
