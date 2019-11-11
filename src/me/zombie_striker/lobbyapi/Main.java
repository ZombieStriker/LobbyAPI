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

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.internal.$Gson$Types;
import me.zombie_striker.lobbyapi.LobbyWorld.WeatherState;
import me.zombie_striker.lobbyapi.utils.*;
import me.zombie_striker.lobbyapi.utils.ConfigHandler.ConfigKeys;
import me.zombie_striker.lobbyapi.utils.portalgroup.NetherPortalFinder;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Main extends JavaPlugin implements Listener {

	private static String prefix = ChatColor.GOLD + "[" + ChatColor.WHITE + "LobbyAPI" + ChatColor.GOLD + "]"
			+ ChatColor.WHITE;
	Set<LobbyServer> bungeeServers = new HashSet<LobbyServer>();
	Set<LobbyDecor> decor = new HashSet<LobbyDecor>();

	Random random = ThreadLocalRandom.current();
	int inventorySize = 9;
	Inventory inventory;
	String title = ChatColor.GOLD + "LobbyAPI " + ChatColor.WHITE + "- World selector";
	private HashMap<String, World> lastWorld = new HashMap<String, World>();
	@SuppressWarnings("unused")
	private LobbyAPI la = new LobbyAPI(this);
	private boolean enablePWI = true;
	private ItemStack worldSelector = null;

	private boolean enableCustomEnderchests = true;

	private boolean enableTeleportToSpawnIfSameWorld = true;

	private String enderchest_title = "Ender Chest";

	public static String getPrefix() {
		return prefix;
	}

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
	public void onTeleport(PlayerTeleportEvent event) {
		if (event instanceof PlayerPortalEvent)
			return;
		if (event.getTo().getWorld() != event.getFrom().getWorld()) {
			setLastLocationForWorld(event.getPlayer(), event.getFrom().getWorld(), event.getFrom());
		} else {
			setLastLocationForWorld(event.getPlayer(), event.getTo().getWorld(), event.getTo());
		}
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
				WorldCreator wc = new WorldCreator(name.toLowerCase()).environment(env);
				if (ConfigHandler.containsWorldVariable(name, ConfigKeys.CustomAddedWorlds_Seed))
					wc.seed(ConfigHandler.getWorldVariableInt(name, ConfigKeys.CustomAddedWorlds_Seed));
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
		if (!ConfigHandler.containsLobbyAPIVariable(ConfigKeys.PER_WORLD_ENNDERCHESTS)) {
			ConfigHandler.setLobbyAPIVariable(ConfigKeys.PER_WORLD_ENNDERCHESTS, enableCustomEnderchests);
		}
		if (!ConfigHandler.containsLobbyAPIVariable(ConfigKeys.TELEPORTTOSPAWN)) {
			ConfigHandler.setLobbyAPIVariable(ConfigKeys.TELEPORTTOSPAWN, enableTeleportToSpawnIfSameWorld);
		}

		enablePWI = ConfigHandler.getLobbyAPIVariableBoolean(ConfigKeys.ENABLE_PER_WORLD_INVENTORIES);
		enableCustomEnderchests = ConfigHandler.getLobbyAPIVariableBoolean(ConfigKeys.ENABLE_PER_WORLD_INVENTORIES);
		enableTeleportToSpawnIfSameWorld = ConfigHandler.getLobbyAPIVariableBoolean(ConfigKeys.TELEPORTTOSPAWN);


		Plugin minv = Bukkit.getPluginManager().getPlugin("Multiverse-Inventories");
		if (minv != null) {
			Bukkit.getPluginManager().disablePlugin(minv);
			Bukkit.broadcastMessage(prefix
					+ " LobbyAPI is incompatible with Multiverse-Inventories. Remove Multiverse-Inventories, as LobbyAPI should handle multiple inventories");
		}
		Plugin pwi = Bukkit.getPluginManager().getPlugin("PerWorldInventory");
		if (enablePWI && pwi != null) {
			Bukkit.getPluginManager().disablePlugin(pwi);
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
				for (LobbyWorld lb : LobbyWorld.getLobbyWorlds()) {
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
		//GithubUpdater.autoUpdate(this, "ZombieStriker", "LobbyAPI", "LobbyAPI.jar");
		SpiGetUpdater.checkAutoUpdate(this, 17659, getConfig().getBoolean("auto-update") == true);

		@SuppressWarnings("unused")
		Metrics met = new Metrics(this);

		if (!getConfig().contains("Version")
				|| !getConfig().getString("Version").equals(this.getDescription().getVersion())) {
			getConfig().set("Version", this.getDescription().getVersion());
			saveConfig();
		}

		this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeMessager());
	}

	public void onDisable() {
	}

	public void setInventorySize(boolean isOp) {
		int maxSlot = 0;
		for (LobbyWorld w : LobbyWorld.getLobbyWorlds()) {
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
			LobbyAPI.openGUI(e.getPlayer());

		if (enableCustomEnderchests) {
			LobbyWorld lw = LobbyAPI.getLobbyWorld(e.getPlayer().getWorld());
			if (lw != null) {
				if (e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.ENDER_CHEST) {
					if (e.getAction() == Action.RIGHT_CLICK_BLOCK && !e.getPlayer().isSneaking() && !e.getClickedBlock().getRelative(BlockFace.UP).getType().isSolid()) {
						e.setCancelled(true);
						e.getPlayer().openInventory(getEnderChest(e.getPlayer(), e.getPlayer().getWorld()));
					}
				}
			}
		}
	}

	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		if (enableCustomEnderchests) {
			LobbyWorld lw = LobbyAPI.getLobbyWorld(e.getPlayer().getWorld());
			if (lw != null) {
				if (e.getView().getTitle().equals(enderchest_title)) {
					saveEnderChest((Player) e.getPlayer(), e.getInventory(), e.getPlayer().getWorld());
				}
			}
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
		LobbyWorld lb = LobbyAPI.getLobbyWorld(event.getPlayer().getWorld());

		if (lb == null)
			return;

		/*if (!event.getPlayer().getWorld().getGameRuleValue("keepInventory").equalsIgnoreCase("true")) {
			clearInventory(event.getPlayer());
		}*/
		saveInventory(event.getPlayer(), event.getPlayer().getWorld());
		try {
			if (event.getPlayer().getBedSpawnLocation() != null
					&& (event.getPlayer().getBedSpawnLocation().getWorld().equals(lb.getWorld())))
				return;
		} catch (Error | Exception e231124) {
		}
		event.setRespawnLocation(lb.getSpawn());
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
						lastWorld.put(event.getPlayer().getName(), LobbyWorld.getMainLobby().getWorld());
						if (LobbyWorld.getMainLobby() != null && LobbyWorld.getMainLobby().getSpawnItems() != null)
							for (ItemStack is : LobbyWorld.getMainLobby().getSpawnItems())
								if (is != null)
									if (!event.getPlayer().getInventory().containsAtLeast(is, is.getAmount()))
										event.getPlayer().getInventory().addItem(is);
						cancel();
					}
				}
			}.runTaskTimer(this, 0, 5);
		} else {
			LobbyWorld lw = LobbyAPI.getLobbyWorld(goingTo);
			if (lw != null && lw.getSpawnItems() != null && lw.getSpawnItems().size() > 0) {

				new BukkitRunnable() {
					public void run() {
						for (ItemStack is : lw.getSpawnItems())
							if (is != null)
								if (!event.getPlayer().getInventory().containsAtLeast(is, is.getAmount()))
									event.getPlayer().getInventory().addItem(is);
					}
				}.runTaskTimer(this, 0, 5);
			}

		}
		lastWorld.put(event.getPlayer().getName(), event.getPlayer().getWorld());
	}

	@EventHandler
	public void onTorchDrop(ItemSpawnEvent event) {
		//TODO: find better fix.
		if (event.getEntityType() == EntityType.DROPPED_ITEM) {
			Item item = (Item) event.getEntity();
			if (item.getItemStack().getType() == Material.TORCH && item.getWorld().getEnvironment() == Environment.THE_END) {
				try {
					if (item.getLocation().getBlock().getType() != Material.END_PORTAL)
						return;
				} catch (Error | Exception e4) {
					if (!item.getLocation().getBlock().getType().name().equals("ENDER_PORTAL"))
						return;
				}
				item.setPortalCooldown(200);
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler
	private void onTeleport(EntityPortalEvent event) {

		LobbyWorld curr = LobbyAPI.getLobbyWorld(event.getFrom().getWorld());
		Location to = handlePortalConversion(event.getEntity(), curr, event.getFrom(), event.getTo(), (event.getTo().getWorld() == Bukkit.getWorlds().get(2) ? TeleportCause.END_PORTAL : TeleportCause.NETHER_PORTAL),
				event);
		if (to != null)
			event.setTo(to);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onTeleport(PlayerPortalEvent event) {
		LobbyWorld curr = LobbyAPI.getLobbyWorld(event.getFrom().getWorld());
		//saveInventory(event.getPlayer(), curr);
		Location to = handlePortalConversion(event.getPlayer(), curr, event.getFrom(), event.getTo(), event.getCause(),
				event);
		setLastLocationForWorld(event.getPlayer(), event.getFrom().getWorld(), event.getFrom());
		if (to != null) {
			event.setTo(to);
		}
		new BukkitRunnable() {
			public void run() {
				//	loadInventory(event.getPlayer(), event.getPlayer().getWorld());
			}
		}.runTaskLater(this, 5);
	}

	private Location handlePortalConversion(Entity to, LobbyWorld curr, Location getFrom, Location getTo,
											TeleportCause cause, Event event) {
		Location portal = getTo;

		if (curr != null && !curr.hasPortal()) {
			((Cancellable) event).setCancelled(true);
			Bukkit.broadcastMessage("previous world is null");
			return null;
		}
		if (cause == TeleportCause.END_PORTAL) {
			if (getFrom.getWorld().getEnvironment() == Environment.THE_END) {
				portal = curr.getSpawn();
			} else {
				//portal = getTo.clone();
				//	getTo.setWorld(curr.getEnd());
				portal = curr.getEnd().getSpawnLocation();
			}
		} else if (cause == TeleportCause.NETHER_PORTAL) {
			if (getFrom.getWorld().getEnvironment() == Environment.NETHER) {
				portal = new Location(curr.getWorld(), getFrom.getX() * 8, getFrom.getY(), getFrom.getZ() * 8);
			} else {
				portal = new Location(curr.getNether(), getFrom.getX() / 8, getFrom.getY(), getFrom.getZ() / 8);
			}
		} else if (getTo.getWorld() == Bukkit.getWorlds().get(0)) {
			if (curr.getNether() != null) {
				portal.setWorld(curr.getNether());
			} else if (curr.getEnd() != null) {
				portal.setWorld(curr.getEnd());
			}
		}

		if (portal != null) {
			try {
				if (!portal.getBlock().getType().name().endsWith("PORTAL")) {
					NetherPortalFinder.locate(portal);
				}
			} catch (Error | Exception e4) {
			}
			return portal;
		}

		return null;

	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onWorldchange(PlayerChangedWorldEvent event) {
		final Player p = event.getPlayer();
		final LobbyWorld lw = LobbyAPI.getLobbyWorld(p.getWorld());
		final LobbyWorld lwFrom = LobbyAPI.getLobbyWorld(event.getFrom());

		saveInventory(p, lwFrom);

		lastWorld.put(p.getName(), p.getWorld());

		if (lw != null) {
			if (enablePWI)
				if (lwFrom == null || (!lwFrom.getSaveName().equals(lw.getSaveName())))
					clearInventory(p);
		}


		new BukkitRunnable() {
			public void run() {
				LobbyWorld lwTo = LobbyAPI.getLobbyWorld(p.getWorld());
				if (lwFrom == null || (!lwFrom.getSaveName().equals(lwTo.getSaveName()))) {
					if (lwTo != null) {
						if (enablePWI) {
							loadInventory(p, lwTo);
						}
						if (lwTo.getGameMode() != null && !lwTo.getSaveName().equals(lwFrom.getSaveName()))
							p.setGameMode(lwTo.getGameMode());

						if (lwTo.getSpawnItems() != null && lwTo.getSpawnItems().size() > 0)
							for (ItemStack is : lwTo.getSpawnItems())
								if (is != null)
									if (!p.getInventory().containsAtLeast(is, 1))
										p.getInventory().addItem(is);
					}
				} else {
					p.updateInventory();
				}

				lastWorld.put(p.getName(), p.getWorld());
			}
		}.runTaskLater(this, 16);

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
						for (LobbyWorld wo : LobbyWorld.getLobbyWorlds()) {
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

									Location lastLoc;
									if (enableTeleportToSpawnIfSameWorld && wo.getSpawn().getWorld() == event.getWhoClicked().getWorld()) {
										e.setDestination(wo.getSpawn());
									} else {
										if ((lastLoc = getLastLocationForWorld((Player) event.getWhoClicked(), wo)) != null) {
											e.setDestination(lastLoc);
										}
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
		p.setFoodLevel(20);
		for (PotionEffect ep : p.getActivePotionEffects())
			p.removePotionEffect(ep.getType());
	}

	private void loadInventory(Player p, World w) {
		if (w == null) {
			getServer().getConsoleSender().sendMessage(
					prefix + " The world that " + p.getName() + " is teleporting to is null! Inventories for this world will not be saved.");
			return;
		}
		LobbyWorld lw = LobbyAPI.getLobbyWorld(w);
		if (lw == null) {
			getServer().getConsoleSender().sendMessage(prefix + " You have no registered worlds called \"" + w.getName()
					+ "\"! Inventories for this world will not be loaded.");
			return;
		}
		loadInventory(p, lw);

	}

	private void loadInventory(Player p, LobbyWorld lw) {
		if (p.getInventory() == null) {
			Bukkit.getConsoleSender().sendMessage(prefix + "Inventory is null");
			return;
		}
		String s = lw.getSaveName();
		File tempHolder = new File(getDataFolder() + File.separator + "playerfiles",
				p.getUniqueId().toString() + ".yml");
		FileConfiguration config = getConfig();
		if (tempHolder.exists())
			config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(tempHolder);
		if (!config.contains(p.getName() + "." + s))
			return;
		if (config.contains(p.getName() + "." + s + ".i")) {
			for (int i = 0; i < p.getInventory().getSize(); i++) {
				if (config.contains(p.getName() + "." + s + ".i." + i)) {
					Object item = config.get(p.getName() + "." + s + ".i." + i);
					;
					if (item instanceof ItemStack) {
						ItemStack temp = (ItemStack) item;
						if (!temp.equals(p.getInventory().getItem(i))) {
							p.getInventory().setItem(i, temp);
						}
					} else if (item instanceof String) {
						String[] b = ((String) item).split(",");
						ItemStack recreate = new ItemStack(Material.matchMaterial(b[0]));
						if (b.length > 1)
							recreate.setAmount(Integer.parseInt(b[1]));
						if (b.length > 2)
							recreate.setDurability(Short.parseShort(b[2]));
						if (!recreate.equals(p.getInventory().getItem(i))) {
							p.getInventory().setItem(i, recreate);
						}
					}
				} else {
					if (p.getInventory().getItem(i) != null)
						p.getInventory().setItem(i, new ItemStack(Material.AIR));
				}
			}
		}
		p.getInventory().setBoots((ItemStack) config.get(p.getName() + "." + s + ".a." + 1));
		p.getInventory().setLeggings((ItemStack) config.get(p.getName() + "." + s + ".a." + 2));
		p.getInventory().setChestplate((ItemStack) config.get(p.getName() + "." + s + ".a." + 3));
		p.getInventory().setHelmet((ItemStack) config.get(p.getName() + "." + s + ".a." + 4));

		try {
			if (config.contains(p.getName() + "." + s + ".i.offhand"))
				p.getInventory().setItemInOffHand(config.getItemStack(p.getName() + "." + s + ".i.offhand"));
		} catch (Error | Exception e2) {
		}

		if (config.contains(p.getName() + "." + s + ".xpl"))
			p.setLevel((int) config.get(p.getName() + "." + s + ".xpl"));
		if (config.contains(p.getName() + "." + s + ".xp"))
			p.setExp((float) (double) config.get(p.getName() + "." + s + ".xp"));
		if (config.contains(p.getName() + "." + s + ".hunger"))
			p.setFoodLevel((int) config.get(p.getName() + "." + s + ".hunger"));

		if (config.contains(p.getName() + "." + s + ".potions_effects")) {
			for (String effect : config.getConfigurationSection(p.getName() + "." + s + ".potions_effects").getKeys(false)) {
				PotionEffectType type = PotionEffectType.getByName(effect);
				int duration = config.getInt(p.getName() + "." + s + ".potions_effects." + effect + ".dur");
				int amplifier = config.getInt(p.getName() + "." + s + ".potions_effects." + effect + ".amp");
				p.addPotionEffect(new PotionEffect(type, duration, amplifier));
			}
		}

		if (config.contains(p.getName() + "." + s + ".allowflight"))
			p.setAllowFlight(config.getBoolean(p.getName() + "." + s + ".allowflight"));
		if (config.contains(p.getName() + "." + s + ".compasslocation"))
			p.setCompassTarget((Location) config.get(p.getName() + "." + s + ".compasslocation"));
		if (config.contains(p.getName() + "." + s + ".fireticks"))
			p.setFireTicks(config.getInt(p.getName() + "." + s + ".fireticks"));
		try {
			if (config.contains(p.getName() + "." + s + ".bedspawn"))
				p.setBedSpawnLocation((Location) config.get(p.getName() + "." + s + ".bedspawn"), true);
		} catch (Error | Exception e45) {
		}
		if (config.contains(p.getName() + "." + s + ".saturation"))
			p.setSaturation((float) (double) config.get(p.getName() + "." + s + ".saturation"));
		if (config.contains(p.getName() + "." + s + ".exhaustion"))
			p.setExhaustion((float) (double) config.get(p.getName() + "." + s + ".exhaustion"));
		if (config.contains(p.getName() + "." + s + ".air"))
			p.setRemainingAir(config.getInt(p.getName() + "." + s + ".air"));


		if (config.contains(p.getName() + "." + s + ".advancements")) {
			try {
				Iterator<org.bukkit.advancement.Advancement> it = Bukkit.advancementIterator();
				for (org.bukkit.advancement.Advancement a = it.next(); it.hasNext(); a = it.next()) {
					if (a.getKey().getKey().startsWith("recipes"))
						continue;
					if (config.contains(p.getName() + "." + s + ".advancements." + a.getKey().getKey() + ".won")) {
						org.bukkit.advancement.AdvancementProgress progress = p.getAdvancementProgress(a);
						for (String adv : new ArrayList<>(progress.getRemainingCriteria())) {
							progress.awardCriteria(adv);
						}
					} else if (config.contains(p.getName() + "." + s + ".advancements." + a.getKey().getKey() + ".awarded")) {
						Collection<String> awarded = config.getStringList(p.getName() + "." + s + ".advancements." + a.getKey().getKey() + ".awarded");
						org.bukkit.advancement.AdvancementProgress progress = p.getAdvancementProgress(a);
						for (String adv : a.getCriteria()) {
							if (awarded.contains(adv)) {
								if (!progress.getAwardedCriteria().contains(adv))
									progress.awardCriteria(adv);
							} else {
								if (progress.getAwardedCriteria().contains(adv))
									progress.revokeCriteria(adv);
							}
						}
					} else if (config.contains(p.getName() + "." + s + ".advancements." + a.getKey().getKey() + ".remaining")) {
						Collection<String> remaining = config.getStringList(p.getName() + "." + s + ".advancements." + a.getKey().getKey() + ".remaining");
						org.bukkit.advancement.AdvancementProgress progress = p.getAdvancementProgress(a);
						for (String adv : a.getCriteria()) {
							if (!remaining.contains(adv)) {
								if (!progress.getAwardedCriteria().contains(adv))
									progress.awardCriteria(adv);
							} else {
								if (progress.getAwardedCriteria().contains(adv))
									progress.revokeCriteria(adv);
							}
						}
					} else {
						//Is not in config. Player was not rewarded
						org.bukkit.advancement.AdvancementProgress progress = p.getAdvancementProgress(a);
						for (String adv : new ArrayList<>(progress.getAwardedCriteria())) {
							progress.revokeCriteria(adv);
						}
					}
				}
			} catch (Error | Exception e4) {
			}
		} else {
			try {
				Iterator<org.bukkit.advancement.Advancement> it = Bukkit.advancementIterator();
				for (org.bukkit.advancement.Advancement a = it.next(); it.hasNext(); a = it.next()) {
					if (a.getKey().getKey().startsWith("recipes"))
						continue;
					//Is not in config. Player was not rewarded
					org.bukkit.advancement.AdvancementProgress progress = p.getAdvancementProgress(a);
					for (String adv : new ArrayList<>(progress.getAwardedCriteria())) {
						progress.revokeCriteria(adv);
					}
				}
			} catch (Error | Exception e4) {
			}
		}
	}

	protected Location getLastLocationForWorld(Player p, LobbyWorld lw) {
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

	protected void setLastLocationForWorld(Player p) {
		LobbyWorld lw = LobbyAPI.getLobbyWorld(p.getWorld());
		if (lw != null)
			if (lw.shouldWorldShouldSavePlayerLocation())
				setLastLocationForWorld(p, lw, p.getLocation());
	}

	protected void setLastLocationForWorld(Player p, LobbyWorld lw) {
		setLastLocationForWorld(p, lw, p.getLocation());
	}

	protected void setLastLocationForWorld(Player p, World w, Location newLoc) {
		LobbyWorld lw = LobbyAPI.getLobbyWorld(w);
		if (lw != null)
			if (lw.shouldWorldShouldSavePlayerLocation())
				setLastLocationForWorld(p, lw, newLoc);
	}

	protected void setLastLocationForWorld(Player p, LobbyWorld lw, Location newLoc) {
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
		Inventory ender = Bukkit.createInventory(null, p.getEnderChest().getSize(), enderchest_title);

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
		LobbyWorld lw = LobbyAPI.getLobbyWorld(w);
		if (lw == null) {
			getServer().getConsoleSender().sendMessage(prefix + " You have no registered world called \"" + w.getName()
					+ "\"! Inventories for this world will not be saved.");
			return;
		}
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
		config.set(p.getName() + "." + world2 + ".allowflight", p.getAllowFlight());
		config.set(p.getName() + "." + world2 + ".compasslocation", p.getCompassTarget());
		config.set(p.getName() + "." + world2 + ".fireticks", p.getFireTicks());
		try {
			try {
				config.set(p.getName() + "." + world2 + ".bedspawn", p.getBedSpawnLocation());
			} catch (Error | Exception e4) {
			}
			config.set(p.getName() + "." + world2 + ".exhaustion", p.getExhaustion());
			config.set(p.getName() + "." + world2 + ".saturation", p.getSaturation());
			config.set(p.getName() + "." + world2 + ".air", p.getRemainingAir());
		} catch (Error | Exception e4) {
		}
		ItemStack[] is = p.getInventory().getContents();
		config.set(p.getName() + "." + world2 + ".i", null);
		for (int itemIndex = 0; itemIndex < 36; itemIndex++) {
			if (is[itemIndex] != null) {
				if (is[itemIndex].hasItemMeta()) {
					config.set(p.getName() + "." + world2 + ".i." + itemIndex, is[itemIndex]);
				} else {
					String saveStuff = "";
					saveStuff += (is[itemIndex].getType().name());
					if (is[itemIndex].getDurability() > 0) {
						saveStuff += ("," + is[itemIndex].getAmount() + "," + is[itemIndex].getDurability());
					} else if (is[itemIndex].getAmount() > 1) {
						saveStuff += ("," + is[itemIndex].getAmount());
					}
					config.set(p.getName() + "." + world2 + ".i." + itemIndex, saveStuff);
				}
			}
		}

		try {
			if (p.getInventory().getItemInOffHand() == null
					|| p.getInventory().getItemInOffHand().getType() == Material.AIR) {
				config.set(p.getName() + "." + world2 + ".i.offhand", null);
			} else {
				config.set(p.getName() + "." + world2 + ".i.offhand", p.getInventory().getItemInOffHand());
			}
		} catch (Error | Exception e2) {
		}

		config.set(p.getName() + "." + world2 + ".a." + 1, p.getInventory().getBoots());
		config.set(p.getName() + "." + world2 + ".a." + 2, p.getInventory().getLeggings());
		config.set(p.getName() + "." + world2 + ".a." + 3, p.getInventory().getChestplate());
		config.set(p.getName() + "." + world2 + ".a." + 4, p.getInventory().getHelmet());

		config.set(p.getName() + "." + world2 + ".potions_effects", null);
		for (PotionEffect eff : p.getActivePotionEffects()) {
			config.set(p.getName() + "." + world2 + ".potions_effects." + eff.getType().getName() + ".dur", eff.getDuration());
			config.set(p.getName() + "." + world2 + ".potions_effects." + eff.getType().getName() + ".amo", eff.getAmplifier());
		}

		try {
			config.set(p.getName() + "." + world2 + ".advancements", null);
			Iterator<org.bukkit.advancement.Advancement> it = Bukkit.advancementIterator();
			for (org.bukkit.advancement.Advancement a = it.next(); it.hasNext(); a = it.next()) {
				if (a.getKey().getKey().startsWith("recipes"))
					continue;
				org.bukkit.advancement.AdvancementProgress pro = p.getAdvancementProgress(a);

				if (pro.getRemainingCriteria().size() <= 0) {
					config.set(p.getName() + "." + world2 + ".advancements." + a.getKey().getKey() + ".won", "");
				} else if (pro.getAwardedCriteria().size() <= 0) {
					//Do not save if player has no reward crit
				} else if (pro.getRemainingCriteria().size() > pro.getAwardedCriteria().size()) {
					config.set(p.getName() + "." + world2 + ".advancements." + a.getKey().getKey() + ".awarded", new ArrayList<String>(pro.getAwardedCriteria()));
				} else {
					config.set(p.getName() + "." + world2 + ".advancements." + a.getKey().getKey() + ".remaining", new ArrayList<String>(pro.getRemainingCriteria()));
				}
			}
		} catch (Error | Exception e4) {
		}

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
		//getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
		//@SuppressWarnings({"deprecation", "unchecked"})
		//public void run() {
		if (getConfig().contains("Worlds")) {
			for (final String key : getConfig().getConfigurationSection("Worlds").getKeys(false)) {
				if (getConfig().contains("Worlds." + key + ".respawnWorld")) {
					getConfig().set("Worlds." + key, null);
					saveConfig();
					continue;
				}

				try {
					String name = getConfig().getString("Worlds." + key + ".name");
					String displayname = null;
					if (getConfig().contains("Worlds." + key + ".displayname")) {
						displayname = getConfig().getString("Worlds." + key + ".displayname");
					} else {
						displayname = key;
					}

					/**
					 * Only here to make sure that the loc variable will **always** be removed in
					 * case a user updates
					 */
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
						if ((w = Bukkit.getWorld(name.toLowerCase())) == null) {
							WorldCreator wc = new WorldCreator(name.toLowerCase()).environment(env).seed(
									ConfigHandler.getWorldVariableInt(key, ConfigKeys.CustomAddedWorlds_Seed));
							if (env == Environment.NETHER) {
								wc.generator(Bukkit.getWorlds().get(1).getGenerator());
							}
							if (env == Environment.THE_END) {
								wc.generator(Bukkit.getWorlds().get(2).getGenerator());
							}
							w = Bukkit.createWorld(wc);
						}
						l = new Location(w, x, y, z);
						if (l == null || w == null)
							Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.RED + "WorldInst for '" + name + "' is still null after recreating world. Something may be wrong!");
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


					if (getConfig().contains("Worlds." + key + "." + ConfigKeys.LINKED_NETHER)) {
						String connectname = getConfig().getString("Worlds." + key + "." + ConfigKeys.LINKED_NETHER);
						Environment env = Environment.NETHER;
						World nether = null;
						if ((nether = Bukkit.getWorld(connectname.toLowerCase())) == null) {
							WorldCreator wc = new WorldCreator(connectname.toLowerCase()).environment(env).seed(w.getSeed());
							wc.generator(Bukkit.getWorlds().get(1).getGenerator());
							nether = Bukkit.createWorld(wc);
						}
						lw.setNether(nether);
					}
					if (getConfig().contains("Worlds." + key + "." + ConfigKeys.LINKED_END)) {
						String connectname = getConfig().getString("Worlds." + key + "." + ConfigKeys.LINKED_END);
						Environment env = Environment.THE_END;
						World end = null;
						if ((end = Bukkit.getWorld(connectname.toLowerCase())) == null) {
							WorldCreator wc = new WorldCreator(connectname.toLowerCase()).environment(env).seed(w.getSeed());
							wc.generator(Bukkit.getWorlds().get(2).getGenerator());
							end = Bukkit.createWorld(wc);
						}
						lw.setEnd(end);
					}


					if (getConfig().contains("Worlds." + key + ".weatherstate")) {
						WeatherState ws = WeatherState.getWeatherStateByName(
								getConfig().getString("Worlds." + key + ".weatherstate"));
						lw.setWeatherState(ws);
					}

					lw.setDisplayName(displayname);

					boolean hidden = getConfig().contains("Worlds." + key + ".hidden") && getConfig().getBoolean("Worlds." + key + ".hidden");
					lw.setHidden(hidden);

					int maxplayers = getConfig().contains("Worlds." + key + ".maxPlayers")
							? getConfig().getInt("Worlds." + key + ".maxPlayers")
							: -1;
					lw.setMaxPlayers(maxplayers >= 0, maxplayers);

					boolean locsaving = getConfig().contains("Worlds." + key + ".shouldsavelocation") && getConfig().getBoolean("Worlds." + key + ".shouldsavelocation");
					lw.setWorldShouldSavePlayerLocation(locsaving);

					boolean portal = getConfig().contains("Worlds." + key + ".canuseportals") && getConfig().getBoolean("Worlds." + key + ".canuseportals");
					lw.setPortal(portal);

					if (getConfig().contains("Worlds." + key + ".connectedTo")) {
						getConfig().set("Worlds." + key + ".respawnWorld",
								getConfig().getString("Worlds." + key + ".connectedTo"));
						getConfig().set("Worlds." + key + ".connectedTo", null);
						saveConfig();
					}

					if (ConfigHandler.containsWorldVariable(lw, ConfigKeys.DefaultItems))
						lw.setSpawnItems((List<ItemStack>) ConfigHandler.getWorldVariableObject(lw,
								ConfigKeys.DefaultItems));

					if (ConfigHandler.containsWorldVariable(lw, ConfigKeys.DisableHealthAndHunger))
						lw.setDisableHungerAndHealth(
								ConfigHandler.getWorldVariableBoolean(lw, ConfigKeys.DisableHealthAndHunger));
					if (ConfigHandler.containsWorldVariable(lw, ConfigKeys.DisableVoid))
						lw.setVoidDisable(ConfigHandler.getWorldVariableBoolean(lw, ConfigKeys.DisableVoid));

					boolean isprivate = getConfig().contains("Worlds." + key + ".isprivate") && getConfig().getBoolean("Worlds." + key + ".isprivate");
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
		//	}
		//}, 0L);
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

	public Set<LobbyServer> getBungeeServers() {
		return bungeeServers;
	}

}
