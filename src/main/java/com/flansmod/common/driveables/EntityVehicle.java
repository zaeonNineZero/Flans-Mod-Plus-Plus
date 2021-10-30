package com.flansmod.common.driveables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import com.flansmod.api.IExplodeable;
import com.flansmod.common.FlansMod;
import com.flansmod.common.RotatedAxes;
import com.flansmod.common.driveables.DriveableType.ParticleEmitter;
import com.flansmod.common.driveables.VehicleType.SmokePoint;
import com.flansmod.common.guns.EntityBullet;
import com.flansmod.common.guns.EnumFireMode;
import com.flansmod.common.guns.InventoryHelper;
import com.flansmod.common.guns.raytracing.BulletHit;
import com.flansmod.common.network.PacketDriveableKey;
import com.flansmod.common.network.PacketDriveableKeyHeld;
import com.flansmod.common.network.PacketReload;
import com.flansmod.common.network.PacketParticle;
import com.flansmod.common.network.PacketPlaySound;
import com.flansmod.common.network.PacketVehicleControl;
import com.flansmod.common.teams.TeamsManager;
import com.flansmod.common.tools.ItemTool;
import com.flansmod.common.vector.Vector3f;
import com.flansmod.common.vector.Vector3i;
import com.flansmod.utils.MathUtils;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public class EntityVehicle extends EntityDriveable implements IExplodeable
{
	/** Weapon delays */
	public int shellDelay, gunDelay;
	/** Position of looping sounds */
	public int soundPosition;
	public int idlePosition;
	/** Front wheel yaw, used to control the vehicle steering */
	public float wheelsYaw;
	/** Despawn time */
	private int ticksSinceUsed = 0;
    /** Aesthetic door switch */
    public boolean varDoor;
    /** Wheel rotation angle. Only applies to vehicles that set a rotating wheels flag */
    public float wheelsAngle;
    /** Delayer for door button */
    public int toggleTimer = 0;
	/** Cruise control: hold throttle when enabled */
	public boolean cruiseControl = false;
	/** Delayer for cruise control button */
    public int toggleCruiseTimer = 0;
	
	public float throttleCalc = 0;
	public float velocityScaleG = 0;
	public float steeringForce = 0;
	public float driftSteerForce = 0;
	
	public boolean highThrottlePatch = true;
	public float throttleDivider = 1;
	
	public float finalTurnFactor = 0;
	
	public float boostGauge = 0F;
	public double boostTimer = 0;
	public boolean boostExhausted = true;
    
    public float yaw = 0;
    public float pitch = 0;
    public float roll = 0;
	
    public float yawSpeed = 0;
	
	public boolean isDrifting = false;
	public float driftSlide = 0;
	
	public float displayDriftFactor = 0;
	
	/** Submarine floating height */
	public float subFloatLevel = 0;
    
    //Handling stuff
    public boolean leftTurnHeld = false;
    public boolean rightTurnHeld = false;
    public boolean accelHeld = false;
    public boolean brakeHeld = false;
    public boolean allWheelsOnGround;
    public int totalWheelsOnGround = 0;
    
    //Some nonsense
    boolean lockTurretForward = false;
    //Animation vectors
	public Vector3f doorPos = new Vector3f(0,0,0);
	public Vector3f doorRot = new Vector3f(0,0,0);
	
	//Main turret sounds for server (bullshit basically)
	public int yawDelay = 0;
	public int pitchDelay = 0;
	public boolean turretYawing;
	public boolean turretPitching;
	
	public boolean deployedSmoke = false;
	

    public EntityVehicle(World world)
    {
        super(world);
        stepHeight = 1.0F;
    }

    //This one deals with spawning from a vehicle spawner
	public EntityVehicle(World world, double x, double y, double z, VehicleType type, DriveableData data)
	{
		super(world, type, data);
		stepHeight = type.wheelStepHeight;
		setPosition(x, y, z);
		initType(type, false);
		if (!type.highThrottlePatch)
		{
			highThrottlePatch = false;
		}
	}

	//This one allows you to deal with spawning from items
	public EntityVehicle(World world, double x, double y, double z, EntityPlayer placer, VehicleType type, DriveableData data)
	{
		super(world, type, data);
		stepHeight = type.wheelStepHeight;
		setPosition(x, y, z);
		rotateYaw(placer.rotationYaw + 90F);
		initType(type, false);
		if (!type.highThrottlePatch)
		{
			highThrottlePatch = false;
			throttleDivider = Math.min(1,type.maxThrottle);
		}
	}

	@Override
	protected void initType(DriveableType type, boolean clientSide)
	{
		super.initType(type, clientSide);
	}

	@Override
	public void readSpawnData(ByteBuf data)
	{
		super.readSpawnData(data);
	}

	@Override
    protected void writeEntityToNBT(NBTTagCompound tag)
    {
        super.writeEntityToNBT(tag);
        tag.setBoolean("VarDoor", varDoor);
    }

	@Override
    protected void readEntityFromNBT(NBTTagCompound tag)
    {
        super.readEntityFromNBT(tag);
        varDoor = tag.getBoolean("VarDoor");
    }
	
	@Override
    public boolean isInRangeToRenderDist(double d)
    {
        double d1 = 400D;
        return d < d1 * d1;
    }

	/**
	 * Called with the movement of the mouse. Used in controlling vehicles if need be.
	 * @param deltaY
	 * @param deltaX
	 */
	@Override
	public void onMouseMoved(int deltaX, int deltaY)
	{
	}

	@Override
	public void setPositionRotationAndMotion(double x, double y, double z, float yaw, float pitch, float roll, double motX, double motY, double motZ, float velYaw, float velPitch, float velRoll, float throt, float steeringYaw)
	{
		super.setPositionRotationAndMotion(x, y, z, yaw, pitch, roll, motX, motY, motZ, velYaw, velPitch, velRoll, throt, steeringYaw);
		wheelsYaw = steeringYaw;
	}

	@Override
	public boolean interactFirst(EntityPlayer entityplayer)
    {
		if(isDead)
			return false;
		if(worldObj.isRemote)
			return false;

		//If they are using a repair tool, don't put them in
		ItemStack currentItem = entityplayer.getCurrentEquippedItem();
		if(currentItem != null && currentItem.getItem() instanceof ItemTool && ((ItemTool)currentItem.getItem()).type.healDriveables)
			return true;

		VehicleType type = getVehicleType();
		//Check each seat in order to see if the player can sit in it
		for(int i = 0; i <= type.numPassengers; i++)
		{
			if(seats[i].interactFirst(entityplayer))
			{
				if(i == 0)
				{
					shellDelay = type.vehicleShellDelay;
					FlansMod.proxy.doTutorialStuff(entityplayer, this);
				}
				return true;
			}
		}
        return false;
    }

	
	//Throttle handling
	@Override
	public boolean pressKey(int key, EntityPlayer player)
	{
    	VehicleType type = getVehicleType();
    	//Send keys which require server side updates to the server
    	if(worldObj.isRemote && (key == 6 || key == 8 || key == 9))
    	{
    		FlansMod.getPacketHandler().sendToServer(new PacketDriveableKey(key));
    		return true;
    	}
		
		
		switch(key)
		{
			case 0 : //Accelerate : Increase the throttle, up to 1.
			{
				if (cruiseControl && (hasEnoughFuel() || type.fuelTankSize==-1))
				throttle += 0.005F;
				else
				{
				accelHeld = true;
				if ((hasEnoughFuel() || type.fuelTankSize==-1) && !isBoosting)
				{
					if (throttle >= 0)
					throttle += (0.023F/throttleDivider) * type.acceleration;
					else throttle += 0.023F * type.deceleration;
				}
				}
				if(throttle > 1F && cruiseControl)
					throttle = 1F;
				else if(throttle > 1.02F && !cruiseControl)
					throttle = 1.02F;
				return true;
			}
			case 1 : //Decelerate : Decrease the throttle, down to -1, or 0 if the vehicle cannot reverse
			{
				if (cruiseControl && (hasEnoughFuel() || type.fuelTankSize==-1))
				throttle -= 0.005F;
				else
				if (hasEnoughFuel() || type.fuelTankSize==-1)
				{
					if (throttle <= 0)
					throttle -= 0.030F * type.deceleration;
					else throttle -= (0.030F/throttleDivider) * type.acceleration;
				}
				if(throttle < -1F && cruiseControl)
					throttle = -1F;
				else if(throttle < -1.02F && !cruiseControl)
					throttle = -1.02F;
				if(throttle < 0F && type.maxNegativeThrottle == 0F)
					throttle = 0F;
				return true;
			}
			case 2 : //Left : Yaw the wheels left
			{
				wheelsYaw -= 2F;
				leftTurnHeld = true;
				
				return true;
			}
			case 3 : //Right : Yaw the wheels right
			{
				wheelsYaw += 2F;
				rightTurnHeld = true;
				
				return true;
			}
			case 4 : //Up : Brake (or ascend if sub)
			{
				if (!type.submarine)
				{
				if(adjustThrottle>type.driftSpeed && adjustThrottle>0 && (Math.abs(wheelsYaw)>=5 || isDrifting))
				{
					isDrifting = true;
					//Initial boost to drift sliding to start the drift.
					if (Math.abs(wheelsYaw)>=0.2 && Math.abs(driftSlide)<1)
					driftSlide -= 2F*(wheelsYaw > 0 ? type.turnLeftModifier*-1.0F : type.turnRightModifier*1.0F);
					else
					if (Math.abs(driftSlide)>=1)
					driftSlide += 1.4F*(driftSlide < 0 ? type.turnLeftModifier*-1.0F : type.turnRightModifier*1.0F);
				}
				brakeHeld = true;
				
				throttle -= ((throttle > 0 ? 0.01F-(Math.abs(wheelsYaw)/3000) : -0.01F)*type.brakePower)*0.7;
				
				//Additional braking power
				throttle *= 0.97F+(0.03*(Math.min(Math.abs(wheelsYaw)/20,1)));
				
				if(throttle < 0.02F*type.brakePower && throttle > -0.02F*type.brakePower)
					throttle = 0F;
				}
				else
				{
					if (subFloatLevel < type.maxSubFloatHeight)
						subFloatLevel += (type.subSurfacingSpeed)/3;
					else
						subFloatLevel = type.maxSubFloatHeight;
				}
				return true;
			}
			case 5 : //Down : Boost (or descend if sub)
			{
				if (type.submarine)
				{
					subFloatLevel -= (type.subSubmergingSpeed)/3;
				}
				else
				if (type.canBoost && boostExhausted == false && (hasEnoughFuel() || type.fuelTankSize==-1))
				{
					if (boostTimer == 0F)
					{
						PacketPlaySound.sendSoundPacket(posX, posY, posZ, 16, dimension, "vehicleboost", false);
						boostTimer = type.boostMinTime;
					}
					else
					if (boostTimer < 4F)
					boostTimer = 4F;
				}
				return true;
			}
			case 6 : //Exit : Get out
			{
				seats[0].riddenByEntity.setInvisible(false);
				seats[0].riddenByEntity.mountEntity(null);
          		return true;
			}
			case 7 : //Inventory
			{
				if(worldObj.isRemote)
                {
					FlansMod.proxy.openDriveableMenu((EntityPlayer)seats[0].riddenByEntity, worldObj, this);
                }
				return true;
			}
			case 8 : //Shoot shell
			case 9 : //Shoot bullet
			{
				return super.pressKey(key, player);
			}
			case 10 : //Change control mode : Toggle cruise control
			{
				if(toggleCruiseTimer <= 0)
				{
					cruiseControl = !cruiseControl;
					player.addChatMessage(new ChatComponentText("Cruise Control " + (cruiseControl ? "enabled" : "disabled")));
					toggleCruiseTimer = 10;
					FlansMod.getPacketHandler().sendToServer(new PacketVehicleControl(this));
				}
				return true;
			}
			case 11 : //Roll left : Do nothing
			{
				return true;
			}
			case 12 : //Roll right : Do nothing
			{
				return true;
			}
			case 13 : // Gear : Reset yaw (hold to adjust pitch without moving yaw)
			{
				seats[0].playerLooking = new RotatedAxes(0,seats[0].playerLooking.getPitch(),0);
				return true;
			}
			case 14 : // Door
			{
				if(toggleTimer <= 0)
				{
					varDoor = !varDoor;
					if(type.hasDoor)
						player.addChatMessage(new ChatComponentText("Doors " + (varDoor ? "opened" : "closed")));
					toggleTimer = 10;
					FlansMod.getPacketHandler().sendToServer(new PacketVehicleControl(this));
				}
				return true;
			}
			case 15 : // Wing : Do nothing
			{
				return true;
			}
            case 16 : // Trim Button
            {
				//applyTorque(new Vector3f(axes.getRoll() / 10, 0F, 0F));
				return true;
            }
            case 17 : //Park
            {
                break;
            }
            
            case 18 : //Flare
            {
				if(type.hasFlare && this.ticksFlareUsing <= 0 && this.flareDelay <= 0)
				{
					this.ticksFlareUsing = type.timeFlareUsing * 20;
					this.flareDelay = type.flareDelay;
					dischargeSmoke();
					if(worldObj.isRemote)
					{
						FlansMod.getPacketHandler().sendToServer(new PacketDriveableKey(key));
					}
					else
					{
						dischargeSmoke();
						if(!type.flareSound.isEmpty())
						{
							PacketPlaySound.sendSoundPacket(posX, posY, posZ, FlansMod.soundRange, dimension, type.flareSound, false);
						}
					}
					return true;
				}
				break;
            }
		}
		return false;
		

	}

    @Override
	public Vector3f getLookVector(ShootPoint dp)
    {
		return rotate(seats[0].looking.getXAxis());
    }
    

    @Override
	public void onUpdate()
    {
    	double bkPrevPosY = this.prevPosY;

        super.onUpdate();
		this.renderDistanceWeight = 4000.0D;

        for(Entity e : findEntitiesWithinbounds())
        {
        	if(e != this){
        		moveRiders(e);
        	}
        }
        
        boolean boostStarted = false;
		boolean inWater = false;
        
		
		//Disable cruise control when the vehicle has no driver
		if(seats[0] == null || seats[0].riddenByEntity == null && cruiseControl)
		cruiseControl = false;
        

		//Get vehicle type
        VehicleType type = this.getVehicleType();
        DriveableData data = getDriveableData();
        if(type == null)
        {
        	FlansMod.log("Vehicle type null. Not ticking vehicle");
        	return;
        }


        //Boosting things
		if (boostTimer > 0F && !boostExhausted)
		{
			boostTimer--;
			if (!isBoosting)
			{
				//if(!worldObj.isRemote)
					//PacketPlaySound.sendSoundPacket(posX, posY, posZ, 16, dimension, "vehicleboost", false);
				
				isBoosting = true;
			}
			throttle += 0.05F * type.acceleration;
			if (throttle < 0F)
			throttle = 0F;
			
			
			isBoosting = true;
			boostGauge -= 5F/(Math.max(type.boostDrain,0.01));
			
			if (boostGauge<=0)
			{
				boostGauge = 0F;
				boostTimer = 0F;
				boostExhausted = true;
			}
		}
		else
		{
			isBoosting = false;
			if (boostGauge<100F)
			boostGauge += 100F/(20*type.boostRecharge);
		}
		
		if (boostExhausted && boostGauge > 100F*type.boostEnableThreshold)
			boostExhausted = false;
		
		if (boostGauge > 100F)
			boostGauge = 100F;
		
		
		//Reduce throttle over time when Cruise Control is not active
      	if(!cruiseControl || (!hasEnoughFuel() && type.fuelTankSize!=-1))
      	{
      		if (throttle > 0.002F)
      		throttle = (throttle - ((0.0015F + (0.015F*throttle))*(type.throttleDecayRate/throttleDivider)));
      		
      		if (throttle < -0.002F)
      		throttle = (throttle + ((0.0015F + (0.016F*(-throttle)))*type.throttleDecayRate));
      		
      		if ((throttle <= 0.002F) && (throttle >= -0.002F))
      		throttle = throttle/2;
			/*else
			{
				if (throttle > 0.35 && throttle < 0.995)
				throttle = (throttle*0.98F)+(0.35F-(throttle*0.98F));
				if (throttle < -0.35 && throttle > -0.995)
				throttle = (throttle*0.98F)-(0.35F-(throttle*0.98F));
			}*/
      	}
		
		//Hard limit throttle.
		throttle = MathUtils.clampf(throttle, -1, 1);
		

        //Work out if this is the client side and the player is driving
        boolean thePlayerIsDrivingThis = worldObj.isRemote && seats[0] != null && seats[0].riddenByEntity instanceof EntityPlayer && FlansMod.proxy.isThePlayer((EntityPlayer)seats[0].riddenByEntity);

        //Despawning
		ticksSinceUsed++;
		if(!worldObj.isRemote && seats[0].riddenByEntity != null)
			ticksSinceUsed = 0;
		if(!worldObj.isRemote && TeamsManager.vehicleLife > 0 && ticksSinceUsed > TeamsManager.vehicleLife * 20)
		{
			setDead();
		}
		
		if(this.worldObj.isRemote && (this.varFlare || this.ticksFlareUsing > (type.timeFlareUsing * 20)-5))
		{
			if(this.ticksExisted % 5 ==0)
			{
				deployedSmoke = true;
			}
		}
		
		if(type.setPlayerInvisible && !this.worldObj.isRemote && seats[0].riddenByEntity != null)
			seats[0].riddenByEntity.setInvisible(true);

		if(this.ticksFlareUsing<= 0) deployedSmoke = false;
		
		if(this.ticksFlareUsing > 0)
			this.ticksFlareUsing--;
		if(this.flareDelay > 0)
			this.flareDelay--;

		//Shooting, inventories, etc.
		//Decrement shell and gun timers
		if(shellDelay > 0)
			shellDelay--;
		if(gunDelay > 0)
			gunDelay--;
		if(toggleTimer > 0)
			toggleTimer--;
		if(toggleCruiseTimer > 0)
			toggleCruiseTimer--;
		if(soundPosition > 0)
			soundPosition--;	
		if(idlePosition > 0)
			idlePosition--;	
				
		if(type.tank && !hasBothTracks()) throttle = 0;
		if(disabled) wheelsYaw = 0;
		
		
		//Boat check - boats have zero throttle out of water.
		if (type.boat)
		{
			if(!worldObj.isAnyLiquid(this.boundingBox))
				throttle = 0;
			if(worldObj.isAnyLiquid(this.boundingBox))
				inWater = true;
		}
		
		
		//Before we move on, get the vehicle's speed
		float motion = (float) Math.sqrt( motionX * motionX + motionY * motionY + motionZ * motionZ);
		float motionH = (float) Math.sqrt( motionX * motionX + motionY * motionY);
		
		//Rotate the wheels
		if(hasEnoughFuel() || type.fuelTankSize==-1)
		{
			wheelsAngle += (throttle * ((throttle > 0 ? type.maxThrottle : type.maxNegativeThrottle) * data.engine.engineSpeed)) / 4F;
		}
		
		if(!varDoor){
			doorPos = transformPart(doorPos, type.doorPos1, type.doorRate);
			doorRot = transformPart(doorRot, type.doorRot1, type.doorRotRate);
		} else {
			doorPos = transformPart(doorPos, type.doorPos2, type.doorRate);
			doorRot = transformPart(doorRot, type.doorRot2, type.doorRotRate);
		}

		//Return the wheels to their resting position
		wheelsYaw *= 0.75F;

		//Limit wheel angles
		if(wheelsYaw > 20)
			wheelsYaw = 20;
		if(wheelsYaw < -20)
			wheelsYaw = -20;
		
		
		/* Drifting mechanics */
		if(isDrifting && totalWheelsOnGround>1)
		{
		//Primary drift mechanics: steering controls some of the sliding.
		driftSlide += 65*(steeringForce)*type.driftControl;
		
		//Drifting slowly returns to normal over time, depending on drift traction.
		driftSlide *= (0.956F + type.driftTraction)/(type.driftTraction+1);
		//if ((!leftTurnHeld && !rightTurnHeld) || (leftTurnHeld && rightTurnHeld))
		
		//Reduce acceleration when drifting.
		if (accelHeld)
		throttle -= ((0.023F * type.acceleration)/10)/throttleDivider;
		
		//Too tight of a drift will cause speed loss, the severity depending on how tight the drift is.
		if(Math.abs(driftSlide)>20 && throttle>0.85)
		throttle-=(0.002/throttleDivider);
		
		if(Math.abs(driftSlide)>55 && throttle>0.05)
		throttle-=(0.02/throttleDivider);
		
		//Too low of a throttle causes drifting to decrease.
		if(adjustThrottle<=0.6)
		driftSlide-=(driftSlide > 0 ? 1 : -1);
	
		//If going way too slow or reversed, all sliding quickly diminishes.
		if(adjustThrottle<=0.2)
		driftSlide*=0.5F;
	
		//Apply the hard drift cap.
		if(driftSlide>type.driftCap)
			driftSlide=type.driftCap;
		else
		if(driftSlide<-type.driftCap)
			driftSlide=-type.driftCap;
		
		if(Math.abs(driftSlide)<3)
			isDrifting = false;
		}
		else
		if(totalWheelsOnGround>1)
		{
		driftSlide*=0.75F;
			
		if(Math.abs(driftSlide)<0.04)
		driftSlide = 0;
		}
		
		
		//Reset total wheels on ground counter.
		totalWheelsOnGround = 0;
		

		//Player is not driving this. Update its position from server update packets
		if(worldObj.isRemote && !thePlayerIsDrivingThis)
		{
			//The driveable is currently moving towards its server position. Continue doing so.
            if (serverPositionTransitionTicker > 0)
            {
                double x = posX + (serverPosX - posX) / serverPositionTransitionTicker;
                double y = posY + (serverPosY - posY) / serverPositionTransitionTicker;
                double z = posZ + (serverPosZ - posZ) / serverPositionTransitionTicker;
                double dYaw = MathHelper.wrapAngleTo180_double(serverYaw - axes.getYaw());
                double dPitch = MathHelper.wrapAngleTo180_double(serverPitch - axes.getPitch());
                double dRoll = MathHelper.wrapAngleTo180_double(serverRoll - axes.getRoll());
                rotationYaw = (float)(axes.getYaw() + dYaw / serverPositionTransitionTicker);
                rotationPitch = (float)(axes.getPitch() + dPitch / serverPositionTransitionTicker);
                float rotationRoll = (float)(axes.getRoll() + dRoll / serverPositionTransitionTicker);
                --serverPositionTransitionTicker;
                setPosition(x, y, z);
                setRotation(rotationYaw, rotationPitch, rotationRoll);
                //return;
            }
            //If the driveable is at its server position and does not have the next update, it should just simulate itself as a server side driveable would, so continue
		}

		//Movement

		correctWheelPos();

		Vector3f amountToMoveCar = new Vector3f();

		for(EntityWheel wheel : wheels)
		{
			if(wheel == null)
				continue;
			
			double prevPosYWheel = wheel.posY;

			//Hacky way of forcing the car to step up blocks
			if (type.wheelStepHeight != 0)
			{
			onGround = true;
			wheel.onGround = true;
			
			List<Entity> shipsCheck = worldObj.getEntitiesWithinAABB(Entity.class, wheel.boundingBox);

			boolean onShip = false;
			for(Entity ship : shipsCheck){
				if(this.getClass().toString().indexOf("cuchaz.ships.EntityShip") > 0){
					wheel.onGround = true;
					onShip = true;
				}
			}
			}
			
			//Update angles
			wheel.rotationYaw = axes.getYaw();
			//Front wheels
			if(!type.tank && (wheel.ID == 2 || wheel.ID == 3))
			{
				wheel.rotationYaw += wheelsYaw;
			}

			wheel.motionX *= 0.9F;
			wheel.motionY *= this.posY - bkPrevPosY < 0? 0.999F: 0.9F;
			wheel.motionZ *= 0.9F;
			

			//Apply velocity
			//If the player driving this is in creative, then we can thrust, no matter what
			boolean canThrustCreatively = !TeamsManager.vehiclesNeedFuel ||  type.fuelTankSize==-1 || (seats != null && seats[0] != null && seats[0].riddenByEntity instanceof EntityPlayer && ((EntityPlayer)seats[0].riddenByEntity).capabilities.isCreativeMode);
			//Otherwise, check the fuel tanks!
			if(canThrustCreatively || data.fuelInTank > data.engine.fuelConsumption * throttle)
			{
				if(getVehicleType().tank)
				{
					boolean left = wheel.ID == 0 || wheel.ID == 3;

					float turningDrag = 0.02F;
					wheel.motionX *= 1F - (Math.abs(wheelsYaw) * turningDrag);
					wheel.motionZ *= 1F - (Math.abs(wheelsYaw) * turningDrag);

					float velocityScale = 0.04F * (adjustThrottle > 0 ? type.maxThrottle : type.maxNegativeThrottle) * data.engine.engineSpeed;
					float steeringScale = 0.1F * (wheelsYaw > 0 ? type.turnLeftModifier : type.turnRightModifier);
					float effectiveWheelSpeed = (adjustThrottle + (wheelsYaw * (left ? 1 : -1) * steeringScale)) * velocityScale;
					wheel.motionX += effectiveWheelSpeed * Math.cos(wheel.rotationYaw * 3.14159265F / 180F);
					wheel.motionZ += effectiveWheelSpeed * Math.sin(wheel.rotationYaw * 3.14159265F / 180F);
					yawSpeed += effectiveWheelSpeed * Math.sin(wheel.rotationYaw * 3.14159265F / 180F);


				}
				else
				{
					float driftFactor = driftSlide;
					
					displayDriftFactor = driftFactor;
					
					//if(getVehicleType().fourWheelDrive || wheel.ID == 0 || wheel.ID == 1)
					{
						float velocityScale = 0.1F * (throttle + (isBoosting ? type.boostPower : 0F)) * (throttle > 0 ? type.maxThrottle : type.maxNegativeThrottle) * data.engine.engineSpeed;
						wheel.motionX += Math.cos((wheel.rotationYaw - (driftFactor/3)) * 3.14159265F / 180F) * velocityScale;
						wheel.motionZ += Math.sin((wheel.rotationYaw - (driftFactor/3)) * 3.14159265F / 180F) * velocityScale;
					}

					//Apply steering
					if(wheel.ID == 2 || wheel.ID == 3)
					{
						float sThrottle = Math.min(adjustThrottle,(type.maxTurningThrottle/throttleDivider));
						
						//float velocityScale = 0.01F * (wheelsYaw > 0 ? type.turnLeftModifier : type.turnRightModifier) * (throttle > 0 ? 1 : -1);
						//float velocityScale = 0.01F * (wheelsYaw > 0 ? type.turnLeftModifier : type.turnRightModifier) * (motionH*(type.traction) > 0 ? 1 : 0);
						//float velocityScale = 0.01F * (wheelsYaw > 0 ? type.turnLeftModifier : type.turnRightModifier) * (adjustThrottle-2F+((type.traction+50)/50F) > 0 ? 1 : 0);
						
						//float velocityScale = (0.01F - Math.max( (0.024F / (((type.traction+10F)*2.5F)/(3+((sThrottle-adjustThrottle))) + 2F/4F)) * (Math.min(Math.max(sThrottle*2-0.3F,0),1)),0.01F )) * (wheelsYaw > 0 ? type.turnLeftModifier*1.5F : type.turnRightModifier*1.5F) * (adjustThrottle > 0 ? 1 : -1);
						
						float velocityScale = ( !isDrifting ? (0.01F*MathUtils.clampf( (type.traction * (wheelsYaw > 0 ? type.turnLeftModifier : type.turnRightModifier))/22,0,1)) * ( sThrottle > 0 ? (sThrottle/(type.maxTurningThrottle/throttleDivider)) : 0) : 0) * (adjustThrottle > 0 ? (1/throttleDivider) : -1);
						
						wheel.motionX -= wheel.getSpeedXZ() * Math.sin((wheel.rotationYaw - (driftFactor/2)) * 3.14159265F / 180F) * velocityScale * (wheelsYaw /*- (driftFactor/1)*/);
						wheel.motionZ += wheel.getSpeedXZ() * Math.cos((wheel.rotationYaw - (driftFactor/2)) * 3.14159265F / 180F) * velocityScale * (wheelsYaw /*- (driftFactor/1)*/);
					}
					else
					{
						wheel.motionX *= 0.9F;
						wheel.motionZ *= 0.9F;
					}
				}
				
				//Rate of fuel consumption may need fixing here.
				if(!canThrustCreatively)
					data.fuelInTank -= data.engine.fuelConsumption * type.fuelUseMultiplier * Math.abs(throttle) * 0.05F;
				
			}
			
			wheel.moveEntity(wheel.motionX, wheel.motionY, wheel.motionZ);

			//Pull wheels towards car
			Vector3f targetWheelPos = axes.findLocalVectorGlobally(getVehicleType().wheelPositions[wheel.ID].position);

			Vector3f currentWheelPos = new Vector3f(wheel.posX - posX, wheel.posY - posY, wheel.posZ - posZ);

			Vector3f dPos = ((Vector3f)Vector3f.sub(targetWheelPos, currentWheelPos, null).scale(type.wheelSpringStrength));
			
			if(dPos.length() > 0.001F)
			{
				wheel.moveEntity(dPos.x, dPos.y, dPos.z);
				dPos.scale(0.5F);
				Vector3f.sub(amountToMoveCar, dPos, amountToMoveCar);
			}
			
			float avgWheelHeight = 0F;
			
			//Secondary check whether all wheels are on ground...
			if(wheels[0] != null && wheels[1] != null && wheels[2] != null && wheels[3] != null)
			{
			avgWheelHeight = (float)(wheels[0].posX + wheels[1].posX + wheels[2].posX + wheels[3].posX)/4;
	    	if(!wheels[0].onGround && !wheels[1].onGround && !wheels[2].onGround && !wheels[3].onGround){
	    		allWheelsOnGround = false;
				for(int i = 0; i < 4; i++)
				{
				if(wheels[i].onGround)
				totalWheelsOnGround++;
				}
	    	} else {
	    		allWheelsOnGround = true;
				totalWheelsOnGround = 4;
	    	}
			}
			
			
			//Before we move on, moderate submarine float heights
			if (type.submarine)
			{
				if (subFloatLevel > ((posY-(posY-subFloatLevel)) + 0.5))
					subFloatLevel = 0.5F;
				if (subFloatLevel < ((posY-(posY-subFloatLevel)) - 0.5))
					subFloatLevel = -0.5F;
				
				if (subFloatLevel > type.maxSubFloatHeight)
					subFloatLevel = type.maxSubFloatHeight;
			}
			
			//Set floating height
			float floatingHeight = type.floatOffset;
			if (type.submarine)
			floatingHeight = subFloatLevel;
			

			//Now we apply gravity
			if(allWheelsOnGround && !(type.floatOnWater && worldObj.isAnyLiquid(wheel.boundingBox.copy().offset(0, -floatingHeight, 0))) && !wheel.onDeck){
				wheel.moveEntity(0F, (!onDeck)?-0.98F/5:0, 0F);
			} else if((type.floatOnWater && worldObj.isAnyLiquid(wheel.boundingBox.copy().offset(0, -floatingHeight, 0))) && worldObj.isAnyLiquid(wheel.boundingBox.copy().offset(0, 1 - floatingHeight, 0)) && !wheel.onDeck){
				wheel.moveEntity(0F, 1F, 0F);	
			} else if((type.floatOnWater && worldObj.isAnyLiquid(wheel.boundingBox.copy().offset(0, -floatingHeight, 0))) && !worldObj.isAnyLiquid(wheel.boundingBox.copy().offset(0, 1 - floatingHeight, 0)) || wheel.onDeck){
				wheel.moveEntity(0F, 0F, 0F);
			this.roll = 0;
			this.pitch = 0;
			} else {
				wheel.moveEntity(0F, (!onDeck)?-0.98F:0, 0F);	
			}
			
			if((throttle >= 0.2 || throttle <= -0.2) && wheel.getSpeedXYZ() <= getSpeedXYZ()/4 && !cruiseControl) throttle *= 0.9;
		}
		
		if(wheels[0] != null && wheels[1] != null && wheels[2] != null && wheels[3] != null)
		{
			lastPos.x = (float)(wheels[0].motionX + wheels[1].motionX + wheels[2].motionX + wheels[3].motionX)/4;
			lastPos.y = (float)(wheels[0].motionY + wheels[1].motionY + wheels[2].motionY + wheels[3].motionY)/4;
			lastPos.z = (float)(wheels[0].motionZ + wheels[1].motionZ + wheels[2].motionZ + wheels[3].motionZ)/4;
		}

		
		double bmy = this.motionY;
		this.motionY = amountToMoveCar.y;
		moveEntity(amountToMoveCar.x, amountToMoveCar.y, amountToMoveCar.z);
		this.motionY = bmy;

		if(wheels[0] != null && wheels[1] != null && wheels[2] != null && wheels[3] != null)
		{
			Vector3f frontAxleCentre = new Vector3f((wheels[2].posX + wheels[3].posX) / 2F, (wheels[2].posY + wheels[3].posY) / 2F, (wheels[2].posZ + wheels[3].posZ) / 2F);
			Vector3f backAxleCentre = new Vector3f((wheels[0].posX + wheels[1].posX) / 2F, (wheels[0].posY + wheels[1].posY) / 2F, (wheels[0].posZ + wheels[1].posZ) / 2F);
			Vector3f leftSideCentre = new Vector3f((wheels[0].posX + wheels[3].posX) / 2F, (wheels[0].posY + wheels[3].posY) / 2F, (wheels[0].posZ + wheels[3].posZ) / 2F);
			Vector3f rightSideCentre = new Vector3f((wheels[1].posX + wheels[2].posX) / 2F, (wheels[1].posY + wheels[2].posY) / 2F, (wheels[1].posZ + wheels[2].posZ) / 2F);

			float dx = frontAxleCentre.x - backAxleCentre.x;
			float dy = frontAxleCentre.y - backAxleCentre.y;
			float dz = frontAxleCentre.z - backAxleCentre.z;
			float drx = leftSideCentre.x - rightSideCentre.x;
			float dry = leftSideCentre.y - rightSideCentre.y;
			float drz = leftSideCentre.z - rightSideCentre.z;


			float dxz = (float)Math.sqrt(dx * dx + dz * dz);
			float drxz = (float)Math.sqrt(drx * drx + drz * drz);

			float tyaw = (float)Math.atan2(dz, dx);
			float tpitch = -(float)Math.atan2(dy, dxz);
			float troll = 0F;
			if(type.canRoll){
				troll = -(float)Math.atan2(dry, drxz);
			}

			yaw = tyaw;
			pitch = Lerp(pitch, tpitch, 0.2F);
			roll = Lerp(roll, troll, 0.2F);
			
			if(type.boat || type.submarine)
			{
			pitch = 0;
			roll = 0;
			}
			
			//Steering
			if(type.tank)
			{
				float velocityScale = 0.04F * (throttle > 0 ? type.maxThrottle : type.maxNegativeThrottle) * data.engine.engineSpeed;
				float steeringScale = 0.1F * (wheelsYaw > 0 ? type.turnLeftModifier : type.turnRightModifier);
				float effectiveWheelSpeed = ((wheelsYaw * steeringScale)) * velocityScale;
				yaw = axes.getYaw()/180F*3.14159F + (effectiveWheelSpeed);
			} else {
				//float velocityScale = 0.1F * throttle * (throttle > 0 ? type.maxThrottle : type.maxNegativeThrottle) * data.engine.engineSpeed;
				throttleCalc = (adjustThrottle > 0 ? (adjustThrottle < (type.maxTurningThrottle/throttleDivider) ? adjustThrottle : (type.maxTurningThrottle/throttleDivider)) : adjustThrottle);
				
				//float velocityScale = 0.1F * Math.min(throttleCalc,Math.abs(type.driftSpeed)) * (adjustThrottle > 0 ? type.maxThrottle : type.maxNegativeThrottle) * data.engine.engineSpeed;
				float cDriftSlide = MathUtils.clampf((driftSlide*0.25f),-7,7);
				
				velocityScaleG = 0.1F * (throttleCalc/throttleDivider) * (adjustThrottle > 0 ? (type.maxThrottle/throttleDivider) : type.maxNegativeThrottle) * data.engine.engineSpeed;
				float steeringScale = 0.1F * (wheelsYaw > 0 ? (type.turnLeftModifier/throttleDivider)*1.5F : (type.turnRightModifier/throttleDivider)*1.5F);
				float driftScale = 0.1F * (cDriftSlide < 0 ? (type.turnLeftModifier/throttleDivider)*1.5F : (type.turnRightModifier/throttleDivider)*1.5F);
				steeringForce = ((wheelsYaw * steeringScale)) * velocityScaleG;
				driftSteerForce =  ((cDriftSlide * driftScale)) * velocityScaleG;
				
				if (!isDrifting)
				{
				finalTurnFactor = steeringForce*(totalWheelsOnGround>1 || inWater ? 1 : 0.1F);
				yaw = axes.getYaw()/180F*3.14159F + (finalTurnFactor);
				}
				else
				{
					//This part is a bit more complex.
					//The minimum steering limit is based on how much the vehicle is sliding.
					float finalSteering = steeringForce;
					
					if (driftSlide>0)
						finalSteering = Math.max(steeringForce*(!isDrifting ? 1 : type.driftSteering),driftSteerForce);
					else
					if (driftSlide<0)
						finalSteering = Math.min(steeringForce*(!isDrifting ? 1 : type.driftSteering),driftSteerForce);
					
					
					finalTurnFactor = finalSteering*(totalWheelsOnGround>1 || inWater ? 1 : 0.1F);
					
					yaw = axes.getYaw()/180F*3.14159F + (finalTurnFactor);
				}
			}
			
			axes.setAngles(yaw * 180F / 3.14159F, pitch * 180F / 3.14159F, roll * 180F / 3.14159F);
		}

    	if(this.ridingEntity != null)
    	{
    		if(this.ridingEntity.getClass().toString().indexOf("mcheli.aircraft.MCH_EntitySeat") > 0)
    		{
    			axes.setAngles(this.ridingEntity.rotationYaw+90, 0, 0);
    		}
    	}

		checkForCollisions();

		//Sounds
		//Starting sound
		if ((((Math.abs(throttle) > 0.02F && Math.abs(throttle) < type.engineSoundThreshold && (hasEnoughFuel() || type.fuelTankSize==-1)) || ((!hasEnoughFuel() && type.fuelTankSize!=-1) && Math.abs(throttle) > 0.02F))) && soundPosition == 0)
		{
			if(!worldObj.isRemote)
			PacketPlaySound.sendSoundPacket(posX, posY, posZ, type.startSoundRange, dimension, type.startSound, false);
			soundPosition = type.startSoundLength;
		}
		//Driving sound
		if (throttle >= type.engineSoundThreshold && soundPosition == 0 && (hasEnoughFuel() || type.fuelTankSize==-1))
		{
			if(!worldObj.isRemote)
			PacketPlaySound.sendSoundPacket(posX, posY, posZ, type.engineSoundRange, dimension, type.engineSound, false);
			soundPosition = type.engineSoundLength;
		}
		//Idle/Motor sound
		if(seats[0] != null){
		if((throttle <= 0.02F && throttle >= -0.02F || type.idleSoundAsMotor) && seats[0].riddenByEntity != null && idlePosition == 0 && (hasEnoughFuel() || type.fuelTankSize==-1) && !disabled)
		{
			if(!worldObj.isRemote)
			PacketPlaySound.sendSoundPacket(posX, posY, posZ, type.engineSoundRange, dimension, type.idleSound, false);	
			idlePosition = type.idleSoundLength;
		}
		}
		//Reverse sound
		if (throttle <= -type.backSoundThreshold && soundPosition == 0 && (hasEnoughFuel() || type.fuelTankSize==-1))
		{
			if(!worldObj.isRemote)
			PacketPlaySound.sendSoundPacket(posX, posY, posZ, type.backSoundRange, dimension, type.backSound, false);
			soundPosition = type.backSoundLength;
		}

		for(EntitySeat seat : seats)
		{
			if(seat != null)
			{
				seat.updatePosition();
	        }	
		}

		//Calculate movement on the client and then send position, rotation etc to the server
		if(thePlayerIsDrivingThis)
		{
			FlansMod.getPacketHandler().sendToServer(new PacketVehicleControl(this));
			serverPosX = posX;
			serverPosY = posY;
			serverPosZ = posZ;
			serverYaw = axes.getYaw();
			//if(type.IT1)
			//FlansMod.getPacketHandler().sendToServer(new PacketIT1Reload(this));
		}

		//If this is the server, send position updates to everyone, having received them from the driver
		if(!worldObj.isRemote && ticksExisted % 5 == 0)
		{
			FlansMod.getPacketHandler().sendToAllAround(new PacketVehicleControl(this), posX, posY, posZ, FlansMod.driveableUpdateRange, dimension);
			//if(type.IT1)
			//FlansMod.getPacketHandler().sendToAllAround(new PacketIT1Reload(this), posX, posY, posZ, FlansMod.driveableUpdateRange, dimension);

		}

		int animSpeed = 4;

		if((throttle > 0.05 && throttle <= 0.33) || (throttle < -0.05 && throttle >= -0.33)){
			animSpeed = 3;
		} else if((throttle > 0.33 && throttle <= 0.66) || (throttle < -0.33 && throttle >= -0.66)){
			animSpeed = 2;
		} else if((throttle > 0.66 && throttle <= 0.9) || (throttle < -0.66 && throttle >= -0.9)){
			animSpeed = 1;
		} else if((throttle > 0.9 && throttle <= 1) || (throttle < -0.9 && throttle >= -1)){
			animSpeed = 0;
		}
		
		boolean turningLeft = false;
		boolean turningRight = false;

		if(throttle > 0.05){
			animCountLeft --;
			animCountRight --;
		} else if (throttle < -0.05){
			animCountLeft ++;
			animCountRight ++;
		} else if (wheelsYaw < -1){
			turningLeft = true;
			animCountLeft ++;
			animCountRight --;
			animSpeed = 1;
			if (soundPosition == 0 && (hasEnoughFuel() || type.fuelTankSize==-1) && type.tank)
			{
				if(!worldObj.isRemote)
				PacketPlaySound.sendSoundPacket(posX, posY, posZ, type.engineSoundRange, dimension, type.engineSound, false);
				soundPosition = type.engineSoundLength;
			}
		} else if(wheelsYaw > 1){
			turningRight = true;
			animCountLeft --;
			animCountRight ++;
			animSpeed = 1;
			if (soundPosition == 0 && (hasEnoughFuel() || type.fuelTankSize==-1) && type.tank)
			{
				if(!worldObj.isRemote)
				PacketPlaySound.sendSoundPacket(posX, posY, posZ, type.engineSoundRange, dimension, type.engineSound, false);
				soundPosition = type.engineSoundLength;
			}
		}else {
			turningLeft = false;
			turningRight = false;
		}

		if(animCountLeft <= 0){
			animCountLeft = animSpeed;
			animFrameLeft ++;
		}
		
		if(animCountRight <= 0){
			animCountRight = animSpeed;
			animFrameRight ++;
		}

		if(throttle < 0 || turningLeft){
			if(animCountLeft >= animSpeed){
				animCountLeft = 0;
	        	animFrameLeft --;
			}
		}
		
		if(throttle < 0 || turningRight){
			if(animCountRight >= animSpeed){
				animCountRight = 0;
	        	animFrameRight --;
			}
		}

		if(animFrameLeft > type.animFrames){
			animFrameLeft = 0;
		} if(animFrameLeft < 0){
			animFrameLeft = type.animFrames;
		}
		
		if(animFrameRight > type.animFrames){
			animFrameRight = 0;
		} if(animFrameRight < 0){
			animFrameRight = type.animFrames;
		}
		
        //rotateYaw(10);
		
		//All done here, reset inputs.
		leftTurnHeld = false;
		rightTurnHeld = false;
		accelHeld = false;
		brakeHeld = false;
    }
    
    public void dischargeSmoke()
    {
    	VehicleType type = this.getVehicleType();
        for(int i = 0; i < type.smokers.size(); i++)
        {
        	SmokePoint smoker = type.smokers.get(i);
        	Vector3f dir = smoker.direction;
        	Vector3f pos = smoker.position;
        	int time = smoker.detTime;
        	
    		dir = axes.findLocalVectorGlobally(dir);
    		pos = axes.findLocalVectorGlobally(pos);
        	
        	if(EnumDriveablePart.getPart(smoker.part) == EnumDriveablePart.turret)
        	{
        		dir = rotate(seats[0].looking.findLocalVectorGlobally(smoker.direction));
            	pos = getPositionOnTurret(smoker.position, false);
        	}
        	
			//FlansMod.getPacketHandler().sendToAllAround(new PacketSmokeGrenade(posX + pos.x/16, posY + pos.y/16, posZ + pos.z/16, dir.x, dir.y, dir.z, time), posX, posY, posZ, 150, dimension);
    	
        	//FlansMod.proxy.spawnSmokeGrenade("flansmod.smoker", posX + pos.x/16, posY + pos.y/16, posZ + pos.z/16, dir.x, dir.y, dir.z, time);
        	
			FlansMod.getPacketHandler().sendToAllAround(new PacketParticle("flansmod.smoker",posX + pos.x/16, posY + pos.y/16, posZ + pos.z/16, dir.x, dir.y, dir.z), posX, posY, posZ, 150, dimension);
        }
    }

    public float Lerp(float start, float end, float percent)
    {
         float result = (start + percent*(end - start));
         
         return result;
    }
    
    public static float Clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }
    
	public List<Entity> findEntitiesWithinbounds(){
    	VehicleType type = this.getVehicleType();
    	AxisAlignedBB initialBox = this.boundingBox.copy();
    	List<Entity> riddenEntities = worldObj.getEntitiesWithinAABB(Entity.class, initialBox);
    	
		Vector3f size = new Vector3f(type.harvestBoxSize.x/8F, type.harvestBoxSize.y/8F, type.harvestBoxSize.z/8F);
		Vector3f pos = new Vector3f(type.harvestBoxPos.x/8F, type.harvestBoxPos.y/8F, type.harvestBoxPos.z/8F);
		for(float x = pos.x; x <= pos.x + size.x; x++)
		{
			for(float y = pos.y; y <= pos.y + size.y; y++)
			{
				for(float z = pos.z; z <= pos.z + size.z; z++)
				{
					Vector3f v = axes.findLocalVectorGlobally(new Vector3f(x, y, z));

					double entX = (posX + v.x);
					double entY = (posY + v.y);
					double entZ = (posZ + v.z);
					AxisAlignedBB checkBox = this.boundingBox.copy().offset(v.x, v.y, v.z);
					
					List<Entity> entityhere = worldObj.getEntitiesWithinAABB(Entity.class, checkBox);
					
					for(int i = 0; i < entityhere.size(); i++)
					{
						if(entityhere.get(i) instanceof EntityLivingBase) riddenEntities.add(entityhere.get(i));
					}
					//Iterator<Entity> iter = entityhere.iterator();
					/**
					while( iter.hasNext() )
					{
						Entity entity = iter.next();
						if(isPartOfThis(entity)) iter.remove();
						if(entity instanceof EntityBullet) iter.remove();
					} */

				}
			}
		}
    	return riddenEntities;
    }
    
	public Vector3f transformPart(Vector3f current, Vector3f target, Vector3f rate){
		Vector3f newPos = current;
		
		if(Math.sqrt((current.x - target.x)*(current.x - target.x)) > rate.x/2){
			if(current.x > target.x){
				current.x = current.x - rate.x;
			} else if (current.x < target.x){
				current.x = current.x + rate.x;
			}
		} else {
			current.x = target.x;
		}
		
		if(Math.sqrt((current.y - target.y)*(current.y - target.y)) > rate.y/2){
			if(current.y > target.y){
				current.y = current.y - rate.y;
			} else if (current.y < target.y){
				current.y = current.y + rate.y;
			}
		} else {
			current.y = target.y;
		}
		
		if(Math.sqrt((current.z - target.z)*(current.z - target.z)) > rate.z/2){
			if(current.z > target.z){
				current.z = current.z - rate.z;
			} else if (current.z < target.z){
				current.z = current.z + rate.z;
			}
		} else {
			current.z = target.z;
		}

		return newPos;
	}

	@Override
	protected void fall(float k)
	{
		if (k <= 10) return;
		float damage = MathHelper.ceiling_float_int(k) * 2;

		boolean no_damage = true;
		if (damage > 0 && invulnerableUnmountCount == 0 && this.ticksExisted > 20)
		{
			DriveableType type = getDriveableType();
			damage = (int)(damage * type.fallDamageFactor);
			attackPart(EnumDriveablePart.core, DamageSource.fall, damage);
			if (type.wheelPositions.length > 0)
			{
				attackPart(type.wheelPositions[0].part, DamageSource.fall, damage / 5);
			}

			no_damage = false;
		}
	//	FlansMod.log("fall%s : tick=%d damage=%.1f", no_damage? " no damage":"", this.ticksExisted, damage);
	}

    private float averageAngles(float a, float b)
    {
    	FlansMod.log("Pre  " + a + " " + b);

    	float pi = (float)Math.PI;
    	for(; a > b + pi; a -= 2 * pi) ;
    	for(; a < b - pi; a += 2 * pi) ;

    	float avg = (a + b) / 2F;

    	for(; avg > pi; avg -= 2 * pi) ;
    	for(; avg < -pi; avg += 2 * pi) ;

    	FlansMod.log("Post " + a + " " + b + " " + avg);

    	return avg;
    }

	private Vec3 subtract(Vec3 a, Vec3 b)
	{
		return Vec3.createVectorHelper(a.xCoord - b.xCoord, a.yCoord - b.yCoord, a.zCoord - b.zCoord);
	}

	private Vec3 crossProduct(Vec3 a, Vec3 b)
	{
        return Vec3.createVectorHelper(a.yCoord * b.zCoord - a.zCoord * b.yCoord, a.zCoord * b.xCoord - a.xCoord * b.zCoord, a.xCoord * b.yCoord - a.yCoord * b.xCoord);
	}

    @Override
    public boolean landVehicle()
    {
    	return true;
    }

    @Override
    public boolean attackEntityFrom(DamageSource damagesource, float i)
    {
        if(worldObj.isRemote || isDead)
            return true;

        VehicleType type = getVehicleType();

		if(damagesource.damageType.equals("player") && damagesource.getEntity().onGround && (seats[0] == null || seats[0].riddenByEntity == null) && !locked)
		{
			ItemStack vehicleStack = new ItemStack(type.item, 1, 0);
			vehicleStack.stackTagCompound = new NBTTagCompound();
			driveableData.writeToNBT(vehicleStack.stackTagCompound);
			entityDropItem(vehicleStack, 0.5F);
	 		setDead();
		}
        return super.attackEntityFrom(damagesource, i);
    }

	public VehicleType getVehicleType()
	{
		return VehicleType.getVehicle(driveableType);
	}

	@Override
	public float getPlayerRoll()
	{
		return axes.getRoll();
	}
	
	public float getAvgWheelSpeedXYZ()
	{
		float speed = (float)(wheels[0].getSpeedXYZ() + wheels[1].getSpeedXYZ() + wheels[2].getSpeedXYZ() + wheels[3].getSpeedXYZ())/4;
		
		return speed;
	}
	
	public void Recoil()
	{
		
	}

	@Override
	protected void dropItemsOnPartDeath(Vector3f midpoint, DriveablePart part)
	{
	}

	@Override
	public String getBombInventoryName()
	{
		return "Mines";
	}

	@Override
	public String getMissileInventoryName()
	{
		return "Shells";
	}

	@Override
	public boolean hasMouseControlMode()
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EntityLivingBase getCamera()
	{
		return null;
	}
	
	public boolean hasBothTracks()
	{
		boolean tracks = true;
		if(!isPartIntact(EnumDriveablePart.leftTrack))
		{
			tracks = false;
		}
		
		if(!isPartIntact(EnumDriveablePart.rightTrack))
		{
			tracks = false;
		}

		return tracks;
	}

	@Override
	public void setDead()
	{
		super.setDead();
		for(EntityWheel wheel : wheels)
			if(wheel != null)
				wheel.setDead();
	}
}
