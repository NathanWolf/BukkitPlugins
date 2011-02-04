package com.elmakers.mine.bukkit.plugins.persistence.dao;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import com.elmakers.mine.bukkit.plugins.persistence.annotation.Persist;
import com.elmakers.mine.bukkit.plugins.persistence.annotation.PersistClass;
/**
 * A class to encapsulate data for a plugin.
 * 
 * Each plugin can register any number of messages and commands.
 * 
 * @author NathanWolf
 *
 */
@PersistClass(schema="global", name="plugin")
public class PluginData
{

	public PluginData()
	{
	}
	
	public PluginData(Plugin plugin)
	{
		update(plugin);
	}
	
	public void update(Plugin plugin)
	{
		PluginDescriptionFile pdfFile = plugin.getDescription();
		id = pdfFile.getName();
		version = pdfFile.getVersion();
		description = pdfFile.getDescription();
		authors = new ArrayList<String>();
		authors.addAll(pdfFile.getAuthors());
		website = pdfFile.getWebsite();
	}

	@Persist
	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	@Persist
	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	@Persist
	public List<String> getAuthors()
	{
		return authors;
	}

	public void setAuthors(List<String> authors)
	{
		this.authors = authors;
	}

	@Persist
	public String getWebsite()
	{
		return website;
	}

	public void setWebsite(String website)
	{
		this.website = website;
	}

	@Persist(id=true)
	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}
	
	protected String			id;
	protected String			version		= null;
	protected String			description	= null;
	protected List<String>		authors		= new ArrayList<String>();
	protected String			website		= null;

}
