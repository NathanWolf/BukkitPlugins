package com.elmakers.mine.bukkit.plugins.spells;

import net.minecraft.server.EntitySheep;
import net.minecraft.server.World;

import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.Location;

import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;

public class FamiliarSpell extends Spell
{
	private String DEFAULT_FAMILIARS = "chicken,sheep,cow,pig";
	private List<String> defaultFamiliars = new ArrayList<String>();
	
	@Override
	public boolean onCast(String[] parameters)
	{
		Block target = getTargetBlock();
		if (target == null)
		{
			castMessage(player, "No target");
		}
		Location targetLocation = new Location(player.getWorld(), target.getX(), target.getY(), target.getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
		CraftPlayer craftPlayer = (CraftPlayer)player;
		CraftWorld craftWorld = (CraftWorld)craftPlayer.getWorld();
		World world = craftWorld.getHandle();
		EntitySheep sheep = new EntitySheep(world);

		((CraftWorld)player.getWorld()).getHandle().a(sheep);
		sheep.getBukkitEntity().teleportTo(targetLocation);
        
        
        
		return false;
	}

	@Override
	public String getName()
	{
		return "familiar";
	}

	@Override
	public String getCategory()
	{
		return "WIP";
	}

	@Override
	public String getDescription()
	{
		return "Create an animal familiar to follow you around";
	}

	@Override
	public void onLoad(PluginProperties properties)
	{
		maxRange = properties.getInteger("spells-blink-range", maxRange);
		allowAscend = properties.getBoolean("spells-blink-allow-ascend", allowAscend);
		allowDescend = properties.getBoolean("spells-blink-allow-ascend", allowDescend);
	}
	
}
