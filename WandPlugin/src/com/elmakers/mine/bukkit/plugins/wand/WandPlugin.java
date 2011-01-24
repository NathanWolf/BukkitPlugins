package com.elmakers.mine.bukkit.plugins.wand;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.plugins.spells.SpellsPlugin;
import com.elmakers.mine.bukkit.plugins.wand.utilities.PluginProperties;

public class WandPlugin extends JavaPlugin 
{
	private final String propertiesFile = "wand.properties";

	private int wandTypeId = 280;
	
	private final Logger log = Logger.getLogger("Minecraft");
	private final HashMap<String, WandPermissions> permissions = new HashMap<String, WandPermissions>();
	private final WandPlayerListener playerListener = new WandPlayerListener();
	private SpellsPlugin spells = null;
	
	private boolean allCanUse = true;
	private boolean allCanAdminister = true;
	private boolean allCanModify = true;
	
	public WandPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File dataFolder, File plugin, ClassLoader cLoader) 
	{
		super(pluginLoader, instance, desc, dataFolder, plugin, cLoader);
	}
	 
	@Override
	public void onEnable() 
	{
		load();
		
		bindSpellsPlugin();
		
		playerListener.setPlugin(this);
		
        PluginManager pm = getServer().getPluginManager();
		
        pm.registerEvent(Type.PLAYER_ANIMATION, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_ITEM, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
        
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
	}

	@Override
	public void onDisable() 
	{
		save();
	}
	
	public void bindSpellsPlugin() 
	{
		Plugin checkForSpells = this.getServer().getPluginManager().getPlugin("Spells");

		if (spells == null) 
		{
		    if(checkForSpells != null) 
		    {
		    	this.spells = (SpellsPlugin)checkForSpells;
		    } 
		    else 
		    {
		    	log.warning("The Wand plugin depends on Spells v0.50 or higher - please install it!");
		    	this.getServer().getPluginManager().disablePlugin(this);
		    }
		}
	}
	
	public void loadProperties()
	{
		PluginProperties properties = new PluginProperties(propertiesFile);
		properties.load();
		
		// Get and set all properties
		wandTypeId = properties.getInteger("wand-type-id", wandTypeId);
		String wandUsers = properties.getString("wand-users", "");
		String wandMods = properties.getString("wand-mods", "");
		String wandAdmins = properties.getString("wand-admins", "");
		
		parsePermissions(wandUsers, wandMods, wandAdmins);
		
		properties.save();
	}
	
	protected void parsePermissions(String wandUserString, String wandModString, String wandAdminString)
	{
		permissions.clear();
		
		List<String> wandUsers = parseUserList(wandUserString);
		List<String> wandMods = parseUserList(wandModString);
		List<String> wandAdmins = parseUserList(wandAdminString);
		
		allCanUse = true;
		allCanAdminister = true;
		allCanModify = true;
		
		for (String user : wandUsers)
		{
			allCanUse = false;
			WandPermissions player = getPermissions(user);
			player.setCanUse(true);
		}
		
		for (String mod : wandMods)
		{
			allCanModify = false;
			WandPermissions player = getPermissions(mod);
			player.setCanModify(true);
		}
		
		for (String admin : wandAdmins)
		{
			allCanAdminister = false;
			WandPermissions player = getPermissions(admin);
			player.setCanAdminister(true);
		}
	}
	
	protected List<String> parseUserList(String userList)
	{	
		List<String> users = new ArrayList<String>();
		if (userList == null || userList.length() == 0)
		{
			return users;
		}
		String[] userSplit = userList.split(",");
		
		for (int i = 0; i < userSplit.length; i++)
		{
			String userName = userSplit[i];
			if (userName == null || userName.length() == 0)
			{
				continue;
			}
			users.add(userName.toLowerCase());
		}
		return users;
	}
	
	public WandPermissions getPermissions(String playerName)
	{
		WandPermissions player = permissions.get(playerName.toLowerCase());
		
		if (player == null)
		{
			player = new WandPermissions();
			player.setCanAdminister(allCanAdminister);
			player.setCanModify(allCanModify);
			player.setCanUse(allCanUse);
			permissions.put(playerName, player);
		}
		
		return player;
	}	

    public void save() 
    {
	}

	public void load() 
	{	
		loadProperties();
	}
	
	public int getWandTypeId()
	{
		return wandTypeId;
	}
	
	public SpellsPlugin getSpells()
	{
		return spells;
	}
}
