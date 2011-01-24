package com.elmakers.mine.bukkit.plugins.wandmin;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerItemEvent;
import org.bukkit.event.player.PlayerListener;

public class WandPlayerListener extends PlayerListener 
{
	private WandPlugin plugin;
	
	public void setPlugin(WandPlugin plugin)
	{
		this.plugin = plugin;
	}
	
	 /**
     * Called when a player plays an animation, such as an arm swing
     * 
     * @param event Relevant event details
     */
	@Override
    public void onPlayerAnimation(PlayerAnimationEvent event) 
	{
		if (event.getAnimationType() == PlayerAnimationType.ARM_SWING)
		{
			if (event.getPlayer().getInventory().getItemInHand().getTypeId() == plugin.getWandTypeId())
			{
				WandPermissions permissions = plugin.getPermissions(event.getPlayer().getName());	
				if (!permissions.canUse())
				{
					return;
				}
				
				PlayerWandList wands = plugin.getPlayerWands(event.getPlayer());
				Wand wand = wands.getCurrentWand();
				if (wand == null)
				{
					return;
				}
				wand.use(plugin, event.getPlayer());
			}
		}
    }
  
    /**
     * Called when a player uses an item
     * 
     * @param event Relevant event details
     */
	@Override
    public void onPlayerItem(PlayerItemEvent event) 
	{
		if (event.getPlayer().getInventory().getItemInHand().getTypeId() == plugin.getWandTypeId())
		{
			WandPermissions permissions = plugin.getPermissions(event.getPlayer().getName());	
			if (!permissions.canUse())
			{
				return;
			}
			
			PlayerWandList wands = plugin.getPlayerWands(event.getPlayer());
			Wand wand = wands.getCurrentWand();
			if (wand == null)
			{
				return;
			}
			wand.nextCommand();
			event.getPlayer().sendMessage(" " + wand.getName() + " : " + wand.getCurrentCommand().getName());
		}
    }
	
	private void showHelp(PlayerWandList wands)
	{
		Player player = wands.getPlayer();
		WandPermissions permissions = plugin.getPermissions(player.getName());
		player.sendMessage("Usage: /"  + plugin.getWandCommand() + " [command] [parameters]");
		player.sendMessage(" spells : List the spells bound to your wand");
		player.sendMessage(" wands : List all of your wands");
		player.sendMessage(" next : Switch to the next wand");

		if (permissions.canModify())
		{
			player.sendMessage(" create [name] : Create a magic wand");
			player.sendMessage(" bind [command] : Bind a command to your wand");
			player.sendMessage(" unbind [command] : Unbind a command from your wand");
			player.sendMessage(" destroy [name] : Destroy one of your wands");
		}
		
		if (permissions.canAdminister())
		{
			player.sendMessage(" reload : Reload the configuration");
		}
		
	}
	
