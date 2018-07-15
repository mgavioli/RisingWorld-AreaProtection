/****************************
	A r e a P r o t e c t i o n  -  A Rising World Java plug-in for area permissions.

	GuiAreaList.java - A menu to select an area to work on.

	Created by : Maurizio M. Gavioli 2017-03-08

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

import java.util.Map;
import java.util.Map.Entry;
import org.miwarre.ap.gui.GuiDefs;
import org.miwarre.ap.gui.GuiMenu;
import net.risingworld.api.objects.Player;

class GuiAreaList extends GuiMenu
{
	private static final	int			MAX_NUM_OF_MENULINES	= 25;
	private final	GuiDefs.GuiCallback	callback;
	private			boolean				hasItems;

	public GuiAreaList(Player player, boolean ownedOnly, GuiDefs.GuiCallback callback)
	{
		super(AreaProtection.plugin, Msgs.msg[Msgs.gui_selectArea], null, MAX_NUM_OF_MENULINES);
		setCallback(new MenuHandler());
		this.callback	= callback;
		hasItems		= false;
		Map<Integer,ProtArea>	areas	= ownedOnly ? Db.getAreas() : Db.getOwnedAreas(player);
		for (Entry<Integer,ProtArea> entry : areas.entrySet())
		{
			ProtArea	area	= entry.getValue();
			addTextItem(area.getName()+" ("+AreaProtection.getAreaCentre(area)+")", entry.getKey(), entry.getValue());
			hasItems	= true;
		}
		if (!hasItems)
			addTextItem(Msgs.msg[ownedOnly ? Msgs.gui_noDefinedArea : Msgs.gui_noOwnedArea], 1, null);
	}

	//********************
	// HANDLERS
	//********************

	private class MenuHandler implements GuiDefs.GuiCallback
	{
		@Override
		public void onCall(Player player, int id, Object obj)
		{
			if (id != GuiDefs.ABORT_ID)
			{
				pop(player);				// dismiss menu
				if (!hasItems)				// if menu had no items, do nothing
					id	= GuiDefs.ABORT_ID;	// and notify an ABORT
				if (callback != null)
					callback.onCall(player, id, obj);
			}
		}
	}

}
