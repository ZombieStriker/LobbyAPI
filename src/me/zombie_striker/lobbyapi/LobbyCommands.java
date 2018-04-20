package me.zombie_striker.lobbyapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import me.zombie_striker.lobbyapi.LobbyWorld.WeatherState;
import me.zombie_striker.lobbyapi.utils.ConfigHandler;
import me.zombie_striker.lobbyapi.utils.ConfigHandler.ConfigKeys;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LobbyCommands implements CommandExecutor, TabCompleter {

	private Main m;
	private String prefix;
	private HashMap<String, String> usages = new HashMap<>();

	public LobbyCommands(Main mm) {
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
		for (LobbyWorld wo : m.worlds) {
			if (wo.getWorldName().toLowerCase().startsWith(arg.toLowerCase()))
				tab.add(wo.getWorldName());
		}
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
				} else if (b(args[0], "ChangeSpawn", "addJoiningCommand", "setDisplayName", "ListJoiningCommands",
						"setDescription", "RemoveWorld", "SetMainLobby", "AddDefaultItem", "changeWorldSlot",
						"RemoveDefaultItem", "ListDefaultItems")) {
					if (args.length == 2) {
						bLW(tab, args[1]);
					}
				} else if (args[0].equalsIgnoreCase("setMaterial")) {
					if (args.length == 2) {
						bLW(tab, args[1]);
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
						tab.add("Slot");
					}
					if (args.length == 4)
						tab.add("X");
					if (args.length == 5)
						tab.add("Y");
					if (args.length == 6)
						tab.add("Z");

					if (args.length == 7) {
						tab.add(m.random.nextInt(15) + "");
						tab.add("~");
					}

					if (args.length == 8) {
						for (LobbyWorld wo : m.worlds)
							if (!tab.contains(wo.getSaveName()))
								tab.add(wo.getSaveName());
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
						tab.add("Server_Name");
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
					sender.sendMessage(ChatColor.GOLD + "/LobbyAPI " + ((String) usages.keySet().toArray()[i]));
					sender.sendMessage(ChatColor.WHITE + usages.get((String) usages.keySet().toArray()[i]));
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
							ConfigHandler.setWorldVariable(lw, ConfigKeys.DefaultItems.s, items);
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
			} else if (args[0].equalsIgnoreCase("addDecor")) {
				if (args.length >= 4) {
					int slot = Integer.parseInt(args[1]);
					Material m = Material.SPONGE;
					short data = 0;
					try {
						m = Material.matchMaterial(args[2]);
					} catch (Exception e) {
						String[] vals = args[2].split(":");
						m = Material.getMaterial(Integer.parseInt(vals[0]));
						if (vals.length > 1) {
							data = Short.parseShort(vals[1]);
						}
					}
					StringBuilder display = new StringBuilder();
					for (int i = 3; i < args.length; i++)
						display.append(args[i] + (i < args.length ? " " : ""));

					for (LobbyWorld lw : this.m.worlds)
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

					m.getConfig().set("Worlds." + lw.getSaveName() + ".i", index);
					m.saveConfig();
					sender.sendMessage(prefix + "The slot for the world has been changed to " + index);
				} else {
					sender.sendMessage(
							prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI changeWorldSlot [world] [slot]");
					sender.sendMessage(prefix
							+ " [world] = '~' for the world you are in or the world's name (it is Case Sensitive)");
					sender.sendMessage(prefix + " [slot] = the new slot for the world");
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
					ConfigHandler.setWorldVariable(lw, ConfigKeys.DefaultItems.s, items);
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
						if (m.getConfig().getString("Worlds." + LobbyWorld.getMainLobby().getSaveName() + ".name")
								.equalsIgnoreCase(LobbyWorld.getMainLobby().getWorldName())) {
							m.getConfig().set("Worlds." + LobbyWorld.getMainLobby().getSaveName() + ".isMainLobby",
									false);
							m.saveConfig();
						}
					}
					if (m.getConfig().contains("Worlds." + lw.getSaveName() + ".name") && m.getConfig()
							.getString("Worlds." + lw.getSaveName() + ".name").equalsIgnoreCase(lw.getWorldName())) {
						m.getConfig().set("Worlds." + lw.getSaveName() + ".isMainLobby", true);
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
					if (m.getConfig().getString("Worlds." + LobbyWorld.getMainLobby().getSaveName() + ".name")
							.equalsIgnoreCase(lw.getWorldName())) {
						m.getConfig().set("Worlds." + LobbyWorld.getMainLobby().getSaveName() + ".isMainLobby", false);
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
					m.getConfig().set("Worlds." + lw.getSaveName() + ".desc", g);
					m.saveConfig();
					sender.sendMessage(prefix + "Changed world description for world \"" + lw.getWorldName() + "\" to "
							+ sb.toString() + ".");
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
						wo = m.getServer().getWorld(args[1]);
						if (wo == null) {
							sender.sendMessage(prefix
									+ " This world does not exist. Creating world using LobbyAPI WorldGenerator");
							wo = Bukkit.createWorld(new WorldCreator(args[1]));
							ConfigHandler.setCustomWorldValue(wo.getName(), ConfigKeys.CustomAddedWorlds_Seed.s,
									wo.getSeed());
						}
					}
					if (LobbyAPI.getLobbyWorld(wo) != null) {
						sender.sendMessage(prefix + " This world has already been registered!");
						return false;
					}
					int i = LobbyAPI.getOpenSlot(Integer.parseInt(args[2]));
					double x = 0;
					double y = 100;
					double z = 0;
					if (args[3].contains("~"))
						if (!(sender instanceof Player))
							x = 0;
						else
							x = ((Player) sender).getLocation().getX();
					else
						x = Double.parseDouble(args[3]);

					if (args[4].contains("~"))
						if (!(sender instanceof Player))
							y = 0;
						else
							y = ((Player) sender).getLocation().getY();
					else
						y = Double.parseDouble(args[4]);

					if (args[5].contains("~"))
						if (!(sender instanceof Player))
							z = 0;
						else
							z = ((Player) sender).getLocation().getZ();
					else
						z = Double.parseDouble(args[5]);
					int color = 1;
					if (args.length >= 7 && (!args[6].equalsIgnoreCase("~"))) {
						color = Integer.parseInt(args[6]) % 15;
					} else {
						color = m.random.nextInt(15);
					}
					String savename = wo.getName();
					if (args.length >= 8) {
						savename = args[7];
					}

					if (wo != null) {
						Location l = new Location(wo, x, y, z);
						if (sender instanceof Player) {
							l.setYaw(((Player) sender).getLocation().getYaw());
							l.setPitch(((Player) sender).getLocation().getPitch());
						}
						LobbyAPI.registerWorldFromConfig(wo, l, savename, wo.getName(), color, i, GameMode.SURVIVAL,
								false);
						String fi = wo.getName();
						if (m.getConfig().contains("Worlds." + fi)) {
							sender.sendMessage(prefix
									+ " The config already has registered this world, even though LobbyAPI has not. This should not happen, but if it did, report this to Zombie_Striker on the bukkitdev page: https://dev.bukkit.org/projects/lobbyapi");
							return false;
						}
						saveWorld(fi, savename, wo, l, color, i, false);
						sender.sendMessage(prefix + "Added world \"" + wo.getName() + "\" with a slot of " + i
								+ ": Spawn at " + x + " " + y + " " + z + ".");
						if (Bukkit.getWorlds().get(0).equals(wo)) {
							// If the wo is the main world
							sender.sendMessage(prefix
									+ "Since this world is the main world, the nether and end world will be registered and linked to this world if they have not been registered already.");

							World nether = Bukkit.getWorld("world_nether");
							World end = Bukkit.getWorld("world_the_end");
							if (nether == null) {
								Bukkit.createWorld(new WorldCreator("world_nether"));
								nether = Bukkit.getWorld("world_nether");
							}
							if (end == null) {
								Bukkit.createWorld(new WorldCreator("world_the_end"));
								end = Bukkit.getWorld("world_the_end");
							}
							if (LobbyAPI.getLobbyWorld(nether) == null) {
								LobbyAPI.registerWorldFromConfig(nether, nether.getSpawnLocation(), savename,
										nether.getName(), color, i, GameMode.SURVIVAL, true);
								saveWorld(nether.getName(), savename, nether, nether.getSpawnLocation(), color, 51,
										true);
							}
							if (LobbyAPI.getLobbyWorld(end) == null) {
								LobbyAPI.registerWorldFromConfig(end, end.getSpawnLocation(), savename, end.getName(),
										color, i, GameMode.SURVIVAL, true);
								saveWorld(end.getName(), savename, end, end.getSpawnLocation(), color, 50, true);
							}
						}
						m.loadLocalWorlds();
					}
				} else {
					sender.sendMessage(prefix + " Usage:" + ChatColor.BOLD
							+ " /LobbyAPI addword [name] [slot] [x] [y] [z] [color] [savename]");
					sender.sendMessage(prefix + " [name] = The world's name (it is Case Sensitive)");
					sender.sendMessage(prefix + " [slot] = The slot in the menu for the world");
					sender.sendMessage(prefix + " [x] = The world's spawn's X location");
					sender.sendMessage(prefix + " [y] = The world's spawn's Y location");
					sender.sendMessage(prefix + " [z] = The world's spawn's z location");
					sender.sendMessage(prefix + " [color] = OPTIONAL: The color of the block.");
					sender.sendMessage(prefix
							+ " [savename] = OPTIONAL: The name of inventory save (only useful if you wish for multiple worlds to have the same inventory)");
				}

			} else if (args[0].equalsIgnoreCase("removeWorldSelector")) {
				m.setWorldSelector(null);
				sender.sendMessage(prefix + "World selector removed.");
			} else if (args.length > 0 && args[0].equalsIgnoreCase("setWorldSelector")) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					if (p.getItemInHand() != null) {
						m.setWorldSelector(p.getItemInHand());
						sender.sendMessage(prefix + " World seelector set to item in player's hand");
					} else {
						sender.sendMessage(prefix
								+ " You need to have an item in your hands in order to set it as a world selector.");
					}
				}
			} else if (args[0].equalsIgnoreCase("setLocationSaving")) {
				if (args.length >= 3) {
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null)
						return false;
					Boolean b = Boolean.parseBoolean(args[2]);
					lw.setWorldShouldSavePlayerLocation(b);
					ConfigHandler.setWorldVariable(lw, ConfigHandler.ConfigKeys.ShouldBeSavingLocation.s, b);
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
					LobbyWorld lw = gLW(sender, args[1]);
					if (lw == null)
						return false;
					Material change = Material.WOOL;
					try {
						change = Material.getMaterial(Integer.parseInt(args[2]));
					} catch (Exception e) {
						change = Material.matchMaterial(args[2]);
						if (change == null || change == Material.AIR) {
							sender.sendMessage(prefix
									+ " You need to provide a valid material. Either use the ID or the Material Enum name");
							return true;
						}
					}

					lw.setMaterial(change);
					lw.setColor((short) 0);
					m.getConfig().set("Worlds." + lw.getSaveName() + ".material", change.toString());
					m.getConfig().set("Worlds." + lw.getSaveName() + ".color", 0);
					m.saveConfig();
					sender.sendMessage(prefix + "Changed material for \"" + lw.getWorldName() + "\" to "
							+ change.toString() + ".");

				} else {
					sender.sendMessage(
							prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI setMaterial [name] [material]");
					sender.sendMessage(prefix + " [name] = The world's name (it is Case Sensitive)");
					sender.sendMessage(prefix + " [material] = The new material of the icon");
				}

			} else if (args[0].equalsIgnoreCase("setDisplayName")) {
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
					lw.setDisplayName(ChatColor.translateAlternateColorCodes('&', sb.toString()));
					sender.sendMessage(prefix + " Changing the worlds displayname to \"" + sb.toString() + "\"");
					m.getConfig().set("Worlds." + lw.getSaveName() + ".displayname", lw.getDisplayName());
					m.saveConfig();
				} else {
					sender.sendMessage(prefix + " Usage: /lobbyAPI setDisplayName [World] [The displayname]");
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
					m.getConfig().set("Worlds." + lw.getSaveName() + ".joincommands", lw.getCommandsOnJoin());
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
					m.getConfig().set("Worlds." + lw.getSaveName() + ".joincommands", lw.getCommandsOnJoin());
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
					try {
						change = GameMode.valueOf(args[2]);
					} catch (Exception e) {
						e.printStackTrace();
					}

					lw.setGameMode(change);
					m.getConfig().set("Worlds." + lw.getSaveName() + ".gamemode", change.toString());
					m.saveConfig();
					sender.sendMessage(
							prefix + "Changed gamemode for \"" + lw.getWorldName() + "\" to " + change.name() + ".");
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

					Location l = new Location(lw.getMainWorld(), x, y, z);
					if (sender instanceof Player) {
						l.setYaw(((Player) sender).getLocation().getYaw());
						l.setPitch(((Player) sender).getLocation().getPitch());
					}
					lw.setSpawn(l);
					m.getConfig().set("Worlds." + lw.getSaveName() + ".spawnLoc.x", l.getX());
					m.getConfig().set("Worlds." + lw.getSaveName() + ".spawnLoc.y", l.getY());
					m.getConfig().set("Worlds." + lw.getSaveName() + ".spawnLoc.z", l.getZ());
					m.getConfig().set("Worlds." + lw.getSaveName() + ".spawnLoc.yaw", l.getYaw());
					m.getConfig().set("Worlds." + lw.getSaveName() + ".spawnLoc.pitch", l.getPitch());
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
						if (ConfigHandler.getCustomWorldKeys() == null) {
							sender.sendMessage(prefix + "There are no worlds registered!");
							return true;
						}
						if (ConfigHandler.getCustomWorldKeys().contains(wo.getName()) && wo.getPlayers().size() > 0) {
							sender.sendMessage(prefix + "There are still " + wo.getPlayers().size()
									+ " players in that world! There must not be any players in the world when you remove it.");
							return true;
						}

						m.getConfig().set("Worlds." + LobbyAPI.getLobbyWorld(wo).getSaveName(), null);
						m.saveConfig();
						LobbyAPI.unregisterWorld(wo);
						sender.sendMessage(prefix + "Removed world \"" + wo.getName() + "\"");
						m.loadLocalWorlds();
						ConfigHandler.setCustomWorldValue(wo.getName(), ConfigKeys.CustomAddedWorlds_Seed.s, null);
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
					ConfigHandler.setWorldVariable(lw, ConfigKeys.CanUsePortals.s, b);
					m.saveConfig();
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
					ConfigHandler.setWorldVariable(lw, ConfigKeys.DisableHealthAndHunger.s, b);
					m.saveConfig();
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
					ConfigHandler.setWorldVariable(lw, ConfigKeys.DisableVoid.s, b);
					m.saveConfig();
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
					ConfigHandler.setWorldVariable(lw, ConfigKeys.isHidden.s, true);
					m.saveConfig();
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
					ConfigHandler.setWorldVariable(lw, ConfigKeys.isHidden.s, false);
					m.saveConfig();
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
					ConfigHandler.setWorldVariable(lw, ConfigKeys.Weather.s, ws.name());
					m.saveConfig();
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
						color = m.random.nextInt(15);
					}
					LobbyAPI.registerBungeeServerFromConfig(server, i, color);
					if (m.getConfig().contains("Server." + server)) {
						sender.sendMessage(prefix
								+ " The config already has registered this world, even though LobbyAPI has not. This should not happen, but if it did, report this to Zombie_Striker on the bukkitdev page: https://dev.bukkit.org/projects/lobbyapi");
						return false;
					}
					m.getConfig().set("Server." + server + ".name", server);
					m.getConfig().set("Server." + server + ".i", i);
					m.getConfig().set("Server." + server + ".color", color);
					m.saveConfig();
					sender.sendMessage(prefix + "Added server \"" + server + "\" with a slot of " + i + ".");
					m.loadLocalServers();
				} else {
					sender.sendMessage(
							prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI addserver [name] [slot] [color]");
					sender.sendMessage(prefix + " [name] = The world's name (it is Case Sensitive)");
					sender.sendMessage(prefix + " [slot] = The slot in the menu for the world");
					sender.sendMessage(prefix + " [color] = OPTIONAL: The color of the block.");
				}
			} else if (args.length > 0 && args[0].equalsIgnoreCase("removeserver")) {
				if (args.length >= 2) {
					String server = args[1];
					LobbyAPI.unregisterBungeeServer(server);
					for (String fi : m.getConfig().getConfigurationSection("Server").getKeys(false)) {
						if (m.getConfig().getString("Server." + fi + ".name") != null
								&& m.getConfig().getString("Server." + fi + ".name").equals(server)) {
							m.getConfig().set("Server." + fi, null);
							m.saveConfig();
							break;
						}
						sender.sendMessage(prefix + "Removed server \"" + server + "\"");
						m.loadLocalServers();
					}
				} else {
					sender.sendMessage(prefix + " Usage:" + ChatColor.BOLD + " /LobbyAPI removeserver [name]");
					sender.sendMessage(prefix + " [name] = The server's name (it is Case Sensitive)");
				}
			} else if (args.length > 0 && args[0].equalsIgnoreCase("listWorlds")) {
				sender.sendMessage(prefix + " Worlds that have been added via command");
				for (String fi : m.getConfig().getConfigurationSection("Worlds").getKeys(false)) {
					if (m.getConfig().getString("Worlds." + fi + ".name") != null) {
						sender.sendMessage(prefix + " Savename=" + fi + ": World="
								+ m.getConfig().getString("Worlds." + fi + ".name"));
					}
				}
			} else if (args.length > 0 && args[0].equalsIgnoreCase("listServers")) {
				sender.sendMessage(prefix + " Servers that have been added via command");
				for (String fi : m.getConfig().getConfigurationSection("Server").getKeys(false)) {
					if (m.getConfig().getString("Server." + fi + ".name") != null) {
						sender.sendMessage(prefix + " " + fi + ": Server "
								+ m.getConfig().getString("Server." + fi + ".name") + ".");
					}
				}
			} else if (args.length > 0 && args[0].equalsIgnoreCase("clearPlayerData")) {
				if (args.length >= 4) {
					if (m.getServer().getPlayer(args[1]) != null) {
						if (m.getServer().getWorld(args[2]) != null) {
							if (args[3].equalsIgnoreCase("exp") || args[3].equalsIgnoreCase("xp")) {
								m.getConfig().set(m.getServer().getPlayer(args[1]).getName() + "."
										+ m.getServer().getWorld(args[2]).getName() + ".xp", null);
								m.getConfig().set(m.getServer().getPlayer(args[1]).getName() + "."
										+ m.getServer().getWorld(args[2]).getName() + ".xpl", null);
							}
							if (args[3].equalsIgnoreCase("Health"))
								m.getConfig().set(m.getServer().getPlayer(args[1]).getName() + "."
										+ m.getServer().getWorld(args[2]).getName() + ".health", 20);
							if (args[3].equalsIgnoreCase("Hunger"))
								m.getConfig().set(m.getServer().getPlayer(args[1]).getName() + "."
										+ m.getServer().getWorld(args[2]).getName() + ".hunger", 20);
							if (args[3].equalsIgnoreCase("Inventory")) {
								for (int kl = 0; kl < 36; kl++) {
									m.getConfig().set(m.getServer().getPlayer(args[1]).getName() + "."
											+ m.getServer().getWorld(args[2]).getName() + ".i." + kl, null);
								}
								m.getConfig().set(m.getServer().getPlayer(args[1]).getName() + "."
										+ m.getServer().getWorld(args[2]).getName() + ".a." + 1, null);
								m.getConfig().set(m.getServer().getPlayer(args[1]).getName() + "."
										+ m.getServer().getWorld(args[2]).getName() + ".a." + 2, null);
								m.getConfig().set(m.getServer().getPlayer(args[1]).getName() + "."
										+ m.getServer().getWorld(args[2]).getName() + ".a." + 3, null);
								m.getConfig().set(m.getServer().getPlayer(args[1]).getName() + "."
										+ m.getServer().getWorld(args[2]).getName() + ".a." + 4, null);
							}
							m.saveConfig();
						} else {
							sender.sendMessage(prefix + " That world does not exist");
						}
					} else {
						sender.sendMessage(prefix + " That player does not exist.");
					}

				} else {
					sender.sendMessage(prefix
							+ " Useage: clearPlayerData (Player) (WorldName) (Exp,exp / Health / Hunger / Inventory)");
					sender.sendMessage(prefix + " This will allow OPs to delete player data for specific worlds.");
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
					sender.sendMessage(ChatColor.GOLD + "/lobbyAPI " + ((String) usages.keySet().toArray()[i]));
					sender.sendMessage(ChatColor.WHITE + usages.get((String) usages.keySet().toArray()[i]));
				}
				return true;
			}
		}

		if (cmd.getName().equalsIgnoreCase("Lobby") || cmd.getName().equalsIgnoreCase("hub")) {

			if (sender instanceof Player) {
				Player player = (Player) sender;

				if (m.getConfig().contains("disallowHubCommandNoPerm")
						&& m.getConfig().getBoolean("disallowHubCommandNoPerm")
						&& !player.hasPermission("lobbyapi.hub")) {
					sender.sendMessage(prefix + ChatColor.RED + "You do not have permission to access this command.");
					return false;
				}

				m.setInventorySize(false);
				m.inventory = m.getServer().createInventory(null, m.inventorySize,
						ChatColor.GOLD + "LobbyAPI " + ChatColor.WHITE + "- World selector");

				for (LobbyWorld wo : m.worlds) {
					if (wo != null) {
						if (wo.isHidden())
							continue;

						List<String> ls = new ArrayList<String>();
						if (wo == LobbyWorld.getMainLobby())
							ls.add(ChatColor.BOLD + "" + ChatColor.YELLOW + "Main World");
						ls.addAll(wo.getDescription());
						Set<Player> players = wo.getPlayers();

						String players2 = ChatColor.GOLD + "" + players.size() + " Players ";
						if (wo.hasMaxPlayers()) {
							players2 = ChatColor.GOLD + "" + players.size() + " out of " + wo.getMaxPlayers();
						}
						ls.add(players2);

						for (Player s : players) {
							if (s.equals(player))
								ls.add(ChatColor.WHITE + s.getName());
							else
								ls.add(ChatColor.GRAY + s.getName());
						}

						if ((wo.isPrivate() && players.contains(player)) || !wo.isPrivate()) {
							ItemStack is = LobbyAPI.setName(wo.getDisplayName(), wo.getColor(), wo.getMaterial(), ls);
							is.setAmount(wo.getSlotAmount());
							if (player.getWorld().equals(wo.getMainWorld())) {
								try {
									me.zombie_striker.pluginconstructor.InWorldGlowEnchantment pps = new me.zombie_striker.pluginconstructor.InWorldGlowEnchantment(
											m.enchID);
									is.addEnchantment(pps, 1);
								} catch (Exception e) {

								}
							}
							m.inventory.setItem(wo.getSlot(), is);
						}

					}
				}
				for (LobbyDecor d : m.decor) {
					Material mk = d.getMaterial();
					if (mk == null || mk == Material.AIR)
						mk = Material.BARRIER;

					ItemStack material = new ItemStack(mk);
					material.setAmount(d.getAmount());
					material.setDurability(d.getData());
					ItemMeta im = material.getItemMeta();
					im.setDisplayName(d.getDisplayname());
					im.setLore(d.getLore());
					material.setItemMeta(im);
					m.inventory.setItem(d.getSlot(), material);
				}
				if (m.getConfig() != null && m.getConfig().contains("hasBungee")
						&& m.getConfig().getBoolean("hasBungee")) {
					for (LobbyServer lb : m.bungeeServers) {
						List<String> ls = new ArrayList<String>();
						ls.add(ChatColor.RED + "" + ChatColor.GREEN + ChatColor.GOLD + "BungeeCord Server");
						ItemStack is = LobbyAPI.setName(lb.getName(), lb.getColor(), lb.getMaterial(), ls);
						is.setAmount(lb.getAmount());
						m.inventory.addItem(is);

					}
				}
				player.openInventory(m.inventory);
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
		usages.put("Worlds", "Shows all stats of worlds");

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
		usages.put("clearPlayerData", "Clear data for a player for a world");
		usages.put("Bungee", "Toggles if this server has bungee enabled");
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
	}

	public void saveWorld(String fi, String savename, World wo, Location l, int color, int i, boolean hidden) {
		m.getConfig().set("Worlds." + wo.getName() + ".name", wo.getName());
		m.getConfig().set("Worlds." + wo.getName() + ".displayname", wo.getName());
		m.getConfig().set("Worlds." + wo.getName() + ".spawnLoc.x", l.getX());
		m.getConfig().set("Worlds." + wo.getName() + ".spawnLoc.y", l.getY());
		m.getConfig().set("Worlds." + wo.getName() + ".spawnLoc.z", l.getZ());
		m.getConfig().set("Worlds." + wo.getName() + ".spawnLoc.yaw", l.getYaw());
		m.getConfig().set("Worlds." + wo.getName() + ".spawnLoc.pitch", l.getPitch());
		m.getConfig().set("Worlds." + wo.getName() + ".spawnLoc.w", l.getWorld().getName());
		m.getConfig().set("Worlds." + wo.getName() + ".weatherstate", WeatherState.NORMAL.name());
		m.getConfig().set("Worlds." + wo.getName() + ".i", i);
		m.getConfig().set("Worlds." + wo.getName() + ".save", savename);
		m.getConfig().set("Worlds." + wo.getName() + ".desc", "");
		m.getConfig().set("Worlds." + wo.getName() + ".gamemode", GameMode.SURVIVAL.name());
		m.getConfig().set("Worlds." + wo.getName() + ".color", color);
		m.getConfig().set("Worlds." + wo.getName() + "." + ConfigKeys.isHidden.s, hidden);
		m.saveConfig();
	}

}
