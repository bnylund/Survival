package Core;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberJoinEvent;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberLeaveEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.server.ServerJoinEvent;
import org.javacord.api.event.server.ServerLeaveEvent;
import org.javacord.api.event.server.member.ServerMemberBanEvent;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.event.server.member.ServerMemberLeaveEvent;
import org.javacord.api.event.server.member.ServerMemberUnbanEvent;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberJoinListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberLeaveListener;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.server.ServerJoinListener;
import org.javacord.api.listener.server.ServerLeaveListener;
import org.javacord.api.listener.server.member.ServerMemberBanListener;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;
import org.javacord.api.listener.server.member.ServerMemberLeaveListener;
import org.javacord.api.listener.server.member.ServerMemberUnbanListener;

import com.coloredcarrot.api.sidebar.Sidebar;
import com.coloredcarrot.api.sidebar.SidebarString;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Survival extends JavaPlugin implements Listener {

	public static Plugin pl;
	public static PluginManager pm;
	public static String _motd;
	public static DiscordApi api;
	public static Thread th;
	public static String PREFIX = ChatColor.DARK_RED + "[" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "MINECRAFT: " + ChatColor.RED + ChatColor.BOLD.toString() + "SURVIVAL" + ChatColor.DARK_RED + "] " + ChatColor.RESET;
	public static List<Location> chests = new ArrayList<>();
	public static List<Merchant> merchants = new ArrayList<Merchant>();
	public static Sidebar main;
	public static HashMap<Material, List<Enchantment>> validEnchantments = new HashMap<Material, List<Enchantment>>();

	public Scoreboard sb;

	public void onEnable() {
		Survival.pl = this;
		pm = getServer().getPluginManager();
		
		Bukkit.getLogger().log(Level.INFO, "Registering events...");
		pm.registerEvents(this, this);
		pm.registerEvents(new EnchantmentListeners(), this);
		pm.registerEvents(new ShopListeners(), this);
		pm.registerEvents(new DungeonListeners(), this);

		Bukkit.getLogger().log(Level.INFO, "Registering commands...");
		getCommand("dungeon").setExecutor(new DungeonCommand());
		getCommand("shop").setExecutor(new ShopCommands());
		getCommand("shopsetup").setExecutor(new ShopCommands());
		getCommand("sregion").setExecutor(new RegionCommand());

		Bukkit.getLogger().log(Level.INFO, "Setting up enchanter...");
		for(String item : getConfig().getStringList("EnchantmentItems")) {
			Material mat = Material.valueOf(item);
			for(Enchantment ench : Enchantment.values()) {
				ItemStack test = new ItemStack(mat, 1);
				if(ench.canEnchantItem(test)) {
					if(validEnchantments.containsKey(mat)) {
						List<Enchantment> enchantments = validEnchantments.get(mat);
						enchantments.add(ench);
						validEnchantments.put(mat, enchantments);
					} else {
						List<Enchantment> enchantments = new ArrayList<Enchantment>();
						enchantments.add(ench);
						validEnchantments.put(mat, enchantments);
					}
				}
			}
		}

		Bukkit.getLogger().log(Level.INFO, "Setting MOTD...");
		if (getServer().getPort() == 25565) {
			_motd = ChatColor.GREEN + ChatColor.BOLD.toString() + "    -= " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "MINECRAFT: " + ChatColor.RED + ChatColor.BOLD.toString() + "SURVIVAL" + ChatColor.GREEN + ChatColor.BOLD.toString() + " =-    ";
		} else {
			_motd = ChatColor.RED + ChatColor.BOLD.toString() + "Development Server";
		}
		
		this.sb = Bukkit.getScoreboardManager().getNewScoreboard();

		if(!pl.getConfig().contains("discord.token")) {
			pl.getConfig().set("discord.token", "TOKEN_HERE");
			pl.saveConfig();
		}

		Bukkit.getLogger().log(Level.INFO, "Starting Discord...");
		try {
			th = new Thread() {
				public void run() {
					api = new DiscordApiBuilder().setToken(pl.getConfig().getString("discord.token")).login().join();
					Permissions perms = new PermissionsBuilder().setAllowed(PermissionType.CHANGE_NICKNAME).setAllowed(PermissionType.CONNECT).setAllowed(PermissionType.READ_MESSAGE_HISTORY).setAllowed(PermissionType.SEND_MESSAGES).setAllowed(PermissionType.READ_MESSAGES).build();
					Bukkit.getLogger().log(Level.INFO, "The bot can be invited to servers with the following link: " + api.createBotInvite(perms));
					api.updateActivity(ActivityType.PLAYING, "with " + Bukkit.getOnlinePlayers().size() + " players!");
					api.addServerJoinListener(new ServerJoinListener() {

						public void onServerJoin(ServerJoinEvent event) {
							Bukkit.broadcastMessage(ChatColor.AQUA + "[Discord - Server Join] " + ChatColor.YELLOW + "The bot just joined " + ChatColor.GREEN + event.getServer().getName() + ChatColor.YELLOW + "!");
						}
					
					});
					api.addServerLeaveListener(new ServerLeaveListener() {

						public void onServerLeave(ServerLeaveEvent event) {
							Bukkit.broadcastMessage(ChatColor.AQUA + "Discord - Server Leave] " + ChatColor.YELLOW + "The bot just left " + ChatColor.GREEN + event.getServer().getName() + ChatColor.YELLOW + ".");
						}
						
					});
					
					api.addMessageCreateListener(new MessageCreateListener () {

						public void onMessageCreate(MessageCreateEvent e) {
							if(!((ServerTextChannel)e.getChannel()).getName().equalsIgnoreCase("minecraft-commands")) {
								if(!((ServerTextChannel)e.getChannel()).getName().equalsIgnoreCase("welcome")) {
									Bukkit.broadcastMessage(ChatColor.AQUA + "[Discord - #" + ((ServerTextChannel)e.getChannel()).getName() + "] " + ChatColor.YELLOW + e.getMessageContent());
									if(e.getMessageAttachments().size() > 0) Bukkit.broadcastMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "+" + e.getMessageAttachments().size() + " Attachments");
								}
							} else {
								// Commands
								try {
									if(e.getMessageContent().split(" ")[0].equalsIgnoreCase("!command_here")) {
										
									}
								} catch(Exception ex) {
									ex.printStackTrace();
									e.getChannel().sendMessage("An error occurred! <@369948469986852874> check logs plsthnx");
								}
							}
						}
						
					});
					api.addServerMemberJoinListener(new ServerMemberJoinListener() {

						@Override
						public void onServerMemberJoin(ServerMemberJoinEvent e) {
							Bukkit.broadcastMessage(ChatColor.AQUA + "[Discord - Members] " + ChatColor.YELLOW + e.getUser().getDiscriminatedName() + " just joined the discord server!");
						}
						
					});
					api.addServerMemberLeaveListener(new ServerMemberLeaveListener() {

						@Override
						public void onServerMemberLeave(ServerMemberLeaveEvent e) {
							Bukkit.broadcastMessage(ChatColor.AQUA + "[Discord - Members] " + ChatColor.YELLOW + e.getUser().getDiscriminatedName() + " just left the discord server! :(");
						}
						
					});
					api.addServerMemberBanListener(new ServerMemberBanListener() {

						@Override
						public void onServerMemberBan(ServerMemberBanEvent e) {
							Bukkit.broadcastMessage(ChatColor.AQUA + "[Discord - Members] " + ChatColor.YELLOW + e.getUser().getDiscriminatedName() + " was banned from the discord server!");
						}
						
					});
					api.addServerMemberUnbanListener(new ServerMemberUnbanListener() {

						@Override
						public void onServerMemberUnban(ServerMemberUnbanEvent e) {
							Bukkit.broadcastMessage(ChatColor.AQUA + "[Discord - Members] " + ChatColor.YELLOW + e.getUser().getDiscriminatedName() + " was just unbanned from the discord server!");
						}
						
					});
					api.addServerVoiceChannelMemberJoinListener(new ServerVoiceChannelMemberJoinListener() {

						public void onServerVoiceChannelMemberJoin(ServerVoiceChannelMemberJoinEvent e) {
							Bukkit.broadcastMessage(ChatColor.AQUA + "[Discord] "+ChatColor.YELLOW + e.getUser().getDiscriminatedName() + " joined \"" + e.getChannel().getName() + "\""); 
						}
						
					});
					api.addServerVoiceChannelMemberLeaveListener(new ServerVoiceChannelMemberLeaveListener() {
			
						public void onServerVoiceChannelMemberLeave(ServerVoiceChannelMemberLeaveEvent e) {
							Bukkit.broadcastMessage(ChatColor.AQUA + "[Discord] "+ChatColor.YELLOW + e.getUser().getDiscriminatedName() + " left \"" + e.getChannel().getName() + "\""); 
						}
						
					});
				}
			};
			th.start();
		} catch(Exception ex) {
			Bukkit.getLogger().log(Level.SEVERE, "Running plugin without discord integration. Class not found.");
		}

		Bukkit.getLogger().log(Level.INFO, "Loading merchants...");
		reloadMerchants();
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

			@Override
			public void run() {
				try {
					api.updateActivity(ActivityType.PLAYING, "with " + Bukkit.getOnlinePlayers().size() + " player"+(Bukkit.getOnlinePlayers().size() == 1 ? "" : "s")+"!");
					updateScoreboard();
				} catch(Exception ex) {}
			}
			
		}, 0, 20);

		Bukkit.getLogger().log(Level.INFO, "Loading scoreboard...");
		main = new Sidebar(ChatColor.LIGHT_PURPLE+ ChatColor.BOLD.toString() + "     Players     ", (Plugin) this, 20, new SidebarString[0]);

		for (Player p : Bukkit.getOnlinePlayers()) {
			main.showTo(p);
		}

		updateScoreboard();
		
		getServer().getLogger().log(Level.INFO, "Survival started!");
	}

	public static void updateScoreboard() {
		main.setEntries(new ArrayList<SidebarString>());
		main.addEmpty();
		for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
			main.addEntry(new SidebarString((p.isOnline() ? ChatColor.GREEN: ChatColor.RED) + p.getName()));
		}
		main.addEmpty();
	}

	@SuppressWarnings("deprecation")
	public void onDisable() {
		unregisterHealthBar();
		for (Player p: Bukkit.getOnlinePlayers()) {
			main.hideFrom(p);
		}
		for (Hologram ho : HologramsAPI.getHolograms((Plugin) this)) {
			ho.delete();
		}
		if(api != null) {
			api.getListeners().forEach((k, v) -> {
				api.removeListener(k);
			});
			api.disconnect();
		}
		th.stop();
	}

	@SuppressWarnings("deprecation")
	public void registerHealthBar() {
		if (this.sb.getObjective("health") != null) {
			this.sb.getObjective("health").unregister();
		}
		Objective o = this.sb.registerNewObjective("health", "health");
		o.setDisplayName(ChatColor.RED + "❤");
		o.setDisplaySlot(DisplaySlot.BELOW_NAME);
	}

	public void unregisterHealthBar() {
		Set < Objective > sidebar = this.sb.getObjectives();
		for (Objective o: sidebar) {
			if (o.getName().contains("health")) {
				o.unregister();
			}
		}
	}
	
	public void reloadMerchants() {
		// Clear existing merchants
		for(Merchant mc : merchants) {
			mc.Despawn();
		}
		merchants.clear();
		
		// Add merchants
		
		
		// Blocks
		
		// Food
		
		// Enchantment Books
		
		// Dungeon keys?
	}

	public static String getFormattedName(String input) {
		String[] parts = input.split("_");
		String total = "";
		byte b;
		int i;
		String[] arrayOfString1;
		for (i = (arrayOfString1 = parts).length, b = 0; b < i;) {
			String s = arrayOfString1[b];
			for (int j = 0; j < s.length(); j++) {
				if (j == 0) {
					total = String.valueOf(total) + s.charAt(j);
				} else {
					total = String.valueOf(total) + s.toLowerCase().charAt(j);
				}
			}
			total = String.valueOf(total) + " ";
			b++;
		}
		total = total.substring(0, total.length() - 1);
		return total;
	}

	@EventHandler
	public void damage(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity && !(e.getEntity() instanceof Player)) {
			LivingEntity ent = (LivingEntity) e.getEntity();
			EntityType entt = ent.getType();
			if (entt != EntityType.ENDER_DRAGON && entt != EntityType.WITHER) {
				ent.setCustomName(String.valueOf(String.valueOf(Integer.toString((int) ent.getHealth()))) + ChatColor.RED + " ❤");
				ent.setCustomNameVisible(true);
				if (ent.getHealth() - e.getFinalDamage() <= 0.0D) {
					ent.setCustomName(null);
					ent.setCustomNameVisible(false);
				}
			}
		} else if (e.getDamager() instanceof LivingEntity && e.getEntity() instanceof Player) {
			LivingEntity ent = (LivingEntity) e.getDamager();
			EntityType entt = ent.getType();
			Player p = (Player) e.getEntity();
			if (entt != EntityType.ENDER_DRAGON && entt != EntityType.WITHER && p.getHealth() - e.getDamage() < 1.0D) {
				ent.setCustomName(null);
				ent.setCustomNameVisible(false);
			}
		}
	}

	@EventHandler
	public void entityDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof LivingEntity && !(e.getEntity() instanceof Player)) {
			LivingEntity ent = (LivingEntity) e.getEntity();
			EntityType entt = ent.getType();
			if (entt != EntityType.ENDER_DRAGON && entt != EntityType.WITHER) {
				ent.setCustomName(String.valueOf(String.valueOf(Integer.toString((int) ent.getHealth()))) + ChatColor.RED + " ❤");
				ent.setCustomNameVisible(true);
				if (ent.getHealth() - e.getFinalDamage() <= 0.0D) {
					ent.setCustomName(null);
					ent.setCustomNameVisible(false);
				}
			}
		}
	}

	@EventHandler
	public void onEntityRegain(EntityRegainHealthEvent e) {
		if (e.getEntity() instanceof LivingEntity) {
			LivingEntity ent = (LivingEntity) e.getEntity();
			double health = ent.getHealth();
			if (health >= 1.0D) {
				if (health + e.getAmount() > 20.0D) health = 20.0D;
				ent.setCustomName(String.valueOf(String.valueOf(Integer.toString((int) health))) + ChatColor.RED + " ❤");
				ent.setCustomNameVisible(true);
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		if (!pl.getConfig().contains("KnownPlayers")) {
			pl.getConfig().set("KnownPlayers", new String[] {
				e.getPlayer().getUniqueId().toString()
			});
		} else {
			List < String > players = pl.getConfig().getStringList("KnownPlayers");
			if (!players.contains(e.getPlayer().getUniqueId().toString())) players.add(e.getPlayer().getUniqueId().toString());
			pl.getConfig().set("KnownPlayers", players);
		}
		pl.getConfig().set(String.valueOf(e.getPlayer().getUniqueId().toString()) + ".name", e.getPlayer().getName());
		pl.saveConfig();
		e.getPlayer().setGameMode(GameMode.SURVIVAL);
		e.getPlayer().setPlayerListHeaderFooter(ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "MINECRAFT: " + ChatColor.RED + ChatColor.BOLD.toString() + "SURVIVAL", "");
		e.getPlayer().sendMessage(" ");
		e.getPlayer().sendMessage("    " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "MINECRAFT: " + ChatColor.RED + ChatColor.BOLD.toString() + "SURVIVAL");
		e.getPlayer().sendMessage(" ");
		e.getPlayer().sendMessage(ChatColor.GOLD + "Changelog:");
		e.getPlayer().sendMessage(" ");
		e.getPlayer().sendMessage(ChatColor.AQUA + " - Added discord integration.");
		e.getPlayer().sendMessage(ChatColor.GOLD + " - Imported old code");
		e.getPlayer().sendMessage(" ");
		e.setJoinMessage(ChatColor.GREEN + " ✦ " + ChatColor.YELLOW + e.getPlayer().getName() + " connected.");
		for (Player p : Bukkit.getOnlinePlayers())
		p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 200.0F, 100.0F);
		main.showTo(e.getPlayer());
		updateScoreboard();
	}

	@EventHandler
	public void onInvClick(InventoryClickEvent e) {
		if (e.getInventory() instanceof ClickableInventory) { 
			((ClickableInventory) e.getInventory()).onClick(e);
		}
	}

	public static List<SurvivalPlayer> getAllPlayers() {
		List<SurvivalPlayer> players = new ArrayList<>();
		for (String s : pl.getConfig().getStringList("KnownPlayers"))
			players.add(new SurvivalPlayer(UUID.fromString(s)));
		return players;
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		e.setQuitMessage(ChatColor.GREEN + " ✦ " + ChatColor.YELLOW + e.getPlayer().getName() + " disconnected.");
		for (Player p: Bukkit.getOnlinePlayers())
		p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 200.0F, 100.0F);
		updateScoreboard();
	}

	@EventHandler
	public void onPing(ServerListPingEvent e) {
		if (!_motd.equals("none")) e.setMotd(_motd);
		e.setMaxPlayers(Bukkit.getOnlinePlayers().size() + 1);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if (e.getBlock().getType().toString().contains("LOG")) {
			//e.setCancelled(true);
			//onLogBreak(e.getBlock().getLocation());
		}
	}

	@SuppressWarnings("deprecation")
	public void onLogBreak(Location loc) {
		Block b = loc.getBlock();
		for (Player p: Bukkit.getOnlinePlayers())
		p.playEffect(loc, Effect.MOBSPAWNER_FLAMES, 10);
		b.breakNaturally();
		List<Location> surround = getSurroundingBlocks(loc);
		for (Location blo: surround) {
			if (blo.getBlock().getType().toString().contains("LOG")) onLogBreak(blo);
		}
	}

	public List<Location> getSurroundingBlocks(Location loc) {
		List<Location> blocks = new ArrayList<>();
		for (int x = loc.getBlockX() - 1; x <= loc.getBlockX() + 1; x++) {
			for (int y = loc.getBlockY() - 1; y <= loc.getBlockY() + 1; y++) {
				for (int z = loc.getBlockZ() - 1; z <= loc.getBlockZ() + 1; z++)
				blocks.add(new Location(loc.getWorld(), x, y, z));
			}
		}
		return blocks;
	}

	public static void spawnFireworks(Location location, int amount) {
		Location loc = location;
		Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
		FireworkMeta fwm = fw.getFireworkMeta();
		fwm.setPower(2);
		fwm.addEffect(FireworkEffect.builder().withColor(Color.RED).flicker(true).build());
		fw.setFireworkMeta(fwm);
		fw.detonate();
		for (int i = 0; i < amount; i++) {
			Firework fw2 = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
			fw2.setFireworkMeta(fwm);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(e.getAction() == Action.LEFT_CLICK_BLOCK) {
			Location loca = e.getClickedBlock().getLocation();
			
			List<Location> toRemove = new ArrayList<Location>();
			for (Location loc : chests) {
				if(loc.getBlock().getType() == Material.CHEST) {
					
					if(loc.getBlockX() == loca.getBlockX() && loc.getBlockY() == loca.getBlockY() && loc.getBlockZ() == loca.getBlockZ()) {
						loc.getBlock().setType(Material.AIR);
						for (Player p: Bukkit.getOnlinePlayers())
							p.playEffect(loc, Effect.MOBSPAWNER_FLAMES, 30);
						loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0F, 1.0F);
						toRemove.add(loc);
					}
					
				}
			}
			for(Location loc : toRemove) {
				chests.remove(loc);
			}
		} else {
			List<Location> toRemove = new ArrayList<Location>();
			for (Location loc : chests) {
				if(loc.getBlock().getType() == Material.CHEST) {
					Chest chest = (Chest) (org.bukkit.block.BlockState)loc.getBlock().getState();
					Inventory inv = chest.getBlockInventory();
					boolean breakChest = true;
					for (int i = 0; i < 27; i++) {
						if (inv.getItem(i) != null && inv.getItem(i).getType() != Material.AIR) breakChest = false;
					}
					if (breakChest) {
						loc.getBlock().setType(Material.AIR);
						for (Player p: Bukkit.getOnlinePlayers())
							p.playEffect(loc, Effect.MOBSPAWNER_FLAMES, 30);
						loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0F, 1.0F);
						toRemove.add(loc);
					}
				}
			}
			for(Location loc : toRemove) {
				chests.remove(loc);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void invClose(InventoryCloseEvent e) {
		List<Location> toRemove = new ArrayList<Location>();
		for (Location loc : chests) {
			if(loc.getBlock().getType() == Material.CHEST) {
				Chest chest = (Chest) (org.bukkit.block.BlockState)loc.getBlock().getState();
				Inventory inv = chest.getBlockInventory();
				boolean breakChest = true;
				for (int i = 0; i < 27; i++) {
					if (inv.getItem(i) != null && inv.getItem(i).getType() != Material.AIR) breakChest = false;
				}
				if (breakChest) {
					loc.getBlock().setType(Material.AIR);
					for (Player p: Bukkit.getOnlinePlayers())
						p.playEffect(loc, Effect.MOBSPAWNER_FLAMES, 30);
					loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0F, 1.0F);
					toRemove.add(loc);
				}
			}
		}
		for(Location loc : toRemove) {
			chests.remove(loc);
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Location loc = e.getEntity().getLocation();
		
		// If the player died on a slab or something, place the chest 1 block higher. so the slab doesn't get removed
		loc.setY(Math.ceil(loc.getY()));
		
		e.setDeathMessage(ChatColor.RED + e.getEntity().getName() + " died! Location: " + ChatColor.GOLD + loc.getBlockX() + "x " + loc.getBlockY() + "y " + loc.getBlockZ() + "z");
		for (Player p : Bukkit.getOnlinePlayers())
		p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0F, 1.0F);
		loc.getBlock().setType(Material.CHEST);
		Chest chest = (Chest) loc.getBlock().getState();
		Inventory inv = chest.getBlockInventory();
		List<ItemStack> noFit = new ArrayList<>();
		for (ItemStack i : e.getDrops()) {
			for (ItemStack is : inv.addItem(i).values()) {
				noFit.add(is);
			}
		}
		for (ItemStack i : noFit)
		loc.getWorld().dropItemNaturally(loc, i);
		e.getDrops().clear();
		chests.add(loc);
		spawnFireworks(loc, 3);
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onItemDamage(PlayerItemDamageEvent e) {
		ItemStack is = e.getItem();
		ItemMeta im = is.getItemMeta();
		List<String> lore = (im.hasLore() ? im.getLore() : new ArrayList<String>());
		
		short durability = (short) (is.getType().getMaxDurability() - is.getDurability());
		float percentBefore = ((float) (durability + e.getDamage()) / is.getType().getMaxDurability())*100;
		float percentRemaining = ((float) durability / is.getType().getMaxDurability())*100;
		durability -= 1;

		String mainHand = is.getType().toString();
		if (mainHand.contains("AXE") || mainHand.contains("SHOVEL") || mainHand.contains("HOE") || mainHand.contains("FLINT_AND_STEEL") || mainHand.contains("SHEARS") || mainHand.contains("FISHING_ROD") || mainHand.contains("BOW") || mainHand.contains("CHESTPLATE") || mainHand.contains("HELMET") || mainHand.contains("LEGGINGS") || mainHand.contains("BOOTS") || mainHand.contains("SWORD")) {
			ChatColor chatColor;
			if (mainHand.contains("DIAMOND")) {
				chatColor = ChatColor.AQUA;
			} else if (mainHand.contains("GOLD")) {
				chatColor = ChatColor.YELLOW;
			} else if (mainHand.contains("IRON")) {
				chatColor = ChatColor.WHITE;
			} else if (mainHand.contains("STONE")) {
				chatColor = ChatColor.GRAY;
			} else if (mainHand.contains("NETHERITE")){
				chatColor = ChatColor.DARK_GRAY;
			} else if(mainHand.contains("WOODEN")){
				chatColor = ChatColor.GOLD;
			} else {
				chatColor = ChatColor.WHITE;
			}
			
			
			String str1 = String.valueOf(chatColor) + getFormattedName(is.getType().toString());
			String currentDur = "";
			if (percentRemaining < 33) {
				currentDur = String.valueOf(ChatColor.RED.toString()) + durability;
			} else if (percentRemaining < 66) {
				currentDur = String.valueOf(ChatColor.GOLD.toString()) + durability;
			} else {
				currentDur = String.valueOf(ChatColor.GREEN.toString()) + durability;
			}
			boolean containsDur = false;
			if(!mainHand.contains("CHESTPLATE") && !mainHand.contains("HELMET") && !mainHand.contains("LEGGINGS") && !mainHand.contains("BOOTS") && !mainHand.contains("SWORD")) {
				e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(String.valueOf(str1) + ChatColor.WHITE + " - " + currentDur + ChatColor.WHITE + " / " + ChatColor.GREEN + is.getType().getMaxDurability()));
			}
			
			List<String> newLore = new ArrayList<String>();
			for(String ss : lore) {
				if(ss.contains(ChatColor.AQUA + ChatColor.BOLD.toString() + "Durability: "))
					containsDur = true;
			}
			if(containsDur)
				for(String sss : lore) 
					if(sss.contains(ChatColor.AQUA + ChatColor.BOLD.toString() + "Durability: "))
						newLore.add(ChatColor.AQUA + ChatColor.BOLD.toString() + "Durability: " + ((percentRemaining < 33 ? ChatColor.RED.toString() + durability : (percentRemaining < 66 ? ChatColor.YELLOW.toString() + durability : ChatColor.GREEN.toString() + durability))) + ChatColor.GOLD + " / " + ChatColor.GREEN + is.getType().getMaxDurability());
					else newLore.add(sss);
			else {
				for(String ss : lore)
					newLore.add(ss);
				newLore.add(" ");
				newLore.add(ChatColor.AQUA + ChatColor.BOLD.toString() + "Durability: " + ((percentRemaining < 33 ? ChatColor.RED.toString() + durability : (percentRemaining < 66 ? ChatColor.YELLOW.toString() + durability : ChatColor.GREEN.toString() + durability))) + ChatColor.GOLD + " / " + ChatColor.GREEN + is.getType().getMaxDurability());
				newLore.add(" ");
			}
			
			if(percentBefore >= 20 && percentRemaining < 20)
				e.getPlayer().sendMessage(PREFIX + ChatColor.RED + "Item durability dropped below 20%! ("+str1+ ChatColor.RED + ")");
			im.setLore(newLore);
			is.setItemMeta(im);
			e.getPlayer().updateInventory();
		}
	}

	
	public static ItemStack getHead(OfflinePlayer p) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skull = (SkullMeta) item.getItemMeta();
        skull.setOwningPlayer(p);
        item.setItemMeta(skull);
        return item;
    }
	
	public static WorldEditPlugin getWorldEdit() {
		return (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
	}
	
	public static boolean isInRegion(Location loc, String name) {
		if(pl.getConfig().contains("region." + name)) {
			int minX = pl.getConfig().getInt("region." + name + ".minPoint.x");
			int minY = pl.getConfig().getInt("region." + name + ".minPoint.y");
			int minZ = pl.getConfig().getInt("region." + name + ".minPoint.z");
			int maxX = pl.getConfig().getInt("region." + name + ".maxPoint.x");
			int maxY = pl.getConfig().getInt("region." + name + ".maxPoint.y");
			int maxZ = pl.getConfig().getInt("region." + name + ".maxPoint.z");
			
			if(loc.getBlockX() >= minX && loc.getBlockX() <= maxX && loc.getBlockY() >= minY && loc.getBlockY() <= maxY && loc.getBlockZ() >= minZ && loc.getBlockZ() <= maxZ)
				return true;
		}
		return false;
	}
}