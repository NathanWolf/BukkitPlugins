package com.elmakers.mine.bukkit.plugins.nether.dao;

import java.util.Date;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

import com.elmakers.mine.bukkit.gameplay.CSVParser;
import com.elmakers.mine.bukkit.gameplay.dao.BlockList;
import com.elmakers.mine.bukkit.gameplay.dao.BoundingBox;
import com.elmakers.mine.bukkit.gameplay.dao.MaterialList;
import com.elmakers.mine.bukkit.persistence.annotation.PersistClass;
import com.elmakers.mine.bukkit.persistence.annotation.PersistField;
import com.elmakers.mine.bukkit.persistence.dao.LocationData;
import com.elmakers.mine.bukkit.persistence.dao.Orientation;
import com.elmakers.mine.bukkit.plugins.nether.NetherManager;
import com.elmakers.mine.craftbukkit.persistence.Persistence;

@PersistClass(schema="nether", name="portal")
public class Portal
{
	public Portal()
	{
		
	}

	public Portal(Player creator, Location location, PortalType portalType, NetherManager manager)
	{
		initialize(manager);
		Persistence persistence = Persistence.getInstance();
		this.location = new LocationData(location);
		
		// Match the player's facing
		this.location.updateOrientation(creator.getLocation());
		
		this.creator = persistence.get(creator.getName(), NetherPlayer.class);
		this.active = true;
		this.updatePending = false;
		this.type = portalType;
	}
	
	public void use(NetherPlayer player)
	{
		
	}
	
	public void initialize(NetherManager manager)
	{
		this.manager = manager;
		if (destructible == null)
		{
			destructible = new MaterialList();
			needsPlatform = new MaterialList();

			needsPlatform.add(Material.WATER);
			needsPlatform.add(Material.STATIONARY_WATER);
			needsPlatform.add(Material.LAVA);
			needsPlatform.add(Material.STATIONARY_LAVA);
		
			destructible = CSVParser.parseMaterials(DEFAULT_DESTRUCTIBLES);
		}
	}
	
	@PersistField(id=true, auto=true)
	public int getId()
	{
		return id;
	}
	
	public void setId(int id)
	{
		this.id = id;
	}
	
	@PersistField
	public PortalArea getContainer()
	{
		return container;
	}
	
	public void setContainer(PortalArea container)
	{
		this.container = container;
	}
	
	@PersistField
	public Portal getTarget()
	{
		return target;
	}
	
	public void setTarget(Portal target)
	{
		this.target = target;
	}

	@PersistField
	public NetherPlayer getCreator()
	{
		return creator;
	}

	public void setCreator(NetherPlayer creator)
	{
		this.creator = creator;
	}

	@PersistField
	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

	@PersistField
	public boolean isUpdatePending()
	{
		return updatePending;
	}

	public void setUpdatePending(boolean updatePending)
	{
		this.updatePending = updatePending;
	}

	@PersistField
	public Date getLastUsed()
	{
		return lastUsed;
	}

	public void setLastUsed(Date lastUsed)
	{
		this.lastUsed = lastUsed;
	}
	
	@PersistField
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@PersistField(contained=true)
	public LocationData getLocation()
	{
		return location;
	}

	public void setLocation(LocationData location)
	{
		this.location = location;
	}

	@PersistField
	public PortalType getType()
	{
		return type;
	}

	public void setType(PortalType type)
	{
		this.type = type;
	}
	
	@PersistField
	public NetherPlayer getLastUsedBy()
	{
		return lastUsedBy;
	}

	public void setLastUsedBy(NetherPlayer lastUsedBy)
	{
		this.lastUsedBy = lastUsedBy;
	}
	
	// TODO : get this working!
	//@PersistField(contained=true)
	public BlockList getBlocks()
	{
		return blocks;
	}

	public void setBlocks(BlockList blocks)
	{
		this.blocks = blocks;
	}
	
	public void build(boolean fillAir)
	{
		blocks = new BlockList();
		build(fillAir, blocks);
	}
	
	public void build(boolean fillAir, BlockList blockList)
	{
		World world = location.getWorld();
		Location loc = location.getLocation();
		Block centerBlock = world.getBlockAt(loc);
		Orientation orientation = location.getOrientation();
		BlockFace facing = orientation.getYaw() == 0 ? BlockFace.NORTH : BlockFace.WEST;
		if (fillAir)
		{
			clearPortalArea(centerBlock, blockList);
		}
	
		if (type.hasFrame())
		{
			buildFrame(centerBlock, facing, blockList);
		}
		
		if (type.hasPlatform())
		{
			buildPlatform(centerBlock, blockList);
		}
		
		if (type.hasPortal())
		{
			buildPortalBlocks(centerBlock, facing, blockList);
		}
	}
	
