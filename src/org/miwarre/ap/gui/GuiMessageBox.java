/****************************
	A r e a P r o t e c t i o n  -  A Rising World Java plug-in for area permissions.

	gui/GuiMessageBox.java - Displays and manages a modal message box.

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

package org.miwarre.ap.gui;

import net.risingworld.api.Plugin;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.gui.PivotPosition;
import net.risingworld.api.objects.Player;

/**
 * Implements a modal message box. Each message box is made of a title bar,
 * with a title and a close button, a number of text lines and up to 3 buttons.
 * <p>Text lines are shown one below the other and buttons are shown from left
 * to right at the bottom.
 * <p>The message box manages its own event Listener; it also turns the mouse
 * cursor on on display and off on hiding.
 */
public class GuiMessageBox extends GuiModalWindow
{
	private	static final	int		BUTTON1_ID		= 1;
	private	static final	int		BUTTON2_ID		= 2;
	private	static final	int		BUTTON3_ID		= 3;
	private	static final	int		MAX_BUTTONS		= 3;
	private static final	int		MIN_BUTTON_X	= 50;
	private static final	int		BUTTON_X_DELTA	= 100;

	private final	GuiDefs.GuiCallback		callerCallback;
	private final	Object					context;

	public GuiMessageBox(Plugin plugin, String titleText, String[] texts, String[] buttonTexts,
			Object context, GuiDefs.GuiCallback callback)
	{
		super(plugin, titleText, GuiDefs.GROUPTYPE_NONE, 0, null);
		setCallback(new DlgHandler());
		callerCallback	= callback;
		this.context	= context;
		MessagePanel	panel	= new MessagePanel(texts, buttonTexts);
		setPanel(panel);
		
	}

	//********************
	// HANDLERS
	//********************

	private class DlgHandler implements GuiDefs.GuiCallback
	{
		@Override
		public void onCall(Player player, int id, Object data)
		{
			int		retId;
			switch(id)
			{
			case BUTTON1_ID:
				retId	= GuiDefs.OK_ID;
				break;
			case BUTTON2_ID:
				retId	= GuiDefs.ABORT_ID;
				break;
			case BUTTON3_ID:
				retId	= 1;
				break;
			default:
				return;
			}
			callerCallback.onCall(player, retId, context);
			pop(player);
		}
	}

	//********************
	// PRIVATE HELPER METHODS
	//********************

	//********************
	// PANEL CLASS
	//********************

	/**
	 * A private class for the main panel of the GuiTwoListsSelector window.
	 */
	class MessagePanel extends GuiGroupStatic
	{
		GuiLabel		list1Head;
		GuiLabel		list2Head;
		GuiScrollList	list1;
		GuiScrollList	list2;

		public MessagePanel(String[] texts, String[] buttonTexts)
		{
			super(0);
			setMargin(GuiDefs.DEFAULT_PADDING);
			int		panelHeight	= texts.length * (GuiDefs.ITEM_SIZE+GuiDefs.DEFAULT_PADDING)
						+ GuiDefs.ITEM_SIZE*3 + GuiDefs.DEFAULT_PADDING*3;
			int		y			= panelHeight - GuiDefs.DEFAULT_PADDING;
			int		panelWidth	= 0;
			// The message texts
			for (String text : texts)
			{
				GuiLabel	label	= addTextItem(text, null, null);
				label.setPosition(GuiDefs.DEFAULT_PADDING, y, false);
				y				-= GuiDefs.ITEM_SIZE+GuiDefs.DEFAULT_PADDING;
				int		textW	= (int)GuiDefs.getTextWidth(text, GuiDefs.ITEM_SIZE);
				if (textW > panelWidth)
					panelWidth	= textW;
			}
			// The buttons
			int		maxButton	= MAX_BUTTONS;
			if (buttonTexts.length < maxButton)
				maxButton		= buttonTexts.length;
			int		x			= MIN_BUTTON_X;
			y					-= (GuiDefs.ITEM_SIZE*3)/2;
			for (int i = 0; i < maxButton; i++)
			{
				GuiLabel	label	= addTextItem(buttonTexts[i], BUTTON1_ID + i, null);
				label.setPivot(PivotPosition.Center);
				label.setColor(GuiDefs.ACTIVE_COLOUR);
				label.setClickable(true);
				label.setPosition(x, y, false);
				x	+= BUTTON_X_DELTA;
			}
			if (x > panelWidth)
				panelWidth	= x;
			setSize(panelWidth, panelHeight, false);
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
/*		@Override
		public void layout(int minWidth, int minHeight)
		{
			// nothing to do, as all elements are positioned in teh costructor
			// and no longer modified
		}
*/	}
}
