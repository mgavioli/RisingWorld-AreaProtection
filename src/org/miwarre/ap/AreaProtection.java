/****************************
	A r e a P r o t e c t i o n  -  A Rising World Java plug-in for area permissions.

	AreaProtection.java - The main plug-in class

	Created by : Maurizio M. Gavioli 2017-02-25

(C) Copyright 2018 Maurizio M. Gavioli (a.k.a. Miwarre)
This Area Protection plug-in is licensed under the the terms of the GNU General
Public License as published by the Free Software Foundation, either version 3 of
the License, or (at your option) any later version.

This Area Protection plug-in is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
this plug-in.  If not, see <https://www.gnu.org/licenses/>.
*****************************/

package org.miwarre.ap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Map.Entry;
import net.risingworld.api.Plugin;
import net.risingworld.api.objects.Player;
import net.risingworld.api.utils.Area;
import net.risingworld.api.utils.Vector3f;
import net.risingworld.api.utils.Vector3i;
import net.risingworld.api.utils.Utils.ChunkUtils;

/**
 * The main class of the plug-in
 */
public class AreaProtection extends Plugin
{
	// Constants
	public static final		String		version				= "0.10.0";
	public static final		String		publicName			= "Area Protection";
	public static final		int			AREAMANAGER_AREAID	= -1;
	// Some common return codes
	public static final		int			ERR_SUCCESS			= 0;
	public static final		int			ERR_INVALID_ARG		= -1;
	public static final		int			ERR_DB				= -2;
	public static final		int			ERR_CANNOT_ENTER	= -3;
	public static final		int			ERR_CANNOT_LEAVE	= -4;
	public static final		int			ERR_NOTFOUND		= -5;
	// The bits corresponding to each permission
	public static final		int			PERM_ENTER				= 0x00000001;
	public static final		int			PERM_LEAVE				= 0x00000002;
	public static final		int			PERM_PLACEBLOCKS		= 0x00000004;
	public static final		int			PERM_DESTROYBLOCKS		= 0x00000008;
	public static final		int			PERM_PLACECONSTR		= 0x00000010;
	public static final		int			PERM_REMOVECONSTR		= 0x00000020;
	public static final		int			PERM_DESTROYCONSTR		= 0x00000040;
	public static final		int			PERM_PLACEOBJECTS		= 0x00000080;
	public static final		int			PERM_REMOVEOBJECTS		= 0x00000100;
	public static final		int			PERM_DESTROYOBJECTS		= 0x00000200;
	public static final		int			PERM_PLACETERRAIN		= 0x00000400;
	public static final		int			PERM_DESTROYTERRAIN		= 0x00000800;
	public static final		int			PERM_PLACEVEGET			= 0x00001000;
	public static final		int			PERM_REMOVEVEGET		= 0x00002000;
	public static final		int			PERM_DESTROYVEGET		= 0x00004000;
	public static final		int			PERM_PLACEGRASS			= 0x00008000;
	public static final		int			PERM_REMOVEGRASS		= 0x00010000;
	public static final		int			PERM_PLACEWATER			= 0x00020000;
	public static final		int			PERM_REMOVEWATER		= 0x00040000;
	public static final		int			PERM_CREATEBLUEPR		= 0x00080000;
	public static final		int			PERM_PLACEBLUEPRINT		= 0x00100000;
	public static final		int			PERM_CREAT_PLACEBLOCKS	= 0x00200000;
	public static final		int			PERM_CREAT_PLACEVEGET	= 0x00400000;
	public static final		int			PERM_CREAT_TERRAINEDIT	= 0x00800000;
	public static final		int			PERM_PUT2CHEST			= 0x01000000;
	public static final		int			PERM_GETFROMCHEST		= 0x02000000;
//	public static final		int			PERM_CHESTDROP			= 0x02000000;
	public static final		int			PERM_DOORINTERACT		= 0x04000000;
	public static final		int			PERM_FURNACEINTERACT	= 0x08000000;
	public static final		int			PERM_OTHERINTERACT		= 0x10000000;
	public static final		int			PERM_BIT29				= 0x20000000;
	public static final		int			PERM_ADDPLAYER			= 0x40000000;
	public static final		int			PERM_OWNER				= 0x80000000;
	public static final		int			PERM_ALL					= 0xFFFFFFFF;
	public static final		int			PERM_DEFAULT			= (PERM_ENTER | PERM_LEAVE);
	// to convert a permission index (0 - 31) into the corresponding bit flag;
	public static final		int[]		permIdx2bit	=
	{
		PERM_ENTER,				PERM_LEAVE,				PERM_PLACEBLOCKS,		PERM_DESTROYBLOCKS,
		PERM_PLACECONSTR,		PERM_REMOVECONSTR,		PERM_DESTROYCONSTR,		PERM_PLACEOBJECTS,
		PERM_REMOVEOBJECTS,		PERM_DESTROYOBJECTS,	PERM_PLACETERRAIN,		PERM_DESTROYTERRAIN,
		PERM_PLACEVEGET,		PERM_REMOVEVEGET,		PERM_DESTROYVEGET,		PERM_PLACEGRASS,
		PERM_REMOVEGRASS,		PERM_PLACEWATER,		PERM_REMOVEWATER,		PERM_CREATEBLUEPR,
		PERM_PLACEBLUEPRINT,	PERM_CREAT_PLACEBLOCKS,	PERM_CREAT_PLACEVEGET,	PERM_CREAT_TERRAINEDIT,
		PERM_PUT2CHEST,			PERM_GETFROMCHEST,		PERM_DOORINTERACT,		PERM_FURNACEINTERACT,
		PERM_OTHERINTERACT,		PERM_ADDPLAYER,			PERM_OWNER
	};
	// player attribute keys
	public static final		String		key_areas			= "com.mwr.apAreas";	// the areas the player has special permission for
	public static final		String		key_areaPerms		= "com.mwr.apPerms";	// the cumulated permissions the player has at the moment
	public static final		String		key_areasShown		= "com.mwr.apShown";	// whether areas are shown or not for the player
	public static final		String		key_areasText		= "com.mwr.apText";		// the names of the areas the player is in
	public static final		String		key_inAreas			= "com.mwr.apInAreas";	// the areas the player is in at the moment
	public static final		String		key_isAdmin			= "com.mwr.apIsAdmin";	// whether the player is admin or manager

