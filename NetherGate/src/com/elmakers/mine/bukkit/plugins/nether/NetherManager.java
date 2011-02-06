package com.elmakers.mine.bukkit.plugins.nether;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.nether.dao.Nether;
import com.elmakers.mine.bukkit.plugins.persistence.Messaging;
import com.elmakers.mine.bukkit.plugins.persistence.Persistence;
import com.elmakers.mine.bukkit.plugins.persistence.dao.BoundingBox;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PlayerData;
import com.elmakers.mine.bukkit.plugins.persistence.dao.Position;

public class NetherManager
{
	
	public void initialize(Persistence persistence, Messaging messaging)
	{
		this.messaging = messaging;
		this.persistence = persistence;
	}
	
	public boolean create(Player player)
	{
		Location location = player.getLocation();
		Nether nether = new Nether();
		
		int minX = location.getBlockX() - Nether.defaultSize / 2;
		int maxX = location.getBlockX() + Nether.defaultSize / 2;
		int minZ = location.getBlockZ() - Nether.defaultSize / 2;
		int maxZ = location.getBlockZ() + Nether.defaultSize / 2;
		int minY = Nether.defaultFloor + Nether.getFloorPadding();
		int maxY = minY + Nether.minHeight;
		
		int limitY = location.getBlockY() - Nether.getCeilingPadding();
		
		if (maxY > limitY)
		{
			return false;
		}
		
		while (maxY < limitY && maxY - minY < Nether.maxHeight)
		{
			maxY++;
		}
		
		BoundingBox area = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
		nether.setNetherArea(area);
		
		int ratio = Nether.defaultRatio;
		
		minY = 0;
		maxY = 128;
		minX = location.getBlockX() - Nether.defaultSize * ratio / 2;
		maxX = location.getBlockX() + Nether.defaultSize * ratio / 2;
		minZ = location.getBlockZ() - Nether.defaultSize * ratio / 2;
		maxZ = location.getBlockZ() + Nether.defaultSize * ratio / 2;
		
		area = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
		nether.setWorldArea(area);
		
		nether.setOwner(persistence.get(player.getName(), PlayerData.class));
		nether.setRatio(ratio);
		
		nether.create(player.getWorld());
		addToMap(nether);
		
		netherAreas.add(nether);
		persistence.put(nether);
		
		return true;
	}
	
	public void addToMap(Nether nether)
	{
		Chunk chunk = nether.getNetherArea().getCenter().getChunk(world);
		NetherList list = netherMap.get(chunk);
		if (list == null)
		{
			list = new NetherList();
			netherMap.put(chunk, list);
		}
		list.add(nether);
	}
	
	public void load(World w)
	{
		if (world != null)
		{
			return;
		}
		world = w;
		if (world == null) return;
		
		persistence.getAll(netherAreas, Nether.class);
		for (Nether nether : netherAreas)
		{
			addToMap(nether);
		}
	}
	
	public Nether getNether(Position position)
	{
		if (world == null || position == null) return null;
		
		Chunk chunk = position.getChunk(world);
		NetherList list = netherMap.get(chunk);
		if (list == null) return null;
		
		for (Nether nether : list)
		{
			if (nether.getWorldArea().contains(position))
			{
				return nether;
			}
		}
		
		return null;
	}

	protected HashMap<Chunk, NetherList> netherMap = new HashMap<Chunk, NetherList>();
	protected List<Nether>	netherAreas	= new ArrayList<Nether>();
	protected World			world;
	protected Persistence	persistence;
	protected Messaging		messaging;
}
