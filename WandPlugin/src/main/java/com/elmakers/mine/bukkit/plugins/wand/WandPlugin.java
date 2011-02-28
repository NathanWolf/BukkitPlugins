package com.elmakers.mine.bukkit.plugins.wand;

import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.plugins.spells.SpellVariant;
import com.elmakers.mine.bukkit.plugins.spells.SpellsPlugin;

public class WandPlugin extends JavaPlugin 
{
	private final Wands wands = new Wands();
	private final Logger log = Logger.getLogger("Minecraft");
	private final WandPlayerListener playerListener = new WandPlayerListener();
	 
	public void onEnable() 
	{
		wands.initialize(this);
		
		bindSpellsPlugin();
		
		playerListener.setWands(wands);
		
        PluginManager pm = getServer().getPluginManager();
		
        pm.registerEvent(Type.PLAYER_ANIMATION, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_ITEM, playerListener, Priority.Normal, this);
        
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
	}

	public void onDisable() 
	{
		wands.save();
	}
	
	public void bindSpellsPlugin() 
	{
		Plugin checkForSpells = this.getServer().getPluginManager().getPlugin("Spells");
	    if(checkForSpells != null) 
	    {
	    	SpellsPlugin plugin = (SpellsPlugin)checkForSpells;
	    	wands.setSpells(plugin.getSpells());
	    } 
	    else 
	    {
	    	log.warning("The Wand plugin depends on Spells v0.50 or higher - please install it!");
	    	this.getServer().getPluginManager().disablePlugin(this);
	    }
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] parameters)
	{
		// Spells are all in-game commands...
		if (!(sender instanceof Player)) return false;
		
		Player player = (Player)sender;
		
    	WandPermissions permissions = wands.getPermissions(player.getName());

		if (!permissions.canUse())
		{
			return false;
		}
		
		String commandString = command.getName();
    	if (!commandString.equalsIgnoreCase("wand"))
    	{
    		return false;
    	}

    	if (!permissions.canModify() || !permissions.canModify())
    	{
    		showHelp(player);
    		return true;
    	}
    	
    	if (parameters.length < 1)
    	{
    		boolean gaveWand = false;
  
			Inventory inventory = player.getInventory();
			if (!inventory.contains(wands.getWandTypeId()))
			{
				ItemStack itemStack = new ItemStack(Material.getMaterial(wands.getWandTypeId()), 1);
				player.getInventory().addItem(itemStack);
				gaveWand = true;
				
				CraftPlayer cPlayer = ((CraftPlayer)player);
				cPlayer.getHandle().l();
			}
			
    		if (!gaveWand)
    		{
    			showHelp(player);
    		}
    		else
    		{
    			player.sendMessage("Use /wand again for help, /spells for spell list");
    		}
    		return true;
    	}
    	
    	String spellName = parameters[0];
    	SpellVariant spell = wands.getSpells().getSpell(spellName, player);
    	if (spell == null)
    	{
    		player.sendMessage("Spell '" + spellName + "' unknown, Use /spells for spell list");
    		return true;
    	}
    	
		ItemStack itemStack = new ItemStack(spell.getMaterial(), 1);
		player.getInventory().addItem(itemStack);
		
		CraftPlayer cPlayer = ((CraftPlayer)player);
		cPlayer.getHandle().l();
		
		return true;
	}
	
	
	private void showHelp(Player player)
	{
		WandPermissions permissions = wands.getPermissions(player.getName());
			
		player.sendMessage("How to use your wand:");
		player.sendMessage(" Type /spells to see what spells you know");
		player.sendMessage(" Place a spell item in your first inventory slot");
		player.sendMessage(" Left-click your wand to cast!");
		player.sendMessage(" Right-click to cycle spells in your inventory");

		if (permissions.canModify())
		{
			player.sendMessage("/wand <spellname> : Give the item necessary to cast a spell");
		}
		if (permissions.canAdminister())
		{
			player.sendMessage("/wand reload : Reload the configuration");
		}
		
	}
	
}