	// The default values for the settings
	private static final	boolean		adminNoPrivDef		= false;
	private static final	boolean		adminOnlyDef		= true;
	private static final	int			heightTopDef		= 400;
	private static final	int			heightBottomDef		= -600;
	private static final	int			infoBkgColourDef	= 0xE0E0A0E0;
	private static final	int			infoFontColourDef	= 0x000000FF;
	private static final	int			infoXPosDef			= 20;
	private static final	int			infoYPosDef			= 70;
	private static final	String		localeLanguageDef	= "en";

	// FIELDS
	//
	protected static		boolean		adminNoPriv			= adminNoPrivDef;
	protected static		boolean		adminOnly			= adminOnlyDef;
	protected static		String		commandPrefix		= "/ap";
	protected static		int			heightTop			= heightTopDef;
	protected static		int			heightBottom		= heightBottomDef;
	protected static		int			infoBkgColour		= infoBkgColourDef;
	protected static		int			infoFontColour		= infoFontColourDef;
	protected static		int			infoXPos			= infoXPosDef;
	protected static		int			infoYPos			= infoYPosDef;
	protected static		Locale		locale;

	public static			AreaProtection			plugin;
	protected static		Map<String, Integer>	presets;	

	/**
	 * Called by the API when the plug-in is enabled after being loaded.
	 */
	@Override
	public void onEnable()
	{
		plugin	= this;
		initSettings();
		presets	= initPresets(getPath() + "/presets");
		Msgs.init(getPath(), locale);
		Db.init();
		registerEventListener(ListenerPlayer.getInstance());
		System.out.println("AREA PROTECTION "+version+" enabled successfully!");
	}

	/**
	 * Called by the API when the plug-in is disabled, before being unloaded.
	 */
	@Override
	public void onDisable()
	{
		unregisterEventListener(ListenerPlayer.getInstance());
		Db.deinit();
		System.out.println("AREA PROTECTION "+version+" disabled successfully!");
	}

	//****************************
	//	PUBLIC Plug-in Central METHODS
	//*****************************

	/**
	 * Returns the human-readable name of this plug-in.
	 * <p>Conform to the PluginCentral interface.
	 * @return	the plug-in human-readable name as a String.
	 */
	public String getPublicName()
	{
		return publicName;
	}

