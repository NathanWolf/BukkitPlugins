package com.elmakers.mine.bukkit.persistence.dao;

import com.elmakers.mine.bukkit.persistence.annotation.PersistClass;
import com.elmakers.mine.bukkit.persistence.annotation.PersistField;
import com.elmakers.mine.craftbukkit.persistence.Persistence;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BlockVector;

/**
 * Persist data from a location. Convenience getter and setters are available
 * for ease of object interaction. Use locationFactory(Server) to generate a new
 * concrete instance of the bukkit Location class.
 * 
 * @author amkeyte
 */
@PersistClass(schema = "global", name = "location")
public class LocationData extends Persisted
{
	/**
	 * Default constructor for uninitialized LocationData Use setters for
	 * blockVector,orientation, and worldData before attempting to "put" this
	 * object.
	 */
	public LocationData()
	{
	}

	/**
	 * create a new location to persist
	 * 
	 * @param loc
	 *            location to Persist
	 */
	public LocationData(Location loc)
	{
		update(loc);
	}

	/**
	 * matches Location class constructor signature
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 */
	public LocationData(final World world, final double x, final double y, final double z)
	{
		position = new BlockVector(x, y, z);
		worldData = Persistence.getInstance().get(world.getName(), WorldData.class);
		orientation = null;
	}

	/**
	 * matches Location class constructor signature
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param yaw
	 * @param pitch
	 */
	public LocationData(final World world, final double x, final double y, final double z, final float yaw, final float pitch)
	{
		position = new BlockVector(x, y, z);
		worldData = Persistence.getInstance().get(world.getName(), WorldData.class);
		orientation = new Orientation(yaw, pitch);
	}

	/**
	 * Return the position component of this location
	 * 
	 * This is contained because it is assumed that a Location generally
	 * moves around a lot.
	 * 
	 * @return the blockVector
	 */
	@PersistField(contained = true)
	public BlockVector getPosition()
	{
		return position;
	}

	/**
	 * Set the position component of this location
	 * 
	 * @param blockVector the blockVector to set
	 */
	public void setPosition(BlockVector position)
	{
		this.position = position;
	}

	/**
	 * Return the orientation component of this location.
	 * 
	 * This is contained because it is assumed that a Location generally
	 * changes a lot
	 * 
	 * @return the orientation
	 */
	@PersistField(contained = true)
	public Orientation getOrientation()
	{
		return orientation;
	}

	/**
	 * Set the orientation component of this location.
	 * 
	 * @param orientation the orientation to set
	 */
	public void setOrientation(Orientation orientation)
	{
		this.orientation = orientation;
	}
	
	/**
	 * Get the world component of this location
	 * 
	 * @return the worldData component of this location
	 * 
	 */
	@PersistField
	public WorldData getWorldData()
	{
		return worldData;
	}

	/**
	 * Set the world this location belongs to
	 * 
	 * @param worldData the worldData to set
	 */
	public void setWorldData(WorldData worldData)
	{
		this.worldData = worldData;
	}

	/**
	 * Creates a new Location object from the information that has been
	 * persisted. 
	 * 
	 * @return A new location representing this data.
	 */
	public Location getLocation()
	{
		if (worldData == null || position == null)
		{
			// Must have a world and position to make a location;
			return null;
		}
		
		if (orientation == null)
		{
			return new Location(worldData.getWorld(), position.getX(), position.getY(), position.getZ());
		}

		return new Location(worldData.getWorld(), position.getX(), position.getY(), position.getZ(), orientation.getYaw(), orientation.getPitch());
	}

	/**
	 * @return the x component of the position
	 */
	public Double getX()
	{
		if (position == null)
		{
			return null;
		}
		return position.getX();
	}

	/**
	 * @param x the x value to set in position
	 */
	public void setX(Double x)
	{
		if (position == null)
		{
			position = new BlockVector(x, 0, 0);
		}
		else
		{
			position.setX(x);
		}
	}

	/**
	 * @return the y component of the position
	 */
	public Double getY()
	{
		if (position == null)
		{
			return null;
		}

		return position.getY();
	}

