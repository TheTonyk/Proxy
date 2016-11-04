package com.thetonyk.Proxy.Commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.thetonyk.Proxy.Main;
import com.thetonyk.Proxy.Managers.DatabaseManager;
import com.thetonyk.Proxy.Managers.PermissionsManager.Rank;
import com.thetonyk.Proxy.Managers.PlayersManager;
import com.thetonyk.Proxy.Managers.Settings;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class IgnoreCommand extends Command implements TabExecutor {
	
	public IgnoreCommand() {
		
		super("ignore", "proxy.ignore", "block");
		
	}
	
	public void execute(CommandSender sender, String[] args) {
		
		ProxiedPlayer player = (ProxiedPlayer) sender;
		
		try {
			
			Settings settings = Settings.getSettings(player.getUniqueId());
			
			if (args.length < 1) {
				
				ComponentBuilder message = Main.getPrefix().append("Usage: /ignore <player>").color(ChatColor.GRAY);
				sender.sendMessage(message.create());
				
				Set<UUID> ignored = new HashSet<>(settings.getIgnored());
				
				if (!ignored.isEmpty()) {
					
					ComponentBuilder ignoredPlayers = Main.getPrefix().append("Ignored player(s): ").color(ChatColor.GRAY);
					
					Iterator<UUID> iterator = ignored.iterator();
					
					while (iterator.hasNext()) {
						
						UUID ignoredUUID = iterator.next();
						String name = PlayersManager.getField(ignoredUUID, "name");
						
						ignoredPlayers.append("'").color(ChatColor.GRAY)
						.append(name).color(ChatColor.GREEN)
						.append("'").color(ChatColor.GRAY);
						iterator.remove();
						
						if (ignored.size() < 1) continue;
						
						ignoredPlayers.append(", ").color(ChatColor.GRAY);
						
					}
					
					ignoredPlayers.append(".").color(ChatColor.GRAY);
					sender.sendMessage(ignoredPlayers.create());
					
				}
				
				return;
				
			}
			
			UUID toIgnore;
			
			try (Connection connection = DatabaseManager.getConnection();
			Statement statement = connection.createStatement();
			ResultSet query = statement.executeQuery("SELECT uuid FROM users WHERE name = '" + args[0] + "';")) {
				
				if (query.next()) toIgnore = UUID.fromString(query.getString("uuid"));
				else {
					
					ComponentBuilder message = Main.getPrefix()
					.append("The player '").color(ChatColor.GRAY)
					.append(args[0]).color(ChatColor.GREEN)
					.append("' is not know on this server.").color(ChatColor.GRAY);
					
					sender.sendMessage(message.create());
					return;
					
				}
				
			}
			
			if (sender.getName().equalsIgnoreCase(args[0])) {
				
				ComponentBuilder message = Main.getPrefix().append("You can't ignore yourself.").color(ChatColor.GRAY);
				sender.sendMessage(message.create());
				return;
				
			}
			
			Rank rank = PlayersManager.getRank(toIgnore);
			
			if (rank == Rank.ADMIN || rank == Rank.STAFF || rank == Rank.MOD) {
				
				ComponentBuilder message = Main.getPrefix().append("You can't ignore this player.").color(ChatColor.GRAY);
				sender.sendMessage(message.create());
				return;
				
			}
			
			Set<UUID> ignored = settings.getIgnored();
			
			if (ignored.remove(toIgnore)) {
				
				ComponentBuilder message = Main.getPrefix()
				.append("The player '").color(ChatColor.GRAY)
				.append(args[0]).color(ChatColor.GREEN)
				.append("' will no longer be ignored.").color(ChatColor.GRAY);
				
				sender.sendMessage(message.create());
				
			} else {
			
				ignored.add(toIgnore);
				
				ComponentBuilder message = Main.getPrefix()
				.append("The player '").color(ChatColor.GRAY)
				.append(args[0]).color(ChatColor.GREEN)
				.append("' will now be ignored.").color(ChatColor.GRAY);
				
				sender.sendMessage(message.create());
				
			}
			
			settings.setIgnored(ignored);
		
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
				try (Connection connection = DatabaseManager.getConnection();
				Statement statement = connection.createStatement();
				ResultSet query = statement.executeQuery("SELECT name, rank FROM users;")) {
					
					while (query.next()) {
						
						String name = query.getString("name");
						Rank rank = Rank.valueOf(query.getString("rank"));
						
						if (rank == Rank.ADMIN || rank == Rank.STAFF || rank == Rank.MOD) continue;
						
						if (sender.getName().equalsIgnoreCase(name)) continue;
						
						suggestions.add(name);
						
					}
					
				} catch (SQLException exception) {return new ArrayList<>();}
				
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
