/****************************
	A r e a P r o t e c t i o n  -  A Rising World Java plug-in for area permissions.

	GuiTwoListsSelector.java - A window to select a pair of items in two lists.

	Created by : Maurizio M. Gavioli 2017-03-15

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

import java.util.Map.Entry;
import java.util.Set;
import org.miwarre.ap.gui.GuiDefs;
import org.miwarre.ap.gui.GuiGroupStatic;
import org.miwarre.ap.gui.GuiDefs.GuiCallback;
import org.miwarre.ap.gui.GuiModalWindow;
import org.miwarre.ap.gui.GuiScrollList;
import net.risingworld.api.Plugin;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.gui.PivotPosition;
import net.risingworld.api.objects.Player;

public class GuiTwoListsSelector extends GuiModalWindow
{
	//
	// CONSTANTS
	//
	public static final	int		DOBUTTON_ID		= 2000000;
	public static final	int		MIN_PRESET_ID	= 1000001;

	//
	// FIELDS
	//
	private final	GuiCallback		callerCallback;
	private			GuiLabel		doButton;
	private final	TwoListPanel	panel;
	private			Integer			selId1, selId2;
	private			String			selText1, selText2;
	private final	int				type;

	public GuiTwoListsSelector(final Plugin plugin, final String title, final GuiCallback callback,
			final ProtArea area, final int maxLines, final int type)
	{
		super(plugin, title, GuiDefs.GROUPTYPE_NONE, 0, null);
		setCallback(new DlgHandler());
		callerCallback	= callback;
		selId1		= selId2 = null;
		selText1	= "";
		selText2	= "";
		panel	= new TwoListPanel(area, maxLines, type);
		setPanel(panel);
		this.type	= type;
	}

	//********************
	// PUBLIC METHODS
	//********************

	public String getSelectedItemText(boolean list1)
	{
		String	selected	= (list1 ? selText1 : selText2);
		if (selected != null && selected.length() > 0)
			return selected;
		else
			return null;
	}

	public Integer getSelectedItemId(boolean list1)
	{
		Integer	selected	= (list1 ? selId1 : selId2);
		return selected;
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
				selId1		= id;
				selText1	= (String)data;
				panel.list1.selectItem(id);
				updateDoButton();
				return;
			}
			if (id >= MIN_PRESET_ID && id < DOBUTTON_ID)
			{
				selId2		= id;
				selText2	= (String)data;
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
		boolean	complete	= selText1.length() > 0 && (selText2.length() > 0 || type == Db.LIST_TYPE_MANAGERS);
		doButton.setColor(complete ? GuiDefs.ACTIVE_COLOUR : GuiDefs.INACTIVE_COLOUR);
		doButton.setClickable(complete);
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
					Msgs.msg[type == Db.LIST_TYPE_GROUP ? Msgs.gui_selectGroup : Msgs.gui_selectPlayer],
					null, null);
			list2Head	= addTextItem(type == Db.LIST_TYPE_MANAGERS ? "" : Msgs.msg[Msgs.gui_selectPreset], null, null);
			// The PLAYER/GROUP LIST
			list1	= new GuiScrollList(maxLines, true);
			addChild(list1, null, null);
			list1.setBorderThickness(1, false);
			list1.setMargin(GuiDefs.DEFAULT_PADDING);
			if (type == Db.LIST_TYPE_GROUP)
			{
				for (Entry<Integer,String> entry : Db.groupNames.entrySet())
				{
					int		groupId	= entry.getKey();
					if (!area.groups.containsKey(groupId))
					{
						String	groupName	= entry.getValue();
						list1.addTextItem(groupName, groupId, groupName);
					}
				}
			}
			else
			{
				Set<Integer> playerIds	= Db.getPlayerIdSet();
				for (Integer id : playerIds)
				{
					if (!area.players.containsKey(id))
					{
						String	name	= Db.getPlayerNameFromId(id);
						list1.addTextItem(name, id, name);
					}
				}
			}
			// The PRESET LIST
			list2	= new GuiScrollList(maxLines, true);
			addChild(list2, null, null);
			list2.setBorderThickness(1, false);
			list2.setMargin(GuiDefs.DEFAULT_PADDING);
			int	presetId	= MIN_PRESET_ID;		// a number larger enough not to clash with player DN ID's
			// while adding an area manager, presets and permissions make no sense
			// as managers all have admin level by definition
			if (type == Db.LIST_TYPE_MANAGERS)
			{
				selId2		= MIN_PRESET_ID;
				selText2	= "Admin";
				list2.setVisible(false);
			}
			else
				for (Entry<String,Long> entry : AreaProtection.presets.entrySet())
				{
					list2.addTextItem(entry.getKey(), presetId, entry.getKey());
					presetId++;
				}

			// The ADD BUTTON
			doButton	= addTextItem(Msgs.msg[Msgs.gui_editAdd], DOBUTTON_ID, null);
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
