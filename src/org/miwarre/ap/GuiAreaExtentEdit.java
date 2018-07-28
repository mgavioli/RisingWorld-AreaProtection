/****************************
	A r e a P r o t e c t i o n  -  A Rising World Java plug-in for area permissions.

	GuiAreaEdit.java - A dialogue box to edit area properties.

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

package org.miwarre.ap;

import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.gui.GuiTextField;
import net.risingworld.api.gui.PivotPosition;
import net.risingworld.api.objects.Player;
import org.miwarre.ap.gui.GuiDefs;
import org.miwarre.ap.gui.GuiDefs.GuiCallback;
import org.miwarre.ap.gui.GuiModalWindow;
import org.miwarre.ap.gui.GuiTitleBar;

/**
 *	A dialogue box to edit the 3D extent of an area.
 *
 */
public class GuiAreaExtentEdit extends GuiModalWindow
{
	private static final	int		PANEL_HEIGHT	= GuiDefs.TEXTENTRY_HEIGHT * 4 + GuiDefs.DEFAULT_PADDING * 5 + GuiDefs.ITEM_SIZE * 7;
	private static final	int		PANEL_WIDTH		= 400;
	private static final	int		WINDOW_HEIGHT	= GuiTitleBar.TITLEBAR_HEIGHT + PANEL_HEIGHT;
	// Y coordinates of dlg controls
	private static final	int		N_LBL_Y			= PANEL_HEIGHT - GuiDefs.DEFAULT_PADDING;
	private static final	int		N_TXT_Y			= N_LBL_Y + 4;
	private static final	int		W_LBL_Y			= N_TXT_Y - GuiDefs.TEXTENTRY_HEIGHT - GuiDefs.DEFAULT_PADDING;
	private static final	int		W_TXT_Y			= W_LBL_Y + 4;
	private static final	int		E_LBL_Y			= W_LBL_Y;
	private static final	int		E_TXT_Y			= W_TXT_Y;
	private static final	int		S_LBL_Y			= E_TXT_Y - GuiDefs.TEXTENTRY_HEIGHT - GuiDefs.DEFAULT_PADDING;
	private static final	int		S_TXT_Y			= S_LBL_Y + 4;
	private static final	int		BOTTOM_LBL_Y	= S_TXT_Y - GuiDefs.TEXTENTRY_HEIGHT - GuiDefs.ITEM_SIZE;
	private static final	int		BOTTOM_TXT_Y	= BOTTOM_LBL_Y + 4;
	private static final	int		TOP_LBL_Y		= BOTTOM_LBL_Y;
	private static final	int		TOP_TXT_Y		= BOTTOM_TXT_Y;
	private static final	int		CENTRE_LBL_Y	= BOTTOM_TXT_Y - GuiDefs.TEXTENTRY_HEIGHT - GuiDefs.ITEM_SIZE;
	private static final	int		EXTENT_LBL_Y	= CENTRE_LBL_Y - GuiDefs.ITEM_SIZE - GuiDefs.DEFAULT_PADDING;
	private static final	int		BUTT_Y			= EXTENT_LBL_Y - (GuiDefs.ITEM_SIZE * 7) / 2;
/*	private static final	int		CENTREX_LBL_Y	= PANEL_HEIGHT - GuiDefs.DEFAULT_PADDING;
	private static final	int		CENTREX_TXT_Y	= CENTREX_LBL_Y + 4;
	private static final	int		CENTREZ_LBL_Y	= CENTREX_LBL_Y - GuiDefs.TEXTENTRY_HEIGHT - GuiDefs.DEFAULT_PADDING;
	private static final	int		CENTREZ_TXT_Y	= CENTREZ_LBL_Y + 4;
	private static final	int		WIDTH_LBL_Y		= CENTREX_LBL_Y;
	private static final	int		WIDTH_TXT_Y		= CENTREX_TXT_Y;
	private static final	int		LENGTH_LBL_Y	= CENTREZ_LBL_Y - 4;
	private static final	int		LENGTH_TXT_Y	= CENTREZ_TXT_Y - GuiDefs.TEXTENTRY_HEIGHT - GuiDefs.DEFAULT_PADDING;
	private static final	int		BOTTOM_LBL_Y	= CENTREZ_LBL_Y - GuiDefs.TEXTENTRY_HEIGHT - GuiDefs.ITEM_SIZE - GuiDefs.DEFAULT_PADDING;
	private static final	int		BOTTOM_TXT_Y	= BOTTOM_LBL_Y - 4;
	private static final	int		TOP_LBL_Y		= BOTTOM_LBL_Y;
	private static final	int		TOP_TXT_Y		= BOTTOM_TXT_Y;
	private static final	int		BUTT_Y			= BOTTOM_TXT_Y + GuiDefs.TEXTENTRY_HEIGHT + (GuiDefs.ITEM_SIZE * 5) / 2;*/
	// X coordinates of dlg controls
//	private static final	int		N_LBL_X			= (PANEL_WIDTH - GuiDefs.DEFAULT_PADDING) / 2;
//	private static final	int		N_TXT_X			= N_LBL_X + GuiDefs.DEFAULT_PADDING;
//	private static final	int		W_LBL_X			= (PANEL_WIDTH - GuiDefs.DEFAULT_PADDING) / 3;
//	private static final	int		W_TXT_X			= W_LBL_X + GuiDefs.DEFAULT_PADDING;
//	private static final	int		E_LBL_X			= W_LBL_X + PANEL_WIDTH / 3;
//	private static final	int		E_TXT_X			= E_LBL_X + GuiDefs.DEFAULT_PADDING;
//	private static final	int		S_LBL_X			= N_LBL_X;
//	private static final	int		S_TXT_X			= N_TXT_X;
//	private static final	int		BOTTOM_LBL_X	= W_LBL_X;
//	private static final	int		BOTTOM_TXT_X	= W_TXT_X;
//	private static final	int		TOP_LBL_X		= E_LBL_X;
//	private static final	int		TOP_TXT_X		= E_TXT_X;
/*	private static final	int		CENTREX_LBL_X	= 100;
	private static final	int		CENTREX_TXT_X	= CENTREX_LBL_X + GuiDefs.DEFAULT_PADDING;
	private static final	int		CENTREZ_LBL_X	= CENTREX_LBL_X;
	private static final	int		CENTREZ_TXT_X	= CENTREX_TXT_X;
	private static final	int		WIDTH_LBL_X		= 150;
	private static final	int		WIDTH_TXT_X		= WIDTH_LBL_X + GuiDefs.DEFAULT_PADDING;
	private static final	int		LENGTH_LBL_X	= WIDTH_LBL_X;
	private static final	int		LENGTH_TXT_X	= WIDTH_TXT_X;
	private static final	int		BOTTOM_LBL_X	= CENTREX_LBL_X;
	private static final	int		BOTTOM_TXT_X	= CENTREX_TXT_X;
	private static final	int		TOP_LBL_X		= WIDTH_LBL_X;
	private static final	int		TOP_TXT_X		= WIDTH_TXT_X;*/
//	private static final	int		BUTT_X			= PANEL_WIDTH / 2;
	private static final	int		TXT_WIDTH		= 50;

