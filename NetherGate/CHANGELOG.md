# CHANGELOG

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