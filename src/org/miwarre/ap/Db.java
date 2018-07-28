/****************************
	A r e a P r o t e c t i o n  -  A Rising World Java plug-in for area permissions.

	Db.java - The database management class

	Created by : Maurizio M. Gavioli 2017-02-25

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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.risingworld.api.Server;
import net.risingworld.api.database.Database;
import net.risingworld.api.database.WorldDatabase;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.objects.Player;
import net.risingworld.api.utils.Area;
import net.risingworld.api.utils.BoundingInformation;
import net.risingworld.api.utils.Utils.ChunkUtils;
import net.risingworld.api.utils.Utils.GeneralUtils;
import net.risingworld.api.utils.Vector3f;
import net.risingworld.api.utils.Vector3i;
import net.risingworld.api.worldelements.WorldArea;

/**
 * A data base class managing area and permission data.
 * <p>For efficiency and consistency, all methods and data are static.
 * <p>It manages persistent data storage via an SQLite DB, an in-memory
 * global cache of area data for efficiency and the player-specific area
 * permissions directly in the player attributes.
 * <p>The persistent DB is separate for each RW world.
 */
public class Db
{
	//
	// Constants
	//
	static final	int	LIST_TYPE_PLAYER	= 1;
	static final	int	LIST_TYPE_GROUP		= 2;
	static final	int	LIST_TYPE_MANAGERS	= 3;
	// Globals
	private	static	Map<Integer,ProtArea>	areas		= null;
			static	Map<Integer,String>		groupNames	= null;
	private	static	Map<String,Integer>		groupIds	= null;
	private static	Map<Integer,String>		playerNames	= null;
	private	static	Database				db			= null;

	//********************
	// PROTECTED METHODS
	//********************

	/**
		Initialises and opens the DB for this plug-in. Can be run at each
		script startup without destroying existing data.
	 */
	static void init()
	{
		if (db == null)
			db = AreaProtection.plugin.getSQLiteConnection(AreaProtection.plugin.getPath()
					+ "/ap-" + AreaProtection.plugin.getWorld().getName()+".db");

		// the areas, with the name, the extent and the default permissions
		db.execute(
			"CREATE TABLE IF NOT EXISTS `areas` ("
			+ "`id`      INTEGER PRIMARY KEY, "
			+ "`from_x`  INTEGER  NOT NULL DEFAULT ( 0 ),"
			+ "`from_y`  INTEGER  NOT NULL DEFAULT ( 0 ),"
			+ "`from_z`  INTEGER  NOT NULL DEFAULT ( 0 ),"
			+ "`to_x`    INTEGER  NOT NULL DEFAULT ( 0 ),"
			+ "`to_y`    INTEGER  NOT NULL DEFAULT ( 0 ),"
			+ "`to_z`    INTEGER  NOT NULL DEFAULT ( 0 ),"
			+ "`a_perm`  INTEGER NOT NULL DEFAULT ( 0 ),"
			+ "`name`    CHAR(64) NOT NULL DEFAULT ('[NoName]')"
			+ ");");
		// the users with specific permissions for one or more areas;
		// users with permissions for area id 0 (which does not exist) are area managers
		db.execute(
			"CREATE TABLE IF NOT EXISTS `users` ("
			+ "`id`      INTEGER PRIMARY KEY,"
			+ "`area_id` INTEGER NOT NULL DEFAULT ( 0 ),"
			+ "`user_id` INTEGER NOT NULL DEFAULT ( 0 ),"
			+ "`u_perm`  INTEGER NOT NULL DEFAULT ( 0 ),"
			+ "UNIQUE (`user_id`, `area_id`) ON CONFLICT REPLACE "
			+ ");");
		db.execute(
			"CREATE INDEX IF NOT EXISTS `user` ON `users` (`user_id`);"
		);
		// the server permission groups (primarily used to have a persistent id for each group)
		db.execute(
			"CREATE TABLE IF NOT EXISTS `perm_groups` ("
			+ "`id`      INTEGER PRIMARY KEY,"
			+ "`name`    CHAR(64) NOT NULL DEFAULT ('[NoName]') UNIQUE ON CONFLICT REPLACE"
			+ ");");
		// the groups with specific permissions for one or more areas
		db.execute(
			"CREATE TABLE IF NOT EXISTS `groups` ("
			+ "`id`       INTEGER PRIMARY KEY,"
			+ "`area_id`  INTEGER NOT NULL DEFAULT ( 0 ),"
			+ "`group_id` INTEGER NOT NULL DEFAULT ( 0 ),"
			+ "`g_perm`   INTEGER NOT NULL DEFAULT ( 0 ),"
			+ "UNIQUE (`group_id`, `area_id`) ON CONFLICT REPLACE "
			+ ");");
		db.execute(
			"CREATE INDEX IF NOT EXISTS `group` ON `groups` (`group_id`);"
		);
		// the chests with specific permissions
/*		db.execute(
			"CREATE TABLE IF NOT EXISTS `chests` ("
			+ "`id`       INTEGER PRIMARY KEY,"
			+ "`chest_id` INTEGER NOT NULL DEFAULT ( 0 ) UNIQUE ON CONFLICT REPLACE,"
			+ "`c_perm`   INTEGER NOT NULL DEFAULT ( 0 ),"
			+ "`name`     CHAR(64) NOT NULL DEFAULT ('')"
			+ ");");*/
		// using LinkedHashMap ensures areas are enumerated in the same order as they are inserted;
		// as areas are loaded from DB in name order, this makes area lists mostly in name order
		// (exceptions are newly created areas which are at the end and will be sordet at next
		// server and plug-in restart).
		areas	= new LinkedHashMap<>();
		initAreas();
		initGroups();
		AP3LUAImport();
	}
	static void deinit()
	{
		Server	server	= AreaProtection.plugin.getServer();
		for (Map.Entry<Integer,ProtArea> entry : areas.entrySet())
		{
			ProtArea	area	= entry.getValue();
			server.removeArea(area);
		}
		areas.clear();
		db.close();
		db = null;
	}