	/**
	 * @param y the y value to set in position
	 */
	public void setY(Double y)
	{
		if (position == null)
		{
			position = new BlockVector(0, y, 0);
		}
		else
		{
			position.setY(y);
		}
	}

	/**
	 * @return the z component of the position
	 */
	public Double getZ()
	{
		if (position == null)
		{
			return null;
		}

		return position.getZ();
	}

	/**
	 * @param z the z value to set in position
	 */
	public void setZ(Double z)
	{
		if (position == null)
		{
			position = new BlockVector(0, 0, z);
		}
		else
		{
			position.setZ(z);
		}
	}

	/**
	 * @return the pitch component of orientation
	 */
	public Float getPitch()
	{
		if (orientation == null)
		{
			return null;
		}

		return orientation.getPitch();
	}

	/**
	 * @param pitch the pitch to set
	 */
	public void setPitch(Float pitch)
	{
		if (orientation == null)
		{
			orientation = new Orientation(0, pitch);
		}
		else
		{
			orientation.setPitch(pitch);
		}
	}

	/**
	 * @return the yaw
	 */
	public Float getYaw()
	{
		if (orientation == null)
		{
			return null;
		}
		return orientation.getYaw();
	}

	/**
	 * @param yaw
	 *            the yaw to set
	 */
	public void setYaw(Float yaw)
	{
		if (orientation == null)
		{
			orientation = new Orientation(yaw, 0);
		}
		else
		{
			orientation.setYaw(yaw);
		}
	}

	/**
	 * server required for WorldData to operate.
	 * 
	 * @param server
	 * @return
	 */
	public World getWorld()
	{
		if (worldData == null)
		{
			return null;
		}

		return worldData.getWorld();
	}

	/**
	 * set the world
	 * 
	 * @param world
	 */
	public void setWorld(World world)
	{
		worldData = Persistence.getInstance().get(world.getName(), WorldData.class);
	}

	/**
	 * Block coordinate
	 * 
	 * @return Integer
	 */
	public Integer getBlockX()
	{
		if (position == null)
		{
			return null;
		}

		return position.getBlockX();
	}

	/**
	 * Block coordinate
	 * 
	 * @return Integer
	 */
	public Integer getBlockY() 
	{
		if (position == null)
		{
			return null;
		}

		return position.getBlockY();
	}

	/**
	 * Block coordinate
	 * 
	 * @return Integer
	 */
	public Integer getBlockZ()
	{
		if (position == null)
		{
			return null;
		}

		return position.getBlockZ();
	}
	
	/**
	 * Update this LocationData given a location
	 * 
	 * @param location The location to take data from
	 */
	public void update(Location location)
	{
		updatePosition(location);
		updateOrientation(location);
		updateWorld(location);
	}
	
	/**
	 * Update this LocationData's position given a location
	 * 
	 * @param location The location to take position from
	 */
	public void updatePosition(Location loc)
	{
		position = new BlockVector(loc.getX(), loc.getY(), loc.getZ());
	}
	
	/**
	 * Update this LocationData's orientation given a location
	 * 
	 * @param location The location to take orientation from
	 */
	public void updateOrientation(Location loc)
	{
		orientation = new Orientation(loc);
	}
	
	/**
	 * Update this LocationData's world given a location
	 * 
	 * @param location The location to take world from
	 */
	public void updateWorld(Location loc)
	{
		worldData = Persistence.getInstance().get(loc.getWorld().getName(), WorldData.class);
	}

    /**
     * Returns a hash code for this Location- does not include orientation.
     *
     * @return hash code
     */
    @Override
    @PersistField(id=true, name="id", readonly=true)
    public int hashCode()
    {
    	int positionHash = position == null ? 0 : position.hashCode();
    	int worldHash = worldData == null ? 0 : worldData.getName().hashCode();
    	return positionHash ^ (worldHash << 14);
    }
	
	private BlockVector	position	= null;
	private Orientation	orientation	= null;
	private WorldData	worldData	= null;
}
