package com.thetonyk.Proxy.Managers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.thetonyk.Proxy.Main;
import com.thetonyk.Proxy.Managers.PermissionsManager.Rank;
import com.thetonyk.Proxy.Utils.DateUtils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;

public class PunishmentsManager {

	private static ProxyServer proxy = ProxyServer.getInstance();
	
	public static boolean punish(UUID uuid, Punishment type, long duration, UUID operator, Reasons reason, String server) throws SQLException {
		
		int id = Integer.valueOf(PlayersManager.getField(uuid, "id"));
		int operatorId = operator == null ? 0 : Integer.valueOf(PlayersManager.getField(operator, "id"));
		long time = new Date().getTime();
		
		DatabaseManager.updateQuery("INSERT INTO punishments (`type`, `player`, `date`, `duration`, `operator`, `reason`, `server`, `cancelled`) VALUES ('" + type.toString() + "', " + id + ", " + time + ", " + duration + ", " + operatorId + ", '" + reason.toString() + "', '" + server + "', -1);");
		
		ProxiedPlayer player = proxy.getPlayer(uuid);
		ProxiedPlayer sender = proxy.getPlayer(operator);
		String name = PlayersManager.getField(uuid, "name");
		
		if (player != null && type != Punishment.MUTE) {
			
			BaseComponent[] message = getMessage(reason, type, duration);
			
			player.disconnect(message);
			
		}
		
		if (!type.withDuration) return true;
		
		BaseComponent[] announce = getAnnouncement(name, reason, type, duration);
		Collection<ProxiedPlayer> onlines = new ArrayList<>();
		
		if (player != null) {
			
			onlines = player.getServer().getInfo().getPlayers();
			onlines.stream().forEach(p -> p.sendMessage(announce));
			
		}
		
		if (sender != null && (player == null || !player.getServer().equals(sender.getServer()))) {
			
			sender.sendMessage(announce);
			
		}
		
		if (type != Punishment.BAN) return true;
		
		Map<UUID, Integer> alts = PlayersManager.getAlts(uuid);
		BaseComponent[] IPMessage = getIPMessage(uuid, type);
		
		for (UUID alt : alts.keySet()) {
			
			ProxiedPlayer online = proxy.getPlayer(alt);
			Rank rank = PlayersManager.getRank(alt);
			
			if (online == null || rank == Rank.ADMIN) return true;
			
			online.disconnect(IPMessage);
			
		}
		
		return true;
		
	}
	
	public static void cancel(int id, int operator) throws SQLException {
		
		DatabaseManager.updateQuery("UPDATE punishments SET cancelled =  " + operator + " WHERE id = " + id + ";");
		
	}
	
	public static int isPunished(UUID uuid, Punishment type) throws SQLException {
		
		int player = Integer.valueOf(PlayersManager.getField(uuid, "id"));
		long now = new Date().getTime();
		
		try (Connection connection = DatabaseManager.getConnection();
		Statement statement = connection.createStatement();
		ResultSet query = statement.executeQuery("SELECT * FROM punishments WHERE player = " + player + " AND type = '" + type.toString() + "' ORDER BY date;")) {
			
			while (query.next()) {
				
				int id = query.getInt("id");
				long duration = query.getLong("duration");
				long date = query.getLong("date");
				int cancelled = query.getInt("cancelled");
				
				if (duration >= 0 && date + duration < now) continue;
				
				if (cancelled > 0) continue;
				else return id;
				
			}
			
		}
		
		return -1;
		
	}
	
	public static Set<UUID> isIPBanned(UUID uuid) throws SQLException {
		
		Set<UUID> alts = PlayersManager.getAlts(uuid).keySet();
		Iterator<UUID> iterator = alts.iterator();
		
		while (iterator.hasNext()) {
			
			if (isPunished(iterator.next(), Punishment.BAN) < 0) iterator.remove();
			
		}
		
		return alts;
		
	}
	
	public static List<Integer> getAllPunishments(UUID uuid, Punishment type) throws SQLException {
		
		List<Integer> punishments = new ArrayList<>();
		int id = Integer.valueOf(PlayersManager.getField(uuid, "id"));
		
		try (Connection connection = DatabaseManager.getConnection();
		Statement statement = connection.createStatement();
		ResultSet query = statement.executeQuery("SELECT id FROM punishments WHERE player = " + id + " " + (type != null ? "AND type = '" + type.toString() + "' " : "") + "ORDER BY date DESC;")) {
			
			while (query.next()) {
				
				punishments.add(query.getInt("id"));
				
			}
			
		}
		
		return punishments;
		
	}
	
	public static void kick(LoginEvent event, int id) throws NumberFormatException, SQLException {
		
		Reasons reason = Reasons.valueOf(PunishmentsManager.getField(id, "reason"));
		Punishment type = Punishment.valueOf(PunishmentsManager.getField(id, "type"));
		long duration = Long.valueOf(PunishmentsManager.getField(id, "duration"));
		String message = PunishmentsManager.getBanMessage(reason, type, duration);
		
		event.setCancelReason(message);
		event.setCancelled(true);
		
	}
	
