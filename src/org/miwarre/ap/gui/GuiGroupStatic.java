/****************************
	A r e a P r o t e c t i o n / g u i  -  A Java package for common GUI functionalities.

	GuiGroupStatic.java - A GuiGrop which displays a collection of GuiElement's
							with static positions and sizes.

	Created by : Maurizio M. Gavioli 2017-03-08

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2017
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package org.miwarre.ap.gui;

import java.util.ArrayList;
import org.miwarre.ap.gui.GuiDefs.Pair;
import net.risingworld.api.gui.GuiElement;
import net.risingworld.api.gui.GuiImage;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.gui.GuiPanel;
import net.risingworld.api.gui.GuiTextField;
import net.risingworld.api.gui.PivotPosition;
import net.risingworld.api.objects.Player;

public class GuiGroupStatic extends GuiGroup
{
	protected	ArrayList<Pair<GuiElement,Pair<Integer,Object>>>
						children	= null;

	public GuiGroupStatic(int flags)
	{
		super();
		setPivot(PivotPosition.TopLeft);
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
		for (Pair<GuiElement,Pair<Integer,Object>> item : children)
		{
			if (item == null)
				continue;
			GuiElement	element	= item.getL();
			if (element instanceof GuiGroup)
				((GuiGroup)element).show(player);
			else
				player.addGuiElement(element);
		}
	}

	/**
	 * Hides the panel on the player screen.
	 * 
	 * @param	player	the player to hide the panel from.
	 */
	@Override
	public void hide(Player player)
	{
		for (Pair<GuiElement,Pair<Integer,Object>> item : children)
		{
			if (item == null)
				continue;
			GuiElement	element	= item.getL();
			if (element instanceof GuiGroup)
				((GuiGroup)element).hide(player);
			else
				player.removeGuiElement(element);
		}
		player.removeGuiElement(this);
	}

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
	@Override
	public void addChild(GuiElement element, Integer id, Object data)
	{
		if (element == null)
			return;
		if (children == null)
			children	= new ArrayList<Pair<GuiElement,Pair<Integer,Object>>>(4);
		children.add(new Pair<GuiElement,Pair<Integer,Object>>(element, new Pair<Integer,Object>(id, data)));
		if (element instanceof GuiImage)
			((GuiImage)element).setClickable(id != null);
		else if (element instanceof GuiLabel)
		{
			((GuiLabel)element).setClickable(id != null);
			((GuiLabel)element).setFontSize(GuiDefs.ITEM_SIZE);
		}
		else if (element instanceof GuiPanel)
			((GuiPanel)element).setClickable(id != null);
		else if (element instanceof GuiTextField)
		{
			((GuiTextField)element).setClickable(id != null);
			((GuiTextField)element).setBorderThickness(1, false);
			((GuiTextField)element).setBackgroundPreset(1);
			((GuiTextField)element).setEditable(id != null);
			((GuiTextField)element).setListenForInput(id != null);
		}
		element.setPivot(PivotPosition.TopLeft);
		super.addChild(element);
	}

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
	@Override
	public GuiLabel addTextItem(String text, Integer id, Object data)
	{
		GuiLabel	label	= new GuiLabel(0, 0, false);
		if (children == null)
			children	= new ArrayList<Pair<GuiElement,Pair<Integer,Object>>>(4);
		children.add(new Pair<GuiElement,Pair<Integer,Object>>(label, new Pair<Integer,Object>(id, data)));
		if (text != null && text.length() > 0)
			label.setText(text);
		label.setClickable(id != null);
		label.setFontSize(GuiDefs.ITEM_SIZE);
		label.setPivot(PivotPosition.TopLeft);
		super.addChild(label);
		return label;
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
		for (Pair<GuiElement,Pair<Integer, Object>> item : children)
		{
			if (item == null)
				continue;
			GuiElement	e	= item.getL();
			if (e instanceof GuiGroup)
			{
				Pair<Integer,Object>	data	= ((GuiGroup)e).getItemData(element);
				if (data != null)
					return data;
			}
			else if (item.getL() == element)
				return item.getR();
		}
		return null;
	}

	/**
	 * Returns the child with the given id or null if none.
	 * @param id	the id to look for.
	 * @return	the child GuiElement with the given id or null if none is found.
	 */
	@Override
	public GuiElement getChildFromId(int id)
	{
		for (Pair<GuiElement,Pair<Integer, Object>> item : children)
		{
			if (item == null)
				continue;
			if (item.getR().getL() == id)
				return item.getL();
		}
		return null;
	}

	/**
	 * Arranges the elements inside the group according to the group type and settings.
	 * 
	 * For GuiGropStatic, it actually does nothing, as group elements do not arrange themselves.
	 * @param	minWidth	the minimum width required by the context within which the
	 *						group is placed; use 0 if there no external constrains.
	 * @param	minHeight	the minimum height required by the context within which the
	 *						group is placed; use 0 if there no external constrains.
	 */
	@Override
	public void layout(int minWidth, int minHeight)
	{
		// With static groups, does nothing
	}

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
	@Override
	public void free()
	{
		for (Pair<GuiElement,Pair<Integer,Object>> item : children)
		{
			if (item == null)
				continue;
			GuiElement	element	= item.getL();
			if (element instanceof GuiGroup)
				((GuiGroup)element).free();
			super.removeChild(element);
		}
		children.clear();
	}

}
