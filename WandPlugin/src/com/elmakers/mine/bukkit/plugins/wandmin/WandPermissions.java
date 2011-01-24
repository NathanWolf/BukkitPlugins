package com.elmakers.mine.bukkit.plugins.wandmin;

public class WandPermissions 
{
	private boolean use = false;
	private boolean admin = false;
	private boolean modify = false;
	
	public boolean canUse() 
	{
		return use || admin || modify;
	}
	
	public void setCanUse(boolean use) 
	{
		this.use = use;
	}
	
	public boolean canAdminister() 
	{
		return admin;
	}
	
	public void setCanAdminister(boolean admin) 
	{
		this.admin = admin;
		if (admin)
		{
			this.use = true;
			this.modify = true;
		}
	}
	
	public boolean canModify() 
	{
		return modify || admin;
	}
	
	public void setCanModify(boolean modify) 
	{
		this.modify = modify;
		if (modify)
		{
			this.use = true;
		}
	}
}
