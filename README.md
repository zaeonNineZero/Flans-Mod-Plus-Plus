# Flans-Mod-Plus-Plus
Flan's Mod ++ is a fork of Flan's Mod Plus for 1.7.10, with new features- most of which are easily configurable.


Feature List
------------
Many New Features for Guns, Attachments, Ammo, and Grenades:

* KNOCKBACK FIX!  By default, knockback from being hit by bullets will be reduced, even without custom armor from Flan's content packs.
* Knockback can be configured in both the gun and bullet .txt files.

* Configurable translation and up/down rotation of gun models for aligning the sights of guns; separate values for when a scope is attached and when no scope attachment is present.
* Configurable translation of attachment models relative to the gun, for making quick adjustments to attachment locations without having to recompile a model.s

* Configurable model translate/rotation recoil when shooting, independant of player camera recoil.
* Configurable translation/rotation during pump animations.
* Configurable rotation of the pump model, for making rotating bolt-action animations.
* Smooth sprint animations, with multiple presets to pick from.
* Guns cannot be fired while sprinting by default- add "CanShootWhileSprinting True" to enable sprinting and shooting per gun. 
* Configurable accuracy changes for firing while aiming vs. not aiming.
* Added a delay before you can reload a gun by right/left clicking; this can be bypassed by using R to reload, or by adding the ReloadDelay line to a gun file.

* Guns can be configured to rely on bullets for spread, projectile count, and velocity values.  Useful for making birdshot shells, slug rounds, or a multi-projectile ammo for an energy/plasma weapon.
* Glass block with a high enough hardness or blast resistant are now invulnerable to being broken by bullets.  Thermal Expansion Reinforce Glass is truly bullet-proof!
* Mining Lasers! Use "MiningLaserBeam True", then add MiningLaserMaxHardness and MiningLaserMaxResistance to make a bullet behave like a Mining Laser projectile.  If a block is not too hard or blast resistant, it will be mined and dropped by the bullet when hit.
* Ammo items can now drop other items, such as parts, when emptied in Survival Mode.  You laser weapons can now drop empty batteries that can be refueled!

* Grenades can now have descriptions added to them. Easily let players know that your landmine will prime after 5 seconds!
* Grenade and Bullet models can now be rescaled using ModelScale.

* Bipods: Use "Bipod True" and the BipodEffectivenessMultiplier multiplier to set make a grip function as a Bipod.  Bipods only apply their recoil reduction modifier when sneaking.
* BipodEffectivenessMultiplier stacks with RecoilMultiplier, so you can both to make a Grip Pod!


New Vehicle Mechanics.
* Vehicles decelerate automatically when W or S aren't being held.
* Enable Cruise Control using the Mode Switch key to keep your vehicle's throttle steady.
* Vehicles can drift/slide when turning; vehicles with lower Traction will drift more.
* Vehicle idle sounds (used when Throttle is near or at 0 while the driver's seat is occupied) can be set to continue looping regardless of throttle level, providiing a background motor sound.
* Acceleration/Deceleration rates and Traction can be configured in the vehicle config .txt files.

* Engines can now be configured to only take certain types of fuel.  If the Fuel Type of the fuel item does not match the Fuel Type of the engine, no fuel will be added to the engine.
* Fuel items can now leave behind other items when emptied.  For example, a Gasoline Can can turn into an Empty Fuel Can once all of its fuel is consumed.


Improved Plane Mechanics.
* Planes fly faster in a dive, and now longer come to a hard stop in mid-air when out of fuel.
* The MaxAirspeed line allows setting a hard-cap for maximum flying speed, both in a dive and when flying at high throttle values.
* Emitters can be toggled on/off depending on VTOL flight mode. (NOTE: Currently hover emitters are active in flight mode due to a bug)


Misc. Changes.
* Parts can now have descriptions entered for them.



Flan's Mod ++ compared to Flan's Mod Ultimate: Stability Edition
-------------------------------------------------------------------------
Flan's Mod ++ was built off a version of FM+ prior to the Ultimate update.  As such, it is closer to Flan's Mod 4.10 than it is to FMU:SE.  Some features present in Ultimate are also in FM++, but are implemented in a different way.

Compatibility with Flan's Mod 4.10
----------------------------------
FM++ was designed with compatability in mind.  All models and config txt files from normal Flan's packs should be compatable with FM++.  Servers can switch from normal Flan's Mod to FM++ with few, if any, changes required.
