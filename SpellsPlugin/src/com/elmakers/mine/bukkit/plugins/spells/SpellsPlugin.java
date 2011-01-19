package com.elmakers.mine.bukkit.plugins.spells;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.plugins.groups.Permissions;
import com.elmakers.mine.bukkit.plugins.groups.PlayerPermissions;
import com.elmakers.mine.bukkit.utilities.PluginProperties;

public class SpellsPlugin extends JavaPlugin 
{
	private final String propertiesFile = "spells.properties";
	private String permissionsFile = "spell-classes.txt";
	
	private final String wandPropertiesFile = "wand.properties";
	private int wandTypeId = 280;
	
	private final List<BlockList> cleanupBlocks = new ArrayList<BlockList>();
	private final Object cleanupLock = new Object();
	private long lastCleanupTime = 0;
	
	private final Logger log = Logger.getLogger("Minecraft");
	private final Permissions permissions = new Permissions();
	private final SpellsPlayerListener playerListener = new SpellsPlayerListener();
	private final HashMap<String, Spell> spells = new HashMap<String, Spell>();
	private final HashMap<String, PlayerSpells> playerSpells = new HashMap<String, PlayerSpells>();

	public SpellsPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File dataFolder, File plugin, ClassLoader cLoader) 
	{
		super(pluginLoader, instance, desc, dataFolder, plugin, cLoader);
	
		addSpell(new HealSpell());
		addSpell(new BlinkSpell());
		addSpell(new AscendSpell());
		addSpell(new DescendSpell());
		addSpell(new TorchSpell());
		addSpell(new ExtendSpell());
		addSpell(new FireballSpell());
		addSpell(new TowerSpell());
		addSpell(new PillarSpell());
		addSpell(new BridgeSpell());
		addSpell(new AbsorbSpell());
		addSpell(new FillSpell());
		addSpell(new TimeSpell());
		addSpell(new ReloadSpell());
		addSpell(new CushionSpell());
	}
	
	protected void loadProperties()
	{
		PluginProperties properties = new PluginProperties(propertiesFile);
		properties.load();
		
		permissionsFile = properties.getString("spells-classes-file", permissionsFile);
		permissions.load(permissionsFile);
		
		for (Spell spell : spells.values())
		{
			spell.load(properties);
		}
		
		properties.save();
		
		// Load wand properties as well, in case that plugin exists.
		properties = new PluginProperties(wandPropertiesFile);
		properties.load();
		
		// Get and set all properties
		wandTypeId = properties.getInteger("wand-type-id", wandTypeId);
		
		// Don't save the wand properties!!
	}
	
	public void load()
	{
		loadProperties();
	}
	
	protected PlayerSpells getPlayerSpells(Player player)
	{
		PlayerSpells spells = playerSpells.get(player.getName());
		if (spells == null)
		{
			spells = new PlayerSpells();
			playerSpells.put(player.getName(), spells);
		}
		return spells;
	}
	
	private void addSpell(Spell spell)
	{
		spells.put(spell.getName(), spell);
	}
	
	@Override
	public void onEnable() 
	{
		load();
		playerListener.setPlugin(this);
		
        PluginManager pm = getServer().getPluginManager();
		
        pm.registerEvent(Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_ITEM, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_ANIMATION, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
        
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
	}

	@Override
	public void onDisable() 
	{
		forceCleanup();
	}
	
	public void listSpells(Player player, PlayerPermissions playerPermissions)
	{
		player.sendMessage("Use: /cast <spell>: ");
		class SpellGroup implements Comparable<SpellGroup>
		{
			public String groupName;
			public List<Spell> spells = new ArrayList<Spell>();
			
			@Override
			public int compareTo(SpellGroup other) 
			{
				return groupName.compareTo(other.groupName);
			}
		}
		HashMap<String, SpellGroup> spellGroups = new HashMap<String, SpellGroup>();
	
		for (Spell spell : spells.values())
		{
			SpellGroup group = spellGroups.get(spell.getCategory());
			if (group == null)
			{
				group = new SpellGroup();
				group.groupName = spell.getCategory();
				spellGroups.put(group.groupName, group);	
			}
			group.spells.add(spell);
		}
		
		List<SpellGroup> sortedGroups = new ArrayList<SpellGroup>();
		sortedGroups.addAll(spellGroups.values());
		Collections.sort(sortedGroups);
		
		for (SpellGroup group : sortedGroups)
		{
			player.sendMessage(group.groupName + ":");
			Collections.sort(group.spells);
			for (Spell spell : group.spells)
			{
				player.sendMessage(" " + spell.getName() + " : " + spell.getDescription());
			}
		}
	}
	
	public PlayerPermissions getPermissions(String playerName)
	{
		return permissions.getPlayerPermissions(playerName);
	}
	
	public Spell getSpell(String name)
	{
		return spells.get(name);
	}
	
	public void setCurrentMaterialType(Player player, Material material)
	{
		PlayerSpells spells = getPlayerSpells(player);
		if (spells.isUsingMaterial() && spells.getMaterial() != material)
		{
			player.sendMessage("Now using " + material.name().toLowerCase());
		}
		spells.setMaterial(material);
	}
	
	public void startMaterialUse(Player player, Material material)
	{
		PlayerSpells spells = getPlayerSpells(player);
		spells.startMaterialUse(material);
	}
	
	public Material finishMaterialUse(Player player)
	{
		PlayerSpells spells = getPlayerSpells(player);
		return spells.finishMaterialUse();
	}

	public void cleanup()
	{
		synchronized(cleanupLock)
		{
			if (cleanupBlocks.size() == 0) return;
			
			List<BlockList> tempList = new ArrayList<BlockList>();
			tempList.addAll(cleanupBlocks);
			long timePassed = System.currentTimeMillis() - lastCleanupTime;
			for (BlockList blocks : tempList)
			{
				if (blocks.age((int)timePassed))
				{
					blocks.undo();
				}
				if (blocks.isExpired())
				{
					cleanupBlocks.remove(blocks);
				}
			}
			lastCleanupTime = System.currentTimeMillis();
		}
	}
	
	public void forceCleanup()
	{
		for (BlockList blocks : cleanupBlocks)
		{
			blocks.undo();
		}
		cleanupBlocks.clear();
	}
	
	public void scheduleCleanup(BlockList blocks)
	{
		synchronized(cleanupLock)
		{
			if (lastCleanupTime == 0)
			{
				lastCleanupTime = System.currentTimeMillis();
			}
			cleanupBlocks.add(blocks);
		}
	}
	
	public int getWandTypeId()
	{
		return wandTypeId;
	}
	
	public void cancel()
	{
		for (Spell spell : spells.values())
		{
			spell.cancel();
		}
	}
}
