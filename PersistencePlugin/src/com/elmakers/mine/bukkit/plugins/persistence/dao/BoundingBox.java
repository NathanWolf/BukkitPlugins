package com.elmakers.mine.bukkit.plugins.persistence.dao;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.plugins.persistence.annotation.Persist;
import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistClass;

@PersistClass(schema="global", name="area", contained=true)
public class BoundingBox
{
	public BoundingBox()
	{
		
	}
	
	public BoundingBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
	{
		min = new Position(Math.min(minX, maxX), Math.min(minY, maxY), Math.min(minZ, maxZ));
		max = new Position(Math.max(minX, maxX), Math.max(minY, maxY), Math.max(minZ, maxZ));
	}
	
	public BoundingBox(Position min, Position max)
	{
		this.min = min;
		this.max = max;
	}
	
	public boolean contains(Position p)
	{
		return
		(
			(p.x >= min.x && p.x <= max.x)
		&&	(p.y >= min.y && p.y <= max.y)
		&&	(p.z >= min.z && p.z <= max.z)
		);
	}
	
	public Position getCenter()
	{
		return new Position
		(
			(int)Math.floor((min.x + max.x) / 2),
			(int)Math.floor((min.y + max.y) / 2),
			(int)Math.floor((min.z + max.z) / 2)
		);
	}
	
	public int getSizeX()
	{
		return max.x - min.x;
	}
	
	public int getSizeY()
	{
		return max.y - min.y;
	}
	
	public int getSizeZ()
	{
		return max.z - min.z;
	}
	
	public BoundingBox getFace(BlockFace face)
	{
		return getFace(face, 1, 0);
	}
	
	public BoundingBox getFace(BlockFace face, int thickness, int offset)
	{
		// Brute-force this for now. There's probably a Matrix-y way to do this!
		switch(face)
		{
			case UP: return new BoundingBox(min.x, max.y + offset, min.z, max.x, max.y + offset + thickness, max.z);
			case DOWN: return new BoundingBox(min.x, min.y - offset - thickness, min.z, max.x, min.y - offset, max.z);
			case WEST: return new BoundingBox(min.x, min.y, max.z + offset, max.x, max.y, max.z + offset + thickness);
			case EAST: return new BoundingBox(min.x, min.y, min.z - offset - thickness, max.x, max.y, min.z - offset);
			case SOUTH: return new BoundingBox(max.x + offset, min.y, min.z, max.x + offset + thickness, max.y, max.z);
			case NORTH: return new BoundingBox(min.x - offset - thickness, min.y, min.z, min.x - offset, max.y, max.z);
		}
		
		return null;
	}
	
	public void fill(World world, Material material)
	{
		fill(world, material, null);
	}
	
	public void fill(World world, Material material, HashMap<Material, ? extends Object> destructable)
	{
		for (int x = min.x; x < max.x; x++)
		{
			for (int y = min.y; y < max.y; y++)
			{
				for (int z = min.z; z < max.z; z++)
				{
					Block block = world.getBlockAt(x, y, z);
					
					if (destructable == null)
					{
						block.setType(material);
					}
					else
					{
						Material blockType = block.getType();
						if (destructable.get(blockType) != null)
						{
							block.setType(material);
						}
					}
				}
			}
		}
	}
	
	@Persist(contained=true)
	public Position getMin()
	{
		return min;
	}
	
	public void setMin(Position min)
	{
		this.min = min;
	}
	
	@Persist(contained=true)
	public Position getMax()
	{
		return max;
	}
	
	public void setMax(Position max)
	{
		this.max = max;
	}
	
	protected Position min;
	protected Position max;
}
