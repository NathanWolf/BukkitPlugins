package com.elmakers.mine.bukkit.plugins.nether.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.plugins.persistence.annotation.Persist;
import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistClass;
import com.elmakers.mine.bukkit.plugins.persistence.dao.BoundingBox;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PlayerData;

@PersistClass(schema="nether", name="nether")
public class Nether
{
	public static int defaultFloor = 4;
	public static int defaultSize = 128;
	public static int minHeight = 32;
	public static int maxHeight = 64;
	public static int floorPadding = 4;
	public static int poolPadding = 4;
	public static int ceilingPadding = 4;
	public static int aboveGroundPadding = 16;
	public static int defaultRatio = 16;
	public static int bedrockPadding = 1;
	public static int lavaPadding = 1;
	public static int maxSearch = 32;
	public static int emptyBuffer = 8;
	public static int lightstoneHeight = 3;

	// heightmap config
	public static int floorMaxVariance = 8;
	public static int ceilingMaxVariance = 16;
	public static int ceilingPercentChange = 25;
	public static int floorPercentChange = 5;
	public static int poolSize = 4;
	
	public static HashMap<Material, Boolean> destructable = null;
	
	private static final Random random = new Random();

	public static int getFloorPadding()
	{
		return floorPadding + bedrockPadding + poolPadding;
	}
	
	public static int getCeilingPadding()
	{
		return ceilingPadding + bedrockPadding + lightstoneHeight + aboveGroundPadding;
	}
	
	public Nether()
	{
		if (destructable == null)
		{
			destructable = new HashMap<Material, Boolean>();
			destructable.put(Material.STONE, true);
			destructable.put(Material.GRASS, true);
			destructable.put(Material.DIRT, true);
			destructable.put(Material.COBBLESTONE, true);
			destructable.put(Material.SAND, true);
			destructable.put(Material.STONE, true);
			destructable.put(Material.GRAVEL, true);
			destructable.put(Material.WATER, true);
			destructable.put(Material.STATIONARY_WATER, true);
			destructable.put(Material.COAL_ORE, true);
			destructable.put(Material.DIAMOND_ORE, true);
			destructable.put(Material.LAPIS_ORE, true);
			destructable.put(Material.REDSTONE_ORE, true);
			destructable.put(Material.GOLD_ORE, true);
			destructable.put(Material.NETHERRACK, true);
			destructable.put(Material.GLOWSTONE, true);
			destructable.put(Material.IRON_ORE, true);
		}
	}
	
	public void create(World world)
	{
		// Create bedrock box
		BlockFace[] box = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.DOWN, BlockFace.UP};
		for (BlockFace face : box)
		{
			BoundingBox faceArea = netherArea.getFace(face, bedrockPadding, 1);
			faceArea.fill(world, Material.BEDROCK, destructable);
		}

		// Create lava walls
		BlockFace[] walls = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
		for (BlockFace face : walls)
		{
			BoundingBox faceArea = netherArea.getFace(face, lavaPadding, 1 - bedrockPadding);
			faceArea.fill(world, Material.STATIONARY_LAVA, destructable);
		}
		
		// Create netherrack ceiling
		BoundingBox ceiling = netherArea.getFace(BlockFace.UP, ceilingPadding, 1 - bedrockPadding);
		ceiling.fill(world, Material.NETHERRACK);
		
		// Create netherrack floor
		BoundingBox floor = netherArea.getFace(BlockFace.DOWN, floorPadding, 1 - bedrockPadding);
		floor.fill(world, Material.NETHERRACK);

		// Create heightmaps
		ceiling = netherArea.getFace(BlockFace.UP, ceilingMaxVariance, 1 - bedrockPadding - ceilingPadding);
		// Leave room for lava
		ceiling.getMax().setX(ceiling.getMax().getX() - 1);
		ceiling.getMax().setZ(ceiling.getMax().getZ() - 1);
		ceiling.getMin().setX(ceiling.getMin().getX() - 1);
		ceiling.getMin().setZ(ceiling.getMin().getZ() - 1);
		byte[][] ceilingMap = generateHeightMap(ceiling, ceilingPercentChange);
		
