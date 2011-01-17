package com.elmakers.mine.bukkit.plugins.spells;

import net.minecraft.server.EntityFireball;
import net.minecraft.server.WorldServer;
import net.minecraft.server.World;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;

public class FireballSpell extends Spell {

	@Override
	public boolean onCast(String[] parameters) 
	{
		Block target = getTargetBlock();
		Location playerLoc = player.getLocation();
		if (target == null) 
		{
			player.sendMessage("No target");
			return false;
		}
		
		double dx = target.getX() - playerLoc.getX();
		double height = 1;
        double dy = (target.getY() + (double)(height / 2.0F)) - (playerLoc.getY() + (double)(height / 2.0F));
        double dz = target.getZ() - playerLoc.getZ();
		
		player.sendMessage("FOOM!");
		EntityFireball fireball = new EntityFireball(((CraftWorld)player.getWorld()).getHandle());
		
		double d8 = 4D;
        Vec3D vec3d = getLocation(player, 1.0F);
        fireball.p = playerLoc.getX() + vec3d.xCoord * d8;
        fireball.q = playerLoc.getY() + (double)(height / 2.0F) + 0.5D;
        fireball.r = playerLoc.getZ() + vec3d.zCoord * d8;
        
        ((CraftWorld)player.getWorld()).getHandle().a(fireball);
		return true;
	}

	@Override
	public String getName() 
	{
		return "fireball";
	}

	@Override
	public String getDescription() 
	{
		return "Cast an exploding fireball";
	}
	
	public Vec3D getLocation(Player player, float f)
    {
		Location playerLoc = player.getLocation();
    	float rotationYaw = playerLoc.getYaw();
    	float rotationPitch = playerLoc.getPitch();
    	float prevRotationYaw = playerLoc.getYaw();
    	float prevRotationPitch = playerLoc.getPitch();
        if(f == 1.0F)
        {
            float f1 = MathHelper.cos(-rotationYaw * 0.01745329F - 3.141593F);
            float f3 = MathHelper.sin(-rotationYaw * 0.01745329F - 3.141593F);
            float f5 = -MathHelper.cos(-rotationPitch * 0.01745329F);
            float f7 = MathHelper.sin(-rotationPitch * 0.01745329F);
            return Vec3D.createVector(f3 * f5, f7, f1 * f5);
        } else
        {
            float f2 = prevRotationPitch + (rotationPitch - prevRotationPitch) * f;
            float f4 = prevRotationYaw + (rotationYaw - prevRotationYaw) * f;
            float f6 = MathHelper.cos(-f4 * 0.01745329F - 3.141593F);
            float f8 = MathHelper.sin(-f4 * 0.01745329F - 3.141593F);
            float f9 = -MathHelper.cos(-f2 * 0.01745329F);
            float f10 = MathHelper.sin(-f2 * 0.01745329F);
            return Vec3D.createVector(f8 * f9, f10, f6 * f9);
        }
    }

}
