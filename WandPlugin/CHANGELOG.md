# CHANGELOG

# 0.65

 - Bring material cycling back! Requires CB#478

# 0.64

 - 1.3 update temporarily broke "isSneaking", disable material cycling for now

## 0.63

 - HOORAY! Default constructors!
 - Mavenize
 - Remove CB dependency, now that the inventory seems to update properly

## 0.62

 - Add properties file option for turning off the item help

## 0.61

 - Implement material cycling system- crouch and right-click!

## 0.60

 - Support release for Spells refactor. Wand refactor coming soon to a wand near you!

## 0.59

 - Transition to Bukkit Command interface
 - Give items directly to player instead of dropping on the ground

## 0.58

 - Moved code out of plugin handler.
 - Make Ops automatically be Wand admins.

## 0.57

 - Fix "player Blah tried command /wand" messages. Now only shown for unauthorized users.

## 0.56

 - More cleanup/smarts added to help messages. 

## 0.55

 - Only show the wand help message if the player wasn't give a wand with /wand.

## 0.54

 - Cleanup: remove unused classes, orphaned in the new Wand system.
 - Only show right-click help messages for items in your spell inventory, and only when you also have a wand in your inventory.
 
## 0.53

 - Added "deny-users" property to allow co-op with Wandmin.

## 0.50

 - Completely re-worked to avoid console use.

## 0.23

 - Some re-organization to avoid collisions with Spells classes.

## 0.21

 -  Change the current wand/spell if it gets removed.

## 0.18
 
 - Fix a bad bug in permissions / user list parsing.
 - Fix a bad saving/shutdown bug.

## 0.17

 - Added permissions system.
 - Added ability to give out default wands.
 - Added other configuration properties.

## 0.16

 - Fix "/wand wands".
 - Fix the switching wands message.

## 0.11
 - Fix some wand loading/saving bugs.

## 0.10
 - First release.