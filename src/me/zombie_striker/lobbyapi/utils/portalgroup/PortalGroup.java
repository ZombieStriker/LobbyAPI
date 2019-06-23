package me.zombie_striker.lobbyapi.utils.portalgroup;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class PortalGroup
{
	private World world;
	private Set<Vector> portal;
	private HashMap<Integer, Set<Vector>> yBlock;
	private Location teleportTo;
	private double distanceSquared;
	private int bottom;

	/**
	 * A group of Portal block for a Nether portal.
	 *
	 * @param world The world the Portal blocks resides in.
	 */
	public PortalGroup(World world)
	{
		this.world = world;
		portal = new HashSet<Vector>();
		yBlock = new HashMap<Integer, Set<Vector>>();
		bottom = Integer.MAX_VALUE;
		distanceSquared = Double.MAX_VALUE;
	}

	/**
	 * Adds the Location to the PortalGroup.
	 *
	 * @param vec Vector to add
	 * @return if the Location was added. Otherwise false.
	 */
	public boolean add(Vector vec)
	{
		//Check to see if the block is a Portal block.
		try {
			if (vec.toLocation(world).getBlock().getType() != Material.NETHER_PORTAL)
				return false;
		}catch(Error|Exception e43){

			if (!vec.toLocation(world).getBlock().getType().name().equals("PORTAL"))
				return false;
		}
		boolean b = portal.add(vec);
		//If the location was added, do more actions.
		if (b)
		{
			int y = vec.getBlockY();
			if (y < bottom)
			{
				//The bottom of the Nether portal
				bottom = vec.getBlockY();
			}
			//Put the Location in a Map sorted by Y value.
			Set<Vector> set = yBlock.get(y);
			if (set == null)
			{
				set = new HashSet<Vector>();
				yBlock.put(y, set);
			}
			set.add(vec);
			//Reset the teleport location and distance squared since a new block was added.
			distanceSquared = Double.MAX_VALUE;
			teleportTo = null;
		}
		return b;
	}

	public int size()
	{
		return portal.size();
	}

	public Collection<Vector> getBlocks()
	{
		return Collections.unmodifiableCollection(portal);
	}

	/**
	 * Gets the average distance squared of all the portal blocks.
	 *
	 * @param vector vector to compare.
	 * @return distance squared
	 */
	public double distanceSquared(Vector vector)
	{
		if (distanceSquared < Double.MAX_VALUE)
			return distanceSquared;

		double d = 0;
		for (Vector vec : portal)
		{
			d += vec.distanceSquared(vector);
		}
		distanceSquared = d / portal.size();
		return distanceSquared;
	}

	/**
	 * Gets the location to teleport an entity to the Nether portal.
	 *
	 * @return The location in the bottom center of the Nether portal.
	 */
	public Location teleportTo()
	{
		if (teleportTo != null)
			return teleportTo;

		if (portal.size() == 0)
			return null;

		Set<Vector> bottomY = yBlock.get(bottom);
		int x = 0;
		int z = 0;
		for (Vector loc : bottomY)
		{
			x += loc.getBlockX();
			z += loc.getBlockZ();
		}
		teleportTo = new Location(world, ((x / bottomY.size()) + 0.5),
				bottom, ((z / bottomY.size()) + 0.5));
		return teleportTo;
	}

	@Override
	public int hashCode()
	{
		return portal.hashCode();
	}

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof PortalGroup)
		{
			PortalGroup pg = (PortalGroup) o;
			return portal.equals(pg.portal);
		}
		return portal.equals(o);
	}
}