		floor = netherArea.getFace(BlockFace.DOWN, floorMaxVariance, 1 - bedrockPadding - floorPadding);
		byte[][] floorMap = generateHeightMap(ceiling, floorPercentChange);
		
		int yOffset = floor.getMin().getY();
		int xOffset = floor.getMin().getX();
		int zOffset = floor.getMin().getZ();
		
		// Fill interior
		int xSize = ceiling.getSizeX();
		int zSize = ceiling.getSizeZ();
		int ySize = netherArea.getSizeY();
		for (int mapX = 0; mapX < xSize; mapX++)
		{
			for (int mapZ = 0; mapZ < zSize; mapZ++)
			{
				for (int dY = ySize; dY >=0; dY--)
				{
					Block block = world.getBlockAt(xOffset + mapX, yOffset + dY, zOffset + mapZ);
					if (destructable.get(block.getType()) == null) continue;
					
					// Create lava pools
					if (block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA)
					{
						createPool(floorMap, mapX, mapZ);
					}
					
					if (dY < floorMaxVariance && dY < floorMap[mapX][mapZ])
					{
						block.setType(Material.NETHERRACK);
						continue;				
					}
					
					if (dY > ySize - ceilingMaxVariance && dY > ySize - ceilingMap[mapX][mapZ])
					{
						if (ySize - dY > ceilingMaxVariance - lightstoneHeight)
						{
							block.setType(Material.GLOWSTONE);
						}
						else
						{
							block.setType(Material.NETHERRACK);
						}
						continue;
					}
					
					block.setType(Material.AIR);
				}
			}
		}	
	}
	
	protected void createPool(byte[][] map, int mapX, int mapZ)
	{
		// Only really need to go forward, since the old parts of the heightmap don't matter.
		int maxHeight = poolSize * poolSize;
		float ratio = (float)poolPadding / maxHeight;
		for (int x = 0; x < poolSize; x++)
		{
			for (int z = 0; z < poolSize; z++)
			{
				map[x][z] = (byte)(ratio * x * z);
			}
		}
	}
	
	protected byte[][] generateHeightMap(BoundingBox area, int percentChange)
	{
		int zSize = area.getSizeZ();
		int xSize = area.getSizeX();
		int ySize = area.getSizeY();
		byte[][] heightMap = new byte[xSize][zSize];
		
		// Start out somewhere random:
		heightMap[0][0] = (byte)random.nextInt(ySize);
		for (int x = 0; x < xSize; x++)
		{
			for (int z = 0; z < zSize; z++)
			{
				byte current = heightMap[x][z];
				
				if (x > 0 && z > 0 && x < xSize - 2)
				{
					current = (byte)((current + heightMap[x - 1][z]  + heightMap[x][z - 1]  + heightMap[x + 1][z]) / 4);
				}
				else if (x > 0 && z > 0)
				{
					current = (byte)((current + heightMap[x - 1][z]  + heightMap[x][z - 1]) / 3);
				}
				else if (x > 0)
				{
					current = (byte)((current + heightMap[x - 1][z]) / 2);					
				}
				else if (z > 0)
				{
					current = (byte)((current + heightMap[x][z - 1]) / 2);					
				}
				
				int percent = random.nextInt(100);
				if (percentChange > percent)
				{
					if (current >= ySize) current--;
					else if (current == 0) current++;
					else if ((percent % 2) == 0) current++;
					else current--;
				}
				
				if (x < xSize - 2)
				{
					heightMap[x + 1][z] = current;
				}
				if (z < zSize - 2)
				{
					heightMap[x][z + 1] = current;
				}
			}
		}
			
		return heightMap;
	}
	
	protected void wall(World world, Material material, BlockFace side)
	{
		
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
	public List<Portal> getNetherPortals()
	{
		return netherPortals;
	}

	public void setNetherPortals(List<Portal> portals)
	{
		this.netherPortals = portals;
	}

	@Persist
	public List<Portal> geWorldtPortals()
	{
		return worldPortals;
	}

	public void setWorldPortals(List<Portal> portals)
	{
		this.worldPortals = portals;
	}

	protected PlayerData	owner;
	protected List<Portal>	netherPortals;
	protected List<Portal>	worldPortals;
	protected BoundingBox	netherArea;
	protected BoundingBox	worldArea;

	protected int id;
	protected int ratio;
}
