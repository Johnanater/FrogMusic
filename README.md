This mod allows you to add custom records using simple json files!

Requirements:
-------------
* ModLoader
* ModLoaderMP

Compatibility:
--------------
Any mod that overrides EntityCreeper will not be compatibile!


Client:
-------

Simply put the zip in your mods folder, and put the "mods" folder
in your Minecraft root directory.


Server:
-------

Simply add the zip contents into your Minecraft server .jar, and put
the "mods" folder in the server root directory.


Bukkit:
-------

Simply add the zip contents into your Minecraft server .jar, or 
use "FixModsFolder" and put the zip in your mods folder, then put
the "mods" folder in your server root directory.

FixModsFolder can be downloaded here:
https://discord.com/channels/397834523028488203/397839737605586945/609410381881802752
https://www.mediafire.com/file/wii9nkxtf16qivg/FixModsFolder_v1.0.2.zip/file

CraftBukkit has a bug where Creepers will not drop records if shot by a
skeleton, this mod fixes that.


Adding New Records:
-------------------

Records are stored in the "mods/frogmusic/" directory.
To make your own custom record, make a .json file in the
following format:
"mods/frogmusic/{YOUR SONG}/music.json"

Minecraft automatically shifts item ids by 256, meaning that 256 will be added
to the id you put in the config.

Examples:

This record uses a local .ogg file.
```
{
	"type": "record",
	"itemName": "Pigstep",
	"itemId": "2011",
	"soundFile": "pigstep.ogg",
	"soundPath": "/",
	"texturePath": "/music_disc_pigstep.png"
}
```

This record uses the "Blocks" file from resources/streaming.
```
{
	"type": "record",
	"itemName": "Blocks",
	"itemId": "2009",
	"soundFile": "blocks.mus",
	"soundPath": "/../../../resources/streaming/",
	"texturePath": "/music_disc_blocks.png"
}

```


Disclamers:
-----------

Argo, the JSON library bundled with the Minecraft client, is automatically
downloaded on the server version and stored in the "lib" folder.

I do not own any of the bundled records contained in this release.
