/****************************
	A r e a P r o t e c t i o n  -  A Rising World Java plug-in for area permissions.

	GuiMainMenu.java - The main plug-in menu

	Created by : Maurizio M. Gavioli 2017-03-04

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

import org.miwarre.ap.gui.GuiDefs;
import org.miwarre.ap.gui.GuiDefs.GuiCallback;
import org.miwarre.ap.gui.GuiMenu;
import net.risingworld.api.objects.Player;

/**
 * The plug-in main menu. It is the main UI entry point for the plug-in.
 * <p>Relies on gui/GuiMenu.
 */
public class GuiMainMenu extends GuiMenu
{
	// The ID's of the menu items. Used by the MenuHandler.
	private static final	int		MENU_SHOWAREAS_ID		= 1;
	private static final	int		MENU_EDITAREA_ID		= 2;
	private static final	int		MENU_NEWAREA_ID			= 3;
	private static final	int		MENU_DELETEAREA_ID		= 4;
	private static final	int		MENU_CHESTACCESS_ID		= 5;
	private static final	int		MENU_AREAMANAGERS_ID	= 6;
	private static final	int		AREACREAT_PRIORITY		= 3;

	public GuiMainMenu(Player player)
	{
		// construct the containing modal window (callback cannot be created and passed in the
		// call to the c'tor, as there is no context yet, until the modal window object is created)
		super(AreaProtection.plugin, Msgs.msg[Msgs.gui_title], null);
		// create and set the callback
		setCallback(new MenuHandler());
		// add the common menu items
		addTextItem(Msgs.msg[Msgs.gui_showAreas],	MENU_SHOWAREAS_ID, null);
		addTextItem(Msgs.msg[Msgs.gui_editArea],	MENU_EDITAREA_ID, null);
		// add the admin-specific menu items, if required
		if ( (Boolean)player.getAttribute(AreaProtection.key_isAdmin) || !AreaProtection.adminOnly)
		{
			addTextItem(Msgs.msg[Msgs.gui_newArea],		MENU_NEWAREA_ID,		null);
			addTextItem(Msgs.msg[Msgs.gui_deleteArea],	MENU_DELETEAREA_ID,		null);
			addTextItem(Msgs.msg[Msgs.gui_chestAccess], MENU_CHESTACCESS_ID,	null);
			if (player.isAdmin())
				addTextItem(Msgs.msg[Msgs.gui_areaManagers],MENU_AREAMANAGERS_ID,	null);
		}
	}

	//********************
	// HANDLERS
	//********************

	//
	// Handles the call-backs from the main menu.
	//
	private class MenuHandler implements GuiCallback
	{
		@Override
		public void onCall(Player player, int id, Object obj)
		{
			switch (id)
			{
			case MENU_SHOWAREAS_ID:
				AreaProtection.togglePlayerAreas(player);
				break;
			case MENU_NEWAREA_ID:
				pop(player);		// dismiss the menu; the other choices keep it for further commands
				NewAreaCreation nac	= new NewAreaCreation(player);
				nac.setPriority(AREACREAT_PRIORITY);
				nac.start();
				break;
			case MENU_EDITAREA_ID:
				// display a list of areas to choose the one to edit
				push(player, new GuiAreaList(player, new EditListHandler()));
				break;
			case MENU_DELETEAREA_ID:
				// display a list of areas to choose the one to delete
				push(player, new GuiAreaList(player, new DeleteListHandler()));
				break;
			case MENU_CHESTACCESS_ID:
				break;
			case MENU_AREAMANAGERS_ID:
				ProtArea	area	= new ProtArea(AreaProtection.AREAMANAGER_AREAID,
						0, 0, 0,  0, 0, 0,  Msgs.msg[Msgs.gui_areaManagers], 0);
				//		from		to			name						permissions
				push(player, new GuiPlayersEdit(area, Db.LIST_TYPE_MANAGERS));
				break;
			}
		}
	}

	//
	// Handles the call-backs from the list of areas for editing.
	//
	private class EditListHandler implements GuiCallback
	{
		/**
		 * @param	player	the player interacting with the area list
		 * @param	id		the id of the selected area
		 * @param	obj		the selected area
		 */
		@Override
		public void onCall(Player player, int id, Object obj)
		{
			if (id != GuiDefs.ABORT_ID)
				// display a dialogue box to edit the area properties
				push(player, new GuiAreaEdit(null, (ProtArea)obj, player, GuiAreaEdit.TYPE_EDIT));
		}
	}

	//
	// Handles the call-backs from the list of areas for deleting.
	//
	private class DeleteListHandler implements GuiCallback
	{
		/**
		 * @param	player	the player interacting with the area list
		 * @param	id		the id of the selected area
		 * @param	obj		the selected area
		 */
		@Override
		public void onCall(Player player, int id, Object obj)
		{
			if (id != GuiDefs.ABORT_ID)
				Db.deleteArea((ProtArea)obj);
		}
	}

}