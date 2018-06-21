/****************************
	A r e a P r o t e c t i o n  -  A Rising World Java plug-in for area permissions.

	GuiAreaProps.java - The sub-window panel to set the properties of an area

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

package org.miwarre.ap;

import org.miwarre.ap.gui.GuiDefs;
import org.miwarre.ap.gui.GuiGroupStatic;
import net.risingworld.api.gui.GuiImage;
import net.risingworld.api.gui.GuiLabel;

/**
 * A GuiGroupStatic panel to set/reset individual permissions for an area.
 * 
 * <p>It is intended to be used as part of a large GuiModalwindow.
 * <p>Permissions can be masked out to restrict editing to some permissions only.
 */
public class GuiAreaPerms extends GuiGroupStatic
{
	// Constants
	private static final	int		PERMS_PER_ROW			= 2;
	public static final		int		NUM_OF_AREAPERMS		= (Msgs.gui_editPermLastArea - Msgs.gui_editPermFirst + 1);
	private static final	int		AREAPERMS_PER_COLUMN	= ( (NUM_OF_AREAPERMS + 1) / PERMS_PER_ROW);
	public static final		int		NUM_OF_PLAYERPERMS		= (Msgs.gui_editPermLastUser - Msgs.gui_editPermFirst + 1);
	private static final	int		PLAYERPERMS_PER_COLUMN	= ( (NUM_OF_PLAYERPERMS + 1) / PERMS_PER_ROW);
	
	
	// Control positions X: the group heading
	private static final	int		HEADING_X		= 0;
	// Control positions X: the columns of permissions
	private static final	int		CHECKBOX_X		= HEADING_X;
	private static final	int		NAME_X			= CHECKBOX_X + GuiDefs.BUTTON_SIZE + GuiDefs.DEFAULT_PADDING;
	private static final	int		NAME_WIDTH		= 200;
	private static final	int		COLUMN_WIDTH	= NAME_X + NAME_WIDTH + GuiDefs.DEFAULT_PADDING;
	// Control positions X: the total panel width
	public static final		int		PANEL_WIDTH		= COLUMN_WIDTH * PERMS_PER_ROW;

	// Control positions Y: the total panel height (always allow for the max number of permissions)
	public static final		int		PANEL_HEIGHT	=
			GuiDefs.ITEM_SIZE + GuiDefs.DEFAULT_PADDING +							// heading height w/ its padding below
			PLAYERPERMS_PER_COLUMN * (GuiDefs.ITEM_SIZE + GuiDefs.DEFAULT_PADDING);	// num. rows * row hgt

	// Control positions: the group heading
	private static final	int		HEADING_Y		= PANEL_HEIGHT;
	// Control positions Y: the columns of permissions
	private static final	int		COLUMN_TOP_Y	= HEADING_Y - GuiDefs.ITEM_SIZE - GuiDefs.DEFAULT_PADDING;

	//
	// FIELDS
	//
	private	int				permissions;
	private GuiImage[]		permOnOff;

	/**
	 * @param	permissions	the initial permissions
	 * @param	permMask	the permission mask: if the bit of a permission is off,
	 *						that permission cannot be edited. The permission bits
	 *						are defined in AreaProtection.PERM_... codes.
	 * @param	forArea		true if the panel is for an area generic permissions,
	 *						false if the panel is for player-specific permissions.
	 */
	public GuiAreaPerms(int permissions, int permMask, boolean forArea)
	{
		super(0);
		setBorderThickness(GuiDefs.BORDER_THICKNESS, false);
		GuiLabel	label;
		this.permissions	= permissions;
		// Heading
		label	= addTextItem(Msgs.msg[forArea ? Msgs.gui_editPermissGeneral :
				Msgs.gui_editPermissSpecific], null, null);
		label.setPosition(HEADING_X, HEADING_Y, false);
		// The individual permissions
		permOnOff		= new GuiImage[NUM_OF_PLAYERPERMS];	// the check boxes
		int	permId		= -1;								// the current permission id
		int	currFlag;										// the current permission bit flag
		int	permPerCol	= forArea ? AREAPERMS_PER_COLUMN : PLAYERPERMS_PER_COLUMN;
		int	numOfPerms	= forArea ? NUM_OF_AREAPERMS : NUM_OF_PLAYERPERMS;
		int			x, y;
		x	= 0;
		// for each column
		for (int i = 0; i < PERMS_PER_ROW; i++)
		{
			y	= COLUMN_TOP_Y;							// each column starts here and goes down
			// for each row of each column, create one check box and one label
			for (int j = 0; j < permPerCol; j++)
			{
				permId++;
				if (permId >= numOfPerms)				// the last column may not be completely filled
					break;
				currFlag	= AreaProtection.permIdx2bit[permId];
				// show the on/off check box only if the flag is not filtered out by the flag mask
				if ((permMask & currFlag) != 0)
				{
					// create the check box image, add to the panel and set its image & position
					permOnOff[permId]	= new GuiImage(0, 0, false, GuiDefs.BUTTON_SIZE,
							GuiDefs.BUTTON_SIZE, false);
					addChild(permOnOff[permId], permId+1, null);
					GuiDefs.setImage(permOnOff[permId], (permissions & currFlag) != 0 ?
							GuiDefs.ICN_CHECK : GuiDefs.ICN_CROSS);
					permOnOff[permId].setPosition(x+CHECKBOX_X, y, false);
				}
				// add the text label and set its position in the panel
				label	= addTextItem(Msgs.msg[Msgs.gui_editPermFirst + permId], null, null);
				label.setPosition(x+NAME_X, y, false);
				y	-= GuiDefs.ITEM_SIZE + GuiDefs.DEFAULT_PADDING;
			}
			x	+= COLUMN_WIDTH;			// advance to next column horizontal position
		}
//		label	= addTextItem(Msgs.msg[Msgs.gui_notImplemented], null, null);
//		label.setPosition(0, GuiDefs.ITEM_SIZE + GuiDefs.DEFAULT_PADDING, false);
		setSize(PANEL_WIDTH, PANEL_HEIGHT, false);
	}

	public int	getPermissions()	{ return permissions;  }

	public boolean togglePermission(int index)
	{
		int	permToggle	= AreaProtection.permIdx2bit[index];
		permissions	^= permToggle;
		GuiDefs.setImage(permOnOff[index], (permissions & permToggle) != 0 ? 
				GuiDefs.ICN_CHECK : GuiDefs.ICN_CROSS);
		return (permissions & permToggle) != 0;
	}
}
