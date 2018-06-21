/****************************
	A r e a P r o t e c t i o n / g u i  -  A Java package for common GUI functionalities.

	GuiDefs.java - Defines some common constants and utility functions.

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

import org.miwarre.ap.AreaProtection;
import net.risingworld.api.gui.GuiImage;
import net.risingworld.api.objects.Player;
import net.risingworld.api.utils.ImageInformation;

public class GuiDefs
{
	// Standard Sizes
	public static final		int		BORDER_THICKNESS= 2;
	public static final		int		BUTTON_SIZE		= 18;
	public static final		int		DEFAULT_PADDING	= 6;
	public static final		int		ITEM_SIZE		= 15;
	public static final		int		TITLE_SIZE		= 18;
	public static final		int		TEXTENTRY_HEIGHT= (ITEM_SIZE + 8);
	public static final		float	AVG_CHAR_WIDTH1	= 0.5f;		// the average char width at size 1
	// Standard Colours: backgrounds
	public static final		int		PANEL_COLOUR	= 0x202020FF;
	public static final		int		TITLEBAR_COLOUR	= 0x505050FF;
	public static final		int		BORDER_COLOUR	= 0x909090FF;
	public static final		int		ACTIVE_COLOUR	= 0x0060D0FF;
	public static final		int		INACTIVE_COLOUR	= 0x404040FF;
	// Standard colours: texts
	public static final		int		TEXT_COLOUR		= 0xFFFFFFFF;
	public static final		int		TITLE_COLOUR	= 0xFFFFFFFF;
	public static final		int		TEXT_SEL_COLOUR	= 0x00B0FFFF;
	public static final		int		TEXT_DIM_COLOUR	= 0x808080FF;

	// Stock Images
	public static final		int		ICN_ARROW_DOWN	= 0;
	public static final		int		ICN_ARROW_LEFT	= 1;
	public static final		int		ICN_ARROW_RIGHT	= 2;
	public static final		int		ICN_ARROW_UP	= 3;
	public static final		int		ICN_CHECK		= 4;
	public static final		int		ICN_CROSS		= 5;
	public static final		int		ICN_UNCHECK		= 6;
	public static final		int		ICN_PLUS		= 7;
	public static final		int		ICN_MINUS		= 8;
	public static final		int		ICN_RADIO_CHECK	= 9;
	public static final		int		ICN_RADIO_UNCHECK= 10;
	public static final		int		ICN_MIN				= 0;
	public static final		int		ICN_MAX				= ICN_RADIO_UNCHECK;

	// STANDARD CONTROL ID's
	/** The id reported by a click event on the default button of dialogue box. */
	public static final		int		OK_ID		= 0;
	/** The id reported by a click event on a close button. */
	public static final		int		ABORT_ID	= -1;
	// Id's used internally
	protected static final	int		INTERNAL_ID	= -2;

	// STANDARD RETURN CODES
	/** The operation has been successful. */
	public static final		int		ERR_SUCCESS				= 0;
	/** A parameter was out of range or invalid. */
	public static final		int		ERR_INVALID_PARAMETER	= -1;
	/** A resource (icon) looked for did not exist. */
	public static final		int		ERR_MISSING_RESOURCE	= -2;
	/** An item looked for did not exist. */
	public static final		int		ERR_ITEM_NOT_FOUND		= -3;

	// TYPES OF GROUPS
	/** No Guigroup at all! */
	public static final		int		GROUPTYPE_NONE			= -1;
	/** A GuiGroup with absolute positioning */
	public static final		int		GROUPTYPE_STATIC		= 0;
	/** A GuiScrollabelLsit type of GuiGroup */
	public static final		int		GROUPTYPE_SCROLLLIST	= 1;

	//
	// FIELDS
	//
	private static final	ImageInformation[]	stockIcons		= new ImageInformation[ICN_MAX-ICN_MIN+1];
	private static final	String[]			stockIconPaths =
			{	"/resources/arrowDown.png",	"/resources/arrowLeft.png",	"/resources/arrowRight.png",
				"/resources/arrowUp.png",	"/resources/check.png",		"/resources/cross.png",
				"/resources/uncheck.png",	"/resources/plus.png",		"/resources/minus.png",
				"/resources/radioCheck.png","/resources/radioUncheck.png" 
			};

	//********************
	// PUBLIC METHODS & CLASSES
	//********************

	/**
		Sets one of the stock icon image into a GuiImage element.

		@param	image	the GuiImage to set the icon image into
		@param	iconId	the id of the icon
		@return	INVALID_PARAMETER if iconId is out of range; SUCCESS otherwise.
	*/
	public static int setImage(GuiImage image, int iconId)
	{
		if (iconId < ICN_MIN || iconId > ICN_MAX)
			return ERR_INVALID_PARAMETER;
		if (stockIcons[iconId] == null)
		{
			stockIcons[iconId]	= new ImageInformation(AreaProtection.plugin.getPath() + stockIconPaths[iconId]);
		}
		image.setImage(stockIcons[iconId]);
		return ERR_SUCCESS;
	}

	/**
		Returns (an estimate of) the width of a GuiLabel text. Assumes the
		default font is used.

		@param	text		the text to measure
		@param	fontSize	the size of the font used
		@return	an estimate of the text width in pixels corresponding to the
				given font size
	*/
	public static float getTextWidth(String text, float fontSize)
	{
		return (fontSize * AVG_CHAR_WIDTH1 * text.length());
	}

	/**
		A utility class to hold two related objects.

		@param	<L>	the first (left) element of the pair; can be any Java object
		@param	<R>	the second (right)) element of the pair; can be any Java object
	 */
	public static class Pair<L,R>
	{
		private L l;
		private R r;
		public Pair(L l, R r)
		{
			this.l = l;
			this.r = r;
		}
		public	L		getL()		{ return l; }
		public	R		getR()		{ return r; }
		public	void	setL(L l)	{ this.l = l; }
		public	void	setR(R r)	{ this.r = r; }
	}

	/**
	 * An interface for the callback objects reporting click and text entry
	 * events to menus, dialogue boxes and similar.
	 * <p><b>Important</b>: Due to the way Rising World plug-ins are loaded,
	 * this interface <b>cannot be instantiated</b> from within the main Java
	 * class of a plug-in; a separate class has to be used to contain and
	 * instantiate the actual callback object (typically, a class sub-classing
	 * the GUI element for which the callback is used: a GuiMenu,
	 * a GuiDialogueBox, etc...).
	 */
	public abstract interface GuiCallback
	{
		public abstract void	onCall(Player player, int id, Object data);
	}

}
