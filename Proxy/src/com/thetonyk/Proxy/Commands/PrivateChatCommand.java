package com.thetonyk.Proxy.Commands;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import com.thetonyk.Proxy.Main;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class PrivateChatCommand extends Command {
	
	private static ProxyServer proxy = ProxyServer.getInstance();
	
	public PrivateChatCommand() {
		
		super("private", "proxy.privatechat", "p");
		
	}

	public void execute(CommandSender sender, String[] args) {
		
		if (args.length < 1) {
			
			ComponentBuilder message = Main.getPrefix().append("Usage: /private <message>").color(ChatColor.GRAY);
			sender.sendMessage(message.create());
			return;
			
		}
		
		String arg = args[0];
		
		for (int i = 1; i < args.length; i++) {
			
			arg += " " + args[i];
			
		}
		
		Collection<ProxiedPlayer> players = proxy.getPlayers();
		Set<ProxiedPlayer> staffs = players.stream().filter(p -> p.hasPermission("proxy.privatechat")).collect(Collectors.toSet());
		
		if (staffs.size() < 2) {
			
			ComponentBuilder message = Main.getPrefix().append("There are no other staff online.").color(ChatColor.GRAY);
			sender.sendMessage(message.create());
			return;
			
		}
		
		BaseComponent[] message = new ComponentBuilder("StaffChat ").color(ChatColor.GOLD)
		.append("| ").color(ChatColor.DARK_GRAY)
		.append(sender.getName()).color(ChatColor.GRAY)
		.append(" ⫸ ").color(ChatColor.DARK_GRAY)
		.append(arg).color(ChatColor.WHITE).create();
		
		staffs.stream().forEach(s -> s.sendMessage(message));
		
	}
	
}
