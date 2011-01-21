package com.elmakers.mine.bukkit.plugins.spells;

import java.util.HashMap;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;

import com.elmakers.mine.bukkit.utilities.PluginProperties;

/**
 * 
 * Base class for spells. Handles finding player location, targetting, and other
 * common spell activities.
 * 
 * Lots of code taken from:
 * 
 * HitBlox.java - Class for getting blocks along line of sight
 * 
 * NOTES: This class is designed to handle the annoying parts of the seemingly
 * simple task of getting the coordinates of the block a player is currently
 * aimed at. This class abstracts the simpler tasks of finding the current
 * target block and the adjacent unoccupied block to their own methods, but it
 * also provides a public getNextBlock method for processing the entire
 * line-of-sight from the player for more specialized tasks. This method can be
 * used exactly as it is in getTargetBlock, for instance.
 * 
 * WARNING: Servers with map coordinate bugs may experience a one or more block
 * inaccuracy when in affected parts of the world. A good way to test areas for
 * the offset bug is to use Chrisinajar's Magic Carpet plugin.
 * 
 * Contact: For questions, contact Ho0ber@gmail.com or channel #hey0 on
 * irc.esper.net
 * 
 * @author Ho0ber
 */
public abstract class Spell implements Comparable<Spell>
{
	protected Player							player;
	protected SpellsPlugin						plugin;

	protected int								range					= 200;
	protected double							viewHeight				= 1.65;
	protected double							step					= 0.2;

	protected boolean							targetingComplete;
	protected Location							playerLocation;
	protected double							xRotation, yRotation;
	protected double							length, hLength;
	protected double							xOffset, yOffset, zOffset;
	protected int								lastX, lastY, lastZ;
	protected int								targetX, targetY, targetZ;
	protected final HashMap<Material, Boolean>	targetThroughMaterials	= new HashMap<Material, Boolean>();

	// Begin override methods

	public abstract boolean onCast(String[] parameters);

	public abstract String getName();

	public abstract String getCategory();

	public abstract String getDescription();

	public void onLoad(PluginProperties properties)
	{

	}

	public void onCancel()
	{

	}

	// End override methods

	public void cast(String[] parameters, SpellsPlugin plugin, Player player)
	{
		this.player = player;
		this.plugin = plugin;

		targetThrough(Material.AIR);
		targetThrough(Material.WATER);
		targetThrough(Material.STATIONARY_WATER);

		initializeTargeting(player);

		onCast(parameters);
	}

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
		return (checkMat == null || !checkMat);
	}

	public void cancel(SpellsPlugin plugin, Player player)
	{
		this.player = player;
		this.plugin = plugin;

		onCancel();
	}

	public void initializeTargeting(Player player)
	{
		playerLocation = player.getLocation();
		length = 0;
		xRotation = (playerLocation.getYaw() + 90) % 360;
		yRotation = playerLocation.getPitch() * -1;

		targetX = (int) Math.floor(playerLocation.getX());
		targetY = (int) Math.floor(playerLocation.getY() + viewHeight);
		targetZ = (int) Math.floor(playerLocation.getZ());
		lastX = targetX;
		lastY = targetY;
		lastZ = targetZ;
		targetingComplete = false;
	}

	protected void findTargetBlock()
	{
		if (targetingComplete)
		{
			return;
		}

		while (getNextBlock() != null)
		{
			if (isTargetable(getCurBlock().getType()))
			{
				break;
			}
		}
		targetingComplete = true;
	}

	/**
	 * Returns the block at the cursor, or null if out of range
	 * 
	 * @return Block
	 */
	public Block getTargetBlock()
	{
		findTargetBlock();
		return getCurBlock();
	}

	/**
	 * Sets the type of the block at the cursor
	 * 
	 * @param type
	 */
	public void setTargetBlock(int type)
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
	public Block getFaceBlock()
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
	public void setFaceBlock(int type)
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
	public Block getNextBlock()
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
	public Block getCurBlock()
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
	public void setCurBlock(int type)
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
	public Block getLastBlock()
	{
		return getBlockAt(lastX, lastY, lastZ);
	}

	/**
	 * Sets previous block type id
	 * 
	 * @param type
	 */
	public void setLastBlock(int type)
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
	public boolean setBlockAt(int blockType, int x, int y, int z)
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
	public Block getBlockAt(int x, int y, int z)
	{
		World world = player.getWorld();
		return world.getBlockAt(x, y, z);
	}

	public boolean isOkToStandIn(Material mat)
	{
		return (mat == Material.AIR || mat == Material.WATER || mat == Material.STATIONARY_WATER);
	}

	public boolean isOkToStandOn(Material mat)
	{
		return (mat != Material.AIR && mat != Material.LAVA && mat != Material.STATIONARY_LAVA);
	}
	
	public Location findPlaceToStand(Location playerLoc, boolean goUp)
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

	public float getPlayerRotation()
	{
		float playerRot = player.getLocation().getYaw();
		while (playerRot < 0)
			playerRot += 360;
		while (playerRot > 360)
			playerRot -= 360;
		return playerRot;
	}

	public Block getPlayerBlock()
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

	@Override
	public int compareTo(Spell other)
	{
		return getName().compareTo(other.getName());
	}

	public BlockFace getPlayerFacing()
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

	// Should go in BlockFace?
	// Also, there's probably some better matrix-y, math-y way to do this.
	public BlockFace goLeft(BlockFace direction)
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

	public BlockFace goRight(BlockFace direction)
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
		plugin.getServer().setTime(getTime() + margin);
	}

	/**
	 * Returns actual server time (-2^63 to 2^63-1)
	 * 
	 * @return time server time
	 */
	public long getTime()
	{
		return plugin.getServer().getTime();
	}

	public double getDistance(Player player, Block target)
	{
		Location loc = player.getLocation();
		return Math.sqrt(Math.pow(loc.getX() - target.getX(), 2) + Math.pow(loc.getY() - target.getY(), 2)
				+ Math.pow(loc.getZ() - target.getZ(), 2));
	}

	public void castMessage(Player player, String message)
	{
		if (!plugin.isQuiet() && !plugin.isSilent())
		{
			player.sendMessage(message);
		}
	}

	public void sendMessage(Player player, String message)
	{
		if (!plugin.isSilent())
		{
			player.sendMessage(message);
		}
	}

	public Location getSpawnLocation()
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

	public Vector getAimVector()
	{
		return new Vector((0 - Math.sin(Math.toRadians(playerLocation.getYaw()))), (0 - Math.sin(Math
				.toRadians(playerLocation.getPitch()))), Math.cos(Math.toRadians(playerLocation.getYaw())));
	}
	
	public boolean isUnderwater()
	{
		Block playerBlock = getPlayerBlock();
		playerBlock = playerBlock.getFace(BlockFace.UP);
		return (playerBlock.getType() == Material.WATER || playerBlock.getType() == Material.STATIONARY_WATER);
	}
}
