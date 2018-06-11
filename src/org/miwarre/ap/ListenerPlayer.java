/****************************
	A r e a P r o t e c t i o n  -  A Rising World Java plug-in for area permissions.

	ListenerPlayer.java - The listener for player events

	Created by : Maurizio M. Gavioli 2017-02-25

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2017
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package org.miwarre.ap;

import net.risingworld.api.events.Cancellable;
import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.Listener;
import net.risingworld.api.events.player.PlayerCommandEvent;
import net.risingworld.api.events.player.PlayerConnectEvent;
import net.risingworld.api.events.player.PlayerEnterAreaEvent;
import net.risingworld.api.events.player.PlayerLeaveAreaEvent;
import net.risingworld.api.events.player.inventory.PlayerChestDropEvent;
import net.risingworld.api.events.player.inventory.PlayerChestToInventoryEvent;
import net.risingworld.api.events.player.inventory.PlayerInventoryToChestEvent;
//import net.risingworld.api.events.player.world.PlayerChangeObjectStatusEvent;
import net.risingworld.api.events.player.world.PlayerCreateBlueprintEvent;
import net.risingworld.api.events.player.world.PlayerDestroyBlockEvent;
import net.risingworld.api.events.player.world.PlayerDestroyConstructionEvent;
import net.risingworld.api.events.player.world.PlayerDestroyObjectEvent;
import net.risingworld.api.events.player.world.PlayerDestroyTerrainEvent;
import net.risingworld.api.events.player.world.PlayerDestroyVegetationEvent;
import net.risingworld.api.events.player.world.PlayerPlaceBlockEvent;
import net.risingworld.api.events.player.world.PlayerPlaceBlueprintEvent;
import net.risingworld.api.events.player.world.PlayerPlaceConstructionEvent;
import net.risingworld.api.events.player.world.PlayerPlaceGrassEvent;
import net.risingworld.api.events.player.world.PlayerPlaceObjectEvent;
import net.risingworld.api.events.player.world.PlayerPlaceTerrainEvent;
import net.risingworld.api.events.player.world.PlayerPlaceVegetationEvent;
import net.risingworld.api.events.player.world.PlayerPlaceWaterEvent;
import net.risingworld.api.events.player.world.PlayerRemoveConstructionEvent;
import net.risingworld.api.events.player.world.PlayerRemoveGrassEvent;
import net.risingworld.api.events.player.world.PlayerRemoveObjectEvent;
import net.risingworld.api.events.player.world.PlayerRemoveVegetationEvent;
import net.risingworld.api.events.player.world.PlayerRemoveWaterEvent;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.gui.PivotPosition;
import net.risingworld.api.objects.Player;

/**
 * Manages events for the plug-in.
 */
public class ListenerPlayer implements Listener
{
	// Constants
	public static final		int			INFO_FONT_SIZE			= 16;

	private static ListenerPlayer ourInstance = new ListenerPlayer();

	public static ListenerPlayer getInstance()		{	return ourInstance;	}

	private ListenerPlayer()
	{
	}

	//********************
	// EVENTS
	//********************

	/** Called by Rising World when the player connects to a world before spawning.

		@param	event	the connect event
	*/
	@EventMethod
	public void onPlayerConnect(PlayerConnectEvent event)
	{
		Player	player	= event.getPlayer();
		player.setAttribute(AreaProtection.key_areasShown, false);
		// The label with the names of the areas
		GuiLabel	info	= new GuiLabel("", AreaProtection.infoXPos, AreaProtection.infoYPos, false);
		info.setColor(AreaProtection.infoBkgColour);
		info.setFontColor(AreaProtection.infoFontColour);
		info.setFontSize(INFO_FONT_SIZE);
		info.setPivot(PivotPosition.BottomLeft);
		player.addGuiElement(info);
		player.setAttribute(AreaProtection.key_areasText, info);
		Db.loadPlayer(player);
		player.setListenForObjectInteractions(true);
	}

