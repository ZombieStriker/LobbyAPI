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
 */
package me.zombie_striker.lobbyapi;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import me.zombie_striker.lobbyapi.LobbyWorld.WeatherState;
import me.zombie_striker.lobbyapi.utils.*;
import me.zombie_striker.lobbyapi.utils.ConfigHandler.ConfigKeys;

import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class Main extends JavaPlugin implements Listener {

	private HashMap<String, World> lastWorld = new HashMap<String, World>();
	Set<LobbyWorld> worlds = new HashSet<LobbyWorld>();
	Set<LobbyServer> bungeeServers = new HashSet<LobbyServer>();
	Set<LobbyDecor> decor = new HashSet<LobbyDecor>();

	Random random = ThreadLocalRandom.current();
	int inventorySize = 9;
	Inventory inventory;

	@SuppressWarnings("unused")
	private LobbyAPI la = new LobbyAPI(this);

	String title = ChatColor.GOLD + "LobbyAPI " + ChatColor.WHITE + "- World selector";

	private boolean enablePWI = true;

	private ItemStack worldSelector = null;

	private static String prefix = ChatColor.GOLD + "[" + ChatColor.WHITE + "LobbyAPI" + ChatColor.GOLD + "]"
			+ ChatColor.WHITE;

	@EventHandler
	public void event(WeatherChangeEvent e) {
		LobbyWorld lw = LobbyAPI.getLobbyWorld(e.getWorld());
		if (lw == null)
			return;
		if (lw.getWeatherState() != WeatherState.NORMAL)
			if ((e.toWeatherState() == (lw.getWeatherState() == WeatherState.NO_RAIN))
					|| (e.toWeatherState() != (lw.getWeatherState() == WeatherState.ALWAYS_RAIN)))
				e.setCancelled(true);

	}

	@EventHandler
	public void onHungerChange(FoodLevelChangeEvent e) {
		if (LobbyAPI.getLobbyWorld(e.getEntity().getWorld()) != null
				&& LobbyAPI.getLobbyWorld(e.getEntity().getWorld()).hasDisabledHungerAndHealth())
			e.setCancelled(true);
	}

	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player)
			if (LobbyAPI.getLobbyWorld(e.getEntity().getWorld()) != null
					&& LobbyAPI.getLobbyWorld(e.getEntity().getWorld()).hasDisabledHungerAndHealth())
				e.setCancelled(true);
	}

	public void onEnable() {
		if (!getDataFolder().exists() || !new File(getDataFolder(), "config.yml").exists()) {
			getConfig().set("disallowHubCommandNoPerm", true);
			saveConfig();
		}
		ConfigHandler.setConfig(getConfig(), new File(getDataFolder(), "config.yml"), this);

		// Download the API dependancy
		if (Bukkit.getPluginManager().getPlugin("PluginConstructorAPI") == null) {
			me.zombie_striker.lobbyapi.utils.GithubDependDownloader.autoUpdate(this,
					new File(getDataFolder().getParentFile(), "PluginConstructorAPI.jar"), "ZombieStriker",
					"PluginConstructorAPI", "PluginConstructorAPI.jar");
		}

		if (getConfig().contains("CustomWorlds")) {
			try {
				/*
				 * for (String w : ConfigHandler.getCustomWorldKeys()) Bukkit.createWorld(new
				 * WorldCreator(w.toLowerCase())
				 * .environment(ConfigHandler.containsWorldVariable(w,
				 * ConfigKeys.WORLDENVIROMENT) ? Environment.valueOf((String)
				 * ConfigHandler.getWorldVariableObject(w, ConfigKeys.WORLDENVIROMENT)) :
				 * Environment.NORMAL) .seed(ConfigHandler.getCustomWorldInt(w,
				 * ConfigKeys.CustomAddedWorlds_Seed)));
				 */
				for (String w : getConfig().getConfigurationSection("CustomWorlds").getKeys(false)) {
					if (getConfig().contains("CustomWorlds." + w + ".Seeds")) {
						getConfig().set("Worlds." + w + "." + ConfigKeys.CustomAddedWorlds_Seed,
								getConfig().get("CustomWorlds." + w + ".Seeds"));
					}
				}
				getConfig().set("CustomWorlds", null);
				saveConfig();
			} catch (Error | Exception r54) {
				r54.printStackTrace();
			}
		}
		if (getConfig().contains("Worlds")) {
			for (String name : ConfigHandler.getWorlds()) {
				if (Bukkit.getWorld(name.toLowerCase()) != null)
					continue;
				/*
				 * Bukkit.getConsoleSender() .sendMessage(prefix + ChatColor.GOLD + (w == null ?
				 * "WorldInst" : (w == null ? "Location" : "Something")) + " for '" + name +
				 * "' was null. Re-creating world");
				 */
				Environment env = ConfigHandler.containsWorldVariable(name, ConfigKeys.WORLDENVIROMENT)
						? Environment.valueOf(
								(String) ConfigHandler.getWorldVariableObject(name, ConfigKeys.WORLDENVIROMENT))
						: Environment.NORMAL;
				WorldCreator wc = new WorldCreator(name.toLowerCase()).environment(env)
						.seed(ConfigHandler.getWorldVariableInt(name, ConfigKeys.CustomAddedWorlds_Seed));
				if (env == Environment.NETHER) {
					wc.generator(Bukkit.getWorlds().get(1).getGenerator());
				}
				if (env == Environment.THE_END) {
					wc.generator(Bukkit.getWorlds().get(2).getGenerator());
				}
				Bukkit.createWorld(wc);
			}
		}

		if (ConfigHandler.containsLobbyAPIVariable(ConfigKeys.WorldSelector)) {
			setWorldSelector(ConfigHandler.getLobbyAPIVariableItemstack(ConfigKeys.WorldSelector));
		}

		if (!ConfigHandler.containsLobbyAPIVariable(ConfigKeys.ENABLE_PER_WORLD_INVENTORIES)) {
			ConfigHandler.setLobbyAPIVariable(ConfigKeys.ENABLE_PER_WORLD_INVENTORIES, true);
		}

		enablePWI = ConfigHandler.getLobbyAPIVariableBoolean(ConfigKeys.ENABLE_PER_WORLD_INVENTORIES);

		if (Bukkit.getPluginManager().getPlugin("Multiverse-Inventories") != null) {
			Bukkit.getPluginManager().disablePlugin(Bukkit.getPluginManager().getPlugin("Multiverse-Inventories"));
			Bukkit.broadcastMessage(prefix
					+ " LobbyAPI is incompatible with Multiverse-Inventories. Remove Multiverse-Inventories, as LobbyAPI should handle multiple inventories");
		}
		if (Bukkit.getPluginManager().getPlugin("PerWorldInventory") != null) {
			Bukkit.getPluginManager().disablePlugin(Bukkit.getPluginManager().getPlugin("PerWorldInventory"));
			Bukkit.broadcastMessage(prefix
					+ " LobbyAPI is incompatible with PerWorldInventory. Remove PerWorldInventory, as LobbyAPI should handle multiple inventories");
		}

		LobbyCommands command = new LobbyCommands(this);
		getCommand("lobbyAPI").setExecutor(command);
		getCommand("lobby").setExecutor(command);
		getCommand("lobbyAPI").setTabCompleter(command);
		getCommand("lobby").setTabCompleter(command);

		getServer().getPluginManager().registerEvents(this, this);
		new BukkitRunnable() {
			public void run() {
				for (LobbyWorld lb : getWorlds()) {
					if (lb.hasVoidDisable()) {
						for (Player p : lb.getWorld().getPlayers()) {
							if (p.getLocation().getY() < -2) {
								p.teleport(lb.getSpawn());
								p.setVelocity(new Vector(0, 0, 0));
							}
						}
					}
				}
			}
		}.runTaskTimer(this, 0, 20);

		// Time Check
		/*
		 * new BukkitRunnable() { public void run() { for (LobbyWorld wo : worlds) if
		 * (wo != null && wo.hasStaticTime())
		 * getServer().getWorld(wo.getWorldName()).setTime(wo.getStaticTime()); }
		 * }.runTaskTimer(this, 0, 10 * 20L);
		 */
		if (getConfig() != null && getConfig().contains("hasBungee") && getConfig().getBoolean("hasBungee")) {
			getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
			getServer().getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "Bungee For LobbyAPI is enabled");
		} else {
			getServer().getConsoleSender().sendMessage(prefix + ChatColor.DARK_RED + "Bungee For LobbyAPI is disabled");
		}

		loadLocalWorlds();
		loadLocalServers();
		loadDecor();

		if (!getConfig().contains("auto-update")) {
			getConfig().set("auto-update", true);
			saveConfig();
		}

		// @SuppressWarnings("unused")
		// final Updater updater = new Updater(this, 91206, true);
		GithubUpdater.autoUpdate(this, "ZombieStriker", "LobbyAPI", "LobbyAPI");

		@SuppressWarnings("unused")
		Metrics met = new Metrics(this);
		/*
		 * met.addCustomChart(new Metrics.SimplePie("worlds-loaded") {
		 * 
		 * @Override public String getValue() { return String.valueOf(worlds.size()); }
		 * }); met.addCustomChart(new Metrics.SimplePie("bungee-support") {
		 * 
		 * @Override public String getValue() { return
		 * String.valueOf(bungeeServers.size()); } }); met.addCustomChart(new
		 * Metrics.SimplePie("updater-active") {
		 * 
		 * @Override public String getValue() { return
		 * String.valueOf(getConfig().getBoolean("auto-update")); } });
		 */
		if (!getConfig().contains("Version")
				|| !getConfig().getString("Version").equals(this.getDescription().getVersion())) {
			new UpdateAnouncer(this);
			getConfig().set("Version", this.getDescription().getVersion());
			saveConfig();
		}

		this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeMessager());
	}

	public void onDisable() {
		for (Player p : getServer().getOnlinePlayers()) {
			saveInventory(p, p.getWorld());
			LobbyWorld lw = LobbyAPI.getLobbyWorld(p.getWorld());
			setLastLocationForWorld(p, lw);
		}
	}

	public void setInventorySize(boolean isOp) {
		int maxSlot = 0;
		for (LobbyWorld w : this.worlds) {
			if (w.isHidden())
				continue;
			if (w != null && w.getSlot() > maxSlot) {
				maxSlot = w.getSlot();
			}
		}
		for (LobbyDecor d : this.decor)
			if (d.getSlot() > maxSlot)
				maxSlot = d.getSlot();

		for (LobbyServer w : this.bungeeServers) {
			if (w.isHidden())
				continue;
			if (w != null && w.getSlot() > maxSlot) {
				maxSlot = w.getSlot();
			}
		}
		inventorySize = ((maxSlot / 9) + 1) * 9;
	}

	public void startsWith(List<String> ls, String s, String arg) {
		if (s.toLowerCase().startsWith(arg.toLowerCase()))
			ls.add(s);
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onClicky(PlayerInteractEvent e) {
		// Keeping in hand for backwards compatibility.
		if (e.getPlayer().getItemInHand() != null && getWorldSelector() != null
				&& e.getPlayer().getItemInHand().isSimilar(getWorldSelector()))
			Bukkit.dispatchCommand(e.getPlayer(), "lobby");

		if (e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.ENDER_CHEST) {
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				e.setCancelled(true);
				e.getPlayer().openInventory(getEnderChest(e.getPlayer(), e.getPlayer().getWorld()));
			}
		}
	}

	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		if (e.getView().getTitle().equals("Ender Chest")) {
			saveEnderChest((Player) e.getPlayer(), e.getInventory(), e.getPlayer().getWorld());
		}
	}

	@EventHandler
	private void onPlayerLeave(PlayerQuitEvent event) {
		LobbyAPI.updateServerCount(event.getPlayer());
		LobbyWorld lw = LobbyAPI.getLobbyWorld(event.getPlayer().getWorld());
		if (lw != null) {
			if (lw.shouldWorldShouldSavePlayerLocation())
				setLastLocationForWorld(event.getPlayer(), lw);
			saveInventory(event.getPlayer(), event.getPlayer().getWorld());
		}
	}

	@EventHandler
	private void onDeath(PlayerDeathEvent event) {
		LobbyWorld lw = LobbyAPI.getLobbyWorld(event.getEntity().getWorld());
		if (lw != null)
			if (lw.shouldWorldShouldSavePlayerLocation())
				setLastLocationForWorld(event.getEntity(), lw, null);
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	private void onPlayerDeath(PlayerRespawnEvent event) {
		LobbyWorld lb = LobbyAPI.getLobbyWorld(event.getPlayer().getWorld().getName());

		if (lb == null)
			return;

		if (!event.getPlayer().getWorld().getGameRuleValue("keepInventory").equalsIgnoreCase("true"))
			clearInventory(event.getPlayer());
		saveInventory(event.getPlayer(), event.getPlayer().getWorld());

		/*
		 * if (lb == null) { if (lb.getMainWorld() != null) {
		 * event.setRespawnLocation(LobbyAPI.getLobbyWorld(lb.getMainWorld()).getSpawn()
		 * ); return; } else { return; } }
		 */

		if (event.getPlayer().getBedSpawnLocation() != null
				&& (event.getPlayer().getBedSpawnLocation().getWorld().equals(lb.getWorld())
						|| (lb.getRespawnWorld() != null && event.getPlayer().getBedSpawnLocation().getWorld()
								.equals(LobbyAPI.getLobbyWorld(lb.getRespawnWorld()).getWorld()))))
			return;
		// Do not interfere if the player has a bed in this world.

		if (lb.getRespawnWorld() != null) {
			event.setRespawnLocation(LobbyAPI.getLobbyWorld(lb.getRespawnWorld()).getSpawn());
			// } else if (LobbyWorld.getMainLobby() != null) {
			// event.setRespawnLocation(LobbyWorld.getMainLobby().getSpawn());
		} else {
			event.setRespawnLocation(lb.getSpawn());
			// Should never happen. Test it
		}
	}

	public static String getPrefix() {
		return prefix;
	}

	@EventHandler
	private void onPlayeJoin(final PlayerJoinEvent event) {
		World goingTo = event.getPlayer().getWorld();
		LobbyAPI.updateServerCount(event.getPlayer());
		if (LobbyWorld.getMainLobby() != null) {
			goingTo = LobbyWorld.getMainLobby().getWorld();
			new BukkitRunnable() {
				public void run() {
					if (event.getPlayer() != null && LobbyWorld.getMainLobby() != null
							&& LobbyWorld.getMainLobby().getSpawn() != null) {
						event.getPlayer().teleport(LobbyWorld.getMainLobby().getSpawn());
						cancel();
					}
				}
			}.runTaskTimer(this, 0, 5);
		}
		for (LobbyWorld wo : worlds)
			lastWorld.put(event.getPlayer().getName(), wo.hasMainWorld() ? wo.getWorld() : goingTo);
	}

	@EventHandler
	private void onTeleport(EntityPortalEvent event) {
		LobbyWorld curr = LobbyAPI.getLobbyWorld(event.getFrom().getWorld());
		if (curr != null && !curr.hasPortal()) {
			event.setCancelled(true);
			Bukkit.broadcastMessage("previous world is null");
			return;
		}
		if (event.getTo().getWorld() == Bukkit.getWorlds().get(2)) {
			Location temp = event.getTo();
			if (temp == null) {
				if (event.getFrom().getWorld().getEnvironment() == Environment.THE_END) {
					temp = curr.getEnd().getSpawnLocation();// new Location(curr.getEnd(), 0, 100, 0);
				} else {
					temp = curr.getSpawn();
				}
			} else {
				temp.setWorld(curr.getEnd());
			}
			event.setTo(temp);
		} else if (event.getTo().getWorld() == Bukkit.getWorlds().get(1)) {
			Location temp = event.getTo();
			if (temp == null) {
				if (event.getFrom().getWorld().getEnvironment() == Environment.NETHER) {
					temp = new Location(curr.getNether(), event.getFrom().getX() * 8, event.getFrom().getY(),
							event.getFrom().getZ() * 8);
				} else {
					temp = new Location(curr.getNether(), event.getFrom().getX() / 8, event.getFrom().getY(),
							event.getFrom().getZ() / 8);
				}
			} else {
				temp.setWorld(curr.getNether());
			}
			event.setTo(temp);
		} else if (event.getTo().getWorld() == Bukkit.getWorlds().get(0)) {
			Location loc = event.getTo();
			if (curr.getNether() != null) {
				event.getTo().setWorld(curr.getNether());
			} else if (curr.getEnd() != null) {
				event.getTo().setWorld(curr.getEnd());
			}
			event.setTo(loc);
		}

	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onTeleport(PlayerPortalEvent event) {
		LobbyWorld curr = LobbyAPI.getLobbyWorld(event.getFrom().getWorld());
		if (curr != null && !curr.hasPortal()) {
			event.setCancelled(true);
			Bukkit.broadcastMessage("previous world is null");
			return;
		}
		if (event.getCause() == TeleportCause.END_PORTAL) {
			Location temp = event.getTo();
			if (temp == null) {
				if (event.getFrom().getWorld().getEnvironment() == Environment.THE_END) {
					temp = curr.getEnd().getSpawnLocation();// new Location(curr.getEnd(), 0, 100, 0);
				} else {
					temp = curr.getSpawn();
				}
			} else {
				temp.setWorld(curr.getEnd());
			}
			event.setTo(temp);
		} else if (event.getCause() == TeleportCause.NETHER_PORTAL) {
			Location temp = event.getTo();
			if (temp == null) {
				if (event.getFrom().getWorld().getEnvironment() == Environment.NETHER) {
					temp = new Location(curr.getNether(), event.getFrom().getX() * 8, event.getFrom().getY(),
							event.getFrom().getZ() * 8);
				} else {
					temp = new Location(curr.getNether(), event.getFrom().getX() / 8, event.getFrom().getY(),
							event.getFrom().getZ() / 8);
				}
			} else {
				temp.setWorld(curr.getNether());
			}
			event.setTo(temp);
		} else if (event.getTo().getWorld() == Bukkit.getWorlds().get(0)) {
			Location loc = event.getTo();
			if (curr.getNether() != null) {
				event.getTo().setWorld(curr.getNether());
			} else if (curr.getEnd() != null) {
				event.getTo().setWorld(curr.getEnd());
			}
			if (loc != null)
				event.setTo(loc);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onWorldchange(PlayerChangedWorldEvent event) {
		final Player p = event.getPlayer();
		saveInventory(p, event.getFrom());
		LobbyWorld lw = LobbyAPI.getLobbyWorld(p.getWorld());
		final boolean sameWorld;
		if (lw != null && LobbyAPI.getLobbyWorld(event.getFrom()) != null) {
			sameWorld = lw.getSaveName().equals(LobbyAPI.getLobbyWorld(event.getFrom()).getSaveName());
		} else
			sameWorld = false;
		if (lw != null) {
			// if (lw.shouldWorldShouldSavePlayerLocation())
			// lw.setLastLocation(event.getPlayer(), event.getPlayer().getLocation());
			// setLastLocationForWorld(event.getPlayer(), lw, event.getf);
			if (enablePWI)
				if (!sameWorld) {
					clearInventory(p);
				}
		}

		new BukkitRunnable() {
			public void run() {
				if (LobbyAPI.getLobbyWorld(p.getWorld()) != null) {
					if (enablePWI) {
						// if (!sameWorld) {
						clearInventory(p);
						loadInventory(p, p.getWorld());
						// }
					}
					if (LobbyAPI.getLobbyWorld(p.getWorld()).getGameMode() != null)
						p.setGameMode(LobbyAPI.getLobbyWorld(p.getWorld().getName()).getGameMode());

					if (LobbyAPI.getLobbyWorld(p.getWorld()).getSpawnItems() != null
							&& LobbyAPI.getLobbyWorld(p.getWorld()).getSpawnItems().size() > 0)
						for (ItemStack is : LobbyAPI.getLobbyWorld(p.getWorld()).getSpawnItems())
							if (is != null)
								if (!p.getInventory().containsAtLeast(is, 1))
									p.getInventory().addItem(is);
				}
				lastWorld.put(p.getName(), p.getWorld());
			}
		}.runTaskLater(this, 5);
	}

	@EventHandler
	public void onSelect(InventoryClickEvent event) {
		if (event.getInventory() == null)
			return;
		if (event.getCurrentItem() == null)
			return;

		if (!event.getInventory().equals(event.getWhoClicked().getInventory())) {
			if (inventory != null && event.getView().getTitle().equals(title)) {
				if (event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName()) {
					event.setCancelled(true);

					boolean isBungee = false;
					if (this.bungeeServers.size() > 0) {

						for (String s : event.getCurrentItem().getItemMeta().getLore()) {
							if (s.contains(ChatColor.RED + "" + ChatColor.GREEN)) {
								isBungee = true;
								break;
							}
						}
					}

					if (isBungee) {
						for (LobbyServer s : bungeeServers) {
							if (s.getSlot() == event.getSlot()) {
								teleportBungee((Player) event.getWhoClicked(), s.getName());
								event.getWhoClicked().closeInventory();
								break;
							}

						}
					} else
						for (LobbyWorld wo : worlds) {
							if (wo == null)
								continue;
							if (wo.getSlot() == event.getSlot() && !wo.isHidden()) {
								int playersize = wo.getPlayers().size();
								if (wo.getNether() != null)
									playersize += (wo.getNether().getPlayers().size());
								if (wo.getEnd() != null)
									playersize += (wo.getEnd().getPlayers().size());

								if (!event.getWhoClicked().hasPermission("lobbyapi.bypassworldlimits")
										&& (wo.hasMaxPlayers() && playersize >= wo.getMaxPlayers())) {
									event.getWhoClicked()
											.sendMessage(ChatColor.RED + "This world is full, please try again later.");
								} else if (!event.getWhoClicked().hasPermission("lobbyapi.bypassworldlimits")
										&& (wo.isPrivate() && !wo.getWhitelistedPlayersUUID()
												.contains(event.getWhoClicked().getUniqueId()))) {
									event.getWhoClicked()
											.sendMessage(ChatColor.RED + "You are not whitelisted for this world.");
								} else {

									final PlayerSelectWorldEvent e = new PlayerSelectWorldEvent(
											(Player) event.getWhoClicked(), wo);
									// TODO: Veify: If world does not save location, return spawn.
									if (getLastLocationForWorld((Player) event.getWhoClicked(), wo) != null) {
										e.setDestination(getLastLocationForWorld((Player) event.getWhoClicked(), wo));
									}
									Bukkit.getPluginManager().callEvent(e);
									if (!e.getIsCanceled()) {

										// TODO: Find a better way of checking.
										// If the player happens to have
										// teleported from another world, this
										// will not save their location.

										LobbyWorld lw = LobbyAPI.getLobbyWorld(event.getWhoClicked().getWorld());
										if (lw != null) {
											if (lw.shouldWorldShouldSavePlayerLocation())
												setLastLocationForWorld((Player) event.getWhoClicked(), lw);
										}

										e.getPlayer().teleport(e.getDestination());

										StringBuilder playersOnline = new StringBuilder();
										Set<Player> players = wo.getPlayers();
										if (lw.getNether() != null)
											players.addAll(lw.getNether().getPlayers());
										if (lw.getEnd() != null)
											players.addAll(lw.getEnd().getPlayers());
										Object[] oo = players.toArray();
										for (int i = 0; i < (oo.length < 6 ? oo.length : 7); i++)
											playersOnline
													.append(((Player) oo[i]).getDisplayName()
															+ (i != (oo.length - 1 < 7 ? oo.length - 1 : 7) ? " ,"
																	: (oo.length - 7 > 0
																			? " ...(" + (oo.length - 7) + " more)"
																			: "")));

										try {
											Method method = e.getPlayer().getClass().getMethod("sendTitle",
													String.class, String.class);
											if (method != null) {
												method.invoke(e.getPlayer(),
														ChatColor.GOLD + "Teleporting to " + (wo.getWorldName()),
														ChatColor.GRAY + "Players: " + playersOnline.toString());
											} else {
												e.getPlayer().sendMessage(
														ChatColor.GOLD + "Teleporting to " + (wo.getWorldName()));
												e.getPlayer().sendMessage(
														ChatColor.GRAY + "Players: " + playersOnline.toString());
											}
										} catch (Exception e2) {
											e.getPlayer().sendMessage(
													ChatColor.GOLD + "Teleporting to " + (wo.getWorldName()));
											e.getPlayer().sendMessage(
													ChatColor.GRAY + "Players: " + playersOnline.toString());

										}
										event.setCancelled(true);

										event.getWhoClicked().closeInventory();
										Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
											public void run() {
												PlayerChangeWorldEvent e2 = new PlayerChangeWorldEvent(e.getPlayer());
												Bukkit.getPluginManager().callEvent(e2);
											}
										}, 2);
										for (String s : wo.getCommandsOnJoin()) {
											s = s.replaceAll("%player%", e.getPlayer().getName());
											Bukkit.dispatchCommand(e.getPlayer(), s);
										}
									} else {
										return;
									}
								}
								event.getWhoClicked().closeInventory();
								break;
							}
						}
					event.setCancelled(true);
				}
			}
		}
	}

	private void teleportBungee(Player p, String to) {
		ByteArrayDataOutput baos = ByteStreams.newDataOutput();
		try {
			baos.writeUTF("Connect");
			baos.writeUTF(to);
		} catch (Exception e) {
			e.printStackTrace();
		}
		p.sendPluginMessage(this, "BungeeCord", baos.toByteArray());
	}

	private void clearInventory(Player p) {
		p.getInventory().clear();
		p.setHealth((double) 20);
		p.setExp((float) 0);
		p.setLevel(0);
		p.setFoodLevel((int) 20);
		for (PotionEffect ep : p.getActivePotionEffects())
			p.removePotionEffect(ep.getType());
	}

	private void loadInventory(Player p, World w) {
		if (w == null) {
			getServer().getConsoleSender().sendMessage(
					prefix + " The world \"" + w + "\" is null! Inventories for this world will not be saved.");
			return;
		}
		if (LobbyAPI.getLobbyWorld(w) == null) {
			getServer().getConsoleSender().sendMessage(prefix + " You have no registered world called \"" + w.getName()
					+ "\"! Inventories for this world will not be loaded.");
			return;
		}
		if (p.getInventory() == null) {
			Bukkit.getConsoleSender().sendMessage(prefix + "Inventory is null");
			return;
		}
		String s = LobbyAPI.getLobbyWorld(w).getSaveName();
		File tempHolder = new File(getDataFolder() + File.separator + "playerfiles",
				p.getUniqueId().toString() + ".yml");
		FileConfiguration config = getConfig();
		if (tempHolder.exists())
			config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(tempHolder);

		for (String key : config.getConfigurationSection(p.getName() + "." + s + ".i").getKeys(false))
			// if ((ItemStack) config.get( p.getName() + "." + s + ".i."+ key) != null)
			if (!key.equals("offhand"))
				p.getInventory().setItem(Integer.parseInt(key),
						(ItemStack) config.get(p.getName() + "." + s + ".i." + key));
		p.getInventory().setBoots((ItemStack) config.get(p.getName() + "." + s + ".a." + 1));
		p.getInventory().setLeggings((ItemStack) config.get(p.getName() + "." + s + ".a." + 2));
		p.getInventory().setChestplate((ItemStack) config.get(p.getName() + "." + s + ".a." + 3));
		p.getInventory().setHelmet((ItemStack) config.get(p.getName() + "." + s + ".a." + 4));

		try {

			if (config.contains(p.getName() + "." + s + ".i.offhand"))
				p.getInventory().setItemInOffHand(config.getItemStack(p.getName() + "." + s + ".i.offhand"));
		} catch (Error | Exception e2) {
		}

		if (config.get(p.getName() + "." + s + ".xpl") != null)
			p.setLevel((int) config.get(p.getName() + "." + s + ".xpl"));
		if (config.get(p.getName() + "." + s + ".hunger") != null)
			p.setFoodLevel((int) config.get(p.getName() + "." + s + ".hunger"));
	}

	private Location getLastLocationForWorld(Player p, LobbyWorld lw) {
		if (lw.shouldWorldShouldSavePlayerLocation()) {
			File tempHolder = new File(getDataFolder() + File.separator + "playerfiles",
					p.getUniqueId().toString() + ".yml");
			if (!tempHolder.getParentFile().exists())
				tempHolder.getParentFile().mkdirs();
			if (!tempHolder.exists())
				try {
					tempHolder.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			FileConfiguration config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(tempHolder);
			Location loc = (Location) config.get(p.getName() + "." + lw.getSaveName() + ".LastLocation");
			return loc;
		}
		return null;
	}

	private void setLastLocationForWorld(Player p, LobbyWorld lw) {
		setLastLocationForWorld(p, lw, p.getLocation());
	}

	private void setLastLocationForWorld(Player p, LobbyWorld lw, Location newLoc) {
		if (lw != null) {
			if (lw.shouldWorldShouldSavePlayerLocation()) {
				File tempHolder = new File(getDataFolder() + File.separator + "playerfiles",
						p.getUniqueId().toString() + ".yml");
				if (!tempHolder.getParentFile().exists())
					tempHolder.getParentFile().mkdirs();
				if (!tempHolder.exists())
					try {
						tempHolder.createNewFile();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				FileConfiguration config = org.bukkit.configuration.file.YamlConfiguration
						.loadConfiguration(tempHolder);
				config.set(p.getName() + "." + lw.getSaveName() + ".LastLocation", newLoc);
				try {
					config.save(tempHolder);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void saveEnderChest(Player p, Inventory chest, World w) {

		LobbyWorld lw = LobbyAPI.getLobbyWorld(w.getName());
		if (lw == null) {
			p.sendMessage(prefix
					+ " The world you are in is not registered by LobbyAPI. Contact the server owner or OP and show them this message.");
			return;
		}
		String world2 = lw.getSaveName();
		File tempHolder = new File(getDataFolder() + File.separator + "playerfiles",
				p.getUniqueId().toString() + ".yml");
		if (!tempHolder.getParentFile().exists())
			tempHolder.getParentFile().mkdirs();
		if (!tempHolder.exists())
			try {
				tempHolder.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		FileConfiguration config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(tempHolder);

		config.set(p.getName() + "." + world2 + ".enderchest", null);
		for (int itemIndex = 0; itemIndex < chest.getSize(); itemIndex++)
			config.set(p.getName() + "." + world2 + ".enderchest." + itemIndex, chest.getContents()[itemIndex]);
		try {
			config.save(tempHolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Inventory getEnderChest(Player p, World w) {
		if (w == Bukkit.getWorlds().get(0))
			return p.getEnderChest();

		LobbyWorld lw = LobbyAPI.getLobbyWorld(w.getName());
		if (lw == null) {
			p.sendMessage(prefix
					+ " The world you are in is not registered by LobbyAPI. Contact the server owner or OP and show them this message.");
			return p.getEnderChest();
		}
		String world2 = lw.getSaveName();
		File tempHolder = new File(getDataFolder() + File.separator + "playerfiles",
				p.getUniqueId().toString() + ".yml");
		if (!tempHolder.getParentFile().exists())
			tempHolder.getParentFile().mkdirs();
		if (!tempHolder.exists())
			try {
				tempHolder.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		FileConfiguration config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(tempHolder);
		Inventory ender = Bukkit.createInventory(null, p.getEnderChest().getSize(), "Ender Chest");

		for (int itemIndex = 0; itemIndex < p.getEnderChest().getSize(); itemIndex++)
			try {
				ender.setItem(itemIndex,
						(ItemStack) config.get(p.getName() + "." + world2 + ".enderchest." + itemIndex));
			} catch (Error | Exception e4) {
			}
		return ender;
	}

	private void saveInventory(Player p, World w) {
		if (w == null) {
			getServer().getConsoleSender().sendMessage(
					prefix + " The world \"" + w + "\" is null! Inventories for this world will not be saved.");
			return;
		}
		if (LobbyAPI.getLobbyWorld(w) == null) {
			getServer().getConsoleSender().sendMessage(prefix + " You have no registered world called \"" + w.getName()
					+ "\"! Inventories for this world will not be saved.");
			return;
		}
		LobbyWorld lw = LobbyAPI.getLobbyWorld(w.getName());
		saveInventory(p, lw);

	}

	private void saveInventory(Player p, LobbyWorld lw) {
		String world2 = lw.getSaveName();
		File tempHolder = new File(getDataFolder() + File.separator + "playerfiles",
				p.getUniqueId().toString() + ".yml");
		if (!tempHolder.getParentFile().exists())
			tempHolder.getParentFile().mkdirs();
		if (!tempHolder.exists())
			try {
				tempHolder.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		FileConfiguration config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(tempHolder);
		config.set(p.getName() + "." + world2 + ".xp", p.getExp());
		config.set(p.getName() + "." + world2 + ".xpl", p.getLevel());
		config.set(p.getName() + "." + world2 + ".health", p.getHealth());
		config.set(p.getName() + "." + world2 + ".hunger", p.getFoodLevel());
		ItemStack[] is = p.getInventory().getContents();
		config.set(p.getName() + "." + world2 + ".i", null);
		for (int itemIndex = 0; itemIndex < 36; itemIndex++)
			config.set(p.getName() + "." + world2 + ".i." + itemIndex, is[itemIndex]);

		try {
			if (p.getInventory().getItemInOffHand() == null
					|| p.getInventory().getItemInOffHand().getType() == Material.AIR) {
				config.set(p.getName() + "." + world2 + ".i.offhand", null);
			} else {
				// if(config.contains(p.getName() + "." + world2 + ".i.offhand"))
				config.set(p.getName() + "." + world2 + ".i.offhand", p.getInventory().getItemInOffHand());
			}
		} catch (Error | Exception e2) {
		}

		config.set(p.getName() + "." + world2 + ".a." + 1, p.getInventory().getBoots());
		config.set(p.getName() + "." + world2 + ".a." + 2, p.getInventory().getLeggings());
		config.set(p.getName() + "." + world2 + ".a." + 3, p.getInventory().getChestplate());
		config.set(p.getName() + "." + world2 + ".a." + 4, p.getInventory().getHelmet());

		// if (lw.shouldWorldShouldSavePlayerLocation()) {
		// config.set(p.getName() + "." + world2 + ".LastLocation", p.getLocation());
		// }
		try {
			config.save(tempHolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadDecor() {
		if (getConfig().contains("Decor"))
			for (String name : getConfig().getConfigurationSection("Decor").getKeys(false)) {
				String displayname = getConfig().getString("Decor." + name + ".displayname");
				int slot = getConfig().getInt("Decor." + name + ".slot");
				List<String> lore = getConfig().getStringList("Decor." + name + ".lore");
				Material material = Material.matchMaterial(getConfig().getString("Decor." + name + ".material"));
				int data = getConfig().getInt("Decor." + name + ".durib");

				LobbyDecor decor = new LobbyDecor(slot, name, displayname);
				decor.setMaterial(material);
				decor.setData(Short.parseShort("" + data));
				decor.setLore(lore);
				this.decor.add(decor);
			}
	}

	public void loadLocalWorlds() {
		final Main main = this;
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@SuppressWarnings({ "deprecation", "unchecked" })
			public void run() {
				if (getConfig().contains("Worlds")) {
					for (final String key : getConfig().getConfigurationSection("Worlds").getKeys(false)) {
						try {
							String name = getConfig().getString("Worlds." + key + ".name");
							String displayname = null;
							if (getConfig().contains("Worlds." + key + ".displayname")) {
								displayname = getConfig().getString("Worlds." + key + ".displayname");
							} else {
								displayname = key;
							}
							if (getConfig().contains("Worlds." + key + ".loc")) {
								Location l = (Location) getConfig().get("Worlds." + key + ".loc");
								if (l != null) {
									getConfig().set("Worlds." + key + ".spawnLoc.x", l.getX());
									getConfig().set("Worlds." + key + ".spawnLoc.y", l.getY());
									getConfig().set("Worlds." + key + ".spawnLoc.z", l.getZ());
								} else {
									getConfig().set("Worlds." + key + ".spawnLoc.x", 0);
									getConfig().set("Worlds." + key + ".spawnLoc.y", 90);
									getConfig().set("Worlds." + key + ".spawnLoc.z", 0);
									Bukkit.broadcastMessage(prefix + " SpawnLocation has been reset for world " + name
											+ ". Please reset the spawnlocation by using /LobbyAPI changeSpawn.");
								}
								/**
								 * Only here to make sure that the loc variable will **always** be removed in
								 * case a user updates
								 */
								getConfig().set("Worlds." + key + ".loc", null);
								saveConfig();
							}
							double x = getConfig().getDouble("Worlds." + key + ".spawnLoc.x");
							double y = getConfig().getDouble("Worlds." + key + ".spawnLoc.y");
							double z = getConfig().getDouble("Worlds." + key + ".spawnLoc.z");
							World w = Bukkit.getWorld(name.toLowerCase());
							Location l = new Location(w, x, y, z);
							if (l == null || w == null) {
								Bukkit.getConsoleSender()
										.sendMessage(prefix + ChatColor.GOLD
												+ (w == null ? "WorldInst" : (w == null ? "Location" : "Something"))
												+ " for '" + name + "' was null. Re-creating world");
								Environment env = ConfigHandler.containsWorldVariable(key, ConfigKeys.WORLDENVIROMENT)
										? Environment.valueOf((String) ConfigHandler.getWorldVariableObject(key,
												ConfigKeys.WORLDENVIROMENT))
										: Environment.NORMAL;
								WorldCreator wc = new WorldCreator(name.toLowerCase()).environment(env).seed(
										ConfigHandler.getWorldVariableInt(key, ConfigKeys.CustomAddedWorlds_Seed));
								if (env == Environment.NETHER) {
									wc.generator(Bukkit.getWorlds().get(1).getGenerator());
								}
								if (env == Environment.THE_END) {
									wc.generator(Bukkit.getWorlds().get(2).getGenerator());
								}
								w = Bukkit.createWorld(wc);
								l = new Location(w, x, y, z);
								if (l == null || w == null)
									Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.RED
											+ (w == null ? "WorldInst" : (w == null ? "Location" : "Something"))
											+ " for '" + name
											+ "' is still null after recreating world. Something may be wrong!");
							}
							if (getConfig().contains("Worlds." + key + ".spawnLoc.yaw")) {
								float yaw = (float) getConfig().getDouble("Worlds." + key + ".spawnLoc.yaw");
								float pitch = (float) getConfig().getDouble("Worlds." + key + ".spawnLoc.pitch");
								l.setYaw(yaw);
								l.setPitch(pitch);
							}

							int i = getConfig().getInt("Worlds." + key + ".i");
							String save = getConfig().getString("Worlds." + key + ".save");
							if (save == null)
								save = key;
							String desc = getConfig().getString("Worlds." + key + ".desc");
							int color = getConfig().getInt("Worlds." + key + ".color");
							GameMode gm = null;
							if (getConfig().contains("Worlds." + key + ".gamemode"))
								try {
									String s4 = getConfig().getString("Worlds." + key + ".gamemode");
									for (GameMode g : GameMode.values())
										if (g.name().equals(s4))
											gm = g;
								} catch (Exception e) {
									e.printStackTrace();
								}

							if (LobbyAPI.getLobbyWorld(name) != null)
								LobbyAPI.unregisterWorld(getServer().getWorld(name.toLowerCase()));

							final LobbyWorld lw = LobbyAPI.registerWorldFromConfig(w, l, save, desc, color, i, gm,
									false);
							for (String jC : getConfig().getStringList("Worlds." + key + ".joincommands"))
								lw.addCommand(jC);
							final World fWorld = w;
							new BukkitRunnable() {

								@Override
								public void run() {
									if (getConfig().contains("Worlds." + key + "." + ConfigKeys.PORTALLIST)) {
										List<Location> portals = new ArrayList<>();
										List<String> listAsString = getConfig()
												.getStringList("Worlds." + key + "." + ConfigKeys.PORTALLIST);
										for (String a : listAsString) {
											String[] splits = a.split(",");
											int x2 = Integer.parseInt(splits[0]);
											int y2 = Integer.parseInt(splits[1]);
											int z2 = Integer.parseInt(splits[2]);
											Location portaltest = new Location(fWorld, x2, y2, z2);
											portals.add(portaltest);
										}
										lw.setPortalLocations(portals);
									}
								}
							}.runTaskLater(main, 2);

							if (getConfig().contains("Worlds." + key + ".weatherstate")) {
								WeatherState ws = WeatherState.getWeatherStateByName(
										getConfig().getString("Worlds." + key + ".weatherstate"));
								lw.setWeatherState(ws);
							}

							lw.setDisplayName(displayname);

							boolean hidden = getConfig().contains("Worlds." + key + ".hidden")
									? getConfig().getBoolean("Worlds." + key + ".hidden")
									: false;
							lw.setHidden(hidden);

							int maxplayers = getConfig().contains("Worlds." + key + ".maxPlayers")
									? getConfig().getInt("Worlds." + key + ".maxPlayers")
									: -1;
							lw.setMaxPlayers(maxplayers >= 0, maxplayers);

							boolean locsaving = getConfig().contains("Worlds." + key + ".shouldsavelocation")
									? getConfig().getBoolean("Worlds." + key + ".shouldsavelocation")
									: false;
							lw.setWorldShouldSavePlayerLocation(locsaving);

							boolean portal = getConfig().contains("Worlds." + key + ".canuseportals")
									? getConfig().getBoolean("Worlds." + key + ".canuseportals")
									: false;
							lw.setPortal(portal);

							if (getConfig().contains("Worlds." + key + ".connectedTo")) {
								getConfig().set("Worlds." + key + ".respawnWorld",
										getConfig().getString("Worlds." + key + ".connectedTo"));
								getConfig().set("Worlds." + key + ".connectedTo", null);
								saveConfig();
							}
							if (getConfig().contains("Worlds." + key + ".respawnWorld")) {
								lw.setRespawnWorld(Bukkit.getWorld(
										getConfig().getString("Worlds." + key + ".respawnWorld").toLowerCase()));
							}
							new BukkitRunnable() {

								@Override
								public void run() {
									if (getConfig().contains("Worlds." + key + "." + ConfigKeys.LINKED_END))
										lw.setEnd(Bukkit.getWorld(ConfigHandler
												.getWorldVariableString(key, ConfigKeys.LINKED_END).toLowerCase()));
									if (getConfig().contains("Worlds." + key + "." + ConfigKeys.LINKED_NETHER))
										lw.setNether(Bukkit.getWorld(ConfigHandler
												.getWorldVariableString(key, ConfigKeys.LINKED_NETHER).toLowerCase()));
								}
							}.runTaskLater(Main.this, 6);

							if (ConfigHandler.containsWorldVariable(lw, ConfigKeys.DefaultItems))
								lw.setSpawnItems((List<ItemStack>) ConfigHandler.getWorldVariableObject(lw,
										ConfigKeys.DefaultItems));

							if (ConfigHandler.containsWorldVariable(lw, ConfigKeys.DisableHealthAndHunger))
								lw.setDisableHungerAndHealth(
										ConfigHandler.getWorldVariableBoolean(lw, ConfigKeys.DisableHealthAndHunger));
							if (ConfigHandler.containsWorldVariable(lw, ConfigKeys.DisableVoid))
								lw.setVoidDisable(ConfigHandler.getWorldVariableBoolean(lw, ConfigKeys.DisableVoid));

							boolean isprivate = getConfig().contains("Worlds." + key + ".isprivate")
									? getConfig().getBoolean("Worlds." + key + ".isprivate")
									: false;
							List<String> uuids = getConfig().contains("Worlds." + key + ".whitelistedUUIDS")
									? getConfig().getStringList("Worlds." + key + ".whitelistedUUIDS")
									: null;

							lw.setIsPrivate(isprivate);
							if (uuids != null)
								lw.initWhitelist(uuids);

							if (getConfig().contains("Worlds." + key + ".material"))
								lw.setMaterial(
										Material.matchMaterial(getConfig().getString("Worlds." + key + ".material")));

							Bukkit.getConsoleSender().sendMessage(prefix + " Added world '" + name + "'");
							if (getConfig().contains("Worlds." + key + ".isMainLobby")
									&& getConfig().getBoolean("Worlds." + key + ".isMainLobby")) {
								LobbyWorld.setMainLobby(LobbyAPI.getLobbyWorld(getServer().getWorld(name)));
								Bukkit.getConsoleSender()
										.sendMessage(prefix + ChatColor.GOLD + "'" + name + "' is now the main lobby.");

							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}, 2L);
	}

	public void loadLocalServers() {
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				if (getConfig().contains("Server")) {
					for (String fi : getConfig().getConfigurationSection("Server").getKeys(false)) {
						String s = getConfig().getString("Server." + fi + ".name");
						int i = getConfig().getInt("Server." + fi + ".i");
						int color = getConfig().getInt("Server." + fi + ".color");
						LobbyAPI.unregisterBungeeServer(s);
						LobbyAPI.registerBungeeServerFromConfig(s, i, color);
						LobbyServer server = getBungeeServer(s);
						if (getConfig().contains("Server." + fi + ".material"))
							server.setMaterial(
									Material.matchMaterial(getConfig().getString("Server." + fi + ".material")));
						if (getConfig().contains("Server." + fi + ".displayname"))
							server.setDisplayName(ChatColor.translateAlternateColorCodes('&',
									getConfig().getString("Server." + fi + ".displayname")));
						if (getConfig().contains("Server." + fi + ".lore"))
							server.setLore(getConfig().getStringList("Server." + fi + ".lore"));
						Bukkit.getConsoleSender().sendMessage(prefix + " Added server '" + s + "'");
					}
				}
			}
		}, 2L);
	}

	public LobbyServer getBungeeServer(String name) {
		for (LobbyServer s : bungeeServers) {
			if (s.getName().equals(name))
				return s;
		}
		return null;
	}

	public ItemStack getWorldSelector() {
		return worldSelector;
	}

	public void setWorldSelector(ItemStack i) {
		worldSelector = i;
	}

	public HashMap<String, World> getLastWorld() {
		return lastWorld;
	}

	public Set<LobbyWorld> getWorlds() {
		return worlds;
	}

	public Set<LobbyServer> getBungeeServers() {
		return bungeeServers;
	}

}
