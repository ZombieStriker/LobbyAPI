/*
 *  Copyright (C) 2016 Zombie_Striker
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
 */
package me.zombie_striker.lobbyapi;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class LobbyWorld extends LobbyIcon {

	private static LobbyWorld MAIN_LOBBY = null;

	private static Set<LobbyWorld> worlds = new HashSet<LobbyWorld>();

	private Location spawn;
	private boolean canUsePortal = false;
	private boolean canUseEnderChest;
	private boolean isStaticTime;
	private boolean disableHungerAndHealth;
	private boolean disableVoidDeath;
	private int staticTime;
	private WeatherState weatherState = WeatherState.NORMAL;
	private boolean hasMaxPlayers;
	private int maxPlayers;
	private World mainWorld;
	private World nether;
	private World end;
	private boolean isPrivate;
	private Set<UUID> allowedPlayersUUID = new HashSet<UUID>();
	private List<String> commandsIssed = new ArrayList<>();
	private List<Location> portalLocations = new ArrayList<Location>();
	private boolean shouldSavePlayerLocation = false;
	private List<ItemStack> worldItems;
	private GameMode gamemode = null;
	private List<String> worldDescription = new ArrayList<String>();
	private boolean isLobby = false;

	private Difficulty worldDifficulty;

	public LobbyWorld(boolean loadedFC, String worldname, int ID, int amount, short color, Location spawn,
					  String saveName, GameMode gm) {
		super(loadedFC, worldname, ID, amount, color);
		this.mainWorld = Bukkit.getWorld(worldname.toLowerCase());
		this.spawn = spawn;
		setInventorySaveName(saveName);
		this.gamemode = gm;
	}

	public static LobbyWorld getLobbyWorldFromWorld(World world) {
		for (LobbyWorld lw : worlds) {
			if (lw.getWorld() == world || (lw.getNether() != null && lw.getNether() == world) || (lw.getEnd() != null && lw.getEnd() == world))
				return lw;
		}
		return null;
	}

	public static Set<LobbyWorld> getLobbyWorlds() {
		return worlds;
	}

	public static LobbyWorld getMainLobby() {
		return MAIN_LOBBY;
	}

	/**
	 * Changes the Main lobby world.
	 *
	 * @param lw
	 */
	public static void setMainLobby(LobbyWorld lw) {
		MAIN_LOBBY = lw;
	}

	/**
	 * Removes main lobby world.
	 *
	 */
	public static void removeMainLobby() {
		MAIN_LOBBY = null;
	}

	public World getNether() {
		return nether;
	}

	public void setNether(World nether) {
		this.nether = nether;
	}

	public World getEnd() {
		return end;
	}

	public void setEnd(World end) {
		this.end = end;
	}

	/**
	 * Sets whether a world should save the location of a world.
	 *
	 * @return
	 */
	public void setWorldShouldSavePlayerLocation(boolean b) {
		this.shouldSavePlayerLocation = b;
	}

	/**
	 * Sets whether a world should save the location of a world.

	 * @return
	 */
	public boolean shouldWorldShouldSavePlayerLocation() {
		return this.shouldSavePlayerLocation;
	}

	public Difficulty getWorldDifficulty(){
		return worldDifficulty;
	}
	public void setWorldDifficulty(Difficulty difficulty){
		this.worldDifficulty = difficulty;
	}

	/**
	 * Returns if a world has food and health disabled. Useful for worlds where you
	 * don't want players to die.
	 *
	 * @return
	 */
	public boolean hasDisabledHungerAndHealth() {
		return disableHungerAndHealth;
	}

	/**
	 * Sets if a world will disable food and health
	 *
	 * @param b
	 */
	public void setDisableHungerAndHealth(boolean b) {
		this.disableHungerAndHealth = b;
	}

	/**
	 * Returns if a world has the void disabled. Useful for lobby worlds
	 *
	 * @return
	 */
	public boolean hasVoidDisable() {
		return disableVoidDeath;
	}

	/**
	 * Sets if a world will disable food and health
	 *
	 * @param b
	 */
	public void setVoidDisable(boolean b) {
		this.disableVoidDeath = b;
	}

	/**
	 * Sets the Main lobby world to be equal to this world.
	 *
	 */
	public void setAsMainLobby() {
		MAIN_LOBBY = this;
	}

	public void addCommand(String commands) {
		this.commandsIssed.add(commands);
	}

	public List<String> getCommandsOnJoin() {
		return this.commandsIssed;
	}

	public WeatherState getWeatherState() {
		return this.weatherState;
	}

	public void setWeatherState(WeatherState weatherstate) {
		this.weatherState = weatherstate;
	}

	public boolean isLobby() {
		return this.isLobby;
	}

	public void setLobby(boolean b) {
		this.isLobby = b;
	}

	@Deprecated
	public String getWorldName() {
		return getName();
	}


	public Location getSpawn() {
		if (spawn.getWorld() == null) {
			spawn.setWorld(Bukkit.getWorld(this.getName()));
			if (spawn.getWorld() == null) {
				// Loads a world with the name given in the constructor
				WorldCreator wc = new WorldCreator(this.getName());
				Bukkit.createWorld(wc);
			}

		}

		return spawn;
	}

	public void setSpawn(Location l) {
		spawn = l;
	}

	public boolean hasPortal() {
		return canUsePortal;
	}

	public boolean hasEnderChest() {
		return canUseEnderChest;
	}

	public boolean hasStaticTime() {
		return isStaticTime;
	}

	public int getStaticTime() {
		if (hasStaticTime()) {
			return staticTime;
		}
		return 0;
	}

	/**
	 * @param i is the time the world is set to. Will
	 */
	public void setStaticTime(int i) {
		if (i < 0)
			this.isStaticTime = false;
		else
			this.isStaticTime = false;
		this.staticTime = i;
	}

	public boolean hasMainWorld() {
		return this.mainWorld != null;
	}

	@Deprecated
	public World getMainWorld() {
		return getWorld();
	}

	public void setMainWorld(World w) {
		this.mainWorld = w;
	}

	public World getWorld() {
		return this.mainWorld;
	}

	public boolean hasMaxPlayers() {
		return hasMaxPlayers;
	}

	public int getMaxPlayers() {
		if (hasMaxPlayers)
			return maxPlayers;
		return -1;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public Set<Player> getPlayers() {
		return new HashSet<Player>(this.getWorld().getPlayers());
	}

	public Set<Player> getWhitelistedPlayers() {
		if (isPrivate()) {
			Set<Player> allowed = new HashSet<Player>();
			for (UUID uuid : allowedPlayersUUID) {
				allowed.add((Player) Bukkit.getOfflinePlayer(uuid));
			}
			return allowed;
		}
		return null;
	}

	public Set<UUID> getWhitelistedPlayersUUID() {
		if (isPrivate()) {
			return allowedPlayersUUID;
		}
		return null;
	}

	public List<ItemStack> getSpawnItems() {
		return worldItems;
	}

	public void setSpawnItems(List<ItemStack> i) {
		worldItems = i;
	}

	public List<String> getDescription() {
		return worldDescription;
	}

	public void setDescription(List<String> l) {
		worldDescription = l;
	}

	public GameMode getGameMode() {
		return this.gamemode;
	}

	public void setGameMode(GameMode g) {
		this.gamemode = g;
	}

	public void setPortal(boolean b) {
		canUsePortal = b;
	}

	public void setEnderChest(boolean b) {
		canUseEnderChest = b;
	}

	public void setStaticTime(boolean b, int i) {
		this.isStaticTime = b;
		this.staticTime = i;
	}

	public void setMaxPlayers(boolean b, int max) {
		this.hasMaxPlayers = b;
		this.maxPlayers = max;
	}

	public void setIsPrivate(boolean b) {
		isPrivate = b;
	}

	public void addWhitelistedPlayer(OfflinePlayer p) {
		if (p != null) {
			allowedPlayersUUID.add(p.getUniqueId());
		}
	}

	public void removeWhitelistedPlayer(OfflinePlayer p) {
		if (p != null) {
			allowedPlayersUUID.remove(p.getUniqueId());
		}
	}

	public void initWhitelist(List<String> uuids) {
		for (String u : uuids) {
			allowedPlayersUUID.add(UUID.fromString(u));
		}
	}

	public List<String> whitelistToString() {
		List<String> list = new ArrayList<String>();
		for (UUID uuid : allowedPlayersUUID) {
			list.add(uuid.toString());
		}
		return list;
	}

	public enum WeatherState {
		NORMAL(0), NO_RAIN(1), ALWAYS_RAIN(2);

		int data;

		WeatherState(int data) {
			this.data = data;
		}

		public static WeatherState getWeatherStateByName(String name) {
			if (name == null)
				return null;

			if (name.equalsIgnoreCase("normal"))
				return NORMAL;
			if (name.equalsIgnoreCase("always_rain"))
				return ALWAYS_RAIN;
			if (name.equalsIgnoreCase("no_rain"))
				return NO_RAIN;
			return null;
		}

		public String toString() {
			if (data == 0)
				return "NORMAL";
			if (data == 1)
				return "NO_RAIN";
			if (data == 2)
				return "ALWAYS_RAIN";
			return "null";
		}

		public int getRawValue() {
			return data;
		}

	}

}
