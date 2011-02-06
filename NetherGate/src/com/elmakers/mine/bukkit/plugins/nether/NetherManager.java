package com.elmakers.mine.bukkit.plugins.nether;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.nether.dao.PortalArea;
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
		PortalArea nether = new PortalArea();
		
		int minX = location.getBlockX() - PortalArea.defaultSize / 2;
		int maxX = location.getBlockX() + PortalArea.defaultSize / 2;
		int minZ = location.getBlockZ() - PortalArea.defaultSize / 2;
		int maxZ = location.getBlockZ() + PortalArea.defaultSize / 2;
		int minY = PortalArea.defaultFloor + PortalArea.getFloorPadding();
		int maxY = minY + PortalArea.minHeight;
		
		int limitY = location.getBlockY() - PortalArea.getCeilingPadding();
		
		if (maxY > limitY)
		{
			return false;
		}
		
		while (maxY < limitY && maxY - minY < PortalArea.maxHeight)
		{
			maxY++;
		}
		
		BoundingBox area = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
		nether.setInternalArea(area);
		
		int ratio = PortalArea.defaultRatio;
		
		minY = 0;
		maxY = 128;
		minX = location.getBlockX() - PortalArea.defaultSize * ratio / 2;
		maxX = location.getBlockX() + PortalArea.defaultSize * ratio / 2;
		minZ = location.getBlockZ() - PortalArea.defaultSize * ratio / 2;
		maxZ = location.getBlockZ() + PortalArea.defaultSize * ratio / 2;
		
		area = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
		nether.setExternalArea(area);
		
		nether.setOwner(persistence.get(player.getName(), PlayerData.class));
		nether.setRatio(ratio);
		
		nether.create(player.getWorld());
		addToMap(nether);
		
		netherAreas.add(nether);
		persistence.put(nether);
		
		return true;
	}
	
	public void addToMap(PortalArea nether)
	{
		Chunk chunk = nether.getInternalArea().getCenter().getChunk(world);
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
		
		persistence.getAll(netherAreas, PortalArea.class);
		for (PortalArea nether : netherAreas)
		{
			addToMap(nether);
		}
	}
	
	public PortalArea getNether(Position position)
	{
		if (world == null || position == null) return null;
		
		Chunk chunk = position.getChunk(world);
		NetherList list = netherMap.get(chunk);
		if (list == null) return null;
		
		for (PortalArea nether : list)
		{
			if (nether.getExternalArea().contains(position))
			{
				return nether;
			}
		}
		
		return null;
	}

	protected HashMap<Chunk, NetherList> netherMap = new HashMap<Chunk, NetherList>();
	protected List<PortalArea>	netherAreas	= new ArrayList<PortalArea>();
	protected World			world;
	protected Persistence	persistence;
	protected Messaging		messaging;
}
