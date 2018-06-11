/****************************
	A r e a P r o t e c t i o n / g u i  -  A Java package for common GUI functionalities.

	gui/GuiModalWindow.java - Displays and manages a modal window.

	Created by : Maurizio M. Gavioli 2017-03-04

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2017
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package org.miwarre.ap.gui;

import org.miwarre.ap.gui.GuiDefs.GuiCallback;
import org.miwarre.ap.gui.GuiDefs.Pair;
import net.risingworld.api.Plugin;
import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.Listener;
import net.risingworld.api.events.player.gui.PlayerGuiElementClickEvent;
import net.risingworld.api.events.player.gui.PlayerGuiInputEvent;
import net.risingworld.api.gui.GuiElement;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.gui.GuiPanel;
import net.risingworld.api.gui.GuiTextField;
import net.risingworld.api.gui.PivotPosition;
import net.risingworld.api.objects.Player;

/**
 * A class implementing the concept of a modal window, i.e. of a window with
 * which the player can interact through the mouse, suspending interaction with
 * Rising World environment. Each modal window is made of a top title bar, with
 * title and close button, and a main panel underneath where controls can be added.
 * 
 * <p>This class is not aimed at being used as it is; it is the base class of
 * several, more specialised, classes which should be used instead. It can also
 * be sub-classed, to implement specific structures or behaviours.
 * The main panel is one of the GuiGroup subclasses, with its specific arrangement
 * of children, according to the layoutType of the constructor. To the panel,
 * standard GuiElement's can be added with addChild() and addTextItem().
 * <p>GuiModalWindow manages its own event Listener; it also turns the mouse
 * cursor on on display and off on hiding.
 * <p>GuiModalWindow manages the close button in the title bar, hiding the box
 * from the player screen, freeing its resources and turning off the mouse cursor.
 * The callback object is notified of a close event by passing an id parameter
 * with a value of GuiDefs.ABORT_ID.
 * <p>The consumer plug-in needs not to do any additional management of the
 * window itself in response to this notification (of course, it should do any
 * management of its own resources).
 * <p>GuiModalWindow notifies of click and text entry events via an
 * GuiCallback object passed to the constructor or set after construction with
 * the setCallback() method.
 * <p>On events, the onCall() method of the callback object is called with
 * parameters for the player originating the event, the id of the GuiElement
 * and any additional data set for the GuiElement. Id and data for each child
 * are set when the child is added.
 * <p>This class implements a 'display stack' of modal windows: 'pushing' a new
 * window with the push() method displays the new window and 'popping' it,
 * with the pop() method, restores the window previously displayed. 
 */
public class GuiModalWindow extends GuiPanel implements Listener
{
	GuiCallback		_callback;
	GuiGroup		_panel;
	int				listenerRef;
	GuiModalWindow	prevWindow;
	Plugin			plugin;
	GuiTitleBar		titleBar;

	/**
	 * Creates a new GuiModalWindow.
	 * @param	plugin		the plug-in the GuiModalWindow is intended for. This
	 * 						is only needed to manage the internal event listener
	 * 						and has no effects on the plug-in itself.
	 * @param	title		the text of the title.
	 * @param	groupType	the type of the main window GuiGroup (one of the GuiDefs.GROUPTYPE_ABSOLUTE
	 * 						or GuiDefs.GROUPTYPE_SCROLLLIST values)
	 * @param	callback	the callback object to which to report events. Can
	 * 						be null, but in this case no event will reported
	 * 						until an actual callback object is set with the
	 * 						setCallback() method.
	 */
	public GuiModalWindow(Plugin plugin, String title, int groupType, GuiCallback callback)
	{
		setPosition(0.5f, 0.5f, true);
		setPivot(PivotPosition.Center);
		setBorderColor(GuiDefs.BORDER_COLOUR);
		setBorderThickness(GuiDefs.BORDER_THICKNESS, false);
		setColor(GuiDefs.PANEL_COLOUR);
		this._callback	= callback;
		this.plugin		= plugin;
		switch (groupType)
		{
		case GuiDefs.GROUPTYPE_STATIC:
			_panel	= new GuiGroupStatic(0);
			break;
		case GuiDefs.GROUPTYPE_SCROLLLIST:
			_panel	= new GuiScrollList(12, false);
			break;
		default:
			_panel	= null;
			break;
		}
		if (_panel != null)
		{
			_panel.setMargin(GuiDefs.DEFAULT_PADDING);
			_panel.setPivot(PivotPosition.BottomLeft);
			_panel.setPosition(0, 0, false);
			super.addChild(_panel);
		}
		// we can't directly add the title bar, as this.addChild()
		// is overridden to add to the layout
		titleBar		= new GuiTitleBar(this, title, true);
		super.addChild(titleBar);
		listenerRef		= 0;
	}

