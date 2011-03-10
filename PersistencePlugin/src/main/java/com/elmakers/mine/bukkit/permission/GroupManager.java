package com.elmakers.mine.bukkit.permission;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.persistence.dao.Group;
import com.elmakers.mine.bukkit.persistence.dao.Message;
import com.elmakers.mine.bukkit.persistence.dao.PlayerData;
import com.elmakers.mine.bukkit.persistence.dao.PluginCommand;
import com.elmakers.mine.bukkit.persistence.dao.ProfileData;
import com.elmakers.mine.bukkit.plugins.persistence.PersistencePlugin;
import com.elmakers.mine.bukkit.utilities.PluginUtilities;
import com.elmakers.mine.craftbukkit.permission.InvalidPermissionProfileException;
import com.elmakers.mine.craftbukkit.permission.PermissionDescriptionNode;
import com.elmakers.mine.craftbukkit.permission.PermissionProfile;
import com.elmakers.mine.craftbukkit.permission.RootPermissionDescription;
import com.elmakers.mine.craftbukkit.persistence.Persistence;

public class GroupManager implements PermissionManager, PermissionHandler
{
	public GroupManager(Server server, Persistence persistence, File dataFolder)
	{
		this.persistence = persistence;
		this.server = server;
		this.dataFolder = dataFolder;
	}
	
	public void initialize()
	{
	    GroupManagerDefaults d = new GroupManagerDefaults();
	    PluginUtilities utilities = PersistencePlugin.getInstance().getUtilities();
	    
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
		groupCommand = utilities.getGeneralCommand(d.groupCommand[0], d.groupCommand[1], d.groupCommand[2]);
		groupCreateCommand = groupCommand.getSubCommand(d.groupCreateCommand[0], d.groupCreateCommand[1], d.groupCreateCommand[2]);
		groupAddCommand = groupCommand.getSubCommand(d.groupAddCommand[0], d.groupAddCommand[1], d.groupAddCommand[2]);
		groupRemoveCommand = groupCommand.getSubCommand(d.groupRemoveCommand[0], d.groupRemoveCommand[1], d.groupRemoveCommand[2]);
		
		denyCommand = utilities.getGeneralCommand(d.denyCommand[0], d.denyCommand[1], d.denyCommand[2]);
		denyPlayerCommand = denyCommand.getSubCommand(d.denyPlayerCommand[0], d.denyPlayerCommand[1], d.denyPlayerCommand[2]);
		denyGroupCommand = denyCommand.getSubCommand(d.denyGroupCommand[0], d.denyGroupCommand[1], d.denyGroupCommand[2]);	
		
		grantCommand = utilities.getGeneralCommand(d.grantCommand[0], d.grantCommand[1], d.grantCommand[2]);
		grantPlayerCommand = grantCommand.getSubCommand(d.grantPlayerCommand[0], d.grantPlayerCommand[1], d.grantPlayerCommand[2]);
		grantGroupCommand = grantCommand.getSubCommand(d.grantGroupCommand[0], d.grantGroupCommand[1], d.grantGroupCommand[2]);

		// Bind commands

		groupCreateCommand.bind("onCreateGroup");
		groupAddCommand.bind("onAddToGroup");
		groupRemoveCommand.bind("onRemoveFromGroup");
		denyPlayerCommand.bind("onDenyPlayer");
		denyGroupCommand.bind("onDenyGroupr");
		grantPlayerCommand.bind("onGrantPlayer");
		grantGroupCommand.bind("onGrantGroup");
	}
	
