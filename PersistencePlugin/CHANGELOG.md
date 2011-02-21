# CHANGELOG

# 0.49

 - Allow access to Persistence and Security (where hasPermission now lives) via PluginUtilities

# 0.48

 - Bit o' refactoring for a little splitup that may just happen :)

# 0.47

 - Fix a list saving bug with lists of objects- need the concrete id when deleting missing items.

# 0.46

 - Rename IUser to IProfile, add an additional function signature to support Permissions
   backwards compatibility.
 - Yay! Constructor!

# 0.45

 - Move BoundingBox to Gameplay library
 - Use LocationData in global DAO's as appropriate
 - First round auto migration on PlayerData, to handle above case

# 0.44

 - Just going to call this next release 0.50 (however, I guess I'm not there yet!)
 - Use the sqlite jdbc jar provided by the bukkit Maven repo.
 - Rip out permissions integration so I can actually release all the below fixes.. jumped the gun,
   on that one, it seems :(

# 0.43 (integration)

 - Some migration work
 - More fixing message/command DAOs.. yeesh
 - Pull permissions integration branch back into master
 - First round bukkit.permissions integration
 - Supports some new commands : grant, deny, group
 - Implement proper config.yml for internal Persistence commands
 - Fix some bad log statements...
 - Fix some pretty stupid "in a hurry" bugs.
 - Fix List<Object> loading, which was broken.
 - Just a little infinite recursion (again).. NBD :\
 - Merge in permission-branch changes.... by hand... bleh. Really should have been more diligent about that.
 - bukkit.permissions is working!!! Still have some data duplicate errors going on...
 - Fix autogen'd ints
 - Break out Groups into its own plugin
 - Add better double/long support in ConvertType, move that to DataType
 
# 0.42

 - De-contain Message and CommandData from PluginData. This was a stupid bug, but I think it 
   may have exposed something Deep and Scary- how was the PK being violated so badly?
   
   I need to investigate this more by reproducing this client error in a test case.

 - Fix parameter check in /persist RESET
 - Fix /su toggling off on each player update() call

# 0.41

 - Fix the "su" command, which I had broken if you weren't using Permissions. It was kind of a circular issue,
   ops need to be able to use "su", or else they can't use any admin commands by default (including "su"). Oops.
   I needed to make a whole new permission type to cover this case- OPS_DEFAULT. This specifies that an op may use
   this command, but unlike OPS_ONLY, Permissions may override the player's op status. 

# 0.40

 - Return true from a command handler on Permission fail, to avoid having Bukkit print auto-help.
 - Merge in latest LocationData class (not really in this commit, but accepted the pull round this time)
 - Allow overriding an op's ability to use SU with a pnode- just like sudoers!
 - Temporarily turn off all the new debug logging (via a flag in Persistence) until I can get a properties file in.

# 0.39

 - Adding in amkeyte's LocationData class- THANKS!
   Had to just throw is in real quick- I might add to it later, and Persistence
   code needs a global reformat anyway, so I'm not too worried about it for now- I trust ya ;)

# 0.38

 - More object-as-id fixes, this time for id-objects with primitive id types.
   You can't use reflection (easily) to cast down an Integer to an int (for instance- I mean this in a blind way)
   so I now make use of the DataField data conversion function I'd already been using to deal with type conversion
   to/from the database- I use this when I need to look up by conrete id- because by the time it gets to me, any 
   primitive types will be wrapped in their object equivalent, and will fail the isAssignable from when looking
   up that object by concrete id.
   
   Whew!

# 0.37

 - Move BlockRequestListener to Persistence
 - Don't defer object binding if the object's id is null anyway

# 0.36

 - Another nasty object-as-id fix: 
   Objects with objects as ids need to use the value from the data store to cache their "concrete"
   id at load time, before deferred binding.
   
   This is because their true id is null at the time- it will be deferred-bound once the other
   object's persisted class has had a chance ot load it.
   
   So, caching the object by id results in a big mess- I think this should be cleaned up now.

# 0.35

 - Some BoundingBox additions and fixes.

# 0.34

 - Some improvements to /phelp.
 - Fix a bug in the auto-command-child-map creation (I think this was only screwing up phelp)
 - Allow commands to have no usage information (just pass null for now, no new function signatures)

# 0.33

 - _more_ object-as-id fixes, and making "/persist list" AWESOME.

# 0.32

 - Fix/improve /persist list. (minor release)
 TODO: I notice that player.orientation is not getting saved correctly- I need to fix this, its a sign of something deeper.

# 0.31
 
 - Yes, MORE object-as-id fixes. I think it's starting to get cleaner now, but could still use some looking-over and refactoring, probably.

# 0.30

 - Fix server console commands, which I broke with the Permissions integration.
 - Another fix to object->(object->id(object)) kinds of setups. Yikes! Glad I'm doing such crazy stuff with nethergate...

# 0.29

 - Fix using an object as an id and then embedding the containg object inside another object. I fixed this with recursion, so
   it should handle any depth of craziness like this now.

# 0.28

 - Don't preload a World when calling getWorld, just create the shadow data. 

# 0.27

 - Don't persist command.children, it can be built at runtime from the parents.
 - Permissions integration (Permissions.hasPermission, and CommandData.permissionNode)
 - Remove NetherGate-specific stuff from WorldData- my bad! I promise not to abuse my dual dev/client status in the future :)

# 0.26
 
  - Prefix contained value names
  - Persist Player location and position
  - Really fix persisting longs and floats
  - Add built-in WorldData type. Little bit o' portal-specific stuff in here, I will admit.
  - Add ability to create and track server Worlds
  - Need to clone fields when cloning a class, or ownership trees get screwed up (BlockVector, again!!)
  - Correctly bind messages to their owning plugins
  - Fix concurrency issues when trying to access commands (e.g. auto help) from within a command handler
  - Allowed contained enums
  - Support using a class an an id
  - Fixed containing an object with a read-only id
  - BlockVector!
 
# 0.25

 - LOTS of refactoring. I honestly wouldn't consider this highly stable at the moment,
   though I have done a good bit of testing.
   - API for programmatically creating persisted classes
   - Built-in support for persistence of a BlockVector
   - Lots of other thing my brain is too melty too remember right now.

# 0.24

 - Implement /phelp, a universal help system for Persistence plugins
 - Some general bug fixes
 - Add some data to the player class, save last location and orientation

# 0.23

 - Vastly improve Plugin command interface:
 - Support sub-commands
 - Auto-dispatch to individual command handlers
 - Auto-typing with flexible data-driven "sender" system
 - Automatically generated, detailed, explorable help

# 0.22

 - Change contained classes so sub-field names get prefixed.
 - Support containers of containers of containers...
 - Allow for pure contained classes, which don't need an id.

# 0.21

 - Finally support contained lists!
 - Also, fix the server console interface.
 - Extend the Command and Plugin DAOs

# 0.20

 - Massive, massive refactor. No real functionality change, but:
 - All functionality related to creating and populatd classed moved to core.
 - SqlStore only deals with raw database-type actions now.
 - Very, very close to supporting contained references and lists.

# 0.19

 - Fully support Bukkit command interface, you can now use persistence commands from the server console!

# 0.18

 - Integrate Classes plugin directly into Persistence
 - Re organize namespaces for clarity.

# 0.17

 - Update to use the command standard. The persist command is now defined in the YML file.
 - Start work on basic CommandData framework. I'd like to support sub-commands, such as all the sub-commands of /persist,
   and also support auto-generating the help based on this data.
 - Get about halfway there on contained object support.

# 0.16

 - Implement Messaging utility, for consolidated storage of messages and commands.
 - Remove complex jar loading mechanism, use manifest classpath instead. Thanks A LOT, tkelly!
 - Implement contained objects.

# 0.15

 - Escape all table and column names, to allow for entity names with spaces, and also so I can use "group".
 - Support lists of objects.

# 0.14

 - Implement persistence of Lists of primitive objects. Lists of DAO references is not far behind.

# 0.13

 - Implement reload console command, add save command and RESET command.
 - Add list command for data querying.

# 0.12

- Support Lists- not fully functional yet.
- Add basic console UI.

## 0.11

- Support references to other DAO's, including circular class references, for storing structures such as trees.

## 0.10

- First release
- Auto-load JDBC drivers
- Implement support for simple object structure persistence and caching.
