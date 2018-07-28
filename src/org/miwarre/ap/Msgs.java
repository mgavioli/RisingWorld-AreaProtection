/****************************
	A r e a P r o t e c t i o n  -  A Rising World Java plug-in for area permissions.

	Msgs.java - Localisable user interface texts.

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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

/**
 * The localisable texts for the plug-in.
 */
class Msgs
{
	//
	// The ID's of the texts
	//
	// Main menu texts
	static final int	gui_title				=  0;
	static final int	gui_showAreas			=  1;
	static final int	gui_hideAreas			=  2;
	static final int	gui_newArea				=  3;
	static final int	gui_gotoArea				=  4;
	static final int	gui_editArea				=  5;
	static final int	gui_deleteArea			=  6;
	static final int	gui_chestAccess			=  7;
	static final int	gui_areaManagers		=  8;
	static final int	gui_adminsOff			=  9;
	static final int	gui_adminsOn			= 10;
	// Cardinal points
	static final int	gui_N					= 11;
	static final int	gui_E					= 12;
	static final int	gui_S					= 13;
	static final int	gui_W					= 14;
	// New area texts
	static final int	gui_areaKeys			= 15;
	static final int	gui_areaCentreFmt		= 16;
	static final int	gui_areaSpanFmt			= 17;
	// Area properties edit
	static final int	gui_editTitle			= 18;
	static final int	gui_editName			= 19;
	static final int	gui_editPermissGeneral	= 20;
	static final int	gui_editPermissSpecific	= 21;
	static final int	gui_editPermFirst		= 22;
	static final int	gui_editPermLastArea		= 51;	// last permission applicable to an area
	static final int	gui_editPermLastUser	= 53;	// last permission applicable to a user
	// Other GUI texts
	static final int	gui_editeditPlayers		= 54;
	static final int	gui_editeditGroups		= 55;
	static final int	gui_editeditExtent		= 56;
	static final int	gui_notImplemented		= 57;
	static final int	gui_specPermPlayersTitle= 58;
	static final int	gui_specPermGroupsTitle	= 59;
	static final int	gui_areaPlayerPermsTitle= 60;
	static final int	gui_areaName			= 61;
	static final int	gui_playerName			= 62;
	static final int	gui_editCreate			= 63;
	static final int	gui_editUpdate			= 64;
	static final int	gui_editAdd				= 65;
	static final int	gui_editDelete			= 66;
	static final int	gui_editEdit			= 67;
	static final int	gui_editKeep			= 68;
	static final int	gui_noDefinedArea		= 69;
	static final int	gui_noOwnedArea			= 70;
	static final int	gui_customPerms			= 71;
	static final int	gui_selectPlayer		= 72;
	static final int	gui_selectGroup			= 73;
	static final int	gui_selectPreset		= 74;
	static final int	gui_topAreaHeight		= 75;
	static final int	gui_bottomAreaHeight	= 76;
	static final int	gui_setToDefault		= 77;
	// Other menu title
	static final int	gui_areaExtentTitle		= 78;
	static final int	gui_selectArea			= 79;
	static final int	gui_confirmAreaDelete	= 80;

	private static final int	LAST_TEXT	= gui_confirmAreaDelete;

