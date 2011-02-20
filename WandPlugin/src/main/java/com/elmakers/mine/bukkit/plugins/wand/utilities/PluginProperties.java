package com.elmakers.mine.bukkit.plugins.wand.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Material;

public class PluginProperties extends Properties 
{
	static final long serialVersionUID = 0;
	static final Logger log = Logger.getLogger("minecraft");
	private String fileName;
	
	public PluginProperties(String file)
	{
		fileName = file;
	}
	
	public void load()
	{
		File file = new File(fileName);
		if (file.exists())
		{
			try 
			{
				load(new FileInputStream(fileName));
			} 
			catch (IOException ex)
			{
			    log.log(Level.SEVERE, "Unable to load " + fileName, ex);
			}
		}
	}
	
	public void save()
	{
		try 
		{
		    store(new FileOutputStream(fileName), "Minecraft Properties File");
		} 
		catch (IOException ex) 
		{
		    log.log(Level.SEVERE, "Unable to save " + fileName, ex);
		}
	}
	
	public int getInteger(String key, int value)
	{
		if (containsKey(key)) 
		{
            return Integer.parseInt(getProperty(key));
        }

		put(key, String.valueOf(value));
        return value;
	}

	public double getDouble(String key, double value)
	{
		if (containsKey(key)) 
		{
            return Double.parseDouble(getProperty(key));
        }

		put(key, String.valueOf(value));
        return value;
	}
	
	public String getString(String key, String value)
	{
		if (containsKey(key)) 
		{
            return getProperty(key);
        }

		put(key, value);
        return value;
	}
	
	public boolean getBoolean(String key, boolean value)
	{
		if (containsKey(key)) 
		{
            String boolString = getProperty(key);
            return (boolString.length() > 0 && boolString.toLowerCase().charAt(0) == 't');
        }
		put(key, value ? "true" : "false");
        return value;
	}
	
	public List<Material> getMaterials(String key, String csvList)
	{
		if (containsKey(key)) 
		{
			csvList = getProperty(key);
		}
		List<Material> materials = new ArrayList<Material>();
		
		String[] matIds = csvList.split(",");
		for (String matId : matIds)
		{
			int typeId = Integer.parseInt(matId);
			materials.add(Material.getMaterial(typeId));
		}
		put(key, csvList);
		
		return materials;
	}
}
