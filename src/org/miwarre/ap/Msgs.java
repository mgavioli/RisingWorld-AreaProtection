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
public class Msgs
{
	//
	// The ID's of the texts
	//
	// Main menu texts
	public static final int	gui_title				=  0;
	public static final int	gui_showAreas			=  1;
	public static final int	gui_newArea				=  2;
	public static final int	gui_editArea				=  3;
	public static final int	gui_deleteArea			=  4;
	public static final int gui_chestAccess			=  5;
	public static final int	gui_areaManagers		=  6;
	// Cardinal points
	public static final int	gui_N					=  7;
	public static final int	gui_E					=  8;
	public static final int	gui_S					=  9;
	public static final int	gui_W					= 10;
	// New area texts
	public static final int	gui_areaKeys			= 11;
	public static final int	gui_areaCentreFmt		= 12;
	public static final int	gui_areaSpanFmt			= 13;
	// Area properties edit
	public static final int	gui_editTitle			= 14;
	public static final int	gui_editName			= 15;
	public static final int	gui_editPermissGeneral	= 16;
	public static final int	gui_editPermissSpecific	= 17;
	public static final int	gui_editPermFirst		= 18;
	public static final int	gui_editPermLastArea		= 46;	// last permission applicable to an area
	public static final int	gui_editPermLastUser	= 48;	// last permission applicable to a user
	// Other GUI texts
	public static final int	gui_editeditPlayers		= 49;
	public static final int	gui_editeditGroups		= 50;
	public static final int	gui_notImplemented		= 51;
	public static final	int	gui_specPermPlayersTitle= 52;
	public static final	int	gui_specPermGroupsTitle	= 53;
	public static final	int	gui_areaPlayerPermsTitle= 54;
	public static final	int	gui_areaName			= 55;
	public static final	int	gui_playerName			= 56;
	public static final int	gui_editCreate			= 57;
	public static final int	gui_editUpdate			= 58;
	public static final int	gui_editAdd				= 59;
	public static final int	gui_editDelete			= 60;
	public static final int	gui_editEdit			= 61;
	public static final int	gui_noOwnedArea			= 62;
	public static final int	gui_customPerms			= 63;
	public static final	int	gui_selectPlayer		= 64;
	public static final	int	gui_selectGroup			= 65;
	public static final	int	gui_selectPreset		= 66;
	public static final	int	gui_topAreaHeight		= 67;
	public static final	int	gui_bottomAreaHeight	= 68;
	public static final	int	gui_setToDefault		= 69;
	// Other menu title
	public static final int	gui_selectArea			= 70;

	private static final int	LAST_TEXT	= gui_selectArea;

	//
	// The default built-in texts, used as fall-back if no message file is found.
	//
	public static String[]		msg = {
			// Main menu texts
			"Area Protection",							// 0
			"Show/Hide areas",
			"New area",
			"Edit area",
			"Delete area",
			"Chest access",
			"Area Managers",
			// Cardinal points
			"N",
			"E",
			"S",
			"W",										// 10
			// New area texts
			"RETURN to create, ESCAPE to abort",
			"Area Centre: %.1f%s, %.1f%s, %.1fh",
			"N/S: %d blk | E/W: %d blk | H: %d blk",
			// Area properties edit
			"Area Properties",
			"Name:",
			"Generic Area Permissions:",
			"Specific Area Permissions:",
			// Property names
			"Enter area",
			"Leave area",
			"Place blocks",								// 20
			"Destroy blocks",
			"Place constructions",
			"Remove constructions",
			"Destroy constructions",
			"Place objects",
			"Remove objects",
			"Destroy objects",
			"Place terrain",
			"Destroy terrain",
			"Place vegetation",							// 30
			"Remove vegetation",
			"Destroy vegetation",
			"Place grass",
			"Remove grass",
			"Place water",
			"Remove water",
			"Create blueprint",
			"Place blueprint",
			"Place block (creative)",
			"Place vegetation (creative)",				// 40
			"Edit terrain (creative)",
			"Put into chest",
			"Get from chest",
			"Door interaction",
			"Furnace interaction",
			"Other interaction",
			"Can add players",
			"Owner",
			// other GUI texts
			"Edit Players",
			"Edit Groups",								// 50
			"Coming soon!",
			"Players with special permissions",
			"Groups with special permissions",
			"Player/Group Permissions for Area",
			"Area Name:",
			"Player/Group Name:",
			"\n CREATE \n ",
			"\n UPDATE \n ",
			"\n ADD \n ",
			"\n DELETE \n ",							// 60
			"\n EDIT \n ",
			"[You own no area]",
			"Custom",
			"Select a player:",
			"Select a group:",
			"Select a preset:",
			"Top Height",
			"Bottom Height",
			"Set to default",
			// other menu titles
			"Select an Area"							// 70
	};

	private static final	String		MSGS_FNAME	= "/locale/messages";

	/**
	 * Initialises the texts overwriting the built-in texts with the texts for
	 * a specific locale.
	 * @param path		the plug-in path, used to locate the text files.
	 * @param locale	the locale to load texts for.
	 * @return			true: success | false: failure (built-in texts are used)
	 */
	public static boolean init(String path, Locale locale)
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
		return true;
	}

}
