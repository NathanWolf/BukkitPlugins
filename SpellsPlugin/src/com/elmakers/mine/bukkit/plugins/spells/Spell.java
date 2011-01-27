package com.elmakers.mine.bukkit.plugins.spells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;

import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;

/**
 * 
 * Base class for spells. Handles finding player location, targeting, and other
 * common spell activities.
 * 
 * Original targeting code ported from: HitBlox.java, Ho0ber@gmail.com 
 *
 */
public abstract class Spell implements Comparable<Spell>
{	
	/*
	 * protected members that are helpful to use
	 */
	protected Player						player;
	protected Spells						spells;
	
	/*
	 * Spell abstract interface- you must override these.
	 */
	public abstract boolean onCast(String[] parameters);

	protected abstract String getName();

	public abstract String getCategory();

	public abstract String getDescription();
	
	public abstract Material getMaterial();

	/*
	 * Spell optional interface- you may override these.
	 */
	public void onLoad(PluginProperties properties)
	{

	}

	public void onCancel()
	{

	}
	
	/*
	 *  Listener methods- you must register to receive these
	 */
	public void onPlayerMove(PlayerMoveEvent event)
	{
		
	}
	
	public void onMaterialChoose(Player player)
	{
		
	}
	
	public void onPlayerQuit(PlayerEvent event)
	{

	}
	
	public void onPlayerDeath(Player player, EntityDeathEvent event)
	{

	}
	
	/*
	 * Constructor - override to add additional spell variants
	 */
	public Spell()
	{
		variants.add(new SpellVariant(this));
	}
	
	/*
	 * General helper functions
	 */
	protected ItemStack getBuildingMaterial()
	{
		return getBuildingMaterial(false);
	}
	
	protected ItemStack getBuildingMaterial(boolean allowAir)
	{
		ItemStack result = null;
		List<Material> buildingMaterials = spells.getBuildingMaterials();
		Inventory inventory = player.getInventory();
		ItemStack[] contents = inventory.getContents();
		for (int i = 0; i < 9; i++)
		{
			if (contents[i] == null) break;
			Material candidate = contents[i].getType();
			if ((allowAir && candidate == Material.AIR) || buildingMaterials.contains(candidate))
			{
				result = contents[i];
				break;
			}
		}
		return result;
	}

	/*
	 * Targeting modification functions
	 */
	protected void targetThrough(Material mat)
	{
		targetThroughMaterials.put(mat, true);
	}

	protected void noTargetThrough(Material mat)
	{
		targetThroughMaterials.put(mat, false);
	}
	
	protected boolean isTargetable(Material mat)
	{
		Boolean checkMat = targetThroughMaterials.get(mat);
		if (reverseTargeting)
		{
			return(checkMat != null && checkMat);
		}
		return (checkMat == null || !checkMat);
	}

	protected void setReverseTargeting(boolean reverse)
	{
		reverseTargeting = reverse;
	}
	
	protected boolean isReverseTargeting()
	{
		return reverseTargeting;
	}
	
	protected void setTargetHeightRequired(int height)
	{
		targetHeightRequired = height;
	}
	
	protected int getTargetHeightRequired()
	{
		return targetHeightRequired;
	}
	
	/*
	 * Ground / location search and test function functions
	 */
	protected boolean isOkToStandIn(Material mat)
	{
		return (mat == Material.AIR || mat == Material.WATER || mat == Material.STATIONARY_WATER);
	}

	protected boolean isOkToStandOn(Material mat)
	{
		return (mat != Material.AIR && mat != Material.LAVA && mat != Material.STATIONARY_LAVA);
	}
	
	protected Location findPlaceToStand(Location playerLoc, boolean goUp)
	{
		int step;
		if (goUp)
		{
			step = 1;
		}
		else
		{
			step = -1;
		}

		// get player position
		int x = (int) Math.round(playerLoc.getX() - 0.5);
		int y = (int) Math.round(playerLoc.getY() + step + step);
		int z = (int) Math.round(playerLoc.getZ() - 0.5);

		World world = player.getWorld();

		// search for a spot to stand
		while (2 < y && y < 255)
		{
			Block block = world.getBlockAt(x, y, z);
			Block blockOneUp = world.getBlockAt(x, y + 1, z);
			Block blockTwoUp = world.getBlockAt(x, y + 2, z);
			if 
			(
				isOkToStandOn(block.getType())
			&&	isOkToStandIn(blockOneUp.getType())
			&& 	isOkToStandIn(blockTwoUp.getType())
			)
			{
				// spot found - return location
				return new Location(world, (double) x + 0.5, (double) y + 1, (double) z + 0.5, playerLoc.getYaw(),
						playerLoc.getPitch());
			}
			y += step;
		}

		// no spot found
		return null;
	}
	
