package com.thetonyk.Proxy.Commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.thetonyk.Proxy.Main;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class ChatCommand extends Command implements TabExecutor {
	
	private static ProxyServer proxy = ProxyServer.getInstance();
	
	public ChatCommand() {
		
		super("chat", "proxy.chat", "c");
		
	}
	
	public void execute(CommandSender sender, String[] args) {
		
		ProxiedPlayer player = (ProxiedPlayer) sender;
		ServerInfo server = player.getServer().getInfo();
		
		if (args.length < 1 || (!args[0].equalsIgnoreCase("clear") && !args[0].equalsIgnoreCase("mute"))) {
			
			ComponentBuilder message = Main.getPrefix().append("Usage: /chat <mute|clear> [server]").color(ChatColor.GRAY);
			sender.sendMessage(message.create());
			return;
			
		}
		
		if (args.length > 1) {
			
			Set<String> servers = proxy.getServers().keySet();
			
			if (args[1].equalsIgnoreCase("all")) {
				
				server = null;
				
			} else {
			
				if (!servers.contains(args[1])) {
					
					ComponentBuilder message = Main.getPrefix()
					.append("'").color(ChatColor.GRAY)
					.append(args[1]).color(ChatColor.GOLD)
					.append("' is not a server.").color(ChatColor.GRAY);
					
					sender.sendMessage(message.create());
					return;
					
				}
				
				server = proxy.getServerInfo(args[1]);
				
			}
			
		}
		
		if (args[0].equalsIgnoreCase("clear")) {
			
			BaseComponent[] space = new ComponentBuilder("").create();
			
			for (int i = 0; i < 100; i++) {
				
				if (server == null) proxy.broadcast(space);
				else server.getPlayers().stream().forEach(p -> p.sendMessage(space));
				
			}
			
			BaseComponent[] message = Main.getPrefix().append("The chat has been cleared.").color(ChatColor.GRAY).create();
			
			if (server == null) proxy.broadcast(message);
			else {
				
				server.getPlayers().stream().forEach(p -> p.sendMessage(message));
				
				if (!server.equals(player.getServer().getInfo())) {
					
					ComponentBuilder confirm = Main.getPrefix()
					.append("The chat has been cleared in the server '").color(ChatColor.GRAY)
					.append(server.getName()).color(ChatColor.GOLD)
					.append("'.").color(ChatColor.GRAY);
					
					player.sendMessage(confirm.create());
					
				}
				
			}
			
			return;
			
		}
		
		if (args[0].equalsIgnoreCase("mute")) {
			
			if (Main.muted.remove(server)) {
				
				BaseComponent[] message = Main.getPrefix().append("The chat is now unmuted.").color(ChatColor.GRAY).create();
				
				if (server == null) proxy.broadcast(message);
				else {
					
					server.getPlayers().stream().forEach(p -> p.sendMessage(message));
					
					if (!server.equals(player.getServer().getInfo())) {
						
						ComponentBuilder confirm = Main.getPrefix()
						.append("The chat is now unmuted in the server '").color(ChatColor.GRAY)
						.append(server.getName()).color(ChatColor.GOLD)
						.append("'.").color(ChatColor.GRAY);
						
						player.sendMessage(confirm.create());
						
					}
					
				}
				
				return;
				
			}
			
			Main.muted.add(server);
			
			BaseComponent[] message = Main.getPrefix().append("The chat is now muted.").color(ChatColor.GRAY).create();
			
			if (server == null) proxy.broadcast(message);
			else {
				
				server.getPlayers().stream().forEach(p -> p.sendMessage(message));
				
				if (!server.equals(player.getServer().getInfo())) {
					
					ComponentBuilder confirm = Main.getPrefix()
					.append("The chat is now muted in the server '").color(ChatColor.GRAY)
					.append(server.getName()).color(ChatColor.GOLD)
					.append("'.").color(ChatColor.GRAY);
					
					player.sendMessage(confirm.create());
					
				}
				
			}
			
			return;
			
		}
		
	}

	public List<String> onTabComplete(CommandSender sender, String[] args) {
		
		List<String> suggestions = new ArrayList<>();
		
		switch (args.length) {
		
			case 1:
				suggestions.add("clear");
				suggestions.add("mute");
				break;
			case 2:
				Set<String> servers = proxy.getServers().keySet();
				
				suggestions.addAll(servers);
				suggestions.add("all");
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