	/**
	 * Displays the main user interaction entry point for this plug-in
	 * (GUI or not!).
	 * <p>Conforms to the PluginCentral interface.
	 * @param player	the player for whom to display the plug-in GUI.
	 */
	public void mainGui(Player player)
	{
		GuiMainMenu	mainMenu	= new GuiMainMenu(player);
		mainMenu.show(player);
	}

	//********************
	// PUBLIC METHODS
	//********************

	/**
		Toggles on/off the area visibility for the player
		@param	player	the player
		@return	the new status of area visibility for the player
	*/
	public static boolean togglePlayerAreas(Player player)
	{
		return Db.togglePlayerAreas(player);
	}

	/**
		Returns the current area permissions for the player
		@param	player	the player
		@return	the player permissions for the areas he is currently in or
				all permissions, if he is currently in no area
	*/
	public static int getPlayerPermissions(Player player)
	{
		Integer		perm	= (Integer)player.getAttribute(key_areaPerms);
		return (perm != null ? perm : 0xFFFFFFFF);
	}

	/**
	 * Retrieves the permissions associated with a preset given the preset name.
	 * @param	name	the name of preset
	 * @return	the preset permissions or 0 if such a preset does not exists.
	 */
	public static int getPresetPermissionsFromName(String name)
	{
		Integer	perms	= presets.get(name);
		if (perms == null)
			return 0;
		return perms;
	}

	/**
	 * Returns the name of the first listed preset whose permissions match a given permission set.
	 * @param	perms		the permission to match
	 * @return	the name of the first found group matching the permissions
	 * 			or "Custom if no group matching group is found.
	 */
	public static String getPresetNameFromPermissions(int perms)
	{
		for (Entry<String,Integer> entry : presets.entrySet())
		{
			if (entry.getValue() == perms)
				return entry.getKey();
		}
		return Msgs.msg[Msgs.gui_customPerms];
	}

	/**
	 * Rearranges the starting and ending point of an area, so that the starting point
	 * is in the area point with min x,y,z coordinates and the ending point in the area
	 * point with max x,y,z coordinates.
	 * <p>The area span is not changed in any way, but the starting and ending point may be moved.
	 * <p>Needed to circumvent a bug in Area.rearrange().
	 * @param	area	the area to rearrange.
	 * @return	true if coordinates have been rearranged | false if not.
	 */
	public static boolean rearrangeArea(Area area)
	{
		boolean		arrange	= false;
		Vector3f	from	= new Vector3f();
		Vector3f	to		= new Vector3f();
		Vector3i	endB	= area.getEndBlockPosition();
		Vector3i	endC	= area.getEndChunkPosition();
		Vector3i	startB	= area.getStartBlockPosition();
		Vector3i	startC	= area.getStartChunkPosition();
		int			temp;
		ChunkUtils.getGlobalPosition(startC, startB, from);
		ChunkUtils.getGlobalPosition(endC,   endB,   to);
		if (from.x > to.x)
		{
			temp		= startC.x;
			startC.x	= endC.x;
			endC.x		= temp;
			temp		= startB.x;
			startB.x	= endB.x;
			endB.x		= temp;
			arrange		= true;
		}
		if (from.y > to.y)
		{
			temp		= startC.y;
			startC.y	= endC.y;
			endC.y		= temp;
			temp		= startB.y;
			startB.y	= endB.y;
			endB.y		= temp;
			arrange		= true;
		}
		if (from.z > to.z)
		{
			temp		= startC.z;
			startC.z	= endC.z;
			endC.z		= temp;
			temp		= startB.z;
			startB.z	= endB.z;
			endB.z		= temp;
			arrange		= true;
		}

		return arrange;
	}