	/**
     * Commands sent from in game to us.
     *
     * @param player The player who sent the command.
     * @param split The input line split by spaces.
     * @return <code>boolean</code> - True denotes that the command existed, false the command doesn't.
     */
    @Override
    public void onPlayerCommand(PlayerChatEvent event) 
    {
    	WandPermissions permissions = plugin.getPermissions(event.getPlayer().getName());

		if (!permissions.canUse())
		{
			return;
		}
		
    	String[] split = event.getMessage().split(" ");
    	String commandString = split[0];
    	
    	if (!commandString.equalsIgnoreCase("/" + plugin.getWandCommand()))
    	{
    		return;
    	}

    	PlayerWandList wands = plugin.getPlayerWands(event.getPlayer());
    	
    	if (split.length < 2)
    	{
    		showHelp(wands);
    		return;
    	}

    	// No params
   
    	String wandCommand = split[1];
    	if (wandCommand.equalsIgnoreCase("help"))
    	{
    		showHelp(wands);
    		return;
    	}
    	
    	if (wandCommand.equalsIgnoreCase("reload") && permissions.canAdminister())
    	{
    		plugin.load();
    		event.getPlayer().sendMessage("Wands reloaded");
    		return;
    	}

    	if (wandCommand.equalsIgnoreCase("next"))
    	{
    		Wand wand = wands.getCurrentWand();
    		if (wand == null)
    		{
    			event.getPlayer().sendMessage("Create a wand first");
    			return;
    		}
    		wands.nextWand();
    		wand = wands.getCurrentWand();
    		event.getPlayer().sendMessage(" " + wand.getName() + " : " + wand.getCurrentCommand().getName());
    		return;
    	}

    	if (wandCommand.equalsIgnoreCase("wands"))
    	{
    		event.getPlayer().sendMessage("You have " + wands.getWands().size() + " wands:");
    		for (Wand wand : wands.getWands())
    		{
    			String prefix = " ";
    			if (wand == wands.getCurrentWand())
    			{
    				prefix = "*";
    			}
    			String wandMessage = prefix + wand.getName();
    			String wandDescription = wand.getDescription();
    			if (wandDescription != null && wandDescription.length() > 0)
    			{
    				wandMessage = wandMessage + " : " + wandDescription;
    			}
    			event.getPlayer().sendMessage(wandMessage);
    		}
    		return;
    	}
    	
    	if (wandCommand.equalsIgnoreCase("spells"))
    	{
    		Wand wand = wands.getCurrentWand();
    		if (wand == null)
    		{
    			event.getPlayer().sendMessage("Create a wand first");
    			return;
    		}
    		event.getPlayer().sendMessage("You have " + wand.getCommands().size() + " spells on your " + wand.getName() + " wand:");
    		event.getPlayer().sendMessage(wand.getName());
    		for (WandCommand command : wand.getCommands())
    		{
    			String prefix = " ";
    			if (command == wand.getCurrentCommand())
    			{
    				prefix = "*";
    			}
    			String commandMessage = prefix + command.getName();
    			String commandDescription = command.getDescription();
    			if (commandDescription != null && commandDescription.length() > 0)
    			{
    				commandMessage = commandMessage + " : " + commandDescription;
    			}
    			event.getPlayer().sendMessage(commandMessage);
    		}
    		return;
    	}
    	
    	// All mod stuff from here
    	if (!permissions.canModify())
    	{
    		showHelp(wands);
    		return;
    	}
    	
    	// One param
    	if (split.length < 3)
		{
			showHelp(wands);
			return;
		}
    	
    	String parameters = "";
    	for (int i = 2; i < split.length; i++) 
    	{
    		parameters += split[i];
			if (i != split.length - 1)
			{
				parameters += " ";
			}
		}
    	
    	if (wandCommand.equalsIgnoreCase("create"))
    	{
    		String wandName = split[2];
    		wands.addWand(wandName);
    		event.getPlayer().sendMessage("Added wand '" + wandName + "'");
    		return;
    	}
    	
    	if (wandCommand.equalsIgnoreCase("destroy"))
    	{
    		String wandName = split[2];
    		wands.removeWand(wandName);
    		event.getPlayer().sendMessage("Removed wand '" + wandName + "'");
    		return;
    	}

    	// Needs a wand
    	Wand wand = wands.getCurrentWand();
    	if (wand == null)
    	{
    		event.getPlayer().sendMessage("Create a wand first");
    		return;
    	}
    	
    	if (wandCommand.equalsIgnoreCase("bind"))
    	{
    		wand.addCommand(parameters);
    		event.getPlayer().sendMessage("Bound wand '" + wand.getName() + "' to '" + parameters + "'");
    		return;
    	}

    	if (wandCommand.equalsIgnoreCase("unbind"))
    	{
    		wand.removeCommand(parameters);
    		event.getPlayer().sendMessage("Unbound wand '" + wand.getName() + "' from '" + parameters + "'");
    		return;
    	}

    }
    
    /**
     * Called when a player joins a server
     *
     * @param event Relevant event details
     */
    @Override
    public void onPlayerJoin(PlayerEvent event) 
    {
    	PlayerWandList wands = plugin.getPlayerWands(event.getPlayer().getName());
    	wands.setPlayer(event.getPlayer());
    	plugin.save();
    }

    /**
     * Called when a player leaves a server
     *
     * @param event Relevant event details
     */
    @Override
    public void onPlayerQuit(PlayerEvent event) 
    {
    	PlayerWandList wands = plugin.getPlayerWands(event.getPlayer().getName());
    	wands.setPlayer(null);
    	plugin.save();
    }
}
