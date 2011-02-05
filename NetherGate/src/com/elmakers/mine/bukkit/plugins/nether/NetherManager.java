package com.elmakers.mine.bukkit.plugins.nether;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.nether.dao.Nether;
import com.elmakers.mine.bukkit.plugins.persistence.Messaging;
import com.elmakers.mine.bukkit.plugins.persistence.Persistence;
import com.elmakers.mine.bukkit.plugins.persistence.dao.BoundingBox;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PlayerData;

public class NetherManager
{
	
	public void initialize(Persistence persistence, Messaging messaging)
	{
		this.messaging = messaging;
		this.persistence = persistence;
		load();
	}
	
	public boolean create(Player player)
	{
		Location location = player.getLocation();
		World world = player.getWorld();
		Nether nether = new Nether();
		
		Block topBlock = world.getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
		
		for (int i = 0; i < Nether.maxSearch; i++)
		{
			if (topBlock.getType() != Material.WATER && topBlock.getType() != Material.STATIONARY_WATER && topBlock.getType() != Material.AIR) break;
			topBlock = topBlock.getFace(BlockFace.DOWN);
		}
		
		for (int i = 0; i < Nether.ceilingPadding; i++)
		{
			topBlock = topBlock.getFace(BlockFace.DOWN);
			if (topBlock.getType() == Material.BEDROCK) return false;
		}
		
		Block bottomBlock = topBlock;
		
		for (int i = 0; i < Nether.minHeight; i++)
		{
			bottomBlock = bottomBlock.getFace(BlockFace.DOWN);
			if (bottomBlock.getType() == Material.BEDROCK) return false;			
		}
		
		for (int i = 0; i < Nether.maxHeight; i++)
		{
			bottomBlock = bottomBlock.getFace(BlockFace.DOWN);
			if (bottomBlock.getType() == Material.BEDROCK)
			{
				bottomBlock = bottomBlock.getFace(BlockFace.UP);
				break;
			}
		}
		
		int minY = topBlock.getY() - Nether.bedrockPadding - Nether.ceilingPadding - Nether.lightstonePadding;
		int maxY = bottomBlock.getY() + Nether.bedrockPadding + Nether.floorPadding;
		int minX = location.getBlockX() - Nether.defaultSize / 2;
		int maxX = location.getBlockX() + Nether.defaultSize / 2;
		int minZ = location.getBlockZ() - Nether.defaultSize / 2;
		int maxZ = location.getBlockZ() + Nether.defaultSize / 2;
		
		BoundingBox area = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
		nether.setNetherArea(area);
		
		int ratio = Nether.defaultRatio;
		
		minY = 0;
		maxY = 128;
		minX = location.getBlockX() - Nether.defaultSize * ratio / 2;
		maxX = location.getBlockX() + Nether.defaultSize * ratio / 2;
		minZ = location.getBlockZ() - Nether.defaultSize * ratio / 2;
		maxZ = location.getBlockZ() + Nether.defaultSize * ratio / 2;
		
		nether.setOwner(persistence.get(player.getName(), PlayerData.class));
		nether.setRatio(ratio);
		
		nether.create(player.getWorld());
		
		netherAreas.add(nether);
		persistence.put(nether);
		
		player.teleportTo(new Location(world, location.getX(), minY + 1, location.getZ(), location.getYaw(), location.getPitch()));
		
		return true;
	}
	
	public void load()
	{
		persistence.getAll(netherAreas, Nether.class);
	}

	protected List<Nether>	netherAreas	= new ArrayList<Nether>();
	protected Persistence	persistence;
	protected Messaging		messaging;
}
