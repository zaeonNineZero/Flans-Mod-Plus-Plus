package com.flansmod.common.guns;

public enum EnumAmmoMode 
{
	EXACT, PERCENT;
	
	public static EnumAmmoMode getAmmoMode(String s)
	{
		s = s.toLowerCase();
		if(s.equals("percent") || s.equals("percentage"))
			return PERCENT;
		return EXACT;
	}
}
