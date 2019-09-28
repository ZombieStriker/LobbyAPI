package me.zombie_striker.lobbyapi.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.IOUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class SpiGetUpdater {

	final static String VERSION_URL = "https://api.spiget.org/v2/resources/%ID%/versions?size=" + Integer.MAX_VALUE + "&spiget__ua=SpigetDocs";
	final static String DESCRIPTION_URL = "https://api.spiget.org/v2/resources/%ID%/updates?size=" + Integer.MAX_VALUE + "&spiget__ua=SpigetDocs";
	final static String DOWNLOAD_URL = "https://api.spiget.org/v2/resources/%ID%/download";


	public static void checkAutoUpdate(JavaPlugin mainClass, int projectID, boolean autoUpdate ) {
		checkAutoUpdate(mainClass, projectID, autoUpdate,false);
	}

	public static void checkAutoUpdate(JavaPlugin mainClass, int projectID, boolean autoUpdate, boolean improperVersionOrderFix) {
		Object[] response = getLastUpdate(mainClass, projectID, improperVersionOrderFix);
		if (response.length >= 2) {
			Bukkit.broadcast(ChatColor.GOLD + "[" + mainClass.getDescription().getName() + "]  New update available:", "op");
			Bukkit.broadcast(ChatColor.GOLD + "New Version:" + ChatColor.AQUA + response[0], "op");
			Bukkit.broadcast(ChatColor.GOLD + "Installed Version:" + ChatColor.AQUA + mainClass.getDescription().getVersion(), "op");
			Bukkit.broadcast(ChatColor.GOLD + "Update Title: " + ChatColor.AQUA + response[1], "op");
			if (autoUpdate) {
				try {
					InputStream input = new URL(DOWNLOAD_URL.replaceAll("%ID%", projectID + "")).openStream();
					File currentPluginJarName = new File(URLDecoder.decode(
							mainClass.getClass().getProtectionDomain().getCodeSource().getLocation().getPath(),
							StandardCharsets.UTF_8.name()));

					File updateFile = new File(Bukkit.getUpdateFolderFile(), currentPluginJarName.getName());
					FileUtils.copyInputStreamToFile(input, updateFile);
					Bukkit.broadcast(ChatColor.YELLOW + "Download Complete! Reload server to load update.", "op");
				} catch (IOException e) {
					Bukkit.broadcast(ChatColor.RED + "Auto Updating failed. Check console for error report", "op");
					e.printStackTrace();
				}
			}
		}
	}


	public static Object[] getLastUpdate(JavaPlugin mainClass, int projectID) {
		return getLastUpdate(mainClass, projectID, false);
	}

	public static Object[] getLastUpdate(JavaPlugin mainClass, int projectID, boolean improperVersionOrderFix) {
		try {
			JSONArray versionsArray = (JSONArray) JSONValue.parseWithException(IOUtils.toString(new URL(String.valueOf(VERSION_URL.replaceAll("%ID%", projectID + "")))));
			String lastVersion = null;
			int version = -1;
			int index = 0;
			if (improperVersionOrderFix) {
				for (int i = 0; i < versionsArray.size(); i++) {
					try {
						String temp = ((JSONObject) versionsArray.get(i)).get("name").toString();
						int tempVer = Integer.parseInt(lastVersion.replaceAll("\\.", "").replaceAll("[^\\d.-]", ""));
						if (tempVer > version) {
							version = tempVer;
							lastVersion = temp;
							index = i;
						}
					} catch (Error | Exception e43) {
					}
				}
			} else {
				index = (versionsArray.size() - 1);
				lastVersion = ((JSONObject) versionsArray.get(index)).get("name").toString();
				version = Integer.parseInt(lastVersion.replaceAll("\\.", ""));
			}

			if (version > Integer.parseInt(mainClass.getDescription().getVersion().replaceAll("\\.", ""))) {
				JSONArray updatesArray = (JSONArray) JSONValue.parseWithException(IOUtils.toString(new URL(String.valueOf(DESCRIPTION_URL.replaceAll("%ID%", projectID + "")))));
				String updateName = ((JSONObject) updatesArray.get(index)).get("title").toString();

				Object[] update = {lastVersion, updateName};
				return update;
			}
		} catch (Exception e) {
			return new String[0];
		}

		return new String[0];
	}
}