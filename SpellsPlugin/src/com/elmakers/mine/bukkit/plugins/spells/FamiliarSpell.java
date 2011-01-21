package com.elmakers.mine.bukkit.plugins.spells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.minecraft.server.EntityChicken;
import net.minecraft.server.EntityCow;
import net.minecraft.server.EntityCreeper;
import net.minecraft.server.EntityGhast;
import net.minecraft.server.EntityPig;
import net.minecraft.server.EntityPigZombie;
import net.minecraft.server.EntitySheep;
import net.minecraft.server.EntitySkeleton;
import net.minecraft.server.EntitySpider;
import net.minecraft.server.EntitySquid;
import net.minecraft.server.EntityZombie;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.World;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.Location;

import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;

public class FamiliarSpell extends Spell
{
	private String DEFAULT_FAMILIARS = "chicken,sheep,cow,pig";
	private List<String> defaultFamiliars = new ArrayList<String>();
	private final Random rand = new Random();
	private HashMap<String, PlayerFamiliar> familiars = new HashMap<String, PlayerFamiliar>();
	
	class PlayerFamiliar
	{
		public EntityLiving familiar = null;
		
		public boolean hasFamiliar()
		{
			return familiar != null;
		}
		
		public void setFamiliar(EntityLiving f)
		{
			familiar = f;
		}
		
		public void releaseFamiliar()
		{
			if (familiar != null)
			{
				((CraftLivingEntity)(familiar.getBukkitEntity())).setHealth(0);
				familiar = null;
			}
		}
	}
	
	enum FamiliarType
	{
		CHICKEN,
		SHEEP,
		COW,
		PIG,
		CREEPER,
		PIGZOMBIE,
		SKELETON,
		SPIDER,
		SQUID,
		ZOMBIE,
		GHAST,
		FISH,
		UNKNOWN;
		
		public static FamiliarType parseString(String s)
		{
			return parseString(s, UNKNOWN);
		}
		
		public static FamiliarType parseString(String s, FamiliarType defaultFamiliarType)
		{
			FamiliarType foundType = defaultFamiliarType;
			for (FamiliarType t : FamiliarType.values())
			{
				if (t.name().equalsIgnoreCase(s))
				{
					foundType = t;
				}
			}
			return foundType;
		}
		
	};
	
	@Override
	public boolean onCast(String[] parameters)
	{
		PlayerFamiliar fam = getFamiliar(player.getName());
		if (fam.hasFamiliar())
		{
			fam.releaseFamiliar();
			castMessage(player, "You release your familiar");
			return true;
		}
		else
		{
			Block target = getTargetBlock();
			if (target == null)
			{
				castMessage(player, "No target");
				return false;
			}
			target = target.getFace(BlockFace.UP);
			
			FamiliarType famType = FamiliarType.UNKNOWN;
			if (parameters.length > 0)
			{
				if (parameters[0].equalsIgnoreCase("any"))
				{
					int randomFamiliar = rand.nextInt(FamiliarType.values().length - 1);
					famType = FamiliarType.values()[randomFamiliar];
				}
				else
				{
					famType = FamiliarType.parseString(parameters[0]);
				}
			}
			
			if (famType == FamiliarType.UNKNOWN)
			{
				int randomFamiliar = rand.nextInt(defaultFamiliars.size());
				famType = FamiliarType.parseString(defaultFamiliars.get(randomFamiliar));
			}
			
			if (isUnderwater())
			{
				famType = FamiliarType.SQUID;
			}
			
			EntityLiving entity =  spawnFamiliar(target, famType);
			if (entity == null)
			{
				sendMessage(player, "Your familiar is DOA");
				return false;
			}
			castMessage(player, "You create a " + famType.name().toLowerCase() + " familiar!");
			fam.setFamiliar(entity);
			return true;
		}
	}
		
	protected EntityLiving spawnFamiliar(Block target, FamiliarType famType)
	{
		Location targetLocation = new Location(player.getWorld(), target.getX(), target.getY(), target.getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
		CraftPlayer craftPlayer = (CraftPlayer)player;
		CraftWorld craftWorld = (CraftWorld)craftPlayer.getWorld();
		World world = craftWorld.getHandle();
		
		EntityLiving e = null;
		
		switch (famType)
		{
			case SHEEP: e = new EntitySheep(world); break;
			case PIG: e = new EntityPig(world); break;
			case CHICKEN: e = new EntityChicken(world); break;
			case COW: e = new EntityCow(world); break;
			case CREEPER: e = new EntityCreeper(world); break;
			case PIGZOMBIE: e = new EntityPigZombie(world); break;
			case SKELETON: e = new EntitySkeleton(world); break;
			case SPIDER: e = new EntitySpider(world); break;
			case SQUID: e = new EntitySquid(world); break;
			case GHAST: e = new EntityGhast(world); break;
			case ZOMBIE: e = new EntityZombie(world); break;
			//case FISH: e = new EntityFish(world); break;
		}
		
		if (e != null)
		{
			((CraftWorld)player.getWorld()).getHandle().a(e);
			e.getBukkitEntity().teleportTo(targetLocation);
		}
		
		return e;
	}

	protected PlayerFamiliar getFamiliar(String playerName)
	{
		PlayerFamiliar familiar = familiars.get(playerName);
		if (familiar == null)
		{
			familiar = new PlayerFamiliar();
			familiars.put(playerName, familiar);
		}
		return familiar;
	}	
	
	@Override
	public String getName()
	{
		return "familiar";
	}

	@Override
	public String getCategory()
	{
		return "WIP";
	}

	@Override
	public String getDescription()
	{
		return "Create an animal familiar to follow you around";
	}

	@Override
	public void onLoad(PluginProperties properties)
	{
		defaultFamiliars = properties.getStringList("spells-familiar-animals", DEFAULT_FAMILIARS);
	}
	
}
