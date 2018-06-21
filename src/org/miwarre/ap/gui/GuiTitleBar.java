/****************************
	A r e a P r o t e c t i o n  -  A Rising World Java plug-in for area permissions.

	gui/GuiTitleBar.java - Implements a common title bar for all windows.

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

import net.risingworld.api.gui.GuiElement;
import net.risingworld.api.gui.GuiImage;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.gui.GuiPanel;
import net.risingworld.api.gui.PivotPosition;
import net.risingworld.api.objects.Player;

/**
 * Implements the concept of a title bar common to many other classes of this
 * package.
 * <p>A title bar is made of a title text and of an optional close button.
 */
public class GuiTitleBar extends GuiPanel
{
	public static final		int		TITLEBAR_HEIGHT	= GuiDefs.TITLE_SIZE + GuiDefs.DEFAULT_PADDING*2;
	private static final	int		CANCEL_YPOS		= TITLEBAR_HEIGHT - GuiDefs.DEFAULT_PADDING;
	private static final	int		TITLE_XPOS		= GuiDefs.DEFAULT_PADDING;
	private static final	int		TITLE_YPOS		= TITLEBAR_HEIGHT - GuiDefs.DEFAULT_PADDING;

	private			GuiImage	cancelButton;
	private final	int			minWidth;
	private			GuiLabel	title;

	/**
	 * Creates a new title bar as a child of an existing GuiElement (typically
	 * a GuiPanel).
	 * <p>The bar is added to the given parent, is given a specific background
	 * colour and pivot position (TopLeft); its title text is given a specific
	 * font colour and size.
	 * <p>If the close button is required, the image element for it is also
	 * created.
	 * @param	parent			the GuiElement to which to add the title bar
	 * @param	titleText		the text of the title
	 * @param	hasCancelButton	whether the title bar should have a cancel
	 *							button or not.
	 */
	public GuiTitleBar(GuiElement parent, String titleText, boolean hasCancelButton)
	{
		super();
		setColor(GuiDefs.TITLEBAR_COLOUR);
		setPivot(PivotPosition.TopLeft);
		if (parent != null)
			parent.addChild(this);

		title	= new GuiLabel(titleText, TITLE_XPOS, TITLE_YPOS, false);
		title.setPivot(PivotPosition.TopLeft);
		title.setText(titleText);
		title.setFontSize(GuiDefs.TITLE_SIZE);
		title.setFontColor(GuiDefs.TITLE_COLOUR);
		addChild(title);

		if (hasCancelButton)
		{
			cancelButton	= new GuiImage(0, 0, false, GuiDefs.BUTTON_SIZE, GuiDefs.BUTTON_SIZE, false);
			GuiDefs.setImage(cancelButton, GuiDefs.ICN_CROSS);
			cancelButton.setPivot(PivotPosition.TopLeft);
			cancelButton.setClickable(true);
			cancelButton.setVisible(true);
			addChild(cancelButton);
		}
		// compute minimal width
		minWidth	= (int)(GuiDefs.getTextWidth(titleText, GuiDefs.TITLE_SIZE) +
				(hasCancelButton ? GuiDefs.BUTTON_SIZE + GuiDefs.DEFAULT_PADDING*3 : GuiDefs.DEFAULT_PADDING));
		// set initial sizes to give parent something on which to base its own layout
		setSize(minWidth, TITLEBAR_HEIGHT, false);
	}

	/**
	 * Checks if the given element is the cancel button or not.
	 * 
	 * @param	element	the GuiElement to check.
	 * @return	true if the passed GuiElement is the title bar cancel button,
	 *			false if it is not.
	 */
	public boolean isCancelButton(GuiElement element)
	{
		return (element == cancelButton);
	}

	/**
	 * Returns the current title text.
	 * @return	the current title text as a String.
	 */
	protected String getTitleText()
	{
		return title.getText();
	}

	/**
	 * Releases all the resources used by the title bar. To be used before
	 * disposing of the title bar itself.
	 * <p>After calling this method, the title bar cannot be used any longer.
	 */
	protected void free()
	{
		removeChild(title);
		title			= null;
		if (cancelButton != null)
		{
			removeChild(cancelButton);
			cancelButton	= null;
		}
		removeFromParent();
	}

	/**
	 * Returns a minimal width (in pixels) suitable to contain the whole title bar.
	 * @return	 a minimal width in pixels.
	 */
	public int getMinWidth()	{ return minWidth; }

	/**
	 * Updates the width and the position of the title bar to match the current
	 * width and height of the parent GuiElement.
	 * <p>To be used <b>after</b> the parent GuiElement has reached its
	 * definitive width and height, typically right before displaying the
	 * parent to a player.
	 * <p>The title bar is placed at the top of the parent and inset a few
	 * pixels from the parent left, top and right border. 
	 */
	public void relayout()
	{
		GuiElement parent	= getParent();
		if (parent == null)
			return;
		int	parentHeight	= (int)parent.getHeight();
		int	parentWidth		= (int)parent.getWidth();
		int	parentBorder	= (int)parent.getBorderThickness();
		// position the title bat at the top left corner of the parent, but
		// inside the parent border, if any
		setPosition(parentBorder, parentHeight-parentBorder, false);
		// size the title bar to occupy the full parent width minus any border
		setSize(parentWidth - parentBorder*2, TITLEBAR_HEIGHT - parentBorder, false);
		if (cancelButton != null)
			cancelButton.setPosition(parentWidth - (GuiDefs.DEFAULT_PADDING + GuiDefs.BUTTON_SIZE),
					CANCEL_YPOS, false);
	}

	/**
	 * Displays the title bar on the screen of a player.
	 * <p>To be used instead of player.addGuiElement(titleBar), as this method
	 * manages the addition of all the elements making the bar up at once.
	 * @param player	the player to show this title bar to.
	 */
	public void show(Player player)
	{
		player.addGuiElement(this);
		player.addGuiElement(title);
		if (cancelButton != null)
			player.addGuiElement(cancelButton);
	}

	/**
	 * Removes the title bar from the screen of a player.
	 * <p>To be used instead of player.removeGuiElement(titleBar), as this
	 * method removes all the elements making the bar up at once.
	 * @param player	the player to remove this title bar from.
	 */
	public void hide(Player player)
	{
		player.removeGuiElement(this);
		player.removeGuiElement(title);
		if (cancelButton != null)
			player.removeGuiElement(cancelButton);
	}

}
