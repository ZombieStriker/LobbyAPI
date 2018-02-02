/*
 *  Copyright (C) 2017 Zombie_Striker
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307 USA
 */package me.zombie_striker.lobbyapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LobbyAPI {

	private static Main ml;

	public LobbyAPI(Main mll) {
		ml = mll;
	}


	/**
	 * returns if the world has a max amount of players for the world
	 * 
	 * @param the
	 *            world
	 * @return if it was successful.
	 */
	public static boolean hasMaxPlayers(World wo) {
		return getLobbyWorld(wo).hasMaxPlayers();
	}

	/**
	 * Returns the max amount of players
	 * 
	 * @param the
	 *            world
	 * @return the max amount of players
	 */
	public static int getMaxPlayers(World wo) {
		if (getLobbyWorld(wo).hasMaxPlayers()) {
			return getLobbyWorld(wo).getMaxPlayers();
		}
		return -1;
	}

	/**
	 * Adds a new lobbyworld
	 * 
	 * NOTE: You should use registerWorld instead if you want to create a new
	 * lobby world.
	 * 
	 * @param lobby
	 */
	public static void addLobbyWorld(LobbyWorld lobby) {
		ml.getWorlds().add(lobby);
	}

	/**
	 * Removes a lobby world
	 * 
	 * NOTE: You should use unregisterWorld if you want to unregister a world
	 * 
	 * @param lobby
	 */
	public static void removeLobbyWorld(LobbyWorld lobby) {
		ml.getWorlds().remove(lobby);
	}

	/**
	 * Sets the max players for a world
	 * 
	 * @param the
	 *            world
	 * @param the
	 *            maxvalue
	 */
	public static void setMaxPlayers(World wo, int maxvalue) {
		getLobbyWorld(wo).setMaxPlayers(maxvalue < 0, maxvalue);
	}

	/**
	 * Use the methods from LobbyWorld instead of this. (Use
	 * LobbyAPI.getLobbyWorld(World instance) to get the LobbyWorld instance)
	 * 
	 * @param wo
	 * @param material
	 */
	@Deprecated
	public static void setWorldMaterial(World wo, Material material) {
		getLobbyWorld(wo).setMaterial(material);
	}

	/**
	 * Use the methods from LobbyWorld instead of this. (Use
	 * LobbyAPI.getLobbyWorld(World instance) to get the LobbyWorld instance)
	 * 
	 * @param wo
	 * @param material
	 */
	@Deprecated
	public static Material getWorldMaterial(World wo) {
		return getLobbyWorld(wo).getMaterial();
	}

	/**
	 * Use the methods from LobbyWorld instead of this. (Use
	 * LobbyAPI.getLobbyWorld(World instance) to get the LobbyWorld instance)
	 * 
	 * @param wo
	 * @param material
	 */
	@Deprecated
	public static void setIsWorldPrivate(boolean b, World wo) {
		getLobbyWorld(wo).setIsPrivate(b);
	}

	/**
	 * Use the methods from LobbyWorld instead of this. (Use
	 * LobbyAPI.getLobbyWorld(World instance) to get the LobbyWorld instance)
	 * 
	 * @param wo
	 * @param material
	 */
	@Deprecated
	public static boolean isWorldPrivate(World wo) {
		return getLobbyWorld(wo).isPrivate();
	}

	/**
	 * Use the methods from LobbyWorld instead of this. (Use
	 * LobbyAPI.getLobbyWorld(World instance) to get the LobbyWorld instance)
	 * 
	 * @param wo
	 * @param material
	 */
	@Deprecated
	public static boolean hasNoEnderChestRules(World wo) {
		return getLobbyWorld(wo).hasEnderChest();
	}

	/**
	 * Use the methods from LobbyWorld instead of this. (Use
	 * LobbyAPI.getLobbyWorld(World instance) to get the LobbyWorld instance)
	 * 
	 * @param wo
	 * @param material
	 */
	@Deprecated
	public static void setNoEnderChests(World wo, boolean b) {
		getLobbyWorld(wo).setEnderChest(b);
	}

	/**
	 * Whitelists a UUID for a specific worlds
	 * 
	 * @param uuid
	 * @param wo
	 * @return if it was successful
	 */
	public static boolean whitelistContainsPlayer(UUID uuid, World wo) {
		return getLobbyWorld(wo).getWhitelistedPlayers().contains(
				Bukkit.getOfflinePlayer(uuid));
	}

	/**
	 * Whitelists a player's name for the wortd
	 * 
	 * @param name
	 * @param wo
	 * @return if it was successful
	 */
	@SuppressWarnings("deprecation")
	public static boolean whitelistContainsPlayer(String name, World wo) {
		return getLobbyWorld(wo).getWhitelistedPlayers().contains(
				Bukkit.getOfflinePlayer(name));
	}

	/**
	 * Whitelists a player through there name.
	 * 
	 * @param name
	 * @param wo
	 */
	@SuppressWarnings("deprecation")
	public static void addWhitelistPlayer(String name, World wo) {
		if (!getLobbyWorld(wo).isPrivate()) {
			System.out.println("The world \"" + wo.getName()
					+ "\" Is not whitelisted!");
			return;
		}
		getLobbyWorld(wo).addWhitelistedPlayer(
				(Player) Bukkit.getOfflinePlayer(name));
	}

	/**
	 * Whitelists a player through their UUID
	 * 
	 * @param uuid
	 * @param wo
	 */
	public static void addWhitelistPlayer(UUID uuid, World wo) {
		if (!getLobbyWorld(wo).isPrivate()) {
			System.out.println("The world \"" + wo.getName()
					+ "\" Is not whitelisted!");
			return;
		}
		getLobbyWorld(wo).addWhitelistedPlayer(
				(Player) Bukkit.getOfflinePlayer(uuid));
	}

	/**
	 * Whitelits a group of players through their names
	 * 
	 * @param name
	 * @param wo
	 */
	@SuppressWarnings("deprecation")
	public static void addWhitelistPlayerArray(ArrayList<String> name, World wo) {
		if (!getLobbyWorld(wo).isPrivate()) {
			System.out.println("The world \"" + wo.getName()
					+ "\" Is not whitelisted!");
			return;
		}
		for (String s : name) {
			getLobbyWorld(wo).addWhitelistedPlayer(
					(Player) Bukkit.getOfflinePlayer(s));
		}
	}

	/**
	 * Whitelists a group of players through their UUIDs
	 * 
	 * @param uuid
	 * @param wo
	 */
	public static void addWhitelistPlayer(ArrayList<UUID> uuid, World wo) {
		if (!getLobbyWorld(wo).isPrivate()) {
			System.out.println("The world \"" + wo.getName()
					+ "\" Is not whitelisted!");
			return;
		}
		for (UUID s : uuid) {
			getLobbyWorld(wo).addWhitelistedPlayer(
					(Player) Bukkit.getOfflinePlayer(s));
		}
	}

	/**
	 * Use this to register your world
	 * 
	 * @param The
	 *            world instance
	 * @param The
	 *            spawn location
	 * @param The
	 *            address you will save all the inventories to (E.g. "world1" or
	 *            "minigame")
	 * @param The
	 *            description of the world
	 * @param The
	 *            material's duribility
	 * @param The
	 *            slot in the menu the item will be in. Note: If that slot was
	 *            already taken, it will find an open one.
	 * @param The
	 *            Dafault gamemode
	 */
	public static LobbyWorld registerWorld(World world, Location spawn,
			String saveLetter, String worldDescription, Integer woolColor,
			int menuSlot, GameMode gamemode) {
		LobbyWorld lw = new LobbyWorld(false, world.getName(), menuSlot, 1,
				Short.parseShort(woolColor + ""), spawn, saveLetter, gamemode);
		addLobbyWorld(lw);
		addWorldDescriptionLine(world, worldDescription);
		return lw;
	}

	/**
	 * Registers a "hidden" world (a world not seen in the menu)
	 * 
	 * @param The
	 *            world instance
	 * @param The
	 *            spawn location
	 * @param The
	 *            address you will save all the inventories to (E.g. "world1" or
	 *            "minigame")
	 * @param The
	 *            dafault gamemode
	 */
	public static LobbyWorld registerHiddenWorld(World world, Location spawn,
			String saveLetter, GameMode gamemode) {
		LobbyWorld lw = new LobbyWorld(false, world.getName(),
				getOpenSlot(500), 1, (short) 0, spawn, saveLetter, gamemode);
		addLobbyWorld(lw);
		hideWorld(world, true);
		return lw;
	}

	/**
	 * Registers a "Hidden" world (a world not awwn in the menu)
	 * 
	 * @param The
	 *            world instance
	 * @param The
	 *            spawn location
	 * @param The
	 *            address you will save all the inventories to (E.g. "world1" or
	 *            "minigame")
	 * @param The
	 *            world description
	 * @param
	 * @param gamemode
	 */
	public static LobbyWorld registerHiddenWorld(World world, Location spawn,
			String saveLetter, String worldDescription, GameMode gamemode) {
		LobbyWorld lw = new LobbyWorld(false, world.getName(), 99, 1,
				(short) 0, spawn, saveLetter, gamemode);
		addLobbyWorld(lw);
		addWorldDescriptionLine(world, worldDescription);
		hideWorld(world, true);
		return lw;
	}

	/**
	 * USED ONLY IF REGISTERING WORLDS FROM THE LOBBYAPI CONFIG
	 * 
	 * @param world
	 * @param spawn
	 * @param saveLetter
	 * @param worldDescription
	 * @param woolColor
	 * @param worldMaterial
	 * @param menuSlot
	 * @param gamemode
	 */
	@Deprecated
	public static LobbyWorld registerWorldFromConfig(World world, Location spawn,
			String saveLetter, String worldDescription, Integer woolColor,
			Material worldMaterial, int menuSlot, GameMode gamemode) {
		LobbyWorld lw = new LobbyWorld(true, world.getName(), menuSlot, 1,
				Short.parseShort(woolColor + ""), spawn, saveLetter, gamemode);
		addLobbyWorld(lw);
		addWorldDescriptionLine(world, worldDescription);
		setWorldMaterial(world, worldMaterial);
		return lw;
	}

	/**
	 * USED ONLY IF LOADING WORLDS FROM LOBBYAPI CONFIG
	 * 
	 * @param world
	 * @param spawn
	 * @param saveLetter
	 * @param worldDescription
	 * @param woolColor
	 * @param menuSlot
	 * @param gamemode
	 */
	@Deprecated
	public static LobbyWorld registerWorldFromConfig(World world, Location spawn,
			String saveLetter, String worldDescription, Integer woolColor,
			int menuSlot, GameMode gamemode) {
		LobbyWorld lw = new LobbyWorld(true, world.getName(), menuSlot, 1,
				Short.parseShort(woolColor + ""), spawn, saveLetter, gamemode);
		addLobbyWorld(lw);
		addWorldDescriptionLine(world, worldDescription);
		return lw;
	}

	/**
	 * Unregisters a world
	 * 
	 * @param world
	 */
	@Deprecated
	public static void unregisterWorld(World world) {
		removeWorld(world);
	}

	/**
	 * Unregisters a bungee server
	 * 
	 * @param server
	 */
	@Deprecated
	public static void unregisterBungeeServer(String server) {
		removeBungeeServer(server);
	}

	/**
	 * Registers a bungee server
	 * 
	 * @param server
	 * @param slot
	 * @param amount
	 * @param woolColor
	 */
	public static void registerBungeeServer(String server, int slot,
			int amount, Integer woolColor) {
		LobbyServer lw = new LobbyServer(false, server, slot, amount,
				Short.parseShort(woolColor + ""));
		addBungeeServer(lw);
	}

	/**
	 * ONLY TO BE USED IF REIGSTERING A SERVER FROM LOBBYAPI CONFIG
	 * 
	 * @param server
	 * @param slot
	 * @param woolColor
	 */
	@Deprecated
	public static void registerBungeeServerFromConfig(String server, int slot,
			Integer woolColor) {
		LobbyServer lw = new LobbyServer(true, server, slot, 1,
				Short.parseShort(woolColor + ""));
		addBungeeServer(lw);
	}

	/**
	 * Adds a bungee server NOTE: instead use registerBungeeServer
	 * 
	 * @param ls
	 */
	public static void addBungeeServer(LobbyServer ls) {
		ml.getBWorlds().add(ls);
	}

	/**
	 * Return the LobbyServer instance
	 * 
	 * @param name
	 * @return the LobbyServer instance
	 */
	public static LobbyServer getServer(String name) {
		for (LobbyServer ls : ml.getBWorlds()) {
			if (ls.getName().equals(name))
				return ls;
		}
		return null;
	}

	/**
	 * Removes a BungeeServer
	 * 
	 * @param name
	 * @return
	 */
	public static boolean removeBungeeServer(String name) {
		return ml.getBWorlds().remove(name);

	}

	/**
	 * Creates a custom itemstack
	 * 
	 * @param Displayname
	 * @param Amount
	 * @param Material
	 * @param Lore
	 * @return the custom itemstack
	 */
	public static ItemStack setName(String name, int numb, Material mat,
			List<String> lore) {
		ItemStack is = new ItemStack(mat, 1);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);
		im.setLore(lore);
		is.setDurability((short) numb);
		is.setItemMeta(im);
		return is;
	}

	/**
	 * Hides the LobbyWorld from the menu
	 * 
	 * @param wo
	 * @param hidden
	 */
	public static void hideWorld(World wo, boolean hidden) {
		getLobbyWorld(wo).setHidden(hidden);
	}

	/**
	 * Returns if the world is hidden from the menu
	 * 
	 * @param wo
	 * @return
	 */
	public static boolean isHidden(World wo) {
		return getLobbyWorld(wo).isHidden();
	}

	/**
	 * Creates a custom itemstack
	 * 
	 * @param name
	 * @param numb
	 * @param mat
	 * @return
	 */
	public static ItemStack setName(String name, int numb, Material mat) {
		ItemStack is = new ItemStack(mat, 1);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);
		is.setDurability((short) numb);
		is.setItemMeta(im);
		return is;
	}

	/*
	 * public static void addBungeeServer(String server) {
	 * ml.bungeeServers.add(server); }
	 * 
	 * public static void setBungeeDescription(String server, String message) {
	 * ml.ServerDescription.put(server, message); }
	 * 
	 * public static void setBungeeMenuColor(String server, Integer color) {
	 * ml.ServerWoolColor.put(server, color); }
	 * 
	 * public static void setBungeeMenuAmount(String server, Integer amount) {
	 * ml.ServerWoolAmount.put(server, amount); }
	 */

	/**
	 * Use the methods from LobbyWorld instead of this. (Use
	 * LobbyAPI.getLobbyWorld(World instance) to get the LobbyWorld instance)
	 * 
	 * @param wo
	 * @param material
	 */
	@Deprecated
	public static void setWorldSpawn(World world, Location location) {
		getLobbyWorld(world).setSpawn(location);
	}

	/**
	 * Use the methods from LobbyWorld instead of this. (Use
	 * LobbyAPI.getLobbyWorld(World instance) to get the LobbyWorld instance)
	 * 
	 * @param wo
	 * @param material
	 */
	@Deprecated
	public static void setWorldsStaticTime(World world, int time) {
		getLobbyWorld(world).setStaticTime(time > -1, time);
	}

	/**
	 * Use the methods from LobbyWorld instead of this. (Use
	 * LobbyAPI.getLobbyWorld(World instance) to get the LobbyWorld instance)
	 * 
	 * @param wo
	 * @param material
	 */
	@Deprecated
	public static void setWorldDescription(World world, List<String> message) {
		getLobbyWorld(world).setDescription(message);
	}

	/**
	 * Adds one line to the world's description in the menu.
	 * 
	 * @param world
	 * @param message
	 */
	public static void addWorldDescriptionLine(World world, String message) {
		List<String> mes = getLobbyWorld(world).getDescription();
		mes.add(message);
		getLobbyWorld(world).setDescription(mes);
	}

	/**
	 * Use the methods from LobbyWorld instead of this. (Use
	 * LobbyAPI.getLobbyWorld(World instance) to get the LobbyWorld instance)
	 * 
	 * @param wo
	 * @param material
	 */
	@Deprecated
	public static void setWorldMenuColor(World world, Integer color) {
		getLobbyWorld(world).setColor(Short.parseShort(color + ""));
	}

	/**
	 * Use the methods from LobbyWorld instead of this. (Use
	 * LobbyAPI.getLobbyWorld(World instance) to get the LobbyWorld instance)
	 * 
	 * @param wo
	 * @param material
	 */
	@Deprecated
	public static void setWorldMenuAmount(World world, Integer amount) {
		getLobbyWorld(world).setSlotAmount(amount);
	}

	/**
	 * Sets the speific world to be the "Mainworld" (E.g. inputting
	 * "world_nether", "world" would mean that anytime the player teleports to
	 * "world_nether" through the menu, it will redirect them to "world")
	 * 
	 * @param world
	 * @param main
	 */
	public static void setMainWorld(World world, World main) {
		getLobbyWorld(world).setMainWorld(main);
	}

	/**
	 * Removes a world from menu.
	 * 
	 * @param world
	 */
	public static void removeWorld(World world) {
		LobbyWorld Rwo = null;
		for (LobbyWorld wo : ml.getWorlds()) {
			if (wo.getWorldName().equals(world.getName())) {
				Rwo = wo;
				break;
			}
		}
		if (Rwo != null) {
			ml.getWorlds().remove(Rwo);
		}
	}

	/**
	 * Use the methods from LobbyWorld instead of this. (Use
	 * LobbyAPI.getLobbyWorld(World instance) to get the LobbyWorld instance)
	 * 
	 * @param wo
	 * @param material
	 */
	@Deprecated
	public static void addWorldThatCantUsePortals(World world) {
		getLobbyWorld(world).setPortal(true);
	}

	/**
	 * Use the methods from LobbyWorld instead of this. (Use
	 * LobbyAPI.getLobbyWorld(World instance) to get the LobbyWorld instance)
	 * 
	 * @param wo
	 * @param material
	 */
	@Deprecated
	public static void removeWorldThatCantUsePortals(World world) {
		getLobbyWorld(world).setPortal(true);
	}

	/**
	 * Use the methods from LobbyWorld instead of this. (Use
	 * LobbyAPI.getLobbyWorld(World instance) to get the LobbyWorld instance)
	 * 
	 * @param wo
	 * @param material
	 */
	@Deprecated
	public static void setWorldThatCantUsePortals(World world, boolean b) {
		getLobbyWorld(world).setPortal(b);
	}

	/**
	 * Use the methods from LobbyWorld instead of this. (Use
	 * LobbyAPI.getLobbyWorld(World instance) to get the LobbyWorld instance)
	 * 
	 * @param wo
	 * @param material
	 */
	@Deprecated
	public static void setWorldItems(World world, List<ItemStack> items) {
		getLobbyWorld(world).setSpawnItems(items);
	}

	/**
	 * Use the methods from LobbyWorld instead of this. (Use
	 * LobbyAPI.getLobbyWorld(World instance) to get the LobbyWorld instance)
	 * 
	 * @param wo
	 * @param material
	 */
	@Deprecated
	public static void setWorldGameMode(World world, GameMode gm) {
		getLobbyWorld(world).setGameMode(gm);
	}

	/**
	 * Use the methods from LobbyWorld instead of this. (Use
	 * LobbyAPI.getLobbyWorld(World instance) to get the LobbyWorld instance)
	 * 
	 * @param wo
	 * @param material
	 */
	@Deprecated
	public static void setWorldMenuSlot(World world, Integer slot) {
	}

	/**
	 * Checks if a world has already been registers
	 * 
	 * @param world
	 * @return if the world has been registered
	 */
	public static boolean isRegistered(World world) {
		for (LobbyWorld wo : ml.getWorlds()) {
			if (wo.getWorldName().equals(world.getName()))
				return true;
		}
		return false;
	}

	/**
	 * Checks if the world has been registered
	 * 
	 * @param world
	 * @return if the world has been registered (Most cases, it will return
	 *         true)
	 */
	public static boolean isRegistered(LobbyWorld world) {
		return ml.getWorlds().contains(world);
	}

	/**
	 * Use the methods from LobbyWorld instead of this. (Use
	 * LobbyAPI.getLobbyWorld(World instance) to get the LobbyWorld instance)
	 * 
	 * @param wo
	 * @param material
	 */
	@Deprecated
	public static Location getWorldSpawn(World world) {
		return getLobbyWorld(world).getSpawn();
	}

	/**
	 * Use the methods from LobbyWorld instead of this. (Use
	 * LobbyAPI.getLobbyWorld(World instance) to get the LobbyWorld instance)
	 * 
	 * @param wo
	 * @param material
	 */
	@Deprecated
	public static void setWorldToTeleportToOnDeath(World world,
			World worldToTeleportTo) {
		getLobbyWorld(world).setRespawnWorld(worldToTeleportTo);
	}

	/**
	 * Returns all the LobbyWorlds on the server
	 * 
	 * @return
	 */
	public static Set<LobbyWorld> getWorlds() {
		return ml.getWorlds();
	}

	/**
	 * Gets the first open slot (after the specified slot) in thew menu
	 * 
	 * @param slot
	 * @return the first open slot (after the specified slot)
	 */
	public static int getOpenSlot(int slot) {
		int openslot = slot;
		int times = 1;
		for (int i = 0; i < times; i++) {
			boolean isTaken = false;
			for (LobbyWorld lw : ml.getWorlds()) {
				if (openslot == lw.getSlot()) {
					isTaken = true;
				}
			}
			if (!isTaken) {
				for (LobbyServer ls : ml.getBWorlds()) {
					if (openslot == ls.getSlot()) {
						isTaken = true;
						break;
					}
				}
			}
			if (isTaken) {
				openslot++;
				times++;
			}
		}
		return openslot;
	}

	/**
	 * Returns the LobbbyWorld instance of the world
	 * 
	 * @param name
	 * @return
	 */
	public static LobbyWorld getLobbyWorld(String name) {
		for (LobbyWorld w : ml.getWorlds()) {
			if (w.getWorldName().equals(name))
				return w;
		}
		return null;
	}

	/**
	 * Returns thelobbyWorld instance of the world
	 * 
	 * @param wo
	 * @return
	 */
	public static LobbyWorld getLobbyWorld(World wo) {
		for (LobbyWorld w : ml.getWorlds()) {
			if (wo == null) {
				System.out.println(Main.getPrefix() + "The world provided is null +");
				break;
			}
			if (w.getWorldName().equals(wo.getName()))
				return w;
		}
		return null;
	}

	/**
	 * Gets the "Main lobby" (the default world the player will be teleported
	 * to)
	 * 
	 * @return the default world
	 */
	public static LobbyWorld getLobby() {
		LobbyWorld lobby = null;
		for (LobbyWorld w : ml.getWorlds()) {
			if (w.isLobby())
				return w;
			if (lobby == null || lobby.getSlot() > w.getSlot()) {
				lobby = w;
			}
		}
		return lobby;
	}

	/**
	 * Returns all of the save names for every world on the server (if multiple
	 * worlds share the same savename, the savename will only be added once)
	 * 
	 * @return
	 */
	public static List<String> getSaveNames() {
		List<String> savenames = new ArrayList<String>();
		for (LobbyWorld wo : ml.getWorlds()) {
			if (!savenames.contains(wo.getSaveName()))
				savenames.add(wo.getSaveName());
		}
		return savenames;
	}

	/**
	 * Returns all of the LobbyWorlds that share the same name as the one
	 * provided
	 * 
	 * @param savename
	 * @return
	 */
	public static List<LobbyWorld> getWorldsBySaveNameAsLobby(String savename) {
		List<LobbyWorld> worlds = new ArrayList<LobbyWorld>();
		for (LobbyWorld wo : ml.getWorlds()) {
			if (wo.getSaveName().equals(savename)) {
				worlds.add(wo);
			}
		}
		return worlds;
	}

	/**
	 * Returns all of the worlds that share the same save name as the one
	 * provided
	 * 
	 * @param savename
	 * @return
	 */
	public static List<World> getWorldsBySaveName(String savename) {
		List<World> worlds = new ArrayList<World>();
		for (LobbyWorld wo : ml.getWorlds()) {
			if (wo.getSaveName().equals(savename)) {
				worlds.add(Bukkit.getWorld(wo.getWorldName()));
			}
		}
		return worlds;
	}

	/**
	 * Returns all of the LobbyWorlds that share the same material as the one provided
	 * @param material
	 * @return
	 */
	public static List<LobbyWorld> getWorldsWithMaterialAsLobby(
			Material material) {
		List<LobbyWorld> worlds = new ArrayList<LobbyWorld>();
		for (LobbyWorld wo : ml.getWorlds()) {
			if (wo.getMaterial() == material) {
				worlds.add(wo);
			}
		}
		return worlds;
	}
	/**
	 * Returns all of the worlds that shre the same materials as the one provided
	 * @param material
	 * @return
	 */
	public static List<World> getWorldsWithMaterial(Material material) {
		List<World> worlds = new ArrayList<World>();
		for (LobbyWorld wo : ml.getWorlds()) {
			if (wo.getMaterial() == material) {
				worlds.add(Bukkit.getWorld(wo.getWorldName()));
			}
		}
		return worlds;
	}
}
