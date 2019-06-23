package me.zombie_striker.lobbyapi.utils.portalgroup;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

import static org.bukkit.block.BlockFace.*;
import static org.bukkit.World.Environment.*;

public class NetherPortalFinder
{
	private static final BlockFace[] BLOCK_FACES = new BlockFace[] {
			EAST, WEST, NORTH, SOUTH, UP, DOWN };

	/**
	 * * Gets the nearest Nether portal within the specified radius in relation to the given location.
	 *
	 * @param location  Center of the search radius
	 * @param radius    The search radius
	 * @param minHeight Minimum height of search
	 * @param maxHeight Maximum height of search
	 * @return Returns location in the bottom center of the nearest nether portal if found. Otherwise returns null.
	 */
	public static Location locate(Location location, int radius, int minHeight,
								  int maxHeight)
	{
		//Collection of all the PortalGroups found
		Set<PortalGroup> portals = new HashSet<PortalGroup>();
		//Collection of all the Portal blocks found in the PortalGroups
		Set<Vector> stored = new HashSet<Vector>();
		World world = location.getWorld();

		//The nearest PortalGroup and its distance.
		PortalGroup nearest = null;
		double nearestDistance = Double.MAX_VALUE;

		//Setting starting and ending Y value
		int yStart = location.getBlockY() - radius;
		yStart = yStart < minHeight ? minHeight : yStart;
		int yEnd = location.getBlockY() + radius;
		yEnd = yEnd > maxHeight ? maxHeight : yEnd;

		for (int x = location.getBlockX() - radius;
			 x <= location.getBlockX() + radius; x++)
		{
			for (int y = yStart; y <= yEnd; y += 2)
			{
				for (int z = location.getBlockZ() - radius;
					 z <= location.getBlockZ() + radius; z++)
				{
					//Check in a checkerboard-like fashion
					if (x + z % 2 == 0)
						break;

					//Location being iterated over.
					Location loc = new Location(world, x, y, z);
					Vector vec = loc.toVector();
					//Don't do anything if the Portal block is already stored.
					if (!stored.contains(vec))
					{
						PortalGroup pg = getPortalBlocks(loc);
						//Do nothing if there are no Portal blocks
						if (pg != null)
						{
							//If the PortalGroup was added, store the Portal blocks in the Collection
							if (portals.add(pg))
							{
								stored.addAll(pg.getBlocks());

								//Getting the nearest PortalGroup
								double distanceSquared = pg
										.distanceSquared(vec);
								if (distanceSquared < nearestDistance)
								{
									nearestDistance = distanceSquared;
									nearest = pg;
								}
							}
						}
					}
				}
			}
		}
		return nearest == null ? null : nearest.teleportTo();
	}

	public static Location locate(Location location)
	{
		World w = location.getWorld();
		World.Environment dimension = w.getEnvironment();
		int maxHeight = dimension == NORMAL ? w.getMaxHeight() - 1 : 127;
		return locate(location, 128, 1, maxHeight);
	}

	/**
	 * Gets the Portal blocks that is part of the Nether portal.
	 *
	 * @param loc - Location to start the getting the portal blocks.
	 * @return A PortalGroup of all the found Portal blocks. Otherwise returns null. This will return null if the amount of found blocks if below 6.
	 */
	public static PortalGroup getPortalBlocks(Location loc)
	{
		try {
			if (loc.getBlock().getType() != Material.NETHER_PORTAL)
				return null;
		}catch(Error|Exception e43){

			if (!loc.getBlock().getType().name().equals("PORTAL"))
				return null;
		}

		PortalGroup pg = portalBlock(new PortalGroup(loc.getWorld()), loc);
		return pg.size() > 5 ? pg : null;
	}

	private static PortalGroup portalBlock(PortalGroup group, Location loc)
	{
		for (BlockFace face : BLOCK_FACES)
		{
			Block relative = loc.getBlock().getRelative(face);
			Location relLoc = relative.getLocation();
			if (group.add(relLoc.toVector()))
			{
				portalBlock(group, relLoc);
			}
		}
		return group;
	}
}