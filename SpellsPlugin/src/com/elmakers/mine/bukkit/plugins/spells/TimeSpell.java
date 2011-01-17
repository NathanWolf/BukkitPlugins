package com.elmakers.mine.bukkit.plugins.spells;

public class TimeSpell extends Spell {

	@Override
	public boolean onCast(String[] parameters) 
	{
		long targetTime = 0;
		String timeDescription = "day";
		
		if (parameters.length > 0)
		{
			String param = parameters[0];
			if (param.equalsIgnoreCase("night"))
			{
				targetTime = 13000;
				timeDescription = "night";
			}
			else
			{
				try 
				{
					targetTime = Long.parseLong(param);
					timeDescription = "raw: " + targetTime;
				} 
				catch (NumberFormatException ex) 
				{
					targetTime = 0;
				}
			}
		}
		
		setRelativeTime(targetTime);	
		player.sendMessage("Changed time to " + timeDescription);
		
		return true;
	}


	/**
	 * Sets the current server time
	 *
	 * @param time
	 *            time (0-24000)
	 */
	public void setRelativeTime(long time) 
	{
	    long margin = (time - getTime()) % 24000;
	    // Java modulus is stupid.
	    if (margin < 0) 
	    {
	        margin += 24000;
	    }
	   plugin.getServer().setTime(getTime() + margin);
	}

	/**
     * Returns actual server time (-2^63 to 2^63-1)
     *
     * @return time server time
     */
    public long getTime() 
    {
        return plugin.getServer().getTime();
    }
	
	@Override
	public String getName() 
	{
		return "time";
	}

	@Override
	public String getCategory() 
	{
		return "help";
	}

	@Override
	public String getDescription() 
	{
		return "Changes the time of day";
	}

}
