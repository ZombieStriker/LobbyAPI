package me.zombie_striker.lobbyapi.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import me.zombie_striker.lobbyapi.LobbyWorld;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class ConfigHandler {

	private static FileConfiguration config;
	private static File file;

	public static void setConfig(FileConfiguration f, File f2) {
		config = f;
		file = f2;
	}

	public enum ConfigKeys {
		WorldSelector("worldselector"), CanUsePortals("canuseportals"), DisableHealthAndHunger(
				"disablehealthandhunger"), DisableVoid("disablevoid"), isHidden("hidden"), Material(
						"material"), Weather("weatherstate"), Color("color"), JoiningCommands("joincommands"), GameMode(
								"gamemode"), SavingLocation("playerworldsavinglocations"), ShouldBeSavingLocation(
										"shouldsavelocation"), DefaultItems("defaultitems"), CustomAddedWorlds(
												"CustomWorlds"), CustomAddedWorlds_Seed(
														"Seeds"), ENABLE_PER_WORLD_INVENTORIES(
																"Enable_Per_World_Inventories");

		public String s;

		ConfigKeys(String s2) {
			this.s = s2;
		}
	}

	public static Set<String> getCustomWorldKeys() {
		if (!config.contains(ConfigKeys.CustomAddedWorlds.s))
			return null;
		return config.getConfigurationSection(ConfigKeys.CustomAddedWorlds.s).getKeys(false);
	}

	public static int getCustomWorldInt(String name, String path) {
		return config.getInt(ConfigKeys.CustomAddedWorlds.s + "." + name + "." + path);
	}

	public static String getCustomWorldString(String name, String path) {
		return config.getString(ConfigKeys.CustomAddedWorlds.s + "." + name + "." + path);
	}

	public static void setCustomWorldValue(String name, String path, Object o) {
		config = YamlConfiguration.loadConfiguration(file);
		config.set(ConfigKeys.CustomAddedWorlds.s + "." + name + "." + path, o);
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setLobbyAPIVariable(String key, Object value) {
		config = YamlConfiguration.loadConfiguration(file);
		config.set("Settings." + key, value);
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ItemStack getLobbyAPIVariableItemstack(String key) {
		return (ItemStack) config.get("Settings." + key);
	}

	public static String getLobbyAPIVariableString(String key) {
		return config.getString("Settings." + key);
	}

	public static boolean getLobbyAPIVariableBoolean(String key) {
		return config.getBoolean("Settings." + key);
	}

	public static boolean containsLobbyAPIVariable(String key) {
		return config.contains("Settings." + key);
	}

	public static boolean containsWorldVariable(LobbyWorld lw, String key) {
		return config.contains("Worlds." + lw.getWorldName() + "." + key);
	}

	public static void setWorldVariable(LobbyWorld lw, String key, Object value) {
		config = YamlConfiguration.loadConfiguration(file);
		config.set("Worlds." + lw.getWorldName() + "." + key, value);
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int getWorldVariableInt(LobbyWorld lw, String key) {
		return config.getInt("Worlds." + lw.getWorldName() + "." + key);
	}

	public static double getWorldVariableDouble(LobbyWorld lw, String key) {
		return config.getDouble("Worlds." + lw.getWorldName() + "." + key);
	}

	public static ItemStack getWorldVariableItemStack(LobbyWorld lw, String key) {
		return (ItemStack) config.get("Worlds." + lw.getWorldName() + "." + key);
	}

	public static String getWorldVariableString(LobbyWorld lw, String key) {
		return config.getString("Worlds." + lw.getWorldName() + "." + key);
	}

	public static Location getWorldVariableLocation(LobbyWorld lw, String key) {
		return (Location) config.get("Worlds." + lw.getWorldName() + "." + key);
	}

	public static List<String> getWorldVariableList(LobbyWorld lw, String key) {
		return config.getStringList("Worlds." + lw.getWorldName() + "." + key);
	}

	public static Boolean getWorldVariableBoolean(LobbyWorld lw, String key) {
		return config.getBoolean("Worlds." + lw.getWorldName() + "." + key);
	}

	public static Object getWorldVariableObject(LobbyWorld lw, String key) {
		return config.get("Worlds." + lw.getWorldName() + "." + key);
	}
}
