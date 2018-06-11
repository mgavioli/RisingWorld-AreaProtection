/****************************
	A r e a P r o t e c t i o n / g u i  -  A Java package for common GUI functionalities.

	GuiScrollList.java - A GuiGroup which displays a scroll list of textual item.

	Created by : Maurizio M. Gavioli 2017-03-04

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2017
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package org.miwarre.ap.gui;

import java.util.ArrayList;
import java.util.List;
import org.miwarre.ap.gui.GuiGroup;
import org.miwarre.ap.gui.GuiDefs.Pair;
import net.risingworld.api.gui.GuiElement;
import net.risingworld.api.gui.GuiImage;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.gui.PivotPosition;
import net.risingworld.api.objects.Player;

/**
 * A GuiGroup which displays a vertically arranged list of textual items within
 * a user-defined max. If the list has more items, two arrow buttons allows to
 * scroll the list up and down.
 * <p>One of the items can be selected and shown highlighted.
 * <p>If the number of items is less than the maximum, the list can accommodate
 * only the height necessary to show them or unconditionally allocate the
 * maximum height, according to the fixedHeight parameter of the constructor.
 * <p>The former is useful for menus and to reduce the screen occupation at the
 * minimum; the latter when the scroll list is part of a larger window and a
 * fixed height is more convenient.
 * <p>With a variable height, the scroll list -- and the window it belongs to --
 * has to be laid out again, if the number of items changes after it has been
 * shown to a player. 
 */
public class GuiScrollList extends GuiGroup
{
	//
	// CONSTANTS
	//
	private static final	int	SELECTED_ID_NONE	= -1;
	//
	// FIELDS
	//
	private	GuiLabel[]		guiItems;			// the visible items of the list
	private GuiImage		buttonNext;			// the button to scroll down
	private GuiImage		buttonPrev;			// the button to scroll up
	private	int				firstItem;			// the index of the first shown item in the list of
												// all the items;
	private	boolean			fixedHeight;		// if true, the height for max NumOfShownItem is always allocated
	private List<Pair<String,Pair<Integer,Object>>>	items;	// the text items and their data
	private int				itemsWidth;			// the max width of the shown items
	private	int				maxNumOfShownItems;	// the max number of items to show
	private	int				numOfItems;			// total number of items in list
	private	int				numOfShownItems;	// number of items actually shown
	private	int				selectedItemId;		// the item id of the selected item, if any
	private	int				shown;

	/**
		Creates an empty scrollable list layout.
		@param	maxSize	the max number of items to show
	 */
	public GuiScrollList(int maxSize, boolean fixedHeight)
	{
		maxNumOfShownItems	= maxSize;
		firstItem		= 0;
		this.fixedHeight= fixedHeight;
		guiItems		= new GuiLabel[maxNumOfShownItems];
		items			= new ArrayList<Pair<String,Pair<Integer,Object>>>();
		itemsWidth		= 0;
		numOfItems		= 0;
		selectedItemId	= SELECTED_ID_NONE;
		shown			= 0;
		setPivot(PivotPosition.TopLeft);
		setColor(GuiDefs.PANEL_COLOUR);
		setVisible(true);

		// always create [Up] and [Down] arrows, but hide them until required
		buttonNext	= new GuiImage(0, 0, false, GuiDefs.BUTTON_SIZE, GuiDefs.BUTTON_SIZE, false);
		GuiDefs.setImage(buttonNext, GuiDefs.ICN_ARROW_DOWN);
		buttonNext.setPivot(PivotPosition.BottomRight);
		buttonNext.setClickable(true);
		buttonNext.setVisible(false);
		addChild(buttonNext);
		buttonPrev	= new GuiImage(0, 0, false, GuiDefs.BUTTON_SIZE, GuiDefs.BUTTON_SIZE, false);
		GuiDefs.setImage(buttonPrev, GuiDefs.ICN_ARROW_UP);
		buttonPrev.setPivot(PivotPosition.TopRight);
		buttonPrev.setClickable(true);
		buttonPrev.setVisible(false);
		addChild(buttonPrev);
	}

	//********************
	// PUBLIC METHODS
	//********************

	/**
	 * Sets the maximum number of visible items of the scroll list; if the list
	 * has more items, arrow buttons allow to scroll it.
	 * @param val	the maximum number of visible rows.
	 */
	public void setMaxVisibleRows(int val)	{ maxNumOfShownItems = val; }

	/**
	 * This method does nothing, as list text items are not statically linked
	 * to any GuiElement.
	 */
	@Override
	public void addChild(GuiElement element, Integer id, Object data)
	{
		// Does nothing!
	}

