package org.sample;

import com.elmakers.mine.bukkit.plugins.spells.Spells;
import com.elmakers.mine.bukkit.plugins.spells.SpellsPlugin;
import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.sample.SampleSpell;

public class SampleSpellPlugin extends JavaPlugin
{
	public SampleSpellPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder,
			File plugin, ClassLoader cLoader)
	{
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
		// TODO Auto-generated constructor stub
	}

	private Spells	spells	= null;

	@Override
	public void onEnable()
	{
		Plugin checkForSpells = this.getServer().getPluginManager().getPlugin("Spells");

		if (checkForSpells != null)
		{
			SpellsPlugin spellsPlugin = (SpellsPlugin) checkForSpells;
			this.spells = spellsPlugin.getSpells();
		}
		else
		{
			log.warning("The SampleSpell plugin depends on the Spells plugin");
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		}

		spells.addSpell(new SampleSpell());
		
		PluginDescriptionFile pdfFile = this.getDescription();
        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
	}

	@Override
	public void onDisable()
	{
		// TODO Auto-generated method stub

	}

	static private final Logger log = Logger.getLogger("Minecraft");

}
