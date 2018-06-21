/****************************
	A r e a P r o t e c t i o n  -  A Rising World Java plug-in for area permissions.

	NewAreasCreation.java - A Runnable class which manages the creation of a new area

	Created by : Maurizio M. Gavioli 2017-02-25

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2017
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

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

import java.util.ArrayList;
import java.util.List;
import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.Listener;
import net.risingworld.api.events.player.PlayerKeyEvent;
import net.risingworld.api.objects.Player;
import net.risingworld.api.utils.Area;
import net.risingworld.api.utils.KeyInput;
import net.risingworld.api.utils.Vector3f;
import net.risingworld.api.utils.Utils.ChunkUtils;
import org.miwarre.ap.gui.*;

public class NewAreaCreation extends Thread implements Listener
{
	// CONSTANTS
	private static final	int		AREASELECT_ID		= 1;
	private static final	int		AREAEDIT_ID			= 2;

	// FIELDS
	private			Vector3f	from, to;
	private			int			step;
	private final	Player		player;
	private			boolean		running;
	private			boolean		aborted;
	private			GuiModelessWindow	dataWindow;

	/**
	 * Creates a New Area Creation (NAC) thread for player.
	 * <p>The thread stops when the Area Creation is completed or aborted.
	 * @param	player	the player this thread is used by.
	 */
	public NewAreaCreation(Player player)
	{
		this.player		= player;
	}

	/** The thread procedure */
	@Override
	public void run()
	{
		running	= true;
		aborted	= false;
		List<String>	texts	= new ArrayList<>();
		texts.add(Msgs.msg[Msgs.gui_areaKeys]);
		step	= AREASELECT_ID;
		dataWindow	= new GuiModelessWindow(AreaProtection.plugin, player, Msgs.msg[Msgs.gui_newArea], texts);
		AreaProtection.plugin.registerEventListener(this);
		player.registerKeys(KeyInput.KEY_LEFT, KeyInput.KEY_RIGHT, KeyInput.KEY_UP, KeyInput.KEY_DOWN,
				KeyInput.KEY_SUBTRACT, KeyInput.KEY_ADD, KeyInput.KEY_PGUP, KeyInput.KEY_PGDN,
				KeyInput.KEY_RETURN, KeyInput.KEY_ESCAPE);
		player.setListenForKeyInput(true);
		// enable area selection and wait for selection to be over
		player.enableAreaSelectionTool();
		while (true)
		{
			// give the server some rest during this idle loop!
			try								{ sleep(100); }
			catch (InterruptedException e)	{ /* nothing to do! */ }
			// running is toggled to false when area selection is interrupted
			// (onKey(), case AREASELECT_ID, sub-case KEY_ESCAPE)
			// or completed (AreaSizeGetter.onCall())
			if (!running)
				break;
		}
		player.disableAreaSelectionTool();
		player.setListenForKeyInput(false);
		dataWindow.free();
		dataWindow	= null;
		// area selection has been aborted; stop
		if (aborted)
		{
			AreaProtection.plugin.unregisterEventListener(this);
			return;
		}
		// show Area Edit dlg box and wait for dlg closed
		running	= true;
		step	= AREAEDIT_ID;
		ProtArea	area		= new ProtArea(from, to, "", AreaProtection.PERM_DEFAULT);
		GuiAreaEdit	winAreaEdit	= new GuiAreaEdit(this, area, player, GuiAreaEdit.TYPE_CREATE);
		winAreaEdit.show(player);
		while (true)
		{
			// give the server some more rest!
			try								{ sleep(100); }
			catch (InterruptedException e)	{ /* nothing to do! */ }
			// running is toggled to false by winAreaEdit when area properties
			// editing is done (either completed or aborted)
			if (!running)
				break;
		}
		try								{ sleep(100); }
		catch (InterruptedException e)	{ /* nothing to do! */ }
		AreaProtection.plugin.unregisterEventListener(this);
	}

	//********************
	// EVENTS
	//********************

	/**
	 *  Manages key pressed during the area selection phase
	 * @param	event	The key event being reported
	 */
	@EventMethod
	public void OnKey(PlayerKeyEvent event)
	{
		if (!event.isPressed())
			return;
		switch (step)
		{
		// while in AREA SELECTION
		case AREASELECT_ID:
			switch(event.getKeyCode())
			{
			case KeyInput.KEY_LEFT:
			case KeyInput.KEY_RIGHT:
			case KeyInput.KEY_UP:
			case KeyInput.KEY_DOWN:
			case KeyInput.KEY_PGDN:
			case KeyInput.KEY_PGUP:
			case KeyInput.KEY_ADD:
			case KeyInput.KEY_SUBTRACT:
				// update the area data in dataWindow, without stopping the thread
				player.getAreaSelectionData(new AreaSizeGetter(false));
				break;
			case KeyInput.KEY_RETURN:
//				running = false;		// NO! Wait for the AreaSizeGetter to return
				// update the area data in dataWindow, and then stop the thread
				player.getAreaSelectionData(new AreaSizeGetter(true));
				aborted	= false;
				break;
			case KeyInput.KEY_ESCAPE:
				running = false;
				aborted	= true;
				break;
			}
			break;
		}
	}

	//********************
	// PUBLIC METHODS
	//********************

	/*
	 * Stops the NAC thread.
	 */
	public void stopRun()	{ running	= false; }

	//********************
	// PRIVATE HELPER METHODS
	//********************

	/**
	 * Handles Player.getAreaSelectionData() notifications.
	 */
	private class AreaSizeGetter implements Callback<Area>
	{
		private boolean	stopRunning = false;

		/**
		 * Constructs a new AreaSizeGetter object.
		 * 
		 * @param	stopRunning	if true, the NAC thread will be stopped upon receiving
		 * 						an onCall() notification.
		 */
		public AreaSizeGetter(boolean stopRunning)
		{
			this.stopRunning	= stopRunning;
		}

		@Override
		public void onCall(Area result)
		{
			//if result is null, player did not select an area
			if(result != null)
			{
				AreaProtection.rearrangeArea(result);
				// update the texts in dataWindow
				ArrayList<String>	texts	= new ArrayList<>();
				texts.add(Msgs.msg[Msgs.gui_areaKeys]);
				texts.add(AreaProtection.getAreaCentre(result));
				texts.add(AreaProtection.getAreaSpans(result));
				dataWindow.setTexts(texts);
				// if asked to stop the thread, store area range and stop it now
				if (stopRunning)
				{
					from	= ChunkUtils.getGlobalPosition(result.getStartChunkPosition(),
							result.getStartBlockPosition());
					to		= ChunkUtils.getGlobalPosition(result.getEndChunkPosition(),
							result.getEndBlockPosition());
					running = false;
				}
			}
		}
	}

}
