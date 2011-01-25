package com.elmakers.mine.bukkit.plugins.spells;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.plugins.groups.Permissions;
import com.elmakers.mine.bukkit.plugins.groups.PlayerPermissions;
import com.elmakers.mine.bukkit.plugins.spells.builtin.*;
import com.elmakers.mine.bukkit.plugins.spells.dynmap.MapSpell;
import com.elmakers.mine.bukkit.plugins.spells.utilities.BlockList;
import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;
import com.elmakers.mine.bukkit.plugins.spells.utilities.UndoQueue;

import org.dynmap.DynmapPlugin;

public class SpellsPlugin extends JavaPlugin
{
	/*
	 * Public API - Use for hooking up a plugin, or calling a spell
	 */
		
	public SpellVariant getSpell(Material material, String playerName)
	{
		PlayerPermissions permissions = getPermissions(playerName);
		SpellVariant spell = spellsByMaterial.get(material);
		if (spell != null && !permissions.hasPermission(spell.getName())) return null;
		return spell;
	}
	
	public PlayerPermissions getPermissions(String playerName)
	{
		return permissions.getPlayerPermissions(playerName);
	}
	
	public PlayerSpells getPlayerSpells(Player player)
	{
		PlayerSpells spells = playerSpells.get(player.getName());
		if (spells == null)
		{
			spells = new PlayerSpells();
			playerSpells.put(player.getName(), spells);
		}
		return spells;
	}
	
	public SpellVariant getSpell(String name, String playerName)
	{
		PlayerPermissions permissions= getPermissions(playerName);
		if (!permissions.hasPermission(name)) return null;
		return spellVariants.get(name);
	}
	
	public boolean castSpell(SpellVariant spell, Player player)
	{
		return spell.cast(player);
	}
	
	public void addSpell(Spell spell)
	{
		List<SpellVariant> variants = spell.getVariants();
		for (SpellVariant variant : variants)
		{
			SpellVariant conflict = spellVariants.get(variant.getName());
			if (conflict != null)
			{
				log.log(Level.WARNING, "Duplicate spell name: '" + conflict.getName() + "'");
			}
			else
			{
				spellVariants.put(variant.getName(), variant);
			}
			conflict = spellsByMaterial.get(variant.getMaterial());
			if (conflict != null)
			{
				log.log(Level.WARNING, "Duplicate spell material: '" + conflict.getMaterial().name() + "'");
			}
			else
			{
				spellsByMaterial.put(variant.getMaterial(), variant);
			}
		}
		
		spells.add(spell);
		spell.setPlugin(this);
	}
	
	/*
	 * Material use system
	 */
	
	public List<Material> getBuildingMaterials()
	{
		return buildingMaterials;
	}
	
	public void setCurrentMaterialType(Player player, Material material, byte data)
	{
		PlayerSpells spells = getPlayerSpells(player);
		if 
		(
				spells.isUsingMaterial() 
		&& 		(spells.getMaterial() != material || spells.getData() != data)
		)
		{
			spells.setMaterial(material);
			spells.setData(data);
			player.sendMessage("Now using " + material.name().toLowerCase());
			// Must allow listeners to remove themselves during the event!
			List<Spell> active = new ArrayList<Spell>();
			active.addAll(materialListeners);
			for (Spell listener : active)
			{
				listener.onMaterialChoose(player);
			}
		}

	}
	
	public void startMaterialUse(Player player, Material material, byte data)
	{
		PlayerSpells spells = getPlayerSpells(player);
		spells.startMaterialUse(material, data);
	}
	
	public Material finishMaterialUse(Player player)
	{
		PlayerSpells spells = getPlayerSpells(player);
		return spells.finishMaterialUse();
	}
	
	public byte getMaterialData(Player player)
	{
		PlayerSpells spells = getPlayerSpells(player);
		return spells.getData();
	}
	
	/*
	 * Undo system 
	 */
	