	/**
	 * Set / unset the text of the GuiLabel corresponding to id to the selected colour,
	 * if any. Returns null if id does not correspond to a shown GuiLabel. 
	 * @param	id		the id of the item to select / unselect.
	 * @return	the GuiLabel with id, if any | null, if id does not correspond
	 *			to a shown GuiLabel.
	 */
	public GuiLabel selectItem(int id)
	{
		GuiLabel	label	= (GuiLabel)getChildFromId(selectedItemId);
		if (label != null)
			label.setFontColor(GuiDefs.TEXT_COLOUR);
		selectedItemId	= id;
		label	= (GuiLabel)getChildFromId(selectedItemId);
		if (label != null)
			label.setFontColor(GuiDefs.TEXT_SEL_COLOUR);
		return label;
	}

	/**
	 * Adds a new menu item with the associated id and data.
	 * 
	 * <p>id can be any Integer and id's should be all different from one
	 * another within each dialogue box.
	 * 
	 * <p>The data parameter can be any Java object and can store additional
	 * information required to deal with the element, when a click event is
	 * reported for it via the callback object. It can also be null if no
	 * additional info is needed for the element.
	 * 
	 * <p>id and data are reported by the callback object upon click events.
	 * 
	 * @param	text	the text of the new menu item.
	 * @param	id		the id associated with the item.
	 * @param	data	the data associated with the element; may be null for
	 * 					elements which need no additional data other than their id.
	 * @return	null, as the text item is not attached to a specific GuiLabel.
	 */
	@Override
	public GuiLabel addTextItem(String text, Integer id, Object data)
	{
		Pair<String,Pair<Integer,Object>>	item	=
				new Pair<String,Pair<Integer,Object>>(text, new Pair<Integer,Object>(id,data));
		items.add(item);
		// adjust panel width if new item is longer than current max item width (including left and right padding)
		int		textWidth;
		if ( (textWidth = (int)(GuiDefs.getTextWidth(text, GuiDefs.ITEM_SIZE)))
				> itemsWidth)
			itemsWidth = textWidth;
		// if within the max number of items, create a new GuiLabel for the item
		if (numOfItems < maxNumOfShownItems)
		{
			guiItems[numOfItems]	= new GuiLabel(0, 0, false);		// temporary position
			guiItems[numOfItems].setPivot(PivotPosition.TopLeft);
			guiItems[numOfItems].setFontSize(GuiDefs.ITEM_SIZE);
			guiItems[numOfItems].setClickable(true);
			addChild(guiItems[numOfItems]);
			numOfShownItems++;											// one more shown item
		}
		numOfItems++;
		// if the panel is already shown, update visible items
		if (shown > 0)
		{
			// TODO : addGuiElement!
			layout((int)getWidth(), (int)getHeight());
		}
		return null;
	}

	public void removeTextItem(int id)
	{
		int	index	= 0;
		for (Pair<String,Pair<Integer,Object>> item : items)
		{
			if (item != null && item.getR().getL() == id)
			{
				items.remove(index);
				// if the panel is already shown, update visible items
				if (shown > 0)
				{
					// TODO : removeGuiElement!
					layout((int)getWidth(), (int)getHeight());
				}
				return;
			}
			index++;
		}
	}

	/**
	 * Returns the id associated with element, if element is one of the
	 * children of the group (recursively); or null otherwise.

	 * @param	element	the GuiElement to look for.
	 * @return	the id associated with element if present, null if not.
	 */
	@Override
	public Integer getItemId(GuiElement element)
	{
		Pair<Integer, Object>	itemData	= getItemData(element);
		if (itemData == null)
				return null;
		return itemData.getL();
	}

	/**
	 * Returns the id and data pair associated with element, if element is one
	 * of the children of the group (recursively); or null otherwise.

	 * @param	element	the GuiElement to look for.
	 * @return	the id and data pair associated with element if present,
	 * 			null if not.
	 */
	@Override
	public Pair<Integer, Object> getItemData(GuiElement element)
	{
		// Manage [Up] and [Down] arrows
		if (element == buttonPrev)
		{
			scrollUp();
			return (new Pair<Integer,Object>(GuiDefs.INTERNAL_ID, null));
		}
		if (element == buttonNext)
		{
			scrollDown();
			return (new Pair<Integer,Object>(GuiDefs.INTERNAL_ID, null));
		}
		// check for GuiLabel's 
		int	count	= 0;
		for (GuiElement guiItem : guiItems)
		{
			if (guiItem == null)
				continue;
			// GuiLabel found: return the corresponding data in items
			if (guiItem == element)
				return items.get(count+firstItem).getR();
			count++;
		}
		return null;
	}

	/**
	 * Returns the child GuiLabel attached to the text item of the given id or null if none.
	 * 
	 * <p>If the list has more items than can be displayed, only the displayed
	 * items are actually attached to a GuiLabel, looking for the id's of the
	 * other returns null.  
	 * @param	id	the id to look for.
	 * @return	the child GuiLabel attached to the text item with the given id or null if none is found.
	 */
	@Override
	public GuiElement getChildFromId(int id)
	{
		int	count	= 0;
		for (Pair<String,Pair<Integer, Object>> item : items)
		{
			if (item == null)
				continue;
			if (item.getR().getL() == id)
				return guiItems[count - firstItem];
			count++;
		}
		return null;
	}

