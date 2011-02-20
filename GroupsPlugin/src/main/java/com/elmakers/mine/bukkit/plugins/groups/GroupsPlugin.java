package com.elmakers.mine.bukkit.plugins.groups;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.plugins.groups.dao.Group;
import com.elmakers.mine.bukkit.plugins.groups.dao.ProfileData;
import com.elmakers.mine.bukkit.plugins.groups.dao.User;
import com.elmakers.mine.bukkit.plugins.persistence.Persistence;
import com.elmakers.mine.bukkit.plugins.persistence.PersistencePlugin;
import com.elmakers.mine.bukkit.plugins.persistence.PluginUtilities;
import com.elmakers.mine.bukkit.plugins.persistence.dao.Message;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PermissionType;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PlayerData;
import com.elmakers.mine.bukkit.plugins.persistence.dao.PluginCommand;
import com.nijikokun.bukkit.Permissions.Permissions;

public class GroupsPlugin extends JavaPlugin
{

	/* Process player quit and join messages.
	 * 
	 * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		return utilities.dispatch(this, sender, cmd.getName(), args);
	}
	
	public GroupsPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder,
			File plugin, ClassLoader cLoader)
	{
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
	}

	public void onDisable()
	{
		// TODO Auto-generated method stub

	}

	public void onEnable()
	{
		try
		{
			initialize();
			PluginDescriptionFile pdfFile = this.getDescription();
	        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
		}
		catch(Throwable e)
		{
			PluginDescriptionFile pdfFile = this.getDescription();
	        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " failed to initialize");	
	        e.printStackTrace();
		}	
	}
	
	public void initialize()
	{
		Plugin checkForPersistence = this.getServer().getPluginManager().getPlugin("Persistence");
	    if(checkForPersistence != null) 
	    {
	    	PersistencePlugin plugin = (PersistencePlugin)checkForPersistence;
	    	persistence = plugin.getPersistence();
	    } 
	    else 
	    {
	    	log.warning("The Groups plugin depends on Persistence");
	    	this.getServer().getPluginManager().disablePlugin(this);
	    	return;
	    }
	    
	    // Permissions backward compatibility
	    bindToPermissions();
	    
	    GroupsDefaults d = new GroupsDefaults();
	    utilities = persistence.getUtilities(this);
	    
	    // Messages
		addedPlayerToGroupMessage = utilities.getMessage("addedPlayerToGroup", d.addedPlayerToGroupMessage);
		removedPlayerFromGroupMessage = utilities.getMessage("removedPlayerFromGroup", d.removedPlayerFromGroupMessage);
		createdGroupMessage = utilities.getMessage("createdGroup", d.createdGroupMessage);
		denyAccessMessage = utilities.getMessage("denyAccess", d.denyAccessMessage);
		grantAccessMessage = utilities.getMessage("grantAccess", d.grantAccessMessage);
		groupExistsMessage = utilities.getMessage("groupExistss", d.groupExistsMessage);
		playerNotFoundMessage = utilities.getMessage("playerNotFound", d.playerNotFoundMessage);
		groupNotFoundMessage = utilities.getMessage("groupNotFound", d.groupNotFoundMessage);
		unknownProfileMessage = utilities.getMessage("unknownProfile", d.unknownProfileMessage);

		// Commands
		groupCommand = utilities.getGeneralCommand(d.groupCommand[0], d.groupCommand[1], d.groupCommand[2], PermissionType.ADMINS_ONLY);
		groupCreateCommand = groupCommand.getSubCommand(d.groupCreateCommand[0], d.groupCreateCommand[1], d.groupCreateCommand[2], PermissionType.ADMINS_ONLY);
		groupAddCommand = groupCommand.getSubCommand(d.groupAddCommand[0], d.groupAddCommand[1], d.groupAddCommand[2], PermissionType.ADMINS_ONLY);
		groupRemoveCommand = groupCommand.getSubCommand(d.groupRemoveCommand[0], d.groupRemoveCommand[1], d.groupRemoveCommand[2], PermissionType.ADMINS_ONLY);
		
		denyCommand = utilities.getGeneralCommand(d.denyCommand[0], d.denyCommand[1], d.denyCommand[2], PermissionType.ADMINS_ONLY);
		denyPlayerCommand = denyCommand.getSubCommand(d.denyPlayerCommand[0], d.denyPlayerCommand[1], d.denyPlayerCommand[2], PermissionType.ADMINS_ONLY);
		denyGroupCommand = denyCommand.getSubCommand(d.denyGroupCommand[0], d.denyGroupCommand[1], d.denyGroupCommand[2], PermissionType.ADMINS_ONLY);	
		
		grantCommand = utilities.getGeneralCommand(d.grantCommand[0], d.grantCommand[1], d.grantCommand[2], PermissionType.ADMINS_ONLY);
		grantPlayerCommand = grantCommand.getSubCommand(d.grantPlayerCommand[0], d.grantPlayerCommand[1], d.grantPlayerCommand[2], PermissionType.ADMINS_ONLY);
		grantGroupCommand = grantCommand.getSubCommand(d.grantGroupCommand[0], d.grantGroupCommand[1], d.grantGroupCommand[2], PermissionType.ADMINS_ONLY);

		// Bind commands

		groupCreateCommand.bind("onCreateGroup");
		groupAddCommand.bind("onAddToGroup");
		groupRemoveCommand.bind("onRemoveFromGroup");
		denyPlayerCommand.bind("onDenyPlayer");
		denyGroupCommand.bind("onDenyGroupr");
		grantPlayerCommand.bind("onGrantPlayer");
		grantGroupCommand.bind("onGrantGroup");
	}
	
	public void bindToPermissions()
	{
		Plugin checkForPermissions = this.getServer().getPluginManager().getPlugin("Permissions");
	    if (checkForPermissions != null) 
	    {
	    	User.setPermissions((Permissions)checkForPermissions);
	    	log.info("Groups: Found Permissions, using it for permissions.");
	    }
	}


	public boolean onCreateGroup(CommandSender messageOutput, String[] parameters)
	{
		if (parameters.length == 0)
		{
			return false;
		}
		
		String groupName = parameters[0];
		
		// First check for existing
		Group group = persistence.get(groupName, Group.class);
		if (group != null)
		{
			groupExistsMessage.sendTo(messageOutput, groupName);
			return true;
		}
		
		group = new Group(groupName);
		persistence.put(group);
		createdGroupMessage.sendTo(messageOutput, groupName);
		
		return true;
	}
	
	public User createUser(String playerName)
	{
		User user = null;
		
		// Check for playerData
		PlayerData playerData = persistence.get(playerName, PlayerData.class);
		if (playerData == null)
		{
			return null;
		}
		
		return user;
	}
	
	public boolean onAddToGroup(CommandSender messageOutput, String[] parameters)
	{
		if (parameters.length < 2)
		{
			return false;
		}
		
		String playerName = parameters[0];
		String groupName = parameters[1];
		
		// First check for group
		Group group = persistence.get(groupName, Group.class);
		if (group == null)
		{
			groupNotFoundMessage.sendTo(messageOutput, groupName);
			return true;
		}
		
		// Check for player data, create user if a player exists
		User user = createUser(playerName);
		if (user == null)
		{
			playerNotFoundMessage.sendTo(messageOutput, playerName);
			return true;
		}
		
		user.addToGroup(group);
		persistence.put(user);
		addedPlayerToGroupMessage.sendTo(messageOutput, playerName, groupName);
		
		return true;
	}
	
	// TODO: Less copy+paste! In a hurry....
	public boolean onRemoveFromGroup(CommandSender messageOutput, String[] parameters)
	{
		if (parameters.length < 2)
		{
			return false;
		}
		
		String playerName = parameters[0];
		String groupName = parameters[1];
		
		// First check for group
		Group group = persistence.get(groupName, Group.class);
		if (group == null)
		{
			groupNotFoundMessage.sendTo(messageOutput, group);
			return true;
		}
		
		// Check for player data, create user if a player exists
		User user = createUser(playerName);
		if (user == null)
		{
			playerNotFoundMessage.sendTo(messageOutput, playerName);
			return true;
		}
		
		user.removeFromGroup(group);
		persistence.put(user);
		removedPlayerFromGroupMessage.sendTo(messageOutput, playerName, groupName);
		
		return true;
	}
	
	public boolean onDenyPlayer(CommandSender messageOutput, String[] parameters)
	{
		if (parameters.length < 2)
		{
			return false;
		}
		
		String playerName = parameters[0];
		String profileName = parameters[1];
		
		// First check for permission profile
		ProfileData profileData = persistence.get(profileName, ProfileData.class);
		if (profileData == null)
		{
			unknownProfileMessage.sendTo(messageOutput, profileName);
			return true;
		}
		
		// Check for player
		// Check for player data, create user if a player exists
		User user = createUser(playerName);
		if (user == null)
		{
			playerNotFoundMessage.sendTo(messageOutput, playerName);
			return true;
		}
		
		user.denyPermission(profileData);
		persistence.put(user);
		denyAccessMessage.sendTo(messageOutput, profileName, "player", playerName);
		
		return true;
	}
	
	public boolean onGrantPlayer(CommandSender messageOutput, String[] parameters)
	{
		if (parameters.length < 2)
		{
			return false;
		}
		
		String playerName = parameters[0];
		String profileName = parameters[1];
		
		// First check for permission profile
		ProfileData profileData = persistence.get(profileName, ProfileData.class);
		if (profileData == null)
		{
			unknownProfileMessage.sendTo(messageOutput, profileName);
			return true;
		}
		
		// Check for player
		// Check for player data, create user if a player exists
		User user = createUser(playerName);
		if (user == null)
		{
			playerNotFoundMessage.sendTo(messageOutput, playerName);
			return true;
		}
		
		user.grantPermission(profileData);
		persistence.put(user);
		grantAccessMessage.sendTo(messageOutput, profileName, "player", playerName);
		
		return true;
	}
	
	public boolean onDenyGroup(CommandSender messageOutput, String[] parameters)
	{
		if (parameters.length < 2)
		{
			return false;
		}
		
		String groupName = parameters[0];
		String profileName = parameters[1];
		
		// First check for permission profile
		ProfileData profileData = persistence.get(profileName, ProfileData.class);
		if (profileData == null)
		{
			unknownProfileMessage.sendTo(messageOutput, profileName);
			return true;
		}
		
		// Check for group
		Group group = persistence.get(groupName, Group.class);
		if (group == null)
		{
			groupNotFoundMessage.sendTo(messageOutput, groupName);
			return true;
		}
		
		group.denyPermission(profileData);
		persistence.put(group);
		denyAccessMessage.sendTo(messageOutput, profileName, "group", groupName);
			
		return true;
	}
	
	public boolean onGrantGroup(CommandSender messageOutput, String[] parameters)
	{
		if (parameters.length < 2)
		{
			return false;
		}
		
		String groupName = parameters[0];
		String profileName = parameters[1];
		
		// First check for permission profile
		ProfileData profileData = persistence.get(profileName, ProfileData.class);
		if (profileData == null)
		{
			unknownProfileMessage.sendTo(messageOutput, profileName);
			return true;
		}
		
		// Check for group
		Group group = persistence.get(groupName, Group.class);
		if (group == null)
		{
			groupNotFoundMessage.sendTo(messageOutput, groupName);
			return true;
		}
		
		group.grantPermission(profileData);
		persistence.put(group);
		grantAccessMessage.sendTo(messageOutput, profileName, "group", groupName);
		
		return true;
	}
	
	protected boolean loadProfiles(Reader reader)
	{
		/* waiting for bukkit.permissions...
		 * 
		PermissionProfile[] profiles;
		try
		{
			profiles = PermissionProfile.loadProfiles(server, reader);
			for (PermissionProfile profile : profiles)
			{
				ProfileData profileData = get(profile.getName(), ProfileData.class);
				if (profileData == null)
				{
					profileData = new ProfileData(profile.getName());
					put(profileData);
				}
				
				/// This is setting a transient instance
				profileData.setProfile(profile);
			}
		}
		catch (InvalidPermissionProfileException e)
		{
			return false;
		}
		
		log.info("Persistence: Loaded permission profiles from " + permissionsFile);
		*/
		
