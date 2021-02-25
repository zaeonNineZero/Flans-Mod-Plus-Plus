# Flans-Mod-Plus-Plus
Flan's Mod ++ (FM++) is a fork of Flan's Mod Plus for 1.7.10, with new features- most of which are easily configurable.

Original created for use in a modpack, the goal of FM++ is to provide an improved user-end experience, with easy-to-use config options to customize your server or modpack's experience.  All new configurable options can be set up in the gun/vehicle/item .txt config files themselves- no need to edit and recompile .java files over and over again just to move an attachment.


Feature Summary
------------

Features for Guns, Attachments, Ammo, and Grenades:
* Better controls for knockback from bullets.
* Txt file configuration for gun animations.
* Updated HUD and tooltips for guns and bullets.
* Functionality for "Mining Laser" weapons.
* Bipod attachment functionality.


New Vehicle Mechanics.
* More accurate bullet hit detection for all driveables. (Code by GoldSloth, ported from FMU)

* Passive vehicle deceleration from friction, and cruise control for maintaining throttle.
* Improved physics for boats and other watercraft.
* Highly configurable driving model, including traction, and vehicle acceleration/deceleration.
* Configurable drifting and boosting; braking and turning starts a drift, and boosting temporarily makes a vehicle go faster.
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
* Almost all item types can now have description tags ingame.


Flan's Mod ++ compared to Flan's Mod Ultimate: Stability Edition
-------------------------------------------------------------------------
Flan's Mod ++ was built off a version of FM+ prior to the Ultimate update.  As such, it is closer to Flan's Mod 4.10 than it is to FMU:SE.  Some features present in Ultimate are also in FM++, but are implemented in a different way.

Compatibility with Flan's Mod 4.10
----------------------------------
FM++ was designed with compatability in mind.  All models and config txt files from normal Flan's packs should be compatable with FM++.  Servers can switch from normal Flan's Mod to FM++ with few, if any, changes required.
