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
import net.risingworld.api.utils.Utils.ChunkUtils;
import net.risingworld.api.utils.Vector3f;
import net.risingworld.api.utils.Vector3i;
import net.risingworld.api.worldelements.WorldArea;

/**
 * A class wrapping the Rising World own Area class to add permission data.
 * <p>All fields are package-accessible for efficiency.
 */
public class ProtArea extends Area
{
	int					id;			// the persistent id of the PermArea
	Map<Integer,Long>	groups;		// the groups with group-specific permissions for this area and their permissions
	String				name;		// the name of the PermArea
	long				permissions;// the default permissions (may be overridden by player-specific permissions)
	Map<Integer,Long>	players;	// the players with player-specific permissions for this area and their permissions
	WorldArea			worldArea;	// the associated WorldArea (i.e. the visualisation of the area span)

	public ProtArea(Vector3f fromF, Vector3f toF)
	{
		super(fromF, toF);
		initPlayers();
	}

	public ProtArea(int id, int fromX, int fromY, int fromZ, int toX, int toY, int toZ,
			String name, long permissions)
	{
		super(new Vector3f(fromX, fromY, fromZ), new Vector3f(toX, toY, toZ));
		this.id				= id;
		this.name			= name;
		this.permissions	= permissions;
		initPlayers();
	}

	public ProtArea(Vector3f fromF, Vector3f toF, String name, long permissions)
	{
		super(fromF, toF);
		this.name			= name;
		this.permissions	= permissions;
		initPlayers();
	}

	public Extent	getExtent()						{	return new Extent(this);		}
	public int		getId()							{	return id;						}
	public String	getName()						{	return name;					}
	public long		getPermissions()				{	return permissions;				}

	public void setName(String newName)				{	name		= newName;			}
	public void setPermissions(long newPermissions)	{	permissions	= newPermissions;	}

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

	public class Extent
	{
		//
		// FIELDS
		//
		int		minX, maxX, minY, maxY, minZ, maxZ;

		public Extent(int minX, int maxX, int minY, int maxY, int minZ, int maxZ)
		{
			this.minX	= minX;
			this.maxX	= maxX;
			this.minZ	= minZ;
			this.maxZ	= maxZ;
			this.minY	= minY;
			this.maxY	= maxY;
			rearrange();
		}

		public Extent(Area area)
		{
			area.rearrange();
			Vector3f	from	= ChunkUtils.getGlobalPosition(area.getStartChunkPosition(), area.getStartBlockPosition());
			Vector3f	to		= ChunkUtils.getGlobalPosition(area.getEndChunkPosition(), area.getEndBlockPosition());
			minX	= (int)from.x;
			maxX	= (int)to.x;
			minY	= (int)from.y;
			maxY	= (int)to.y;
			minZ	= (int)from.z;
			maxZ	= (int)to.z;
			rearrange();
		}

		public Vector3f getFrom()	{	return new Vector3f(minX, minY, minZ);	}
		public Vector3f getTo()		{	return new Vector3f(maxX, maxY, maxZ);	}
		public int		getMinX()	{	return minX;							}
		public int		getMaxX()	{	return maxX;							}
		public int		getMinY()	{	return minY;							}
		public int		getMaxY()	{	return maxY;							}
		public int		getMinZ()	{	return minZ;							}
		public int		getMaxZ()	{	return maxZ;							}

		public void		setMinX(int val)	{	minX = val; rearrange();		}
		public void		setMaxX(int val)	{	maxX = val; rearrange();		}
		public void		setMinY(int val)	{	minY = val; rearrange();		}
		public void		setMaxY(int val)	{	maxY = val; rearrange();		}
		public void		setMinZ(int val)	{	minZ = val; rearrange();		}
		public void		setMaxZ(int val)	{	maxZ = val; rearrange();		}

		private void rearrange()
		{
			int		temp;
			if (minX > maxX)
			{
				temp	= minX;
				minX	= maxX;
				maxX	= temp;
			}
			if (minY > maxY)
			{
				temp	= minY;
				minY	= maxY;
				maxY	= temp;
			}
			if (minZ > maxZ)
			{
				temp	= minZ;
				minZ	= maxZ;
				maxZ	= temp;
			}
		}
	}
}
