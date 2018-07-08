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

//import java.util.ArrayList;
//import net.risingworld.api.callbacks.Callback;
import java.util.Map;
import net.risingworld.api.gui.GuiLabel;
//import net.risingworld.api.objects.Chest;
import org.miwarre.ap.gui.GuiDefs;
import org.miwarre.ap.gui.GuiDefs.GuiCallback;
import org.miwarre.ap.gui.GuiMenu;
import net.risingworld.api.objects.Player;
import net.risingworld.api.utils.Area;
import org.miwarre.ap.gui.GuiMessageBox;
//import net.risingworld.api.utils.CollisionType;
//import net.risingworld.api.utils.RayCastResult;
//import net.risingworld.api.utils.Vector3f;

/**
 * The plug-in main menu. It is the main UI entry point for the plug-in.
 * <p>Relies on gui/GuiMenu.
 */
class GuiMainMenu extends GuiMenu
{
	//
	// CONSTANTS
	//
	// The ID's of the menu items. Used by the MenuHandler.
	private static final	int		MENU_SHOWAREAS_ID		= 1;
	private static final	int		MENU_EDITAREA_ID		= 2;
	private static final	int		MENU_NEWAREA_ID			= 3;
	private static final	int		MENU_GOTOAREA_ID		= 4;
	private static final	int		MENU_DELETEAREA_ID		= 5;
	private static final	int		MENU_CHESTACCESS_ID		= 6;
	private static final	int		MENU_AREAMANAGERS_ID	= 7;
	private static final	int		MENU_ADMINSACCESS_ID	= 8;
	private static final	int		AREACREAT_PRIORITY		= 3;

	//
	// FIELDS
	//
//	private final	GuiLabel	hideShowMenuItem;
//	private			GuiLabel	adminMenuItem;

	public GuiMainMenu(Player player)
	{
		// construct the containing modal window (callback cannot be created and passed in the
		// call to the c'tor, as there is no context yet, until the modal window object is created)
		super(AreaProtection.plugin, Msgs.msg[Msgs.gui_title], null);
		// create and set the callback
		setCallback(new MenuHandler());
		// add the common menu items
		addTextItem(Msgs.msg[(boolean)player.getAttribute(AreaProtection.key_areasShown) ?
				Msgs.gui_hideAreas : Msgs.gui_showAreas], MENU_SHOWAREAS_ID, null);
		addTextItem(Msgs.msg[Msgs.gui_editArea],	MENU_EDITAREA_ID, null);
		// add the admin-specific menu items, if required
		if ( (Boolean)player.getAttribute(AreaProtection.key_isAdmin) || !AreaProtection.adminOnly)
		{
			addTextItem(Msgs.msg[Msgs.gui_newArea],		MENU_NEWAREA_ID,		null);
			addTextItem(Msgs.msg[Msgs.gui_gotoArea],		MENU_GOTOAREA_ID,		null);
			addTextItem(Msgs.msg[Msgs.gui_deleteArea],	MENU_DELETEAREA_ID,		null);
			addTextItem(Msgs.msg[Msgs.gui_chestAccess], MENU_CHESTACCESS_ID,	null);
			if (player.isAdmin())
			{
				addTextItem(Msgs.msg[Msgs.gui_areaManagers],MENU_AREAMANAGERS_ID,	null);
				addTextItem(Msgs.msg[AreaProtection.adminNoPriv ?
						Msgs.gui_adminsOn : Msgs.gui_adminsOff],MENU_ADMINSACCESS_ID,	null);
			}
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
				boolean		show		= AreaProtection.togglePlayerAreas(player);
				GuiLabel	menuItem	= (GuiLabel)getChildFromId(MENU_SHOWAREAS_ID);
				if (menuItem != null)
					menuItem.setText(Msgs.msg[show ? Msgs.gui_hideAreas : Msgs.gui_showAreas]);
				break;
			case MENU_NEWAREA_ID:
				pop(player);		// dismiss the menu; the other choices keep it for further commands
				NewAreaCreation nac	= new NewAreaCreation(player);
				nac.setPriority(AREACREAT_PRIORITY);
				nac.start();
				break;
			case MENU_GOTOAREA_ID:
				// display a list of areas to choose the one to go to
				push(player, new GuiAreaList(player, new GotoListHandler()));
				break;
			case MENU_EDITAREA_ID:
				Map<Integer,Long> areas = (Map<Integer,Long>)player.getAttribute(AreaProtection.key_inAreas);
				// if inside some area(s), jump to edit the first of them
				if (areas != null && !areas.isEmpty())
				{
					ProtArea	area	= Db.getAreaFromId((int) areas.keySet().toArray()[0]);
					push(player, new GuiAreaEdit(null, area, player, GuiAreaEdit.TYPE_EDIT));
				}
				else
				// if not inside any area, display a list of areas to choose
					push(player, new GuiAreaList(player, new EditListHandler()));
				break;
			case MENU_DELETEAREA_ID:
				// display a list of areas to choose the one to delete
				push(player, new GuiAreaList(player, new DeleteListHandler()));
				break;
//			case MENU_CHESTACCESS_ID:
//				player.raycast(CollisionType.OBJECTS, new RaycastHandler());
//				break;
			case MENU_AREAMANAGERS_ID:
				ProtArea	area	= new ProtArea(AreaProtection.AREAMANAGER_AREAID,
						0, 0, 0,  0, 0, 0,  Msgs.msg[Msgs.gui_areaManagers], 0);
				//		from		to			name						permissions
				push(player, new GuiPlayersEdit(area, Db.LIST_TYPE_MANAGERS));
				break;
			case MENU_ADMINSACCESS_ID:
				// flip admin privileges
				AreaProtection.adminNoPriv = !AreaProtection.adminNoPriv;
				// update menu item text
				menuItem	= (GuiLabel)getChildFromId(MENU_ADMINSACCESS_ID);
				if (menuItem != null)
					menuItem.setText(Msgs.msg[AreaProtection.adminNoPriv ? Msgs.gui_adminsOn : Msgs.gui_adminsOff]);
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
			push(player, new GuiMessageBox(AreaProtection.plugin, Msgs.msg[Msgs.gui_deleteArea],
					new String[] {Msgs.msg[Msgs.gui_confirmAreaDelete], ((ProtArea)obj).getName()},
					new String[] {Msgs.msg[Msgs.gui_editDelete], Msgs.msg[Msgs.gui_editKeep]},
					obj,
					new DeleteConfirmHandler()));
		}
	}

	//
	// Handles the call-backs from the delete confirmation message box
	//
	private class DeleteConfirmHandler implements GuiCallback
	{
		@Override
		public void onCall(Player player, int id, Object obj)
		{
			if (id == GuiDefs.OK_ID)
				Db.deleteArea((ProtArea)obj);
		}
	}

	//
	// Handles the call-backs from the list of areas for editing.
	//
	private class GotoListHandler implements GuiCallback
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
			{
				Db.movePlayerToArea(player, (ProtArea)obj);
			}
		}
	}

	//
	//
/*	private class RaycastHandler implements Callback<RayCastResult>
	{
		@Override
		public void onCall(RayCastResult result)
		{
			Vector3f	pt;
			if (result == null || (pt=result.getCollisionPoint()) != null)
				return;
			ArrayList<Chest> chests = (ArrayList<Chest>)AreaProtection.plugin.getWorld().getAllChests(null);
			for (Chest chest : chests)
			{

			}
		}
	}
*/
}
