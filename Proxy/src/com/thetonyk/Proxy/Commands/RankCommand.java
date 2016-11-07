package com.thetonyk.Proxy.Commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.thetonyk.Proxy.Main;
import com.thetonyk.Proxy.Managers.PermissionsManager.Rank;
import com.thetonyk.Proxy.Managers.PlayersManager;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class RankCommand extends Command implements TabExecutor {
	
	private static ProxyServer proxy = ProxyServer.getInstance();
	
	public RankCommand() {
		
		super("rank", "proxy.rank");
		
	}

	public void execute(CommandSender sender, String[] args) {
		
		if (args.length < 2) {
			
			ComponentBuilder message = Main.getPrefix().append("Usage: /rank <player> <rank>").color(ChatColor.GRAY);
			sender.sendMessage(message.create());
			return;
			
		}
		
		try {
			
			UUID uuid = PlayersManager.getUUID(args[0]);
			ProxiedPlayer player = proxy.getPlayer(uuid);
			
			if (uuid == null) {
				
				ComponentBuilder message = Main.getPrefix()
				.append("The player '").color(ChatColor.GRAY)
				.append(args[0]).color(ChatColor.GREEN)
				.append("' is not know on this server.").color(ChatColor.GRAY);
				
				sender.sendMessage(message.create());
				return;
				
			}
			
			Rank rank = Rank.valueOf(args[1].toUpperCase());
			
			PlayersManager.setRank(uuid, rank);
			
			if (!sender.getName().equalsIgnoreCase(player.getName()) && player != null) {
					
					ComponentBuilder message = Main.getPrefix()
					.append("Your rank has been set to '").color(ChatColor.GRAY)
					.append(rank.name().toLowerCase()).color(ChatColor.GOLD)
					.append("'.").color(ChatColor.GRAY);
					
					player.sendMessage(message.create());
					
					message = Main.getPrefix()
					.append("The rank of the player '").color(ChatColor.GRAY)
					.append(player.getName()).color(ChatColor.GREEN)
					.append("' has been set to '").color(ChatColor.GRAY)
					.append(rank.name().toLowerCase()).color(ChatColor.GOLD)
					.append("'.").color(ChatColor.GRAY);
					
					sender.sendMessage(message.create());
					return;
				
			}
			
			ComponentBuilder message = Main.getPrefix()
			.append("Your rank has been set to '").color(ChatColor.GRAY)
			.append(rank.name().toLowerCase()).color(ChatColor.GOLD)
			.append("'.").color(ChatColor.GRAY);
			
			sender.sendMessage(message.create());
			return;
			
		} catch (IllegalArgumentException exception) {
			
			ComponentBuilder message = Main.getPrefix()
			.append("The rank '").color(ChatColor.GRAY)
			.append(args[1]).color(ChatColor.GOLD)
			.append("' doesn't exist. Use tab complete.").color(ChatColor.GRAY);
			
			sender.sendMessage(message.create());
			return;
			
		} catch (SQLException exception) {
			
			ComponentBuilder message = Main.getPrefix().append("An error has occured while processing the command. Please try again later.").color(ChatColor.GRAY);
			sender.sendMessage(message.create());
			return;
			
		}
		
	}

	public List<String> onTabComplete(CommandSender sender, String[] args) {
		
		List<String> suggestions = new ArrayList<>();
		
		switch (args.length) {
		
			case 1:
				Set<String> players = new HashSet<>();
				
				proxy.getPlayers().stream().forEach(p -> players.add(p.getName()));
				suggestions.addAll(players);
				break;
			case 2:
				Set<String> ranks = new HashSet<>();
				
				Arrays.stream(Rank.values()).forEach(r -> ranks.add(r.name().toLowerCase()));
				suggestions.addAll(ranks);
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
