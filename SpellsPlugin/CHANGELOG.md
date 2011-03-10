# CHANGELOG

## 0.991

 - Yes, I'm doing that with the version numbers until 1.0 :P
 - Update config.yml to support internal permissions

## 0.99

 - Bring back tunnel, torches disabled.
 - Update permissions support for Persistence 0.55, drop Groups
 - Simplify the spells list 

## 0.98

 - But blast paramaters back in :)
 - Invincible!

## 0.97

 - Update for portal, NetherGate 0.45
 - More Gameplay integration - use BlockList from Gameplay
 - Auto-expanding undo (cave-in/breakage prevention) temporarily disabled
 - Finally fix findPlaceToStand so blink doesn't take you to 255 y!

## 0.96

 - Update for NetherGate 0.44
 - Add first-rev fire and lava spells - DANGEROUS!
 - Fix some torch bugs
 - Frost now puts out fires

## 0.95

 - Refactor to work with Persistence 0.49

## 0.94

 - Torch now casts "night" when pointing down. Don't know why I didn't do that earlier... (yes I do)

## 0.93

 - Mmmm... default constructor good!
 - Also, Mavenize!

## 0.92

 - Fix "shell", add "box"
 - Remove non-sticky materials like doors, torches, etc from the buildable list
 - Move material-giving code to common library

## 0.91

 - Make spell permission node names consistent
 - Colored wool! Yes! Thanks, SqualSeeD31
 - Add "with" command for construct, "sandblast" variant - thanks, anon!

## 0.90

 - Fix a really nasty material selection bug that allowed selection of items!

## 0.89

 - Merge "with" variant code to base fill- though I had done this already!
 - Separate "peek" and "window".

## 0.88

 - Keep trying to undo blocks in an unloaded chunk
 - Modify absorb to give you blocks starting at the right of your inventory
 - Always use the right-most inventory slot for construction, unless you have no building materials

## 0.87

 - Move BlockRequestListener to Persistence
 - Modify torch to turn netherrak to glowstone

## 0.86

 - Use NetherGate to create a portal-less portal for the portal spell!

## 0.85

 - First-round Persistence integration.

## 0.84

 - Add "world" variant of "peek" (!)

## 0.83

 - Add Netherrak and Slowsand to the destructible materials list.

## 0.82

 - Add "default" permission group.

## 0.81

 - Fix undo system - I was checking for chunk load in a bad way!
 - Add "portal" and "phase" spell, NetherGateintegration.

## 0.80

 - Check to see if a chunk is loaded before undoing a block, fail undo
 - Temporarily remove player death - the auto-recall drop is not multi-world compliant.
 - Fix a problem if you specify a player before its group

## 0.79

 - Add "peek" spell

## 0.78

 - Absorb and mine now handle variants properly (mine can mine LL now)- thanks to Firestar for sharing the code that clued me in!
 - Absorb and manifest now give you the material directly, instead of dropping it at your feet.

## 0.77
 
 - API release, many protected Spell methods made public. Some javadocs added.

## 0.76

 - Updated to work with Bukkit#210

## 0.75

 - Fix the familiar spell. I needed mc-dev, now!
 - Fix spell variants when used on the console.
 - Tree variants now work, by the way! So does auto-recall-on-death.

## 0.74

 - Fix a really heinous bug that was keeping all spells from saving/loading their properties.
 - Update to work with newest CraftBukkit changes.

## 0.73

 - Recall automatically drops a marker on death. (Requires a Craftbukkit update!)
 - Moved code out of plugin handler. LOTS of refactoring to make API cleaner.
 - Make ops automatically spell admins.

## 0.72

 - Fix "player tried command /cast" messages. Now only shown for unauthorized users.

## 0.71

 - Change around the way default material selection works will a few spells.
 - Make rewind and transmute targetable by default. Remove revert.
 - Remove wip spells. I can add them back as I test them- I'm tired of people reporting bugs on them...
 - Fix an NPE when trying to access permissions for a player who had none!

## 0.70

 - Disallow air selection by default- right now, only transmute and fillwith allow it. I'm not sure how intuitive this is!

## 0.69

 - Add revert, a targeted variant of rewind.
 - Add blob and superblob, variants of construct.
 - Add manifest, for getting a material by name.

## 0.68

 - Don't allow material selection for non-buildable materials (such as items!)

## 0.67

 - Fixed command-line use with spell variants!

## 0.66

 - Alter now knows what data values are valid for alterable materials.

## 0.65
 
 - More work on the undo system, make the cave-in-proof thing optional.
 - Add paint and shell spells.
 - Fix variants with multiple parameters.
 - Re-arrange spell materials, again- now that I can use right-clickable items again.

## 0.64

 - Improved the undo system to automatically add sticky blocks to the undo list, as well
  as auto-fill in sand and gravel that would fall with dirt.

## 0.63

- Added "selected material" system, which is more elegant than the "material selection" system :)
- Transmute now fills with the selected material in one click.
- Add "fillwith" variant to fill with the selected material.

## 0.62

- Added "disintegrate" spell.

## 0.61

- Added "map" spell, re-renders a dynmap tile.

## 0.60

- Added "recall" spell
- Removed the "upload" command from UndoableBlock, and all its uses. It seems it was unnecessary!

## 0.59

- Remove time, ascend and descend- make them variants of torch and blink instead.
- Add "night" spell, another torch variant.
- Make blink smart about putting you up on ledges.

## 0.58

- Fix the "allow-command-use" flag, which was backwards!

## 0.57

- Added a transformation list to "mine", so it can convert diamond ore to diamonds and coal ore to coal.

## 0.56

- Re-arrange the materials used for certain spells

## 0.55

- Some bug fixes, fix case-insenstive permissions
- Add "giants" to "familiar", add some spell variants: "monster" and "superblast"
- More "blink" awesome

## 0.50

- Support release, first release required by Wand
- Spells can now register more than one variant
- Each spell variant is associated with a unique material

## 0.41

- Update "blink", add awesomeness

## 0.40

- Fix "familiar"
- Add "transmute"

## 0.38

- Lots of bug fixes
- Added "construct"

## 0.36

- Added "familiar"

## 0.33

- Added "gills" spell, made lots of spells work well underwater

## 0.32

- Made "alter" recurse

## 0.31

- Added "frost"

## 0.30

- Added "arrow" and "tree"

## 0.29

- Fix multiplayer use of "fill" 
- Added "mine"
- Added "quiet" and "silent" plugin options

## 0.28

- Added "blast"

## 0.27

- Added "rewind" and undo system

## 0.26

- Adopted player animation hook, requires Bukkit update.
- Added "cushion", "tunnel", "pillar down"
- "heal" now working
- Updated "blink" to "ascend" or "descend" automatically
- Updated "torch" to cast "time day" when pointed at the sky
- Implemented material choosing mechanic
- Added lots of configuration properties

## 0.2?

- Permissions system implemented

## 0.17

- Added "fill" and "time"

## 0.16

- Got "fireball" working
- Added "absorb"
- Renamed "extend" to "bridge"
- Shelving "tower"

## 0.10

- First release