package com.elmakers.mine.bukkit.plugins.spells;

import java.util.TimerTask;

public class SpellsCleanupTask extends TimerTask 
{
	private SpellsPlugin plugin;
	private long started;
	
	public SpellsCleanupTask(SpellsPlugin plugin)
	{
		this.plugin = plugin;
		this.started = System.currentTimeMillis();
	}
	
	public void run()
	{
		plugin.cleanup(this);
	}
	
	public long getTimeStarted()
	{
		return started;
	}
}
