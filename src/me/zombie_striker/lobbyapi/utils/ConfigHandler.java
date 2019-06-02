package me.zombie_striker.lobbyapi.utils;

import me.zombie_striker.lobbyapi.LobbyWorld;
import me.zombie_striker.lobbyapi.Main;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ConfigHandler {

	private static FileConfiguration config;
	private static File file;
	private static Main m;

	public static void setConfig(FileConfiguration f, File f2, Main main) {
		config = f;
		file = f2;
		m = main;
	}

	public static void setLobbyAPIVariable(ConfigKeys key, Object value) {
		config = YamlConfiguration.loadConfiguration(file);
		config.set("Settings." + key, value);
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		m.reloadConfig();
	}

	public static ItemStack getLobbyAPIVariableItemstack(ConfigKeys key) {
		return (ItemStack) config.get("Settings." + key);
	}

	public static String getLobbyAPIVariableString(ConfigKeys key) {
		return config.getString("Settings." + key);
	}

	public static boolean getLobbyAPIVariableBoolean(ConfigKeys key) {
		return config.getBoolean("Settings." + key);
	}

	public static boolean containsLobbyAPIVariable(ConfigKeys key) {
		return config.contains("Settings." + key);
	}

	public static Set<String> getWorlds() {
		return config.getConfigurationSection("Worlds").getKeys(false);
	}

	public static boolean containsWorldVariable(LobbyWorld lw, ConfigKeys key) {
		return config.contains("Worlds." + lw.getWorldName() + "." + key);
	}

	public static boolean containsWorldVariable(String worldname, ConfigKeys key) {
		return config.contains("Worlds." + worldname + "." + key);
	}

	public static void setWorldVariable(LobbyWorld lw, ConfigKeys key, Object value) {
		setWorldVariable(lw.getWorldName(), key, value);
	}

	public static void setWorldVariable(String lw, ConfigKeys key, Object value) {
		config = YamlConfiguration.loadConfiguration(file);
		config.set("Worlds." + lw + "." + key, value);
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		m.reloadConfig();
	}

	public static int getWorldVariableInt(String name, ConfigKeys key) {
		return config.getInt("Worlds." + name + "." + key);
	}

	public static int getWorldVariableInt(LobbyWorld lw, ConfigKeys key) {
		return config.getInt("Worlds." + lw.getWorldName() + "." + key);
	}

	public static double getWorldVariableDouble(LobbyWorld lw, ConfigKeys key) {
		return config.getDouble("Worlds." + lw.getWorldName() + "." + key);
	}

	public static ItemStack getWorldVariableItemStack(LobbyWorld lw, ConfigKeys key) {
		return (ItemStack) config.get("Worlds." + lw.getWorldName() + "." + key);
	}

	public static String getWorldVariableString(LobbyWorld lw, ConfigKeys key) {
		return getWorldVariableString(lw.getWorldName(), key);
	}

	public static String getWorldVariableString(String lw, ConfigKeys key) {
		return config.getString("Worlds." + lw + "." + key);
	}

	public static Location getWorldVariableLocation(LobbyWorld lw, ConfigKeys key) {
		return (Location) config.get("Worlds." + lw.getWorldName() + "." + key);
	}

	public static List<String> getWorldVariableList(LobbyWorld lw, ConfigKeys key) {
		return config.getStringList("Worlds." + lw.getWorldName() + "." + key);
	}

	public static Boolean getWorldVariableBoolean(LobbyWorld lw, ConfigKeys key) {
		return config.getBoolean("Worlds." + lw.getWorldName() + "." + key);
	}

	public static Object getWorldVariableObject(LobbyWorld lw, ConfigKeys key) {
		return config.get("Worlds." + lw.getWorldName() + "." + key);
	}

	public static Object getWorldVariableObject(String lw, ConfigKeys key) {
		return config.get("Worlds." + lw + "." + key);
	}

	public enum ConfigKeys {
		WorldSelector("worldselector"), CanUsePortals("canuseportals"), DisableHealthAndHunger(
				"disablehealthandhunger"), DisableVoid("disablevoid"), isHidden("hidden"), Material(
				"material"), Weather("weatherstate"), Color("color"), JoiningCommands("joincommands"), GameMode(
				"gamemode"), SavingLocation("playerworldsavinglocations"), ShouldBeSavingLocation(
				"shouldsavelocation"), DefaultItems("defaultitems"), CustomAddedWorlds_Seed(
				"Seeds"), ENABLE_PER_WORLD_INVENTORIES(
				"Enable_Per_World_Inventories"), LINKED_NETHER(
				"Linked_nether"), LINKED_END(
				"Linked_End"), WORLDENVIROMENT(
				"World_Enviroment"), PORTALLIST(
				"Portal_Loc_List");

		private String s;

		ConfigKeys(String s2) {
			this.s = s2;
		}

		@Override
		public String toString() {
			return s;
		}
	}/*
	 *
	 * public static Set<String> getCustomWorldKeys() { if
	 * (!config.contains(ConfigKeys.CustomAddedWorlds.s)) return null; return
	 * config.getConfigurationSection(ConfigKeys.CustomAddedWorlds.s).getKeys(false)
	 * ; }
	 *
	 * public static int getCustomWorldInt(String name, ConfigKeys path) { return
	 * config.getInt(ConfigKeys.CustomAddedWorlds.s + "." + name + "." + path); }
	 *
	 * public static String getCustomWorldString(String name, ConfigKeys path) {
	 * return config.getString(ConfigKeys.CustomAddedWorlds.s + "." + name + "." +
	 * path); }
	 *
	 * public static void setCustomWorldValue(String name, ConfigKeys path, Object
	 * o) { config = YamlConfiguration.loadConfiguration(file);
	 * config.set(ConfigKeys.CustomAddedWorlds.s + "." + name + "." + path, o); try
	 * { config.save(file); } catch (IOException e) { e.printStackTrace(); }
	 * m.reloadConfig(); }
	 */
}
