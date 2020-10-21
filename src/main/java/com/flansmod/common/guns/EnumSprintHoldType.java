package com.flansmod.common.guns;

public enum EnumSprintHoldType 
{
	SIMPLE, RAISE, LOWER, PISTOL, RIFLE, HOLSTER, HEAVY, NONE;
	
	public static EnumSprintHoldType getSprintHoldType(String s)
	{
		s = s.toLowerCase();
		if(s.equals("raise"))
			return RAISE;
		if(s.equals("lower"))
			return LOWER;
		if(s.equals("pistol"))
			return PISTOL;
		if(s.equals("rifle"))
			return RIFLE;
		if(s.equals("holster"))
			return HOLSTER;
		if(s.equals("heavy"))
			return HEAVY;
		if(s.equals("none"))
			return NONE;
		return SIMPLE;
	}
}
