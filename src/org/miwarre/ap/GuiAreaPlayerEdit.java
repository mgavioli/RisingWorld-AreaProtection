/****************************
	A r e a P r o t e c t i o n  -  A Rising World Java plug-in for area permissions.

	GuiAreaPlayerEdit.java - A dialogue box to edit the permissions of a player for an area.

	Created by : Maurizio M. Gavioli 2017-04-03

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

import java.util.Map;
import org.miwarre.ap.gui.GuiDefs;
import org.miwarre.ap.gui.GuiModalWindow;
import org.miwarre.ap.gui.GuiTitleBar;
import org.miwarre.ap.gui.GuiDefs.GuiCallback;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.gui.PivotPosition;
import net.risingworld.api.objects.Player;

/**
 * A dialogue box to edit the permission given to a player for an area
 */
class GuiAreaPlayerEdit extends GuiModalWindow
{
	// Control positions: X
	private static final	int		NAME_LBL_X		= 150;
	private static final	int		NAME_TXT_X		= (NAME_LBL_X + GuiDefs.DEFAULT_PADDING);
	private static final	int		PERMISS_X		= GuiDefs.DEFAULT_PADDING;
	// Control positions: Y
	private static final	int		PANEL_HEIGHT	=
			GuiAreaPerms.PANEL_HEIGHT + GuiDefs.ITEM_SIZE * 5 + GuiDefs.DEFAULT_PADDING * 5;
//			the permission group	2 row of 1-line texts & 1 row of 3-line button + and 5 paddings between the rows
	private static final	int		WINDOW_HEIGHT	= GuiTitleBar.TITLEBAR_HEIGHT + PANEL_HEIGHT; 
	private static final	int		AREA_LBL_Y		= PANEL_HEIGHT - GuiDefs.DEFAULT_PADDING;
	private static final	int		PLAYER_LBL_Y	= AREA_LBL_Y - GuiDefs.ITEM_SIZE - GuiDefs.DEFAULT_PADDING;
	private static final	int		PERMISS_Y		= PLAYER_LBL_Y - GuiDefs.ITEM_SIZE - GuiDefs.DEFAULT_PADDING;
	private static final	int		DO_BUTT_Y		= PERMISS_Y - GuiAreaPerms.PANEL_HEIGHT - GuiDefs.DEFAULT_PADDING - (GuiDefs.ITEM_SIZE * 3) / 2;

	// Id's of controls
	// the first GuiAreaPerms.NUM_OF_PERMS id's are reserved for permission check boxes
	// but starting from 1, rather from 0 (=> +1); other controls resume from next id (=> +2)
	private static final	int		DOBUTT_ID		= GuiAreaPerms.NUM_OF_PLAYERPERMS + 2;

	//
	// FIELDS
	//
	private final	GuiCallback		callerCallback;
	private			long			permissions;
	private final	int				permPlayerId;
	private final	String			permPlayerName;
	private			boolean			updated;
	// GUI elements
	private final	GuiAreaPerms	permissGroup;
	private final	GuiLabel		doButton;

