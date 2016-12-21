package com.thetonyk.Proxy.Commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.thetonyk.Proxy.Main;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class BroadcastCommand extends Command implements TabExecutor {
	
	private static ProxyServer proxy = ProxyServer.getInstance();
	
	public BroadcastCommand() {
		
		super("broadcast", "proxy.broadcast", "bc");
		
	}
	
	public void execute(CommandSender sender, String[] args) {
		
		if (args.length < 2) {
			
			ComponentBuilder message = Main.getPrefix().append("Usage: /broadcast <server> <message>").color(ChatColor.GRAY);
			sender.sendMessage(message.create());
			return;
			
		}
		
		ServerInfo server = proxy.getServerInfo(args[0]);
		
		if (server == null) {
			
			ComponentBuilder message = Main.getPrefix()
			.append("'").color(ChatColor.GRAY)
			.append(args[0]).color(ChatColor.GOLD)
			.append("' is not a server.").color(ChatColor.GRAY);
			
			sender.sendMessage(message.create());
			return;
			
		}
		
		String arg = args[1];
		
		for (int i = 2; i < args.length; i++) {
			
			arg += " " + args[i];
			
		}
		
		ComponentBuilder message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
		.append(arg).color(ChatColor.AQUA).bold(true);		
				
		server.getPlayers().stream().forEach(p -> p.sendMessage(message.create()));
		
	}
	
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		
		List<String> suggestions = new ArrayList<>();
		
		switch (args.length) {
		
			case 1:
				Set<String> servers = proxy.getServers().keySet();
				
				suggestions.addAll(servers);
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