	public static void kick(LoginEvent event, Set<UUID> dueTo, Punishment type) throws NumberFormatException, SQLException {
		
		String message = PunishmentsManager.getIPBanMessage(dueTo, type);
		
		event.setCancelReason(message);
		event.setCancelled(true);
		
	}
	
	public static String getField(int id, String field) throws SQLException {
		
		try (Connection connection = DatabaseManager.getConnection();
		Statement statement = connection.createStatement();
		ResultSet query = statement.executeQuery("SELECT " + field + " FROM punishments WHERE id = " + id + ";")) {
			
			if (!query.next()) return null;
			
			return query.getString(field);
			
		}
		
	}
	
	private static BaseComponent[] getAnnouncement(String name, Reasons reason, Punishment type, long duration) {
		
		String time = duration < 0 ? "ever" : DateUtils.toText(duration, false);
		
		ComponentBuilder message = Main.getPrefix()
		.append("'").color(ChatColor.GRAY)
		.append(name).color(ChatColor.GOLD)
		.append("' was " + type.getVerb().toLowerCase() + " for ").color(ChatColor.GRAY)
		.append(reason.getName()).color(ChatColor.GOLD)
		.append(" (").color(ChatColor.GRAY)
		.append(time).color(ChatColor.GREEN)
		.append(")").color(ChatColor.GRAY);
		
		return message.create();
		
	}
	
	private static BaseComponent[] getMessage(Reasons reason, Punishment type, long duration)  {
		
		String time = duration < 0 ? "ever" : DateUtils.toText(duration, false);
		
		ComponentBuilder message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
		.append("You are ").color(ChatColor.GRAY)
		.append(type.getVerb().toLowerCase()).color(ChatColor.GOLD)
		.append(" from ").color(ChatColor.GRAY)
		.append(Main.NAME).color(ChatColor.GREEN)
		.append(" ⫷\n\n").color(ChatColor.DARK_GRAY)
		.append("Reason ").color(ChatColor.GOLD)
		.append("⫸ ").color(ChatColor.DARK_GRAY)
		.append(reason.getName()).color(ChatColor.GRAY);
		
		if (!type.withDuration()) {
			
			message.append("\n\n⫸ ").color(ChatColor.DARK_GRAY)
			.append("This is not a ban").color(ChatColor.GRAY)
			.append(" ⫷").color(ChatColor.DARK_GRAY);
			
		} else {
			
			message.append("\nExpire ").color(ChatColor.GOLD)
			.append("⫸ ").color(ChatColor.DARK_GRAY)
			.append(time).color(ChatColor.GRAY)
			.append("\n\n⫸ ").color(ChatColor.DARK_GRAY)
			.append("To appeal, contact us on Twitter ").color(ChatColor.GRAY)
			.append(Main.TWITTER).color(ChatColor.AQUA)
			.append(" ⫷").color(ChatColor.DARK_GRAY);
			
		}
		
		return message.create();
		
	}
	
	private static String getBanMessage(Reasons reason, Punishment type, long duration) {
		
		String time = duration < 0 ? "ever" : DateUtils.toText(duration, false);
		String message = "§8⫸ §7You are §6" + type.getVerb().toLowerCase() + "§7 from §a" + Main.NAME + " §8⫷\n\n§6Reason §8⫸ §7" + reason.getName();
		
		if (!type.withDuration()) message += "§8\n\n⫸ §7This is not a ban §8⫷";
		else message += "§6\nExpire §8⫸ §7" + time + "§8\n\n⫸ §7To appeal, contact us on Twitter §b" + Main.TWITTER + " §8⫷";
		
		return message;
		
	}
	
	private static BaseComponent[] getIPMessage(UUID dueTo, Punishment type) throws SQLException  {
		
		String name = PlayersManager.getField(dueTo, "name");
		
		ComponentBuilder message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
		.append("You are IP ").color(ChatColor.GRAY)
		.append(type.getVerb().toLowerCase()).color(ChatColor.GOLD)
		.append(" from ").color(ChatColor.GRAY)
		.append(Main.NAME).color(ChatColor.GREEN)
		.append(" ⫷\n\n").color(ChatColor.DARK_GRAY)
		.append("Due to account(s) ").color(ChatColor.GOLD)
		.append("⫸ ").color(ChatColor.DARK_GRAY)
		.append(name).color(ChatColor.GRAY);
		
		if (!type.withDuration()) {
			
			message.append("\n\n⫸ ").color(ChatColor.DARK_GRAY)
			.append("This is not a ban").color(ChatColor.GRAY)
			.append(" ⫷").color(ChatColor.DARK_GRAY);
			
		} else {
			
			message.append("\n\n⫸ ").color(ChatColor.DARK_GRAY)
			.append("To appeal, contact us on Twitter ").color(ChatColor.GRAY)
			.append(Main.TWITTER).color(ChatColor.AQUA)
			.append(" ⫷").color(ChatColor.DARK_GRAY);
			
		}
		
		return message.create();
		
	}
	