	private static final	int		N_TXT_ID		= 1;
	private static final	int		W_TXT_ID		= 2;
	private static final	int		E_TXT_ID		= 3;
	private static final	int		S_TXT_ID		= 4;
/*	private static final	int		CENTREX_TXT_ID	= 1;
	private static final	int		CENTREZ_TXT_ID	= 2;
	private static final	int		WIDTH_TXT_ID	= 3;
	private static final	int		LENGTH_TXT_ID	= 4;*/
	private static final	int		TOP_TXT_ID		= 5;
	private static final	int		BOTTOM_TXT_ID	= 6;
	private static final	int		DEFAULTBUTT_ID	= 7;
	private static final	int		BUTTON_ID		= 8;

	//
	// FIELDS
	//
	private final	GuiCallback		callerCallback;
	private final	ProtArea.Extent	extent;
	private final	GuiLabel		centreLbl, extentLbl;
	private final	GuiTextField	nText, wText, eText, sText, tText, bText;
//	private final	Vector3f		areaFrom, areaTo;

	public GuiAreaExtentEdit(ProtArea.Extent extent, String areaName, Player player, final GuiCallback callback)
	{
		// construct the containing modal window (callback cannot be created and passed in the
		// call to the c'tor, as there is no context yet, until the modal window object is created)
		super(AreaProtection.plugin, String.format(Msgs.msg[Msgs.gui_areaExtentTitle], areaName), GuiDefs.GROUPTYPE_STATIC, 0, null);
		// create and set the callback
		setCallback(new DlgHandler());
		callerCallback	= callback;
		// get area extent global coordinates
		this.extent		= extent;
//		areaFrom = Utils.ChunkUtils.getGlobalPosition(area.getStartChunkPosition(), area.getStartBlockPosition());
//		areaTo   = Utils.ChunkUtils.getGlobalPosition(area.getEndChunkPosition(),   area.getEndBlockPosition());
		// Centre X label and text field
/*		GuiLabel		label	= addTextItem(Msgs.msg[Msgs.gui_centreX], 0, null);
		label.setPivot(PivotPosition.TopRight);
		label.setPosition(CENTREX_LBL_X, CENTREX_LBL_Y, false);
		GuiTextField	text	= new GuiTextField(CENTREX_TXT_X, CENTREX_TXT_Y, false, TXT_WIDTH, GuiDefs.TEXTENTRY_HEIGHT, false);
		String			val		= String.valueOf(extent.getCentreX());
		text.setText(val);
		addChild(text, CENTREX_TXT_ID, null);
		// Centre Z label and text field
		label	= addTextItem(Msgs.msg[Msgs.gui_centreZ], 0, null);
		label.setPivot(PivotPosition.TopRight);
		label.setPosition(CENTREZ_LBL_X, CENTREZ_LBL_Y, false);
		text	= new GuiTextField(CENTREZ_TXT_X, CENTREZ_TXT_Y, false, TXT_WIDTH, GuiDefs.TEXTENTRY_HEIGHT, false);
		val		= String.valueOf(extent.getCentreZ());
		text.setText(val);
		addChild(text, CENTREZ_TXT_ID, null);
		// Extent X label and text field
		label	= addTextItem(Msgs.msg[Msgs.gui_width], 0, null);
		label.setPivot(PivotPosition.TopRight);
		label.setPosition(WIDTH_LBL_X, WIDTH_LBL_Y, false);
		text	= new GuiTextField(WIDTH_TXT_X, WIDTH_TXT_Y, false, TXT_WIDTH, GuiDefs.TEXTENTRY_HEIGHT, false);
		val		= String.valueOf(extent.getExtentX());
		text.setText(val);
		addChild(text, WIDTH_TXT_ID, null);
		// Extent Z label and text field
		label	= addTextItem(Msgs.msg[Msgs.gui_length], 0, null);
		label.setPivot(PivotPosition.TopRight);
		label.setPosition(LENGTH_LBL_X, LENGTH_LBL_Y, false);
		text	= new GuiTextField(LENGTH_TXT_X, LENGTH_TXT_Y, false, TXT_WIDTH, GuiDefs.TEXTENTRY_HEIGHT, false);
		val		= String.valueOf(extent.getExtentX());
		text.setText(val);
		addChild(text, LENGTH_TXT_ID, null);*/
		int panelWidth	= Math.max(getTitleBarMinWidth(), PANEL_WIDTH);
		setPanelSize(panelWidth, PANEL_HEIGHT);
		// North X label and text field
//		Vector3f		extentFrom	= extent.getFrom();
//		Vector3f		extentTo	= extent.getTo();
		int			x		= (panelWidth - GuiDefs.DEFAULT_PADDING) / 2;
		GuiLabel	label	= addTextItem(Msgs.msg[Msgs.gui_N], 0, null);
		label.setPivot(PivotPosition.TopRight);
		label.setPosition(panelWidth / 2, N_LBL_Y, false);
		nText	= new GuiTextField(x + GuiDefs.DEFAULT_PADDING, N_TXT_Y, false, TXT_WIDTH, GuiDefs.TEXTENTRY_HEIGHT, false);
		addChild(nText, N_TXT_ID, null);
		// South label and text field
		label	= addTextItem(Msgs.msg[Msgs.gui_S], 0, null);
		label.setPivot(PivotPosition.TopRight);
		label.setPosition(x, S_LBL_Y, false);
		sText	= new GuiTextField(x + GuiDefs.DEFAULT_PADDING, S_TXT_Y, false, TXT_WIDTH, GuiDefs.TEXTENTRY_HEIGHT, false);
		addChild(sText, S_TXT_ID, null);
		// Top label and text field
		label	= addTextItem(Msgs.msg[Msgs.gui_topAreaHeight], 0, null);
		label.setPivot(PivotPosition.TopRight);
		label.setPosition(x, TOP_LBL_Y, false);
		tText	= new GuiTextField(x + GuiDefs.DEFAULT_PADDING, TOP_TXT_Y, false, TXT_WIDTH, GuiDefs.TEXTENTRY_HEIGHT, false);
		addChild(tText, TOP_TXT_ID, null);
		// West label and text field
		x		= panelWidth / 4;
		label	= addTextItem(Msgs.msg[Msgs.gui_W], 0, null);
		label.setPivot(PivotPosition.TopRight);
		label.setPosition(x, W_LBL_Y, false);
		wText	= new GuiTextField(x + GuiDefs.DEFAULT_PADDING, W_TXT_Y, false, TXT_WIDTH, GuiDefs.TEXTENTRY_HEIGHT, false);
		addChild(wText, W_TXT_ID, null);
		// Bottom label and text field
		label	= addTextItem(Msgs.msg[Msgs.gui_bottomAreaHeight], 0, null);
		label.setPivot(PivotPosition.TopRight);
		label.setPosition(x, BOTTOM_LBL_Y, false);
		bText	= new GuiTextField(x + GuiDefs.DEFAULT_PADDING, BOTTOM_TXT_Y, false, TXT_WIDTH, GuiDefs.TEXTENTRY_HEIGHT, false);
		addChild(bText, BOTTOM_TXT_ID, null);
		// East label and text field
		x		*= 3;
		label	= addTextItem(Msgs.msg[Msgs.gui_E], 0, null);
		label.setPivot(PivotPosition.TopRight);
		label.setPosition(x, E_LBL_Y, false);
		eText	= new GuiTextField(x + GuiDefs.DEFAULT_PADDING, E_TXT_Y, false, TXT_WIDTH, GuiDefs.TEXTENTRY_HEIGHT, false);
		addChild(eText, E_TXT_ID, null);
		// Defaul height button
		label	= addTextItem(Msgs.msg[Msgs.gui_setToDefault], DEFAULTBUTT_ID, null);
		label.setPivot(PivotPosition.TopLeft);
		label.setPosition(x + GuiDefs.DEFAULT_PADDING, TOP_LBL_Y, false);
		label.setColor(GuiDefs.ACTIVE_COLOUR);
		label.setClickable(true);
		// Area centre and extent labels
		centreLbl	= addTextItem("", 0, null);
		centreLbl.setPivot(PivotPosition.TopLeft);
		centreLbl.setPosition(GuiDefs.DEFAULT_PADDING, CENTRE_LBL_Y, false);
		extentLbl	= addTextItem("", 0, null);
		extentLbl.setPivot(PivotPosition.TopLeft);
		extentLbl.setPosition(GuiDefs.DEFAULT_PADDING, EXTENT_LBL_Y, false);
		setTexts();

		// [UPDATE] button
		label	= addTextItem(Msgs.msg[Msgs.gui_editUpdate], BUTTON_ID, null);
		label.setPivot(PivotPosition.Center);
		label.setPosition(panelWidth / 2, BUTT_Y, false);
		label.setColor(GuiDefs.ACTIVE_COLOUR);

		setSize(panelWidth + 2 * (int)getBorderThickness(), WINDOW_HEIGHT + 2 * (int)getBorderThickness(), false);
	}