	protected double getDistance(Location source, Location target)
	{
		return Math.sqrt
		(
			Math.pow(source.getX() - target.getX(), 2) 
		+ 	Math.pow(source.getY() - target.getY(), 2)
		+ 	Math.pow(source.getZ() - target.getZ(), 2)
		);
	}
	
	protected double getDistance(Player player, Block target)
	{
		Location loc = player.getLocation();
		return Math.sqrt
		(
			Math.pow(loc.getX() - target.getX(), 2) 
		+ 	Math.pow(loc.getY() - target.getY(), 2)
		+ 	Math.pow(loc.getZ() - target.getZ(), 2)
		);
	}
	
	/*
	 * Player location / rotation querying
	 */

	protected float getPlayerRotation()
	{
		float playerRot = player.getLocation().getYaw();
		while (playerRot < 0)
			playerRot += 360;
		while (playerRot > 360)
			playerRot -= 360;
		return playerRot;
	}

	protected Block getPlayerBlock()
	{
		Block playerBlock = null;
		Location playerLoc = player.getLocation();
		int x = (int) Math.round(playerLoc.getX() - 0.5);
		int y = (int) Math.round(playerLoc.getY() - 0.5);
		int z = (int) Math.round(playerLoc.getZ() - 0.5);
		int dy = 0;
		while (dy > -3 && (playerBlock == null || isOkToStandIn(playerBlock.getType())))
		{
			playerBlock = player.getWorld().getBlockAt(x, y + dy, z);
			dy--;
		}
		return playerBlock;
	}


	protected BlockFace getPlayerFacing()
	{
		float playerRot = getPlayerRotation();

		BlockFace direction = BlockFace.NORTH;
		if (playerRot <= 45 || playerRot > 315)
		{
			direction = BlockFace.WEST;
		}
		else if (playerRot > 45 && playerRot <= 135)
		{
			direction = BlockFace.NORTH;
		}
		else if (playerRot > 135 && playerRot <= 225)
		{
			direction = BlockFace.EAST;
		}
		else if (playerRot > 225 && playerRot <= 315)
		{
			direction = BlockFace.SOUTH;
		}

		return direction;
	}
	
	/*
	 * Block navigation helpers- for moving in direction relative to a current direction
	 */

	// Should go in BlockFace?
	// Also, there's probably some better matrix-y, math-y way to do this.
	protected BlockFace goLeft(BlockFace direction)
	{
		switch (direction)
		{
			case EAST:
				return BlockFace.NORTH;
			case NORTH:
				return BlockFace.WEST;
			case WEST:
				return BlockFace.SOUTH;
			case SOUTH:
				return BlockFace.EAST;
		}
		return direction;
	}

	protected BlockFace goRight(BlockFace direction)
	{
		switch (direction)
		{
			case EAST:
				return BlockFace.SOUTH;
			case SOUTH:
				return BlockFace.WEST;
			case WEST:
				return BlockFace.NORTH;
			case NORTH:
				return BlockFace.EAST;
		}
		return direction;
	}
	
	/*
	 * Aiming and projectile functions
	 */
	
	/*
	 * Find a good location to spawn a projectile, such as a fireball.
	 */
	protected Location getProjectileSpawnLocation()
	{
		Block spawnBlock = getPlayerBlock();

		int height = 2;
		double hLength = 2;
		double xOffset = (hLength * Math.cos(Math.toRadians(xRotation)));
		double zOffset = (hLength * Math.sin(Math.toRadians(xRotation)));

		Vector aimVector = new Vector(xOffset + 0.5, height + 0.5, zOffset + 0.5);

		Location location = new Location(player.getWorld(), spawnBlock.getX() + aimVector.getX(), spawnBlock.getY()
				+ aimVector.getY(), spawnBlock.getZ() + aimVector.getZ(), player.getLocation().getYaw(), player
				.getLocation().getPitch());

		return location;
	}