	public void initializePermissions()
	{		
		if (permissionsInitialized) return;
		permissionsInitialized = true;
		
		// Set up player profiles for permissions
		FileReader loader = null;
		try
		{
			loader = new FileReader(new File(dataFolder, permissionsFile));

			if (!loadProfiles(loader, permissionsFile))
			{
				log.info("Persistence: There's an error with permissions.yml - hopefully more info about that above.");
			}
		}
		catch(FileNotFoundException ex)
		{
			log.info("Persistence: Create a plugins/Persistence/" + permissionsFile + " to use internal permissions");
			loader = null;
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
		
		// Check for player data
		PlayerData user = persistence.get(playerName, PlayerData.class);
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
		
		// Check for player data
		PlayerData user = persistence.get(playerName, PlayerData.class);
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
		
		// Check for player data
		PlayerData user = persistence.get(playerName, PlayerData.class);
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
		
		// Check for player data
		PlayerData user = persistence.get(playerName, PlayerData.class);
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
	
	protected boolean loadProfiles(Reader reader, String filename)
	{
		PermissionProfile[] profiles;
		try
		{
			profiles = PermissionProfile.loadProfiles(this, server, reader);
			log.info("Persistence: loaded " + profiles.length + " profiles from " + filename);
			for (PermissionProfile profile : profiles)
			{
				String profileName = profile.getName();
				if (profileName.equalsIgnoreCase("default"))
				{
					defaultProfile = profile;
				}
				ProfileData profileData = persistence.get(profileName, ProfileData.class);
				if (profileData == null)
				{
					profileData = new ProfileData(profileName);
					persistence.put(profileData);
				}
				
				/// This is setting a transient instance
				profileData.setProfile(profile);
			}
		}
		catch (InvalidPermissionProfileException e)
		{
			log.info(e.getMessage());
			return false;
		}
		
		return true;
	}
	
	public RootPermissionDescription getPermissionRoot(final String path)
	{
		String root = path.split("\\.", 2)[0];
		return permissions.get(root);
	}

	public PermissionDescriptionNode getPermissionPath(final String path)
	{
		RootPermissionDescription root = getPermissionRoot(path);

		/*
		 * TODO: Add a path cache to avoid having to keep searching for nodes It
		 * will be much more efficient. Need to invalidate the cache every time
		 * a plugin changes one of the node descriptions though (not that they
		 * should...)
		 */

		if (root == null)
		{
			throw new IllegalArgumentException("No permissions are defined for " + path);
		}

		return root.getPath(path);
	}
	

	public void addHandler(PermissionHandler handler)
	{
		permissionHandlers.add(handler);
	}
	
	public void addPluginRootPermission(String pluginName, RootPermissionDescription rootNode)
	{
		if (permissions.get(pluginName) != null) return;
	       	
		if (rootNode != null)
		{
			String[] names = rootNode.getNames();
			for (String name : names)
			{
				permissions.put(name, rootNode);
			}
		}
	}
	
	public boolean isSet(Player player, String permissionNode)
	{
		if (defaultProfile != null)
		{
			if (defaultProfile.isSet(permissionNode))
			{
				return true;
			}
		}

		for (RootPermissionDescription rootNodes : permissions.values())
		{
			if (rootNodes.isDefaultSet(permissionNode))
			{
				return true;
			}
		}
		for (PermissionHandler subHandler : permissionHandlers)
		{
			if (subHandler.isSet(player, permissionNode))
			{
				return true;
			}
		}
		return false;
	}
	
	private final Map<String, RootPermissionDescription> permissions = new HashMap<String, RootPermissionDescription>();
	private final List<PermissionHandler> permissionHandlers = new ArrayList<PermissionHandler>();

	protected Persistence									persistence				= null;
	protected Server										server					= null;
	protected File											dataFolder				= null;
	protected PermissionProfile								defaultProfile			= null;

	protected boolean										permissionsInitialized = false;

	private PluginCommand									groupCommand;
	private PluginCommand									groupCreateCommand;
	private PluginCommand									groupRemoveCommand;
	private PluginCommand									groupAddCommand;
	private PluginCommand									denyCommand;
	private PluginCommand									denyPlayerCommand;
	private PluginCommand									denyGroupCommand;
	private PluginCommand									grantCommand;
	private PluginCommand									grantPlayerCommand;
	private PluginCommand									grantGroupCommand;

	private Message											groupExistsMessage;
	private Message											addedPlayerToGroupMessage;
	private Message											removedPlayerFromGroupMessage;
	private Message											createdGroupMessage;
	private Message											denyAccessMessage;
	private Message											grantAccessMessage;
	private Message											playerNotFoundMessage;
	private Message											unknownProfileMessage;
	private Message											groupNotFoundMessage;

	protected static final Logger log = Persistence.getLogger();
	
	// TOOD : support multiple perm files
	private static final String permissionsFile = "permissions.yml";
}
