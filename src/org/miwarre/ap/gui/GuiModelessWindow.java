/****************************
	A r e a P r o t e c t i o n  -  A Rising World Java plug-in for area permissions.

	gui/GuiModelessWindow.java - Displays and manages a modeless window.

	Created by : Maurizio M. Gavioli 2017-03-04

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2017
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package org.miwarre.ap.gui;

import java.util.List;
import net.risingworld.api.Plugin;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.gui.GuiPanel;
import net.risingworld.api.gui.PivotPosition;
import net.risingworld.api.objects.Player;

/**
 * A class implementing a mode-less window, made of a title bar and a number
 * of textual strings displayed one below the other.
 *
 * The window is placed near the lower left corner.
 * <p>The window has no behaviour, i.e. it just displays the texts and does
 * nothing else; in particular, the player cannot interact with it in any way.
 */
public class GuiModelessWindow extends GuiPanel
{
	// CONSTANTS
	//
	// The default position (relative to screen size) and size (absolute) of the GUI panel
	private static final	int		PANEL_XPOS		= 20;
	private static final	int		PANEL_YPOS		= 70;
	private static final	int		PANEL_WIDTH		= 300;
	private static final	int		PANEL_COLOUR	= 0xe0e0a0E0;	// 0x20202080;
	private static final	int		BORDER_COLOUR	= 0x000000FF;
	private static final	int		TEXT_COLOUR		= 0x000000FF;

	// The various labels
	private static final	int		TEXT_XPOS		= GuiDefs.DEFAULT_PADDING;
	private static final	int		TEXT_YDELTA		= GuiTitleBar.TITLEBAR_HEIGHT + GuiDefs.DEFAULT_PADDING;

	// FIELDS
	//
	private	Player		player;
	private	GuiLabel[]	labels;
	private GuiTitleBar	titleBar;

	/**
	 * Creates a mode-less window with given title and sequence of textual strings
	 * for the given player.
	 * The window is placed near the lower left corner and its height and width depend
	 * on the number and length of textual strings.
	 * 
	 * @param plugin	the plug-in this window will belong to 
	 * @param player	the player this windows will appear for
	 * @param titleText	the title of the window
	 * @param texts		the textual strings
	 */
	public GuiModelessWindow(Plugin plugin, Player player, String titleText, List<String> texts)
	{
		super();
		this.player	= player;

		// create a panel in the lower left corner
		setPivot(PivotPosition.TopLeft);
		setSize(PANEL_WIDTH, 0, false);
		setBorderColor(BORDER_COLOUR);
		setBorderThickness(GuiDefs.BORDER_THICKNESS, false);
		setColor(PANEL_COLOUR);
		setVisible(true);
		if (titleText != null && titleText.length() > 0)
		{
			titleBar	= new GuiTitleBar(this, titleText, false);
			addChild(titleBar);
			titleBar.show(player);
		}
		player.addGuiElement(this);
		if (texts != null)
			setTexts(texts);
	}

	/**
	 * Set new texts into the window. Window position, height and width are
	 * adjusted to the new number and length of the textual strings.
	 * 
	 * @param texts
	 */
	public void setTexts(List<String> texts)
	{
		int	numOfTexts	= texts.size();
		// compute height from number of text lines
		int	panelHeight	= GuiDefs.TITLE_SIZE + GuiDefs.ITEM_SIZE*texts.size() +
				GuiDefs.DEFAULT_PADDING*(texts.size()+3);

		// compute width from number of chars in title
		int panelWidth	= 0;
		if (titleBar != null)
			panelWidth	= titleBar.getMinWidth();
		// compute width from number of chars in each line of text
		int	textWidth;
		for (int i = 0; i < numOfTexts; i++)
			if ( (textWidth = (int)(GuiDefs.AVG_CHAR_WIDTH1 * texts.get(i).length() * GuiDefs.ITEM_SIZE)) > panelWidth)
				panelWidth = textWidth;
		panelWidth	+= 2 * GuiDefs.DEFAULT_PADDING;		// add left and right padding

		// Panel size are known: compute position of each item
		setPosition(PANEL_XPOS, PANEL_YPOS+panelHeight, false);
		setSize(panelWidth, panelHeight, false);
		if (titleBar != null)
			titleBar.relayout();

		// The texts
		releaseTexts();
		int		yPos	= panelHeight - TEXT_YDELTA;
		labels			= new GuiLabel[numOfTexts];
		for (int i = 0; i < numOfTexts; i++)
		{
			labels[i]	= new GuiLabel(texts.get(i), TEXT_XPOS, yPos, false);
			labels[i].setPivot(PivotPosition.TopLeft);
			labels[i].setFontColor(TEXT_COLOUR);
			labels[i].setFontSize(GuiDefs.ITEM_SIZE);
			labels[i].setClickable(false);
			addChild(labels[i]);
			player.addGuiElement(labels[i]);
			yPos		-= GuiDefs.DEFAULT_PADDING + GuiDefs.ITEM_SIZE;
		}
	}

	/**
	 * Closes the window and destroys allocated resources. To be used before
	 * relinquishing the object. After calling this method, the window cannot
	 * be used any longer.
	 */
	public void free()
	{
		if (titleBar != null)
		{
			titleBar.hide(player);
			titleBar.free();
			removeChild(titleBar);
			titleBar	= null;
		}
		player.removeGuiElement(this);
		releaseTexts();
	}

	private void releaseTexts()
	{
		if (labels != null)
		{
			int		numOfTexts	= labels.length;
			for (int i = 0; i < numOfTexts; i++)
			{
				player.removeGuiElement(labels[i]);
				removeChild(labels[i]);
				labels[i]	= null;
			}
			labels	= null;
		}
	}
}
