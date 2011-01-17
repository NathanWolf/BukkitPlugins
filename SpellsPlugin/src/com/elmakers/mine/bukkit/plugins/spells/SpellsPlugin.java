package com.elmakers.mine.bukkit.plugins.spells;
import java.io.File;
import java.util.HashMap;
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

public class SpellsPlugin extends JavaPlugin 
{
	private static final Logger log = Logger.getLogger("Minecraft");
	static final SpellsPlayerListener playerListener = new SpellsPlayerListener();
	static final HashMap<String, Spell> spells = new HashMap<String, Spell>();
	private int currentMaterialType;
	private boolean currentlyUsingMaterial = false;
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
		playerListener.setPlugin(this);
		
        PluginManager pm = getServer().getPluginManager();
		
        pm.registerEvent(Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_ITEM, playerListener, Priority.Normal, this);
        
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
	}

	@Override
	public void onDisable() 
	{
	}
	
	public void listSpells(Player player)
	{
		player.sendMessage("Use: /cast <spell>: ");
		for (Spell spell : spells.values())
		{
			player.sendMessage(" " + spell.getName() + " : " + spell.getDescription());
		}
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
}
