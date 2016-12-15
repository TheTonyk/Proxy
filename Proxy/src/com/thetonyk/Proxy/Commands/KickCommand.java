package com.thetonyk.Proxy.Commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.thetonyk.Proxy.Main;
import com.thetonyk.Proxy.Managers.DatabaseManager;
import com.thetonyk.Proxy.Managers.PermissionsManager.Rank;
import com.thetonyk.Proxy.Managers.PlayersManager;
import com.thetonyk.Proxy.Managers.PunishmentsManager;
import com.thetonyk.Proxy.Managers.PunishmentsManager.Punishment;
import com.thetonyk.Proxy.Managers.PunishmentsManager.Reasons;
import com.thetonyk.Proxy.Utils.DateUtils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class KickCommand extends Command implements TabExecutor {

	private static ProxyServer proxy = ProxyServer.getInstance();
	public static Punishment type = Punishment.KICK;
	
	public KickCommand() {
		
		super(type.getShortName().toLowerCase(), "proxy.punish");
		
	}
	
	public void execute(CommandSender sender, String[] args) {
		
		UUID operator = null;
		
		if (sender instanceof ProxiedPlayer) {
			
			ProxiedPlayer player = (ProxiedPlayer) sender;
			operator = player.getUniqueId();
			
		}
		
		if (args.length < 1) {
			
			ComponentBuilder message = Main.getPrefix().append("Usage: /" + type.getShortName().toLowerCase() + " <player>").color(ChatColor.GRAY);
			sender.sendMessage(message.create());
			return;
			
		}
		
		try (Connection connection = DatabaseManager.getConnection();
		Statement statement = connection.createStatement();
		ResultSet query = statement.executeQuery("SELECT * FROM users WHERE name = '" + args[0] + "';")) {
			
			if (!query.next()) {
				
				ComponentBuilder message = Main.getPrefix()
				.append("The player '").color(ChatColor.GRAY)
				.append(args[0]).color(ChatColor.GREEN)
				.append("' is not know on this server.").color(ChatColor.GRAY);
				
				sender.sendMessage(message.create());
				return;
				
			}
			
			String name = query.getString("name");
			UUID uuid = UUID.fromString(query.getString("uuid"));
			Rank rank = Rank.valueOf(query.getString("rank"));
			ProxiedPlayer punished = proxy.getPlayer(uuid);
			Rank senderRank = operator == null ? Rank.ADMIN : PlayersManager.getRank(operator);
			
			if (rank == Rank.ADMIN && senderRank != Rank.ADMIN) return;
			
			if (punished == null && !type.withDuration()) {
				
				ComponentBuilder message = Main.getPrefix()
				.append("The player '").color(ChatColor.GRAY)
				.append(name).color(ChatColor.GREEN)
				.append("' is not online.").color(ChatColor.GRAY);
				
				sender.sendMessage(message.create());
				return;
				
			}
			
			if (senderRank != Rank.ADMIN && rank != Rank.PLAYER && type == Punishment.BAN) {
				
				ComponentBuilder message = Main.getPrefix()
				.append("The player '").color(ChatColor.GRAY)
				.append(name).color(ChatColor.GREEN)
				.append("' can only be " + type.getVerb().toLowerCase() + " by an ").color(ChatColor.GRAY)
				.append("Admin").color(ChatColor.DARK_RED)
				.append(".").color(ChatColor.GRAY);
				
				sender.sendMessage(message.create());
				return;
				
			}
			
			if (PunishmentsManager.isPunished(uuid, type) > -1) {
				
				ComponentBuilder message = Main.getPrefix()
				.append("The player '").color(ChatColor.GRAY)
				.append(name).color(ChatColor.GREEN)
				.append("' is already " + type.getVerb().toLowerCase() + ".").color(ChatColor.GRAY);
				
				sender.sendMessage(message.create());
				return;
				
			}
			
			if (args.length == 1) {
			
				ComponentBuilder message = Main.getPrefix()
				.append(type.getShortName() + " of '").color(ChatColor.GRAY)
				.append(name).color(ChatColor.GREEN)
				.append("'...").color(ChatColor.GRAY);
				
				sender.sendMessage(message.create());
				
				message = Main.getPrefix()
				.append("Choose the reason: ").color(ChatColor.GRAY)
				.append("(").color(ChatColor.DARK_GRAY)
				.append("Click on it").color(ChatColor.GRAY)
				.append(")").color(ChatColor.DARK_GRAY);
				
				sender.sendMessage(message.create());
				
				PunishmentsManager.getClickableReasons(name, type).stream().forEach(r -> sender.sendMessage(r));
				return;
				
			}
			
			Reasons reason = Reasons.valueOf(args[1]);
			
			if (args.length == 2 && type.withDuration() && reason.getTemp()) {
				
				ComponentBuilder message = Main.getPrefix()
				.append(type.getShortName() + " of '").color(ChatColor.GRAY)
				.append(name).color(ChatColor.GREEN)
				.append("' for ").color(ChatColor.GRAY)
				.append(reason.getName()).color(ChatColor.GOLD)
				.append("...").color(ChatColor.GRAY);
				
				sender.sendMessage(message.create());
				
				message = Main.getPrefix()
				.append("Choose the duration: ").color(ChatColor.GRAY)
				.append("(").color(ChatColor.DARK_GRAY)
				.append("Click on it").color(ChatColor.GRAY)
				.append(")").color(ChatColor.DARK_GRAY);
				
				sender.sendMessage(message.create());
				
				PunishmentsManager.getClickableTimes(name, type, reason, "15m", "30m", "1h", "6h", "1d", "3d", "5d", "1w", "2w", "1mo", "3mo", "ever").stream().forEach(d -> sender.sendMessage(d));
				return;
				
			}
			
			if (args.length > 2 && !args[2].equalsIgnoreCase("ever") && DateUtils.parseDateDiff(args[2]) < 0) {
				
				ComponentBuilder error = Main.getPrefix().append("The duration format is not valid.").color(ChatColor.GRAY);
				sender.sendMessage(error.create());
				return;
				
			}
			
			long duration = args.length < 3 ? -1 : DateUtils.parseDateDiff(args[2]);
			
			if ((!type.withDuration() || !reason.getTemp()) && duration > -1) {
				
				ComponentBuilder message = Main.getPrefix().append("You can't " + type.getShortName().toLowerCase() + " temporary.").color(ChatColor.GRAY);
				sender.sendMessage(message.create());
				return;
				
			}
			
			if (args.length < 4 || !args[3].equalsIgnoreCase("confirm")) {
				
				ComponentBuilder message = Main.getPrefix()
				.append("'").color(ChatColor.GRAY)
				.append(name).color(ChatColor.GREEN)
				.append("' will be " + type.getShortName().toLowerCase() + " for ").color(ChatColor.GRAY)
				.append(reason.getShortName()).color(ChatColor.GOLD);
				
				if (type.withDuration()) {
					
					message.append(" for ").color(ChatColor.GRAY)
					.append(duration < 0 ? "ever" : DateUtils.toText(duration, false)).color(ChatColor.GOLD);
					
				}
				
				message.append(".").color(ChatColor.GRAY);
				
				sender.sendMessage(message.create());
				
				message = Main.getPrefix()
				.append("Yes, " + type.getShortName().toLowerCase() + " this player !").color(ChatColor.GOLD).italic(true)
				.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
						new ComponentBuilder("Click to confirm the " + type.getShortName().toLowerCase() + ".").color(ChatColor.GRAY).create()))
				.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + type.getShortName().toLowerCase() + " " + name + " " + reason.name() + " " + (args.length < 3 ? "ever" : args[2]) + " confirm"));
				
				sender.sendMessage(message.create());
				return;
				
			}
			
			PunishmentsManager.punish(uuid, type, duration, operator, reason, (punished == null ? "" : punished.getServer().getInfo().getName()));
			
		} catch (IllegalArgumentException exception) {	
			
			ComponentBuilder error = Main.getPrefix().append("The reason is not valid.").color(ChatColor.GRAY);
			sender.sendMessage(error.create());
			return;
			
		} catch (SQLException exception) {
			
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
