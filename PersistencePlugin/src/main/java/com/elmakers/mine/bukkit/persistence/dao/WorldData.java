package com.elmakers.mine.bukkit.persistence.dao;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.util.BlockVector;

import com.elmakers.mine.bukkit.persistence.annotation.PersistClass;
import com.elmakers.mine.bukkit.persistence.annotation.PersistField;

@PersistClass(schema="global", name="world")
public class WorldData extends Persisted
{
	public WorldData()
	{
		
	}
	
	public WorldData(String name, Environment type)
	{
		this.name = name;
		setEnvironmentType(type);
	}
	
	public void update(World world)
	{
		this.world = world;
		
		name = world.getName();
		id = world.getId();
		Location location = world.getSpawnLocation();
		spawn = new BlockVector(location.getBlockX(), location.getBlockY(), location.getBlockZ());
		setEnvironmentType(world.getEnvironment());
	}
	
	public World getWorld()
	{
		if (world != null) return world;
		if (persistedClass == null) return null;
		
		Server server = persistedClass.getServer();
		if (server == null) return null;
		
		List<World> worlds = server.getWorlds();
		for (World checkWorld : worlds)
		{
			if (checkWorld.getName().equalsIgnoreCase(name))
			{
				this.world = checkWorld;
				return world;
			}
		}
		
		world = server.createWorld(name, getEnvironmentType());
		return world;
	}
	
	@PersistField(id=true)
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	@PersistField(contained=true)
	public BlockVector getSpawn()
	{
		return spawn;
	}
	
	public void setSpawn(BlockVector spawn)
	{
		this.spawn = spawn;
	}
	
	@PersistField
	public Environment getEnvironmentType()
	{
		return environmentType;
	}
	
	public void setEnvironmentType(Environment environmentType)
	{
		this.environmentType = environmentType;
	}

	protected String		name;
	protected long			id;
	protected BlockVector	spawn;
	protected Environment	environmentType;
	
	protected World			world;
}