	/**	Called when the player issues a command ("/...") in the chat window
	
		@param event	the command event
	*/
	@EventMethod
	public void onPlayerCommand(PlayerCommandEvent event)
	{
		if (event.getCommand().split(" ")[0].equals(AreaProtection.commandPrefix) )
			AreaProtection.plugin.mainGui(event.getPlayer());
	}

	//
	// ENTER / LEAVE AREA
	//
	@EventMethod
	public void onPlayerEnterArea(PlayerEnterAreaEvent event)
	{
		int	retVal	= Db.onPlayerArea(event.getPlayer(), event.getArea(), true);
		if (retVal == AreaProtection.ERR_CANNOT_ENTER)
			event.setCancelled(true);
	}
	@EventMethod
	public void onPlayerLeaveArea(PlayerLeaveAreaEvent event)
	{
		int	retVal	= Db.onPlayerArea(event.getPlayer(), event.getArea(), false);
		if (retVal == AreaProtection.ERR_CANNOT_LEAVE)
			event.setCancelled(true);
	}

	//
	// DESTROY / PLACE BLOCK EVENTS
	//
	@EventMethod
	public void onPlayerDestroyBlock(PlayerDestroyBlockEvent event)
	{
		onCancellableEvent(event, event.getPlayer(), AreaProtection.PERM_DESTROYBLOCKS);
	}
	@EventMethod
	public void onPlayerPlaceBlock(PlayerPlaceBlockEvent event)
	{
		onCancellableEvent(event, event.getPlayer(), AreaProtection.PERM_PLACEBLOCKS);
	}

	//
	// DESTROY / PLACE / REMOVE CONSTRUCTION EVENTS
	//
	@EventMethod
	public void onPlayerDestroyConstruction(PlayerDestroyConstructionEvent event)
	{
		onCancellableEvent(event, event.getPlayer(), AreaProtection.PERM_DESTROYCONSTR);
	}
	@EventMethod
	public void onPlayerPlaceConstruction(PlayerPlaceConstructionEvent event)
	{
		onCancellableEvent(event, event.getPlayer(), AreaProtection.PERM_PLACECONSTR);
	}
	@EventMethod
	public void onPlayerRemoveConstruction(PlayerRemoveConstructionEvent event)
	{
		onCancellableEvent(event, event.getPlayer(), AreaProtection.PERM_REMOVECONSTR);
	}

	//
	// DESTROY / PLACE / REMOVE OBJECT EVENTS
	//
	@EventMethod
	public void onPlayerDestroyObject(PlayerDestroyObjectEvent event)
	{
		onCancellableEvent(event, event.getPlayer(), AreaProtection.PERM_DESTROYOBJECTS);
	}
	@EventMethod
	public void onPlayerPlaceObject(PlayerPlaceObjectEvent event)
	{
		onCancellableEvent(event, event.getPlayer(), AreaProtection.PERM_PLACEOBJECTS);
	}
	@EventMethod
	public void onPlayerRemoveObject(PlayerRemoveObjectEvent event)
	{
		onCancellableEvent(event, event.getPlayer(), AreaProtection.PERM_REMOVEOBJECTS);
	}

	//
	// DESTROY / PLACE TERRAIN EVENTS
	//
	@EventMethod
	public void onPlayerDestroyTerrain(PlayerDestroyTerrainEvent event)
	{
		onCancellableEvent(event, event.getPlayer(), AreaProtection.PERM_DESTROYTERRAIN);
	}
	@EventMethod
	public void onPlayerPlaceTerrain(PlayerPlaceTerrainEvent event)
	{
		onCancellableEvent(event, event.getPlayer(), AreaProtection.PERM_PLACETERRAIN);
	}

