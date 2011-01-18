package com.elmakers.mine.bukkit.plugins.spells;

import net.minecraft.server.WorldServer;

import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;

import com.elmakers.mine.bukkit.utilities.PluginProperties;

public class BlinkSpell extends Spell
{
	private int maxRange = 0;
	
	public String getName()
	{
		return "blink";
	}
	
	public String getDescription()
	{
		return "Teleport to your target";
	}
	
	@Override
	public boolean onCast(String[] parameters)
	{
		Block target = getTargetBlock();
		Block face = getLastBlock();
		
		if (target == null) 
		{
			player.sendMessage("Nowhere to blink to");
			return false;
		}
		if (maxRange > 0 && getDistance(player,target) > maxRange) 
		{
			player.sendMessage("Can't blink that far");
			return false;
		}
		
		World world = player.getWorld();
    	WorldServer server = ((CraftWorld)world).getHandle();
    	CraftWorld craftWorld = server.getWorld();
		
		Block oneUp = craftWorld.getBlockAt(target.getX() ,target.getY() + 1, target.getZ());
		Block twoUp = craftWorld.getBlockAt(target.getX() ,target.getY() + 2, target.getZ());
		if 
		(
			oneUp.getType() == Material.AIR
		&&  twoUp.getType() == Material.AIR
		) 
		{
			player.sendMessage("Blink!");
			player.teleportTo
			(
				new org.bukkit.Location
				(
					world,
					face.getX() + 0.5,
					face.getY(),
					face.getZ() + 0.5,
					player.getLocation().getYaw(),
					player.getLocation().getPitch()
				)
			);
			return true;
		}
		else 
		{
			// no place to stand
			player.sendMessage("Nowhere to stand there");
			return false;
		}
	}

	public double getDistance(Player player, Block target) 
	{
		Location loc = player.getLocation();
		return Math.sqrt
		(
				Math.pow(loc.getX() - target.getX(), 2) 
		+ 		Math.pow(loc.getY() - target.getY(), 2) 
		+ 		Math.pow(loc.getZ() - target.getZ(), 2)
		);
	}
	
	@Override
	public String getCategory() 
	{
		return "movement";
	}

	@Override
	public void load(PluginProperties properties)
	{
		maxRange = properties.getInteger("spells-blink-range", maxRange);
	}
}