	/**
		Loads from the DB the area permissions for which a player has special permissions
		and caches them in player attributes.

 		@param	player	the target player.
	*/
	static void loadPlayer(Player player)
	{
		// admin/manager attribute
		player.setAttribute(AreaProtection.key_isAdmin, player.isAdmin());
		// the map with player-specific area permissions
		HashMap<Integer,Long>	permAreas	= new HashMap<>();
		player.setAttribute(AreaProtection.key_areas, permAreas);
		// the map with permissions for the areas the player currently is in
		HashMap<Integer,Long>	inAreas	= new HashMap<>();
		player.setAttribute(AreaProtection.key_inAreas, inAreas);
		// the cumulated permissions of all areas the player is currently in
		player.setAttribute(AreaProtection.key_areaPerms, AreaProtection.PERM_ALL);
		// fill the player-specific area permissions map from DB
		try (ResultSet result = db.executeQuery("SELECT `area_id`,`u_perm` FROM `users` WHERE `user_id` = "
				+ player.getDbID())) 
		{
			while (result.next())
			{
				int		areaId	= result.getInt(1);
				if (areaId == AreaProtection.AREAMANAGER_AREAID)
					player.setAttribute(AreaProtection.key_isAdmin, true);
				else
					permAreas.put(areaId, result.getLong(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// Due to a bug in RW, if player is flying AND within an area initially,
		// RW will not trigger the initial PlayerEnterAreaEvent; scan defined
		// areas and check the player is within any of them
		if (player.isFlying())
		{
			Vector3f	pos	= player.getPosition();
			for (Map.Entry<Integer,ProtArea> entry : areas.entrySet())
			{
				ProtArea	area	= entry.getValue();
				if (area.isPointInArea(pos) )
					onPlayerArea(player, area, true);
			}
		}
	}

	/**
		Manages entering / exiting an area by a player

		@param	player	the player
		@param	rwArea	the area
		@param	enter	true, if the player is entering the area, false if he is leaving it
		@return	on entering an area in which the player cannot enter, ERROR_CANNOT_ENTER; otherwise SUCCESS
	*/
	static int onPlayerArea(Player player, Area rwArea, boolean enter)
	{
		ProtArea	area;
		// retrieve the permissionArea matching the given RW rwArea
		if ( (area= matchArea(rwArea)) == null)
			return AreaProtection.ERR_NOTFOUND;

		// retrieve the list of areas the player is in and set an initial all-permission for the player
//		@SuppressWarnings("unchecked")
		HashMap<Integer,Long>	inAreas		= (HashMap<Integer, Long>)player.getAttribute(AreaProtection.key_inAreas);
		// retrieve the list of areas the player has specific permission for
//		@SuppressWarnings("unchecked")
		HashMap<Integer,Long>	areaPerms	= (HashMap<Integer, Long>)player.getAttribute(AreaProtection.key_areas);
		long					cumulPerm	= AreaProtection.PERM_ALL;
		int						retVal		= AreaProtection.ERR_SUCCESS;
		// if not admin OR no admin special privilege,
		// retrieve the permissions for this player and this area.
		Long					areaPerm	= AreaProtection.PERM_ALL;
		if (!(Boolean)player.getAttribute(AreaProtection.key_isAdmin) || AreaProtection.adminNoPriv)
		{
			if (areaPerms != null)
				areaPerm	= areaPerms.get(area.id);	// the player-specific permission for this area
			if (areaPerm == null)						// if no player-specific permissions...
			{
				Long	groupPerm;						// ...get the group the player belongs to
				String	groupName	= player.getPermissionGroup();
				// convert group name into group ID
				// and look for group-specific permission for this area
				Integer	groupId;
				if (groupName != null && !groupName.isEmpty() &&
						(groupId=groupIds.get(groupName)) != null &&
						(groupPerm = area.groups.get(groupId)) != null)
					areaPerm	= groupPerm;			// if found, use them as player perms. for area
				else									// if neither group-specific permission (or no group)
					areaPerm	= area.permissions;		// ...use default area permissions
			}
		}

		// upon entering a new area
		if (enter)
		{
			// if player cannot enter this area, return so and do nothing else
			if ((areaPerm & AreaProtection.PERM_ENTER) == 0)
				return AreaProtection.ERR_CANNOT_ENTER;
			// otherwise, add this area to the list of areas the player is in
			if (inAreas != null)
				inAreas.put(area.id, areaPerm);
		}
		// upon leaving an area
		else
		{
			// if player cannot leave this area, return so and do nothing else
			if ((areaPerm & AreaProtection.PERM_LEAVE) == 0)
				return AreaProtection.ERR_CANNOT_LEAVE;
			// otherwise, remove this area from the list of areas the player is in
			if (inAreas != null)
				inAreas.remove(area.id);
		}

		// in any case, re-compute current cumulative permissions and area info text for the player.
		// The cumulative permissions are the logical AND of the permissions
		// (either default or group-specific or player-specific)
		// of all the areas the player is currently in.
		if (inAreas != null)
		{
			String	text	= "";
			for (Map.Entry<Integer,Long> entry : inAreas.entrySet())
			{
				String	name	= areas.get(entry.getKey()).getName();	// the area name
				if (name != null)
				{
					// chain names of areas the player is in
					text	+= (text.isEmpty() ? " " : "| ");
					text	+= name + " ";
				}
// PERM AND
//				cumulPerm	&= entry.getValue();		// accumulate permissions
// PERM OR
				cumulPerm	|= entry.getValue();		// accumulate permissions
			}
			if ((Boolean)player.getAttribute(AreaProtection.key_isAdmin))
				text += "| Priv. " + (AreaProtection.adminNoPriv ? "OFF" : "ON");
			((GuiLabel)player.getAttribute(AreaProtection.key_areasText)).setText(text);
		}
		// if admin (and admin privileges are not limited), any permission is enabled
		if ((Boolean)player.getAttribute(AreaProtection.key_isAdmin) && !AreaProtection.adminNoPriv)
			cumulPerm	= AreaProtection.PERM_ALL;
		player.setAttribute(AreaProtection.key_areaPerms, cumulPerm);
		return retVal;
	}

	//********************
	//	AREA MANAGEMENT
	//********************

	/**
	 * Returns a Map with all the defined areas, hopefully in alphabetic order by name.
	 * @return a Map with all the defined areas.
	 */
	static Map<Integer,ProtArea> getAreas()		{ return areas; }

	/**
		Adds the area to the DB and to the local cache.

		@param	area	the area to add
		@return	An AreaProtection error code.
	*/
	static int addArea(ProtArea area)
	{
		if (area == null)
			return AreaProtection.ERR_INVALID_ARG;
		Vector3f from	= ChunkUtils.getGlobalPosition(area.getStartChunkPosition(), area.getStartBlockPosition());
		Vector3f to		= ChunkUtils.getGlobalPosition(area.getEndChunkPosition(), area.getEndBlockPosition());
		// prepare name parameter to avoid quoting issues
		try(PreparedStatement stmt	= db.getConnection().prepareStatement(
				"INSERT INTO `areas` (from_x,from_y,from_z,to_x,to_y,to_z,a_perm,name) VALUES ("
				+(int)from.x+","+(int)from.y+","+(int)from.z+","
				+(int)to.x+","+(int)to.y+","+(int)to.z+","
				+area.permissions+",?)")
		)
		{
			stmt.setString(1, area.name);
			stmt.executeUpdate();
			try (ResultSet idSet = stmt.getGeneratedKeys())
			{
				if (idSet.next())
				{
					int	newId	= idSet.getInt(1);
					area.id		= newId;
//					areas.put(newId, area);
					insertNewArea(area);
					AreaProtection.plugin.getServer().addArea(area);
				}
			}
		} catch (SQLException e)
		{
			e.printStackTrace();
			return AreaProtection.ERR_DB;
		}
		// show the new area to any player with area display turned on
		for(Player player : AreaProtection.plugin.getServer().getAllPlayers())
		{
			if (area.isPointInArea(player.getPosition()))	// if player happens to be inside the area,
				onPlayerArea(player, area, true);			// notify him he just entered it
			if ((boolean)player.getAttribute(AreaProtection.key_areasShown))
				showAreaToPlayer(player, area);
		}
		return AreaProtection.ERR_SUCCESS;
	}

	/**
	 * Deletes the given area from DB, from RW World and from player data/attributes
	 * @param area	the area to delete
	 * @return	one of the AreaProtection.ERR_ codes.
	 */
	@SuppressWarnings("unchecked")
	static int deleteArea(ProtArea area)
	{
		int		areaId	= area.id;
		// delete area data from DB
		db.executeUpdate("DELETE FROM `users` WHERE area_id="+areaId);
		db.executeUpdate("DELETE FROM `areas` WHERE id="+areaId);
		// delete RW Area
		AreaProtection.plugin.getServer().removeArea(area);
		// remove from player caches and for areas shown to players
		HashMap<Integer,Long>	inAreas;	// the areas the player is in w/ their permissions
		HashMap<Integer,Long>	permAreas;	// the areas for which the player has special permissions
		for(Player player : AreaProtection.plugin.getServer().getAllPlayers())
		{
			if ( (inAreas = (HashMap<Integer,Long>)player.getAttribute(AreaProtection.key_inAreas)) != null)
			{
				if (inAreas.remove(areaId) != null)		// if the player was inside this area,
					onPlayerArea(player, area, false);	// notify him he left it
			}
			if ( (permAreas	= (HashMap<Integer,Long>)player.getAttribute(AreaProtection.key_areas)) != null)
				permAreas.remove(areaId);
			if ((boolean)player.getAttribute(AreaProtection.key_areasShown))
				player.removeWorldElement(area.worldArea);
		}
		// remove from local area list
		areas.remove(area.id);
		return AreaProtection.ERR_SUCCESS;
	}

	/**
	 * Updates an existing area with the data passed in. The area to update is specified
	 * by the id field of the passed data.
	 * @param area	the new area data.
	 * @return		an AreaProtection.ERR_ error code.
	 */
	static int updateArea(ProtArea area)
	{
		if (area == null || area.id < 1)
			return AreaProtection.ERR_INVALID_ARG;
		// update the DB definition of this area
		Vector3f from	= ChunkUtils.getGlobalPosition(area.getStartChunkPosition(), area.getStartBlockPosition());
		Vector3f to		= ChunkUtils.getGlobalPosition(area.getEndChunkPosition(), area.getEndBlockPosition());
		// prepare name parameter to avoid quoting issues
		String	query	= "UPDATE `areas` SET from_x=" + (int)from.x
				+ ",from_y=" + (int)from.y
				+ ",from_z=" + (int)from.z
				+ ",to_x="   + (int)to.x
				+ ",to_y="   + (int)to.y
				+ ",to_z="   + (int)to.z
				+ ",a_perm=" + area.permissions
				+ ",name=? WHERE id=" + area.id;
		try(PreparedStatement stmt	= db.getConnection().prepareStatement(query) )
		{
			stmt.setString(1, area.name);
			stmt.execute();
		} catch (SQLException e)
		{
			e.printStackTrace();
			return AreaProtection.ERR_DB;
		}
		// update local cache too
		ProtArea	oldArea	= areas.get(area.id);	// get existing PermArea with same id
		if (oldArea != null)						// if any exists, check extent
		{
			// if extent is different, remove old RW area and add new
			// (extent of RW areas cannot be changed, once created)
			if (oldArea.getEndBlockPosition() != area.getEndBlockPosition() ||
					oldArea.getEndChunkPosition() != area.getEndChunkPosition() ||
					oldArea.getStartBlockPosition() != area.getStartBlockPosition() ||
					oldArea.getStartChunkPosition() != area.getStartChunkPosition())
			{
				AreaProtection.plugin.getServer().removeArea(oldArea);
				AreaProtection.plugin.getServer().addArea(area);
			}
			// update PermArea in cache, unless it is the same object as the area it would replace
			if (area != oldArea)
				areas.put(area.id, area);
			// check any player changed within/without status and show to him updated area if required
			for(Player player : AreaProtection.plugin.getServer().getAllPlayers())
			{
				Vector3f	playerPos	= player.getPosition();
				boolean		nowWithin;
				// if player within/without area status changed
				if ( (nowWithin=area.isPointInArea(playerPos)) != oldArea.isPointInArea(playerPos))	// if player happens to be inside the area,
					onPlayerArea(player, area, nowWithin);		// notify him he just entered or leaved the area
				// if areas are shown to player, hide old area and show new area
				if ((boolean)player.getAttribute(AreaProtection.key_areasShown))
				{
					if (oldArea.worldArea != null)
						player.removeWorldElement(oldArea.worldArea);
					showAreaToPlayer(player, area);
				}
			}
		}
		return AreaProtection.ERR_SUCCESS;
	}

	/**
	 * Retrieves the area with the given id.
	 * @param	id	the id of the area to retrieve
	 * @return	the area with that id or null if no area has that id.
	 */
	static ProtArea getAreaFromId(int id)
	{
		return areas.get(id);
	}

	//********************
	//	PLAYER MANAGEMENT
	//********************

	/**
	 * Adds a player/permissions pair to the player list of an area,
	 * both in the DB and in the area object.
	 * @param	area		the ares to add the player to.
	 * @param	playerId	the player id to add.
	 * @param	permissions	the permissions to add
	 * @return	an AreaProtection.ERR_ error code.
	 */
	static int addPlayerToArea(ProtArea area, int playerId, long permissions, int type)
	{
		if (area == null || area.id == 0)
			return AreaProtection.ERR_INVALID_ARG;
		// add the player/perm for this area to the DB
		// prepare name parameter to avoid quoting issues
		try(PreparedStatement stmt	= db.getConnection().prepareStatement(
				type == LIST_TYPE_GROUP ?
						"INSERT OR REPLACE INTO `groups` (area_id,group_id,g_perm) VALUES ("+area.id+",?,"+permissions+")"
					:	"INSERT OR REPLACE INTO `users`  (area_id,user_id,u_perm) VALUES ("+area.id+",?,"+permissions+")"
				)
		)
		{
			stmt.setInt(1, playerId);
			stmt.executeUpdate();
			if (type == LIST_TYPE_GROUP)
				area.groups.put(playerId, permissions);
			else
			{
				area.players.put(playerId, permissions);
				// if the player is connected right now, add the details to the player
				// list of areas for which he has special permissions
				Player	player	= connectedPlayerFromDBID(playerId);
				if (player != null)
				{
					if (area.id == AreaProtection.AREAMANAGER_AREAID)
						player.setAttribute(AreaProtection.key_isAdmin, true);
					else
					{
						// the map with player-specific area permissions
						@SuppressWarnings("unchecked")
						HashMap<Integer,Long>	permAreas	=
								(HashMap<Integer, Long>)player.getAttribute(AreaProtection.key_areas);
						if (permAreas != null)
							permAreas.put(area.id, permissions);
						// if player within that area, recompute cumulative permissions
						if (area.isPointInArea(player.getPosition()))
							onPlayerArea(player, area, true);
					}
				}
			}
		} catch (SQLException e)
		{
			e.printStackTrace();
			return AreaProtection.ERR_DB;
		}
		return AreaProtection.ERR_SUCCESS;
	}

	/**
	 * Remove a player from the player list of an area,
	 * both in the DB and in the area object.
	 * @param	area		the ares to remove the player from.
	 * @param	playerId	the player id to remove.
	 * @return	an AreaProtection.ERR_ error code.
	 */
	static int removePlayerFromArea(ProtArea area, int playerId, int type)
	{
		if (area == null || area.id == 0)
			return AreaProtection.ERR_INVALID_ARG;
		// remove the player row(s) for this area from the DB
		// prepare name parameter to avoid quoting issues
		try(PreparedStatement stmt	= db.getConnection().prepareStatement(
				type == LIST_TYPE_GROUP ?
						"DELETE FROM `groups` WHERE group_id = ? AND area_id="+area.id
					:	"DELETE FROM `users`  WHERE user_id = ? AND area_id="+area.id)
		)
		{
			stmt.setInt(1, playerId);
			stmt.executeUpdate();
			if (type == LIST_TYPE_GROUP)
				area.groups.remove(playerId);
			else
			{
				area.players.remove(playerId);
				// if the player is connected right now, remove the details from the player
				// list of areas for which he has special permissions
				Player	player	= connectedPlayerFromDBID(playerId);
				if (player != null)
				{
					if (area.id == AreaProtection.AREAMANAGER_AREAID)
						player.setAttribute(AreaProtection.key_isAdmin, player.isAdmin());
					else
					{
						// the map with player-specific area permissions
						@SuppressWarnings("unchecked")
						HashMap<Integer,Long>	permAreas	=
								(HashMap<Integer,Long>)player.getAttribute(AreaProtection.key_areas);
						if (permAreas != null)
							permAreas.remove(area.id);
						// if player within that area, recompute cumulative permissions
						if (area.isPointInArea(player.getPosition()))
							onPlayerArea(player, area, true);
					}
				}
			}
		} catch (SQLException e)
		{
			e.printStackTrace();
			return AreaProtection.ERR_DB;
		}
		return AreaProtection.ERR_SUCCESS;
	}

	/**
	 * Retrieves the special permissions of all players / groups for a given area.
	 * 
	 * @param	areaId	the id of the area for which to retrieve permissions
	 * @param	type	either LIST_TYPE_PLAYER or LIST_TYPE_GROUP
	 * @return			a Map with player name and player permissions for this area.
	 */
	static Map<Integer,Long> getAllPlayerPermissionsForArea(int areaId, int type)
	{
		Map<Integer,Long> areaUsers	= new HashMap<>();
		// run the query from a separate statement, so that it can be
		// run in parallel with other queries.
		try (Statement	stmt	= db.getConnection().createStatement())
		{
			ResultSet result	=
					stmt.executeQuery(
						type == LIST_TYPE_PLAYER
						?	"SELECT user_id,  u_perm FROM `users`  WHERE area_id = "+areaId
						:	"SELECT group_id, g_perm FROM `groups` WHERE area_id = "+areaId
							);
			while(result.next())
				areaUsers.put(result.getInt(1), result.getLong(2));
			result.close();
		}
		catch(SQLException e)
		{
			//on errors, do nothing and simply use what we got.
		}
		return areaUsers;
	}

	/**
	 * Returns the special permissions a player has for a specific area.
	 * <p>If the player is an admin, unconditionally returns all the permissions.
	 * <p>If the player has no special permissions for this area or is null,
	 * returns the generic permissions for this area.
	 * <p>If the area do not exists, return 0.
	 * @param	player	the player for whom to retrieve the special permissions
	 * @param	areaId	the area
	 * @return	the player permissions for the area (see details above).
	 */
	static long getPlayerPermissionsForArea(Player player, int areaId)
	{
		if (player != null)
		{
			// if player is an admin, he has all the permissions,
			// unless revoked by settings
			if ((Boolean)player.getAttribute(AreaProtection.key_isAdmin) && !AreaProtection.adminNoPriv)
				return AreaProtection.PERM_ALL;
			// the map with player-specific area permissions
			@SuppressWarnings("unchecked")
			HashMap<Integer,Long>	permAreas	=
					(HashMap<Integer,Long>)player.getAttribute(AreaProtection.key_areas);
			if (permAreas != null)
			{
				// if the map exists, look for permissions for this specific area
				Long	perms	= permAreas.get(areaId);
				// if specific permissions exists, return them
				if (perms != null)
					return perms;
			}
		}
		// if specific permission do not exist, or the player has no permissions map,
		// or player do not exist or is not connected, look for generic permissions for this area
		ProtArea	area	= areas.get(areaId);
		// if the area exists, return its generic permissions
		if (area != null)
			return area.permissions;
		return 0;			// if the area do not exist, return no permissions at all
	}

	/**
	 * Retrieves the areas owned by a player.
	 * <p>Any admin implicitly owns all areas.
	 * @param player	the player to retrieve areas for.
	 * @return			a Map with area id and area data for areas owned by the player.
	 */
	static Map<Integer,ProtArea> getOwnedAreas(Player player)
	{
		// if player is an admin, return the list of all known areas.
		if ((Boolean)player.getAttribute(AreaProtection.key_isAdmin))
			return areas;

		Map<Integer,ProtArea> ownedAreas	= new HashMap<>();
		try(ResultSet result = db.executeQuery("SELECT area_id FROM `users` WHERE user_id = '" +
				player.getDbID() + "' AND (u_perm & ("+
				AreaProtection.PERM_OWNER + " | " + AreaProtection.PERM_ADDPLAYER + ")) != 0"))
		{
			while(result.next())
			{
				int			id		= result.getInt(1);
				ProtArea	area	= areas.get(id);
				ownedAreas.put(id, area);
			}
			result.close();
		}
		catch(SQLException e)
		{
			//on errors, do nothing and simply use what we got.
		}
		return ownedAreas;
	}

	/**
	 * Gets the player permissions for an arbitrary point.
	 * @param	player	the player to retrieve permissions for
	 * @param	point	the point
	 * @return	the player permission for the point.
	 */
	public static long getPlayerPermissionsForPoint(Player player, Vector3f point)
	{
		// if admin AND admins are not demoted, return all permissions
		if ((Boolean)player.getAttribute(AreaProtection.key_isAdmin) && !AreaProtection.adminNoPriv)
			return AreaProtection.PERM_ALL;

		// retrieve the player group and convert group name into group ID
		String		groupName	= player.getPermissionGroup();
		Integer		groupId	= null;
		if (groupName != null && !groupName.isEmpty())
				groupId	= groupIds.get(groupName);

		// retrieve the list of areas the player has specific permission for
		HashMap<Integer,Long>	playerPerms	= (HashMap<Integer, Long>)player.getAttribute(AreaProtection.key_areas);
		long					cumulPerm	= AreaProtection.PERM_ALL;			// permissions default to everything

		Long		aPerm;
		// scan all areas to collect all areas which contain the point
		for (ProtArea area : areas.values())
		{
			// if the current area contains the point...
			if (area.isPointInArea(point))
			{
				// ..check the player has special permissions to the area
				if ( (aPerm=playerPerms.get(area.id)) == null)
					// if the player has no special permission,
					// check the player belongs to a group
					// and the area has special permissions for that group
					if (groupId != null)
						aPerm	= area.groups.get(groupId);
				// if no group, use generic area permissions
				if (aPerm == null)
					aPerm	= area.permissions;
				// mask cumulative permissions with player permissions for this area
// PERM AND
//				cumulPerm	&= aPerm;
// PERM OR
				cumulPerm	|= aPerm;
			}
		}
		return cumulPerm;
	}

	/**
	 * Gets the player permissions for an arbitrary 3D extent.
	 * The returned value is the bitwise AND of the player permissions for all the areas
	 * which intersect the 3D extent. In other words, it is the strictest set of permissions
	 * available to the player everywhere in the extent; in some point of it, player
	 * permissions can be wider, but they are never stricter.
	 * @param	player	the player to retrieve permissions for
	 * @param	bi		the 3D extent as a BoundingInformation (for instance, as returned
	 *					by PlayerCreateBlueprintEvent.getBoundingInformation())
	 * @return	the player permission for the extent.
	 */
	public static long getPlayerPermissionsForBounding(Player player, BoundingInformation bi)
	{
		// if admin AND admins are not demoted, return all permissions
		if ((Boolean)player.getAttribute(AreaProtection.key_isAdmin) && !AreaProtection.adminNoPriv)
			return AreaProtection.PERM_ALL;

		// convert bounding info into an area
		Vector3f	centre		= bi.getCenter();
		Vector3f	minBoundary	= centre.subtract(bi.getXExtent(), bi.getYExtent(), bi.getZExtent());
		Vector3f	maxBoundary	= centre.add(bi.getXExtent(), bi.getYExtent(), bi.getZExtent());
		Area		boundArea	= new Area(minBoundary, maxBoundary);

		// retrieve the player group and convert group name into group ID
		String		groupName	= player.getPermissionGroup();
		Integer		groupId	= null;
		if (groupName != null && !groupName.isEmpty())
				groupId	= groupIds.get(groupName);

		// retrieve the list of areas the player has specific permission for
		HashMap<Integer,Long>	playerPerms	= (HashMap<Integer, Long>)player.getAttribute(AreaProtection.key_areas);
		long					cumulPerm	= AreaProtection.PERM_ALL;			// permissions default to everything

		Long		aPerm;
		// scan all areas to collect all areas which intersect the bound area
		for (ProtArea area : areas.values())
		{
			// if the current area contains the point...
//			if (area.intersects(boundArea))
			if (AreaProtection.areaIntersects(area, boundArea))
			{
				// ..check the player has special permissions to the area
				if ( (aPerm=playerPerms.get(area.id)) == null)
					// if the player has no special permission,
					// check the player belongs to a group
					// and the area has special permissions for that group
					if (groupId != null)
						aPerm	= area.groups.get(groupId);
				// if no group, use generic area permissions
				if (aPerm == null)
					aPerm	= area.permissions;
				// mask cumulative permissions with player permissions for this area
//PERM AND
//				cumulPerm	&= aPerm;
// PERM OR
				cumulPerm	|= aPerm;
			}
		}
		return cumulPerm;
	}

	/**
	 * Tele-transports the player to the centre point of an area
	 * @param	player	the player to transport
	 * @param	rwArea	the destination area
	 */
	public static void movePlayerToArea(Player player, Area rwArea)
	{
		Vector3f	from	= ChunkUtils.getGlobalPosition(rwArea.getStartChunkPosition(), rwArea.getStartBlockPosition());
		Vector3f	to		= ChunkUtils.getGlobalPosition(rwArea.getEndChunkPosition(), rwArea.getEndBlockPosition());
		Vector3f	centre	= new Vector3f((from.x + to.x) / 2, player.getPosition().y, (from.z + to.z) / 2);
		player.setFlying(true);
		player.setPosition(centre);
	}

	/**
	 * Toggles on/off the display of areas for a given player.
	 * @param player	the player
	 * @return			the new value of the area display status.
	 */
	static boolean togglePlayerAreas(Player player)
	{
		boolean show	= !(boolean)player.getAttribute(AreaProtection.key_areasShown);
		if (show)
			showAreasToPlayer(player);
		else
			hideAreasToPlayer(player);
		return show;
	}

	static void showAreasToPlayer(Player player)
	{
		player.setAttribute(AreaProtection.key_areasShown, true);
		for (Map.Entry<Integer,ProtArea> entry : areas.entrySet())
			showAreaToPlayer(player, entry.getValue());
	}

	static void hideAreasToPlayer(Player player)
	{
		player.setAttribute(AreaProtection.key_areasShown, false);
		for (Map.Entry<Integer,ProtArea> entry : areas.entrySet())
		{
			ProtArea	area	= entry.getValue();
			if (area.worldArea != null)
				player.removeWorldElement(area.worldArea);
		}
	}

	/**
	 * Returns the name of a player or group from the DB id. The player needs not to be connected.
	 * @param	playerId	the DB id of the player
	 * @param	type		either LIST_TYPE_GROUP or LIST_TYPE_PLAYER
	 * @return	the player/group name or null if no such a player/group id.
	 */
	static String getPlayerNameFromId(int playerId, int type)
	{
		Map<Integer,String>	players = groupNames;
		if (type == LIST_TYPE_PLAYER || type == LIST_TYPE_MANAGERS)
		{
			if (playerNames == null)
				initPlayers();
			players	= playerNames;
		}
		return players.get(playerId);
	}

	static Set<Integer> getPlayerIdSet()
	{
		if (playerNames == null)
			initPlayers();
		return playerNames.keySet();
	}

	static void resetPlayers()		{ playerNames = null; }

	//********************
	// PRIVATE HELPER METHODS
	//********************

	private static void showAreaToPlayer(Player player, ProtArea area)
	{
		if (area.worldArea == null)
		{
			area.worldArea = new WorldArea(area);
			area.worldArea.setColor(GeneralUtils.nextRandomColor(true));
			area.worldArea.setAlwaysVisible(false);
		}
		player.addWorldElement(area.worldArea);
	}

	/**
		Retrieves all the areas currently defined, add them to the server and caches them.
	*/
	private static void initAreas()
	{
		Server	server	= AreaProtection.plugin.getServer();
		areas.clear();
		try(ResultSet result = db.executeQuery("SELECT * FROM `areas` ORDER BY LOWER(`name`)"))
		{
			while(result.next())
			{
				int		id		= result.getInt(1);
				int		fromX	= result.getInt(2);
				int		fromY	= result.getInt(3);
				int		fromZ	= result.getInt(4);
				int		toX		= result.getInt(5);
				int		toY		= result.getInt(6);
				int		toZ		= result.getInt(7);
				int		perm	= result.getInt(8);
				String	name	= result.getString(9);
				ProtArea	area	= new ProtArea(id, fromX, fromY, fromZ, toX, toY, toZ, name, perm);
				areas.put(id, area);
				server.addArea(area);
			}
			result.close();
		}
		catch(SQLException e)
		{
			//on errors, do nothing and simply use what we got.
		}
	}

	/**
		Returns the ProtArea matching the given rwArea or null if no defined ProtArea matches it.

		@param	rwArea	the Area to match
		@return	the matching ProtArea
	*/
	private static ProtArea matchArea(Area rwArea)
	{
		for (Map.Entry<Integer,ProtArea> entry : areas.entrySet())
		{
			ProtArea	area	= entry.getValue();
			if (area.equals(rwArea) )
				return area;
		}
		return null;
	}

	/**
	 * Inserts a new area in the areas Map, sorting it alphabetically according to its name.
	 * @param	area	the area to insert
	 */
	private static void insertNewArea(ProtArea area)
	{
		if (area == null || area.id < 1)
			return;
		LinkedHashMap<Integer,ProtArea>		newAreaMap	= new LinkedHashMap<>();
		for (Map.Entry<Integer,ProtArea> entry : areas.entrySet())
		{
			if (area != null && entry.getValue().name.compareToIgnoreCase(area.name) > 0)
			{
				newAreaMap.put(area.id, area);
				area = null;
			}
			newAreaMap.put(entry.getKey(), entry.getValue());
		}
		// if area still defined, it doesn't go before any existing area
		// (or no area exists!): just add it
		if (area != null)
			newAreaMap.put(area.id, area);
		areas	= newAreaMap;
	}

	private static void initGroups()
	{

		groupNames	= new HashMap<>();
		groupIds	= new HashMap<>();

		// retrieve group ID's already in DB
		Map<String, Integer>	dbGroups	= new HashMap<>();
		try(ResultSet result = db.executeQuery("SELECT * FROM `perm_groups`"))
		{
			while(result.next())
				dbGroups.put(result.getString(2), result.getInt(1));
		}
		catch(SQLException e)
		{
			//on errors, do nothing and simply use what we got.
		}

		// retrieve permission groups from server directory
		String		path		= AreaProtection.plugin.getPath() + "/../../permissions/groups/";
		File		groupDir	= new File(path);
		String[]	rwGroups	= groupDir.list(new FilenameFilter()
			{
				@Override
				public boolean accept(File file, String fileName)
				{
					boolean	accept	= fileName.endsWith(".permissions");
					return accept;
				}
			}
		);

		if (rwGroups != null)						// may be null if the plug-in is run on the Single Player
		{											// which has no "permissions/groups" folder
			// merge group list with groups in DB
			for (String rwGroup : rwGroups)
			{
				// remove the ".permissions" extension from file names
				String	name	= rwGroup.substring(0, rwGroup.length() - 12);
				Integer	id		= dbGroups.get(name);
				if (id == null)				// such a perm. group not know yet: add to DB
				{
					try(PreparedStatement stmt	= db.getConnection().prepareStatement(
						"INSERT INTO `perm_groups` (name) VALUES (?)")
						)
					{
						stmt.setString(1, name);
						stmt.executeUpdate();
						try (ResultSet idSet = stmt.getGeneratedKeys())
						{
							if (idSet.next())
								id	= idSet.getInt(1);
						}
					} catch (SQLException e)
					{
						e.printStackTrace();
					}
				}
				if (id != null)
				{
					groupNames.put(id, name);
					groupIds.put(name, id);
				}
			}
		}
	}

	private static void initPlayers()
	{
		playerNames	= new LinkedHashMap<>();
		// Query world data base for known players
		WorldDatabase	worldDb = AreaProtection.plugin.getWorldDatabase();
		try(ResultSet result = worldDb.executeQuery("SELECT `ID`,`Name` FROM `Player` ORDER BY LOWER(`Name`)"))
		{
			while(result.next())
				playerNames.put(result.getInt(1), result.getString(2));
		}
		catch(SQLException e)
		{
			//on errors, do nothing and simply use what we got.
		}
	}

	private static void AP3LUAImport()
	{
		String	path	= AreaProtection.plugin.getPath() + "/AreaProtection";
		File	LUAdb	= new File(path + "/scriptDatabase.db");
		if (!LUAdb.isFile())
			return;

		// IMPORT AREAS

		// a map used to correlate the id each area had in the LUA db to the id it has in the new db 
		HashMap<Integer, Integer>	oldId2NewId	= new HashMap<>();
		// connect to the old LUA db
		Database	oldDb	= AreaProtection.plugin.getSQLiteConnection(path + "/scriptDatabase.db");
		// scan areas
		try(ResultSet result = oldDb.executeQuery("SELECT * FROM `areas`"))
		{
			while(result.next())
			{
				int			LUAid		= result.getInt(1);
				String		name		= result.getString(2);
				Vector3i	fromChunk	= new Vector3i(result.getInt(3), result.getInt(4), result.getInt(5));
				Vector3i	fromBlock	= new Vector3i(result.getInt(6), result.getInt(7), result.getInt(8));
				Vector3i	toChunk		= new Vector3i(result.getInt(9), result.getInt(10),result.getInt(11));
				Vector3i	toBlock		= new Vector3i(result.getInt(12),result.getInt(13),result.getInt(14));
//				int			playerID	= result.getInt(16);
				Vector3f	from		= ChunkUtils.getGlobalPosition(fromChunk, fromBlock);
				Vector3f	to			= ChunkUtils.getGlobalPosition(toChunk,   toBlock);
				ProtArea	area		= new ProtArea(from, to, name, AreaProtection.PERM_DEFAULT);
				addArea(area);
				oldId2NewId.put(LUAid, area.id);
			}
		}
		catch(SQLException e)
		{
			//on errors, do nothing and simply use what we got.
		}

		// IMPORT RIGHTS

		// the permissions defined in LUA groups
		Map<String,Long>	LUAGroups	= AreaProtection.initPresets(path + "/Groups");
//		WorldDatabase		worldDb		= AreaProtection.plugin.getWorldDatabase();
		// scan rights
		try(ResultSet result = oldDb.executeQuery("SELECT * FROM `rights`"))
		{
			while(result.next())
			{
				// the data of the right
//				int		LUAid		= result.getInt(1);
				int		LUAAreaId	= result.getInt(2);
				int		playerId	= result.getInt(3);
				String	groupName	= result.getString(4);
				// convert old LUA DB area ID into our area ID
				Integer	newAreaId	= oldId2NewId.get(LUAAreaId);
				// and retrieve corresponding area
				ProtArea area		= areas.get(newAreaId);
				// if no such an area, ignore and go on with next right
				if (area == null)
					continue;
				// retrieve permissions corresponding to the group
				Long	groupPerms	= LUAGroups.get(groupName);
				if (groupPerms != null)
					addPlayerToArea(area, playerId, groupPerms, LIST_TYPE_PLAYER);
			}
		}
		catch(SQLException e)
		{
			//on errors, do nothing and simply use what we got.
		}

		// IMPORT CHESTS

		// TODO : ???

		// now rename the file, so that it is not imported again
		oldDb.close();
		Path	oldPath	= Paths.get(path + "/scriptDatabase.db");
		try
		{
			Files.move(oldPath, oldPath.resolveSibling("scriptDatabase.db.imported"),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e)
		{
			// we did what we could...
			e.printStackTrace();
		}
	}

	/**
	 * The player with the given DBID, if such a player exists and is connected;
	 * null otherwise.
	 * @param	dbId	the DB ID of the player to look for.
	 * @return	the player with the given DB ID, if it exists and is connected;
	 *			null if there is no such player or it is not connected now.
	 */
	private static Player connectedPlayerFromDBID(int dbId)
	{
		Player	player		= null;
		Long	playerUID	= playerDBIDtoUID(dbId);
		// if the player is connected right now, add the details to the player
		// list of areas for which he has special permissions
		if (playerUID != null)
			player	= AreaProtection.plugin.getServer().getPlayer(playerUID);
		return player;
	}

	/**
	 * Returns the UID of a player from the player DBID.
	 * @param	dbId	the player DBID to look upon.
	 * @return	the UID of the player identified by the playerDBID or null if there
	 *			there is no such player..
	 */
	private static Long playerDBIDtoUID(final int dbId)
	{
		Long	playerUID	= null;
		// Query world data base for this player
		WorldDatabase	worldDb = AreaProtection.plugin.getWorldDatabase();
		try(ResultSet result = worldDb.executeQuery("SELECT `UID` FROM `Player` WHERE `ID` = " + dbId))
		{
			if(result.next())
				playerUID	= result.getLong(1);
		}
		catch(SQLException e)
		{
			//on errors, do nothing and simply return null
		}
		return playerUID;
	}
}
