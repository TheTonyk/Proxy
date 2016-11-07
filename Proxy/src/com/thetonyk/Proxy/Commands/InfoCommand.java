package com.thetonyk.Proxy.Commands;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.thetonyk.Proxy.Main;
import com.thetonyk.Proxy.Managers.DatabaseManager;
import com.thetonyk.Proxy.Managers.PermissionsManager.Rank;
import com.thetonyk.Proxy.Managers.PlayersManager;
import com.thetonyk.Proxy.Managers.PunishmentsManager.Punishment;
import com.thetonyk.Proxy.Managers.PunishmentsManager.Reasons;
import com.thetonyk.Proxy.Utils.DateUtils;
import com.thetonyk.Proxy.Managers.PunishmentsManager;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class InfoCommand extends Command implements TabExecutor {
	
	private static ProxyServer proxy = ProxyServer.getInstance();
	
	public InfoCommand() {
		
		super("info", "proxy.info");
		
	}
	
	public void execute(CommandSender sender, String[] args) {
		
		if (args.length < 1) {
			
			ComponentBuilder message = Main.getPrefix().append("Usage: /info <player>").color(ChatColor.GRAY);
			sender.sendMessage(message.create());
			return;
			
		}
		
		try {
		
			String name;
			UUID uuid;
			String[] ips;
			long firstJoin;
			long lastJoin;
			long lastQuit;
			Rank rank;
			
			try (Connection connection = DatabaseManager.getConnection();
			Statement statement = connection.createStatement();
			ResultSet query = statement.executeQuery("SELECT * FROM users WHERE name = '" + args[0] + "';")) {
				
				if (query.next()) {
				
					name = query.getString("name");
					uuid = UUID.fromString(query.getString("uuid"));
					ips = query.getString("ip").split(";");
					firstJoin = query.getLong("firstJoin");
					lastJoin = query.getLong("lastJoin");
					lastQuit = query.getLong("lastQuit");
					rank = Rank.valueOf(query.getString("rank"));
				
				} else {
					
					ComponentBuilder message = Main.getPrefix()
					.append("The player '").color(ChatColor.GRAY)
					.append(args[0]).color(ChatColor.GREEN)
					.append("' is not know on this server.").color(ChatColor.GRAY);
					
					sender.sendMessage(message.create());
					return;
					
				}
				
			}
			
			List<Integer> punishments = PunishmentsManager.getAllPunishments(uuid, null);
			int banned = PunishmentsManager.isPunished(uuid, Punishment.BAN);
			int muted = PunishmentsManager.isPunished(uuid, Punishment.MUTE);
			Map<UUID, Integer> alts = PlayersManager.getAlts(uuid);
			ProxiedPlayer player = proxy.getPlayer(uuid);
			ServerInfo server = player == null ? null : player.getServer().getInfo();
			String address = player == null ? null : player.getAddress().getAddress().getHostAddress();
			
			long time = new Date().getTime();
			
			ComponentBuilder message = Main.getPrefix()
			.append("All infos about '").color(ChatColor.GRAY)
			.append(name).color(ChatColor.GREEN)
			.append("':").color(ChatColor.GRAY);
			
			sender.sendMessage(message.create());
			
			message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
			.append("UUID: ").color(ChatColor.GRAY)
			.append(uuid.toString()).color(ChatColor.GOLD);
			
			sender.sendMessage(message.create());
			
			message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
			.append("Rank: ").color(ChatColor.GRAY);
			
			Stream<BaseComponent> part1 = Arrays.stream(message.create());
			Stream<BaseComponent> part2 = Arrays.stream(rank.getName());
 			
			sender.sendMessage(Stream.concat(part1, part2).toArray(size -> new BaseComponent[size]));
		
			message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
			.append("Status: ").color(ChatColor.GRAY)
			.append(player == null ? "Offline" : "Online").color(player == null ? ChatColor.RED : ChatColor.GREEN);
			
			sender.sendMessage(message.create());
			
			if (server != null) {
				
				message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
				.append("Server: ").color(ChatColor.GRAY)
				.append(server.getName()).color(ChatColor.GOLD);
				
				sender.sendMessage(message.create());
				
			}
			
			message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
			.append("Banned: ").color(ChatColor.GRAY);
			
			if (banned < 0) message.append("Not currently").color(ChatColor.GREEN);
			else {
				
				long date = Long.valueOf(PunishmentsManager.getField(banned, "date"));
				long duration = Long.valueOf(PunishmentsManager.getField(banned, "duration"));
				
				if (duration < 0) message.append("Lifetime").color(ChatColor.RED);
				else {
				
					String stringDuration = DateUtils.toShortText(duration - (time - date), true);
					
					message.append(stringDuration.substring(0, stringDuration.length() - 1)).color(ChatColor.RED);
					
				}
				
				message.append(".").color(ChatColor.GRAY);
				
			}
			
			sender.sendMessage(message.create());
			
			message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
			.append("Muted: ").color(ChatColor.GRAY);
			
			if (muted < 0) message.append("Not currently").color(ChatColor.GREEN);
			else {
				
				long date = Long.valueOf(PunishmentsManager.getField(muted, "date"));
				long duration = Long.valueOf(PunishmentsManager.getField(muted, "duration"));
				
				if (duration < 0) message.append("Lifetime").color(ChatColor.RED);
				else {
				
					String stringDuration = DateUtils.toShortText(duration - (time - date), true);
					
					message.append(stringDuration.substring(0, stringDuration.length() - 1)).color(ChatColor.RED);
					
				}
				
				message.append(".").color(ChatColor.GRAY);
				
			}
			
			sender.sendMessage(message.create());
			
			SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy HH:mm");
			
			message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
			.append("First Join: ").color(ChatColor.GRAY)
			.append(format.format(firstJoin)).color(ChatColor.GOLD);
			
			sender.sendMessage(message.create());
			
			message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
			.append("Last Join: ").color(ChatColor.GRAY)
			.append(lastJoin < 1 ? "Never" : format.format(lastJoin)).color(ChatColor.GOLD);
			
			sender.sendMessage(message.create());
			
			message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
			.append("Last Quit: ").color(ChatColor.GRAY)
			.append(lastQuit < 1 ? "Never" : format.format(lastQuit)).color(ChatColor.GOLD);
			
			sender.sendMessage(message.create());
			
			if (sender.hasPermission("proxy.seeips")) {
				
				message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
				.append("All IP's used by this player:").color(ChatColor.GRAY);
				
				sender.sendMessage(message.create());
				
				for (String ip : ips) {
					
					message = new ComponentBuilder("⫸   ").color(ChatColor.DARK_GRAY)
					.append(ip).color(address != null && ip.equalsIgnoreCase(address) ? ChatColor.GREEN : ChatColor.RED);
					
					sender.sendMessage(message.create());
					
				}
				
			}
			
			message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
			.append("All possible alts used by this player:").color(ChatColor.GRAY);
			
			sender.sendMessage(message.create());
			
			if (alts.isEmpty()) {
				
				message = new ComponentBuilder("⫸   ").color(ChatColor.DARK_GRAY)
				.append("This player don't have any know alts.").color(ChatColor.GRAY);
				
				sender.sendMessage(message.create());
				
			} else {
				
				for (Map.Entry<UUID, Integer> alt : alts.entrySet()) {
					
					String altName = PlayersManager.getField(alt.getKey(), "name");
					
					message = new ComponentBuilder("⫸   ").color(ChatColor.DARK_GRAY)
					.append(altName).color(ChatColor.GOLD)
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
							new ComponentBuilder("More informations about ").color(ChatColor.GRAY)
							.append(altName).color(ChatColor.GREEN)
							.append(".").color(ChatColor.GRAY).create()))
					.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/info " + altName))
					.append(" (").retain(FormatRetention.NONE).color(ChatColor.DARK_GRAY)
					.append(String.valueOf(alt.getValue())).color(ChatColor.GREEN)
					.append(" IP's common").color(ChatColor.GRAY)
					.append(")").color(ChatColor.DARK_GRAY);
					
					sender.sendMessage(message.create());
					
				}
				
			}
			
			List<PlayersManager.PlayerName> names = PlayersManager.getPreviousNames(uuid);
			Iterator<PlayersManager.PlayerName> iterator = names.iterator();
			
			message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
			.append("Previous names of this player:").color(ChatColor.GRAY);
			
			sender.sendMessage(message.create());
			
			while (iterator.hasNext()) {
				
				PlayersManager.PlayerName oldName = iterator.next();
				iterator.remove();
				
				message = new ComponentBuilder("⫸   ").color(ChatColor.DARK_GRAY)
				.append(oldName.getName()).color(ChatColor.GOLD)
				.append(" | ").color(ChatColor.DARK_GRAY);
				
				if (oldName.isFirstName()) message.append("First name").color(ChatColor.GRAY);
				else message.append(format.format(oldName.getChangeTime())).color(ChatColor.GRAY);
				
				sender.sendMessage(message.create());
				
			}
			
			if (punishments.isEmpty()) return;
			
			message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
			.append("All punishments of this player:").color(ChatColor.GRAY);
			
			sender.sendMessage(message.create());
			
			format = new SimpleDateFormat("dd'/'MM'/'yy HH:mm");
			
			for (int id : punishments) {
				
				Punishment type;
				long date;
				long duration;
				int operator;
				Reasons reason;
				int cancelled;
				
				try (Connection connection = DatabaseManager.getConnection();
				Statement statement = connection.createStatement();
				ResultSet query = statement.executeQuery("SELECT * FROM punishments WHERE id = " + id + ";")) {
					
					if (!query.next()) continue;
					
					type = Punishment.valueOf(query.getString("type"));
					date = query.getLong("date");
					duration = query.getLong("duration");
					operator = query.getInt("operator");
					reason = Reasons.valueOf(query.getString("reason"));
					cancelled = query.getInt("cancelled");
					
				}
				
				UUID operatorUUID = PlayersManager.getUUID(operator);
				String operatorName = PlayersManager.getField(operatorUUID, "name");
				
				UUID cancellerUUID = cancelled < 0 ? null : PlayersManager.getUUID(cancelled);
				String cancellerName = cancelled < 0 ? null : PlayersManager.getField(cancellerUUID, "name");
				
				message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
				.append(type.getShortName()).color(ChatColor.GOLD)
				.append(" | ").color(ChatColor.DARK_GRAY)
				.append(format.format(date)).color(ChatColor.GRAY)
				.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						new ComponentBuilder(type.getVerb() + " by ").color(ChatColor.GRAY)
						.append(operatorName).color(ChatColor.GREEN)
						.append(".").color(ChatColor.GRAY).create()))
				.append(" | ").retain(FormatRetention.NONE).color(ChatColor.DARK_GRAY)
				.append(reason.getShortName()).color(ChatColor.GREEN)
				.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						new ComponentBuilder(reason.getName()).color(ChatColor.GOLD).create()));
				
				if (type != Punishment.KICK) {
					
					message.append(" | ").retain(FormatRetention.NONE).color(ChatColor.DARK_GRAY)
					.append(duration < 0 ? "Lifetime" : format.format(date + duration)).color(ChatColor.GRAY)
					.append(" | ").color(ChatColor.DARK_GRAY)
					.append("Cancel").color(cancelled < 0 ? ChatColor.RED : ChatColor.GRAY).italic(cancelled < 0 ? false : true);
					
					if (cancelled >= 0) {
						
						message.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
								new ComponentBuilder("Cancelled by ").color(ChatColor.GRAY)
								.append(cancellerName).color(ChatColor.GREEN)
								.append(".").color(ChatColor.GRAY).create()));
						
					}
					
					if (cancelled < 0) {
						
						if (sender.hasPermission("proxy.cancelpunishments")) {
							
							message.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
									new ComponentBuilder("Cancel the ban.").color(ChatColor.GRAY).create()))
							.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/unpunish " + id));
							
						} else {
							
							message.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
									new ComponentBuilder("You don't have enough permissions to cancel the ban.").color(ChatColor.GRAY).create()));
							
						}
						
					}
				
				}
				
				sender.sendMessage(message.create());
				
			}
	
		} catch (SQLException | IOException exception) {
			
			ComponentBuilder error = Main.getPrefix().append("An error has occured while processing the command. Please try again later.").color(ChatColor.GRAY);
			sender.sendMessage(error.create());
			return;
			
		}
		
	}
	
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		
		List<String> suggestions = new ArrayList<>();
		
		switch (args.length) {
		
			case 1:
				
				Set<String> online = new HashSet<>();
				
				proxy.getPlayers().stream().forEach(p -> online.add(p.getName()));
				suggestions.addAll(online);
				break;
			default:
				break;
				
		}
		
		if (!args[args.length - 1].isEmpty()) {
			
			suggestions = suggestions.stream().filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).collect(Collectors.toList());
			
		}
		
		return suggestions;
		
	}

}
