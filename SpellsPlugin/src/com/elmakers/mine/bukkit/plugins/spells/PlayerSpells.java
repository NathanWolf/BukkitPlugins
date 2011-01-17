package com.elmakers.mine.bukkit.plugins.spells;

import org.bukkit.Material;

public class PlayerSpells 
{
	private Material material = Material.AIR;
	private boolean usingMaterial;
	
	public Material getMaterial()
	{
		return material;
	}
	
	public boolean isUsingMaterial()
	{
		return usingMaterial;
	}
	
	public void startMaterialUse(Material mat)
	{
		setMaterial(mat);
		usingMaterial = true;
	}

	public Material finishMaterialUse()
	{
		usingMaterial = false;
		return material;
	}
	
	public void setMaterial(Material mat)
	{
		material = mat;
	}
	
}
