package com.flansmod.client.model;

public class GunAnimations 
{
	public static GunAnimations defaults = new GunAnimations();
	
	/** (Purely aesthetic) gun animation variables */
	/** Slide */
	public float gunSlide = 0F, lastGunSlide = 0F;
	/** RecoilAnim */
	public float gunRecoil = 0F, lastGunRecoil = 0F;
	/** Delayed Reload Animations */
	public int timeUntilPump = 0, timeToPumpFor = 0;
	/** Delayed Reload Animations : -1, 1 = At rest, 0 = Mid Animation */
	public float pumped = -1F, lastPumped = -1F;
	/** Delayed Reload Animations : Doing the delayed animation */
	public boolean pumping = false;

	public boolean toggleSlideBack = false;
	public boolean doSlideBack = false;
	public int resetSlideTime = 0;
	
	public boolean reloading = false;
	
	public float reloadAnimationTime = 0;
	
	public float reloadAnimationProgress = 0F, lastReloadAnimationProgress = 0F;

	public float minigunBarrelRotation = 0F;
	public float minigunBarrelRotationSpeed = 0F;
	
	/** Melee animations */
	public int meleeAnimationProgress = 0, meleeAnimationLength = 0;
	
	public GunAnimations()
	{
		
	}
	
	public void update()
	{
		lastPumped = pumped;
		
		if(timeUntilPump > 0)
		{
			timeUntilPump--;
			if(timeUntilPump == 0)
			{
				//Pump it!
				pumping = true;	
				lastPumped = pumped = -1F;
			}
		}
		
		if(pumping)
		{
			pumped += 2F / timeToPumpFor;
			if(pumped >= 0.999F)
				pumping = false;
		}
		
		lastGunSlide = Math.min(1,gunSlide);
		if(doSlideBack && reloading)
			gunSlide = 0.95F - (0.95F - gunSlide) * 0.4F;
		else
		if(gunSlide > 0)
			gunSlide *= 0.46F;
		
		lastGunRecoil = Math.min(1,gunRecoil);
		if(gunRecoil > 0)
			gunRecoil *= 0.71F;
		
		gunSlide=Math.min(1,gunSlide);
		gunRecoil=Math.min(1,gunRecoil);
		
		lastReloadAnimationProgress = reloadAnimationProgress;
		if(reloading)
			reloadAnimationProgress += 1F / reloadAnimationTime;
		if(reloading && reloadAnimationProgress*reloadAnimationTime >= resetSlideTime && doSlideBack)
			doSlideBack = false;
		if(reloading && reloadAnimationProgress >= 1F)
			reloading = false;
		
		minigunBarrelRotation += minigunBarrelRotationSpeed;
		minigunBarrelRotationSpeed *= 0.9F;
		
		if(meleeAnimationLength > 0)
		{
			meleeAnimationProgress++;
			//If we are done, reset
			if(meleeAnimationProgress == meleeAnimationLength)
				meleeAnimationProgress = meleeAnimationLength = 0;
		}
	}
	
	public void doShoot(int pumpDelay, int pumpTime)
	{
		minigunBarrelRotationSpeed += 2F;
		//lastGunSlide = gunSlide = 1F;
		gunSlide = 1.8F;
		gunRecoil = 1.15F;
		timeUntilPump = pumpDelay;
		timeToPumpFor = pumpTime;
	}
		
	public void doReload(int reloadTime, int pumpDelay, int pumpTime, int lockSlideTime)
	{
		reloading = true;
		lastReloadAnimationProgress = reloadAnimationProgress = 0F;
		reloadAnimationTime = reloadTime;
		timeUntilPump = pumpDelay;
		timeToPumpFor = pumpTime;
		doSlideBack = toggleSlideBack;
		resetSlideTime = lockSlideTime;
	}
	
	public void doMelee(int meleeTime)
	{
		meleeAnimationLength = meleeTime;
	}
	
	public void setSlideBack(boolean b)
	{
		toggleSlideBack = b;
	}
}