	//********************
	// EVENTS
	//********************

	@EventMethod
	public void onClick(PlayerGuiElementClickEvent event)
	{
		if (_callback == null)
			return;
		GuiElement	element	= event.getGuiElement();
		Player		player	= event.getPlayer();
		// on cancel button press, close the window and notify the caller
		if (titleBar.isCancelButton(element))
		{
			pop(player);
			_callback.onCall(player, GuiDefs.ABORT_ID, null);
			return;
		}
		// on other click events, notify the caller.
		// GuiTextField's are treated differently, as a click on them is only
		// reported with an id, without any data.
		Pair<Integer,Object>	data	= _panel.getItemData(element);
		if (data != null)
		{
			int	id	= data.getL();
			// if any non-internal id, forward id to callback
			if(id >= GuiDefs.ABORT_ID)
				_callback.onCall(player, id, (element instanceof GuiTextField) ? null : data.getR());
		}
	}

	@EventMethod
	public void onTextEntry(PlayerGuiInputEvent event)
	{
		if (_callback == null)
			return;
		Integer	id;
		if ( (id=_panel.getItemId(event.getGuiElement())) != null)
		{
			_callback.onCall(event.getPlayer(), id, event.getInput());
		}
	}

	//********************
	// PUBLIC METHODS
	//********************

	/**
	 * Sets the main panel of the window. Used when a special type of panel is required.
	 * @param	panel	the panel to use.
	 */
	public void setPanel(GuiGroup panel)
	{
		_panel	= panel;
		_panel.setPivot(PivotPosition.BottomLeft);
		_panel.setPosition(0, 0, false);
		super.addChild(_panel);
	}

	/**
	 * Sets the sizes of the main window panel.
	 * @param	width	the new panel width, in pixels.
	 * @param	height	the new panel height, in pixels.
	 */
	public void setPanelSize(int width, int height)
	{
		_panel.setSize(width, height, false);
	}

	/**
	 * Sets the callback function called upon click and text entry events.

	 * @param	callback	the new callback
	 */
	public void setCallback(GuiCallback callback)
	{
		this._callback	= callback;
	}

	/**
	 * Sets the margin between the contents of the window and its edges.
	 * @param value	the new margin (in pixels).
	 */
	public void setMargin(int value)	{ _panel.setMargin(value);	}

	/**
	 * Sets the padding (i.e. the minimum distance) between two side-by-side
	 * elements of the main window panel (in pixels),
	 * @param value	the new padding (in pixels).
	 */
	public void setPadding(int value)	{ _panel.setPadding(value);	}

	/**
	 * Returns a minimal width (in pixels) suitable to contain the whole title bar.
	 * @return	 a minimal width in pixels.
	 */
	public int getTitleBarMinWidth()	{ return titleBar.getMinWidth(); }

	/**
	 * Lays the window out, arranging all the children of the layout
	 * hierarchy.
	 * 
	 * This method is always called before showing the window to a player
	 * and it is usually not necessary to call it manually.
	 */
	public void layout()
	{
		int	tbw		= titleBar.getMinWidth();
		_panel.layout(tbw, 0);			// require the layout to be at least as wide as the title bar
		int height	= (int)_panel.getHeight();
		int	width	= (int)_panel.getWidth();
		// place the layout inside any window border
		int	borderW	= (int)getBorderThickness();
		_panel.setPosition(borderW, borderW, false);
		// final size of the dialogue box
		height		+= (int)titleBar.getHeight();
		setSize(width+borderW*2, height+borderW*2, false);
		// tell the title bar to re-position itself within the dialogue box
		titleBar.relayout();
	}

	public GuiLabel addTextItem(String text, Integer id, Object data)
	{
		return _panel.addTextItem(text, id, data);
	}

	/**
	 * Adds a GuiElement with the associated id and data as a direct child of
	 * the window. The element is positioned beside or below the last
	 * added child, depending on the type (RWGui.LAYOUT_HORIZ or
	 * RWGui.LAYOUT_VERT) of the window.
	 * 
	 * If id is not null, the element is active (the player can click on it),
	 * and events on it will be reported to the callback function;
	 * if id is null, the element is not active.
	 * 
	 * <p>id can be any Integer and id's should be all different from one
	 * another within each window.
	 * 
	 * <p>The data parameter can be any Java object and can store additional
	 * information required to deal with the element, when a click event is
	 * reported for it via the callback object. It can also be null if no
	 * additional info is needed for the element.
	 * 
	 * <p>id and data are reported by the callback object upon click and text
	 * entry events. GuiTextField's would generate both click and text entry
	 * events: they can be distinguished because click events on a GuiTextField
	 * will call the callback object with null data parameter, while text entry
	 * events will have the data parameter set to the element current text.
	 * 
	 * @param	element	the element to add.
	 * @param	id		the id associated with the element; may be null for
	 * 					inactive elements.
	 * @param	data	the data associated with the element; may be null for
	 * 					elements which need no additional data other than their id.
	 */
	public void addChild(GuiElement element, Integer id, Object data)
	{
		_panel.addChild(element, id, data);
	}