	protected Vector getAimVector()
	{
		return new Vector((0 - Math.sin(Math.toRadians(playerLocation.getYaw()))), (0 - Math.sin(Math
				.toRadians(playerLocation.getPitch()))), Math.cos(Math.toRadians(playerLocation.getYaw())));
	}

	protected void findTargetBlock()
	{
		if (targetingComplete)
		{
			return;
		}

		while (getNextBlock() != null)
		{
			Block block = getCurBlock();
			if (isTargetable(block.getType()))
			{
				boolean enoughSpace = true;
				for (int i = 1; i < targetHeightRequired; i++)
				{
					block = block.getFace(BlockFace.UP);
					if (!isTargetable(block.getType()))
					{
						enoughSpace = false;
						break;
					}
				}
				if (enoughSpace) break;
			}
		}
		targetingComplete = true;
	}
	
	protected double getYRotation()
	{
		return yRotation;
	}
	
	protected double getXRotation()
	{
		return xRotation;
	}
	
	/*
	 * HitBlox-ported code
	 */

	/**
	 * Returns the block at the cursor, or null if out of range
	 * 
	 * @return Block
	 */
	protected Block getTargetBlock()
	{
		findTargetBlock();
		return getCurBlock();
	}

	/**
	 * Sets the type of the block at the cursor
	 * 
	 * @param type
	 */
	protected void setTargetBlock(int type)
	{
		findTargetBlock();
		if (getCurBlock() != null)
		{
			setBlockAt(type, targetX, targetY, targetZ);
		}
	}

	/**
	 * Returns the block attached to the face at the cursor, or null if out of
	 * range
	 * 
	 * @return Block
	 */
	protected Block getFaceBlock()
	{
		findTargetBlock();
		if (getCurBlock() != null)
		{
			return getLastBlock();
		}
		else
		{
			return null;
		}
	}

	/**
	 * Sets the type of the block attached to the face at the cursor
	 * 
	 * @param type
	 */
	protected void setFaceBlock(int type)
	{
		findTargetBlock();
		if (getCurBlock() != null)
		{
			setBlockAt(type, lastX, lastY, lastZ);
		}
	}

	/**
	 * Returns STEPS forward along line of vision and returns block
	 * 
	 * @return Block
	 */
	protected Block getNextBlock()
	{
		lastX = targetX;
		lastY = targetY;
		lastZ = targetZ;

		do
		{
			length += step;

			hLength = (length * Math.cos(Math.toRadians(yRotation)));
			yOffset = (length * Math.sin(Math.toRadians(yRotation)));
			xOffset = (hLength * Math.cos(Math.toRadians(xRotation)));
			zOffset = (hLength * Math.sin(Math.toRadians(xRotation)));

			targetX = (int) Math.floor(xOffset + playerLocation.getX());
			targetY = (int) Math.floor(yOffset + playerLocation.getY() + viewHeight);
			targetZ = (int) Math.floor(zOffset + playerLocation.getZ());

		}
		while ((length <= range) && ((targetX == lastX) && (targetY == lastY) && (targetZ == lastZ)));

		if (length > range)
		{
			return null;
		}

		return getBlockAt(targetX, targetY, targetZ);
	}

	/**
	 * Returns the current block along the line of vision
	 * 
	 * @return Block
	 */
	protected Block getCurBlock()
	{
		if (length > range)
		{
			return null;
		}
		else
		{
			return getBlockAt(targetX, targetY, targetZ);
		}
	}

	/**
	 * Sets current block type id
	 * 
	 * @param type
	 */
	protected void setCurBlock(int type)
	{
		if (getCurBlock() != null)
		{
			setBlockAt(type, targetX, targetY, targetZ);
		}
	}

	/**
	 * Returns the previous block along the line of vision
	 * 
	 * @return Block
	 */
	protected Block getLastBlock()
	{
		return getBlockAt(lastX, lastY, lastZ);
	}

	/**
	 * Sets previous block type id
	 * 
	 * @param type
	 */
	protected void setLastBlock(int type)
	{
		if (getLastBlock() != null)
		{
			setBlockAt(type, lastX, lastY, lastZ);
		}
	}

