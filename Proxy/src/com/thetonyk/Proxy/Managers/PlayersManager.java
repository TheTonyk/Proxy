package com.thetonyk.Proxy.Managers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import com.google.common.collect.Lists;
import com.thetonyk.Proxy.Main;
import com.thetonyk.Proxy.Managers.PermissionsManager.Rank;
import com.thetonyk.Proxy.Managers.PunishmentsManager.Punishment;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayersManager implements Listener {
	
	private static ProxyServer proxy = ProxyServer.getInstance();
	private static List<String> hiddenCommands = Lists.newArrayList("message", "msg", "tell", "w", "private", "p", "reply", "r");
	public static Set<UUID> cooldown = new HashSet<>();
	
	public static UUID getUUID(int id) throws SQLException {
		
		try (Connection connection = DatabaseManager.getConnection();
		Statement statement = connection.createStatement();
		ResultSet query = statement.executeQuery("SELECT uuid FROM users WHERE id = " + id + ";")) {
			
			if (!query.next()) return null;
				
			return UUID.fromString(query.getString("uuid"));
			
		}
		
	}
	
	public static UUID getUUID(String name) throws SQLException {
		
		try (Connection connection = DatabaseManager.getConnection();
		Statement statement = connection.createStatement();
		ResultSet query = statement.executeQuery("SELECT uuid FROM users WHERE name = '" + name + "';")) {
			
			if (!query.next()) return null;
				
			return UUID.fromString(query.getString("uuid"));
			
		}
		
	}
	
	public static String getField(UUID uuid, String field) throws SQLException {
		
		try (Connection connection = DatabaseManager.getConnection();
		Statement statement = connection.createStatement();
		ResultSet query = statement.executeQuery("SELECT " + field + " FROM users WHERE uuid = '" + uuid.toString() + "';")) {
			
			if (!query.next()) return null;
				
			return query.getString(field);
			
		}
		
	}
	
	public static Settings getSettings(UUID uuid) throws SQLException {
		
		if (Main.settings.containsKey(uuid)) return Main.settings.get(uuid);
		
		Settings settings = Settings.getSettings(uuid);
		
		Main.settings.put(uuid, settings);
		return settings;
		
	}
	
	public static Rank getRank(UUID uuid) throws SQLException {
		
		return Rank.valueOf(getField(uuid, "rank"));
		
	}
	
	public static void setRank(UUID uuid, Rank rank) throws SQLException {
		
		DatabaseManager.updateQuery("UPDATE users SET rank = '" + rank.name() + "' WHERE uuid = '" + uuid.toString() + "';");
		
		ProxiedPlayer player = proxy.getPlayer(uuid);
		
		if (player == null) return;
		
		PermissionsManager.reloadPermissions(player);
		
		ByteArrayOutputStream array = new ByteArrayOutputStream();
		
		try (DataOutputStream output = new DataOutputStream(array)) {
			
			output.writeUTF("updateRank");
			output.writeUTF(uuid.toString());
			
		} catch (IOException exception) {
				
			ComponentBuilder message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
			.append("An error has occured on ").color(ChatColor.GRAY)
			.append(Main.name).color(ChatColor.GREEN)
			.append(" ⫷\n\n").color(ChatColor.DARK_GRAY)
			.append("Please try again later or contact us on Twitter ").color(ChatColor.GRAY)
			.append(Main.twitter).color(ChatColor.AQUA);
			
			player.disconnect(message.create());
			return;
			
		}
		
		player.getServer().getInfo().sendData(Main.channel, array.toByteArray(), true);
		
	}
	
	public static Map<UUID, Integer> getAlts(UUID uuid) throws SQLException {
		
		String[] ips = getField(uuid, "ip").split(";");
		Map<UUID, Integer> alts = new HashMap<>();
		
		for (String ip : ips) {
			
			try (Connection connection = DatabaseManager.getConnection();
			Statement statement = connection.createStatement();
			ResultSet users = statement.executeQuery("SELECT uuid FROM users WHERE ip LIKE '%" + ip + "%';")) {
				
				while (users.next()) {
					
					UUID userUUID = UUID.fromString(users.getString("uuid"));
					
					if (uuid.equals(userUUID)) continue;
					
					alts.put(userUUID, alts.containsKey(userUUID) ? alts.get(userUUID) + 1 : 1);
					
				}
				
			}
			
		}
		
		return alts;
		
	}
	
	private static void error(LoginEvent event) {
		
		event.setCancelReason("§8⫸ §7An error has occured while connecting to §a" + Main.name + " §8⫷\n\n§7Please try again later or contact us on Twitter §b" + Main.twitter);
		event.setCancelled(true);
		
	}
	
	private static void error(PostLoginEvent event) {
		
		ProxiedPlayer player = event.getPlayer();
		errorKick(player);
		
	}
	
	private static void error(ServerConnectedEvent event) {
		
		ProxiedPlayer player = event.getPlayer();
		errorKick(player);
		
	}
	
	private static void errorKick(ProxiedPlayer player) {
		
		ComponentBuilder message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
		.append("An error has occured while connecting to ").color(ChatColor.GRAY)
		.append(Main.name).color(ChatColor.GREEN)
		.append(" ⫷\n\n").color(ChatColor.DARK_GRAY)
		.append("Please try again later or contact us on Twitter ").color(ChatColor.GRAY)
		.append(Main.twitter).color(ChatColor.AQUA);
		
		player.disconnect(message.create());
		
	}
	
	@EventHandler
	public void onPing(ProxyPingEvent event) {
		
		ServerPing response = new ServerPing();
		
		ServerPing.Protocol version = new ServerPing.Protocol("§61.8 §7only", 47);
		ServerPing.PlayerInfo[] playersInfo = new ServerPing.PlayerInfo[3];
		
		playersInfo[0] = new ServerPing.PlayerInfo(" ", UUID.randomUUID());
		playersInfo[1] = new ServerPing.PlayerInfo(" ", UUID.randomUUID());
		playersInfo[2] = new ServerPing.PlayerInfo(" ", UUID.randomUUID());
		
		ServerPing.Players players = new ServerPing.Players(500, proxy.getOnlineCount(), playersInfo);
		
		ComponentBuilder description = new ComponentBuilder("Test");
		
		response.setDescriptionComponent(description.create()[0]);
		response.setPlayers(players);
		response.setVersion(version);
		
		try {
			
			BufferedImage image = ImageIO.read(new File("server-icon.png"));
			Favicon favicon = Favicon.create(image);
			response.setFavicon(favicon);
			
		} catch (IOException exception) {}
		
		event.setResponse(response);
		
	}
	
	@EventHandler
	public void onLogin(LoginEvent event) {
		
		PendingConnection connection = event.getConnection();
		UUID uuid = connection.getUniqueId();
		String name = connection.getName();
		String address = connection.getAddress().getAddress().getHostAddress();
		
		long time = new Date().getTime();
		
		try {
			
			boolean isNew = !DatabaseManager.exist("users", "uuid", uuid.toString(), -1);
		
			if (isNew) {
				
				DatabaseManager.updateQuery("INSERT INTO users (`name`, `uuid`, `ip`, `firstJoin`, `lastJoin`, `lastQuit`, `rank`) VALUES ('" + name + "', '" + uuid.toString() + "', '" + address + ";', " + time + ", 0, 0, '" + Rank.PLAYER.toString() + "');");
				
			} else {
				
				DatabaseManager.updateQuery("UPDATE users SET name = '" + name + "' WHERE uuid = '" + uuid.toString() + "';");
				
				String rawAddresses = getField(uuid, "ip");
				List<String> addresses = Arrays.asList(rawAddresses.split(";"));
				
				if (!addresses.contains(address)) {
					
					rawAddresses += address + ";";
					
					DatabaseManager.updateQuery("UPDATE users SET ip = '" + rawAddresses + "' WHERE uuid = '" + uuid.toString() + "';");
					
				}
				
			}
			
			int id = Integer.valueOf(getField(uuid, "id"));
			
			if (!DatabaseManager.exist("settings", "id", null, id)) DatabaseManager.updateQuery("INSERT INTO settings (`id`, `players`, `chat`, `mentions`, `messages`, `ignored`) VALUES (" + id + ", 1, 1, 1, 1, '');");
				
			int isBanned = PunishmentsManager.isPunished(uuid, Punishment.BAN);
			Set<UUID> isIPBanned = PunishmentsManager.isIPBanned(uuid);
			
			if (isBanned >= 0) PunishmentsManager.kick(event, isBanned);
			
			if (!isIPBanned.isEmpty()) PunishmentsManager.kick(event, isIPBanned, Punishment.BAN);
		
		} catch (SQLException exception) {
			
			error(event);
			return;
			
		}
		
	}
	
	@EventHandler
	public void onJoin(PostLoginEvent event) {
		
		ProxiedPlayer player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		ServerInfo server = player.getServer().getInfo();
		
		long time = new Date().getTime();
		
		try { 
			
			DatabaseManager.updateQuery("UPDATE users SET lastJoin = " + time + " WHERE uuid = '" + uuid.toString() + "';");
			PermissionsManager.setPermissions(player);

		} catch (SQLException exception) {	
			
			error(event);
			return;
			
		}
		
		if (!player.hasPermission("proxy.cmdspy")) return;
			
		Main.cmdspy.put(uuid, server);
		Main.socialspy.put(uuid, server);
		
	}
	
	@EventHandler
	public void onLeave(PlayerDisconnectEvent event) {
		
		ProxiedPlayer player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		
		long time = new Date().getTime();
		
		PermissionsManager.clearPermissions(player);
		
		try {
			
			DatabaseManager.updateQuery("UPDATE users SET lastQuit = " + time + " WHERE uuid = '" + uuid.toString() + "';");
			
		} catch (SQLException exception) {
			
			proxy.getLogger().severe("[PlayersManager] Unable to update last quit date of player '" + uuid.toString() + "'.");
			
		}
		
	}
	
	@EventHandler
	public void onConnect(ServerConnectedEvent event) {
		
		ProxiedPlayer player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		String name = player.getName();
		Set<UUID> alts;
		
		if (Main.cmdspy.containsKey(uuid) && Main.cmdspy.get(uuid) != null) Main.cmdspy.put(uuid, event.getServer().getInfo());
		if (Main.socialspy.containsKey(uuid) && Main.socialspy.get(uuid) != null) Main.socialspy.put(uuid, event.getServer().getInfo());

		try {
			
			alts = getAlts(uuid).keySet();
			
			if (alts.isEmpty()) return;
			
			ComponentBuilder message = Main.getPrefix()
			.append(name).color(ChatColor.GREEN)
			.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
					new ComponentBuilder("More informations about ").color(ChatColor.GRAY)
					.append(name).color(ChatColor.GREEN)
					.append(".").color(ChatColor.GRAY).create()))
			.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/info " + name))
			.append(" is possibly an alt of ").retain(FormatRetention.NONE).color(ChatColor.GRAY);
	
			Iterator<UUID> iterator = alts.iterator();
			
			while (iterator.hasNext()) {
				
				UUID alt = iterator.next();
				iterator.remove();
				
				String altName = getField(alt, "name");
				
				message.append(altName).color(ChatColor.GREEN)
				.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						new ComponentBuilder("More informations about ").color(ChatColor.GRAY)
						.append(altName).color(ChatColor.GREEN)
						.append(".").color(ChatColor.GRAY).create()))
				.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/info " + altName));
				
				if (alts.size() < 1) continue;
				
				message.append(", ").retain(FormatRetention.NONE).color(ChatColor.GRAY);
				
			}
			
			BaseComponent[] alert = message.append(".").retain(FormatRetention.NONE).color(ChatColor.GRAY).create();
			proxy.getPlayers().stream().filter(p -> p.hasPermission("proxy.alerts")).forEach(p -> p.sendMessage(alert));
		
		} catch (SQLException exception) {
			
			error(event);
			return;
			
		}
		
	}
	
	@EventHandler
	public void onChat(ChatEvent event) {
		
		net.md_5.bungee.api.connection.Connection sender = event.getSender();
		
		if (!(sender instanceof ProxiedPlayer)) return;
		
		ProxiedPlayer player = (ProxiedPlayer) sender;
		UUID uuid = player.getUniqueId();
		ServerInfo server = player.getServer().getInfo();
		Rank rank;
		
		try {
			
			rank = getRank(uuid);
			
		} catch (SQLException exception) {
			
			ComponentBuilder message = Main.getPrefix().append("An error has occured while sending your message. Please try again later.");
			
			player.sendMessage(message.create());
			event.setCancelled(true);
			return;
			
		}
		
		if (!player.hasPermission("proxy.chat")) {
		
			if (!cooldown.add(uuid)) {
				
				event.setCancelled(true);
				return;
				
			}
			
			proxy.getScheduler().schedule(Main.plugin, new Runnable() {
	
				public void run() {
					
					cooldown.remove(uuid);
					
				}
				
			}, 2, TimeUnit.SECONDS);
		
		}
		
		if (event.isCommand()) {
			
			String command = event.getMessage().substring(1).split(" ")[0];
			
			if (!hiddenCommands.contains(command.toLowerCase()) && rank != Rank.ADMIN) {
				
				BaseComponent[] message = new ComponentBuilder(player.getName()).color(ChatColor.DARK_BLUE)
				.append(" ⫸ ").color(ChatColor.DARK_GRAY)
				.append(event.getMessage()).color(ChatColor.GRAY).italic(true).create();
				
				Main.cmdspy.keySet().stream().filter(u -> !u.equals(uuid) && (Main.cmdspy.get(u).equals(server) || Main.cmdspy.get(u) == null) && proxy.getPlayer(u) != null).forEach(u -> proxy.getPlayer(u).sendMessage(message));
				
			}
			
		}

		if (Main.muted.contains(null) || Main.muted.contains(server)) {
			
			ComponentBuilder message = Main.getPrefix().append("The chat is currently muted.").color(ChatColor.GRAY);
			
			event.setCancelled(true);
			player.sendMessage(message.create());
			return;
			
		}
		
	}

}
