/****************************
	A r e a P r o t e c t i o n  -  A Rising World Java plug-in for area permissions.

	gui/GuiMenu.java - Displays and manages a modal menu.

	Created by : Maurizio M. Gavioli 2017-03-04

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2017
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package org.miwarre.ap.gui;

import org.miwarre.ap.gui.GuiDefs.GuiCallback;
import net.risingworld.api.Plugin;

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
		super(plugin, titleText, GuiDefs.GROUPTYPE_SCROLLLIST, callback);
		((GuiScrollList)_panel).setMaxVisibleRows(MAX_NUM_OF_ITEMS);
	}

}