	/**
	 * Sets the block type at the specified location
	 * 
	 * @param blockType
	 * @param x
	 * @param y
	 * @param z
	 * @return true if successful
	 */
	protected boolean setBlockAt(int blockType, int x, int y, int z)
	{
		World world = player.getWorld();
		Block block = world.getBlockAt(x, y, z);
		block.setTypeId(blockType);
		CraftWorld craftWorld = (CraftWorld) world;
		craftWorld.updateBlock(x, y, z);
		return block.getTypeId() == blockType;
	}

	/**
	 * Returns the block at the specified location
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return block
	 */
	protected Block getBlockAt(int x, int y, int z)
	{
		World world = player.getWorld();
		return world.getBlockAt(x, y, z);
	}	
	
	/*
	 * Functions to send text to player- use these to respect "quiet" and "silent" modes.
	 */
	
	public void castMessage(Player player, String message)
	{
		if (!spells.isQuiet() && !spells.isSilent())
		{
			player.sendMessage(message);
		}
	}

	public void sendMessage(Player player, String message)
	{
		if (!spells.isSilent())
		{
			player.sendMessage(message);
		}
	}

	/*
	 * Time functions
	 */

	/**
	 * Sets the current server time
	 * 
	 * @param time
	 *            time (0-24000)
	 */
	public void setRelativeTime(long time)
	{
		long margin = (time - getTime()) % 24000;
		// Java modulus is stupid.
		if (margin < 0)
		{
			margin += 24000;
		}
		spells.getPlugin().getServer().setTime(getTime() + margin);
	}

	/**
	 * Returns actual server time (-2^63 to 2^63-1)
	 * 
	 * @return time server time
	 */
	public long getTime()
	{
		return spells.getPlugin().getServer().getTime();
	}
	
	/*
	 * Helper functions
	 */
	
	public boolean isUnderwater()
	{
		Block playerBlock = getPlayerBlock();
		playerBlock = playerBlock.getFace(BlockFace.UP);
		return (playerBlock.getType() == Material.WATER || playerBlock.getType() == Material.STATIONARY_WATER);
	}
	
	/*
	 * Internal functions - do not call
	 */
	public List<SpellVariant> getVariants()
	{
		return variants;
	}
	
	protected void addVariant(String name, Material material, String category, String description, String[] parameters)
	{
		variants.add(new SpellVariant(this, name, material, category, description, parameters));
	}
	
	protected void addVariant(String name, Material material, String category, String description, String parameter)
	{
		String[] parameters = parameter.split(" ");
		variants.add(new SpellVariant(this, name, material, category, description, parameters));
	}
	
	public void setPlugin(Spells plugin)
	{
		this.spells = plugin;
	}
	
	public boolean cast(String[] parameters, Player player)
	{
		this.player = player;

		targetThrough(Material.AIR);
		targetThrough(Material.WATER);
		targetThrough(Material.STATIONARY_WATER);

		initializeTargeting(player);

		return onCast(parameters);
	}

	public void cancel(Spells plugin, Player player)
	{
		this.player = player;
		this.spells = plugin;

		onCancel();
	}

	protected void initializeTargeting(Player player)
	{
		playerLocation = player.getLocation();
		length = 0;
		targetHeightRequired = 1;
		xRotation = (playerLocation.getYaw() + 90) % 360;
		yRotation = playerLocation.getPitch() * -1;
		reverseTargeting = false;

		targetX = (int) Math.floor(playerLocation.getX());
		targetY = (int) Math.floor(playerLocation.getY() + viewHeight);
		targetZ = (int) Math.floor(playerLocation.getZ());
		lastX = targetX;
		lastY = targetY;
		lastZ = targetZ;
		targetingComplete = false;
	}
	
	@Override
	public int compareTo(Spell other)
	{
		return getName().compareTo(other.getName());
	}
	
	/*
	 * private data
	 */

	private int									range					= 200;
	private double								viewHeight				= 1.65;
	private double								step					= 0.2;

	private boolean								targetingComplete;
	private int									targetHeightRequired	= 1;
	private Location							playerLocation;
	private double								xRotation, yRotation;
	private double								length, hLength;
	private double								xOffset, yOffset, zOffset;
	private int									lastX, lastY, lastZ;
	private int									targetX, targetY, targetZ;
	private final HashMap<Material, Boolean>	targetThroughMaterials	= new HashMap<Material, Boolean>();
	private boolean								reverseTargeting		= false;
	private final List<SpellVariant>			variants				= new ArrayList<SpellVariant>();

}
