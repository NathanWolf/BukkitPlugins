package com.elmakers.mine.bukkit.plugins.wand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.spells.Spells;
import com.elmakers.mine.bukkit.plugins.wand.utilities.PluginProperties;

public class Wands
{

	/*
	 * Public API 
	 */
	
	public void initialize(WandPlugin plugin) 
	{
		this.plugin = plugin;
		load();
	}
	
	public void load() 
	{	
		loadProperties();
	}
	
    public void save() 
    {
	}
    
    /*
     *  Helper functions
     */
	
	public Spells getSpells()
	{
		return spells;
	}
	
	/*
	 * Internal functions, do not call
	 */
	public void setSpells(Spells spells)
	{
		this.spells = spells;
	}
	
	/*
	 * Protected functions
	 */
	protected void loadProperties()
	{
		PluginProperties properties = new PluginProperties(propertiesFile);
		properties.load();
		
		// Get and set all properties
		wandTypeId = properties.getInteger("wand-type-id", wandTypeId);
		String wandUsers = properties.getString("wand-users", "");
		String wandMods = properties.getString("wand-mods", "");
		String wandAdmins = properties.getString("wand-admins", "");
		String wandDenyUsers = properties.getString("wand-deny-users", "");
		
		String itemHelp = properties.getProperty("wand-item-help", "true");
		showItemHelp = itemHelp.equalsIgnoreCase("true");
		
		parsePermissions(wandUsers, wandMods, wandAdmins, wandDenyUsers);
		
		properties.save();
	}
	

	protected void parsePermissions(String wandUserString, String wandModString, String wandAdminString, String wandNoUseString)
	{
		permissions.clear();
		
		List<String> wandUsers = parseUserList(wandUserString);
		List<String> wandMods = parseUserList(wandModString);
		List<String> wandAdmins = parseUserList(wandAdminString);
		List<String> wandNoUse = parseUserList(wandNoUseString);
		
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
		
		for (String noUse : wandNoUse)
		{
			WandPermissions player = getPermissions(noUse);
			player.setCanAdminister(false);
			player.setCanUse(false);
			player.setCanModify(false);
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
		
		Player mcPlayer = plugin.getServer().getPlayer(playerName);
		if (player.canUse() && mcPlayer != null && mcPlayer.isOp())
		{
			player.setCanModify(true);
			player.setCanAdminister(true);
		}
		return player;
	}	

	public int getWandTypeId()
	{
		return wandTypeId;
	}
	
	public boolean showItemHelp()
	{
		return showItemHelp;
	}

	/*
	 * Private data
	 */

	private final String propertiesFile = "wand.properties";
	private int wandTypeId = 280;
	
	private Spells spells = null;
	private WandPlugin plugin = null;
	
	private final HashMap<String, WandPermissions> permissions = new HashMap<String, WandPermissions>();
	
	private boolean allCanUse = true;
	private boolean allCanAdminister = true;
	private boolean allCanModify = true;
	
	private boolean showItemHelp = true;
	

}
