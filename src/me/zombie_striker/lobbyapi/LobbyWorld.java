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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LobbyWorld {

	private static LobbyWorld MAIN_LOBBY = null;
	
	public static LobbyWorld getMainLobby(){
	return MAIN_LOBBY;	
	}
	
	
	private String worldname;
	private int ID;

	private int amount;
	private short color;
	private Material material = Material.DIAMOND_BLOCK;

	private World respawnWorld;
	private Location spawn;
	private boolean canUsePortal=false;
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
	//private Set<String> allowedPlayersString = new HashSet<String>();
	
	
	private List<String> commandsIssed = new ArrayList<>();
	
	private List<Location> portalLocations = new ArrayList<Location>();
	
	private boolean shouldSavePlayerLocation = false;	

	private List<ItemStack> worldItems;
	private GameMode gamemode=null;

	private List<String> worldDescription = new ArrayList<String>();
	private String saveName;
	
	private boolean isHidden = false;

	private boolean loadedFromConfig;
	private boolean isLobby = false;
	
	private String displayName;
	
	public void setNether(World nether) {
		this.nether = nether;
	}
	public void setEnd(World end) {
		this.end =end;
	}
	public World getNether() {
		return nether;
	}
	public World getEnd() {
		return end;
	}
	
	public List<Location> getPortalLocations(){
		return portalLocations;
	}
	public void setPortalLocations(List<Location> loc) {
		portalLocations = loc;
	}
	
	
	
	
	public void setDisplayName(String display) {
		this.displayName = display;
	}
	public String getDisplayName() {
		return displayName;
	}
	
	
	/**
	 * Sets whether a world should save the location of a world.
	 * @param should save the location for a world
	 * @return
	 */
	public void setWorldShouldSavePlayerLocation(boolean b){
		this.shouldSavePlayerLocation = b;
	}
	/**
	 * Sets whether a world should save the location of a world.
	 * @param should save the location for a world
	 * @return
	 */
	public boolean shouldWorldShouldSavePlayerLocation(){
		return this.shouldSavePlayerLocation;
	}
	
	/**
	 * Returns if a world has food and health disabled. Useful for worlds where you don't want players to die.
	 * @return
	 */
	public boolean hasDisabledHungerAndHealth(){
		return disableHungerAndHealth;
	}
	/**
	 * Sets if a world will disable food and health
	 * @param b
	 */
	public void setDisableHungerAndHealth(boolean b){
		this.disableHungerAndHealth = b;
	}

	/**
	 * Returns if a world has the void disabled. Useful for lobby worlds
	 * @return
	 */
	public boolean hasVoidDisable(){
		return disableVoidDeath;
	}
	/**
	 * Sets if a world will disable food and health
	 * @param b
	 */
	public void setVoidDisable(boolean b){
		this.disableVoidDeath = b;
	}
	
	
	/**
	 * Changes the Main lobby world. 
	 * @param lw
	 */
	public static void setMainLobby(LobbyWorld lw){
		MAIN_LOBBY=lw;
	}
	/**
	 * Sets the Main lobby world to be equal to this world. 
	 * @param lw
	 */
	public void setAsMainLobby(){
		MAIN_LOBBY=this;
	}

	/**
	 * Removes main lobby world.
	 * @param lw
	 */
	public static void removeMainLobby(){
		MAIN_LOBBY=null;
	}

	public LobbyWorld(boolean loadedFC, String worldname, int ID, int amount,
			short color, Location spawn, String saveName, GameMode gm) {
		this.loadedFromConfig = loadedFC;
		this.worldname = worldname;
		this.mainWorld = Bukkit.getWorld(worldname);
		this.ID = ID;
		this.amount = amount;
		this.color = color;
		this.spawn = spawn;
		this.saveName = saveName;
		this.gamemode = gm;
		
		//this.canUsePortal = ((mainWorld!=null&&Bukkit.getWorlds().get(0).equals(mainWorld))||worldname.equals("world_nether"));
	}
	
	public enum WeatherState{
		NORMAL(0),NO_RAIN(1),ALWAYS_RAIN(2);
		
		int data;
		private WeatherState(int data){
			this.data = data;
		}
		public static WeatherState getWeatherStateByName(String name){
			if(name==null)
				return null;
			
			if(name.equalsIgnoreCase("normal"))
				return NORMAL;
			if(name.equalsIgnoreCase("always_rain"))
				return ALWAYS_RAIN;
			if(name.equalsIgnoreCase("no_rain"))
				return NO_RAIN;
			return null;
		}
		public String toString(){
			if(data == 0)
				return "NORMAL";
			if(data == 1)
				return "NO_RAIN";
			if(data == 2)
				return "ALWAYS_RAIN";
			return "null";
		}
		public int getRawValue(){
			return data;
		}
		
	}
	public void addCommand(String commands){
		this.commandsIssed.add(commands);
	}
	public List<String> getCommandsOnJoin(){
		return this.commandsIssed;
	}
	public void setSaveName(String savename){
		this.saveName = savename;
	}
	public void setWeatherState(WeatherState weatherstate){
		this.weatherState = weatherstate;
	}
	public WeatherState getWeatherState(){
		return this.weatherState;
	}
	
	public void setLobby(boolean b){
		this.isLobby = b;
	}
	public boolean isLobby(){
		return this.isLobby;
	}
	
	public boolean isHidden(){
		return this.isHidden;
	}
	public void setHidden(boolean b){
		this.isHidden = b;
	}

	public boolean loadedFromConfig() {
		return this.loadedFromConfig;
	}

	public String getWorldName() {
		return worldname;
	}

	public int getSlot() {
		return ID;
	}
	
	public void setSlot(int slot) {
		this.ID = slot;
	}
	

	public int getSlotAmount() {
		return amount;
	}

	public short getColor() {
		return color;
	}

	public Material getMaterial() {
		return this.material;
	}

	public World getRespawnWorld() {
		return respawnWorld;
	}

	public Location getSpawn() {

        if(spawn.getWorld() == null){
        	spawn.setWorld(Bukkit.getWorld(this.worldname));
        	if(spawn.getWorld()==null){
            //Loads a world with the name given in the constructor
            WorldCreator wc = new WorldCreator(this.worldname);
            Bukkit.createWorld(wc);
        	}

        }
		
		return spawn;
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

	public boolean hasMainWorld() {
		return this.mainWorld != null;
	}

	public World getMainWorld() {
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
	
	public Set<Player> getPlayers(){
		return new HashSet<Player>(this.getMainWorld().getPlayers());
	}

	public Set<Player> getWhitelistedPlayers() {
		if (isPrivate()) {
			Set<Player> allowed = new HashSet<Player>();
			for (UUID uuid : allowedPlayersUUID) {
				allowed.add((Player) Bukkit.getOfflinePlayer(uuid));
			}
			/*for (String s : allowedPlayersString) {
				if (!allowed.contains(Bukkit.getOfflinePlayer(s)))
					allowed.add((Player) Bukkit.getOfflinePlayer(s));
			}*/
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

	public String getSaveName() {
		return saveName;
	}

	public List<String> getDescription() {
		return worldDescription;
	}

	public GameMode getGameMode() {
		return this.gamemode;
	}

	public void setSlotAmount(int i) {
		amount = i;
	}

	public void setColor(short s) {
		color = s;
	}

	public void setMaterial(Material m) {
		this.material = m;
	}

	public void setRespawnWorld(World world) {
			respawnWorld = world;
	}

	public void setSpawn(Location l) {
		spawn = l;
	}

	public void setPortal(boolean b) {
		canUsePortal = b;
	}

	public void setEnderChest(boolean b) {
		canUseEnderChest = b;
	}

	/**
	 * 
	 * @param i
	 *            is the time the world is set to. Will
	 */
	public void setStaticTime(int i) {
		if (i < 0)
			this.isStaticTime = false;
		else
			this.isStaticTime = false;
		this.staticTime = i;
	}

	public void setMainWorld(World w) {
		this.mainWorld = w;
	}

	public void setStaticTime(boolean b, int i) {
		this.isStaticTime = b;
		this.staticTime = i;
	}

	public void setMaxPlayers(boolean b, int max) {
		this.hasMaxPlayers = b;
		this.maxPlayers = max;
	}

	public void setGameMode(GameMode g) {
		this.gamemode = g;
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
		for(String u : uuids) {
			allowedPlayersUUID.add(UUID.fromString(u));
		}
	}
	public List<String> whitelistToString(){
		List<String> list = new ArrayList<String>();
		for(UUID uuid : allowedPlayersUUID) {
			list.add(uuid.toString());
		}
		return list;
	}

	public void setSpawnItems(List<ItemStack> i) {
		worldItems = i;
	}

	public void setDescription(List<String> l) {
		worldDescription = l;
	}
}
