package me.zombie_striker.lobbyapi.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class GithubUpdater {

	public static boolean autoUpdate(final Plugin main, String author, String githubProject, String jarname) {
		try {
			String version = main.getDescription().getVersion();
			String parseVersion = version.replace(".", "");

			String tagname = null;
			URL api = new URL("https://api.github.com/repos/" + author + "/" + githubProject + "/releases/latest");
			URLConnection con = api.openConnection();
			con.setConnectTimeout(15000);
			con.setReadTimeout(15000);

			JsonObject json = null;
			try {
				json = new JsonParser().parse(new InputStreamReader(con.getInputStream())).getAsJsonObject();
			} catch (Error | Exception e45) {
				return false;
			}
			tagname = json.get("tag_name").getAsString();

			String parsedTagName = tagname.replace(".", "");

			int latestVersion = Integer.valueOf(parsedTagName.substring(1).replaceAll("[^\\d.]", ""));
			int parsedVersion = Integer.parseInt(parseVersion.replaceAll("[^\\d.]", ""));

			final URL download = new URL("https://github.com/" + author + "/" + githubProject + "/releases/download/"
					+ tagname + "/" + jarname);

			if (latestVersion > parsedVersion) {
				Bukkit.getConsoleSender()
						.sendMessage(ChatColor.GREEN + "Found a new version of " + ChatColor.GOLD
								+ main.getDescription().getName() + ": " + ChatColor.WHITE + tagname
								+ ChatColor.LIGHT_PURPLE + " downloading now!!");

				new BukkitRunnable() {

					@Override
					public void run() {
						try {

							InputStream in = download.openStream();

							File pluginFile = null;

							pluginFile = new File(URLDecoder.decode(
									this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath(),
									StandardCharsets.UTF_8.name()));

							// File temp = new File("plugins/update");
							// if (!temp.exists()) {
							// temp.mkdir();
							// }

							File tempInCaseSomethingGoesWrong = new File(main.getName() + "-backup.jar");
							copy(new FileInputStream(pluginFile), new FileOutputStream(tempInCaseSomethingGoesWrong));

							// Path path = new File("plugins/update" + File.separator + "COD.jar").toPath();
							pluginFile.setWritable(true, false);
							pluginFile.delete();
							// Files.copy(in, pluginFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
							copy(in, new FileOutputStream(pluginFile));

							if (pluginFile.length() < 1000) {
								// Plugin is too small. Keep old version in case new one is
								// incomplete/nonexistant
								copy(new FileInputStream(tempInCaseSomethingGoesWrong),
										new FileOutputStream(pluginFile));
							} else {
								// Plugin is valid, and we can delete the temp
								tempInCaseSomethingGoesWrong.delete();
							}

						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}.runTaskLaterAsynchronously(main, 0);
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static long copy(InputStream in, OutputStream out) throws IOException {
		long bytes = 0;
		byte[] buf = new byte[0x1000];
		while (true) {
			int r = in.read(buf);
			if (r == -1)
				break;
			out.write(buf, 0, r);
			bytes += r;
			// debug("Another 4K, current: " + r);
		}
		out.flush();
		out.close();
		in.close();
		return bytes;
	}

}