	/**
	 * Creates a new dialogue box.
	 * @param	editingPlayer	the player doing the editing (HIS own permissions affect which
	 *							permissions he can edit).
	 * @param	area			the area.
	 * @param	permPlayerId	the DBId of the player whose permissions are being edited.
	 * @param	permPlayerName	the name of the player whose permissions are being edited.
	 * @param	type			either Db.LIST_TYPE_PLAYER or Db.LIST_TYPE_GROUP
	 * @param	callback		a GuiCallback to which to report events.
	 */
	public GuiAreaPlayerEdit(Player editingPlayer, ProtArea area, int permPlayerId, String permPlayerName,
			int type, GuiCallback callback)
	{
		// construct the containing modal window (callback cannot be created and passed in the
		// call to the c'tor, as there is no context yet, until the modal window object is created)
		super(AreaProtection.plugin, Msgs.msg[Msgs.gui_areaPlayerPermsTitle], GuiDefs.GROUPTYPE_STATIC, 0, null);
		// create and set the callback
		setCallback(new DlgHandler());
		callerCallback		= callback;
		this.permPlayerId	= permPlayerId;
		this.permPlayerName	= permPlayerName;
		updated				= false;
		int	width			= getTitleBarMinWidth();
		// fit the permission panel in the window width
		if ( (GuiAreaPerms.PANEL_WIDTH + 2*GuiDefs.DEFAULT_PADDING) > width)
			width	= GuiAreaPerms.PANEL_WIDTH + 2*GuiDefs.DEFAULT_PADDING;
		// include the window border too
		setSize(width + 2 * (int)getBorderThickness(), WINDOW_HEIGHT + 2 * (int)getBorderThickness(), false);
		setPanelSize(width, PANEL_HEIGHT);

		// the AREA and PLAYER LABELS and NAMES
		// none of them can be interacted with, then we do not need to remember them
		GuiLabel label	= addTextItem(Msgs.msg[Msgs.gui_areaName], null, null);
		label.setPivot(PivotPosition.TopRight);
		label.setPosition(NAME_LBL_X, AREA_LBL_Y, false);
		label	= addTextItem(area.name, null, null);
		label.setPosition(NAME_TXT_X, AREA_LBL_Y, false);
		label	= addTextItem(Msgs.msg[Msgs.gui_playerName], null, null);
		label.setPivot(PivotPosition.TopRight);
		label.setPosition(NAME_LBL_X, PLAYER_LBL_Y, false);
		label	= addTextItem(permPlayerName, null, null);
		label.setPosition(NAME_TXT_X, PLAYER_LBL_Y, false);

		// retrieve the permissions granted to the target player for this area
		// (used to initialise the permission panel check boxes)
		Map<Integer,Long>	areaPerms	=
				Db.getAllPlayerPermissionsForArea(area.id, type);
		Long	areaPerm	= areaPerms.get(permPlayerId);
		// if this player has no special permissions for this area,
		// default to general area permissions
		permissions	= (areaPerm == null) ? permissions	= area.permissions : areaPerm;
		// retrieve the permissions granted to the player doing the editing,
		// (used to mask the permissions to which the editing player has no access)
		long	permMask	= Db.getPlayerPermissionsForArea(editingPlayer, area.id);
		// OWNERship permissions is not transferable
		if (!(Boolean)editingPlayer.getAttribute(AreaProtection.key_isAdmin) || AreaProtection.adminNoPriv)
		{
			permissions	&= ~AreaProtection.PERM_OWNER;
			permMask	&= ~AreaProtection.PERM_OWNER;
		}

		// the PERMISSIONS group
		permissGroup	= new GuiAreaPerms(permissions, permMask, false);
		addChild(permissGroup, null, null);
		permissGroup.setPosition(PERMISS_X, PERMISS_Y, false);

		// The CREATE button
		doButton	= addTextItem(Msgs.msg[Msgs.gui_editUpdate], DOBUTT_ID, null);
		doButton.setPivot(PivotPosition.Center);
		doButton.setPosition(width / 2, DO_BUTT_Y, false);
		updateDoButton();
	}

	//
	// PUBLIC METHODS
	//
	/**
	 * Returns the permissions set in the dialogue box.
	 * @return the permissions as set in the dialogue box
	 */
	public long getPermissions()	{ return permissions;	}

	/**
	 * Returns the name of player for whom permission are being edited.
	 * @return the player name.
	 */
	public String getPlayerName()	{ return permPlayerName;	}

	//********************
	// HANDLERS
	//********************

	private class DlgHandler implements GuiDefs.GuiCallback
	{
		@Override
		public void onCall(Player player, int id, Object data)
		{
			// if the id is one of the permission check boxes, toggle the permission
			if (id >= 1 && id <= GuiAreaPerms.NUM_OF_PLAYERPERMS)
			{
				permissGroup.togglePermission(id-1);
				updated	= (permissions != permissGroup.getPermissions());
				updateDoButton();
				return;
			}
			switch (id)
			{
//			case GuiDefs.ABORT_ID:		// cancel button: already dealt with in super
//				free();
//				return;
			case DOBUTT_ID:				// the DO button
				// retrieve final permissions
				permissions	= permissGroup.getPermissions();
				callerCallback.onCall(player, GuiDefs.OK_ID, permPlayerId);
				break;
			}
		}
	}

	//********************
	// PRIVATE HELPER METHODS
	//********************

	/**
	 * Activates / de-activates the DO button according to dialogue box conditions
	 * Activation requires something to have been updated.
	 */
	private void updateDoButton()
	{
		doButton.setColor(updated ? GuiDefs.ACTIVE_COLOUR : GuiDefs.INACTIVE_COLOUR);
		doButton.setClickable(updated);
	}

}
