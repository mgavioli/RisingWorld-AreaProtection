/****************************
	A r e a P r o t e c t i o n  -  A Rising World Java plug-in for area permissions.

	GuiAreaList.java - A menu to select an area to work on.

	Created by : Maurizio M. Gavioli 2017-03-08

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2017
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package org.miwarre.ap;

import java.util.Map;
import java.util.Map.Entry;
import org.miwarre.ap.gui.GuiDefs;
import org.miwarre.ap.gui.GuiMenu;
import net.risingworld.api.objects.Player;

public class GuiAreaList extends GuiMenu
{
	private	GuiDefs.GuiCallback	callback;
	private	boolean			hasItems;

	public GuiAreaList(Player player, GuiDefs.GuiCallback callback)
	{
		super(AreaProtection.plugin, Msgs.msg[Msgs.gui_selectArea], null);
		setCallback(new MenuHandler());
		this.callback	= callback;
		hasItems		= false;
		Map<Integer,ProtArea>	areas	= Db.getOwnedAreas(player);
		for (Entry<Integer,ProtArea> entry : areas.entrySet())
		{
			ProtArea	area	= entry.getValue();
			addTextItem(area.getName()+" ("+AreaProtection.getAreaCentre(area)+")", entry.getKey(), entry.getValue());
			hasItems	= true;
		}
		if (!hasItems)
			addTextItem(Msgs.msg[Msgs.gui_noOwnedArea], 1, null);
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