	/**
	 * Removes a GuiElement from the direct children of the window.
	 * 
	 * @param	element	The GuiElement to remove
	 */
/*	@Override
	public void removeChild(GuiElement element)
	{
		_panel.removeChild(element);
	}*/

	/**
	 * Displays the window on the player screen.
	 * 
	 * The window is laid out before being shown and the mouse cursor
	 * is turned on.
	 * @param	player	the player to show the window to.
	 */
	public void show(Player player)
	{
		layout();
		titleBar.show(player);
		_panel.show(player);
		listenerRef++;
		if (listenerRef == 1)
			plugin.registerEventListener(this);
		player.addGuiElement(this);
		player.setMouseCursorVisible(true);
	}

	/**
	 * Hides the window from the player screen, turning the
	 * mouse cursor off.
	 * 
	 * <p>The window resources are <b>not freed</b> and the window can be
	 * re-used if needed; when the window is no longer needed, its resources
	 * can be freed with the free() method, in addition to closing it.
	 * @param	player	the player from whose screen to remove the window.
	 * 					Removing the same window from the same player multiple
	 *					times has no effect and does no harm.
	 */
	public void hide(Player player)
	{
		if (titleBar != null)
			titleBar.hide(player);
		if (_panel != null)
			_panel.hide(player);
		player.removeGuiElement(this);
		listenerRef--;
		if (listenerRef <= 0)
			plugin.unregisterEventListener(this);
		player.setMouseCursorVisible(false);
	}

	/**
	 * Chains another GuiModalWindow in the 'display stack'.
	 * <p>The new window is displayed on the player screen 'over' this
	 * window, keeping the mouse cursor on.
	 * <p>As the Rising World API does not have the concept of window or
	 * of modality and any uncovered element of this window would remain
	 * clickable, this window is hidden (but not freed or destroyed); popping
	 * the new window away (with its pop() method) will show this window back
	 * as it was at the push time.
	 * @param	player	the player on whose screen to display the new window.
	 * @param	win		the new GuiModalWindow to display.
	 */
	public void push(Player player, GuiModalWindow win)
	{
		hide(player);
		win.prevWindow	= this;
		win.show(player);
	}

	/**
	 * Pops this window away from the 'display stack'.
	 * <p>This window is closed down and freed and the window (if any) which
	 * pushed it will be shown back at the state it had at the push time.
	 * <p>If this window was not pushed, nothing will be displayed and the
	 * mouse cursor will be turned off.
	 * <p>After using this method, the window is no longer functional: none of
	 * its methods can be used and the window cannot be shown again or used in
	 * any way.
	 * @param	player	the player from whose screen to pop this window.
	 */
	public void pop(Player player)
	{
		hide(player);
		free();
		if (prevWindow != null)
		{
			prevWindow.show(player);
			player.setMouseCursorVisible(true);
		}
		else
			player.setMouseCursorVisible(false);
	}

	/**
	 * Combines pop() and push(), removing (and destroying) this window and
	 * pushing win <i>in its place</i>.
	 * <p>The new window will become the 'next window'  
	 * @param	player	the player on whose screen to display the new window.
	 * @param	win		the new GuiModalWindow to display.
	 */
	public void poppush(Player player, GuiModalWindow win)
	{
		hide(player);
		free();
		if (prevWindow != null)
			prevWindow.prevWindow	= win;
		win.show(player);
		player.setMouseCursorVisible(true);
	}

	/**
	 * Returns the id associated with element, if element is one of the
	 * children of the window (recursively); or null otherwise.

	 * @param	element	the GuiElement to look for.
	 * @return	the id associated with element if present, null if not.
	 */
	public Integer getItemId(GuiElement element)
	{
		return _panel.getItemId(element);
	}

	/**
	 * Releases the resources used by the window. After this method has
	 * been called, the window cannot be used or displayed any longer.
	 * 
	 * The resources are in any case garbage collected once the window
	 * goes out of scope or all the references to it elapse. Using this method
	 * might be useful to speed up the garbage collection process, once the
	 * window is not longer needed.
	 */
	public void free()
	{
		if (titleBar != null)
		{
			titleBar.free();
			titleBar	= null;
		}
		if (_panel != null)
		{
			_panel.free();
			_panel		= null;
		}
	}

}