	//********************
	// HANDLERS
	//********************

	private class DlgHandler implements GuiDefs.GuiCallback
	{
		@Override
		public void onCall(Player player, int id, Object data)
		{
			int		iVal;
			switch (id)
			{
			case N_TXT_ID:
				if (data == null)
					return;
				try	{	iVal	= Integer.valueOf((String)data);	}
				catch (NumberFormatException e)
				{		iVal	 = extent.getMaxZ();	}
				extent.setMaxZ(iVal);
				break;
			case W_TXT_ID:
				if (data == null)
					return;
				try	{	iVal	= Integer.valueOf((String)data);	}
				catch (NumberFormatException e)
				{		iVal	 = extent.getMinX();	}
				extent.setMinX(iVal);
				break;
			case E_TXT_ID:
				if (data == null)
					return;
				try	{	iVal	= Integer.valueOf((String)data);	}
				catch (NumberFormatException e)
				{		iVal = extent.getMaxX();	}
				extent.setMaxX(iVal);
				break;
			case S_TXT_ID:
				if (data == null)
					return;
				try	{	iVal	= Integer.valueOf((String)data);	}
				catch (NumberFormatException e)
				{		iVal = extent.getMinZ();	}
				extent.setMinZ(iVal);
				break;
			case TOP_TXT_ID:
				if (data == null)
					return;
				try	{	iVal	= Integer.valueOf((String)data);	}
				catch (NumberFormatException e)
				{		iVal = extent.getMaxY();	}
				extent.setMaxY(iVal);
				break;
			case BOTTOM_TXT_ID:
				if (data == null)
					return;
				try	{	iVal	= Integer.valueOf((String)data);	}
				catch (NumberFormatException e)
				{		iVal = extent.getMinY();	}
				extent.setMaxY(iVal);
				break;
			case DEFAULTBUTT_ID:
				extent.setMaxY(AreaProtection.heightTop);
				extent.setMinY(AreaProtection.heightBottom);
				break;
			case BUTTON_ID:
				if (callerCallback != null)
				{
					callerCallback.onCall(player, GuiDefs.OK_ID, extent);
				}
				pop(player);
			default:
				return;
			}
			setTexts();
		}
	}

