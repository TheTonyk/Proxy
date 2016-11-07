package com.thetonyk.Proxy.Commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.thetonyk.Proxy.Main;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class SocialspyCommand extends Command implements TabExecutor {

	private static ProxyServer proxy = ProxyServer.getInstance();
	
	public SocialspyCommand() {
		
		super("socialspy", "proxy.cmdspy", "social");
		
	}
	
	public void execute(CommandSender sender, String[] args) {
		
		ProxiedPlayer player = (ProxiedPlayer) sender;
		UUID uuid = player.getUniqueId();
		ServerInfo server = player.getServer().getInfo();
		
		if (args.length > 1) {
			
			player = proxy.getPlayer(args[1]);
			
			if (player == null) {
				
				ComponentBuilder message = Main.getPrefix()
				.append("The player '").color(ChatColor.GRAY)
				.append(args[1]).color(ChatColor.GREEN)
				.append("' is not online.").color(ChatColor.GRAY);
				
				sender.sendMessage(message.create());
				return;
				
			}
			
		}
		
		if (args.length >= 1) {
			
			if (args[0].equalsIgnoreCase("all")) server = null;
			else {
			
				server = proxy.getServerInfo(args[0]);
			
				if (server == null) {
					
					ComponentBuilder message = Main.getPrefix()
					.append("'").color(ChatColor.GRAY)
					.append(args[0]).color(ChatColor.GREEN)
					.append("' is not a server.").color(ChatColor.GRAY);
					
					sender.sendMessage(message.create());
					return;
					
				}
			
			}
			
		}
			
		if (Main.socialspy.containsKey(uuid)) {
			
			Main.socialspy.remove(uuid);
			
			if (!sender.getName().equalsIgnoreCase(player.getName())) {
				
				ComponentBuilder message = Main.getPrefix()
				.append("The social spy of '").color(ChatColor.GRAY)
				.append(player.getName()).color(ChatColor.GREEN)
				.append("' has been disabled.").color(ChatColor.GRAY);
				
				sender.sendMessage(message.create());
				
			}
			
			ComponentBuilder message = Main.getPrefix().append("Your social spy has been disabled.").color(ChatColor.GRAY);
			
			player.sendMessage(message.create());
			return;
			
		}
		
		Main.socialspy.put(uuid, server);
		
		if (!sender.getName().equalsIgnoreCase(player.getName())) {
			
			ComponentBuilder message = Main.getPrefix()
			.append("The social spy of '").color(ChatColor.GRAY)
			.append(player.getName()).color(ChatColor.GREEN)
			.append("' has been enabled on server '").color(ChatColor.GRAY)
			.append(server == null ? "all" : server.getName()).color(ChatColor.GOLD)
			.append("'.").color(ChatColor.GRAY);
			
			sender.sendMessage(message.create());
			
		}
		
		ComponentBuilder message = Main.getPrefix()
		.append("Your social spy has been enabled on server '").color(ChatColor.GRAY)
		.append(server == null ? "all" : server.getName()).color(ChatColor.GOLD)
		.append("'.").color(ChatColor.GRAY);
		
		player.sendMessage(message.create());
		
	}
	
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		
		List<String> suggestions = new ArrayList<>();
		
		switch (args.length) {
		
			case 1:
				List<String> servers = new ArrayList<>();
				
				proxy.getServers().values().stream().forEach(s -> servers.add(s.getName()));

				suggestions.addAll(servers);
				suggestions.add("all");
				break;
			case 2:
				List<String> players = new ArrayList<>();
				
				proxy.getPlayers().stream().forEach(p -> players.add(p.getName()));
				
				suggestions.addAll(players);
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
