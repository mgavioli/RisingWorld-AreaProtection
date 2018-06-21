/****************************
	A r e a P r o t e c t i o n  -  A Rising World Java plug-in for area permissions.

	ProtArea.java - Implements a world area with specific protection permissions.

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

import java.util.Map;
import java.util.TreeMap;
import net.risingworld.api.utils.Area;
import net.risingworld.api.utils.Vector3f;
import net.risingworld.api.worldelements.WorldArea;

/**
 * A class wrapping the Rising World own Area class to add permission data.
 * <p>All fields are package-accessible for efficiency.
 */
public class ProtArea extends Area
{
	int						id;			// the persistent id of the PermArea
	Map<Integer,Integer>	groups;		// the groups with group-specific permissions for this area and their permissions
	String					name;		// the name of the PermArea
	int						permissions;// the default permissions (may be overridden by player-specific permissions)
	Map<Integer,Integer>	players;	// the players with player-specific permissions for this area and their permissions
	WorldArea				worldArea;	// the associated WorldArea (i.e. the visualisation of the area span)

	public ProtArea(Vector3f fromF, Vector3f toF)
	{
		super(fromF, toF);
		initPlayers();
	}

	public ProtArea(int id, int fromX, int fromY, int fromZ, int toX, int toY, int toZ,
			String name, int permissions)
	{
		super(new Vector3f(fromX, fromY, fromZ), new Vector3f(toX, toY, toZ));
		this.id				= id;
		this.name			= name;
		this.permissions	= permissions;
		initPlayers();
	}

	public ProtArea(Vector3f fromF, Vector3f toF, String name, int permissions)
	{
		super(fromF, toF);
		this.name			= name;
		this.permissions	= permissions;
		initPlayers();
	}

	public int		getId()							{	return id;			}
	public String	getName()						{	return name;		}
	public int		getPermissions()				{	return permissions;	}

	public void setName(String newName)				{	name		= newName;			}
	public void setPermissions(int newPermissions)	{	permissions	= newPermissions;	}

	private void initPlayers()
	{
		if (id != 0)
		{
			players	= Db.getAllPlayerPermissionsForArea(id, Db.LIST_TYPE_PLAYER);
			groups	= Db.getAllPlayerPermissionsForArea(id, Db.LIST_TYPE_GROUP);
		}
		else
		{
			players	= new TreeMap<>();
			groups	= new TreeMap<>();
		}
	}

}
