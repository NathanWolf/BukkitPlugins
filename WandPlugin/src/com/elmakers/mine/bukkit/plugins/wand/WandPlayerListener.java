package com.elmakers.mine.bukkit.plugins.wand;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerItemEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import org.bukkit.craftbukkit.entity.CraftPlayer;

import com.elmakers.mine.bukkit.plugins.spells.SpellVariant;
import com.elmakers.mine.bukkit.plugins.spells.Spells;

class WandPlayerListener extends PlayerListener 
{
	private Wands wands;
	
	public void setWands(Wands wands)
	{
		this.wands = wands;
	}
	
	 /**
     * Called when a player plays an animation, such as an arm swing
     * 
     * @param event Relevant event details
     */
	@Override
    public void onPlayerAnimation(PlayerAnimationEvent event) 
	{
		Player player = event.getPlayer();
		if (event.getAnimationType() == PlayerAnimationType.ARM_SWING)
		{
			if (event.getPlayer().getInventory().getItemInHand().getTypeId() == wands.getWandTypeId())
			{
				WandPermissions permissions = wands.getPermissions(player.getName());	
				if (!permissions.canUse())
				{
					return;
				}
				
				Inventory inventory = player.getInventory();
				ItemStack[] contents = inventory.getContents();
				
				SpellVariant spell = null;
				for (int i = 0; i < 9; i++)
				{
					if (contents[i].getType() == Material.AIR || contents[i].getTypeId() == wands.getWandTypeId())
					{
						continue;
					}
					spell = wands.getSpells().getSpell(contents[i].getType(), player);
					if (spell != null)
					{
						break;
					}
				}
				
				if (spell != null)
				{
					wands.getSpells().castSpell(spell, player);
				}
				
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
		int materialId = event.getPlayer().getInventory().getItemInHand().getTypeId();
		Spells spells = wands.getSpells();
		Player player = event.getPlayer();
		WandPermissions permissions = wands.getPermissions(player.getName());

		if (!permissions.canUse())
		{
			return;
		}
		
		if (materialId == wands.getWandTypeId())
		{	
			Inventory inventory = player.getInventory();
			ItemStack[] contents = inventory.getContents();
			ItemStack[] active = new ItemStack[9];
			
			for (int i = 0; i < 9; i++) { active[i] = contents[i]; }
			
			int maxSpellSlot = 0;
			int firstSpellSlot = -1;
			for (int i = 0; i < 9; i++)
			{
				boolean isWand = active[i].getTypeId() == wands.getWandTypeId();
				boolean isSpell = false;
				if (active[i].getType() != Material.AIR)
				{
					SpellVariant spell = spells.getSpell(active[i].getType(), player);
					isSpell = spell != null;
				}
				
				if (isSpell)
				{
					if (firstSpellSlot < 0) firstSpellSlot = i;
					maxSpellSlot = i;
				}
				else
				{
					if (!isWand && firstSpellSlot >= 0)
					{
						break;
					}
				}
				
			}
			
			int numSpellSlots = firstSpellSlot < 0 ? 0 : maxSpellSlot - firstSpellSlot + 1;
			
			if (numSpellSlots < 2)
			{
				return;
			}
			
			for (int ddi = 0; ddi < numSpellSlots; ddi++)
			{
				int i = ddi + firstSpellSlot;
				if (contents[i].getTypeId() != wands.getWandTypeId())
				{
					for (int di = 1; di < numSpellSlots; di++)
					{
						int dni = (ddi + di) % numSpellSlots;
						int ni = dni + firstSpellSlot;
						if (active[ni].getTypeId() != wands.getWandTypeId())
						{
							contents[i] = active[ni];
							break;
						}
					}
				}
			}
			
			inventory.setContents(contents);
			CraftPlayer cPlayer = ((CraftPlayer)event.getPlayer());
			cPlayer.getHandle().l();
		}
		else
		{
			// Check for magic item
			Inventory inventory = player.getInventory();
			ItemStack[] contents = inventory.getContents();
			
			boolean inInventory = false;
			boolean foundInventory = false;
			SpellVariant spell = null;
			boolean hasWand = false;
			
			for (int i = 0; i < 9; i++)
			{
				if (contents[i].getTypeId() == wands.getWandTypeId())
				{
					hasWand = true;
					continue;
				}
				
				if (contents[i].getType() != Material.AIR)
				{
					SpellVariant ispell = spells.getSpell(contents[i].getType(), player);

					if (!foundInventory)
					{
						if (!inInventory)
						{
							if (ispell != null)
							{
								inInventory = true;
							}
						}
						else
						{
							if (ispell == null)
							{
								inInventory = false;
								foundInventory = true;
							}
						}
					}
					
					if (inInventory && i == player.getInventory().getHeldItemSlot())
					{
						spell = ispell;
					}
				}
			}

			if (hasWand && spell != null)
			{
				player.sendMessage(spell.getName() + " : " + spell.getDescription());
			}
		}
    }

}
