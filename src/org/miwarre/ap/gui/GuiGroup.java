/****************************
	A r e a P r o t e c t i o n / g u i  -  A Java package for common GUI functionalities.

	GuiGroup.java - An abstract class which is the base for grouped collections of
					GuiElement's with associated id data.

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

package org.miwarre.ap.gui;

import org.miwarre.ap.gui.GuiDefs.Pair;
import net.risingworld.api.gui.GuiElement;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.gui.GuiPanel;
import net.risingworld.api.objects.Player;

/**
 * A group of GUI elements with id data associated.
 * 
 * <p>Each element has an id Integer and a data Object (possibly either or
 * both null) and the group can return the id/data associated with each
 * GuiElement or vice versa.
 */
public abstract class GuiGroup extends GuiPanel
{
	int		margin	= 0;						// the margin around the whole group
	int		padding	= GuiDefs.DEFAULT_PADDING;	// the padding between group elements

	/**
	 * Default constructor
	 */
	public GuiGroup()
	{
		super (0, 0, false, 0, 0, false);
	}

	/**
	 * Displays the panel on the player screen.
	 * 
	 * @param	player	the player to show the panel to.
	 */
	public abstract void show(Player player);

	/**
	 * Hides the panel on the player screen.
	 * 
	 * @param	player	the player to hide the panel from.
	 */
	public abstract void hide(Player player);

	/**
	 * Adds a GuiElement with the associated id and data as a direct child of
	 * the layout. The element is positioned according to the type of layout.
	 * 
	 * <p>If id is not null, the element is active (the player can click on it),
	 * if id is null, the element is not active.
	 * 
	 * @param	element	the element to add.
	 * @param	id		the id associated with the element; may be null for
	 * 					inactive elements.
	 * @param	data	the data associated with the element; may be null for
	 * 					elements which need no additional data other than their id.
	 */
	public abstract void addChild(GuiElement element, Integer id, Object data);

	/**
	 * Adds a textual item as a child of the group.
	 * 
	 * <p>The returned GuiLabel may be null, if the GuiGroup manages texts and
	 * Guielement's separately, as for instance the GuiScrollList does.
	 * 
	 * @param	text	the text to display
	 * @param	id		the id associated with the element; may be null for
	 * 					inactive elements.
	 * @param	data	the data associated with the element; may be null for
	 * 					elements which need no additional data other than their id.
	 * @return	with some GuiGroup sub-classes, the GuiLabel containing the text;
	 * 			with other, returns null.
	 */
	public abstract GuiLabel addTextItem(String text, Integer id, Object data);

	/**
	 * Returns the id associated with element, if element is one of the
	 * children of the group (recursively); or null otherwise.

	 * @param	element	the GuiElement to look for.
	 * @return	the id associated with element if present, null if not.
	 */
	public abstract Integer getItemId(GuiElement element);

	/**
	 * Returns the id and data pair associated with element, if element is one
	 * of the children of the group (recursively); or null otherwise.

	 * @param	element	the GuiElement to look for.
	 * @return	the id and data pair associated with element if present,
	 * 			null if not.
	 */
	public abstract Pair<Integer,Object> getItemData(GuiElement element);

	/**
	 * Returns the child with the given id or null if none.
	 * @param id	the id to look for.
	 * @return	the child GuiElement with the given id or null if none is found.
	 */
	public abstract GuiElement getChildFromId(int id);

	/**
	 * Arranges the elements inside the group according to the group type and settings.
	 * 
	 * As this method lays its children out recursively, it is usually
	 * necessary to call this method manually only for the top group of a
	 * group hierarchy.
	 * @param	minWidth	the minimum width required by the context within which the
	 *						group is placed; use 0 if there no external constrains.
	 * @param	minHeight	the minimum height required by the context within which the
	 *						group is placed; use 0 if there no external constrains.
	 */
	public abstract void layout(int minWidth, int minHeight);

	public int  getMargin()			{ return margin;	}
	public int  getPadding()		{ return padding;	}
	public void setMargin(int val)	{ margin	= val;	}
	public void setPadding(int val)	{ padding	= val;	}

	/**
	 * Releases the resources used by the group and all its descending
	 * hierarchy of children. After this method has been called, the used
	 * cannot be used or displayed any longer.
	 * 
	 * The resources are in any case garbage collected once the group goes
	 * out of scope or all the references to it elapse. Using this method
	 * might be useful to speed up the garbage collection process, once the
	 * group is not longer needed.
	 * <p>It is necessary to call this method only for the top group of hierarchy
	 * and only if it not part of a managed element (like GuiModalWindow or GuiMenu).
	 */
	public abstract void free();
}
