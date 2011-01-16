package com.elmakers.mine.bukkit.plugins.spells;

import net.minecraft.server.WorldServer;

import org.bukkit.Block;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Player;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.plugin.Plugin;

/**
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
public class HitBlox {

    private Location player_loc;
    private Player player;
    private double rot_x, rot_y, view_height;
    private double length, h_length, step;
    private int range;
    private double x_offset, y_offset, z_offset;
    private int last_x, last_y, last_z;
    private int target_x, target_y, target_z, target_type;

    /**
     * Constructor requiring player, uses default values
     * 
     * @param in_player
     */
    public HitBlox(Player in_player) {
    	player = in_player;
        init(in_player.getLocation(), 200, 0.2, 1.65); // Reasonable default
        // values
    }

    /**
     * Constructor requiring location, uses default values
     * 
     * @param in_location
     */
    public HitBlox(Location in_location) {
        init(in_location, 200, 0.2, 0);
    }

    /**
     * Constructor requiring player, max range, and a stepping value
     * 
     * @param in_player
     * @param in_range
     * @param in_step
     */
    public HitBlox(Player in_player, int in_range, double in_step) {
        init(in_player.getLocation(), in_range, in_step, 1.65);
    }

    /**
     * Constructor requiring location, max range, and a stepping value
     * 
     * @param in_location
     * @param in_range
     * @param in_step
     */
    public HitBlox(Location in_location, int in_range, double in_step) {
        init(in_location, in_range, in_step, 0);
    }

    /**
     * Initialization method
     * 
     * @param in_location
     * @param in_range
     * @param in_step
     * @param in_view_height
     */
    public void init(Location in_location, int in_range, double in_step, double in_view_height) {
        player_loc = in_location;
        view_height = in_view_height;
        range = in_range;
        step = in_step;
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
    	WorldServer server = ((CraftWorld)world).getHandle();
        return false;
    	//return server.e.d(x, y, z, blockType);
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
	   WorldServer server = ((CraftWorld)world).getHandle();
	   return server.getWorld().getBlockAt(x, y, z);
   }

}
