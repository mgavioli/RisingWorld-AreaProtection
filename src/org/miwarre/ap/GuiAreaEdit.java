/****************************
	A r e a P r o t e c t i o n  -  A Rising World Java plug-in for area permissions.

	GuiAreaEdit.java - A dialogue box to edit area properties.

	Created by : Maurizio M. Gavioli 2017-03-07

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2017
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package org.miwarre.ap;

import org.miwarre.ap.gui.GuiDefs;
import org.miwarre.ap.gui.GuiModalWindow;
import org.miwarre.ap.gui.GuiTitleBar;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.gui.GuiTextField;
import net.risingworld.api.gui.PivotPosition;
import net.risingworld.api.objects.Player;

public class GuiAreaEdit extends GuiModalWindow
{
	// some general dimensions
	private static final	int		NAME_TXT_WIDTH	= 300;
	// Control positions: X
	private static final	int		NAME_LBL_X		= 150;
	private static final	int		NAME_TXT_X		= (NAME_LBL_X + GuiDefs.DEFAULT_PADDING);
	private static final	int		PERMISS_X		= GuiDefs.DEFAULT_PADDING;
	private static final	int		PLAYERS_BUTT_X	= GuiDefs.DEFAULT_PADDING;
	private static final	int		GROUPS_BUTT_X	= NAME_TXT_X;
	// Control positions: Y
	private static final	int		PANEL_HEIGHT	=
			GuiDefs.TEXTENTRY_HEIGHT + GuiAreaPerms.PANEL_HEIGHT + GuiDefs.ITEM_SIZE * 4 + GuiDefs.DEFAULT_PADDING * 5;
//			the area name row			the permission group	1 row of 1-line button & 1 row of 3-line button + and 5 paddings between the rows
	private static final	int		WINDOW_HEIGHT	= GuiTitleBar.TITLEBAR_HEIGHT + PANEL_HEIGHT; 
	private static final	int		NAME_LBL_Y		= PANEL_HEIGHT - GuiDefs.DEFAULT_PADDING;
	private static final	int		NAME_TXT_Y		= NAME_LBL_Y;
	private static final	int		PERMISS_Y		= NAME_TXT_Y - GuiDefs.TEXTENTRY_HEIGHT - GuiDefs.DEFAULT_PADDING;
	private static final	int		PLAYERS_BUTT_Y	= PERMISS_Y - GuiAreaPerms.PANEL_HEIGHT - GuiDefs.DEFAULT_PADDING;
	private static final	int		GROUPS_BUTT_Y	= PLAYERS_BUTT_Y;
	private static final	int		DO_BUTT_Y		= GROUPS_BUTT_Y - GuiDefs.DEFAULT_PADDING - (GuiDefs.ITEM_SIZE * 5) / 2;
	// Id's of controls
	// the first GuiAreaPerms.NUM_OF_PERMS id's are reserved for permission check boxes
	// but starting from 1, rather from 0 (=> +1); other controls resume from next id (=> +2)
	private static final	int		NAMETEXT_ID		= GuiAreaPerms.NUM_OF_AREAPERMS + 2;
	private static final	int		PLAYERSBUTT_ID	= NAMETEXT_ID + 1;
	private static final	int		GROUPSBUTT_ID	= PLAYERSBUTT_ID + 1;
	private static final	int		DOBUTT_ID		= GROUPSBUTT_ID + 1;

	public  static final	int		TYPE_CREATE		= 0;
	public  static final	int		TYPE_EDIT		= 1;

	//
	// FIELDS
	//
	private	ProtArea		area;
	private	String			name;
	private NewAreaCreation	nac;
	private	int				type;
	private	boolean			updated;
	// GUI elements
	private GuiTextField	nameText;
	private GuiAreaPerms	permissGroup;
	private	GuiLabel		playersButton;
	private GuiLabel		groupsButton;
	private GuiLabel		doButton;

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
		super(AreaProtection.plugin, Msgs.msg[Msgs.gui_editTitle], GuiDefs.GROUPTYPE_STATIC, null);
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

		// if creating a new area and player is admin, he has access to all permissions
		int	permMask	= (type == TYPE_CREATE && player.isAdmin()) ? AreaProtection.PERM_ALL
				// otherwise, use standard permission mask
				: Db.getPlayerPermissionsForArea(player, area.id);
		// if editing and player is neither admin or owner, he can only manage players
		if (type == TYPE_EDIT && (!player.isAdmin() || AreaProtection.adminNoPriv) && (permMask & AreaProtection.PERM_OWNER) == 0)
			permMask	&= AreaProtection.PERM_ADDPLAYER;

		// the PERMISSIONS group
		permissGroup	= new GuiAreaPerms(area.permissions, permMask, true);
		addChild(permissGroup, null, null);
		permissGroup.setPosition(PERMISS_X, PERMISS_Y, false);

		// The EDIT PLAYERS / GROUPS buttons
		boolean	addPlayers	= (permMask & AreaProtection.PERM_ADDPLAYER) != 0;
		playersButton	= addTextItem(Msgs.msg[Msgs.gui_editeditPlayers],
				addPlayers ? PLAYERSBUTT_ID : null, null);
		playersButton.setPosition(PLAYERS_BUTT_X, PLAYERS_BUTT_Y, false);
		playersButton.setColor(addPlayers ? GuiDefs.ACTIVE_COLOUR : GuiDefs.INACTIVE_COLOUR);
		// group management button is currently de-activated
		groupsButton	= addTextItem(Msgs.msg[Msgs.gui_editeditGroups],
				addPlayers ? GROUPSBUTT_ID : null, null);
		groupsButton.setColor(addPlayers ? GuiDefs.ACTIVE_COLOUR : GuiDefs.INACTIVE_COLOUR);
		groupsButton.setPosition(GROUPS_BUTT_X, GROUPS_BUTT_Y, false);
		// only show PLAYERS and GROPS buttons while editing, not while creating
		if (type == TYPE_CREATE)
		{
			playersButton.setVisible(false);
			groupsButton.setVisible(false);
		}

		// The CREATE button
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
			case PLAYERSBUTT_ID:		// the player management button
				// open a dialogue box to manage the player with special permissions for this area
				push(player, new GuiPlayersEdit(area, Db.LIST_TYPE_PLAYER));
				updated	= true;
				updateDoButton();
				break;
			// THE GROUP MANAGEMENT IS CURRENTLY DISABLED
			case GROUPSBUTT_ID:			// the group management button
				// open a dialogue box to manage the player with special permissions for this area
				push(player, new GuiPlayersEdit(area, Db.LIST_TYPE_GROUP));
				updated	= true;
				updateDoButton();
				break;
			case DOBUTT_ID:				// the DO button (CREATE / UPDATE)
				// store new/updated name & permissions in the area
				area.name			= name;
				area.permissions	= permissGroup.getPermissions();
				// if creating a new area, stop the NewAreaCreation procedure
				if (type == TYPE_CREATE && nac != null)
					nac.stopRun();
				// if editing an existing area, save its data
				else if (type == TYPE_EDIT)
					Db.updateArea(area);
				pop(player);
				break;
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
		doButton.setClickable(complete ? true : false);
	}

}