	//
	// The default built-in texts, used as fall-back if no message file is found.
	//
	static String[]		msg = {
			// Main menu texts
			"Area Protection",							// 0
			"Show areas",
			"Hide areas",
			"New area",
			"Go to area",
			"Edit area",
			"Delete area",
			"Chest access (NOT impl.)",
			"Area Managers",
			"Admin priv. OFF",
			"Admin priv. ON",							// 10
			// Cardinal points
			"N",
			"E",
			"S",
			"W",
			// New area texts
			"RETURN to create, ESCAPE to abort",
//			"Area Centre: %.1f%s, %.1f%s, %.1fh",
			"Area Centre: %.1f (%s), %.1f (h), %.1f (%s)",
			"N/S: %d blk | E/W: %d blk | H: %d blk",
			// Area properties edit
			"Area Properties",
			"Name:",
			"Generic Area Permissions:",				// 20
			"Specific Area Permissions:",
			// Property names
			"Enter area",
			"Leave area",
			"Place blocks",
			"Destroy blocks",
			"Place constructions",
			"Remove constructions",
			"Destroy constructions",
			"Place objects",
			"Remove objects",							// 30
			"Destroy objects",
			"Place terrain",
			"Destroy terrain",
			"Place vegetation",
			"Remove vegetation",
			"Destroy vegetation",
			"Place grass",
			"Remove grass",
			"Place water",
			"Remove water",								// 40
			"Create blueprint",
			"Place blueprint",
			"Place block (creative)",
			"Place vegetation (creative)",
			"Edit terrain (creative)",
			"Put into chest",
			"Get from chest",
			"Door interaction",
			"Furnace interaction",
			"Other interaction",						// 50
			"Explosions",
			"Can add players",
			"Owner",
			// other GUI texts
			"Edit Players",
			"Edit Groups",
			"Edit Extent",
			"Coming soon!",
			"Players with special permissions",
			"Groups with special permissions",
			"Player/Group Permissions for Area",		// 60
			"Area Name:",
			"Player/Group Name:",
			"\n CREATE \n ",
			"\n UPDATE \n ",
			"\n ADD \n ",
			"\n DELETE \n ",
			"\n EDIT \n ",
			"\n KEEP \n",
			"[No area defined]",
			"[You own no area]",						// 70
			"Custom",
			"Select a player:",
			"Select a group:",
			"Select a preset:",
			"Top Height",
			"Bottom Height",
			"Set to default",
			// other menu titles
			"\"%s\" area extent",
			"Select an Area",
			"Are you sure you want to delete the area:"	// 80
	};

	private static final	String		MSGS_FNAME	= "/locale/messages";

	/**
	 * Initialises the texts overwriting the built-in texts with the texts for
	 * a specific locale.
	 * @param path		the plug-in path, used to locate the text files.
	 * @param locale	the locale to load texts for.
	 * @return			true: success | false: failure (built-in texts are used)
	 */
	static boolean init(String path, Locale locale)
	{
		if (locale == null)
			return false;
		String		country		= locale.getCountry();
		String		variant		= locale.getVariant();
		String		fname		= MSGS_FNAME + "_" + locale.getLanguage();
		if (country.length() > 0)	fname += "_" + country;
		if (variant.length() > 0)	fname += "_" + variant;
		fname	+= ".properties";
		Properties settings	= new Properties();
		// NOTE : use getResourcesAsStream() if the setting file is included in the distrib. .jar)
		FileInputStream in;
		try
		{
		in = new FileInputStream(path + fname);
		settings.load(in);
		in.close();
		} catch (IOException e) {
			System.out.println("** AREA PROTECTION plug-in ERROR: Property file '" + fname + "' for requested locale '"+ locale.toString() + "' not found. Defaulting to built-in 'en'");
			return false;
		}
		// Load strings from localised bundle
		for (int i = 0; i <= LAST_TEXT; i++)
			msg[i]	= settings.getProperty(String.format("%03d", i) );
		// a few strings require additional steps, to add margin around the text.
		msg[gui_editCreate]		= "\n " + msg[gui_editCreate] + " \n ";
		msg[gui_editUpdate]		= "\n " + msg[gui_editUpdate] + " \n ";
		msg[gui_editAdd]		= "\n " + msg[gui_editAdd]    + " \n ";
		msg[gui_editDelete]		= "\n " + msg[gui_editDelete] + " \n ";
		msg[gui_editEdit]		= "\n " + msg[gui_editEdit]   + " \n ";
		msg[gui_editKeep]		= "\n " + msg[gui_editKeep]   + " \n ";
		return true;
	}

}