	//
	// DESTROY / PLACE / REMOVE VEGETATION EVENTS
	//
	@EventMethod
	public void onPlayerDestroyVegetation(PlayerDestroyVegetationEvent event)
	{
		onCancellableEvent(event, event.getPlayer(), AreaProtection.PERM_DESTROYVEGET);
	}
	@EventMethod
	public void onPlayerPlaceVegetation(PlayerPlaceVegetationEvent event)
	{
		onCancellableEvent(event, event.getPlayer(), AreaProtection.PERM_PLACEVEGET);
	}
	@EventMethod
	public void onPlayerRemoveVegetation(PlayerRemoveVegetationEvent event)
	{
		onCancellableEvent(event, event.getPlayer(), AreaProtection.PERM_REMOVEVEGET);
	}

	//
	// PLACE / REMOVE GRASS EVENTS
	//
	@EventMethod
	public void onPlayerPlaceGrass(PlayerPlaceGrassEvent event)
	{
		onCancellableEvent(event, event.getPlayer(), AreaProtection.PERM_PLACEGRASS);
	}
	@EventMethod
	public void onPlayerRemoveGrass(PlayerRemoveGrassEvent event)
	{
		onCancellableEvent(event, event.getPlayer(), AreaProtection.PERM_REMOVEGRASS);
	}

	//
	// PLACE / REMOVE WATER EVENTS
	//
	@EventMethod
	public void onPlayerRemoveWater(PlayerRemoveWaterEvent event)
	{
		onCancellableEvent(event, event.getPlayer(), AreaProtection.PERM_REMOVEWATER);
	}
	@EventMethod
	public void onPlayerPlaceWater(PlayerPlaceWaterEvent event)
	{
		onCancellableEvent(event, event.getPlayer(), AreaProtection.PERM_PLACEWATER);
	}

	//
	// CREATE / PLACE / REMOVE BLUEPRINT EVENT
	//
	@EventMethod
	public void onPlayerCreateBlueprint(PlayerCreateBlueprintEvent event)
	{
		onCancellableEvent(event, event.getPlayer(), AreaProtection.PERM_CREATEBLUEPR);
	}
	@EventMethod
	public void onPlayerPlaceBlueprint(PlayerPlaceBlueprintEvent event)
	{
		onCancellableEvent(event, event.getPlayer(), AreaProtection.PERM_PLACEBLUEPRINT);
	}
/*	@EventMethod
	public void onPlayerRemoveBlueprint(PlayerRemoveBlueprintEvent event)
	{
		onCancellableEvent(event, event.getPlayer(), AreaProtection.PERM_REMOVEBLUEPR);
	}
*/
	//
	// MISCELLANEOUS EVENTS
	//
//	@EventMethod
//	public void onPlayerChangeObjectStatus(PlayerChangeObjectStatusEvent event)
//	{
//		onCancellableEvent(event, event.getPlayer(), AreaProtection.PERM_CHANGESTATUS);
//	}
	@EventMethod
	public void onPlayerInventoryToChest(PlayerInventoryToChestEvent event)
	{
		onCancellableEvent(event, event.getPlayer(), AreaProtection.PERM_INVENT2CHEST);
	}
	@EventMethod
	public void onPlayerChestToInventory(PlayerChestToInventoryEvent event)
	{
		onCancellableEvent(event, event.getPlayer(), AreaProtection.PERM_CHEST2INVENT);
	}
	@EventMethod
	public void onPlayerChestDrop(PlayerChestDropEvent event)
	{
		onCancellableEvent(event, event.getPlayer(), AreaProtection.PERM_CHESTDROP);
	}

	//
	// PRIVATE METHODS
	//
	private void onCancellableEvent(Cancellable event, Player player, int permissionFlag)
	{
		if (!AreaProtection.adminNoPriv && player.isAdmin())		// any permission is enabled
			return;
		Integer	perms	= (Integer)player.getAttribute(AreaProtection.key_areaPerms);
		if (perms != null && (perms & permissionFlag) == 0)
			event.setCancelled(true);
	}
}