	public UndoQueue getUndoQueue(String playerName)
	{
		UndoQueue queue = playerUndoQueues.get(playerName);
		if (queue == null)
		{
			queue = new UndoQueue();
			queue.setMaxSize(undoQueueDepth);
			playerUndoQueues.put(playerName, queue);
		}
		return queue;
	}
	
	public void addToUndoQueue(Player player, BlockList blocks)
	{
		UndoQueue queue = getUndoQueue(player.getName());
		queue.add(blocks);
	}
	
	public boolean undo(String playerName)
	{
		UndoQueue queue = getUndoQueue(playerName);
		return queue.undo();
	}
	
	public BlockList getLastBlockList(String playerName)
	{
		UndoQueue queue = getUndoQueue(playerName);
		return queue.getLast();
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
			if (cleanupBlocks.size() == 0)
			{
				lastCleanupTime = System.currentTimeMillis();
			}
			cleanupBlocks.add(blocks);
		}
	}
	
	/*
	 * Event registration- call to listen for events
	 */
	
	public void registerEvent(SpellEventType type, Spell spell)
	{
		switch (type)
		{
			case PLAYER_MOVE:
				if (!movementListeners.contains(spell)) movementListeners.add(spell);
				break;
			case MATERIAL_CHANGE:
				if (!materialListeners.contains(spell)) materialListeners.add(spell);
				break;
			case PLAYER_QUIT:
				if (!quitListeners.contains(spell)) quitListeners.add(spell);
				break;		}
	}
	
	public void unregisterEvent(SpellEventType type, Spell spell)
	{
		switch (type)
		{
			case PLAYER_MOVE:
				movementListeners.remove(spell);
				break;
			case MATERIAL_CHANGE:
				materialListeners.remove(spell);
				break;
			case PLAYER_QUIT:
				quitListeners.remove(spell);
				break;
		}
	}

	/*
	 * Random utility functions
	 */
	
	public int getWandTypeId()
	{
		return wandTypeId;
	}
	
	public void cancel(Player player)
	{
		for (Spell spell : spells)
		{
			spell.cancel(this, player);
		}
	}
	
	public boolean allowCommandUse()
	{
		return allowCommands;
	}	
	
	public boolean isQuiet()
	{
		return quiet;
	}

	public boolean isSilent()
	{
		return silent;
	}
	
	/*
	 * dynmap access functions
	 */
	public boolean isDynmapBound()
	{
		return dynmap != null;
	}
	
	public DynmapPlugin getDynmapPlugin()
	{
		return dynmap;
	}
	
	/*
	 * Internal functions - don't call these, or really anything below here.
	 */
	
	public void onPlayerQuit(PlayerEvent event)
	{
		// Must allow listeners to remove themselves during the event!
		List<Spell> active = new ArrayList<Spell>();
		active.addAll(quitListeners);
		for (Spell listener : active)
		{
			listener.onPlayerQuit(event);
		}
	}
	
	public void onPlayerMove(PlayerMoveEvent event)
	{
		// Must allow listeners to remove themselves during the event!
		List<Spell> active = new ArrayList<Spell>();
		active.addAll(movementListeners);
		for (Spell listener : active)
		{
			listener.onPlayerMove(event);
		}
	}
	
	/* 
	 * Help commands
	 */

	public void listSpellsByCategory(Player player, String category, PlayerPermissions playerPermissions)
	{
		List<SpellVariant> spells = new ArrayList<SpellVariant>();
		
		for (SpellVariant spell : spellVariants.values())
		{
			if (spell.getCategory().equalsIgnoreCase(category) && playerPermissions.hasPermission(spell.getName()))
			{
				spells.add(spell);
			}
		}
		
		if (spells.size() == 0)
		{
			player.sendMessage("You don't know any spells");
			return;
		}
		
		Collections.sort(spells);
		for (SpellVariant spell : spells)
		{
			player.sendMessage(spell.getName() + " [" + spell.getMaterial().name().toLowerCase() + "] : " + spell.getDescription());
		}
	}
	
	public void listCategories(Player player, PlayerPermissions playerPermissions)
	{
		HashMap<String, Integer> spellCounts = new HashMap<String, Integer>();
		List<String> spellGroups = new ArrayList<String>();
		
		for (SpellVariant spell : spellVariants.values())
		{
			if (!playerPermissions.hasPermission(spell.getName())) continue;
			
			Integer spellCount = spellCounts.get(spell.getCategory());
			if (spellCount == null || spellCount == 0)
			{
				spellCounts.put(spell.getCategory(), 1);
				spellGroups.add(spell.getCategory());
			}
			else
			{
				spellCounts.put(spell.getCategory(), spellCount + 1);
			}
		}
		if (spellGroups.size() == 0)
		{
			player.sendMessage("You don't know any spells");
			return;
		}
		
		Collections.sort(spellGroups);
		for (String group : spellGroups)
		{
			player.sendMessage(group + " [" + spellCounts.get(group) + "]");
		}
	}
	
	public void listSpells(Player player, PlayerPermissions playerPermissions)
	{
		HashMap<String, SpellGroup> spellGroups = new HashMap<String, SpellGroup>();
	
		for (SpellVariant spell : spellVariants.values())
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
			for (SpellVariant spell : group.spells)
			{
				if (playerPermissions.hasPermission(spell.getName()))
				{
					player.sendMessage(" " + spell.getName() + " [" + spell.getMaterial().name().toLowerCase() + "] : " + spell.getDescription());
				}
			}
		}
	}
	
	/*
	 * Saving and loading
	 */
	
	public void load()
	{
		loadProperties();
	}

	protected void loadProperties()
	{
		PluginProperties properties = new PluginProperties(propertiesFile);
		properties.load();
		
		permissionsFile = properties.getString("spells-general-classes-file", permissionsFile);
		undoQueueDepth = properties.getInteger("spells-general-undo-depth", undoQueueDepth);
		silent = properties.getBoolean("spells-general-silent", silent);
		quiet = properties.getBoolean("spells-general-quiet", quiet);
		allowCommands = properties.getBoolean("spells-general-allow-commands", allowCommands);
		buildingMaterials = properties.getMaterials("spells-general-building", DEFAULT_BUILDING_MATERIALS);
		
		permissions.load(permissionsFile);
		
		for (Spell spell : spells)
		{
			spell.onLoad(properties);
		}
		
		properties.save();
		
		// Load wand properties as well, in case that plugin exists.
		properties = new PluginProperties(wandPropertiesFile);
		properties.load();
		
		// Get and set all properties
		wandTypeId = properties.getInteger("wand-type-id", wandTypeId);
		
		// Don't save the wand properties!!
	}
	
	/*
	 * Plugin interface
	 */
	
	@Override
	public void onEnable() 
	{
		bindDynmapPlugin();
		addBuiltinSpells();
		load();
		listener.setPlugin(this);
		playerListener.setMaster(listener);
		entityListener.setMaster(listener);
		
        PluginManager pm = getServer().getPluginManager();
		
        pm.registerEvent(Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_ITEM, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_ANIMATION, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
        /*
        pm.registerEvent(Type.ENTITY_DAMAGED, entityListener, Priority.Normal, this);
        pm.registerEvent(Type.ENTITY_COMBUST, entityListener, Priority.Normal, this);
        pm.registerEvent(Type.ENTITY_DAMAGEDBY_BLOCK, entityListener, Priority.Normal, this);
        pm.registerEvent(Type.ENTITY_DAMAGEDBY_ENTITY, entityListener, Priority.Normal, this);
        pm.registerEvent(Type.ENTITY_DAMAGEDBY_PROJECTILE, entityListener, Priority.Normal, this);
        pm.registerEvent(Type.ENTITY_EXPLODE, entityListener, Priority.Normal, this);
        */
        
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
	}

	@Override
	public void onDisable() 
	{
		forceCleanup();
		movementListeners.clear();
		materialListeners.clear();
		quitListeners.clear();
		spells.clear();
		spellVariants.clear();
		spellsByMaterial.clear();
	}
	
	/*
	 * Private data
	 */
	private final String propertiesFile = "spells.properties";
	private String permissionsFile = "spell-classes.txt";
	
	private final String wandPropertiesFile = "wand.properties";
	private int wandTypeId = 280;
	
	static final String		DEFAULT_BUILDING_MATERIALS	= "0,1,2,3,4,5,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,24,25,35,41,42,43,45,46,47,48,49,56,57,60,73,74,79,80,81,82,83,85,86,87,88,89,91";

	private List<Material>	buildingMaterials	= new ArrayList<Material>();
	
	private final List<BlockList> cleanupBlocks = new ArrayList<BlockList>();
	private final Object cleanupLock = new Object();
	private long lastCleanupTime = 0;
	
	private int undoQueueDepth = 256;
	private boolean silent = false;
	private boolean quiet = false;
	private boolean allowCommands = true;
	private HashMap<String, UndoQueue> playerUndoQueues =  new HashMap<String, UndoQueue>();
	
	private final Logger log = Logger.getLogger("Minecraft");
	private final Permissions permissions = new Permissions();
	private final SpellsMasterListener listener = new SpellsMasterListener();
	private final SpellsPlayerListener playerListener = new SpellsPlayerListener();
	private final SpellsEntityListener entityListener = new SpellsEntityListener();
	private final HashMap<String, SpellVariant> spellVariants = new HashMap<String, SpellVariant>();
	private final HashMap<Material, SpellVariant> spellsByMaterial = new HashMap<Material, SpellVariant>();
	private final List<Spell> spells = new ArrayList<Spell>();
	private final HashMap<String, PlayerSpells> playerSpells = new HashMap<String, PlayerSpells>();
	private final List<Spell> movementListeners = new ArrayList<Spell>();
	private final List<Spell> materialListeners = new ArrayList<Spell>();
	private final List<Spell> quitListeners = new ArrayList<Spell>();
	private DynmapPlugin dynmap = null;
	
	/*
	 * Constructor - add default spells.
	 */
	public SpellsPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File dataFolder, File plugin, ClassLoader cLoader) 
	{
		super(pluginLoader, instance, desc, dataFolder, plugin, cLoader);
	}
	
	protected void addBuiltinSpells()
	{
		addSpell(new HealSpell());
		addSpell(new BlinkSpell());
		addSpell(new TorchSpell());
		addSpell(new ExtendSpell());
		addSpell(new FireballSpell());
		addSpell(new TowerSpell());
		addSpell(new PillarSpell());
		addSpell(new BridgeSpell());
		addSpell(new AbsorbSpell());
		addSpell(new FillSpell());
		addSpell(new CushionSpell());
		addSpell(new TunnelSpell());
		addSpell(new UndoSpell());
		addSpell(new AlterSpell());
		addSpell(new StairsSpell());
		addSpell(new BlastSpell());
		addSpell(new MineSpell());
		addSpell(new TreeSpell());
		addSpell(new ArrowSpell());
		addSpell(new FrostSpell());
		addSpell(new GillsSpell());
		addSpell(new FamiliarSpell());
		addSpell(new ConstructSpell());
		addSpell(new TransmuteSpell());
		addSpell(new RecallSpell());
		addSpell(new DisintegrateSpell());
		
		// dynmap spells
		if (isDynmapBound())
		{
			addSpell(new MapSpell());
		}
	}
	
	protected void bindDynmapPlugin() 
	{
		Plugin checkForMap = this.getServer().getPluginManager().getPlugin("dynmap");

		if (dynmap == null) 
		{
		    if (checkForMap != null) 
		    {
		    	this.dynmap = (DynmapPlugin)checkForMap;
		    	log.info("Spells: Found dynmap plugin, binding to it");
		    }
		}
	}
	
}