	private static String getIPBanMessage(Set<UUID> dueTo, Punishment type) throws SQLException {
		
		String message = "§8⫸ §7You are IP §6" + type.getVerb().toLowerCase() + " §7from §a" + Main.NAME + " §8⫷\n\n§6Due to account(s) §8⫸ §7";
		
		for (UUID player : dueTo) {
			
			String name = PlayersManager.getField(player, "name");
			message += name + ", ";
			
		}
		
		message = message.substring(0, message.length() - 2);
		
		if (!type.withDuration()) message += "§8\n\n⫸ §7This is not a ban §8⫷";
		else message += "§8\n\n⫸ §7To appeal, contact us on Twitter §b" + Main.TWITTER + " §8⫷";
		
		return message;
		
	}
	
	public static List<BaseComponent[]> getClickableReasons(String name, Punishment type) {
		
		List<BaseComponent[]> clickable = new ArrayList<>();
		List<Reasons> reasons = Arrays.stream(Reasons.values()).filter(r -> Arrays.asList(r.getPunishments()).contains(type)).collect(Collectors.toList());
		
		for (Reasons reason : reasons) {
			
			ComponentBuilder message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
			.append(reason.getName()).color(ChatColor.GOLD).italic(true)
			.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
					new ComponentBuilder("Click to choose.").color(ChatColor.GRAY).create()))
			.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + type.getShortName() + " " + name + " " + reason.name()));
			
			clickable.add(message.create());
			
		}
		
		return clickable;
		
	}
	
	public static List<BaseComponent[]> getClickableTimes(String name, Punishment type, Reasons reason, String... times) {
		
		List<BaseComponent[]> clickable = new ArrayList<>();
		
		for (String time : times) {
			
			long millis = DateUtils.parseDateDiff(time);
			
			ComponentBuilder message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
			.append("for " + (millis < 0 ? "ever" : DateUtils.toText(millis, false))).color(ChatColor.GOLD).italic(true)
			.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
					new ComponentBuilder("Click to choose.").color(ChatColor.GRAY).create()))
			.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + type.getShortName() + " " + name + " " + reason.name() + " " + time));
			
			clickable.add(message.create());
			
		}
		
		return clickable;
		
	}
	
	public enum Punishment {
		
		BAN("Banned", "Ban", true), KICK("Kicked", "Kick", false), MUTE("Muted", "Mute", true);
		
		private String verb;
		private String shortName;
		private boolean withDuration;
		
		private Punishment(String verb, String shortName, boolean withDuration) {
			
			this.verb = verb;
			this.shortName = shortName;
			this.withDuration = withDuration;
			
		}
		
		public String getVerb() {
			
			return this.verb;
			
		}
		
		public String getShortName() {
			
			return this.shortName;
			
		}
		
		public boolean withDuration() {
			
			return this.withDuration;
			
		}
		
	}
	
	public enum Reasons {
		
		FORCEFIELD("Forcefield/Aimbot", "Forcefield", false, Punishment.BAN),
		FLY("Flyhack/Speedhack", "Speed/Fly", false, Punishment.BAN),
		XRAY("Xray/Cave Finder", "Xray", false, Punishment.BAN),
		AUTOCLIC("Autoclic/Macro", "Autoclic", false, Punishment.BAN),
		TEAM("Teaming in FFA", "Teaming", true, Punishment.BAN, Punishment.KICK),
		LANGUAGE("Bad language and/or spam", "Chat Rules", true, Punishment.BAN, Punishment.KICK, Punishment.MUTE),
		ALT("Alt Account for evading", "Alt account", false, Punishment.BAN),
		SPAM("Spam", "Spam", true, Punishment.MUTE),
		SPOIL("Spoiling when dead", "Spoil", true, Punishment.MUTE),
		INSULTS("Insults, disrespect and/or provocation", "Insults", true, Punishment.MUTE),
		HACKUSATION("Excessive hackusations", "Hackusations", true, Punishment.MUTE);
		
		private String name;
		private String shortName;
		private boolean temp;
		private Punishment[] type;
		
		private Reasons(String name, String shortName, boolean temp, Punishment... type) {
			
			this.name = name;
			this.shortName = shortName;
			this.temp = temp;
			this.type = type;
			
		}
		
		public String getName() {
			
			return this.name;
			
		}
		
		public String getShortName() {
			
			return this.shortName;
			
		}
		
		public boolean getTemp() {
			
			return this.temp;
			
		}
		
		public Punishment[] getPunishments() {
			
			return this.type;
			
		}
		
	}
	
}
