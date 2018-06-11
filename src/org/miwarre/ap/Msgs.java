/****************************
	A r e a P r o t e c t i o n  -  A Rising World Java plug-in for area permissions.

	Msgs.java - Localisable user interface texts.

	Created by : Maurizio M. Gavioli 2017-02-25

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2017
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

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
	public static final int	gui_editArea			=  3;
	public static final int	gui_deleteArea			=  4;
	// Cardinal points
	public static final int	gui_N					=  5;
	public static final int	gui_E					=  6;
	public static final int	gui_S					=  7;
	public static final int	gui_W					=  8;
	// New area texts
	public static final int	gui_areaKeys			=  9;
	public static final int	gui_areaCentreFmt		= 10;
	public static final int	gui_areaSpanFmt			= 11;
	public static final int	gui_editTitle			= 12;
	// Area properties edit
	public static final int	gui_editName			= 13;
	public static final int	gui_editPermissGeneral	= 14;
	public static final int	gui_editPermissSpecific	= 15;
	public static final int	gui_editPermFirst		= 16;
	public static final int	gui_editPermLastArea	= 41;	// last permission applicable to an area
	public static final int	gui_editPermLastUser	= 43;	// last permission applicable to a user
	public static final int	gui_editeditPlayers		= 44;
	public static final int	gui_editeditGroups		= 45;
	public static final int	gui_notImplemented		= 46;
	// Other GUI texts
	public static final	int	gui_specPermPlayersTitle= 47;
	public static final	int	gui_specPermGroupsTitle	= 48;
	public static final	int	gui_areaPlayerPermsTitle= 49;
	public static final	int	gui_areaName			= 50;
	public static final	int	gui_playerName			= 51;
	public static final int	gui_editCreate			= 52;
	public static final int	gui_editUpdate			= 53;
	public static final int	gui_editAdd				= 54;
	public static final int	gui_editDelete			= 55;
	public static final int	gui_editEdit			= 56;
	public static final int	gui_noOwnedArea			= 57;
	public static final int	gui_customPerms			= 58;
	public static final	int	gui_selectPlayer		= 59;
	public static final	int	gui_selectGroup			= 60;
	public static final	int	gui_selectPreset		= 61;
	// Other menu title
	public static final int	gui_selectArea			= 62;
	public static final int	gui_selectPlayerTitle	= 63;

	private static final int	LAST_TEXT	= gui_selectPlayerTitle;

	//
	// The default built-in texts, used as fall-back if no message file is found.
	//
	public static String[]		msg = {
			// Main menu texts
			"Area Protection",
			"Show/Hide areas",
			"New area",
			"Edit area",
			"Delete area",
			// Cardinal points
			"N",
			"E",
			"S",
			"W",
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
			"Destroy blocks",
			"Place blocks",
			"Destroy constructions",
			"Place constructions",
			"[Remove constructions]",
			"Destroy objects",
			"Place objects",
			"Remove objects",
			"Destroy terrain",
			"Place terrain",
			"Destroy vegetation",
			"Place vegetation",
			"Remove vegetation",
			"Place grass",
			"Remove grass",
			"Place water",
			"Remove water",
			"Create blueprint",
			"Place blueprint",
			"[Remove blueprint]",
			"[Change object status]",
			"Inventory to chest",
			"Chest to inventory",
			"Chest drop",
			"Can add players",
			"Owner",
			"Edit Players",
			"Edit Groups",
			// other GUI texts
			"[Permissions within square brackets are not implemented yet]",
			"Players with special permissions",
			"Groups with special permissions",
			"Player/Group Permissions for Area",
			"Area Name:",
			"Player/Group Name:",
			"\n CREATE \n ",
			"\n UPDATE \n ",
			"\n ADD \n ",
			"\n DELETE \n ",
			"\n EDIT \n ",
			"[You own no area]",
			"Custom",
			"Select a player:",
			"Select a group:",
			"Select a preset:",
			// other menu titles
			"Select an Area",
			"Player/Group Selection"
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
