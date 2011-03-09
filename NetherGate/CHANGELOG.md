# CHANGELOG

# 0.53

 - Update plugin.yml with all NetherGate permissions, to support internal permissions
 - Re-arrange all the console commands
 - Fix kit

# 0.52

 - Update permissions support for Persistence 0.55, drop Groups

# 0.51

- Forcibly disable fast-travel on startup, for now.

# 0.50

 - Make you temporarily invincible during TP
 - Maaaybe fix the "moved wrongly" bug? Or, break portalling on machines with lots of RAM- not sure which yet.

# 0.49

 - Don't platform air blocks!
 - Change the pnodes up a bit- still testing permissions.

# 0.48

 - Preload worlds
 - Add "spawn" command, "clean", fix setspawn for 1.3
 - Make all commands available server-side. (Some commands don't really work/apply, such as "spawn")
 - Fix for platforms always being created- should only be on air, water or lava

# 0.47

 - Turn auto-portalling back off for now :P
 - Some fixes to the "find place to stand" algorithm- including the proper nether/normal switch!
 - Remove CB dependency

# 0.46

 - More auto-portal work
 - Fix an NPE when a user tries to TP but has no permission to do so

# 0.45

 - Portal tracking! First round. Creating portals on the other side now.
 - Add compass command.
 - Enable fast-travel by default for new world/nether worlds.
 - Commented-out PortalArea.create, just in case

# 0.44

 - Start playing with portal tracking, finally!
 - Remove nuke - See CrowdControl- sorry! :P
 - Make "kit" more Spells-friendly
 - 1.3 update, temporarily disable "setspawn" command

# 0.43

 - Add "list worlds" command.

# 0.42

 - Updated to work with latest Persistence refactor

# 0.41

 - Integrate with Gameplay library
 - Default constructor!

# 0.40

 - Version jump for Persistence/permissions integration.
 - Maven re-org.

# 0.32

 - Add "center" command for re-centering a world. Needs testing.

# 0.31

 - Disable the "return you on death" code, too much trouble.

# 0.30

 - Work on the find place to stand code. Looking good!
 - Add "all" and entity type parameters to nuke
 - Allow "0" to the "scale" command to allow disabling scaling again once you've turned it on

# 0.29

 - Maven-ize. Just never got around to that.
 - Nasty BlockPhysics bugfix- I think this is why portals are so much more resilient than I expected them to be!
 - No need to persist all that player teleporting state data. Not sure why I thought I needed to do that.
 - Get rid of any fire in the target area
 - Add ore blocks to destructibles- better to lose some ore than die :)

# 0.28

 - Turn off auto-gate creation for now. Rollback to 0.27 if you want it.

# 0.27

 - Not a "real" release, I just fixed the Persistence bug that was keeping home locations from saving.

# 0.26

 - More location mapping work
 - Move BlockRequestListener to Persistence

# 0.25

 - Add home and sethome commands

# 0.24

 - Add setspawn command.

# 0.23

 - Add "/nether nuke", to kill all ghasts!
 - Set scales to 0 by default until I've got that sorted out.
 - Add permission nodes:
   NetherGate.portal.create
   NetherGate.portal.create.platform
   NetherGate.portal.create.portal
 - I also fixed a really nasty bug in Persistence that was keeping worlds from auto-binding.
 - Some work on the portal creation- nearly there!
 
# 0.22

 - Slowly making some progress on a lot of weird little bugs...

# 0.21

 -  .... peek ..... world .... (!!!!!!)

# 0.20

 - Fix some bugs in the "scale" command.

# 0.19

 - Fix NPE when specifying an unknown world to target or delete
 - Add "scale" command

# 0.18

 - Make sure there's air for the player.
 - Start creating portal on other side.
 - Simplify location mapping logic so it's at least symmetrical.
 - Add "target" and "delete" commands.

# 0.17
 
 - Fix a very bad bug in findPlaceToStand
 - First version that really feels "all worky".

# 0.16 

 - OOPS... put back the y-value of the player, that got lost in a refactor. Very embarassing. (Thanks, theLephty!)

# 0.15

 - Portal blocks are okay to stand in! Duh.
 - Swap scale ratio
 - Build a Better Platform
 - Some fixes to the "find a place to stand" code
 - Distance mapping!

# 0.14

 - Commands should be admin-only.
 - Permissions integration (via Persistence->Permissions integration)
 - Don't auto-create a world when using "/go", unless there is only one world
 - Some refactoring to move NetherGate-specific data from Persistence
 - Adding some data to objects, such as a home world for each player. Not used yet, though.

# 0.13

 - Spells integration!

# 0.12

- Automatically generate a place to stand when placing the player on lava or water.

# 0.11

- Improvements to the data structure.
- Started on event handlers.
- Started on data storage, checking for existing Nether areas
- Added "kit" command
- Multi-World!!!!

# 0.10

- First release
- Basic Nether area created.
- Data structure created.