	/**
	 * Returns the 3D coordinates of the centre of an RW area as a String.
	 * @param	rwArea	the area for which to retrieve the centre.
	 * @return	a String describing the area centre.
	 */
	public static String getAreaCentre(Area rwArea)
	{
		Vector3f	from, to;

		from	= ChunkUtils.getGlobalPosition(rwArea.getStartChunkPosition(),
				rwArea.getStartBlockPosition());
		to		= ChunkUtils.getGlobalPosition(rwArea.getEndChunkPosition(),
				rwArea.getEndBlockPosition());
		float		centreN	= (from.z + to.z + 1) / 2;
		float		centreE	= (from.x + to.x + 1) / 2;
		float		centreH	= (from.y + to.y + 1) / 2;
		String		nSign	= Msgs.msg[Msgs.gui_N];
		String		eSign	= Msgs.msg[Msgs.gui_W];
		if (centreN < 0)
		{
			centreN = -centreN;
			nSign	= Msgs.msg[Msgs.gui_S];
		}
		if (centreE < 0)
		{
			centreE = -centreE;
			eSign	= Msgs.msg[Msgs.gui_E];
		}
		return String.format(Msgs.msg[Msgs.gui_areaCentreFmt], centreN, nSign, centreE, eSign, centreH);
	}
	/**
	 * Returns the 3D span of an RW area as a String.
	 * @param	rwArea	the area for which to retrieve the spans.
	 * @return	a String describing the area spans.
	 */
	public static String getAreaSpans(Area rwArea)
	{
		Vector3f	from, to;

		from	= ChunkUtils.getGlobalPosition(rwArea.getStartChunkPosition(),
				rwArea.getStartBlockPosition());
		to		= ChunkUtils.getGlobalPosition(rwArea.getEndChunkPosition(),
				rwArea.getEndBlockPosition());
		return String.format(Msgs.msg[Msgs.gui_areaSpanFmt],
				(int)(to.z - from.z)+1, (int)(to.x - from.x)+1, (int)(to.y - from.y)+1);
	}

	//********************
	// PRIVATE HELPER METHODS
	//********************

	/**
		Initialises settings from settings file.
	*/
	private void initSettings()
	{
		// create and load settings from disk
		Properties settings	= new Properties();
		String			strLocale;
		FileInputStream in;
		try {
			in = new FileInputStream(getPath() + "/settings.properties");
			settings.load(in);
			in.close();
			// fill global values
			commandPrefix	= "/" + settings.getProperty("command", commandPrefix);

			adminNoPriv		= propertyToInt(settings, "adminNoPriv",	adminNoPrivDef ? 1 : 0) != 0;
			adminOnly		= propertyToInt(settings, "adminOnly",		adminOnlyDef ? 1 : 0) != 0;
			heightTop		= propertyToInt(settings, "heightTop",		heightTopDef);
			heightBottom	= propertyToInt(settings, "heightBottom",	heightBottomDef);
			infoBkgColour	= propertyToInt(settings, "infoBkgColour",	infoBkgColourDef);
			infoFontColour	= propertyToInt(settings, "infoFontColour",	infoFontColourDef);
			infoXPos		= propertyToInt(settings, "infoXPos",		infoXPosDef);
			infoYPos		= propertyToInt(settings, "infoYPos",		infoYPosDef);
			strLocale		= settings.getProperty("locale", localeLanguageDef);
		}
		catch (IOException e)
		{
			strLocale	= localeLanguageDef;	// other settings are init'ed anyway: on exception, do nothing
		}
		// locale is a bit more complex
		String[]	localeParams	= strLocale.split("-");
		if (localeParams.length > 0)
		{
			if (localeParams.length > 1 && localeParams[2].length() > 0)
			{
				if (localeParams.length > 2 && localeParams[2].length() > 0)
					locale	= new Locale(localeParams[0], localeParams[1], localeParams[2]);
				else
					locale	= new Locale(localeParams[0], localeParams[1]);
			}
			else
				locale	= new Locale(localeParams[0]);
		}
		else
			locale	= new Locale(localeLanguageDef);
	}

