package com.flansmod.common.guns;

import net.minecraft.item.ItemStack;

public class Paintjob 
{
	public String iconName;
	public String paintjobName;
	public String textureName;
	public ItemStack[] dyesNeeded;
	
	public Paintjob(String iconName, String textureName, ItemStack[] dyesNeeded)
	{
		this.paintjobName = "null";
		this.iconName = iconName;
		this.textureName = textureName;
		this.dyesNeeded = dyesNeeded;
	}
	
	public Paintjob(String iconName, String textureName, String paintjobName, ItemStack[] dyesNeeded)
	{
		this.paintjobName = paintjobName;
		this.iconName = iconName;
		this.textureName = textureName;
		this.dyesNeeded = dyesNeeded;
	}
}
