package me.zombie_striker.lobbyapi;

import me.zombie_striker.lobbyapi.LobbyWorld.WeatherState;
import me.zombie_striker.lobbyapi.utils.ConfigHandler;
import me.zombie_striker.lobbyapi.utils.ConfigHandler.ConfigKeys;
import me.zombie_striker.pluginconstructor.PluginConstructorAPI;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class LobbyCommands implements CommandExecutor, TabCompleter {

	private Main m;
	private String prefix;
	private HashMap<String, String> usages = new HashMap<>();

	protected LobbyCommands(Main mm) {
		this.m = mm;
		this.prefix = Main.getPrefix();
		addUsages();
	}

	private void bWorld(List<String> tab, String arg) {
		if ("~".toLowerCase().startsWith(arg.toLowerCase()))
			tab.add("~");
		for (World wo : Bukkit.getServer().getWorlds()) {
			if (LobbyAPI.getLobbyWorld(wo) == null)
				if (wo.getName().toLowerCase().startsWith(arg.toLowerCase()))
					tab.add(wo.getName());
		}
	}

	private void bLW(List<String> tab, String arg) {
		if ("~".toLowerCase().startsWith(arg.toLowerCase()))
			tab.add("~");
		for (LobbyWorld wo : LobbyWorld.getLobbyWorlds()) {
			if (wo.getWorldName().toLowerCase().startsWith(arg.toLowerCase()))
				tab.add(wo.getWorldName());
		}
	}

	private void bLS(List<String> tab, String arg) {
		for (LobbyServer wo : m.bungeeServers) {
			if (wo.getName().toLowerCase().startsWith(arg.toLowerCase()))
				tab.add(wo.getName());
		}
	}

	private LobbyServer gLS(CommandSender sender, String args) {
		LobbyServer wo = null;
		wo = m.getBungeeServer(args);
		if (wo == null) {
			sender.sendMessage(prefix + " This world does not exist!");
			return null;
		}
		return wo;
	}

	private LobbyWorld gLW(CommandSender sender, String args) {
		World wo;
		if (args.equalsIgnoreCase("~")) {
			if (sender instanceof Player) {
				wo = ((Player) sender).getWorld();
			} else {
				sender.sendMessage(prefix + " Only players can use '~' ");
				return null;
			}
		} else {
			wo = m.getServer().getWorld(args);
			if (LobbyAPI.getLobbyWorld(wo) == null) {
				sender.sendMessage(prefix + " This world does not exist!");
				return null;
			}
		}
		return LobbyAPI.getLobbyWorld(wo);
	}

	private boolean b(String arg, String... s) {
		for (String ss : s) {
			if (arg.equalsIgnoreCase(ss))
				return true;
		}
		return false;
	}

	private List<String> c(String arg, String... s) {
		List<String> cc = new ArrayList<String>();
		for (String ss : s)
			if (ss.toLowerCase().startsWith(arg.toLowerCase()))
				cc.add(ss);
		return cc;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (command.getName().equalsIgnoreCase("lobbyAPI")) {
			List<String> tab = new ArrayList<String>();
			if (args.length == 1) {
				for (String key : usages.keySet()) {
					m.startsWith(tab, key, args[0]);
				}
				return tab;
			} else if (args.length > 1) {
				if (b(args[0], "addDecor")) {
					if (args.length == 2) {
						tab.add("1");
						tab.add("2");
						tab.add("3");
					} else if (args.length == 3) {
						c(args[2], "DIRT", "GRASS", "OBSIDIAN", "APPLE", "DIAMOND_SWORD", "GOLDEN_SHOVEL");
					} else if (args.length == 4) {
						tab.add("DisplayName");
					}
				} else if (b(args[0], "removeDecor")) {
					if (args.length == 2)
						for (LobbyDecor d : m.decor)
							tab.add(d.getSlot() + "");
				} else if (b(args[0], "ChangeSpawn", "addJoiningCommand", "ListJoiningCommands", "setDescription",
						"RemoveWorld", "SetMainLobby", "AddDefaultItem", "changeWorldSlot", "RemoveDefaultItem",
						"ListDefaultItems", "goto", "listwhitelist", "addTowhitelist", "removefromwhitelist",
						"togglewhitelist", "generateNetherAndEndFor", "setmaxplayers", "setsavename")) {
					if (args.length == 2) {
						bLW(tab, args[1]);
					}

				} else if (b(args[0], "setsavename")) {
					if (args.length == 2)
						for (LobbyWorld lw : LobbyAPI.getWorlds())
							if (!tab.contains(lw.getSaveName()))
								tab.add(lw.getSaveName());
				} else if (b(args[0], "setDisplayName")) {
					if (args.length == 2) {
						bLW(tab, args[1]);
						bLS(tab, args[1]);
					}
				} else if (args[0].equalsIgnoreCase("setMaterial")) {
					if (args.length == 2) {
						bLW(tab, args[1]);
						bLS(tab, args[1]);
					} else if (args.length == 3) {
						for (Material m : Material.values()) {
							if (m.name().toLowerCase().startsWith(args[2].toLowerCase()))
								tab.add(m.name());
						}
					}
				} else if (args[0].equalsIgnoreCase("setDefaultWeather")) {
					if (args.length == 2) {
						bLW(tab, args[1]);
					} else if (args.length == 3) {
						for (WeatherState ws : WeatherState.values()) {
							if (ws.name().toLowerCase().startsWith(args[2].toLowerCase()))
								tab.add(ws.name());
						}
					}
				} else if (args[0].equalsIgnoreCase("setGameMode")) {
					if (args.length == 2) {
						bLW(tab, args[1]);
					} else if (args.length == 3) {
						if ("~".toLowerCase().startsWith(args[2].toLowerCase()))
							tab.add("~");
						if ("Any".toLowerCase().startsWith(args[2].toLowerCase()))
							tab.add("Any");
						for (GameMode gm : GameMode.values()) {
							if (gm.name().toLowerCase().startsWith(args[2].toLowerCase()))
								tab.add(gm.name());
						}
					}
				} else if (b(args[0], "setcanuseportals", "setLocationSaving", "setdisablehealthandhunger",
						"setvoidlooping", "hideWorld", "showWorld")) {
					if (args.length == 2) {
						bLW(tab, args[1]);
					}
					if (args.length == 3) {
						m.startsWith(tab, "true", args[2]);
						m.startsWith(tab, "false", args[2]);
					}
				} else if (args[0].equalsIgnoreCase("addWorld")) {
					if (args.length == 2) {
						bWorld(tab, args[1]);
					}
					if (args.length == 3) {
						tab.add(LobbyAPI.getOpenSlot(0)+"");
					}

					if (args.length == 4) {
						for (LobbyWorld wo : LobbyWorld.getLobbyWorlds())
							if (!tab.contains(wo.getSaveName()))
								tab.add(wo.getSaveName());
					}
					if (args.length == 5) {
						tab.add(m.random.nextInt(15) + "");
						tab.add("~");
					}

				}
				if (args[0].equalsIgnoreCase("addserver")) {
					if (args.length == 2) {
						tab.add("Server_Name");
					}
					if (args.length == 3) {
						tab.add("Slot");
					}
					if (args.length == 4) {
						tab.add(m.random.nextInt(15) + "");
						tab.add("~");
					}

				} else if (args[0].equalsIgnoreCase("removeServer")) {
					if (args.length == 2) {
						bLS(tab, args[1]);
					}
				}
			}
			return tab;
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

		if (cmd.getName().equalsIgnoreCase("lobbyapi")) {
			if (args.length > 0 && args[0].equalsIgnoreCase("version")) {
				sender.sendMessage(prefix + " Current version :" + ChatColor.GRAY + m.getDescription().getVersion());
				return false;
			}

			if (args.length == 0 || args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")) {
				int page = 0;
				if (args.length > 1) {
					try {
						page = Integer.parseInt(args[1]) - 1;
						if (page < 1)
							page = 1;
					} catch (Exception e) {
					}
				}
				final int msgPerPage = 5;

				sender.sendMessage(
						prefix + " ===== Page: " + (page + 1) + "/ " + ((usages.size() / msgPerPage) + 1) + " =====");
				for (int i = page * msgPerPage; i < (page * msgPerPage) + msgPerPage; i++) {
					if (i >= usages.size())
						break;
					sender.sendMessage(ChatColor.GOLD + "/LobbyAPI " + usages.keySet().toArray()[i]);
					sender.sendMessage(ChatColor.WHITE + usages.get(usages.keySet().toArray()[i]));
				}
				return true;
			}

			// Regaurdless of if they are op, if they are looking for versions,
			// they should see it.
			if (!sender.hasPermission("lobbyapi.commands")) {
				sender.sendMessage(prefix + ChatColor.RED + "You do not have permission to access this command.");
				return false;
			}

			if (args[0].equalsIgnoreCase("bungee")) {
				m.getConfig().set("hasBungee", !m.getConfig().getBoolean("hasBungee"));
				m.saveConfig();
				sender.sendMessage(ChatColor.GOLD + "Bungee for LobbyAPI has been set to " + ChatColor.WHITE
						+ m.getConfig().getBoolean("hasBungee") + "!");

				sender.sendMessage(ChatColor.GOLD + "Reload/Restart server for this to take effect.");

			} else if (args[0].equalsIgnoreCase("addDefaultItem")) {
				if (args.length >= 2) {
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null)
						return false;
					if (sender instanceof Player) {
						Player p = (Player) sender;
						if (p.getItemInHand() != null) {
							List<ItemStack> items = lw.getSpawnItems();
							if (items == null)
								items = new ArrayList<>();
							items.add(p.getItemInHand());
							lw.setSpawnItems(items);
							ConfigHandler.setWorldVariable(lw, ConfigKeys.DefaultItems, items);
							sender.sendMessage(prefix + " Added the item " + (items.size() - 1) + " = "
									+ p.getItemInHand().getType().name() + ":" + p.getItemInHand().getDurability());

						} else {
							sender.sendMessage(prefix + " The item must be in your main hand.");
						}
					} else {
						sender.sendMessage(prefix + " Only players can send this command.");
					}
				} else {
					sender.sendMessage(prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI addDefaultItem [world]");
					sender.sendMessage(prefix
							+ " [world] = '~' for the world you are in or the world's name (it is Case Sensitive)");
				}
			} else if (args[0].equalsIgnoreCase("setmaxplayers")) {
				if (args.length >= 3) {
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null)
						return false;
					int maxamount = -1;
					try {
						maxamount = Integer.parseInt(args[2]);
					} catch (Error | Exception e4) {

					}
					m.getConfig().set("Worlds." + lw.getWorldName() + ".maxPlayers", maxamount);
					lw.setMaxPlayers(maxamount >= 0, maxamount);

					m.saveConfig();
					sender.sendMessage(
							prefix + "Max players " + (maxamount <= 0 ? "disabled" : "set to " + maxamount) + ".");

				} else {
					sender.sendMessage(
							prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI setmaxplayers [world] [max-amount]");
					sender.sendMessage(
							prefix + " [world] = '~' for the world you want to go toe (it is Case Sensitive)");
					sender.sendMessage(prefix + " [max-amount] = The max players for that world (-1 to remove limit).");
				}
			} else if (args[0].equalsIgnoreCase("setSaveName")) {
				if (args.length >= 3) {
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null)
						return false;
					String save = args[2];
					m.getConfig().set("Worlds." + lw.getWorldName() + ".save", save);
					lw.setSaveName(save);

					m.saveConfig();
					sender.sendMessage(prefix + "Save name changed to \"" + save + "\".");
					sender.sendMessage(prefix
							+ "(If you have any worlds connected to this world, like a nether or end, you will need to change the savename for those worlds as well.)");

				} else {
					sender.sendMessage(
							prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI setmaxplayers [world] [max-amount]");
					sender.sendMessage(
							prefix + " [world] = '~' for the world you want to go toe (it is Case Sensitive)");
					sender.sendMessage(prefix + " [save-name] = The the savename that will be used.");
				}
			} else if (args[0].equalsIgnoreCase("goto")) {
				if (args.length >= 2) {
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null)
						return false;
					Player who = null;
					if (args.length >= 3) {
						who = Bukkit.getPlayer(args[2]);
					} else {
						who = (Player) sender;
					}
					if (who != null) {
						who.teleport(lw.getSpawn());
						sender.sendMessage(prefix + " Teleporting to " + lw.getWorldName() + "'s spawn...");
					} else {
						sender.sendMessage(prefix + " Please provide a valid username.");
					}
				} else {
					sender.sendMessage(prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI goto [world] [player]");
					sender.sendMessage(
							prefix + " [world] = '~' for the world you want to go toe (it is Case Sensitive)");
					sender.sendMessage(prefix
							+ " [player] = [OPTIONAL]: The name of the player to teleport (if not specified, sender will be teleported)");
				}
			} else if (args[0].equalsIgnoreCase("addDecor")) {
				if (args.length >= 4) {
					int slot = Integer.parseInt(args[1]);
					Material m = Material.SPONGE;
					short data = 0;
					try {
						m = Material.matchMaterial(args[2]);
					} catch (Exception e) {
						String[] vals = args[2].split(":");
						// m = Material.getMaterial(Integer.parseInt(vals[0]));
						if (vals.length > 1) {
							data = Short.parseShort(vals[1]);
						}
					}
					StringBuilder display = new StringBuilder();
					for (int i = 3; i < args.length; i++)
						display.append(args[i] + (i < args.length ? " " : ""));

					for (LobbyWorld lw : LobbyWorld.getLobbyWorlds())
						if (lw.getSlot() == slot) {
							sender.sendMessage(prefix + " There is already a world at slot " + slot + "!");
							return true;
						}
					for (LobbyDecor decor : this.m.decor)
						if (decor.getSlot() == slot) {
							sender.sendMessage(prefix + " There is already a decor object at slot " + slot + "!");
							return true;
						}

					LobbyDecor decor = new LobbyDecor(slot, slot + "",
							ChatColor.translateAlternateColorCodes('&', display.toString()));
					decor.setMaterial(m);
					decor.setData(data);
					this.m.getConfig().set("Decor." + decor.getSaveName() + ".displayname", decor.getDisplayname());
					this.m.getConfig().set("Decor." + decor.getSaveName() + ".material", decor.getMaterial().name());
					this.m.getConfig().set("Decor." + decor.getSaveName() + ".durib", decor.getData());
					this.m.getConfig().set("Decor." + decor.getSaveName() + ".slot", decor.getSlot());
					this.m.getConfig().set("Decor." + decor.getSaveName() + ".lore", decor.getLore());
					this.m.saveConfig();
					sender.sendMessage(prefix + " Added decor item at slot " + slot + "!");
					this.m.decor.add(decor);
				} else {
					sender.sendMessage(prefix + " Usage:" + ChatColor.BOLD
							+ " /LobbyAPI addDecor [slot] [material] [display name] ");
					sender.sendMessage(prefix + " [slot] = the new slot for the decor object");
					sender.sendMessage(prefix + " [material] = the Material for the decor object");
					sender.sendMessage(prefix + " [display name] = the displayname");
				}

			} else if (args[0].equalsIgnoreCase("removeDecor")) {
				if (args.length >= 2) {
					int slot = Integer.parseInt(args[1]);
					LobbyDecor d = null;
					for (LobbyDecor decor : m.decor)
						if (decor.getSlot() == slot) {
							this.m.getConfig().set("Decor." + decor.getSaveName(), null);
							this.m.saveConfig();
							sender.sendMessage(prefix + " Removed decor item at slot " + slot + "!");
							d = decor;
							break;
						}
					if (d != null) {
						m.decor.remove(d);
						return true;
					}
					sender.sendMessage(prefix + " There is no decor item at slot " + slot + "!");
				} else {
					sender.sendMessage(prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI removeDecor [slot]");
					sender.sendMessage(prefix + " [slot] = the slot for the decor object you want to remove");
				}
			} else if (args[0].equalsIgnoreCase("changeWorldSlot")) {
				if (args.length >= 3) {
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null)
						return false;
					int index = 0;
					try {
						index = Integer.parseInt(args[2]);
					} catch (Exception e) {
						sender.sendMessage(prefix + " The index must be a number");
						return true;
					}
					lw.setSlot(index);

					m.getConfig().set("Worlds." + lw.getWorldName() + ".i", index);
					m.saveConfig();
					sender.sendMessage(prefix + "The slot for the world has been changed to " + index);
				} else {
					sender.sendMessage(
							prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI changeWorldSlot [world] [slot]");
					sender.sendMessage(prefix
							+ " [world] = '~' for the world you are in or the world's name (it is Case Sensitive)");
					sender.sendMessage(prefix + " [slot] = the new slot for the world");
				}

			} else if (args[0].equalsIgnoreCase("toggleWhitelist")) {
				if (args.length >= 3) {
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null)
						return false;
					boolean isPrivate = Boolean.parseBoolean(args[2]);
					lw.setIsPrivate(isPrivate);

					m.getConfig().set("Worlds." + lw.getWorldName() + ".isprivate", isPrivate);
					m.saveConfig();
					sender.sendMessage(prefix + "The world " + lw.getWorldName() + " is now "
							+ (isPrivate ? "private" : "public"));
				} else {
					sender.sendMessage(
							prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI toggleWhitelist [world] [true/false]");
					sender.sendMessage(prefix
							+ " [world] = '~' for the world you are in or the world's name (it is Case Sensitive)");
					sender.sendMessage(prefix + " [true/false] whether the world is private");
				}

			} else if (args[0].equalsIgnoreCase("listWhitelist")) {
				if (args.length >= 2) {
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null)
						return false;
					sender.sendMessage(prefix + " All players that are allowed for this world");
					boolean isMaxed = lw.getWhitelistedPlayersUUID().size() > 18;
					if (isMaxed) {
						StringBuilder names = new StringBuilder();
						for (UUID uuid : lw.getWhitelistedPlayersUUID()) {
							names.append(Bukkit.getOfflinePlayer(uuid).getName() + ", ");
							if (names.length() > 320) {
								names.append("....(and more)");
								break;
							}
						}
						sender.sendMessage(names.toString());
					} else {
						for (UUID uuid : lw.getWhitelistedPlayersUUID()) {
							sender.sendMessage("-" + Bukkit.getOfflinePlayer(uuid).getName());
						}
					}
				} else {
					sender.sendMessage(prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI listWhitelist [world]");
					sender.sendMessage(prefix
							+ " [world] = '~' for the world you are in or the world's name (it is Case Sensitive)");
				}

			} else if (args[0].equalsIgnoreCase("addToWhitelist")) {
				if (args.length >= 2) {
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null)
						return false;
					String name = null;
					if (args.length >= 3) {
						name = args[2];
						if (name.equalsIgnoreCase("~"))
							name = sender.getName();
					} else {
						name = sender.getName();
					}
					OfflinePlayer player = Bukkit.getOfflinePlayer(name);
					if (!player.hasPlayedBefore()) {
						sender.sendMessage(
								prefix + " The player must have joined before to add them to the whitelist.");
						return true;
					} else {
						lw.addWhitelistedPlayer(player);
						m.getConfig().set("Worlds." + lw.getWorldName() + ".whitelistedUUIDS", lw.whitelistToString());
						m.saveConfig();
						sender.sendMessage(prefix + "Added player " + name + " to whitelist");
					}
				} else {
					sender.sendMessage(
							prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI addToWhitelist [world] [player]");
					sender.sendMessage(prefix
							+ " [world] = '~' for the world you are in or the world's name (it is Case Sensitive)");
					sender.sendMessage(prefix + " [player] = The player that will be added. '~' for yourself");
				}
			} else if (args[0].equalsIgnoreCase("removeFromWhitelist")) {
				if (args.length >= 2) {
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null)
						return false;
					String name = null;
					if (args.length >= 3) {
						name = args[2];
						if (name.equalsIgnoreCase("~"))
							name = sender.getName();
					} else {
						name = sender.getName();
					}
					OfflinePlayer player = Bukkit.getOfflinePlayer(name);
					if (!player.hasPlayedBefore()) {
						sender.sendMessage(
								prefix + " The player must have joined before to remove them from the whitelist.");
						return true;
					} else {
						lw.removeWhitelistedPlayer(player);
						m.getConfig().set("Worlds." + lw.getWorldName() + ".whitelistedUUIDS", lw.whitelistToString());
						m.saveConfig();
						sender.sendMessage(prefix + "Removed player " + name + " from whitelist");
					}
				} else {
					sender.sendMessage(
							prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI removefromwhitelist [world] [player]");
					sender.sendMessage(prefix
							+ " [world] = '~' for the world you are in or the world's name (it is Case Sensitive)");
					sender.sendMessage(prefix + " [player] = The player that will be removed. '~' for yourself");
				}

			} else if (args[0].equalsIgnoreCase("removeDefaultItem")) {
				if (args.length >= 3) {
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null)
						return false;
					int index = 0;
					try {
						index = Integer.parseInt(args[2]);
					} catch (Exception e) {
						sender.sendMessage(prefix + " The index must be a number");
						return true;
					}
					if (lw.getSpawnItems() == null) {
						sender.sendMessage(prefix + " There are no default items for this world.");
						return true;
					}
					List<ItemStack> items = lw.getSpawnItems();
					sender.sendMessage(prefix + " Removed the item " + index + " = "
							+ lw.getSpawnItems().get(index).getType().name() + ":"
							+ lw.getSpawnItems().get(index).getDurability());
					items.remove(index);
					lw.setSpawnItems(items);
					ConfigHandler.setWorldVariable(lw, ConfigKeys.DefaultItems, items);
				} else {
					sender.sendMessage(prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI addDefaultItem [world]");
					sender.sendMessage(prefix
							+ " [world] = '~' for the world you are in or the world's name (it is Case Sensitive)");
					sender.sendMessage(prefix + " [index] = the index of the item you wish to remove");
				}

			} else if (args[0].equalsIgnoreCase("listDefaultItems")) {
				if (args.length >= 2) {
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null)
						return false;
					if (lw.getSpawnItems() == null) {
						sender.sendMessage(prefix + " There are no default items for this world.");
						return true;
					}
					for (int i = 0; i < lw.getSpawnItems().size(); i++) {
						sender.sendMessage(i + "- " + lw.getSpawnItems().get(i).getType().name() + ":"
								+ lw.getSpawnItems().get(i).getDurability());
					}
				} else {
					sender.sendMessage(prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI addDefaultItem [world]");
					sender.sendMessage(prefix
							+ " [world] = '~' for the world you are in or the world's name (it is Case Sensitive)");
				}

			} else if (args[0].equalsIgnoreCase("setMainLobby")) {
				if (args.length >= 2) {
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null)
						return false;
					if (LobbyWorld.getMainLobby() != null) {
						if (m.getConfig().getString("Worlds." + LobbyWorld.getMainLobby().getWorldName() + ".name")
								.equalsIgnoreCase(LobbyWorld.getMainLobby().getWorldName())) {
							m.getConfig().set("Worlds." + LobbyWorld.getMainLobby().getWorldName() + ".isMainLobby",
									false);
							m.saveConfig();
						}
					}
					if (m.getConfig().contains("Worlds." + lw.getWorldName() + ".name") && m.getConfig()
							.getString("Worlds." + lw.getWorldName() + ".name").equalsIgnoreCase(lw.getWorldName())) {
						m.getConfig().set("Worlds." + lw.getWorldName() + ".isMainLobby", true);
						m.saveConfig();
					}
					lw.setAsMainLobby();
					sender.sendMessage(prefix + "Set world \"" + lw.getWorldName() + "\" to be the main lobby.");
				} else {
					sender.sendMessage(prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI seMaintLobby [name]");
					sender.sendMessage(prefix
							+ " [name] = '~' for the world you are in or the world's name (it is Case Sensitive)");
				}
			} else if (args[0].equalsIgnoreCase("removeMainLobby")) {
				if (args.length >= 1) {
					LobbyWorld lw = LobbyWorld.getMainLobby();
					if (lw == null) {
						sender.sendMessage(prefix + " There is no main lobby!");
						return true;
					}
					if (m.getConfig().getString("Worlds." + LobbyWorld.getMainLobby().getWorldName() + ".name")
							.equalsIgnoreCase(lw.getWorldName())) {
						m.getConfig().set("Worlds." + LobbyWorld.getMainLobby().getWorldName() + ".isMainLobby", false);
						m.saveConfig();
						LobbyWorld.removeMainLobby();
					}
					sender.sendMessage(prefix + "Removed the main lobby.");
				} else {
					sender.sendMessage(prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI removeMainLobby [name]");
					sender.sendMessage(prefix + " [name] = The world's name (it is Case Sensitive)");
				}
			} else if (args[0].equalsIgnoreCase("setDescription")) {
				if (args.length >= 2) {
					if (gLS(sender, args[1]) != null) {

						LobbyServer lw = gLS(sender, args[1]);
						if (lw == null)
							return false;
						StringBuilder sb = new StringBuilder();
						for (int i = 2; i < args.length; i++) {
							sb.append(args[i] + " ");
						}
						List<String> g = new ArrayList<String>();
						g.add(ChatColor.translateAlternateColorCodes('&', sb.toString()));
						lw.setLore(g);
						m.getConfig().set("Server." + lw.getName() + ".lore", g);
						m.saveConfig();
						sender.sendMessage(prefix + "Changed server description for server \"" + lw.getName() + "\" to "
								+ sb.toString() + ".");
					} else {
						LobbyWorld lw = gLW(sender, args[1]);
						if (lw == null)
							return false;
						StringBuilder sb = new StringBuilder();
						for (int i = 2; i < args.length; i++) {
							sb.append(args[i] + " ");
						}
						List<String> g = new ArrayList<String>();
						g.add(ChatColor.translateAlternateColorCodes('&', sb.toString()));
						lw.setDescription(g);
						m.getConfig().set("Worlds." + lw.getWorldName() + ".desc", g);
						m.saveConfig();
						sender.sendMessage(prefix + "Changed world description for world \"" + lw.getWorldName()
								+ "\" to " + sb.toString() + ".");
					}
				} else {
					sender.sendMessage(
							prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI setDescription [world] [description...]");
					sender.sendMessage(prefix + " [world] = '~' or the world's name (it is Case Sensitive)");
					sender.sendMessage(prefix + " [description] = The desctiption of the world.");
				}

			} else if (args[0].equalsIgnoreCase("addworld")) {
				if (args.length >= 6) {
					World wo;
					if (args[1].equalsIgnoreCase("~")) {
						if (sender instanceof Player) {
							wo = ((Player) sender).getWorld();
						} else {
							sender.sendMessage(prefix + " Only players can use '~' ");
							return true;
						}
					} else {
						String name = args[1].toLowerCase();
						wo = m.getServer().getWorld(name);
						if (wo == null) {
							sender.sendMessage(prefix
									+ " This world does not exist. Creating world using LobbyAPI WorldGenerator");
							WorldCreator wc = new WorldCreator(name);
							if (args.length >= 6) {
								wc.seed(Long.parseLong(args[5]));
							}

							wo = Bukkit.createWorld(wc);
							ConfigHandler.setWorldVariable(wo.getName(), ConfigKeys.CustomAddedWorlds_Seed,
									wo.getSeed());
						}
					}
					if (LobbyAPI.getLobbyWorld(wo) != null) {
						sender.sendMessage(prefix + " This world has already been registered!");
						return false;
					}
					int i = LobbyAPI.getOpenSlot(Integer.parseInt(args[2]));
					String savename = wo.getName();
					if (args.length >= 4)
						savename = args[3];
					boolean genNether = false;
					if (args.length >= 5)
						genNether = Boolean.parseBoolean(args[4]);


					if (wo != null) {
						LobbyWorld lw = LobbyAPI.registerWorldFromConfig(wo, wo.getSpawnLocation(), savename, wo.getName(), 0, i,
								GameMode.SURVIVAL, false);
						String fi = wo.getName();
						saveWorld(fi, savename, wo, wo.getSpawnLocation(), 0, i, false);
						sender.sendMessage(prefix + "Added world \"" + wo.getName() + "\" with a slot of " + i);

						if (genNether) {
							sender.sendMessage(prefix
									+ "The nether and end world will be registered and linked to this world if they have not been registered already.");
							generateConnctingWorlds(lw);
						}
						m.loadLocalWorlds();
					}
				} else {
					sender.sendMessage(prefix + " Usage:" + ChatColor.BOLD
							+ " /LobbyAPI addword [name] [slot] [savename] [shouldGenerateNether] [seed] ");
					sender.sendMessage(prefix + " [name] = The world's name (it is Case Sensitive)");
					sender.sendMessage(prefix + " [slot] = The slot in the menu for the world");
					sender.sendMessage(prefix
							+ " [savename] = OPTIONAL: The name of inventory save (only useful if you wish for multiple worlds to have the same inventory)");
					sender.sendMessage(prefix
							+ " [shouldGenerateNether] = OPTIONAL: True or false: Whether LobbyAPI should allow that world to have a nether");
					sender.sendMessage(prefix + " [seed] = OPTIONAL: The seed for the world");
				}

			} else if (args[0].equalsIgnoreCase("removeWorldSelector")) {
				m.setWorldSelector(null);
				sender.sendMessage(prefix + "World selector removed.");
				ConfigHandler.setLobbyAPIVariable(ConfigHandler.ConfigKeys.WorldSelector, null);
			} else if (args.length > 0 && args[0].equalsIgnoreCase("setWorldSelector")) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					if (p.getItemInHand() != null) {
						m.setWorldSelector(p.getItemInHand());
						ConfigHandler.setLobbyAPIVariable(ConfigHandler.ConfigKeys.WorldSelector, p.getItemInHand());
						sender.sendMessage(prefix + " World seelector set to item in player's hand");
					} else {
						sender.sendMessage(prefix
								+ " You need to have an item in your hands in order to set it as a world selector.");
					}
				}
			} else if (args[0].equalsIgnoreCase("generateNetherAndEndFor")) {
				if (args.length >= 2) {
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null)
						return false;
					generateConnctingWorlds(lw);
					sender.sendMessage(prefix + " Generated worlds for " + lw.getWorldName() + ".");
				} else {
					sender.sendMessage(
							prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI generateNetherAndEndFor [name] ");
					sender.sendMessage(prefix + " [name] = The world's name (it is Case Sensitive)");
				}
			} else if (args[0].equalsIgnoreCase("setLocationSaving")) {
				if (args.length >= 3) {
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null)
						return false;
					Boolean b = Boolean.parseBoolean(args[2]);
					lw.setWorldShouldSavePlayerLocation(b);
					ConfigHandler.setWorldVariable(lw, ConfigHandler.ConfigKeys.ShouldBeSavingLocation, b);
					sender.sendMessage(prefix + " Set location saving to " + b);
				} else {
					sender.sendMessage(prefix + " Usage:" + ChatColor.BOLD
							+ " /LobbyAPI setLocationSaving [name] [True or false]");
					sender.sendMessage(prefix + " [name] = The world's name (it is Case Sensitive)");
					sender.sendMessage(prefix
							+ " [True or false] = If the world should save the locations of the players when they log off.");
				}
			} else if (args[0].equalsIgnoreCase("setMaterial")) {
				if (args.length >= 3) {
					if (m.getBungeeServer(args[1]) != null) {
						LobbyServer lw = gLS(sender, args[1]);
						if (lw == null)
							return false;
						Material change = Material.IRON_BLOCK;
						try {
							change = Material.matchMaterial(args[2]);
							// change = Material.getMaterial(Integer.parseInt(args[2]));
						} catch (Exception e) {
							// change = Material.matchMaterial(args[2]);
						}
						if (change == null || change == Material.AIR) {
							sender.sendMessage(prefix
									+ " You need to provide a valid material. Either use the Material Enum name");
							return true;
						}

						lw.setMaterial(change);
						// lw.setColor((short) 0);
						m.getConfig().set("Server." + lw.getName() + ".material", change.toString());
						// m.getConfig().set("Worlds." + lw.getWorldName() + ".color", 0);
						m.saveConfig();
						sender.sendMessage(prefix + "Changed material for server \"" + lw.getName() + "\" to "
								+ change.toString() + ".");

					} else {
						LobbyWorld lw = gLW(sender, args[1]);
						if (lw == null)
							return false;
						Material change = Material.IRON_BLOCK;
						try {
							change = Material.matchMaterial(args[2]);
							// change = Material.getMaterial(Integer.parseInt(args[2]));
						} catch (Exception e) {
							// change = Material.matchMaterial(args[2]);
						}
						if (change == null || change == Material.AIR) {
							sender.sendMessage(prefix
									+ " You need to provide a valid material. Either use the Material Enum name");
							return true;
						}

						lw.setMaterial(change);
						// lw.setColor((short) 0);
						m.getConfig().set("Worlds." + lw.getWorldName() + ".material", change.toString());
						// m.getConfig().set("Worlds." + lw.getWorldName() + ".color", 0);
						m.saveConfig();
						sender.sendMessage(prefix + "Changed material for \"" + lw.getWorldName() + "\" to "
								+ change.toString() + ".");
					}

				} else {
					sender.sendMessage(
							prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI setMaterial [name] [material]");
					sender.sendMessage(prefix + " [name] = The world's name (it is Case Sensitive)");
					sender.sendMessage(prefix + " [material] = The new material of the icon");
				}

			} else if (args[0].equalsIgnoreCase("setDisplayName")) {
				if (args.length >= 3) {
					if (gLS(sender, args[1]) != null) {
						LobbyServer lw = gLS(sender, args[1]);
						if (lw == null) {
							sender.sendMessage("The world has not been registeed. Use /lobbyapi addWorld");
							return false;
						}
						StringBuilder sb = new StringBuilder();
						for (int i = 2; i < args.length; i++) {
							sb.append(args[i]);
							if (i != args.length - 1)
								sb.append(" ");
						}
						lw.setDisplayName(ChatColor.translateAlternateColorCodes('&', sb.toString()));
						sender.sendMessage(prefix + " Changing the worlds displayname to \"" + sb.toString() + "\"");
						m.getConfig().set("Server." + lw.getName() + ".displayname", lw.getDisplayName());
						m.saveConfig();
					} else {
						LobbyWorld lw = gLW(sender, args[1]);
						if (lw == null) {
							sender.sendMessage("The world has not been registeed. Use /lobbyapi addWorld");
							return false;
						}
						StringBuilder sb = new StringBuilder();
						for (int i = 2; i < args.length; i++) {
							sb.append(args[i]);
							if (i != args.length - 1)
								sb.append(" ");
						}
						lw.setDisplayName(ChatColor.translateAlternateColorCodes('&', sb.toString()));
						sender.sendMessage(prefix + " Changing the worlds displayname to \"" + sb.toString() + "\"");
						m.getConfig().set("Worlds." + lw.getWorldName() + ".displayname", lw.getDisplayName());
						m.saveConfig();
					}
				} else {
					sender.sendMessage(prefix + " Usage: /lobbyAPI setDisplayName [World] [Displayname]");

				}
			} else if (args[0].equalsIgnoreCase("addJoiningCommand")) {
				if (args.length >= 3) {
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null) {
						sender.sendMessage("The world has not been registeed. Use /lobbyapi addWorld");
						return false;
					}
					StringBuilder sb = new StringBuilder();
					for (int i = 2; i < args.length; i++) {
						sb.append(args[i]);
						if (i != args.length - 1)
							sb.append(" ");
					}
					lw.addCommand(sb.toString());
					sender.sendMessage(prefix + " Adding command " + (lw.getCommandsOnJoin().size() - 1) + " ("
							+ sb.toString() + ")");
					m.getConfig().set("Worlds." + lw.getWorldName() + ".joincommands", lw.getCommandsOnJoin());
					m.saveConfig();
				} else {
					sender.sendMessage(
							prefix + " Usage: /lobbyAPI addJoiningCommand [World] [The command [can include spaces]]");
					sender.sendMessage(prefix + " Use %player% to get the player's name.");
				}
			} else if (args[0].equalsIgnoreCase("removeJoiningCommand")) {
				if (args.length >= 3) {
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null)
						return false;
					int i = 0;
					try {
						i = Integer.parseInt(args[2]);
					} catch (Exception e) {
						sender.sendMessage(prefix + " You must provide the index of the command you want to remove");
						return true;
					}
					String command = lw.getCommandsOnJoin().get(i);
					lw.getCommandsOnJoin().remove(i);
					sender.sendMessage(prefix + " Removing command " + i + " (" + command + ")");
					m.getConfig().set("Worlds." + lw.getWorldName() + ".joincommands", lw.getCommandsOnJoin());
					m.saveConfig();
				}
			} else if (args[0].equalsIgnoreCase("ListJoiningCommands")) {
				if (args.length >= 2) {
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null)
						return false;

					sender.sendMessage(prefix + " Listing joining commands for " + lw.getWorldName());
					int i = 0;
					for (String command : lw.getCommandsOnJoin()) {
						sender.sendMessage("-" + i + " = " + command);
						i++;
					}
				}

			} else if (args[0].equalsIgnoreCase("setGameMode")) {
				if (args.length >= 3) {
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null)
						return false;
					GameMode change = GameMode.SURVIVAL;
					String name = null;
					String save = null;
					if (args[2].equalsIgnoreCase("Any")) {
						change = null;
						name = "Any";
						save = null;
					} else {
						try {
							change = GameMode.valueOf(args[2].toUpperCase());
						} catch (Exception e) {
							try {
								change = GameMode.getByValue(Integer.parseInt(args[2]));
							} catch (Exception e2) {
								e.printStackTrace();
							}
						}

						name = change.name();
						save = change.toString();
					}

					lw.setGameMode(change);
					m.getConfig().set("Worlds." + lw.getWorldName() + ".gamemode", save);
					m.saveConfig();
					sender.sendMessage(prefix + "Changed gamemode for \"" + lw.getWorldName() + "\" to " + name + ".");
				} else {
					sender.sendMessage(
							prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI setGameMode [name] [gamemode]");
					sender.sendMessage(prefix + " [name] = The world's name (it is Case Sensitive)");
					sender.sendMessage(prefix + " [gamemode] = The new default gamemode");
				}
			} else if (args[0].equalsIgnoreCase("changespawn")) {

				if (args.length >= 5) {
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null)
						return false;
					double x = 0;
					double y = 100;
					double z = 0;
					if (args[2].contains("~"))
						x = ((Player) sender).getLocation().getX();
					else
						x = Double.parseDouble(args[2]);

					if (args[3].contains("~"))
						y = ((Player) sender).getLocation().getY();
					else
						y = Double.parseDouble(args[3]);

					if (args[4].contains("~"))
						z = ((Player) sender).getLocation().getZ();
					else
						z = Double.parseDouble(args[4]);

					Location l = new Location(lw.getWorld(), x, y, z);
					if (sender instanceof Player) {
						l.setYaw(((Player) sender).getLocation().getYaw());
						l.setPitch(((Player) sender).getLocation().getPitch());
					}
					lw.setSpawn(l);
					m.getConfig().set("Worlds." + lw.getWorldName() + ".spawnLoc.x", l.getX());
					m.getConfig().set("Worlds." + lw.getWorldName() + ".spawnLoc.y", l.getY());
					m.getConfig().set("Worlds." + lw.getWorldName() + ".spawnLoc.z", l.getZ());
					m.getConfig().set("Worlds." + lw.getWorldName() + ".spawnLoc.yaw", l.getYaw());
					m.getConfig().set("Worlds." + lw.getWorldName() + ".spawnLoc.pitch", l.getPitch());
					m.saveConfig();
					sender.sendMessage(prefix + "Changed spawn for world \"" + lw.getWorldName() + "\" to " + x + " "
							+ y + " " + z + ".");
				} else {
					sender.sendMessage(
							prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI changeSpawn [name] [x] [y] [z]");
					sender.sendMessage(prefix + " [name] = The world's name (it is Case Sensitive)");
					sender.sendMessage(prefix + " [x] = The world's spawn's X location");
					sender.sendMessage(prefix + " [y] = The world's spawn's Y location");
					sender.sendMessage(prefix + " [z] = The world's spawn's z location");
				}
			} else if (args[0].equalsIgnoreCase("removeworld")) {
				if (args.length >= 2) {
					World wo;
					if (args[1].equalsIgnoreCase("~")) {
						if (sender instanceof Player) {
							wo = ((Player) sender).getWorld();
						} else {
							sender.sendMessage(prefix + " Only players can use '~' ");
							return true;
						}
					} else {
						wo = m.getServer().getWorld(args[1]);
					}
					if (wo != null) {
						if (ConfigHandler.getWorlds() == null) {
							sender.sendMessage(prefix + "There are no worlds registered!");
							return true;
						}
						if (ConfigHandler.getWorlds().contains(wo.getName()) && wo.getPlayers().size() > 0) {
							sender.sendMessage(prefix + "There are still " + wo.getPlayers().size()
									+ " players in that world! There must not be any players in the world when you remove it.");
							return true;
						}

						m.getConfig().set("Worlds." + LobbyAPI.getLobbyWorld(wo).getWorldName(), null);
						m.saveConfig();
						LobbyAPI.unregisterWorld(wo);
						sender.sendMessage(prefix + "Removed world \"" + wo.getName() + "\"");
						m.loadLocalWorlds();
						// ConfigHandler.setWorldVariable(wo.getName(),
						// ConfigKeys.CustomAddedWorlds_Seed, null);
						Bukkit.unloadWorld(wo, true);

					} else {
						sender.sendMessage(prefix + "Could not remove world: World does not exist");
					}
				} else {
					sender.sendMessage(prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI removeworld [name]");
					sender.sendMessage(prefix + " [name] = The world's name (it is Case Sensitive)");
				}
			} else if (args[0].equalsIgnoreCase("setCanUsePortals")) {
				if (args.length >= 3) {
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null)
						return false;
					boolean b = Boolean.parseBoolean(args[2]);
					lw.setPortal(b);
					ConfigHandler.setWorldVariable(lw, ConfigKeys.CanUsePortals, b);
					sender.sendMessage(
							prefix + "Set the CanUsePortals for world \"" + lw.getWorldName() + "\" to " + b + "");
				} else {
					sender.sendMessage(
							prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI setCanUsePortals [name] [true/false]");
					sender.sendMessage(prefix + " [name] = The world's name (it is Case Sensitive)");
					sender.sendMessage(
							prefix + " [true/false] If the world should allow portals (needed for MV-Portals)");
				}

			} else if (args[0].equalsIgnoreCase("setdisablehealthandhunger")) {
				if (args.length >= 3) {
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null)
						return false;
					boolean b = Boolean.parseBoolean(args[2]);
					lw.setDisableHungerAndHealth(b);
					ConfigHandler.setWorldVariable(lw, ConfigKeys.DisableHealthAndHunger, b);
					sender.sendMessage(prefix + "Set the DisableHealthAndHunger for world \"" + lw.getWorldName()
							+ "\" to " + b + "");
				} else {
					sender.sendMessage(
							prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI setCanUsePortals [name] [true/false]");
					sender.sendMessage(prefix + " [name] = The world's name (it is Case Sensitive)");
					sender.sendMessage(
							prefix + " [true/false] If the world should disable hunger (useful for lobbies)");
				}
			} else if (args[0].equalsIgnoreCase("setvoidlooping")) {
				if (args.length >= 3) {
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null)
						return false;
					boolean b = Boolean.parseBoolean(args[2]);
					lw.setVoidDisable(b);
					ConfigHandler.setWorldVariable(lw, ConfigKeys.DisableVoid, b);
					sender.sendMessage(
							prefix + "Set the DisableVoid for world \"" + lw.getWorldName() + "\" to " + b + "");
				} else {
					sender.sendMessage(
							prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI setCanUsePortals [name] [true/false]");
					sender.sendMessage(prefix + " [name] = The world's name (it is Case Sensitive)");
					sender.sendMessage(prefix
							+ " [true/false] If the world should teleport players back to spawn if they are in the void.");
				}
			} else if (args.length > 0 && args[0].equalsIgnoreCase("hideworld")) {
				if (args.length >= 2) {
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null)
						return false;
					lw.setHidden(true);
					ConfigHandler.setWorldVariable(lw, ConfigKeys.isHidden, true);
					sender.sendMessage(prefix + "Hidding world \"" + lw.getWorldName());
				} else {
					sender.sendMessage(
							prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI hideworld [name] [true/false]");
					sender.sendMessage(prefix + " [name] = The world's name (it is Case Sensitive)");
					sender.sendMessage(prefix
							+ " [true/false] If the world should teleport players back to spawn if they are in the void.");
				}
			} else if (args.length > 0 && args[0].equalsIgnoreCase("showworld")) {
				if (args.length >= 2) {
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null)
						return false;
					lw.setHidden(false);
					ConfigHandler.setWorldVariable(lw, ConfigKeys.isHidden, false);
					sender.sendMessage(prefix + "Showing world \"" + lw.getWorldName());
				} else {
					sender.sendMessage(
							prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI showworld [name] [true/false]");
					sender.sendMessage(prefix + " [name] = The world's name (it is Case Sensitive)");
					sender.sendMessage(prefix
							+ " [true/false] If the world should teleport players back to spawn if they are in the void.");
				}
			} else if (args.length > 0 && args[0].equalsIgnoreCase("setDefaultWeather")) {
				if (args.length >= 3) {
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null)
						return false;
					WeatherState ws = WeatherState.getWeatherStateByName(args[2]);
					if (ws == null) {
						sender.sendMessage(
								prefix + "The weather state is invalid. Choose either NORMAL, ALWAYS_RAIN, or NO_RAIN");
						return false;
					}

					lw.setWeatherState(ws);
					ConfigHandler.setWorldVariable(lw, ConfigKeys.Weather, ws.name());
					sender.sendMessage(prefix + "Set the default weather for world \"" + lw.getWorldName() + "\" to "
							+ ws.name() + "");
				} else {
					sender.sendMessage(
							prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI setDefaultWeather [name] [weather]");
					sender.sendMessage(prefix + " [name] = The world's name (it is Case Sensitive)");
					sender.sendMessage(prefix + " [weather] = The world's weather (NO_RAIN, ALWAYS_RAIN, NORMAL) ");
				}
			} else if (args.length > 0 && args[0].equalsIgnoreCase("addserver")) {
				if (args.length >= 3) {
					String server = args[1];
					if (LobbyAPI.getServer(server) != null) {
						sender.sendMessage(prefix + " This server has already been registered!");
						return false;
					}
					int i = LobbyAPI.getOpenSlot(Integer.parseInt(args[2]));
					int color = 1;
					if (args.length >= 4 && (!args[3].equalsIgnoreCase("~"))) {
						color = Integer.parseInt(args[6]) % 15;
					} else {
						color = 0;// m.random.nextInt(15);
					}
					LobbyAPI.registerBungeeServerFromConfig(server, i, color);
					if (m.getConfig().contains("Server." + server)) {
						sender.sendMessage(prefix
								+ " The config already has registered this server, even though LobbyAPI has not. This should not happen, but if it did, report this to Zombie_Striker on the bukkitdev page: https://dev.bukkit.org/projects/lobbyapi");
						return false;
					}
					m.getConfig().set("Server." + server + ".name", server);
					m.getConfig().set("Server." + server + ".i", i);
					m.getConfig().set("Server." + server + ".color", color);
					m.getConfig().set("Server." + server + ".material", "GLASS");
					m.getConfig().set("Server." + server + ".displayname", "&a" + server);
					m.getConfig().set("Server." + server + ".lore", new ArrayList<>());
					m.saveConfig();
					sender.sendMessage(prefix + "Added server \"" + server + "\" with a slot of " + i + ".");
					m.loadLocalServers();
				} else {
					sender.sendMessage(
							prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI addserver [name] [slot] [color]");
					sender.sendMessage(prefix + " [name] = The server's name (it is Case Sensitive)");
					sender.sendMessage(prefix + " [slot] = The slot in the menu for the world");
					sender.sendMessage(prefix + " [color] = OPTIONAL: The color of the block.");
				}
			} else if (args.length > 0 && args[0].equalsIgnoreCase("removeserver")) {
				if (args.length >= 2) {
					String server = args[1];
					if (gLS(sender, server) == null) {
						sender.sendMessage(prefix + " Server is not registered");
						return true;
					}
					LobbyAPI.unregisterBungeeServer(server);
					for (String fi : m.getConfig().getConfigurationSection("Server").getKeys(false)) {
						if (m.getConfig().getString("Server." + fi + ".name") != null
								&& m.getConfig().getString("Server." + fi + ".name").equals(server)) {
							m.getConfig().set("Server." + fi, null);
							m.saveConfig();
							break;
						}
					}
					sender.sendMessage(prefix + "Removed server \"" + server + "\"");
					m.loadLocalServers();
				} else {
					sender.sendMessage(prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI removeserver [name]");
					sender.sendMessage(prefix + " [name] = The server's name (it is Case Sensitive)");
				}
			} else if (args.length > 0 && args[0].equalsIgnoreCase("listWorlds")) {
				sender.sendMessage(prefix + " Worlds that have been added via command");
				if (m.getConfig().contains("Worlds"))
					for (String fi : m.getConfig().getConfigurationSection("Worlds").getKeys(false)) {
						if (m.getConfig().getString("Worlds." + fi + ".name") != null) {
							sender.sendMessage(prefix + " Savename=" + fi + ": World="
									+ m.getConfig().getString("Worlds." + fi + ".name"));
						}
					}
			} else if (args.length > 0 && args[0].equalsIgnoreCase("listServers")) {
				sender.sendMessage(prefix + " Servers that have been added via command");
				if (m.getConfig().contains("Server"))
					for (String fi : m.getConfig().getConfigurationSection("Server").getKeys(false)) {
						if (m.getConfig().getString("Server." + fi + ".name") != null) {
							sender.sendMessage(prefix + " " + fi + ": Server "
									+ m.getConfig().getString("Server." + fi + ".name") + ".");
						}
					}
			} else {
				int page = 0;
				if (args.length > 1) {
					try {
						page = Integer.parseInt(args[1]) - 1;
						if (page < 1)
							page = 1;
					} catch (Exception e) {
					}
				}
				final int msgPerPage = 5;

				sender.sendMessage(
						prefix + " ===== Page: " + (page + 1) + "/ " + ((usages.size() / msgPerPage) + 1) + " =====");
				for (int i = page * msgPerPage; i < (page * msgPerPage) + msgPerPage; i++) {
					if (i >= usages.size())
						break;
					sender.sendMessage(ChatColor.GOLD + "/lobbyAPI " + usages.keySet().toArray()[i]);
					sender.sendMessage(ChatColor.WHITE + usages.get(usages.keySet().toArray()[i]));
				}
				return true;
			}
		}

		if (cmd.getName().equalsIgnoreCase("Lobby") || cmd.getName().equalsIgnoreCase("hub")) {

			if (sender instanceof Player) {
				Player player = (Player) sender;
	LobbyAPI.openGUI(player);
			} else {
				sender.sendMessage(prefix + " The sender must be a player to operate this command!");
			}
		}
		return false;
	}

	public void addUsages() {
		usages.put("addWorld", "Adds a world to the list");
		usages.put("removeWorld", "Removes a world from the list");
		usages.put("listWorlds", "Lists all the worlds added");
		usages.put("generateNetherAndEndFor", "Generates a nether and end world for the world");
		usages.put("setsavename", "Sets the save name for a world");
		usages.put("setmaxplayers", "Sets the maximum players for a world");
		// usages.put("Worlds", "Shows all stats of worlds");

		usages.put("setMainLobby", "Changes the default spawn world");
		usages.put("removeMainLobby", "Removes default spawn world");

		usages.put("setmaterial", "Sets the material icon for a world");
		usages.put("setDisplayName", "Changes a display name for a world.");
		usages.put("setDescription", "Sets the description for a world");

		usages.put("hideWorld", "Hides a world from the menu.");
		usages.put("showWorld", "Shows the world from the menu, if previously hidden");

		usages.put("addJoiningCommand", "Adds a command that should be issued when a player joins the world");
		usages.put("listJoiningCommands", "Lists all the commands that are sent when a player joins a world.");
		usages.put("removeJoiningCommand", "Removes a command that issued when a player joins that world");

		usages.put("setWorldSelector", "Sets the word selector to be the item in the sender's hand");
		usages.put("removeWorldSelector", "removes word selector");

		usages.put("setDefaultWeather", "Changes the default weather to either No-rain, always-raining, or default");
		usages.put("setGamemode", "Sets the description for a world");
		usages.put("changeSpawn", "Changes the spawn location for a world");
		usages.put("version", "Gets the version of the plugin");
		usages.put("Bungee", "Toggles if this server has bungee enabled");
		usages.put("addServer", "Adds a server icon to the menu");
		usages.put("listServer", "Lists all the registered servers");
		usages.put("removeServer", "Removes a server from the list");
		usages.put("setvoidlooping", "Enables or disables teleporting players to spawn if they are in the void");
		usages.put("setdisablehealthandhunger", "Enables or disables health or hunger changes for worlds");
		usages.put("setlocationsaving", "Enables or disables location saving when a player leaves the world");
		usages.put("setcanuseportals", "Enables or disables portals for certain worlds.");
		usages.put("addDefaultItem", "Adds the item in your hand to the list of default items.");
		usages.put("removeDefaultItem", "Adds the item in your hand to the list of default items.");
		usages.put("listDefaultItems", "Adds the item in your hand to the list of default items.");
		usages.put("addDecor", "Adds a decor item to the hub menu");
		usages.put("removeDecor", "Removes a decor item.");

		usages.put("toggleWhitelist", "Toggles whether the world is restricted to only certain players");
		usages.put("addToWhitelist", "Adds a player to a world's whitelist");
		usages.put("removeFromWhitelist", "Adds a player to a world's whitelist");
		usages.put("listWhitelist", "Adds a player to a world's whitelist");

		usages.put("goto", "Teleports the player to the spawn of a world");
	}

	public void generateConnctingWorlds(LobbyWorld mainWorld) {
		World wo = mainWorld.getWorld();
		World nether = Bukkit.getWorld(wo.getName() + "_nether");
		World end = Bukkit.getWorld(wo.getName() + "_the_end");
		if (nether == null) {
			Bukkit.createWorld(new WorldCreator(wo.getName() + "_nether").environment(Environment.NETHER)
					.generator(Bukkit.getWorlds().get(1).getGenerator()).seed(mainWorld.getWorld().getSeed()));
			nether = Bukkit.getWorld(wo.getName() + "_nether");
		}
		if (end == null) {
			Bukkit.createWorld(new WorldCreator(wo.getName() + "_the_end").environment(Environment.THE_END)
					.generator(Bukkit.getWorlds().get(2).getGenerator()).seed(mainWorld.getWorld().getSeed()));
			end = Bukkit.getWorld(wo.getName() + "_the_end");
		}
		//int netherid = LobbyAPI.getOpenSlot(10);
		//int endid = LobbyAPI.getOpenSlot(10);

		/*@SuppressWarnings("deprecation")
		LobbyWorld n = LobbyAPI.registerWorldFromConfig(nether, nether.getSpawnLocation(), mainWorld.getSaveName(),
				null, 0, netherid, mainWorld.getGameMode(), true);
		@SuppressWarnings("deprecation")
		LobbyWorld e = LobbyAPI.registerWorldFromConfig(end, end.getSpawnLocation(), mainWorld.getSaveName(), null, 0,
				endid, mainWorld.getGameMode(), true);

		saveWorld(mainWorld.getSaveName(), nether, nether.getSpawnLocation(), 0, netherid, true, true,
				mainWorld.getWorldName());
		saveWorld(mainWorld.getSaveName(), end, end.getSpawnLocation(), 0, endid, true, true, mainWorld.getWorldName());*/

		mainWorld.setNether(nether);
		mainWorld.setEnd(end);
		//n.setNether(wo);
		//e.setEnd(wo);
		mainWorld.setPortal(true);
		m.getConfig().set("Worlds." + wo.getName() + "." + ConfigKeys.CanUsePortals, true);
		//m.getConfig().set("Worlds." + nether.getName() + "." + ConfigKeys.WORLDENVIROMENT, Environment.NETHER.name());
		//m.getConfig().set("Worlds." + end.getName() + "." + ConfigKeys.WORLDENVIROMENT, Environment.THE_END.name());
		m.getConfig().set("Worlds." + wo.getName() + "." + ConfigKeys.LINKED_NETHER, nether.getName());
	//	m.getConfig().set("Worlds." + nether.getName() + "." + ConfigKeys.LINKED_NETHER, wo.getName());
		m.getConfig().set("Worlds." + wo.getName() + "." + ConfigKeys.LINKED_END, end.getName());
		//m.getConfig().set("Worlds." + end.getName() + "." + ConfigKeys.LINKED_END, wo.getName());
		m.saveConfig();
	}

	private void saveWorld(String fi, String savename, World wo, Location l, int color, int i, boolean hidden) {
		boolean k = false;
		String j = null;
		if (wo == Bukkit.getWorlds().get(1) || wo == Bukkit.getWorlds().get(2)) {
			k = true;
			j = Bukkit.getWorlds().get(0).getName();
		}
		if (wo == Bukkit.getWorlds().get(0)) {
			k = true;
		}
		saveWorld(savename, wo, l, color, i, hidden, k, j);

	}

	private void saveWorld(String savename, World wo, Location l, int color, int i, boolean hidden,
						   boolean canUsePortals, String connectedTo) {
		m.getConfig().set("Worlds." + wo.getName() + ".name", wo.getName());
		// m.getConfig().set("Worlds." + wo.getName() + ".displayname", wo.getName());
		m.getConfig().set("Worlds." + wo.getName() + ".spawnLoc.x", l.getX());
		m.getConfig().set("Worlds." + wo.getName() + ".spawnLoc.y", l.getY());
		m.getConfig().set("Worlds." + wo.getName() + ".spawnLoc.z", l.getZ());
		m.getConfig().set("Worlds." + wo.getName() + ".spawnLoc.yaw", l.getYaw());
		m.getConfig().set("Worlds." + wo.getName() + ".spawnLoc.pitch", l.getPitch());
		m.getConfig().set("Worlds." + wo.getName() + ".spawnLoc.w", l.getWorld().getName());
		m.getConfig().set("Worlds." + wo.getName() + ".weatherstate", WeatherState.NORMAL.name());
		m.getConfig().set("Worlds." + wo.getName() + ".i", i);
		if (wo.getName() != savename)
			m.getConfig().set("Worlds." + wo.getName() + ".save", savename);

		// m.getConfig().set("Worlds." + wo.getName() + ".desc", "");
		m.getConfig().set("Worlds." + wo.getName() + ".gamemode", GameMode.SURVIVAL.name());
		m.getConfig().set("Worlds." + wo.getName() + ".color", color);
		m.getConfig().set("Worlds." + wo.getName() + "." + ConfigKeys.isHidden, hidden);
		m.getConfig().set("Worlds." + wo.getName() + ".canuseportals", canUsePortals);

		m.getConfig().set("CustomWorlds." + wo.getName() + "." + ConfigKeys.CustomAddedWorlds_Seed, connectedTo);

		// m.getConfig().set("Worlds." + wo.getName() + ".maxPlayers", -1);

		m.saveConfig();
	}


}
