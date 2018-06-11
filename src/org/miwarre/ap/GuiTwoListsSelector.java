/****************************
	A r e a P r o t e c t i o n  -  A Rising World Java plug-in for area permissions.

	GuiTwoListsSelector.java - A window to select a pair of items in two lists.

	Created by : Maurizio M. Gavioli 2017-03-15

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2017
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package org.miwarre.ap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;
import org.miwarre.ap.gui.GuiDefs;
import org.miwarre.ap.gui.GuiGroupStatic;
import org.miwarre.ap.gui.GuiDefs.GuiCallback;
import org.miwarre.ap.gui.GuiModalWindow;
import org.miwarre.ap.gui.GuiScrollList;
import net.risingworld.api.Plugin;
import net.risingworld.api.database.WorldDatabase;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.gui.PivotPosition;
import net.risingworld.api.objects.Player;

public class GuiTwoListsSelector extends GuiModalWindow
{
	//
	// CONSTANTS
	//
	public static final	int		DOBUTTON_ID		= 20000;
	public static final	int		MIN_PRESET_ID	= 10000;

	//
	// FIELDS
	//
	private GuiCallback		callerCallback;
	private GuiLabel		doButton;
	private TwoListPanel	panel;
	private String			selected1, selected2;

	public GuiTwoListsSelector(Plugin plugin, String title, GuiCallback callback, ProtArea area,
			int maxLines, int type)
	{
		super(plugin, title, GuiDefs.GROUPTYPE_NONE, null);
		setCallback(new DlgHandler());
		callerCallback	= callback;
		panel	= new TwoListPanel(area, maxLines, type);
		setPanel(panel);
		selected1	= "";
		selected2	= "";
	}

	//********************
	// PUBLIC METHODS
	//********************

	public String getList1SelectedItem()
	{
		if (selected1 != null && selected1.length() > 0)
			return selected1;
		else
			return null;
	}

	public String getList2SelectedItem()
	{
		if (selected2 != null && selected2.length() > 0)
			return selected2;
		else
			return null;
	}

	//********************
	// HANDLERS
	//********************

	private class DlgHandler implements GuiDefs.GuiCallback
	{
		@Override
		public void onCall(Player player, int id, Object data)
		{
			if (id >= 1 && id < MIN_PRESET_ID)
			{
				selected1	= (String)data;
				panel.list1.selectItem(id);
				updateDoButton();
				return;
			}
			if (id >= MIN_PRESET_ID && id < DOBUTTON_ID)
			{
				selected2	= (String)data;
				panel.list2.selectItem(id);
				updateDoButton();
				return;
			}
			if (id == DOBUTTON_ID)
			{
				callerCallback.onCall(player, GuiDefs.OK_ID, null);
			}
		}
	}
	//********************
	// PRIVATE HELPER METHODS
	//********************

	private void updateDoButton()
	{
		boolean	complete	= selected1.length() > 0 && selected2.length() > 0;
		doButton.setColor(complete ? GuiDefs.ACTIVE_COLOUR : GuiDefs.INACTIVE_COLOUR);
		doButton.setClickable(complete ? true : false);
	}

	//********************
	// PANEL CLASS
	//********************

	/**
	 * A private class for the main panel of the GuiTwoListsSelector window.
	 */
	class TwoListPanel extends GuiGroupStatic
	{
		GuiLabel		list1Head;
		GuiLabel		list2Head;
		GuiScrollList	list1;
		GuiScrollList	list2;

		public TwoListPanel(ProtArea area, int maxLines, int type)
		{
			super(0);
			setMargin(GuiDefs.DEFAULT_PADDING);
			// The list HEADINGS
			list1Head	= addTextItem(
					Msgs.msg[type == Db.LIST_TYPE_PLAYER ? Msgs.gui_selectPlayer : Msgs.gui_selectGroup],
					null, null);
			list2Head	= addTextItem(Msgs.msg[Msgs.gui_selectPreset], null, null);
			// The PLAYER/GROUP LIST
			list1	= new GuiScrollList(maxLines, true);
			addChild(list1, null, null);
			list1.setBorderThickness(1, false);
			list1.setMargin(GuiDefs.DEFAULT_PADDING);
			if (type == Db.LIST_TYPE_PLAYER)
			{
				// Query world data base for known players
				WorldDatabase	db = AreaProtection.plugin.getWorldDatabase();
				try(ResultSet result = db.executeQuery("SELECT `ID`,`Name` FROM `Player` ORDER BY `Name`ASC"))
				{
					while(result.next())
					{
						int		id		= result.getInt(1);
						String	name	= result.getString(2);
						// add the item, if not already in the player list of the area
						if (!area.players.containsKey(name))
							list1.addTextItem(name, id, name);
					}
				}
				catch(SQLException e)
				{
					//on errors, do nothing and simply use what we got.
				}
			}
			else
			{
				int	id	= 1;
				for (String groupName : Db.permGroups)
				{
					if (!area.groups.containsKey(groupName))
						list1.addTextItem(groupName, id, groupName);
					id++;
				}
			}
			// The PRESET LIST
			list2	= new GuiScrollList(maxLines, true);
			addChild(list2, null, null);
			list2.setBorderThickness(1, false);
			list2.setMargin(GuiDefs.DEFAULT_PADDING);
			int	presetId	= 10000;		// a number larger enough not to clash with player DN ID's
			for (Entry<String,Integer> entry : AreaProtection.presets.entrySet())
			{
				list2.addTextItem(entry.getKey(), presetId, entry.getKey());
				presetId++;
			}

			// The ADD BUTTON
			doButton		= addTextItem(Msgs.msg[Msgs.gui_editAdd], DOBUTTON_ID, null);
			doButton.setPivot(PivotPosition.Center);
			doButton.setColor(GuiDefs.INACTIVE_COLOUR);
			doButton.setClickable(false);
		}

		/**
		 * Arranges the elements inside the panel.
		 * <p>Called by the layout() method of the GuiModalwindow the panel
		 * belongs to, before showing it to a player.
		 * 
		 * @param	minWidth	the minimum width required by the context within which the
		 *						group is placed; use 0 if there no external constrains.
		 * @param	minHeight	the minimum height required by the context within which the
		 *						group is placed; use 0 if there no external constrains.
		 */
		@Override
		public void layout(int minWidth, int minHeight)
		{
			int	margin	= getMargin();
			int	padding	= getPadding();
			// first, layout the lists, to have their sizes
			list1.layout(minWidth, minHeight);
			list2.layout(minWidth, minHeight);
			// from list sizes, compute panel total sizes
			// (note that the two lists may have different widths,
			// but they have the same height, having the same number of visible items
			int	width		= (int)(list1.getWidth()+list2.getWidth()) + 2*margin;
			if (minWidth > width)
				width	= minWidth;
			int	height		= (int)list1.getHeight() + 4*GuiDefs.ITEM_SIZE + 2*padding + 2*margin;
//						the height of the player list	headings & button	paddings
			if (minHeight > height)
				height	= minHeight;
			setSize(width, height, false);

			// position the individual children
			height		-= margin;								// move below the top margin
			int	list1X	= margin;
			int	list2X	= margin+(int)list1.getWidth()+padding;
			list1Head.setPosition(list1X, height, false);
			list2Head.setPosition(list2X, height, false);

			height	-= GuiDefs.ITEM_SIZE + padding;				// move below the headings
			list1.setPosition(list1X, height, false);
			list2.setPosition(list2X, height, false);

			height	-= list1.getHeight() + padding				// move below the lists
					+ (GuiDefs.ITEM_SIZE * 3) / 2;				// & to the middle of button
			doButton   .setPosition(width/2, height, false);
		}
	}

}
