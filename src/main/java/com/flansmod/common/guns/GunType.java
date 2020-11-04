package com.flansmod.common.guns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.flansmod.client.model.ModelGun;
import com.flansmod.client.model.ModelMG;
import com.flansmod.common.FlansMod;
import com.flansmod.common.types.InfoType;
import com.flansmod.common.types.TypeFile;
import com.flansmod.common.vector.Vector3f;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class GunType extends InfoType implements IScope
{
	public static final Random rand = new Random();
	
	//Gun Behaviour Variables
	/** The list of bullet types that can be used in this gun */
	public List<ShootableType> ammo = new ArrayList<ShootableType>();
	/** Whether the player can press the reload key (default R) to reload this gun */
	public boolean canForceReload = true;
	/** Whether the player can shoot while sprinting */
	public boolean canSprintShoot = false;
	/** The time (in ticks) it takes to reload this gun */
	public int reloadTime;
	/** The amount to recoil the player's view by when firing a single shot from this gun */
	public float recoilPitch = 0.0F;
	public float recoilYaw = 0.0F;
	public float rndRecoilPitchRange = 0;
	public float rndRecoilYawRange = 0;
	public float decreaseRecoilPitch = 0;
	public float decreaseRecoilYaw = 1;
	/** The amount that bullets spread out when fired from this gun */
	public float bulletSpread;
	/** Whether the bullet type controls the spread value */
	public boolean allowSpreadByBullet = false;
	/** Damage inflicted by this gun. Multiplied by the bullet damage. */
	public float damage = 0;
	/** The damage inflicted upon punching someone with this gun */
	public float meleeDamage = 1;
	/** The speed of bullets upon leaving this gun */
	public float bulletSpeed = 5.0F;
	/** Whether the bullet type controls the speed of the projectiles */
	public boolean allowBulletSpeedByBulletType = false;
	/** The number of bullet entities created by each shot */
	public int numBullets = 1;
	/** Whether the bullet type controls the number of projectiles spawned */
	public boolean allowNumBulletsByBulletType = false;
	/** The delay between shots in ticks (1/20ths of seconds).  Defaults to 4, breaks if set to 0 or lower */
	public int shootDelay = 4;
	/** Number of ammo items that the gun may hold. Most guns will hold one magazine.
	 * Some may hold more, such as Nerf pistols, revolvers or shotguns */
	public int numAmmoItemsInGun = 1;
	/** The firing mode of the gun. One of semi-auto, full-auto, minigun or burst */
	public EnumFireMode mode = EnumFireMode.FULLAUTO;
	public EnumFireMode[] submode = new EnumFireMode[]{ EnumFireMode.FULLAUTO };
	public EnumFireMode defaultmode = mode;
	/** The number of bullets to fire per burst in burst mode */
	public int numBurstRounds = 3;
	/** The required speed for minigun mode guns to start firing */
	public float minigunStartSpeed = 15F;
	/** Whether this gun can be used underwater */
	public boolean canShootUnderwater = true;
	/** The amount of knockback to impact upon the player per shot */
	public float knockback = 0F;
	/** The secondary function of this gun. By default, the left mouse button triggers this */
	public EnumSecondaryFunction secondaryFunction = EnumSecondaryFunction.ADS_ZOOM;
	public EnumSecondaryFunction secondaryFunctionWhenShoot = null;
	/** If true, then this gun can be dual wielded */
	public boolean oneHanded = false;
	/** For one shot items like a panzerfaust */
	public boolean consumeGunUponUse = false;
	/** Item to drop on shooting */
	public String dropItemOnShoot = null;
	//Custom Melee Stuff
	/** The time delay between custom melee attacks */
	public int meleeTime = 1;
	/** The path the melee weapon takes */
	public ArrayList<Vector3f> meleePath = new ArrayList<Vector3f>(), meleePathAngles = new ArrayList<Vector3f>();
	/** The points on the melee weapon that damage is actually done from. */
	public ArrayList<Vector3f> meleeDamagePoints = new ArrayList<Vector3f>();
	/** Set these to make guns only usable by a certain type of entity */
	public boolean usableByPlayers = true, usableByMechas = true;
	/** Whether Gun makes players to be EnumAction.bow */
	public EnumAction itemUseAction = EnumAction.bow;
	public int canLockOnAngle = 5;
	public int lockOnSoundTime = 0;
	public String lockOnSound = "";
	public int maxRangeLockOn = 80;

	public boolean canSetPosition = false;

	public boolean lockOnToPlanes = false, lockOnToVehicles = false, lockOnToMechas = false, lockOnToPlayers = false, lockOnToLivings = false;
	
	
	/* Zed's Extras! */
	/**Backward intensity in the recoil animation*/
	public float recoilAnimX = 0.5F;
	/**Upward/downward recoil intensity in the recoil animation*/
	public float recoilAnimY = 0.0F;
	/**Angular recoil intensity in the recoil animation*/
	public float recoilAnimRotate = 0.0F;
	
	/**Backward/forward movement in the pump animation*/
	public float pumpAnimX = 0.7F;
	/**Upward/downward movement in the pump animation*/
	public float pumpAnimY = -0.5F;
	/**Sideways movement in the pump animation*/
	public float pumpAnimZ = 0.0F;
	/**Pitch rotation in the pump animation*/
	public float pumpAnimPitch = 4.0F;
	/**Yaw rotation in the pump animation*/
	public float pumpAnimYaw = 0.5F;
	/**Roll/tilt rotation in the pump animation*/
	public float pumpAnimTilt = 3.0F;
	
	/**For bolt-action rifles: tilt factor of the bolt itself.  Unused by default*/
	public float boltAnimTilt = 0.0F;
	/**For bolt-action rifles: the speed in which the bolt tilts.  Unused by default*/
	public float boltTiltSpeed = 1.0F;
	/**For bolt-action rifles: the offset of the bolt tilt.  Unused by default*/
	public float boltCenterOffset = 0.0F;
	
	/**The sprinting animation type*/
	public EnumSprintHoldType sprintAnim = EnumSprintHoldType.SIMPLE;
	
	/**Y-Offset when a scope is equipped, set config-side for precisely lining up attached scopes*/
	public float scopedYOffset = 0;
	/**Y-Offset when a scope is not equipped, set config-side for precisely lining up iron sights*/
	public float unscopedYOffset = 0;
	/**Rotation when a scope is not equipped*/
	public float unscopedRotation = 0;
	/**X-Offset applied to the model*/
	public float gunXOffset = 0;
	/**Z-Offset applied to the model*/
	public float gunZOffset = 0;
	
	/**Additional Y Movement when in ADS, with no scope attached*/
	public float sightsYShiftUnscoped = 0;
	/**Additional Y Movement when in ADS, with a scope attached*/
	public float sightsYShiftScoped = 0;
	/**Additional X Movement when in ADS*/
	public float sightsXShift = 0;
	/**Increase to sightsXShift when scoped in*/
	public float sightsXScopedShift = 0;
	/**Additional Z Movement when in ADS, with no scope attached*/
	public float sightsZShiftUnscoped = 0;
	/**Additional Z Movement when in ADS, with a scope attached*/
	public float sightsZShiftScoped = 0;
	/**Z-Offset when the gun is NOT being aimed*/
	public float hipZShift = 0;
	
	public boolean renderCrosshairUnscoped = true;
	public boolean renderCrosshairScoped = false;
	public boolean renderCrosshairAiming = false;
	
	public boolean alwaysShowFiremode = false;
	
	/**Whether the DecreaseRecoil variable is a divider or subtracter.  True is divider, false is subtracter*/
	public boolean decreaseRecoilDivider = false;
	
	/**Whether to swing the weapon on shooting.  Useful for making things like magic wands... for whatever reason*/
	public boolean swingOnShoot = false;
	
	/**The delay before the gun can reload by right clicking when empty.  Has no effect when lower than shootDelay*/
	public int reloadDelay = 10;
	
	/**Multiplier for bullet spread when sprinting*/
	public float spreadSprintMultiplier = 1.5F;
	/**Multiplier for bullet spread when firing from the hip*/
	public float spreadHipfireMultiplier = 1.0F;
	/**Multiplier for bullet spread when aiming*/
	public float spreadAimMultiplier = 1.0F;
	
	/*Knockback stuff*/
	public boolean doKnockback = true;
	public boolean modifyKnockback = true;
	public double kbDampener = 2;
	public boolean useBulletKnockback = false;
	
	
	
	//Attachment Offsets
	//These stack with the positions defined by the models.
	/*Scope Offsets*/
	public float attachScopeXOffset = 0.0F;
	public float attachScopeYOffset = 0.0F;
	public float attachScopeZOffset = 0.0F;
	
	/*Barrel Offsets*/
	public float attachBarrelXOffset = 0.0F;
	public float attachBarrelYOffset = 0.0F;
	public float attachBarrelZOffset = 0.0F;
	
	/*Grip Offsets*/
	public float attachGripXOffset = 0.0F;
	public float attachGripYOffset = 0.0F;
	public float attachGripZOffset = 0.0F;
	
	/*Stock Offsets*/
	public float attachStockXOffset = 0.0F;
	public float attachStockYOffset = 0.0F;
	public float attachStockZOffset = 0.0F;
	
	
	public float attachScopeScale = 1.0F;
	public float attachBarrelScale = 1.0F;
	public float attachGripScale = 1.0F;
	public float attachStockScale = 1.0F;
	

	//Information
	//Show any variables into the GUI when hovering over items.
	/** If false, then attachments wil not be listed in item GUI */
	public boolean showAttachments = true;
	/** Show statistics */
	public boolean showDamage = false, showRecoil = false, showSpread = false;
	/** Show reload time in seconds */
	public boolean showReloadTime = false;
	public boolean showPaintjobName = false;

	//Shields
	//A shield is actually a gun without any shoot functionality (similar to knives or binoculars)
	//and a load of shield code on top. This means that guns can have in built shields (think Nerf Stampede)
	/** Whether or not this gun has a shield piece */
	public boolean shield = false;
	/** Shield collision box definition. In model co-ordinates */
	public Vector3f shieldOrigin, shieldDimensions;
	/** Float between 0 and 1 denoting the proportion of damage blocked by the shield */
	public float shieldDamageAbsorption = 0F;

	//Sounds
	/** The sound played upon shooting */
	public String shootSound;
	/** Whether to use a custom silenced shoot sound */
	public boolean customSilencedSound = false;
	/** The sound played upon shooting with a silencer */
	public String shootSoundSilenced;
	/** The length of the sound for looping sounds */
	public int shootSoundLength;
	/** The length of the sound for looping sounds */
	public int shootSilencedSoundLength;
	/** Whether to distort the sound or not. Generally only set to false for looping sounds */
	public boolean distortSound = true;
	/** The sound to play upon reloading */
	public String reloadSound;
	public int idleSoundRange = 50;
	public int meleeSoundRange = 50;
	public int reloadSoundRange = 50;
	public int gunSoundRange = 50;

	//Looping sounds
	/** Whether the looping sounds should be used. Automatically set if the player sets any one of the following sounds */
	public boolean useLoopingSounds = false;
	/** Played when the player starts to hold shoot */
	public String warmupSound;
	public int warmupSoundLength = 20;
	/** Played in a loop until player stops holding shoot */
	public String loopedSound;
	public int loopedSoundLength = 20;
	/** Played when the player stops holding shoot */
	public String cooldownSound;


	/** The sound to play upon weapon swing */
	public String meleeSound;
	/** The sound to play while holding the weapon in the hand*/
	public String idleSound;
	public int idleSoundLength;


	//Deployable Settings
	/** If true, then the bullet does not shoot when right clicked, but must instead be placed on the ground */
	public boolean deployable = false;
	/** The deployable model */
	@SideOnly(Side.CLIENT)
	public ModelMG deployableModel;
	/** The deployable model's texture*/
	public String deployableTexture;
	/** Various deployable settings controlling the player view limits and standing position */
	public float standBackDist = 1.5F, topViewLimit = -60F, bottomViewLimit = 30F, sideViewLimit = 45F, pivotHeight = 0.375F;

	//Default Scope Settings. Overriden by scope attachments
	//In many cases, this will simply be iron sights
	/** Default scope overlay texture */
	public String defaultScopeTexture = "None";
	/** Whether the default scope has an overlay */
	public boolean hasScopeOverlay = false;
	/** The zoom level of the default scope */
	public float zoomLevel = 1.0F;
	/** The FOV zoom level of the default scope */
	public float FOVFactor = 1.5F;

	public boolean allowNightVision = false;

	/** For guns with 3D models */
	@SideOnly(Side.CLIENT)
	public ModelGun model;
	/** For making detailed models and scaling down */
	public float modelScale = 1F;

	//Attachment settings
	/** If this is true, then all attachments are allowed. Otherwise the list is checked */
	public boolean allowAllAttachments = false;
	/** The list of allowed attachments for this gun */
	public ArrayList<AttachmentType> allowedAttachments = new ArrayList<AttachmentType>();
	/** Whether each attachment slot is available */
	public boolean allowBarrelAttachments = false, allowScopeAttachments = false,
			allowStockAttachments = false, allowGripAttachments = false;
	/** The number of generic attachment slots there are on this gun */
	public int numGenericAttachmentSlots = 0;

	//Paintjobs
	/** The list of all available paintjobs for this gun */
	public ArrayList<Paintjob> paintjobs = new ArrayList<Paintjob>();
	/** The default paintjob for this gun. This is created automatically in the load process from existing info */
	public Paintjob defaultPaintjob;
	/** The name for the default paintjob for this gun. */
	public String defaultPaintjobName = "null";

	/** The static hashmap of all guns by shortName */
	public static HashMap<String, GunType> guns = new HashMap<String, GunType>();
	/** The static list of all guns */
	public static ArrayList<GunType> gunList = new ArrayList<GunType>();

	//Modifiers
	/** Speeds up or slows down player movement when this item is held */
	public float moveSpeedModifier = 1F;
	/** Speeds slows down player movement when this item is in Inventory */
	public int activateSlowInInventoryLevel = -1;
	/** Gives knockback resistance to the player */
	public float knockbackModifier = 0F;

	public GunType(TypeFile file)
	{
		super(file);
	}

	@Override
	public void postRead(TypeFile file)
	{
		gunList.add(this);
		guns.put(shortName, this);

		//After all lines have been read, set up the default paintjob
		defaultPaintjob = new Paintjob(iconPath, texture, defaultPaintjobName, new ItemStack[0]);
		//Move to a new list to ensure that the default paintjob is always first
		ArrayList<Paintjob> newPaintjobList = new ArrayList<Paintjob>();
		newPaintjobList.add(defaultPaintjob);
		newPaintjobList.addAll(paintjobs);
		paintjobs = newPaintjobList;
	}

	@Override
	protected void read(String[] split, TypeFile file)
	{
		super.read(split, file);
		try
		{
			if(split[0].equals("Damage"))
				damage = Float.parseFloat(split[1]);
			else if(split[0].equals("MeleeDamage"))
			{
				meleeDamage = Float.parseFloat(split[1]);
				if(meleeDamage > 0F)
					secondaryFunction = EnumSecondaryFunction.MELEE;
			}
			else if(split[0].equals("CanForceReload"))
				canForceReload = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("CanShootWhileSprinting"))
				canSprintShoot = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("ReloadTime"))
				reloadTime = Integer.parseInt(split[1]);
			else if(split[0].equals("Recoil"))
				recoilPitch = Float.parseFloat(split[1]);
			else if(split[0].equals("RecoilYaw"))
				recoilYaw = Float.parseFloat(split[1]) / 10;
			else if(split[0].equals("RandomRecoilRange"))
				rndRecoilPitchRange = Float.parseFloat(split[1]);
			else if(split[0].equals("RandomRecoilYawRange"))
				rndRecoilYawRange = Float.parseFloat(split[1]);
			else if(split[0].equals("DecreaseRecoil"))
				decreaseRecoilPitch = Float.parseFloat(split[1]);
			else if(split[0].equals("DecreaseRecoilYaw"))
				decreaseRecoilYaw = Float.parseFloat(split[1]);
			else if(split[0].equals("Knockback"))
				knockback = Float.parseFloat(split[1]);
			else if(split[0].equals("Accuracy") || split[0].equals("Spread"))
				bulletSpread = Float.parseFloat(split[1]);
			else if(split[0].equals("NumBullets"))
				numBullets = Integer.parseInt(split[1]);
			else if(split[0].equals("AllowNumBulletsByBulletType"))
				allowNumBulletsByBulletType = Boolean.parseBoolean(split[1]);
			else if(split[0].equals("AllowSpreadByBullet"))
				allowSpreadByBullet = Boolean.parseBoolean(split[1]);
			else if(split[0].equals("AllowBulletSpeedByBulletType") || split[0].equals("allowBulletSpeedByBulletType"))
				allowBulletSpeedByBulletType = Boolean.parseBoolean(split[1]);
			else if(split[0].equals("CanLockAngle"))
				canLockOnAngle = Integer.parseInt(split[1]);
			else if(split[0].equals("LockOnSoundTime"))
				lockOnSoundTime = Integer.parseInt(split[1]);
			else if(split[0].equals("LockOnToDriveables"))
				lockOnToPlanes = lockOnToVehicles = lockOnToMechas = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("LockOnToVehicles"))
				lockOnToVehicles = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("LockOnToPlanes"))
				lockOnToPlanes = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("LockOnToMechas"))
				lockOnToMechas = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("LockOnToPlayers"))
				lockOnToPlayers = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("LockOnToLivings"))
				lockOnToLivings = Boolean.parseBoolean(split[1].toLowerCase());

			else if(split[0].equals("ConsumeGunOnUse"))
				consumeGunUponUse = Boolean.parseBoolean(split[1]);
			else if(split[0].equals("DropItemOnShoot"))
				dropItemOnShoot = split[1];
			else if(split[0].equals("NumBurstRounds"))
				numBurstRounds = Integer.parseInt(split[1]);
			else if(split[0].equals("MinigunStartSpeed"))
				minigunStartSpeed = Float.parseFloat(split[1]);
			else if(split[0].equals("ItemUseAction")){
				itemUseAction = EnumAction.valueOf(split[1].toLowerCase());
			}
			else if(split[0].equals("MaxRangeLockOn"))
				maxRangeLockOn = Integer.parseInt(split[1]);


			//Information
			else if(split[0].equals("ShowAttachments"))
				showAttachments = Boolean.parseBoolean(split[1]);
			else if(split[0].equals("ShowDamage"))
				showDamage = Boolean.parseBoolean(split[1]);
			else if(split[0].equals("ShowRecoil"))
				showRecoil = Boolean.parseBoolean(split[1]);
			else if(split[0].equals("ShowAccuracy"))
				showSpread = Boolean.parseBoolean(split[1]);
			else if(split[0].equals("ShowReloadTime"))
				showReloadTime = Boolean.parseBoolean(split[1]);
			else if(split[0].equals("ShowPaintjobName"))
				showPaintjobName = Boolean.parseBoolean(split[1]);
			

			//Sounds
			else if(split[0].equals("ShootDelay"))
				shootDelay = Integer.parseInt(split[1]);
			else if(split[0].equals("SoundLength"))
				shootSoundLength = Integer.parseInt(split[1]);
			else if(split[0].equals("SilencedSoundLength"))
				shootSilencedSoundLength = Integer.parseInt(split[1]);
			else if(split[0].equals("DistortSound"))
				distortSound = split[1].equals("True");
			else if(split[0].equals("IdleSoundRange"))
				idleSoundRange = Integer.parseInt(split[1]);
			else if(split[0].equals("MeleeSoundRange"))
				meleeSoundRange = Integer.parseInt(split[1]);
			else if(split[0].equals("ReloadSoundRange"))
				reloadSoundRange = Integer.parseInt(split[1]);
			else if(split[0].equals("GunSoundRange"))
				gunSoundRange = Integer.parseInt(split[1]);
			else if(split[0].equals("CustomSilencedSound"))
				customSilencedSound = Boolean.parseBoolean(split[1]);
			else if(split[0].equals("ShootSound"))
			{
				shootSound = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			}
			else if(split[0].equals("ShootSoundSilenced"))
			{
				shootSoundSilenced = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			}
			else if(split[0].equals("ReloadSound"))
			{
				reloadSound = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			}
			else if(split[0].equals("IdleSound"))
			{
				idleSound = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			}
			else if(split[0].equals("IdleSoundLength"))
				idleSoundLength = Integer.parseInt(split[1]);
			else if(split[0].equals("MeleeSound"))
			{
				meleeSound = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			}

			//Looping sounds
			else if(split[0].equals("WarmupSound"))
			{
				warmupSound = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			}
			else if(split[0].equals("WarmupSoundLength"))
				warmupSoundLength = Integer.parseInt(split[1]);
			else if(split[0].equals("LoopedSound") || split[0].equals("SpinSound"))
			{
				loopedSound = split[1];
				useLoopingSounds = true;
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			}
			else if(split[0].equals("LoopedSoundLength") || split[0].equals("SpinSoundLength"))
				loopedSoundLength = Integer.parseInt(split[1]);
			else if(split[0].equals("CooldownSound"))
			{
				cooldownSound = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			}
			else if(split[0].equals("LockOnSound"))
			{
				lockOnSound = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			}

			//Modes and zoom settings
			else if(split[0].equals("Mode"))
			{
				mode = EnumFireMode.getFireMode(split[1]);
				defaultmode = mode;
				submode = new EnumFireMode[ split.length - 1 ];
				for(int i=0; i<submode.length; i++)
				{
					submode[i] = EnumFireMode.getFireMode(split[1 + i]);
				}
			}
			else if(split[0].equals("Scope"))
			{
				hasScopeOverlay = true;
				if (split[1].equals("None"))
					hasScopeOverlay = false;
				else defaultScopeTexture = split[1];
			}
			else if(split[0].equals("AllowNightVision"))
			{
				allowNightVision = Boolean.parseBoolean(split[1]);
			}
			else if(split[0].equals("ZoomLevel"))
			{
				zoomLevel = Float.parseFloat(split[1]);
				if(zoomLevel > 1F)
					secondaryFunction = EnumSecondaryFunction.ZOOM;
			}
			else if(split[0].equals("FOVZoomLevel"))
			{
				FOVFactor = Float.parseFloat(split[1]);
				if(FOVFactor > 1F)
					secondaryFunction = EnumSecondaryFunction.ADS_ZOOM;
			}
			else if(split[0].equals("Deployable"))
				deployable = split[1].equals("True");
			else if(FMLCommonHandler.instance().getSide().isClient() && deployable && split[0].equals("DeployedModel"))
			{
				deployableModel = FlansMod.proxy.loadModel(split[1], shortName, ModelMG.class);
			}
			else if(FMLCommonHandler.instance().getSide().isClient() && (split[0].equals("Model")))
			{
				model = FlansMod.proxy.loadModel(split[1], shortName, ModelGun.class);
			}
			else if(split[0].equals("ModelScale"))
				modelScale = Float.parseFloat(split[1]);
			else if(split[0].equals("Texture"))
				texture = split[1];
			else if(split[0].equals("DefaultPaintjobName"))
			{
				String paintName = split[1];
				paintName = paintName.replace("_"," ");
				defaultPaintjobName = paintName;
			}
			else if(split[0].equals("DeployedTexture"))
				deployableTexture = split[1];
			else if(split[0].equals("StandBackDistance"))
				standBackDist = Float.parseFloat(split[1]);
			else if(split[0].equals("TopViewLimit"))
				topViewLimit = -Float.parseFloat(split[1]);
			else if(split[0].equals("BottomViewLimit"))
				bottomViewLimit = Float.parseFloat(split[1]);
			else if(split[0].equals("SideViewLimit"))
				sideViewLimit = Float.parseFloat(split[1]);
			else if(split[0].equals("PivotHeight"))
				pivotHeight = Float.parseFloat(split[1]);
			else if(split[0].equals("Ammo"))
			{
				ShootableType type = ShootableType.getShootableType(split[1]);
				if(type != null)
					ammo.add(type);
			}
			else if(split[0].equals("NumAmmoSlots") || split[0].equals("NumAmmoItemsInGun") || split[0].equals("LoadIntoGun"))
				numAmmoItemsInGun = Integer.parseInt(split[1]);
			else if(split[0].equals("BulletSpeed"))
				bulletSpeed = Float.parseFloat(split[1]);
			else if(split[0].equals("CanShootUnderwater"))
				canShootUnderwater = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("CanSetPosition"))
				canSetPosition = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("OneHanded"))
				oneHanded = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("SecondaryFunction"))
				secondaryFunction = EnumSecondaryFunction.get(split[1]);
			else if(split[0].equals("UsableByPlayers"))
				usableByPlayers = Boolean.parseBoolean(split[1]);
			else if(split[0].equals("UsableByMechas"))
				usableByMechas = Boolean.parseBoolean(split[1]);

			//Custom Melee Stuff
			else if(split[0].equals("UseCustomMelee") && Boolean.parseBoolean(split[1]))
			{
				secondaryFunction = EnumSecondaryFunction.CUSTOM_MELEE;
				sprintAnim = EnumSprintHoldType.NONE;
			}
			else if(split[0].equals("UseCustomMeleeWhenShoot") && Boolean.parseBoolean(split[1]))
				secondaryFunctionWhenShoot = EnumSecondaryFunction.CUSTOM_MELEE;
			else if(split[0].equals("MeleeTime"))
				meleeTime = Integer.parseInt(split[1]);
			else if(split[0].equals("AddNode"))
			{
				meleePath.add(new Vector3f(Float.parseFloat(split[1]) / 16F, Float.parseFloat(split[2]) / 16F, Float.parseFloat(split[3]) / 16F));
				meleePathAngles.add(new Vector3f(Float.parseFloat(split[4]), Float.parseFloat(split[5]), Float.parseFloat(split[6])));
			}
			else if(split[0].equals("MeleeDamagePoint") || split[0].equals("MeleeDamageOffset"))
			{
				meleeDamagePoints.add(new Vector3f(Float.parseFloat(split[1]) / 16F, Float.parseFloat(split[2]) / 16F, Float.parseFloat(split[3]) / 16F));
			}

			//Player modifiers
			else if(split[0].equals("MoveSpeedModifier") || split[0].equals("Slowness"))
				moveSpeedModifier = Float.parseFloat(split[1]);
			else if(split[0].equals("ActivateSlowInInventoryLevel"))
				activateSlowInInventoryLevel = Integer.parseInt(split[1]);
			else if(split[0].equals("KnockbackReduction") || split[0].equals("KnockbackModifier"))
				knockbackModifier = Float.parseFloat(split[1]);

			//Attachment settings
			else if(split[0].equals("AllowAllAttachments"))
				allowAllAttachments = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("AllowAttachments"))
			{
				for(int i = 1; i < split.length; i++)
				{
					allowedAttachments.add(AttachmentType.getAttachment(split[i]));
				}
			}
			else if(split[0].equals("AllowBarrelAttachments"))
				allowBarrelAttachments = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("AllowScopeAttachments"))
				allowScopeAttachments = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("AllowStockAttachments"))
				allowStockAttachments = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("AllowGripAttachments"))
				allowGripAttachments = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("NumGenericAttachmentSlots"))
				numGenericAttachmentSlots = Integer.parseInt(split[1]);

			//Paintjobs
			else if(split[0].toLowerCase().equals("paintjob"))
			{
				ItemStack[] dyeStacks = new ItemStack[(split.length - 3) / 2];
				for(int i = 0; i < (split.length - 3) / 2; i++)
					dyeStacks[i] = new ItemStack(Items.dye, Integer.parseInt(split[i * 2 + 4]), getDyeDamageValue(split[i * 2 + 3]));
				paintjobs.add(new Paintjob(split[1], split[2], dyeStacks));
			}
			else if(split[0].toLowerCase().equals("namedpaintjob") || split[0].toLowerCase().equals("addpaintjob"))
			{
				ItemStack[] dyeStacks = new ItemStack[(split.length - 4) / 2];
				for(int i = 0; i < (split.length - 4) / 2; i++)
					dyeStacks[i] = new ItemStack(Items.dye, Integer.parseInt(split[i * 2 + 5]), getDyeDamageValue(split[i * 2 + 4]));
				String paintName = split[3];
				paintName = paintName.replace("_"," ");
				paintjobs.add(new Paintjob(split[1], split[2], paintName, dyeStacks));
			}

			//Shield settings
			else if(split[0].toLowerCase().equals("shield"))
			{
				shield = true;
				shieldDamageAbsorption = Float.parseFloat(split[1]);
				shieldOrigin = new Vector3f(Float.parseFloat(split[2]) / 16F, Float.parseFloat(split[3]) / 16F, Float.parseFloat(split[4]) / 16F);
				shieldDimensions = new Vector3f(Float.parseFloat(split[5]) / 16F, Float.parseFloat(split[6]) / 16F, Float.parseFloat(split[7]) / 16F);
			}
			
			//Zed additional settings
			else if(split[0].equals("RecoilAnimXForce"))
				recoilAnimX = Float.parseFloat(split[1]);
			else if(split[0].equals("RecoilAnimYForce"))
				recoilAnimY = Float.parseFloat(split[1]);
			else if(split[0].equals("RecoilAnimRotation"))
				recoilAnimRotate = Float.parseFloat(split[1]);
			
			else if(split[0].equals("PumpAnimXShift"))
				pumpAnimX = Float.parseFloat(split[1]);
			else if(split[0].equals("PumpAnimYShift"))
				pumpAnimY = Float.parseFloat(split[1]);
			else if(split[0].equals("PumpAnimZShift"))
				pumpAnimZ = Float.parseFloat(split[1]);
			else if(split[0].equals("PumpAnimPitch"))
				pumpAnimPitch = Float.parseFloat(split[1]);
			else if(split[0].equals("PumpAnimYaw"))
				pumpAnimYaw = Float.parseFloat(split[1]);
			else if(split[0].equals("PumpAnimTilt"))
				pumpAnimTilt = Float.parseFloat(split[1]);
			else if(split[0].equals("BoltAnimTilt"))
				boltAnimTilt = Float.parseFloat(split[1]);
			else if(split[0].equals("BoltRotateSpeed"))
				boltTiltSpeed = Float.parseFloat(split[1]);
			else if(split[0].equals("BoltCenterOffset"))
				boltCenterOffset = Float.parseFloat(split[1]);
			
			else if(split[0].equals("ScopedYOffset"))
				scopedYOffset = Float.parseFloat(split[1]);
			else if(split[0].equals("UnscopedYOffset"))
				unscopedYOffset = Float.parseFloat(split[1]);
			else if(split[0].equals("UnscopedRotation"))
				unscopedRotation = Float.parseFloat(split[1]);
			else if(split[0].equals("GunXOffset") || split[0].equals("XOffset"))
				gunXOffset = Float.parseFloat(split[1]);
			else if(split[0].equals("GunZOffset") || split[0].equals("ZOffset"))
				gunZOffset = Float.parseFloat(split[1]);
			
			else if(split[0].equals("SightsYShiftUnscoped") || split[0].equals("AimYShiftUnscoped"))
				sightsYShiftUnscoped = Float.parseFloat(split[1]);
			else if(split[0].equals("SightsYShiftScoped") || split[0].equals("AimYShiftScoped"))
				sightsYShiftScoped = Float.parseFloat(split[1]);
			else if(split[0].equals("SightsXShift") || split[0].equals("AimXShift"))
				sightsXShift = Float.parseFloat(split[1]);
			else if(split[0].equals("SightsXScopedOffset") || split[0].equals("AimXScopedOffset"))
				sightsXScopedShift = Float.parseFloat(split[1]);
			else if(split[0].equals("SightsZShiftUnscoped") || split[0].equals("AimZShiftUnscoped"))
				sightsZShiftUnscoped = Float.parseFloat(split[1]);
			else if(split[0].equals("SightsZShiftScoped") || split[0].equals("AimZShiftScoped"))
				sightsZShiftScoped = Float.parseFloat(split[1]);
			else if(split[0].equals("HipZShift"))
				hipZShift = Float.parseFloat(split[1]);
			
			
			else if(split[0].equals("UseDecreaseRecoilDivider"))
			{
				if (decreaseRecoilPitch > 0)
				decreaseRecoilDivider = Boolean.parseBoolean(split[1]);
			}
			
			else if(split[0].equals("SwingWeaponOnShoot"))
				swingOnShoot = Boolean.parseBoolean(split[1]);
			else if(split[0].equals("RenderCrosshairWhenUnscoped"))
				renderCrosshairUnscoped = Boolean.parseBoolean(split[1]);
			else if(split[0].equals("RenderCrosshairWhenScoped"))
				renderCrosshairScoped = Boolean.parseBoolean(split[1]);
			else if(split[0].equals("RenderCrosshairWhenAiming"))
				renderCrosshairAiming = Boolean.parseBoolean(split[1]);
			else if(split[0].equals("AlwaysShowFiremode"))
				alwaysShowFiremode = Boolean.parseBoolean(split[1]);
			else if(split[0].equals("SprintAnimationType"))
				sprintAnim = EnumSprintHoldType.getSprintHoldType(split[1].toLowerCase());
			
			else if(split[0].equals("ReloadDelay"))
				reloadDelay = Integer.parseInt(split[1]);
			
			else if(split[0].equals("SpreadMultiplierSprinting"))
				spreadSprintMultiplier = Float.parseFloat(split[1]);
			else if(split[0].equals("SpreadMultiplierAiming"))
				spreadAimMultiplier = Float.parseFloat(split[1]);
			else if(split[0].equals("SpreadMultiplierHip"))
				spreadHipfireMultiplier = Float.parseFloat(split[1]);
			
			else if(split[0].equals("DoKnockback"))
				doKnockback = Boolean.parseBoolean(split[1]);
			else if(split[0].equals("ModifyKnockback"))
				modifyKnockback = Boolean.parseBoolean(split[1]);
			else if(split[0].equals("KnockbackDampening"))
				kbDampener = Double.parseDouble(split[1]);
			else if(split[0].equals("UseBulletKnockback"))
				useBulletKnockback = Boolean.parseBoolean(split[1]);
			
			else if(split[0].equals("ScopeAttachmentOffset"))
			{
				attachScopeXOffset = Float.parseFloat(split[1])/16;
				attachScopeYOffset = Float.parseFloat(split[2])/16;
				attachScopeZOffset = Float.parseFloat(split[3])/16;
			}
			else if(split[0].equals("BarrelAttachmentOffset"))
			{
				attachBarrelXOffset = Float.parseFloat(split[1])/16;
				attachBarrelYOffset = Float.parseFloat(split[2])/16;
				attachBarrelZOffset = Float.parseFloat(split[3])/16;
			}
			else if(split[0].equals("GripAttachmentOffset"))
			{
				attachGripXOffset = Float.parseFloat(split[1])/16;
				attachGripYOffset = Float.parseFloat(split[2])/16;
				attachGripZOffset = Float.parseFloat(split[3])/16;
			}
			else if(split[0].equals("StockAttachmentOffset"))
			{
				attachStockXOffset = Float.parseFloat(split[1])/16;
				attachStockYOffset = Float.parseFloat(split[2])/16;
				attachStockZOffset = Float.parseFloat(split[3])/16;
			}
			
			else if(split[0].equals("AllAttachmentScale"))
			{
				attachScopeScale = Float.parseFloat(split[1]);
				attachBarrelScale = Float.parseFloat(split[1]);
				attachGripScale = Float.parseFloat(split[1]);
				attachStockScale = Float.parseFloat(split[1]);
			}
			else if(split[0].equals("ScopeAttachmentScale"))
				attachScopeScale = Float.parseFloat(split[1]);
			else if(split[0].equals("BarrelAttachmentScale"))
				attachBarrelScale = Float.parseFloat(split[1]);
			else if(split[0].equals("GripAttachmentScale"))
				attachGripScale = Float.parseFloat(split[1]);
			else if(split[0].equals("StockAttachmentScale"))
				attachStockScale = Float.parseFloat(split[1]);
			
			
			
		}
		catch (Exception e)
		{
			if(split!=null)
			{
				String msg = " : ";
				for(String s : split) msg = msg +" "+ s;
				System.out.println("Reading gun file failed. " + file.name + msg);
			}
			else
			{
				System.out.println("Reading gun file failed. " + file.name);
			}
			if(FlansMod.printStackTrace)
			{
				e.printStackTrace();
			}
		}


	}

	/** Return a dye damage value from a string name */
	private int getDyeDamageValue(String dyeName)
	{
		int damage = -1;
		for(int i = 0; i < ItemDye.field_150923_a.length; i++)
		{
			if(ItemDye.field_150923_a[i].equals(dyeName))
				damage = i;
		}
		if(damage == -1)
			FlansMod.log("Failed to find dye colour : " + dyeName + " while adding " + contentPack);

		return damage;
	}

	public boolean isAmmo(ShootableType type)
	{
		return ammo.contains(type);
	}

	public boolean isAmmo(ItemStack stack)
	{
		if (stack == null)
			return false;
		else if(stack.getItem() instanceof ItemBullet)
		{
			return isAmmo(((ItemBullet)stack.getItem()).type);
		}
		else if(stack.getItem() instanceof ItemGrenade)
		{
			return isAmmo(((ItemGrenade)stack.getItem()).type);
		}
		return false;
	}

	/** To be overriden by subtypes for model reloading */
	public void reloadModel()
	{
		model = FlansMod.proxy.loadModel(modelString, shortName, ModelGun.class);
	}

	@Override
	public float getZoomFactor()
	{
		return zoomLevel;
	}

	@Override
	public boolean hasZoomOverlay()
	{
		return hasScopeOverlay;
	}

	@Override
	public String getZoomOverlay()
	{
		return defaultScopeTexture;
	}

	@Override
	public float getFOVFactor()
	{
		return FOVFactor;
	}

	//ItemStack specific methods

	/** Return the currently active scope on this gun. Search attachments, and by default, simply give the gun */
	public IScope getCurrentScope(ItemStack gunStack)
	{
		IScope attachedScope = getScope(gunStack);
		return attachedScope == null ? this : attachedScope;
	}

	/** Returns all attachments currently attached to the specified gun */
	public ArrayList<AttachmentType> getCurrentAttachments(ItemStack gun)
	{
		checkForTags(gun);
		ArrayList<AttachmentType> attachments = new ArrayList<AttachmentType>();
		NBTTagCompound attachmentTags = gun.stackTagCompound.getCompoundTag("attachments");
		NBTTagList genericsList = attachmentTags.getTagList("generics", (byte)10); //TODO : Check this 10 is correct
		for(int i = 0; i < numGenericAttachmentSlots; i++)
		{
			appendToList(gun, "generic_" + i, attachments);
		}
		appendToList(gun, "barrel", attachments);
		appendToList(gun, "scope", attachments);
		appendToList(gun, "stock", attachments);
		appendToList(gun, "grip", attachments);
		return attachments;
	}

	/** Private method for attaching attachments to a list of attachments with a nullcheck */
	private void appendToList(ItemStack gun, String name, ArrayList<AttachmentType> attachments)
	{
		AttachmentType type = getAttachment(gun, name);
		if(type != null) attachments.add(type);
	}

	//Attachment getter methods
	public AttachmentType getBarrel(ItemStack gun) { return getAttachment(gun, "barrel"); }
	public AttachmentType getScope(ItemStack gun) { return getAttachment(gun, "scope"); }
	public AttachmentType getStock(ItemStack gun) { return getAttachment(gun, "stock"); }
	public AttachmentType getGrip(ItemStack gun) { return getAttachment(gun, "grip"); }
	public AttachmentType getGeneric(ItemStack gun, int i) { return getAttachment(gun, "generic_" + i); }

	//Attachment ItemStack getter methods
	public ItemStack getBarrelItemStack(ItemStack gun) { return getAttachmentItemStack(gun, "barrel"); }
	public ItemStack getScopeItemStack(ItemStack gun) { return getAttachmentItemStack(gun, "scope"); }
	public ItemStack getStockItemStack(ItemStack gun) { return getAttachmentItemStack(gun, "stock"); }
	public ItemStack getGripItemStack(ItemStack gun) { return getAttachmentItemStack(gun, "grip"); }
	public ItemStack getGenericItemStack(ItemStack gun, int i) { return getAttachmentItemStack(gun, "generic_" + i); }

	/** Generalised attachment getter method */
	public AttachmentType getAttachment(ItemStack gun, String name)
	{
		checkForTags(gun);
		return AttachmentType.getFromNBT(gun.stackTagCompound.getCompoundTag("attachments").getCompoundTag(name));
	}

	/** Generalised attachment ItemStack getter method */
	public ItemStack getAttachmentItemStack(ItemStack gun, String name)
	{
		checkForTags(gun);
		return ItemStack.loadItemStackFromNBT(gun.stackTagCompound.getCompoundTag("attachments").getCompoundTag(name));
	}

	/** Method to check for null tags and assign default empty tags in that case */
	private void checkForTags(ItemStack gun)
	{
		//If the gun has no tags, give it some
		if(!gun.hasTagCompound())
		{
			gun.stackTagCompound = new NBTTagCompound();
		}
		//If the gun has no attachment tags, give it some
		if(!gun.stackTagCompound.hasKey("attachments"))
		{
			NBTTagCompound attachmentTags = new NBTTagCompound();
			for(int i = 0; i < numGenericAttachmentSlots; i++)
				attachmentTags.setTag("generic_" + i, new NBTTagCompound());
			attachmentTags.setTag("barrel", new NBTTagCompound());
			attachmentTags.setTag("scope", new NBTTagCompound());
			attachmentTags.setTag("stock", new NBTTagCompound());
			attachmentTags.setTag("grip", new NBTTagCompound());

			gun.stackTagCompound.setTag("attachments", attachmentTags);
		}
	}

	/** Get the melee damage of a specific gun, taking into account attachments */
	public float getMeleeDamage(ItemStack stack)
	{
		float stackMeleeDamage = meleeDamage;
		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			stackMeleeDamage = Math.round(meleeDamage * attachment.meleeDamageMultiplier);
		}
		return stackMeleeDamage;
	}

	/** Get the damage of a specific gun, taking into account attachments */
	public float getDamage(ItemStack stack)
	{
		float stackDamage = damage;
		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			stackDamage *= attachment.damageMultiplier;
		}
		return stackDamage;
	}

	/** Get the bullet spread of a specific gun, taking into account attachments */
	public float getSpread(ItemStack stack)
	{
		float stackSpread = bulletSpread;
		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			stackSpread *= attachment.spreadMultiplier;
		}
		return stackSpread;
	}

	/** Get the recoil of a specific gun, taking into account attachments */
	public float getRecoilPitch(ItemStack stack)
	{
		float stackRecoil = this.recoilPitch + (rand.nextFloat() * this.rndRecoilPitchRange);
		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			stackRecoil *= attachment.recoilMultiplier;
		}
		return stackRecoil;
	}
	
	public float getRecoilYaw(ItemStack stack)
	{
		float stackRecoilYaw = this.recoilYaw + ((rand.nextFloat()-0.5F) * this.rndRecoilYawRange);
		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			stackRecoilYaw *= attachment.recoilMultiplier;
		}
		return stackRecoilYaw;
	}
	
	/** Get the bipod modifier for recoil reduction from crouching */
	public float getDecreaseRecoilPitch(ItemStack stack)
	{
		float stackDecreasePitch = this.decreaseRecoilPitch;
		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			stackDecreasePitch *= attachment.bipodMultiplier;
		}
		return stackDecreasePitch;
	}

	/** Get the bullet speed of a specific gun, taking into account attachments */
	public float getBulletSpeed(ItemStack stack)
	{
		float stackBulletSpeed = bulletSpeed;
		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			stackBulletSpeed *= attachment.bulletSpeedMultiplier;
		}
		return stackBulletSpeed;
	}

	/** Get the reload time of a specific gun, taking into account attachments */
	public float getReloadTime(ItemStack stack)
	{
		float stackReloadTime = reloadTime;
		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			stackReloadTime *= attachment.reloadTimeMultiplier;
		}
		return stackReloadTime;
	}

	/** Get the muzzle particle settings, taking into account attachments.  Remove the argument for no attachments (DEPRECATED) */
	public String getMuzzleSettings(ItemStack stack)
	{
		/*String stackParticle = muzzleParticleType;
		if (!useMuzzleParticle)
		stackParticle = "false";
		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			if (attachment.overrideMuzzleSettings)
			{
				if (attachment.useMuzzleParticle)
				stackParticle = attachment.muzzleParticleType;
				else
				stackParticle = "false";
			}
		}
		return stackParticle;*/
		return "false";
	}
	
	/** Get the muzzle particle settings, WITHOUT taking into account attachments.  Add an ItemStack argument for attachments (DEPRECATED) */
	public String getMuzzleSettings()
	{
		/*String stackParticle = muzzleParticleType;
		if (!useMuzzleParticle)
		stackParticle = "false";
		return stackParticle;*/
		return "false";
	}

	public void setFireMode(ItemStack stack, int fireMode)
	{
		if(!stack.hasTagCompound())
		{
			stack.setTagCompound(new NBTTagCompound());
		}
		
		if(fireMode < EnumFireMode.values().length)
		{
			stack.getTagCompound().setByte("GunMode", (byte)fireMode);
		}
		else
		{
			stack.getTagCompound().setByte("GunMode", (byte)mode.ordinal());
		}
	}

	/** Get the firing mode of a specific gun, taking into account attachments */
	public EnumFireMode getFireMode(ItemStack stack)
	{
		if(stack.hasTagCompound() && stack.getTagCompound().hasKey("GunMode"))
		{
			int gm = stack.getTagCompound().getByte("GunMode");
			if(gm < EnumFireMode.values().length)
			{
				for(int i=0; i<submode.length; i++)
				{
					if(gm == submode[i].ordinal())
					{
						return EnumFireMode.values()[gm];
					}
				}
			}
		}

		setFireMode(stack, mode.ordinal());
		
		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			if(attachment.modeOverride != null)
				return attachment.modeOverride;
		}
		return mode;
	}

	/** Static String to GunType method */
	public static GunType getGun(String s)
	{
		return guns.get(s);
	}

	/** Method for getting paintjob details from the current paintjob's item icon */
	public Paintjob getPaintjob(String s)
	{
		for(Paintjob paintjob : paintjobs)
		{
			if(paintjob.iconName.equals(s))
				return paintjob;
		}
		return defaultPaintjob;
	}
}