	/**
	 * Sets the text of an item to a new String.
	 * @param	id		the id of the item to change
	 * @param	newText	the new text of the item
	 */
	public void setItemText(int id, String newText)
	{
		for (Pair<String,Pair<Integer, Object>> item : items)
			if (item != null && item.getR().getL() == id)
			{
				item.setL(newText);
				return;
			}
	}

	/**
	 * Arranges the elements inside the list.
	 * 
	 * @param	minWidth	the minimum width required by the context within which the
	 *						group is placed; use 0 if there no external constrains.
	 * @param	minHeight	the minimum height required by the context within which the
	 *						group is placed; use 0 if there no external constrains.
	 */
	@Override
	public void layout(int minWidth, int minHeight)
	{
		int	finalH, finalW;		// the final height and width of the panel

		// set total sizes
		// HEIGHT: each item is ITEM_SIZE high + padding below, except last item which has no below padding
		// + top and bottom margin
		finalH	= (fixedHeight ? maxNumOfShownItems : numOfShownItems) * (GuiDefs.ITEM_SIZE + padding)
				- padding + margin*2;
		if (minHeight > finalH)
			finalH	= minHeight;

		// include room for arrow buttons and for left and right margin
		finalW		= itemsWidth + padding + GuiDefs.BUTTON_SIZE + margin*2;
		if (minWidth > finalW)
			finalW	= minWidth;
		setSize(finalW, finalH, false);

		// place buttons and visible text items
		buttonPrev.setPosition(finalW - margin, finalH - margin, false);
		buttonNext.setPosition(finalW - margin, margin, false);
		for (int i = 0, y = finalH - margin; i < numOfShownItems; i++,
				y-=(GuiDefs.ITEM_SIZE+margin) )
			guiItems[i].setPosition(margin, y, false);
		updateTexts();
	}

	/**
	 * Displays the panel on the player screen.
	 * 
	 * @param	player	the player to show the panel to.
	 */
	@Override
	public void show(Player player)
	{
		player.addGuiElement(this);
		for (GuiElement element : guiItems)
		{
			if (element == null)
				continue;
			player.addGuiElement(element);
		}
		player.addGuiElement(buttonNext);
		player.addGuiElement(buttonPrev);
		shown++;
	}

	/**
	 * Hides the panel on the player screen.
	 * 
	 * @param	player	the player to hide the panel from.
	 */
	@Override
	public void hide(Player player)
	{
		player.removeGuiElement(this);
		for (GuiElement element : guiItems)
		{
			if (element == null)
				continue;
			player.removeGuiElement(element);
		}
		player.removeGuiElement(buttonNext);
		player.removeGuiElement(buttonPrev);
		shown--;
		if (shown < 0)
			shown	= 0;
	}

	/**
	 * Releases the resources used by the layout and all its descending
	 * hierarchy of children. After this method has been called, the layout
	 * cannot be used or displayed any longer.
	 * 
	 * The resources are in any case garbage collected once the layout goes
	 * out of scope or all the references to it elapse. Using this method
	 * might be useful to speed up the garbage collection process, once the
	 * layout is not longer needed.
	 * <p>It is necessary to call this method only for the top layout of hierarchy
	 * and only if it not part of a managed element (like GuiDialogueBox).
	 */
	@Override
	public void free()
	{
		super.removeChild(buttonNext);
		buttonNext	= null;
		super.removeChild(buttonPrev);
		buttonPrev	= null;
		for (GuiElement element : guiItems)
		{
			if (element == null)
				continue;
			if (element instanceof GuiGroup)
				((GuiGroup)element).free();
			super.removeChild(element);
		}
		guiItems	= null;
		items.clear();
	}

	//********************
	// PRIVATE HELPER METHODS
	//********************

	private void scrollDown()
	{
		firstItem	+= maxNumOfShownItems-1;
		if (firstItem + maxNumOfShownItems > numOfItems)
			firstItem	= numOfItems - maxNumOfShownItems;
		updateTexts();
	}

	private void scrollUp()
	{
		firstItem	-= maxNumOfShownItems-1;
		if (firstItem < 0)
			firstItem	= 0;
		updateTexts();
	}

	private void updateArrows()
	{
		buttonPrev.setVisible(firstItem > 0);
		buttonNext.setVisible(firstItem + maxNumOfShownItems < numOfItems);
	}

	private void updateTexts()
	{
		for (int i = 0; i < numOfShownItems; i++)
		{
			Pair<String,Pair<Integer,Object>>	item	= items.get(firstItem+i);
			guiItems[i].setText(item.getL());
			Integer	id	= item.getR().getL();
			if (id != null)
				guiItems[i].setFontColor(id == selectedItemId ?
						GuiDefs.TEXT_SEL_COLOUR : GuiDefs.TEXT_COLOUR);
		}
		updateArrows();
	}

}