	// Two arrays used to iterate on permission names in preset/group definitions
	// Permission names, as used in the preset/group definition files
	static private final String permNames[]	=
	{
		"CanEnter",								"CanLeave",						"PlaceBlock",					"DestroyBlock",
		"PlaceConstructions",					"RemoveConstructions",			"DestroyConstructions",			"PlaceObjects",
"RemoveObjects","PickupObject",					"DestroyObjects",				"FillWorld",					"DestroyWorld",
		"PlaceVegetation",			"RemoveVegetation","PickupVegetation",		"DestroyVegetation",			"PlaceGrass",
		"CutGrass",								"PlaceWater",					"RemoveWater",					"CreateBlueprint",					
		"PlaceBlueprint",						"CreativePlaceBlock",			"CreativePlaceVegetation",		"CreativeTerrainEdit",
"InventoryToChest","PutToChest", "ChestToInventory","ChestDrop","GetFromChest",	"DoorInteraction",				"FurnaceInteraction",
"OtherInteraction","ChangeObjectStatus",		"CanAddPlayer",					"Owner"
	};
	// old permissions, currently not implemented
	//	"ObjectsPlaceFilter", "ObjectsRemoveDestroyFilter", "ConstructionsFilter", "BlockFilter"
	// New permissions added:
	//	"PlaceGrass", "PlaceWater", "RemoveWater", "CreateBlueprints", "PlaceBlueprints", "RemoveBlueprints",
	//	"CreativePlaceBlock", "CreativePlaceVegetation", "CreativeTerrainEdit", "DoorInteraction", "FurnaceInteraction",
	//	"OtherInteraction"
	// Permission flags associated with each permission name
	static private final int	permValues[]	=
	{
			PERM_ENTER,							PERM_LEAVE,								PERM_PLACEBLOCKS,			PERM_DESTROYBLOCKS,
			PERM_PLACECONSTR,					PERM_REMOVECONSTR,						PERM_DESTROYCONSTR,			PERM_PLACEOBJECTS,
PERM_REMOVEOBJECTS,PERM_REMOVEOBJECTS,			PERM_DESTROYOBJECTS,					PERM_PLACETERRAIN,			PERM_DESTROYTERRAIN,
			PERM_PLACEVEGET,			PERM_REMOVEVEGET,PERM_REMOVEVEGET,				PERM_DESTROYVEGET,			PERM_PLACEGRASS,
			PERM_REMOVEGRASS,					PERM_PLACEWATER,						PERM_REMOVEWATER,			PERM_CREATEBLUEPR,
			PERM_PLACEBLUEPRINT,				PERM_CREAT_PLACEBLOCKS,					PERM_CREAT_PLACEVEGET,		PERM_CREAT_TERRAINEDIT,
PERM_PUT2CHEST,PERM_PUT2CHEST,	PERM_GETFROMCHEST,PERM_GETFROMCHEST,PERM_GETFROMCHEST,	PERM_DOORINTERACT,			PERM_FURNACEINTERACT,
			PERM_OTHERINTERACT,					PERM_ADDPLAYER,							PERM_OWNER
	};
	/**
	 * Initialises preset/group data from preset/group definition files
	 * @param	path	the path where to look for presets/group definitions.
	 * @return	a Map mapping preset name to preset permissions.
	 */
	static Map<String, Integer> initPresets(String path)
	{
		TreeMap<String, Integer>	map 	= new TreeMap<>();
		File dir = new File(path);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null)
		{
			FileInputStream	in;
			String			name;
			int				perms;
			String			val;
			for (File child : directoryListing)
			{
				try
				{
					in = new FileInputStream(child.getPath());
					Properties	settings	= new Properties();
					settings.load(in);
					in.close();
					perms	= 0;
					name	= settings.getProperty("GroupName");
					for (int i = 0; i < permNames.length; i++)
					{
						val = settings.getProperty(permNames[i], "0");
						perms	|= (val.equals("true") || val.equals("1") ? permValues[i] : 0);
					}
					if (name != null)
						map.put(name, perms);
				} catch (IOException e)
				{
					// Should not happen; anyway, if it cannot be read, do nothing!
				}
			}
		}
		return map;
	}

	/**
		Returns txt as an integer number if it can be interpreted as one
		or defaultVal if it cannot.

		@param	settings	the settings to look the property into
		@param	txt			the property key
		@param	defaultVal	the default value to use if the property is absent or does not
							represents an int
		@return	the equivalent int or defaultVal if txt cannot represent an integer.
	*/
	static protected int propertyToInt(Properties settings, String txt, int defaultVal)
	{
		String	txtVal	= settings.getProperty(txt);
		if (txtVal == null)
			return defaultVal;
		long val;			// using long ensures that full-32-bit hex strings can be decoded
		try {				// without overflowing the signed int representation
			val = Long.decode(txtVal);
		} catch (NumberFormatException e) {		// txt cannot be parsed as a number
			return defaultVal;
		}
		return (int)(val & 0xFFFFFFFF);
	}

}
