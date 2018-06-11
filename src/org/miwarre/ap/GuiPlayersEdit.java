/****************************
	A r e a P r o t e c t i o n  -  A Rising World Java plug-in for area permissions.

	GuiPlayersEdit.java - GuiPanel specialised to edit players for an area.

	Created by : Maurizio M. Gavioli 2017-03-14

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2017
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package org.miwarre.ap;

import java.util.Map.Entry;
import org.miwarre.ap.gui.GuiDefs;
import org.miwarre.ap.gui.GuiDefs.GuiCallback;
import org.miwarre.ap.gui.GuiGroupStatic;
import org.miwarre.ap.gui.GuiModalWindow;
import org.miwarre.ap.gui.GuiScrollList;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.gui.PivotPosition;
import net.risingworld.api.objects.Player;

/**
 * A Dialogue Box to manage the players or groups associated to an area.
 * <p>Relies on GuiDefs.GuiModalWindow.
 */
public class GuiPlayersEdit extends GuiModalWindow
{
	//
	// CONSTANTS
	//
	private static final	int	MAX_NUM_OF_ROWS		= 10;
	private static final	int	UNKNOWN_ID			= -1;
	// An ID which is unlikely to be exceeded by the player list
	private static final	int	MAX_PLAYER_DBID	= 1000000;
	// for the dialogue box controls, use ID's which do not clashes with player ID's
	private static final	int	ADDBUTT_ID		= MAX_PLAYER_DBID + 1;
	private static final	int	EDITBUTT_ID		= MAX_PLAYER_DBID + 2;
	private static final	int	DELETEBUTT_ID	= MAX_PLAYER_DBID + 3;

	//
	// FIELDS
	//
	private	ProtArea		area;
	private	int				selPlayerIdx;	// data for the selected player (if any)
	private	String			selPlayerName;
	// GUI elements
	private	GuiLabel		addButt;
	private	GuiLabel		nameLabel;
	private	GuiLabel		deleteButt;
	private	GuiLabel		editButt;
	private	int				nextPlayerId;
	private	GuiAreaPlayerEdit	playerEditor;
	private GuiScrollList	playerList;
	private	GuiTwoListsSelector	playerSelector;
	private	int				type;

	public GuiPlayersEdit(ProtArea area, int type)
	{
		super(AreaProtection.plugin, Msgs.msg[type == Db.LIST_TYPE_PLAYER ?
					Msgs.gui_specPermPlayersTitle :
					Msgs.gui_specPermGroupsTitle],
				GuiDefs.GROUPTYPE_NONE, null);
		setCallback(new DlgHandler());
		this.area	= area;
		this.type	= type;
		nextPlayerId= 1;			// the next player added to the list will have ID = 1
		selPlayerIdx= UNKNOWN_ID;
		selPlayerName= null;
		setPanel(new PlayersEditPanel(area.name));
	}

	//********************
	// HANDLERS
	//********************

	/**
	 * Handles the notifications from the GuiPlayersEdit window
	 */
	private class DlgHandler implements GuiCallback
	{
		@Override
		public void onCall(Player player, int id, Object data)
		{
			// if id is for one of the player names, select it
			if (id >= 1 && id <= MAX_PLAYER_DBID)
			{
				selPlayerIdx	= id;
				selPlayerName	= (String)data;
				playerList.selectItem(selPlayerIdx);
				updateButtons();
				return;
			}
			switch (id)
			{
			case ADDBUTT_ID:			// "ADD a player" button
				// open a user/preset selection dialogue box, allowing to select one
				// player and one permissions preset and managing the actual addition
				playerSelector	= new GuiTwoListsSelector(AreaProtection.plugin,
						Msgs.msg[type == Db.LIST_TYPE_PLAYER ?
								Msgs.gui_selectPlayerTitle :
								Msgs.gui_selectGroup],
						new SelectorHandler(), area, MAX_NUM_OF_ROWS, type);
				push(player, playerSelector);
				break;
			case EDITBUTT_ID:			// "EDIT player permissions" button
				if (selPlayerIdx != UNKNOWN_ID && selPlayerName != null)
				{
					playerEditor	= new GuiAreaPlayerEdit(player, area,
							selPlayerName, type, new PlayerHandler());
					push(player, playerEditor);
				}
				break;
			case DELETEBUTT_ID:			// "DELETE player" button
				if (selPlayerIdx != UNKNOWN_ID && selPlayerName != null)
				{
					Db.removePlayerFromArea(area, selPlayerName, type);
					playerList.removeTextItem(selPlayerIdx);
					selPlayerIdx	= UNKNOWN_ID;
				}
				updateButtons();
				break;
			}
		}
	}

	/**
	 * Manages notifications from the GuiTwoListsSelector dialogue box.
	 */
	private class SelectorHandler implements GuiCallback
	{
		@Override
		public void onCall(Player player, int id, Object name)
		{
			// GuiTwoListsSelector dialogue box notifies OK_ID when its "DO" button is pressed
			if (id == GuiDefs.OK_ID && playerSelector != null)
			{
				// retrieve the name of the player and of the preset
				String	playerName	= playerSelector.getList1SelectedItem();
				String	presetName	= playerSelector.getList2SelectedItem();
				// check both are valid
				if (playerName != null && playerName.length() > 0 &&
						presetName != null && presetName.length() > 0)
				{
					// add the new pair to the DB
					Integer	permissions	= AreaProtection.presets.get(presetName);
					if (permissions != null)
					{
						// OWNERship is not transferable
						if (!player.isAdmin() || AreaProtection.adminNoPriv)
							permissions	&= ~(AreaProtection.PERM_OWNER);
						Db.addPlayerToArea(area, playerName, permissions, type);
					}
					// add it to the shown list too
					String	txt	= playerName + " (" + presetName + ")";
					playerList.addTextItem(txt, nextPlayerId, playerName);
					nextPlayerId++;
					// remove the GuiTwoListsSelector dialogue box
					playerSelector.pop(player);
					playerSelector	= null;
					updateButtons();
				}
			}
		}
	}

