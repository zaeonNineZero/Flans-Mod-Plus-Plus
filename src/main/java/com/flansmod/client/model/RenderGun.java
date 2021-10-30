package com.flansmod.client.model;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.IItemRenderer;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.FlansModResourceHandler;
import com.flansmod.common.FlansMod;
import com.flansmod.common.PlayerData;
import com.flansmod.common.PlayerHandler;
import com.flansmod.common.guns.AttachmentType;
import com.flansmod.common.guns.EnumFireMode;
import com.flansmod.common.guns.EnumSprintHoldType;
import com.flansmod.common.guns.GunType;
import com.flansmod.common.guns.IScope;
import com.flansmod.common.guns.ItemBullet;
import com.flansmod.common.guns.ItemGun;
import com.flansmod.common.vector.Vector3f;

import cpw.mods.fml.relauncher.Side;

public class RenderGun implements IItemRenderer
{
	private static TextureManager renderEngine;
	
	public static float smoothing;
	
	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) 
	{
		switch(type)
		{
		case ENTITY : if(!Minecraft.getMinecraft().gameSettings.fancyGraphics) return false;
		case EQUIPPED : case EQUIPPED_FIRST_PERSON :  /*case INVENTORY : */return item != null && item.getItem() instanceof ItemGun && ((ItemGun)item.getItem()).type.model != null;
		default : break;
		}
		return false;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) 
	{
		return false;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) 
	{
		//Avoid any broken cases by returning
		if(!(item.getItem() instanceof ItemGun))
			return;
		
		RenderBlocks renderBlocks = (RenderBlocks)data[0];
		
		
		GunType gunType = ((ItemGun)item.getItem()).type;
		if(gunType == null)
			return;
		
		ModelGun model = gunType.model;
		if(model == null)
			return;

		//Render main hand gun
		{
			GunAnimations animations = type == ItemRenderType.ENTITY ? new GunAnimations() : FlansModClient.getGunAnimations((EntityLivingBase)data[1], false);
			renderGun(type, item, gunType, animations, false, data);
		}
		
		//Render off-hand gun
		if(gunType.oneHanded && type == ItemRenderType.EQUIPPED_FIRST_PERSON)
		{
			EntityLivingBase entity = (EntityLivingBase)data[1];
			if(entity instanceof EntityPlayer)
			{
				EntityPlayer player = (EntityPlayer)entity;
				PlayerData playerData = PlayerHandler.getPlayerData(player, Side.CLIENT);
				if(playerData.offHandGunSlot != 0)
				{
					GunAnimations animations = FlansModClient.gunAnimationsLeft.get(data[1]);
					if(animations == null)
					{
						animations = new GunAnimations();
						FlansModClient.gunAnimationsLeft.put((EntityLivingBase)data[1], animations);
					}
					ItemStack offHandItem = player.inventory.getStackInSlot(playerData.offHandGunSlot - 1);
					if(offHandItem == null || !(offHandItem.getItem() instanceof ItemGun))
						return;
					GunType offHandGunType = ((ItemGun)offHandItem.getItem()).type;
					if(!offHandGunType.oneHanded)
						return;
					
					renderGun(type, offHandItem, offHandGunType, animations, true, data);
				}
			
			}
		}
	}
	
	//Render off-hand gun in 3rd person
	public void renderOffHandGun(EntityPlayer player, ItemStack offHandItemStack)
	{
		GunAnimations animations = FlansModClient.gunAnimationsLeft.get(player);
		if(animations == null)
		{
			animations = new GunAnimations();
			FlansModClient.gunAnimationsLeft.put(player, animations);
		}
		GunType offHandGunType = ((ItemGun)offHandItemStack.getItem()).type;
		if(!offHandGunType.oneHanded)
			return;
		
		renderGun(ItemRenderType.EQUIPPED, offHandItemStack, offHandGunType, animations, true, player);
	}
		
	private void renderGun(ItemRenderType type, ItemStack item, GunType gunType, GunAnimations animations, boolean offHand, Object... data)
	{
		//The model scale
		float f = 1F / 16F;
		ModelGun model = gunType.model;
		
		int flip = offHand ? -1 : 1;
		
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.001F);
		GL11.glEnable(GL11.GL_BLEND);
		int srcBlend = GL11.glGetInteger(GL11.GL_BLEND_SRC);
		int dstBlend = GL11.glGetInteger(GL11.GL_BLEND_DST);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glPushMatrix();
		{
			//Get the reload animation rotation
			float reloadRotate = 0F;
			
			//Setup transforms based on gun position
			switch(type)
			{
				case ENTITY :
				{
					EntityItem entity = (EntityItem)data[1];
					GL11.glRotatef((entity.age * 2) + (entity.age == 0 ? 0 : smoothing), 0F, 1F, 0F);
					GL11.glTranslatef(-0.2F + model.itemFrameOffset.x, 0.2F + model.itemFrameOffset.y, 0.1F + model.itemFrameOffset.z);
					
					break;
				}
				case EQUIPPED:
				{
					if(offHand)
					{
						GL11.glRotatef(-70F, 1F, 0F, 0F);
						GL11.glRotatef(48F, 0F, 0F, 1F);
						GL11.glRotatef(105F, 0F, 1F, 0F);
						GL11.glTranslatef(-0.1F, -0.22F, -0.15F);
					}
					else
					{
						GL11.glRotatef(35F, 0F, 0F, 1F);
						GL11.glRotatef(-5F, 0F, 1F, 0F);
						GL11.glTranslatef(0.75F, -0.22F, -0.08F);
						GL11.glScalef(1F, 1F, -1F);
					}
					GL11.glTranslatef(model.thirdPersonOffset.x, model.thirdPersonOffset.y, model.thirdPersonOffset.z);
					
					/*
					if(animations.meleeAnimationProgress > 0 && animations.meleeAnimationProgress < gunType.meleePath.size()) 
					{
						Vector3f meleePos = gunType.meleePath.get(animations.meleeAnimationProgress);
						Vector3f nextMeleePos = animations.meleeAnimationProgress + 1 < gunType.meleePath.size() ? gunType.meleePath.get(animations.meleeAnimationProgress + 1) : new Vector3f();
						GL11.glTranslatef(meleePos.x + (nextMeleePos.x - meleePos.x) * smoothing, meleePos.y + (nextMeleePos.y - meleePos.y) * smoothing, meleePos.z + (nextMeleePos.z - meleePos.z) * smoothing);
					}
					*/
					break;
				}
				case EQUIPPED_FIRST_PERSON:
				{
					IScope scope = gunType.getCurrentScope(item);
					AttachmentType scopeAttachmentFP = gunType.getScope(item);
					GunType gunTypeFirst = ((ItemGun)item.getItem()).type;
					if(gunTypeFirst == null)
					return;
					GL11.glRotatef(gunTypeFirst.unscopedRotation, 0F, 0F, 1F); 
					if(FlansModClient.zoomProgress > 0.9F && scope.hasZoomOverlay())
					{
						GL11.glPopMatrix();

						GL11.glBlendFunc(srcBlend, dstBlend);
						GL11.glDisable(GL11.GL_BLEND);
						return;
					}
					float adsSwitch = FlansModClient.lastZoomProgress + (FlansModClient.zoomProgress - FlansModClient.lastZoomProgress) * smoothing;//0F;//((float)Math.sin((FlansMod.ticker) / 10F) + 1F) / 2F;
					float adsCompression = 1F;
					float zoomCriteria = 1.5F;
					
					/*
					if (scope.getFOVFactor()>zoomCriteria)
					adsCompression = Math.max(1+(scope.getFOVFactor() * ((adsSwitch*adsSwitch)/1.0F)),0.1F);
					else
					adsCompression = Math.max(1+(scope.getFOVFactor() * ((adsSwitch*adsSwitch)*0.4F)),0.1F);
					*/
				
					float sprintProgress = FlansModClient.lastSprintProgress + (FlansModClient.sprintProgress - FlansModClient.lastSprintProgress) * smoothing;
					float sprintProgressTrans = sprintProgress/3;
					
					
					//Hold Positioning
					if(offHand)
					{
						GL11.glTranslatef(0F, 0.03F, -0.76F);
						GL11.glRotatef(23F, 0F, 0F, 1F); 
						GL11.glRotatef(-4F, 0F, 1F, 0F);
						GL11.glTranslatef(0.15F, 0.2F - (2F * adsSwitch), -0.6F);
						
						if(scopeAttachmentFP != null)
						GL11.glTranslatef(0F, (gunTypeFirst.sightsYShiftScoped / 16F), 0F);
						else
						if(scopeAttachmentFP == null)
						GL11.glTranslatef(0F, (gunTypeFirst.sightsYShiftUnscoped / 16F), 0F);
					
						GL11.glTranslatef((gunTypeFirst.sightsXShift / 16F), 0F, ((gunTypeFirst.hipZShift + 1) / 16F));
					}
					else
					{
						GL11.glRotatef(25F - 5F * adsSwitch, 0F, 0F, 1F); 
						GL11.glRotatef(-5F, 0F, 1F, 0F);
						GL11.glTranslatef(0.15F, 0.2F + 0.175F * adsSwitch, -0.6F - 0.405F * adsSwitch);
						if(gunType.hasScopeOverlay)
							GL11.glTranslatef(-0.3F * adsSwitch, 0F, 0F);
						GL11.glRotatef(4.5F * adsSwitch, 0F, 0F, 1F);
						GL11.glTranslatef(0F, -0.03F * adsSwitch, 0F);
						
						if(scopeAttachmentFP != null)
						GL11.glTranslatef((gunTypeFirst.sightsXScopedShift / 16F) * adsSwitch, (gunTypeFirst.sightsYShiftScoped / 16F) * adsSwitch, 0F);
						else
						if(scopeAttachmentFP == null)
						GL11.glTranslatef(0F, (gunTypeFirst.sightsYShiftUnscoped / 16F) * adsSwitch, 0F);
					
						GL11.glTranslatef((gunTypeFirst.sightsXShift / 16F) * adsSwitch, 0F, (-(gunTypeFirst.hipZShift + 1) / 16F) * (1 - adsSwitch));
					}
					
					
					//Adjustment variables
					GL11.glTranslatef(gunTypeFirst.gunXOffset / 16F, 0F, 0F);
					GL11.glTranslatef(0F, 0F, gunTypeFirst.gunZOffset / 16F);
		
					if(scopeAttachmentFP != null)
					{
						GL11.glTranslatef(0F, -scopeAttachmentFP.model.renderOffset / 16F, 0F);
						GL11.glTranslatef(0F, gunTypeFirst.scopedYOffset / 16F, 0F);
					}
					else
					if(scopeAttachmentFP == null)
					{
						GL11.glTranslatef(0F, gunTypeFirst.unscopedYOffset / 16F, 0F);
					}
					
					
					//Model compression when aiming the gun
					
					//if(scopeAttachmentFP != null)
					//GL11.glScalef(1F/Math.max(1+(scopeAttachmentFP.getFOVFactor() * (adsSwitch/2)),0.1F), 1F, 1F);
					//else
					//GL11.glScalef(1F/Math.max(1+(gunTypeFirst.getFOVFactor() * (adsSwitch/2)),0.1F), 1F, 1F);
					if (scope.getFOVFactor()>zoomCriteria)
					{
					GL11.glScalef(1F/Math.max(1+(scope.getFOVFactor() * ((adsSwitch*adsSwitch)/1.9F)),0.1F), 1F, 1F);
						GL11.glTranslatef((1F/Math.max(1+(scope.getFOVFactor() * ((adsSwitch*adsSwitch)/2.0F)),0.1F)-1F)*1.0F, 0, 0);
					}
					else
					{
						GL11.glScalef(1F/Math.max(1+(scope.getFOVFactor() * ((adsSwitch*adsSwitch)/5F)),0.1F), 1F, 1F);
						//GL11.glTranslatef((1F/Math.max(1+(scope.getFOVFactor() * ((adsSwitch*adsSwitch)/8F)),0.1F)-1F)*1F, 0, 0);
					}
					
					
					//Gun recoil animation, defined by the GunType
					GL11.glTranslatef(-(animations.lastGunRecoil + (animations.gunRecoil - animations.lastGunRecoil) * smoothing) * gunType.recoilAnimX * 0.0625F * (1 - ((adsSwitch*adsSwitch)*0.14F)), -(animations.lastGunRecoil + (animations.gunRecoil - animations.lastGunRecoil) * smoothing) * -gunType.recoilAnimY * 0.0625F, 0F);
					GL11.glRotatef(-(animations.lastGunRecoil + (animations.gunRecoil - animations.lastGunRecoil) * smoothing) * -gunType.recoilAnimRotate, 0F, 0F, 1F);
		
		
					//Gun pump animation, defined by the GunType and only used if pump distance is not zero
					if (model.pumpHandleDistance != 0 || gunTypeFirst.moveSlideOnPump) {
					GL11.glTranslatef(-(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * gunType.pumpAnimX * 0.0625F, -(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * -gunType.pumpAnimY * 0.0625F, -(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * gunType.pumpAnimZ * 0.0625F);
					GL11.glRotatef(-(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * -gunType.pumpAnimPitch, 0F, 0F, 1F);
					GL11.glRotatef(-(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * -gunType.pumpAnimYaw, 0F, 1F, 0F);
					GL11.glRotatef(-(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * -gunType.pumpAnimTilt, 1F, 0F, 0F);
					}
					
					
					//Sprint animation handling
					//Perform the corresponding animation if not dual wielding
					if (sprintProgress > 0 && gunTypeFirst.sprintAnim != EnumSprintHoldType.NONE && FlansModClient.isDualWielding == false)
					{
					switch(gunTypeFirst.sprintAnim)
					{
						case SIMPLE :
						{
							GL11.glTranslatef(0.0F*sprintProgressTrans, -0.15F*sprintProgressTrans, 0);
							GL11.glRotatef(-24.0F*sprintProgress, 0F, 0F, 1F);
							break;
						}
						case RAISE :
						{
							GL11.glTranslatef(0.4F*sprintProgressTrans, 0.25F*sprintProgressTrans, 0);
							GL11.glRotatef(35.0F*sprintProgress, 0F, 0F, 1F);
							break;
						}
						case LOWER :
						{
							GL11.glTranslatef(0.5F*sprintProgressTrans, -0.2F*sprintProgressTrans, -0.01F*sprintProgressTrans);
							GL11.glRotatef(73.0F*sprintProgress, 0F, 1F, 0F);
							GL11.glRotatef(-5.0F*sprintProgress, 0F, 0F, 1F);
							break;
						}
						case PISTOL :
						{
							GL11.glTranslatef(0.6F*sprintProgressTrans, 0.25F*sprintProgressTrans, 0);
							GL11.glRotatef(62.0F*sprintProgress, 0F, 0F, 1F);
							break;
						}
						case RIFLE :
						{
							GL11.glTranslatef(0.1F*sprintProgressTrans, -0.0F*sprintProgressTrans, -0.08F*sprintProgressTrans);
							GL11.glRotatef(68.0F*sprintProgress, 0F, 1F, 0F);
							GL11.glRotatef(-22.5F*sprintProgress, 0F, 0F, 1F);
							break;
						}
						case HEAVY :
						{
							GL11.glTranslatef(0.0F*sprintProgressTrans, -1.5F*sprintProgressTrans, 0);
							GL11.glRotatef(12.0F*sprintProgress, 0F, 0F, 1F);
							break;
						}
						case HOLSTER :
						{
							GL11.glTranslatef(-1.0F*sprintProgressTrans, 4.0F*sprintProgressTrans, 0);
							GL11.glRotatef(-45.0F*sprintProgress, 0F, 0F, 1F);
							break;
						}
					}}
					else
					//If the player is dual wielding, play alternate animations
					if (sprintProgress > 0 && gunTypeFirst.sprintAnim != EnumSprintHoldType.NONE && FlansModClient.isDualWielding == true)
					{
					switch(gunTypeFirst.sprintAnim)
					{
						case SIMPLE :
						{
							GL11.glTranslatef(0.0F*sprintProgressTrans, -0.15F*sprintProgressTrans, 0);
							GL11.glRotatef(-24.0F*sprintProgress*sprintProgress, 0F, 0F, 1F);
							break;
						}
						case RAISE :
						{
							GL11.glTranslatef(0.4F*sprintProgressTrans, 0.25F*sprintProgressTrans, 0);
							GL11.glRotatef(35.0F*sprintProgress, 0F, 0F, 1F);
							break;
						}
						case LOWER :
						{
							GL11.glTranslatef(0.1F*sprintProgressTrans, -0.2F*sprintProgressTrans, 0);
							GL11.glRotatef(-10.0F*sprintProgress, 0F, 0F, 1F);
							break;
						}
						case PISTOL :
						{
							GL11.glTranslatef(0.6F*sprintProgressTrans, 0.25F*sprintProgressTrans, 0);
							GL11.glRotatef(61.0F*sprintProgress, 0F, 0F, 1F);
							break;
						}
						case RIFLE :
						{
							GL11.glTranslatef(0.05F*sprintProgressTrans, 0.2F*sprintProgressTrans, 0);
							GL11.glRotatef(50.0F*sprintProgress, 0F, 0F, 1F);
							break;
						}
						case HEAVY :
						{
							GL11.glTranslatef(0.0F*sprintProgressTrans, -1.5F*sprintProgressTrans, 0);
							GL11.glRotatef(12.0F*sprintProgress, 0F, 0F, 1F);
							break;
						}
						case HOLSTER :
						{
							GL11.glTranslatef(-1.0F*sprintProgressTrans, 4.0F*sprintProgressTrans, 0);
							GL11.glRotatef(-45.0F*sprintProgress, 0F, 0F, 1F);
							break;
						}
					}}
					
					
					if(animations.meleeAnimationProgress > 0 && animations.meleeAnimationProgress < gunType.meleePath.size()) 
					{
						Vector3f meleePos = gunType.meleePath.get(animations.meleeAnimationProgress);
						Vector3f nextMeleePos = animations.meleeAnimationProgress + 1 < gunType.meleePath.size() ? gunType.meleePath.get(animations.meleeAnimationProgress + 1) : new Vector3f();
						GL11.glTranslatef(meleePos.x + (nextMeleePos.x - meleePos.x) * smoothing, meleePos.y + (nextMeleePos.y - meleePos.y) * smoothing, meleePos.z + (nextMeleePos.z - meleePos.z) * smoothing);
						Vector3f meleeAngles = gunType.meleePathAngles.get(animations.meleeAnimationProgress);
						Vector3f nextMeleeAngles = animations.meleeAnimationProgress + 1 < gunType.meleePathAngles.size() ? gunType.meleePathAngles.get(animations.meleeAnimationProgress + 1) : new Vector3f();
						GL11.glRotatef(meleeAngles.y + (nextMeleeAngles.y - meleeAngles.y) * smoothing, 0F, 1F, 0F);
						GL11.glRotatef(meleeAngles.z + (nextMeleeAngles.z - meleeAngles.z) * smoothing, 0F, 0F, 1F);
						GL11.glRotatef(meleeAngles.x + (nextMeleeAngles.x - meleeAngles.x) * smoothing, 1F, 0F, 0F);

					}
					
					if(model.spinningCocking)
					{
						GL11.glTranslatef(model.spinPoint.x, model.spinPoint.y, model.spinPoint.z);
						float pumped = (animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing);
						GL11.glRotatef(pumped * 180F + 180F, 0F, 0F, 1F);
						GL11.glTranslatef(-model.spinPoint.x, -model.spinPoint.y, -model.spinPoint.z);
					}
					
					if(animations.reloading)
					{
						//Calculate the amount of tilt required for the reloading animation
						float effectiveReloadAnimationProgress = animations.lastReloadAnimationProgress + (animations.reloadAnimationProgress - animations.lastReloadAnimationProgress) * smoothing;
						reloadRotate = 1F;
						if(effectiveReloadAnimationProgress < model.tiltGunTime)
							reloadRotate = effectiveReloadAnimationProgress / model.tiltGunTime;
						if(effectiveReloadAnimationProgress > model.tiltGunTime + model.unloadClipTime + model.loadClipTime)
							reloadRotate = 1F - (effectiveReloadAnimationProgress - (model.tiltGunTime + model.unloadClipTime + model.loadClipTime)) / model.untiltGunTime;
						
						//Rotate the gun dependent on the animation type
						switch(model.animationType)
						{
							case BOTTOM_CLIP : case PISTOL_CLIP : case SHOTGUN : case END_LOADED : 
							{
								GL11.glRotatef(60F * reloadRotate, 0F, 0F, 1F);
								GL11.glRotatef(30F * reloadRotate * flip, 1F, 0F, 0F);
								GL11.glTranslatef(0.25F * reloadRotate, 0F, 0F);
								break;
							}
							case BACK_LOADED :
							{
								GL11.glRotatef(-75F * reloadRotate, 0F, 0F, 1F);
								GL11.glRotatef(-30F * reloadRotate * flip, 1F, 0F, 0F);
								GL11.glTranslatef(0.5F * reloadRotate, 0F, 0F);
								break;
							}
							case BULLPUP :
							{
								GL11.glRotatef(70F * reloadRotate, 0F, 0F, 1F);
								GL11.glRotatef(10F * reloadRotate * flip, 1F, 0F, 0F);
								GL11.glTranslatef(0.5F * reloadRotate, -0.2F * reloadRotate, 0F);
								break;
							}
							case RIFLE : 
							{
								GL11.glRotatef(30F * reloadRotate, 0F, 0F, 1F);
								GL11.glRotatef(-30F * reloadRotate * flip, 1F, 0F, 0F);
								GL11.glTranslatef(0.5F * reloadRotate, 0F, -0.5F * reloadRotate);
								break;
							}
							case RIFLE_TOP : case REVOLVER :
							{
								GL11.glRotatef(30F * reloadRotate, 0F, 0F, 1F);
								GL11.glRotatef(10F * reloadRotate, 0F, 1F, 0F);
								GL11.glRotatef(-10F * reloadRotate * flip, 1F, 0F, 0F);
								GL11.glTranslatef(0.1F * reloadRotate, -0.2F * reloadRotate, -0.02F * reloadRotate);
								break;
							}

							case REVOLVER2 :
							{
								GL11.glRotatef(20F * reloadRotate, 0F, 0F, 1F);
								GL11.glRotatef(-10F * reloadRotate * flip, 1F, 0F, 0F);
								break;
							}
							case BREAK_ACTION :
							{
								GL11.glRotatef(reloadRotate * (model.breakAngle/3), 0F, 0F, 1F);
								break;
							}


							case ALT_PISTOL_CLIP :
							{
								GL11.glRotatef(60F * reloadRotate * flip, 0F, 1F, 0F);
								GL11.glTranslatef(0.15F * reloadRotate, 0.25F * reloadRotate, 0F);
								break;
							}
							case STRIKER : 
							{
								GL11.glRotatef(-35F * reloadRotate * flip, 1F, 0F, 0F);
								GL11.glTranslatef(0.2F * reloadRotate, 0F, -0.1F * reloadRotate);
								break;
							}
							case GENERIC :
							{
								//Gun reloads partly or completely off-screen.
								GL11.glRotatef(45F * reloadRotate, 0F, 0F, 1F);
								GL11.glTranslatef(-0.2F * reloadRotate, -0.5F * reloadRotate, 0F);
								break;
							}
							case CUSTOM :
							{
								GL11.glRotatef(model.rotateGunVertical * reloadRotate, 0F, 0F, 1F);
								GL11.glRotatef(model.rotateGunHorizontal * reloadRotate, 0F, 1F, 0F);
								GL11.glRotatef(model.tiltGun * reloadRotate, 1F, 0F, 0F);
								GL11.glTranslatef(model.translateGun.x * reloadRotate,  model.translateGun.y * reloadRotate, model.translateGun.z * reloadRotate);
								break;
							}
							default : break;
						}
					}
					
					break;
				}
				default : break;
			}
			
			renderGun(item, gunType, f, model, animations, reloadRotate, type);
		}
		GL11.glPopMatrix();
		
		GL11.glBlendFunc(srcBlend, dstBlend);
		GL11.glDisable(GL11.GL_BLEND);
	}
	
	/** Gun render method, seperated from transforms so that mecha renderer may also call this 
	 * @param entity */
	public void renderGun(ItemStack item, GunType type, float f, ModelGun model, GunAnimations animations, float reloadRotate, ItemRenderType rtype)
	{
		//Make sure we actually have the renderEngine
		if(renderEngine == null)
			renderEngine = Minecraft.getMinecraft().renderEngine;
		
		//If we have no animation variables, use defaults
		if(animations == null)
			animations = GunAnimations.defaults;
		
		//Get all the attachments that we may need to render
		AttachmentType scopeAttachment = type.getScope(item);
		AttachmentType barrelAttachment = type.getBarrel(item);
		AttachmentType stockAttachment = type.getStock(item);
		AttachmentType gripAttachment = type.getGrip(item);
		AttachmentType gadgetAttachment = type.getGadget(item);
		
		//ItemStack scopeItemStack = type.getScopeItemStack(item);
		//ItemStack barrelItemStack = type.getBarrelItemStack(item);
		//ItemStack stockItemStack = type.getStockItemStack(item);
		//ItemStack gripItemStack = type.getGripItemStack(item);
		
		ItemStack[] bulletStacks = new ItemStack[type.numAmmoItemsInGun];
		boolean empty = true;
		for(int i = 0; i < type.numAmmoItemsInGun; i++)
		{
			bulletStacks[i] = ((ItemGun)item.getItem()).getBulletItemStack(item, i);
			if(bulletStacks[i] != null && bulletStacks[i].getItem() instanceof ItemBullet && bulletStacks[i].getItemDamage() < bulletStacks[i].getMaxDamage())
				empty = false;
		}
				
		//Load texture
		/*if (rtype == ItemRenderType.EQUIPPED_FIRST_PERSON && model.hasArms && FlansMod.armsEnable)
		{
			Minecraft mc = Minecraft.getMinecraft();
			renderFirstPersonArm(mc.thePlayer, model, animations);
		}*/
		renderEngine.bindTexture(FlansModResourceHandler.getPaintjobTexture(type.getPaintjob(item.stackTagCompound.getString("Paint"))));
		
		
		//Apply the Y offset when a scope is equipped/unequipped
		GunType gunType = ((ItemGun)item.getItem()).type;
		if(gunType == null)
			return;
		
		/*
		if(scopeAttachment != null)
		{
			GL11.glTranslatef(0F, -scopeAttachment.model.renderOffset / 16F, 0F);
			GL11.glTranslatef(0F, gunType.scopedYOffset / 16F, 0F);
		}
		else
		if(scopeAttachment == null)
		{
			GL11.glTranslatef(0F, gunType.unscopedYOffset / 16F, 0F);
		}
		
		
		GL11.glTranslatef(gunType.gunXOffset / 16F, 0F, 0F);
		GL11.glTranslatef(0F, 0F, gunType.gunZOffset / 16F);
		
		
		
		//Gun recoil animation, defined by the GunType
		GL11.glTranslatef(-(animations.lastGunRecoil + (animations.gunRecoil - animations.lastGunRecoil) * smoothing) * gunType.recoilAnimX * 0.0625F, -(animations.lastGunRecoil + (animations.gunRecoil - animations.lastGunRecoil) * smoothing) * -gunType.recoilAnimY * 0.0625F, 0F);
		GL11.glRotatef(-(animations.lastGunRecoil + (animations.gunRecoil - animations.lastGunRecoil) * smoothing) * -gunType.recoilAnimRotate, 0F, 0F, 1F);
		
		
		//Gun pump animation, defined by the GunType and only used if pump distance is not zero
		if (model.pumpHandleDistance != 0) {
		GL11.glTranslatef(-(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * gunType.pumpAnimX * 0.0625F, -(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * -gunType.pumpAnimY * 0.0625F, -(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * gunType.pumpAnimZ * 0.0625F);
		GL11.glRotatef(-(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * -gunType.pumpAnimPitch, 0F, 0F, 1F);
		GL11.glRotatef(-(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * -gunType.pumpAnimYaw, 0F, 1F, 0F);
		GL11.glRotatef(-(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * -gunType.pumpAnimTilt, 1F, 0F, 0F);
		}*/
		
		
		
		//Now, onto the bulk of the stuff!
		//Render the gun and default attachment models
		GL11.glPushMatrix();
		{
			GL11.glScalef(type.modelScale, type.modelScale, type.modelScale);

			model.renderGun(f);
			if(scopeAttachment == null && !model.scopeIsOnSlide && !model.scopeIsOnBreakAction)
				model.renderDefaultScope(f);
			else if(scopeAttachment != null && !model.scopeIsOnSlide && !model.scopeIsOnBreakAction)
				model.renderAddonScope(f);
			if(barrelAttachment == null)
				model.renderDefaultBarrel(f);
			if(stockAttachment == null)
				model.renderDefaultStock(f);
			if(gripAttachment == null && !model.gripIsOnPump)
				model.renderDefaultGrip(f);
			
			//Slide back on reload call
			animations.setSlideBack(type.lockSlideOnReload);
			
			//Render various shoot / reload animated parts
			//Render slide parts
			GL11.glPushMatrix();
			{
				GL11.glTranslatef(-(animations.lastGunSlide + (animations.gunSlide - animations.lastGunSlide) * smoothing) * model.gunSlideDistance, 0F, 0F);
				
				if(model.moveSlideOnPump || type.moveSlideOnPump)
					GL11.glTranslatef(-(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * model.gunSlideDistance, 0F, 0F);
				
				model.renderSlide(f);
				if(model.scopeIsOnSlide)
				{
				if(scopeAttachment == null)
					model.renderDefaultScope(f);
				else if(scopeAttachment != null)
					model.renderAddonScope(f);
				}
			}
			GL11.glPopMatrix();
			
			//Render break action parts
			GL11.glPushMatrix();
			{
				GL11.glTranslatef(model.barrelBreakPoint.x, model.barrelBreakPoint.y, model.barrelBreakPoint.z);
				GL11.glRotatef(reloadRotate * -model.breakAngle, 0F, 0F, 1F);
				GL11.glTranslatef(-model.barrelBreakPoint.x, -model.barrelBreakPoint.y, -model.barrelBreakPoint.z);
				model.renderBreakAction(f);
				if(model.scopeIsOnBreakAction)
				{
				if(scopeAttachment == null)
					model.renderDefaultScope(f);
				else if(scopeAttachment != null)
					model.renderAddonScope(f);
				}
			}
			GL11.glPopMatrix();
			
			//Render pump-action handle
			GL11.glPushMatrix();
			{
				GL11.glTranslatef(-(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * model.pumpHandleDistance, 0F, 0F);
				
				if (gunType.boltAnimTilt != 0)
				{
					GL11.glTranslatef(0F, gunType.boltCenterOffset * 0.0625F, 0F);
					//GL11.glRotatef(-(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * -gunType.boltAnimTilt, 1F, 0F, 0F);
					GL11.glRotatef( Math.min(Math.max(-(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * (-gunType.boltAnimTilt * gunType.boltTiltSpeed),-Math.abs(gunType.boltAnimTilt)),Math.abs(gunType.boltAnimTilt)), 1F, 0F, 0F);
					GL11.glTranslatef(0F, -gunType.boltCenterOffset * 0.0625F, 0F);
				}
				
				model.renderPump(f);
				if(gripAttachment == null && model.gripIsOnPump)
				       model.renderDefaultGrip(f);
			}
			GL11.glPopMatrix();
			
			//Render minigun barrels
			if(type.mode == EnumFireMode.MINIGUN)
			{
				GL11.glPushMatrix();
				GL11.glTranslatef(model.minigunBarrelOrigin.x, model.minigunBarrelOrigin.y, model.minigunBarrelOrigin.z);
				GL11.glRotatef(animations.minigunBarrelRotation, 1F, 0F, 0F);
				GL11.glTranslatef(-model.minigunBarrelOrigin.x, -model.minigunBarrelOrigin.y, -model.minigunBarrelOrigin.z);
				model.renderMinigunBarrel(f);
				GL11.glPopMatrix();
			}
			
			//Render hammer model (new to FM++; rotates when a shot is fired using animations.gunSlide)
			GL11.glPushMatrix();
			{
				GL11.glTranslatef(model.hammerRotatePoint.x, model.hammerRotatePoint.y, model.hammerRotatePoint.z);
				GL11.glRotatef(-(animations.lastGunSlide + (animations.gunSlide - animations.lastGunSlide) * smoothing) * -model.hammerRotateAngle, 0F, 0F, 1F);
				GL11.glTranslatef(-model.hammerRotatePoint.x, -model.hammerRotatePoint.y, -model.hammerRotatePoint.z);
				
				model.renderHammer(f);
			}
			GL11.glPopMatrix();
			
			//Render revolver cylinder
			GL11.glPushMatrix();
			{
				GL11.glTranslatef(model.revolverFlipPoint.x, model.revolverFlipPoint.y, model.revolverFlipPoint.z);
				GL11.glRotatef(reloadRotate * model.revolverFlipAngle, 1F, 0F, 0F);
				GL11.glTranslatef(-model.revolverFlipPoint.x, -model.revolverFlipPoint.y, -model.revolverFlipPoint.z);
				model.renderRevolverBarrel(f);
			}
			GL11.glPopMatrix();
			
			//Render revolver2 cylinder (opens on the opposite side)
			GL11.glPushMatrix();
			{
				GL11.glTranslatef(model.revolverFlipPoint.x, model.revolverFlipPoint.y, model.revolverFlipPoint.z);
				GL11.glRotatef(reloadRotate * model.revolverFlipAngle, -1F, 0F, 0F);
				GL11.glTranslatef(-model.revolverFlipPoint.x, -model.revolverFlipPoint.y, -model.revolverFlipPoint.z);
				model.renderRevolver2Barrel(f);
			}
			GL11.glPopMatrix();
			
			//Render the clip
			GL11.glPushMatrix();
			{
				boolean shouldRender = true;
				//Check to see if the ammo should be rendered first
				switch(model.animationType)
				{
					case END_LOADED : case BACK_LOADED :
					{
						if(empty)
							shouldRender = false;
						break;
					}
					default: break;
				}
				//If it should be rendered, do the transformations required
				if(shouldRender && animations.reloading && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0)
				{
					//Calculate the amount of tilt required for the reloading animation
					float effectiveReloadAnimationProgress = animations.lastReloadAnimationProgress + (animations.reloadAnimationProgress - animations.lastReloadAnimationProgress) * smoothing;
					float clipPosition = 0F;
					if(effectiveReloadAnimationProgress > model.tiltGunTime && effectiveReloadAnimationProgress < model.tiltGunTime + model.unloadClipTime)
						clipPosition = (effectiveReloadAnimationProgress - model.tiltGunTime) / model.unloadClipTime;
					if(effectiveReloadAnimationProgress >= model.tiltGunTime + model.unloadClipTime && effectiveReloadAnimationProgress < model.tiltGunTime + model.unloadClipTime + model.loadClipTime)
						clipPosition = 1F - (effectiveReloadAnimationProgress - (model.tiltGunTime + model.unloadClipTime)) / model.loadClipTime;
					
					float loadOnlyClipPosition = Math.max(0F, Math.min(1F, 1F - ((effectiveReloadAnimationProgress - model.tiltGunTime) / (model.unloadClipTime + model.loadClipTime))));
					
					//Rotate the gun dependent on the animation type
					switch(model.animationType)
					{
						case BREAK_ACTION : 
						{
							GL11.glTranslatef(model.barrelBreakPoint.x, model.barrelBreakPoint.y, model.barrelBreakPoint.z);
							GL11.glRotatef(reloadRotate * -model.breakAngle, 0F, 0F, 1F);
							GL11.glTranslatef(-model.barrelBreakPoint.x, -model.barrelBreakPoint.y, -model.barrelBreakPoint.z);
							GL11.glTranslatef(-1F * clipPosition * 1/type.modelScale, 0F, 0F);
							break;
						}
						case REVOLVER :
						{				
							GL11.glTranslatef(model.revolverFlipPoint.x, model.revolverFlipPoint.y, model.revolverFlipPoint.z);
							GL11.glRotatef(reloadRotate * model.revolverFlipAngle, 1F, 0F, 0F);
							GL11.glTranslatef(-model.revolverFlipPoint.x, -model.revolverFlipPoint.y, -model.revolverFlipPoint.z);
							GL11.glTranslatef(-1F * clipPosition * 1/type.modelScale, 0F, 0F);
							break;
						}

						case REVOLVER2 :
						{				
							GL11.glTranslatef(model.revolver2FlipPoint.x, model.revolver2FlipPoint.y, model.revolver2FlipPoint.z);
							GL11.glRotatef(reloadRotate * model.revolver2FlipAngle, -1F, 0F, 0F);
							GL11.glTranslatef(-model.revolver2FlipPoint.x, -model.revolver2FlipPoint.y, -model.revolver2FlipPoint.z);
							GL11.glTranslatef(-1F * clipPosition * 1/type.modelScale, 0F, 0F);
							break;
						}

						case BOTTOM_CLIP : 
						{
							GL11.glRotatef(-180F * clipPosition, 0F, 0F, 1F);
							GL11.glRotatef(60F * clipPosition, 1F, 0F, 0F);
							GL11.glTranslatef(0.5F * clipPosition * 1/type.modelScale, 0F, 0F);
							break;
						}
						case PISTOL_CLIP :
						{
							GL11.glRotatef(-90F * clipPosition * clipPosition, 0F, 0F, 1F);
							GL11.glTranslatef(0F, -1F * clipPosition * 1/type.modelScale, 0F);
							break;
						}
						case ALT_PISTOL_CLIP :
						{
							GL11.glRotatef(5F * clipPosition, 0F, 0F, 1F);
							GL11.glTranslatef(0F, -3F * clipPosition * 1/type.modelScale, 0F);
							break;
						}
						case SIDE_CLIP : 
						{
							GL11.glRotatef(180F * clipPosition, 0F, 1F, 0F);
							GL11.glRotatef(60F * clipPosition, 0F, 1F, 0F);
							GL11.glTranslatef(0.5F * clipPosition * 1/type.modelScale, 0F, 0F);
							break;
						}
						case BULLPUP :
						{
							GL11.glRotatef(-150F * clipPosition, 0F, 0F, 1F);
							GL11.glRotatef(60F * clipPosition, 1F, 0F, 0F);
							GL11.glTranslatef(1F * clipPosition * 1/type.modelScale, -0.5F * clipPosition * 1/type.modelScale, 0F);
							break;
						}
						case P90 :
						{
							GL11.glRotatef(-15F * reloadRotate * reloadRotate, 0F, 0F, 1F);
							GL11.glTranslatef(0F, 0.075F * reloadRotate, 0F);
							GL11.glTranslatef(-2F * clipPosition * 1/type.modelScale, -0.3F * clipPosition * 1/type.modelScale, 0.5F * clipPosition * 1/type.modelScale);
							break;
						}
						case RIFLE : 
						{
							float thing = clipPosition * model.numBulletsInReloadAnimation;
							int bulletNum = MathHelper.floor_float(thing);
							float bulletProgress = thing - bulletNum;
							
							GL11.glRotatef(bulletProgress * 15F, 0F, 1F, 0F);
							GL11.glRotatef(bulletProgress * 15F, 0F, 0F, 1F);
							GL11.glTranslatef(bulletProgress * -1F * 1/type.modelScale, 0F, bulletProgress * 0.5F * 1/type.modelScale);
							
							break;
						}
						case RIFLE_TOP : 
						{
							float thing = clipPosition * model.numBulletsInReloadAnimation;
							int bulletNum = MathHelper.floor_float(thing);
							float bulletProgress = thing - bulletNum;
							
							GL11.glRotatef(bulletProgress * 55F, 0F, 1F, 0F);
							GL11.glRotatef(bulletProgress * 95F, 0F, 0F, 1F);
							GL11.glTranslatef(bulletProgress * -0.1F * 1/type.modelScale, bulletProgress * 1F * 1/type.modelScale, bulletProgress * 0.5F * 1/type.modelScale);
							
							break;
						}
						case SHOTGUN : case STRIKER :
						{
							float thing = clipPosition * model.numBulletsInReloadAnimation;
							int bulletNum = MathHelper.floor_float(thing);
							float bulletProgress = thing - bulletNum;
							
							GL11.glRotatef(bulletProgress * -30F, 0F, 0F, 1F);
							GL11.glTranslatef(bulletProgress * -0.5F * 1/type.modelScale, bulletProgress * -1F * 1/type.modelScale, 0F);
							
							break;
						}
						case CUSTOM :
						{
							GL11.glRotatef(model.rotateClipVertical * clipPosition, 0F, 0F, 1F);
							GL11.glRotatef(model.rotateClipHorizontal * clipPosition, 0F, 1F, 0F);
							GL11.glRotatef(model.tiltClip * clipPosition, 1F, 0F, 0F);
							GL11.glTranslatef(model.translateClip.x * clipPosition,  model.translateClip.y * clipPosition, model.translateClip.z * clipPosition);
							break;
						}
						case END_LOADED :
						{
							//float bulletProgress = 1F;
							//if(effectiveReloadAnimationProgress > model.tiltGunTime)
							//	bulletProgress = 1F - Math.min((effectiveReloadAnimationProgress - model.tiltGunTime) / (model.unloadClipTime + model.loadClipTime), 1);
							

							
							float dYaw = (loadOnlyClipPosition > 0.5F ? loadOnlyClipPosition * 2F - 1F : 0F);
							
							
							GL11.glRotatef(-45F * dYaw, 0F, 0F, 1F);
							GL11.glTranslatef(-model.endLoadedAmmoDistance * dYaw, -0.5F * dYaw, 0F);
							
							float xDisplacement = (loadOnlyClipPosition < 0.5F ? loadOnlyClipPosition * 2F : 1F);
							
							GL11.glTranslatef(model.endLoadedAmmoDistance * xDisplacement, 0F, 0F);
							
							/*
							GL11.glTranslatef(1F * bulletProgress, -3F * bulletProgress, 0F);
							if(bulletProgress > 0.5F)
								GL11.glRotatef(-90F * (bulletProgress * 2F), 0F, 0F, 1F);	
							
							if(bulletProgress < 0.5F)
							{
								GL11.glTranslatef(-3F * (bulletProgress - 0.5F), 0F, 0F);
								
							}
							*/
							
							
							break;
						}
						case BACK_LOADED :
						{
							float dYaw = (loadOnlyClipPosition > 0.5F ? loadOnlyClipPosition * 2F - 1F : 0F);
							
							
							//GL11.glRotatef(-45F * dYaw, 0F, 0F, 1F);
							GL11.glTranslatef(model.endLoadedAmmoDistance * dYaw, -0.5F * dYaw, 0F);
							
							float xDisplacement = (loadOnlyClipPosition < 0.5F ? loadOnlyClipPosition * 2F : 1F);
							
							GL11.glTranslatef(-model.endLoadedAmmoDistance * xDisplacement, 0F, 0F);
						}
						
						default : break;
					}
				}
				
				/*if (rtype == ItemRenderType.EQUIPPED_FIRST_PERSON && model.hasArms && FlansMod.armsEnable)
				{
					Minecraft mc = Minecraft.getMinecraft();
					renderAnimArm(mc.thePlayer, model, type, animations);
				}*/

				if(shouldRender)
					model.renderAmmo(f);
			}
			GL11.glPopMatrix();
		}
		GL11.glPopMatrix();
		
		//Render static attachments
		//Scope
		if(scopeAttachment != null)
		{
			GL11.glPushMatrix();
			{
				renderEngine.bindTexture(FlansModResourceHandler.getTexture(scopeAttachment));
				if(model.scopeIsOnBreakAction)
				{
					GL11.glTranslatef(model.barrelBreakPoint.x, model.barrelBreakPoint.y, model.barrelBreakPoint.z);
					GL11.glRotatef(reloadRotate * -model.breakAngle, 0F, 0F, 1F);
					GL11.glTranslatef(-model.barrelBreakPoint.x, -model.barrelBreakPoint.y, -model.barrelBreakPoint.z);
				}
				//Scope Position as set by the model
				GL11.glTranslatef(model.scopeAttachPoint.x * type.modelScale, model.scopeAttachPoint.y * type.modelScale, model.scopeAttachPoint.z * type.modelScale);
				//Offset set by the gun's config
				GL11.glTranslatef(type.attachScopeXOffset * type.modelScale, type.attachScopeYOffset * type.modelScale, type.attachScopeZOffset * type.modelScale);

				if(model.scopeIsOnSlide)
				{
					GL11.glTranslatef(-(animations.lastGunSlide + (animations.gunSlide - animations.lastGunSlide) * smoothing) * model.gunSlideDistance * type.modelScale, 0F, 0F);
					
					if(model.moveSlideOnPump || type.moveSlideOnPump)
					GL11.glTranslatef(-(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * (model.gunSlideDistance) * type.modelScale, 0F, 0F);
				}
				GL11.glScalef(scopeAttachment.modelScale * type.attachScopeScale, scopeAttachment.modelScale * type.attachScopeScale, scopeAttachment.modelScale * type.attachScopeScale);
				ModelAttachment scopeModel = scopeAttachment.model;
				if(scopeModel != null)
					scopeModel.renderAttachment(f);
				renderEngine.bindTexture(FlansModResourceHandler.getTexture(type));
			}
			GL11.glPopMatrix();
		}
		
		//Grip
		if(gripAttachment != null)
		{
			GL11.glPushMatrix();
			{
				renderEngine.bindTexture(FlansModResourceHandler.getTexture(gripAttachment));
				//Grip Position as set by the model
				GL11.glTranslatef(model.gripAttachPoint.x * type.modelScale, model.gripAttachPoint.y * type.modelScale, model.gripAttachPoint.z * type.modelScale);
				//Offset set by the gun's config
				GL11.glTranslatef(type.attachGripXOffset * type.modelScale, type.attachGripYOffset * type.modelScale, type.attachGripZOffset * type.modelScale);
				
				if(model.gripIsOnPump)
					GL11.glTranslatef(-(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * model.pumpHandleDistance * type.modelScale, 0F, 0F);
				GL11.glScalef(gripAttachment.modelScale * type.attachGripScale, gripAttachment.modelScale * type.attachGripScale, gripAttachment.modelScale * type.attachGripScale);
				ModelAttachment gripModel = gripAttachment.model;
				if(gripModel != null)
				{
					//Bipod attachment mechanics
					if(gripAttachment.bipod && ((ItemGun)item.getItem()).crouched)
					gripModel.renderDeployedBipod(f);
					else
					gripModel.renderAttachment(f);
				}
				renderEngine.bindTexture(FlansModResourceHandler.getTexture(type));
			}
			GL11.glPopMatrix();
		}
		
		//Barrel
		if(barrelAttachment != null)
		{
			GL11.glPushMatrix();
			{
				renderEngine.bindTexture(FlansModResourceHandler.getTexture(barrelAttachment));
				//Barrel Position as set by the model
				GL11.glTranslatef(model.barrelAttachPoint.x * type.modelScale, model.barrelAttachPoint.y * type.modelScale, model.barrelAttachPoint.z * type.modelScale);
				//Offset set by the gun's config
				GL11.glTranslatef(type.attachBarrelXOffset * type.modelScale, type.attachBarrelYOffset * type.modelScale, type.attachBarrelZOffset * type.modelScale);
				
				GL11.glScalef(barrelAttachment.modelScale * type.attachBarrelScale, barrelAttachment.modelScale * type.attachBarrelScale, barrelAttachment.modelScale * type.attachBarrelScale);
				ModelAttachment barrelModel = barrelAttachment.model;
				if(barrelModel != null)
					barrelModel.renderAttachment(f);
				renderEngine.bindTexture(FlansModResourceHandler.getTexture(type));
			}
			GL11.glPopMatrix();
		}
		
		//Stock
		if(stockAttachment != null)
		{
			GL11.glPushMatrix();
			{
				renderEngine.bindTexture(FlansModResourceHandler.getTexture(stockAttachment));
				//Stock Position as set by the model
				GL11.glTranslatef(model.stockAttachPoint.x * type.modelScale, model.stockAttachPoint.y * type.modelScale, model.stockAttachPoint.z * type.modelScale);
				//Offset set by the gun's config
				GL11.glTranslatef(type.attachStockXOffset * type.modelScale, type.attachStockYOffset * type.modelScale, type.attachStockZOffset * type.modelScale);
				
				GL11.glScalef(stockAttachment.modelScale * type.attachStockScale, stockAttachment.modelScale * type.attachStockScale, stockAttachment.modelScale * type.attachStockScale);
				ModelAttachment stockModel = stockAttachment.model;
				if(stockModel != null)
					stockModel.renderAttachment(f);
				renderEngine.bindTexture(FlansModResourceHandler.getTexture(type));
			}
			GL11.glPopMatrix();
		}
		
		//Gadget
		if(gadgetAttachment != null)
		{
			GL11.glPushMatrix();
			{
				renderEngine.bindTexture(FlansModResourceHandler.getTexture(gadgetAttachment));
				
				//Gadget Rotation
				GL11.glRotatef(reloadRotate * -model.breakAngle, 1F, 0F, 0F);
				
				if(model.gadgetIsOnBreakAction || type.gadgetIsOnBreakAction)
				{
					GL11.glTranslatef(model.barrelBreakPoint.x, model.barrelBreakPoint.y, model.barrelBreakPoint.z);
					GL11.glRotatef(reloadRotate * -model.breakAngle, 0F, 0F, 1F);
					GL11.glTranslatef(-model.barrelBreakPoint.x, -model.barrelBreakPoint.y, -model.barrelBreakPoint.z);
				}
				
				//Stock Position as set by the model
				GL11.glTranslatef(model.gadgetAttachPoint.x * type.modelScale, model.gadgetAttachPoint.y * type.modelScale, model.gadgetAttachPoint.z * type.modelScale);
				//Offset set by the gun's config
				GL11.glTranslatef(type.attachGadgetXOffset * type.modelScale, type.attachGadgetYOffset * type.modelScale, type.attachGadgetZOffset * type.modelScale);
				
				if(model.gadgetIsOnPump || type.gadgetIsOnPump)
					GL11.glTranslatef(-(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * model.pumpHandleDistance * type.modelScale, 0F, 0F);
				else
				if(model.gadgetIsOnSlide || type.gadgetIsOnSlide)
				{
					GL11.glTranslatef(-(animations.lastGunSlide + (animations.gunSlide - animations.lastGunSlide) * smoothing) * model.gunSlideDistance * type.modelScale, 0F, 0F);
				
					if(model.moveSlideOnPump || type.moveSlideOnPump)
					GL11.glTranslatef(-(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * (model.gunSlideDistance) * type.modelScale, 0F, 0F);
				}
				
				GL11.glScalef(gadgetAttachment.modelScale * type.attachGadgetScale, gadgetAttachment.modelScale * type.attachGadgetScale, gadgetAttachment.modelScale * type.attachGadgetScale);
				ModelAttachment gadgetModel = gadgetAttachment.model;
				if(gadgetModel != null)
					gadgetModel.renderAttachment(f);
				renderEngine.bindTexture(FlansModResourceHandler.getTexture(type));
				
				//attachGadgetRotation
			}
			GL11.glPopMatrix();
		}
	}
	
	
	//Arm rendering code (Cleaning up)
}
