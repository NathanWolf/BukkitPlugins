# CHANGELOG

# 0.16

 - Implement Messaging utility, for consolidated storage of messages and commands.
 - Remove complex jar loading mechanism, use manifest classpath instead. Thanks A LOT, tkelly!

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
