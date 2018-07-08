# Area Protection
A Rising World Java plug-in to create and manage player-owned areas.
## Features
- Compatible with the old Area Protection 3 LUA script: existing areas are kept as it can read the old data base directly.
- 30 different permissions can be turned on / off independently.
- Each area can have its own general permissions with any cobination of those 30 permissions.
- Each area can also assign specific permissions to each server group (overriding the area general permissions).
- Each player can have his own specific permissions for each area (overriding the group permissions and the area general permissions).
- Old LUA script `Groups` can be reused (once renamed) with no modification, or edited as preferred, as _presets_, i.e. sets of player (or group) permissions ready to use "with one click" but configurable if needed.
- Selected non-admin players can be appointed by admins as "area managers", with full authority on area management.
- Fully GUI operated.
____________________

## Commands

There is one main chat command: `/ap` (configurable in the plug-in settings.properties) which opens the main plug-in menu with access to all functions and commands.

For backward compatibility with existing habits, the chat commands `\showareas` and `\hideareas` are also recognised, but their function is also accessible throught the main menu.

## Installation

Extract the files in the ZIP placing the whole `ap` folder into the plugins folder of RW (if the plugins folder does not exist, just create one). The resulting hierarchy shall be:

    ── RisingWorld
        ├── plugins
        │    ├── ap
        │    │    ├── locale/
        │    │    ├── presets/
        │    │    ├── resources/
        │    │    ├── ap.jar
        │    │    ├── AreaProtection_Manual_jpg.pdf
        │    │    ├── COPYING
        │    │    └── settings.properties

For further operations, **please read the manual**!

## Old script data base import

To import the existing areas of the old LUA area protection script, **please read the manual**!

## License

Released under the GNU General Public License ver. 3 (or later when available): see `COPYING` file for details.
