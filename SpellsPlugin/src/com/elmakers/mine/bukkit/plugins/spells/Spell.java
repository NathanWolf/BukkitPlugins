package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;

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
public abstract class Spell 
{
	protected Player player;
	protected SpellsPlugin plugin;

	private int range = 200;
	private double view_height = 1.65;
	private double step = 0.2;
	
	private Location player_loc;	
    private double rot_x, rot_y;
    private double length, h_length;
    private double x_offset, y_offset, z_offset;
    private int last_x, last_y, last_z;
    private int target_x, target_y, target_z;
	
	public abstract boolean onCast(String[] parameters);
	public abstract String getName();
	public abstract String getCategory();
	public abstract String getDescription();
	
	public void cast(String[] parameters, SpellsPlugin plugin, Player player)
	{
		this.player = player;
		this.plugin = plugin;
		
		getTargets(player);
        
        onCast(parameters);
	}
	
	public void getTargets(Player player)
	{
		player_loc = player.getLocation();
        length = 0;
        rot_x = (player_loc.getYaw() + 90) % 360;
        rot_y = player_loc.getPitch() * -1;

        target_x = (int) Math.floor(player_loc.getX());
        target_y = (int) Math.floor(player_loc.getY() + view_height);
        target_z = (int) Math.floor(player_loc.getZ());
        last_x = target_x;
        last_y = target_y;
        last_z = target_z;
	}
    

    /**
     * Returns the block at the cursor, or null if out of range
     * 
     * @return Block
     */
    public Block getTargetBlock() {
        while ((getNextBlock() != null) && (getCurBlock().getType() == Material.AIR));
        return getCurBlock();
    }

    /**
     * Sets the type of the block at the cursor
     * 
     * @param type
     */
    public void setTargetBlock(int type) {
        while ((getNextBlock() != null) && (getCurBlock().getType() == Material.AIR));
        if (getCurBlock() != null) {
            //plugin.getServer().setBlockAt(type, target_x, target_y, target_z);
        }
    }

    /**
     * Returns the block attached to the face at the cursor, or null if out of
     * range
     * 
     * @return Block
     */
    public Block getFaceBlock() {
        while ((getNextBlock() != null) && (getCurBlock().getType() == Material.AIR));
        if (getCurBlock() != null) {
            return getLastBlock();
        } else {
            return null;
        }
    }

    /**
     * Sets the type of the block attached to the face at the cursor
     * 
     * @param type
     */
    public void setFaceBlock(int type) {
        while ((getNextBlock() != null) && (getCurBlock().getType() == Material.AIR));
        if (getCurBlock() != null) {
            setBlockAt(type, last_x, last_y, last_z);
        }
    }

    /**
     * Returns STEPS forward along line of vision and returns block
     * 
     * @return Block
     */
    public Block getNextBlock() {
        last_x = target_x;
        last_y = target_y;
        last_z = target_z;

        do {
            length += step;

            h_length = (length * Math.cos(Math.toRadians(rot_y)));
            y_offset = (length * Math.sin(Math.toRadians(rot_y)));
            x_offset = (h_length * Math.cos(Math.toRadians(rot_x)));
            z_offset = (h_length * Math.sin(Math.toRadians(rot_x)));

            target_x = (int) Math.floor(x_offset + player_loc.getX());
            target_y = (int) Math.floor(y_offset + player_loc.getY() + view_height);
            target_z = (int) Math.floor(z_offset + player_loc.getZ());

        } while ((length <= range) && ((target_x == last_x) && (target_y == last_y) && (target_z == last_z)));

        if (length > range) {
            return null;
        }

        return getBlockAt(target_x, target_y, target_z);
    }

    /**
     * Returns the current block along the line of vision
     * 
     * @return Block
     */
    public Block getCurBlock() {
        if (length > range) {
            return null;
        } else {
            return getBlockAt(target_x, target_y, target_z);
        }
    }

    /**
     * Sets current block type id
     * 
     * @param type
     */
    public void setCurBlock(int type) {
        if (getCurBlock() != null) {
            setBlockAt(type, target_x, target_y, target_z);
        }
    }

    /**
     * Returns the previous block along the line of vision
     * 
     * @return Block
     */
    public Block getLastBlock() {
        return getBlockAt(last_x, last_y, last_z);
    }

    /**
     * Sets previous block type id
     * 
     * @param type
     */
    public void setLastBlock(int type) {
        if (getLastBlock() != null) {
            setBlockAt(type, last_x, last_y, last_z);
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
    public boolean setBlockAt(int blockType, int x, int y, int z) {
    	World world = player.getWorld();
    	Block block = world.getBlockAt(x, y, z);
    	block.setTypeId(blockType);
    	CraftWorld craftWorld = (CraftWorld)world;
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
   
   public Location findPlaceToStand(Player player, boolean goUp) 
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
		Location playerLoc = player.getLocation();
		int x = (int)Math.round(playerLoc.getX() - 0.5);
		int y = (int)Math.round(playerLoc.getY() + step + step);
		int z = (int)Math.round(playerLoc.getZ() - 0.5);
		
		World world = player.getWorld();
				
		// search for a spot to stand
		while (2 < y && y < 255) 
		{
			Block block = world.getBlockAt(x, y, z);
			Block blockOneUp = world.getBlockAt(x, y + 1, z);
			Block blockTwoUp = world.getBlockAt(x, y + 2, z);
			if (block.getTypeId() != 0 && blockOneUp.getTypeId() == 0 && blockTwoUp.getTypeId() == 0) 
			{
				// spot found - return location
				return new Location(world, (double)x + 0.5,(double)y + 1, (double)z + 0.5, playerLoc.getYaw(), playerLoc.getPitch());
			}
			y += step;
		}
		
		// no spot found
		return null;
	}
   
	public float getPlayerRotation()
	{
		float playerRot = player.getLocation().getYaw();
		while (playerRot < 0) playerRot += 360;
		while (playerRot > 360) playerRot -= 360;
		return playerRot;
	}
	
	public Block getPlayerBlock()
	{
		Block playerBlock = null;
		Location playerLoc = player.getLocation();
		int x = (int)Math.round(playerLoc.getX() - 0.5);
		int y = (int)Math.round(playerLoc.getY() - 0.5);
		int z = (int)Math.round(playerLoc.getZ() - 0.5);
		int dy = 0;
		while (dy > -3 && (playerBlock == null || playerBlock.getType() == Material.AIR))
		{
			playerBlock = player.getWorld().getBlockAt(x, y + dy, z);
			dy--;
		}			
		return playerBlock;
	}
}
