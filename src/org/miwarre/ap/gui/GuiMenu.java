/****************************
	A r e a P r o t e c t i o n  -  A Rising World Java plug-in for area permissions.

	gui/GuiMenu.java - Displays and manages a modal menu.

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

import org.miwarre.ap.gui.GuiDefs.GuiCallback;
import net.risingworld.api.Plugin;
import net.risingworld.api.objects.Player;

/**
 * A class implementing a modal menu. Each menu is made of a top title bar,
 * with title and close button, and a number of text items which can be clicked
 * to select.
 *
 * The items are arranged vertically and the menu is shown in the middle of the
 * player screen. The menu adapts its vertical and horizontal sizes to the
 * number and length of the texts.
 * <p>If there are more than 12 items, the menu displays them in chunks of 12,
 * with an up and a down button to page among the chunks.
 * <p>GuiMenu inherits all the behaviours and methods of GuiModalWindow,
 * including the management of the mouse cursor, of click events, of the close
 * button and of the 'display stack'.
 */
public class GuiMenu extends GuiModalWindow
{
	private static final	int		MAX_NUM_OF_ITEMS= 12;

	/**
	 * Creates a new GuiMenu.
	 * @param	plugin		the plug-in the GuiMenu is intended for. This
	 *						is only needed to manage the internal event listener
	 *						and has no effects on the plug-in itself.
	 * @param	titleText	the text of the title.
	 * @param	callback	the callback object to which to report events. Can
	 *						be null, but in this case no event will reported
	 *						until an actual callback object is set with the
	 *						setCallback() method.
	 */
	public GuiMenu(Plugin plugin, String titleText, GuiCallback callback)
	{
		super(plugin, titleText, GuiDefs.GROUPTYPE_SCROLLLIST, MAX_NUM_OF_ITEMS, callback);
	}

	public GuiMenu(Plugin plugin, String titleText, GuiCallback callback, int maxNumOfRows)
	{
		super(plugin, titleText, GuiDefs.GROUPTYPE_SCROLLLIST, maxNumOfRows >= 1 ? maxNumOfRows : MAX_NUM_OF_ITEMS, 
				callback);
	}

	/**
	 * Sends a command to the menu, simulating a menu item selection.
	 * The menu responds as if the item with the given menuItemId has been selected.
	 * @param	player		the player the menu is shown to
	 * @param	menuItemId	the ID of the menu item to simulate
	 */
	public void command(Player player, int menuItemId)
	{
		_callback.onCall(player, menuItemId, null);
	}
}
