/****************************
	A r e a P r o t e c t i o n  -  A Rising World Java plug-in for area permissions.

	GuiAreaEdit.java - A dialogue box to edit area properties.

	Created by : Maurizio M. Gavioli 2017-03-07

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
import org.miwarre.ap.gui.GuiModalWindow;
import org.miwarre.ap.gui.GuiTitleBar;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.gui.GuiTextField;
import net.risingworld.api.gui.PivotPosition;
import net.risingworld.api.objects.Player;
import net.risingworld.api.utils.Utils;
import net.risingworld.api.utils.Vector3f;

class GuiAreaEdit extends GuiModalWindow
{
	// some general dimensions
	private static final	int		NAME_TXT_WIDTH	= 300;
	// Control positions: X
	private static final	int		NAME_LBL_X		= 150;
	private static final	int		NAME_TXT_X		= (NAME_LBL_X + GuiDefs.DEFAULT_PADDING);
	private static final	int		HEIGHT_TOP_X	= GuiDefs.DEFAULT_PADDING;
	private static final	int		HEIGHT_TOP_TXT_X= HEIGHT_TOP_X + 100;
	private static final	int		HEIGHT_BOT_X	= HEIGHT_TOP_TXT_X + 75;
	private static final	int		HEIGHT_BOT_TXT_X= HEIGHT_BOT_X + 100;
	private static final	int		DEFAULT_BUTT_X	= HEIGHT_BOT_TXT_X + 75;
	private static final	int		PERMISS_X		= GuiDefs.DEFAULT_PADDING;
	private static final	int		PLAYERS_BUTT_X	= GuiDefs.DEFAULT_PADDING;
	private static final	int		GROUPS_BUTT_X	= PLAYERS_BUTT_X + 100;
	private static final	int		EXTENT_BUTT_X	= GROUPS_BUTT_X +100;
	// Control positions: Y
	private static final	int		PANEL_HEIGHT	=
			GuiDefs.TEXTENTRY_HEIGHT + GuiAreaPerms.PANEL_HEIGHT + GuiDefs.ITEM_SIZE * 4 + GuiDefs.DEFAULT_PADDING * 5;
//			the area name row			the permission group	1 row of 1-line button & 1 row of 3-line button + and 5 paddings between the rows
	private static final	int		WINDOW_HEIGHT	= GuiTitleBar.TITLEBAR_HEIGHT + PANEL_HEIGHT;
	private static final	int		NAME_LBL_Y		= PANEL_HEIGHT - GuiDefs.DEFAULT_PADDING;
	private static final	int		NAME_TXT_Y		= NAME_LBL_Y + 4;
	private static final	int		PERMISS_Y		= NAME_TXT_Y - GuiDefs.TEXTENTRY_HEIGHT - GuiDefs.DEFAULT_PADDING;
	private static final	int		PLAYERS_BUTT_Y	= PERMISS_Y - GuiAreaPerms.PANEL_HEIGHT - GuiDefs.DEFAULT_PADDING;
	private static final	int		GROUPS_BUTT_Y	= PLAYERS_BUTT_Y;
	private static final	int		EXTENT_BUTT_Y	= PLAYERS_BUTT_Y;
	private static final	int		HEIGHT_LBL_Y	= PLAYERS_BUTT_Y;
	private static final	int		HEIGHT_TXT_Y	= HEIGHT_LBL_Y + 4;
	private static final	int		DO_BUTT_Y		= GROUPS_BUTT_Y - GuiDefs.DEFAULT_PADDING - (GuiDefs.ITEM_SIZE * 5) / 2;
	// Id's of controls
	// the first GuiAreaPerms.NUM_OF_PERMS id's are reserved for permission check boxes
	// but starting from 1, rather from 0 (=> +1); other controls resume from next id (=> +2)
	private static final	int		NAMETEXT_ID		= GuiAreaPerms.NUM_OF_AREAPERMS + 2;
	private static final	int		PLAYERSBUTT_ID	= NAMETEXT_ID + 1;
	private static final	int		GROUPSBUTT_ID	= PLAYERSBUTT_ID + 1;
	private static final	int		EXTENTBUTT_ID	= GROUPSBUTT_ID + 1;
	private static final	int		HEIGHTTOPTEXT_ID= EXTENTBUTT_ID + 1;
	private static final	int		HEIGHTBOTTEXT_ID= HEIGHTTOPTEXT_ID + 1;
	private static final	int		DEFAULTBUTT_ID	= HEIGHTBOTTEXT_ID + 1;
	private static final	int		DOBUTT_ID		= DEFAULTBUTT_ID + 1;

	public  static final	int		TYPE_CREATE		= 0;
	public  static final	int		TYPE_EDIT		= 1;

	//
	// FIELDS
	//
	private			ProtArea		area;
//	private			int				heightTop, heightBottom;
	private			ProtArea.Extent	extent;
	private			String			name;
	private final	NewAreaCreation	nac;
	private final	int				type;
	private			boolean			updated;
	// GUI elements
	private			GuiTextField	heightTopText, heightBotText;
	private final	GuiTextField	nameText;
	private final	GuiAreaPerms	permissGroup;
	private final	GuiLabel		doButton;

	/**
	 * Creates a new dialogue box to edit area properties.
	 * @param	nac		a NewAreaCreation procedure; only used when creating a new area;
	 *					use null while editing an existing area
	 * @param	area	the area to display the properties for
	 * @param	player	the player entitled to the edit; used to determine which permissions
	 *					can be edited
	 * @param	type	either TYPE_CREATE when creating a new area or TYPE_EDIT when editing
	 *					an existing area
	 */
	public GuiAreaEdit(NewAreaCreation nac, ProtArea area, Player player, int type)
	{
		// construct the containing modal window (callback cannot be created and passed in the
		// call to the c'tor, as there is no context yet, until the modal window object is created)
		super(AreaProtection.plugin, Msgs.msg[Msgs.gui_editTitle], GuiDefs.GROUPTYPE_STATIC, 0, null);
		// create and set the callback
		setCallback(new DlgHandler());
		this.area	= area;
		this.nac	= nac;
		this.type	= type;
		updated		= false;
		int	width	= getTitleBarMinWidth();
		// fit the permission panel in the window width
		if ( (GuiAreaPerms.PANEL_WIDTH + 2*GuiDefs.DEFAULT_PADDING) > width)
			width	= GuiAreaPerms.PANEL_WIDTH + 2*GuiDefs.DEFAULT_PADDING;
		// include the window border too
		setSize(width + 2 * (int)getBorderThickness(), WINDOW_HEIGHT + 2 * (int)getBorderThickness(), false);
		setPanelSize(width, PANEL_HEIGHT);

		// the NAME and its text field
		GuiLabel label	= addTextItem(Msgs.msg[Msgs.gui_editName], null, null);
		label.setPivot(PivotPosition.TopRight);
		label.setPosition(NAME_LBL_X, NAME_LBL_Y, false);
		nameText= new GuiTextField(NAME_TXT_X, NAME_TXT_Y, false, NAME_TXT_WIDTH, GuiDefs.TEXTENTRY_HEIGHT, false);
		// if an area name is given, cache it locally and copy it in the text entry field
		// (happens when the dialogue is used to edit an existing area)
		name	= (area.name != null ? area.name : "");
		nameText.setText(name);
		addChild(nameText, NAMETEXT_ID, null);

		// if player is admin, he has access to all permissions
		long	permMask	= (Boolean)player.getAttribute(AreaProtection.key_isAdmin) ? AreaProtection.PERM_ALL
				// otherwise, use standard permission mask
				: Db.getPlayerPermissionsForArea(player, area.id);
		// if editing and player is neither admin or owner, he can only manage players
		if (type == TYPE_EDIT && !(Boolean)player.getAttribute(AreaProtection.key_isAdmin)
					&& (permMask & AreaProtection.PERM_OWNER) == 0)
			permMask	&= AreaProtection.PERM_ADDPLAYER;

		// the PERMISSIONS group
		permissGroup	= new GuiAreaPerms(area.permissions, permMask, true);
		addChild(permissGroup, null, null);
		permissGroup.setPosition(PERMISS_X, PERMISS_Y, false);

		extent	= area.getExtent();
		GuiLabel	button;
		if (type != TYPE_CREATE)
		{
			// The EDIT PLAYERS / GROUPS buttons
			boolean	addPlayers	= (permMask & AreaProtection.PERM_ADDPLAYER) != 0;
			button	= addTextItem(Msgs.msg[Msgs.gui_editeditPlayers],
					addPlayers ? PLAYERSBUTT_ID : null, null);
			button.setPosition(PLAYERS_BUTT_X, PLAYERS_BUTT_Y, false);
			button.setColor(addPlayers ? GuiDefs.ACTIVE_COLOUR : GuiDefs.INACTIVE_COLOUR);
			button	= addTextItem(Msgs.msg[Msgs.gui_editeditGroups],
					addPlayers ? GROUPSBUTT_ID : null, null);
			button.setColor(addPlayers ? GuiDefs.ACTIVE_COLOUR : GuiDefs.INACTIVE_COLOUR);
			button.setPosition(GROUPS_BUTT_X, GROUPS_BUTT_Y, false);
		}
		// The EDIT EXTENT button
		button	= addTextItem(Msgs.msg[Msgs.gui_editeditExtent], EXTENTBUTT_ID, null);
		button.setColor(GuiDefs.ACTIVE_COLOUR);
		button.setPosition(EXTENT_BUTT_X, EXTENT_BUTT_Y, false);

		// The CREATE/UPDATE button
		doButton	= addTextItem(type == TYPE_CREATE ? Msgs.msg[Msgs.gui_editCreate] :
			Msgs.msg[Msgs.gui_editUpdate], DOBUTT_ID, null);
		doButton.setPivot(PivotPosition.Center);
		doButton.setPosition(width / 2, DO_BUTT_Y, false);
		updateDoButton();
	}

	//********************
	// HANDLERS
	//********************

	private class DlgHandler implements GuiDefs.GuiCallback
	{
		@Override
		public void onCall(Player player, int id, Object data)
		{
			// if the id is one of the permission check boxes, toggle the permission
			if (id >= 1 && id <= GuiAreaPerms.NUM_OF_AREAPERMS)
			{
				permissGroup.togglePermission(id-1);
				updated	= true;
				updateDoButton();
				return;
			}
			switch (id)
			{
//			case GuiDefs.ABORT_ID:		// cancel button: already dealt with in super
//				free();
//				return;
			case NAMETEXT_ID:			// name text entry field
				if (data != null)		// if a string is given, store it
				{						// (no string is passed if the text field is just clicked on)
					if ( ((String)data).equals(name) )
						return;
					name	= (String)data;
					updated	= true;
					updateDoButton();
				}
				break;
			case HEIGHTTOPTEXT_ID:
				if (data == null)
					return;
				int	value;
				try	{	value	= Integer.valueOf((String)data);	}
				catch (NumberFormatException e)
				{		value = extent.getMaxY();	}
				extent.setMaxY(value);
				break;
			case HEIGHTBOTTEXT_ID:
				if (data == null)
					return;
				try	{	value	= Integer.valueOf((String)data);	}
				catch (NumberFormatException e)
				{		value = extent.getMinY();	}
				extent.setMinY(value);
				break;
			case DEFAULTBUTT_ID:
				extent.setMaxY(AreaProtection.heightTop);
				extent.setMinY(AreaProtection.heightBottom);
				heightTopText.setText(Integer.toString(AreaProtection.heightTop));
				heightBotText.setText(Integer.toString(AreaProtection.heightBottom));
				break;
			case PLAYERSBUTT_ID:		// the player management button
				// open a dialogue box to manage the player with special permissions for this area
				push(player, new GuiPlayersEdit(area, Db.LIST_TYPE_PLAYER));
				updated	= true;
				updateDoButton();
				break;
			case GROUPSBUTT_ID:			// the group management button
				// open a dialogue box to manage the player with special permissions for this area
				push(player, new GuiPlayersEdit(area, Db.LIST_TYPE_GROUP));
				updated	= true;
				updateDoButton();
				break;
			case EXTENTBUTT_ID:
				push(player, new GuiAreaExtentEdit(area.getExtent(), area.getName(), player, new ExtentHandler()));
				break;
			case DOBUTT_ID:				// the DO button (CREATE / UPDATE)
				// store new/updated name & permissions in the area
				area.name			= name;
				area.permissions	= permissGroup.getPermissions();
/*				// height range
				if (type == TYPE_CREATE)
				{
					// TODO : retrieve updated height boundaries
					int	heightTopChunk	= Utils.ChunkUtils.getChunkPositionY(heightTop);
					int	heightTopBlock	= Utils.ChunkUtils.getBlockPositionY(heightTop, heightTopChunk);
					int	heightBtmChunk	= Utils.ChunkUtils.getChunkPositionY(heightBottom);
					int	heightBtmBlock	= Utils.ChunkUtils.getBlockPositionY(heightBottom, heightBtmChunk);
					// if different from current height boundaries...
					Vector3i	fromChunk	= area.getStartChunkPosition();
					Vector3i	fromBlock	= area.getStartBlockPosition();
					Vector3i	toChunk		= area.getEndChunkPosition();
					Vector3i	toBlock		= area.getEndBlockPosition();
					if (heightTopChunk != toChunk.y || heightTopBlock != toBlock.y ||
							heightBtmChunk != fromChunk.y || heightBtmBlock != fromBlock.y)
					{
						// create a new area (as area boudaries cannot be changed once created)
						fromChunk.y	= heightBtmChunk;
						fromBlock.y	= heightBtmBlock;
						toChunk.y	= heightTopChunk;
						toBlock.y	= heightTopBlock;
						ProtArea	newArea	= new ProtArea(Utils.ChunkUtils.getGlobalPosition(fromChunk, fromBlock),
								Utils.ChunkUtils.getGlobalPosition(toChunk, toBlock), area.name, area.permissions);
						area	= newArea;
					}
				}*/
				checkAreaExtent(extent);
				// if creating a new area, stop the NewAreaCreation procedure
				if (type == TYPE_CREATE && nac != null)
				{
					nac.stopRun();
					Db.addArea(area);
				}
				// if editing an existing area, save its data
				else if (type == TYPE_EDIT)
					Db.updateArea(area);
				pop(player);
				break;
			}
		}
	}

	private class ExtentHandler implements GuiDefs.GuiCallback
	{
		@Override
		public void onCall(Player player, int id, Object data)
		{
			if (id == GuiDefs.OK_ID)
			{
				extent	= (ProtArea.Extent)data;
				updated	= true;
				updateDoButton();
			}
		}
	}

	//********************
	// PRIVATE HELPER METHODS
	//********************

	/**
	 * Activates / de-activates the DO button according to dialogue box conditions
	 * Activation requires an area name to exists and something to have been updated.
	 */
	private void updateDoButton()
	{
		boolean	complete	= updated && name.length() > 0;
		doButton.setColor(complete ? GuiDefs.ACTIVE_COLOUR : GuiDefs.INACTIVE_COLOUR);
		doButton.setClickable(complete);
	}

	/**
	 * Checks whether the area extent is different from `extent` and, in case,
	 * adjust the area extent to correspond to it.
	 * <p>(Currently, it creates a new area with the given `extent` and all other
	 * current properties of `area`, as area extent cannot be changed once created.)
	 * @param extent the extent to check
	 */
	private void checkAreaExtent(ProtArea.Extent extent)
	{
		Vector3f	areaFrom, areaTo, extentFrom, extentTo;
		areaFrom	= Utils.ChunkUtils.getGlobalPosition(area.getStartChunkPosition(), area.getStartBlockPosition());
		areaTo		= Utils.ChunkUtils.getGlobalPosition(area.getEndChunkPosition(), area.getEndBlockPosition());
		extentFrom	= extent.getFrom();
		extentTo	= extent.getTo();
		if (areaFrom.x != extentFrom.x || areaFrom.y != extentFrom.y || areaFrom.z != extentFrom.z ||
			areaTo.x != extentTo.x || areaTo.y != extentTo.y || areaTo.z != extentTo.z)
		{
			// create a new area (as area boudaries cannot be changed once created)
			ProtArea	newArea	= new ProtArea(extentFrom, extentTo, area.name, area.permissions);
			newArea.id			= area.id;
			newArea.players		= area.players;
			newArea.groups		= area.groups;
			area	= newArea;
		}
	}
}
