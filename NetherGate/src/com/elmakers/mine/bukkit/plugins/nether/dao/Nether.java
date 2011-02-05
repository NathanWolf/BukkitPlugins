package com.elmakers.mine.bukkit.plugins.nether.dao;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.plugins.persistence.annotation.Persist;
import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistClass;
import com.elmakers.mine.bukkit.plugins.persistence.dao.BoundingBox;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PlayerData;

@PersistClass(schema="nether", name="nether")
public class Nether
{
	public static int defaultSize = 64;
	public static int minHeight = 16;
	public static int maxHeight = 64;
	public static int floorPadding = 4;
	public static int ceilingPadding = 8;
	public static int defaultRatio = 16;
	public static int bedrockPadding = 1;
	public static int lavaPadding = 1;
	public static int lightstonePadding = 1;
	public static int maxSearch = 32;

	public static HashMap<Material, Boolean> destructible = null;
	
	public Nether()
	{
		if (destructible == null)
		{
			destructible = new HashMap<Material, Boolean>();
			destructible.put(Material.STONE, true);
			destructible.put(Material.GRASS, true);
			destructible.put(Material.DIRT, true);
			destructible.put(Material.COBBLESTONE, true);
			destructible.put(Material.SAND, true);
			destructible.put(Material.STONE, true);
			destructible.put(Material.GRAVEL, true);
			destructible.put(Material.WATER, true);
			destructible.put(Material.STATIONARY_WATER, true);
		}
	}
	
	public void create(World world)
	{
		// Clear Area
		int minZ = netherArea.getMin().getZ();
		int maxZ = netherArea.getMax().getZ();
		int minX = netherArea.getMin().getX();
		int maxX = netherArea.getMax().getX();
		int minY = netherArea.getMin().getY();
		int maxY = netherArea.getMax().getY();
		
		fill(world, Material.AIR, minX, minY, minZ, maxX, maxY, maxZ);
		
		// Lightstone ceiling
		minY = netherArea.getMax().getY();
		maxY = netherArea.getMax().getY() + lightstonePadding;
		fill(world, Material.GLOWSTONE, minX, minY, minZ, maxX, maxY, maxZ);

		// Bedrock ceiling
		minY = maxY;
		maxY = minY + bedrockPadding;
		fill(world, Material.BEDROCK, minX, minY, minZ, maxX, maxY, maxZ);

		// Netherrack padding
		minY = netherArea.getMin().getY() - floorPadding;
		maxY = netherArea.getMin().getY();
		fill(world, Material.NETHERRACK, minX, minY, minZ, maxX, maxY, maxZ);
		
		// Bedrock floor
		maxY = minY;
		minY = maxY - bedrockPadding;
		
		fill(world, Material.BEDROCK, minX, minY, minZ, maxX, maxY, maxZ);

		// TODO walls, lava
	}
	
	protected void fill(World world, Material material, int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
	{
		for (int x = minX; x < maxX; x++)
		{
			for (int y = minY; y < maxX; y++)
			{
				for (int z = minZ; z < maxZ; z++)
				{
					Block block = world.getBlockAt(x, y, z);
					Material blockType = block.getType();
					
					if (blockType == Material.AIR) continue;
					
					if (destructible.get(blockType) != null)
					{
						block.setType(material);
					}
				}
			}
		}
	}
	
	@Persist(id=true, auto=true)
	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	@Persist(contained=true)
	public BoundingBox getNetherArea()
	{
		return netherArea;
	}

	public void setNetherArea(BoundingBox netherArea)
	{
		this.netherArea = netherArea;
	}

	@Persist(contained=true)
	public BoundingBox getWorldArea()
	{
		return worldArea;
	}

	public void setWorldArea(BoundingBox worldArea)
	{
		this.worldArea = worldArea;
	}

	@Persist
	public int getRatio()
	{
		return ratio;
	}

	public void setRatio(int ratio)
	{
		this.ratio = ratio;
	}

	@Persist
	public PlayerData getOwner()
	{
		return owner;
	}

	public void setOwner(PlayerData owner)
	{
		this.owner = owner;
	}

	@Persist
	public List<Portal> getPortals()
	{
		return portals;
	}

	public void setPortals(List<Portal> portals)
	{
		this.portals = portals;
	}

	protected PlayerData	owner;
	protected List<Portal>	portals;
	protected BoundingBox	netherArea;
	protected BoundingBox	worldArea;

	protected int id;
	protected int ratio;
}