		// Set up player profiles for permissions
		FileReader loader = null;
		try
		{
			loader = new FileReader(new File(getDataFolder(), permissionsFile));

			if (!loadProfiles(loader))
			{
				log.info("Persistence: There's an error with permissions.yml - hopefully more info about that above.");
			}
			else
			{
				Persistence.setOpsCanSU(false);
			}
		}
		catch(FileNotFoundException ex)
		{
			log.info("Persistence: " + permissionsFile + " not found, ops have /su access.");
			log.info("Persistence: Create a plugins/Persistence/" + permissionsFile + " to use bukkit.permissions");
			loader = null;
			Persistence.setOpsCanSU(true);
		}
		
		return true;
	}
	
	protected Persistence persistence = null;
	protected PluginUtilities utilities = null;
	
	private PluginCommand groupCommand;
	private PluginCommand groupCreateCommand;
	private PluginCommand groupRemoveCommand;
	private PluginCommand groupAddCommand;
	private PluginCommand denyCommand;
	private PluginCommand denyPlayerCommand;
	private PluginCommand denyGroupCommand;
	private PluginCommand grantCommand;
	private PluginCommand grantPlayerCommand;
	private PluginCommand grantGroupCommand;

	private Message groupExistsMessage;
	private Message addedPlayerToGroupMessage;
	private Message removedPlayerFromGroupMessage;
	private Message createdGroupMessage;
	private Message denyAccessMessage;
	private Message grantAccessMessage;
	private Message playerNotFoundMessage;
	private Message unknownProfileMessage;
	private Message groupNotFoundMessage;

	protected static final Logger log = Persistence.getLogger();
	
	// TOOD : support multiple perm files
	private static final String permissionsFile = "permissions.yml";

}