	private void setTexts()
	{
		if (nText != null)
			nText.setText(String.valueOf(extent.getMaxZ()));
		if (sText != null)
			sText.setText(String.valueOf(extent.getMinZ()));
		if (wText != null)
			wText.setText(String.valueOf(extent.getMaxX()));
		if (eText != null)
			eText.setText(String.valueOf(extent.getMinX()));
		if (tText != null)
			tText.setText(String.valueOf(extent.getMaxY()));
		if (bText != null)
			bText.setText(String.valueOf(extent.getMinY()));
		if (centreLbl != null)
		{
			float		centreN	= (extent.getMaxZ() + extent.getMinZ() + 1) * 0.5f;
			float		centreE	= (extent.getMaxX() + extent.getMinX() + 1) * 0.5f;
			float		centreH	= (extent.getMaxY() + extent.getMinY() + 1) * 0.5f;
			String		nSign	= Msgs.msg[Msgs.gui_N];
			String		eSign	= Msgs.msg[Msgs.gui_W];
			if (centreN < 0)
				nSign	= Msgs.msg[Msgs.gui_S];
			if (centreE < 0)
				eSign	= Msgs.msg[Msgs.gui_E];
			centreLbl.setText(String.format(Msgs.msg[Msgs.gui_areaCentreFmt], centreE, eSign, centreH, centreN, nSign));
		}
		if (extentLbl != null)
		{
			extentLbl.setText(String.format(Msgs.msg[Msgs.gui_areaSpanFmt],
				(extent.getMaxX() - extent.getMinX())+1, (extent.getMaxY() - extent.getMinY())+1,
				(extent.getMaxZ() - extent.getMinZ())+1));
		}
	}
}