	/**
	 * Manages notifications from the GuiAreaPlayerEdit dialogue box.
	 */
	private class PlayerHandler implements GuiCallback
	{
		@Override
		public void onCall(Player player, int id, Object permPlayerName)
		{
			// GuiAreaPlayerEdit dialogue box notifies OK_ID when its "DO" button is pressed
			if (id == GuiDefs.OK_ID && playerEditor != null)
			{
				int	permissions	= playerEditor.getPermissions();
				// add the edited player name / permission pair to the DB
				// OWNERship is not transferable
				if (!player.isAdmin() || AreaProtection.adminNoPriv)
					permissions	&= ~(AreaProtection.PERM_OWNER);
				Db.addPlayerToArea(area, (String)permPlayerName, permissions, type);
				String	txt	= permPlayerName +
						" (" + AreaProtection.getPresetNameFromPermissions(permissions) + ")";
				playerList.setItemText(selPlayerIdx, txt);
				// remove the GuiAreaPlayerEdit dialogue box
				playerEditor.pop(player);
				playerEditor	= null;
			}
		}
	}

	//********************
	// PRIVATE HELPER METHODS
	//********************

	/**
	 * Updates the colour and activity of the delete and edit button, according
	 * a player is selected or not.
	 */
	private void updateButtons()
	{
		boolean	hasSelected	= (selPlayerIdx != UNKNOWN_ID);
		editButt.setColor(hasSelected ? GuiDefs.ACTIVE_COLOUR : GuiDefs.INACTIVE_COLOUR);
		editButt.setClickable(hasSelected ? true : false);
		deleteButt.setColor(hasSelected ? GuiDefs.ACTIVE_COLOUR : GuiDefs.INACTIVE_COLOUR);
		deleteButt.setClickable(hasSelected ? true : false);
	}

	//********************
	// PLAYERSEDITPANEL CLASS
	//********************

	/**
	 * A private class for the main panel of the GuiPlayersEdit window.
	 */
	class PlayersEditPanel extends GuiGroupStatic
	{
		public PlayersEditPanel(String areaName)
		{
			super(0);
			setMargin(GuiDefs.DEFAULT_PADDING);
			// The AREA NAME
			nameLabel	= addTextItem(areaName, null, null);
			// The PLAYER LIST
			playerList	= new GuiScrollList(MAX_NUM_OF_ROWS, true);
			addChild(playerList, null, null);
			playerList.setBorderThickness(1, false);
			playerList.setMargin(GuiDefs.DEFAULT_PADDING);
			for (Entry<String,Integer> entry :
				(type == Db.LIST_TYPE_PLAYER ? area.players : area.groups).entrySet())
			{
				String	playerName	= entry.getKey();
				String	txt	= playerName +
						" (" + AreaProtection.getPresetNameFromPermissions(entry.getValue()) + ")";
				playerList.addTextItem(txt, nextPlayerId, playerName);
				nextPlayerId++;
			}

			// The BUTTON ROW
			addButt		= addTextItem(Msgs.msg[Msgs.gui_editAdd], ADDBUTT_ID, null);
			addButt.setPivot(PivotPosition.Center);
			addButt.setColor(GuiDefs.ACTIVE_COLOUR);
			editButt	= addTextItem(Msgs.msg[Msgs.gui_editEdit], EDITBUTT_ID,  null);
			editButt.setPivot(PivotPosition.Center);
			editButt.setColor(GuiDefs.INACTIVE_COLOUR);
			deleteButt	= addTextItem(Msgs.msg[Msgs.gui_editDelete], DELETEBUTT_ID, null);
			deleteButt.setPivot(PivotPosition.Center);
			deleteButt.setColor(GuiDefs.INACTIVE_COLOUR);
		}

		/**
		 * Arranges the elements inside the panel.
		 * <p>Called by the layout() method of the GuiModalwindow the panel
		 * belongs to, before showing it to a player.
		 * 
		 * @param	minWidth	the minimum width required by the context within which the
		 *						group is placed; use 0 if there is no external constrains.
		 * @param	minHeight	the minimum height required by the context within which the
		 *						group is placed; use 0 if there is no external constrains.
		 */
		@Override
		public void layout(int minWidth, int minHeight)
		{
			// first, layout the player list, to have its sizes
			playerList.layout(minWidth, minHeight);
			int	margin	= getMargin();
			int	padding	= getPadding();
			// from player list sizes, compute panel total sizes
			int	width	= (int)(playerList.getWidth() + 2*margin);
			if (minWidth > width)
				width	= minWidth;
			int	height	= 2*margin +
					(int)playerList.getHeight() + 4*GuiDefs.ITEM_SIZE + 3*GuiDefs.DEFAULT_PADDING;
//					the height of the player list	name+button row			paddings
			if (minHeight > height)
				height	= minHeight;
			setSize(width, height, false);
			// position the individual children
			height	-= margin;									// move below top margin
			nameLabel.setPosition(margin, height, false);
			height	-= margin + GuiDefs.ITEM_SIZE + padding;	// move below the name label
			playerList.setPosition(margin, height, false);
			height	-= playerList.getHeight() + padding			// move below the player list
					+ (GuiDefs.ITEM_SIZE * 3) / 2;					// & to the middle of buttons
			// position the buttons
			addButt   .setPosition(width/4,     height, false);
			editButt  .setPosition(width/2,     height, false);
			deleteButt.setPosition((width/4)*3, height, false);
		}
	}

}
