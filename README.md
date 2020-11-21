# Flans-Mod-Plus-Plus
Flan's Mod ++ (FM++) is a fork of Flan's Mod Plus for 1.7.10, with new features- most of which are easily configurable.

Original created for use in a modpack, the goal of FM++ is to provide an improved user-end experience, with easy-to-use config options to customize your server or modpack's experience.  All new configurable options can be set up in the gun/vehicle/item .txt config files themselves- no need to edit and recompile .java files over and over again just to move an attachment.


Feature List
------------

Many New Features for Guns, Attachments, Ammo, and Grenades:

* KNOCKBACK FIX!  By default, knockback from being hit by bullets will be reduced, even without custom armor from Flan's content packs.
* Knockback can be configured in both the gun and bullet .txt files.

* Configurable translation and up/down rotation of gun models for aligning the sights of guns; separate values for when a scope is attached and when no scope attachment is present.
* Configurable translation of attachment models relative to the gun, for making quick adjustments to attachment locations without having to recompile a model.

* Configurable model translate/rotation recoil when shooting, independent of player camera recoil.
* Configurable translation/rotation during pump animations.
* Configurable rotation of the pump model, for making rotating bolt-action animations.
* Smooth sprint animations, with multiple presets to pick from.
* Guns cannot be fired while sprinting by default- add "CanShootWhileSprinting True" to enable sprinting and shooting per gun. 
* Configurable accuracy changes for firing while aiming vs. not aiming.
* Added a delay before you can reload a gun by right/left clicking; this can be bypassed by using R to reload, or by adding the ReloadDelay line to a gun file.
* Added the NamedPaintjob line, which has an additional input for adding names to a gun's paintjob.  Use "ShowPaintjobName True" to enable paintjob names in the item's description, and add DefaultPaintjobName to set the name of the default paintjob. (If DefaultPaintjobName is unset, it will not be shown in the description regardless)

* Guns with multiple fire modes will now show their fire mode outside the ammo counter text; this can be forced on for guns with only a single fire mode as well.
* Added per-gun configuration for whether to sure the default crosshair in certain situations, such as aiming vs. not aiming and scope attached vs. no scope attached.
* The ammo-remaining tooltip for guns has been simplified; holding shift will show the full list of loaded ammo items.
* The ammo counter on the HUD now has a secondary display mode for hiding the maximum shots of a loaded ammo item.

* Guns can be configured to rely on bullets for determining spread, projectile count, and velocity values.  Useful for making birdshot shells, slug rounds, or a multi-projectile ammo for an energy/plasma weapon.
* Glass block with a high enough hardness or blast resistant are now invulnerable to being broken by bullets.  Thermal Expansion's Reinforced Glass is truly bullet-proof!
* Mining Lasers! Use "MiningLaserBeam True", then add MiningLaserMaxHardness and MiningLaserMaxResistance to make a bullet behave like a Mining Laser projectile.  If a block is not too hard or blast resistant, it will be mined and dropped by the bullet when hit.
* Ammo items can now properly drop other items, such as parts, when emptied in Survival Mode.  From example, a laser weapon's ammo could drop an empty battery, which can then be recharged.
* Ammo items display how much ammo they have remaining, with config lines to alter how it is displayed.

* Grenades can now have descriptions added to them. Good for letting players know how long a grenade's fuse lasts.
* Grenade and Bullet models can now be rescaled using ModelScale.

* Bipods: Use "Bipod True" and the BipodEffectivenessMultiplier multiplier to set make a grip function as a Bipod.  Bipods only apply their recoil reduction modifier when sneaking.
* BipodEffectivenessMultiplier stacks with RecoilMultiplier, so you can both to make a Grip Pod!


New Vehicle Mechanics.
* When driving ground vehicles, the camera will now lag slightly behind the vehicle's turn rate when steering.
* The Landing Gear key can be used to reset the camera's yaw in ground/water vehicles.
* Fixed vehicle hitboxes for more accurate bullet hit detection. (Code by GoldSloth, ported from FMU)

* Vehicles decelerate automatically when W or S aren't being held.
* Enable Cruise Control using the Mode Switch key to keep your vehicle's throttle steady.
* Vehicles can drift/slide when turning; vehicles with lower Traction will drift more.  Driving at high throttle levels will result in lower traction and more sliding.
* Boat Vehicles have improved physics; they stop moving when out of water, and will not change pitch/roll.
* Vehicle idle sounds (used when Throttle is near or at 0 while the driver's seat is occupied) can be set to continue looping regardless of throttle level, providing a background motor sound.
* Acceleration/Deceleration rates and Traction can be configured in the vehicle config .txt files.
* Vehicle sound threshold config lines for controlling the throttle level at which driving sounds start playing.

* Engines can now be configured to only take certain types of fuel.  If the Fuel Type of the fuel item does not match the Fuel Type of the engine, no fuel will be added to the engine.
* Fuel items can now leave behind other items when emptied.  For example, a Gasoline Can can turn into an Empty Fuel Can once all of its fuel is consumed.


Improved Plane Mechanics.
* Planes fly faster in a dive, and no longer come to a hard stop in mid-air when out of fuel.
* The MaxAirspeed line allows setting a hard-cap for maximum flying speed, both in a dive and when flying at high throttle values.
* Emitters can be toggled on/off depending on VTOL flight mode.


Misc. Changes.
* New/altered HUD text lines for vehicles and planes, showing throttle, core health, fuel level, heading, and (for planes) pitch and yaw.
* Added a HUD text line for VTOL planes showing the current flight mode.
* Parts can now have items descriptions ingame.



Flan's Mod ++ compared to Flan's Mod Ultimate: Stability Edition
-------------------------------------------------------------------------
Flan's Mod ++ was built off a version of FM+ prior to the Ultimate update.  As such, it is closer to Flan's Mod 4.10 than it is to FMU:SE.  Some features present in Ultimate are also in FM++, but are implemented in a different way.

Compatibility with Flan's Mod 4.10
----------------------------------
FM++ was designed with compatability in mind.  All models and config txt files from normal Flan's packs should be compatable with FM++.  Servers can switch from normal Flan's Mod to FM++ with few, if any, changes required.