	protected void buildPortalBlocks(Block centerBlock, BlockFace facing, BlockList blockList)
	{
		manager.disablePhysics();
		BoundingBox container = new BoundingBox(centerBlock.getX() - 1, centerBlock.getY(), centerBlock.getZ() - 1,
				centerBlock.getX() + 1, centerBlock.getY() + 3, centerBlock.getZ());
		
		container.fill(centerBlock.getWorld(), Material.PORTAL, destructible, blockList);
	}
	
	protected static void buildFrame(Block centerBlock, BlockFace facing, BlockList blockList)
	{
		BoundingBox leftSide = new BoundingBox(centerBlock.getX() - 2, centerBlock.getY() - 1, centerBlock.getZ() - 1,
				centerBlock.getX() - 1, centerBlock.getY() + 4, centerBlock.getZ());
		BoundingBox rightSide = new BoundingBox(centerBlock.getX() + 2, centerBlock.getY() - 1, centerBlock.getZ() - 1,
				centerBlock.getX() + 1, centerBlock.getY() + 4, centerBlock.getZ());
		BoundingBox top = new BoundingBox(centerBlock.getX() - 1, centerBlock.getY() + 3, centerBlock.getZ() - 1,
				centerBlock.getX() + 1, centerBlock.getY() + 4, centerBlock.getZ());
		BoundingBox bottom = new BoundingBox(centerBlock.getX() - 1, centerBlock.getY() - 1, centerBlock.getZ() - 1,
				centerBlock.getX() + 1, centerBlock.getY(), centerBlock.getZ());
		
		leftSide.fill(centerBlock.getWorld(), Material.OBSIDIAN, destructible, blockList);
		rightSide.fill(centerBlock.getWorld(), Material.OBSIDIAN, destructible, blockList);
		top.fill(centerBlock.getWorld(), Material.OBSIDIAN, destructible, blockList);
		bottom.fill(centerBlock.getWorld(), Material.OBSIDIAN, destructible, blockList);
	}
	
	public static void clearPortalArea(Block centerBlock, BlockList blockList)
	{
		BoundingBox container = new BoundingBox(centerBlock.getX() - 3, centerBlock.getY(), centerBlock.getZ() - 3,
				centerBlock.getX() + 2, centerBlock.getY() + 4, centerBlock.getZ() + 2);
		
		container.fill(centerBlock.getWorld(), Material.AIR, destructible, blockList);
	}
	
	public static void buildPlatform(Block centerBlock, BlockList blockList)
	{
		BoundingBox platform = new BoundingBox(centerBlock.getX() - 3, centerBlock.getY() - 1, centerBlock.getZ() - 3,
				centerBlock.getX() + 2, centerBlock.getY(), centerBlock.getZ() + 2);
		
		platform.fill(centerBlock.getWorld(), Material.OBSIDIAN, needsPlatform, blockList);
	}
	
	public BoundingBox getBoundingBox()
	{
		BlockVector position = location.getPosition();
		Orientation orientation = location.getOrientation();
		if (orientation.getYaw() == 0)
		{
			BlockVector min = new BlockVector(position.getBlockX(), position.getBlockY(), position.getBlockZ());
			BlockVector max = new BlockVector(position.getBlockX() + 1, position.getBlockY(), position.getBlockZ());
			return new BoundingBox(min, max);
		}

		BlockVector min = new BlockVector(position.getBlockX(), position.getBlockY(), position.getBlockZ());
		BlockVector max = new BlockVector(position.getBlockX(), position.getBlockY(), position.getBlockZ() + 1);
		return new BoundingBox(min, max);
	}
	
	protected int			id;
	protected LocationData 	location;
	protected PortalArea	container;
	protected String		name;
	protected boolean		active;
	protected boolean		updatePending;
	protected Date			lastUsed;
	protected Portal		target;
	protected NetherPlayer	creator;
	protected NetherPlayer	lastUsedBy;
	protected PortalType	type;
	protected BlockList		blocks;

	// transient
	protected NetherManager			manager;
	protected static final String	DEFAULT_DESTRUCTIBLES	= "0,1,2,3,4,10,11,12,13,14,15,16,21,51,56,78,79,82,87,88,89";
	protected static MaterialList	destructible			= null;
	protected static MaterialList	needsPlatform			= null;
}
