package com.thetonyk.Proxy.Commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.thetonyk.Proxy.Main;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class HelpopCommand extends Command implements TabExecutor {
	
	private static ProxyServer proxy = ProxyServer.getInstance();
	
	public HelpopCommand() {
		
		super("helpop", "proxy.helpop");
		
	}
	
	public void execute(CommandSender sender, String[] args) {
		
		if (args.length < 1) {
			
			ComponentBuilder message = Main.getPrefix().append("Usage: /helpop <message>").color(ChatColor.GRAY);
			sender.sendMessage(message.create());
			return;
			
		}
		
		Set<ProxiedPlayer> sendTo = proxy.getPlayers().stream().filter(p -> p.hasPermission("proxy.replyhelpop")).collect(Collectors.toSet());
		
		if (args[0].equalsIgnoreCase("reply") && args.length > 1 && sender.hasPermission("proxy.replyhelpop")) {
			
			ProxiedPlayer player = proxy.getPlayer(args[1]);
			
			if (player == null) {
				
				ComponentBuilder message = Main.getPrefix()
				.append("The player '").color(ChatColor.GRAY)
				.append(args[1]).color(ChatColor.GOLD)
				.append("' is not online.").color(ChatColor.GRAY);
				
				sender.sendMessage(message.create());
				return;
				
			}
			
			if (args.length < 3) {
				
				ComponentBuilder message = Main.getPrefix().append("Usage: /helpop reply <player> <response>").color(ChatColor.GRAY);
				sender.sendMessage(message.create());
				return;
				
			}
			
			String arg = args[2];
			
			for (int i = 3; i < args.length; i++) {
				
				arg += " " + args[i];
				
			}
			
			ComponentBuilder alert = new ComponentBuilder("Helpop ").color(ChatColor.GOLD)
			.append("| ").color(ChatColor.DARK_GRAY)
			.append(sender.getName()).color(ChatColor.GRAY)
			.append(" ⫸ ").color(ChatColor.DARK_GRAY)
			.append(player.getName()).color(ChatColor.GRAY)
			.append(": ").color(ChatColor.DARK_GRAY)
			.append(arg).color(ChatColor.WHITE);
			
			sendTo.stream().forEach(p -> p.sendMessage(alert.create()));
			
			ComponentBuilder message = new ComponentBuilder("Helpop ").color(ChatColor.GOLD)
			.append("| ").color(ChatColor.DARK_GRAY)
			.append("Staff").color(ChatColor.GRAY)
			.append(" ⫸ ").color(ChatColor.DARK_GRAY)
			.append(arg).color(ChatColor.WHITE);
			
			sender.sendMessage(message.create());
			return;		
			
		}
		
		String arg = args[0];
		
		for (int i = 1; i < args.length; i++) {
			
			arg += " " + args[i];
			
		}
		
		ComponentBuilder message = new ComponentBuilder("Helpop ").color(ChatColor.GOLD)
		.append("| ").color(ChatColor.DARK_GRAY)
		.append("Staff").color(ChatColor.GRAY)
		.append(" ⫷ ").color(ChatColor.RED)
		.append(arg).color(ChatColor.WHITE);
		
		sender.sendMessage(message.create());
		
		ComponentBuilder alert = new ComponentBuilder("Helpop ").color(ChatColor.GOLD)
		.append("| ").color(ChatColor.DARK_GRAY)
		.append(sender.getName()).color(ChatColor.GRAY)
		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("Reply to ").color(ChatColor.GRAY)
				.append(sender.getName()).color(ChatColor.GREEN)
				.append(".").color(ChatColor.GRAY).create()))
		.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/helpop reply " + sender.getName() + " "))
		.append(" ⫸ ").retain(FormatRetention.NONE).color(ChatColor.DARK_GRAY)
		.append(arg).color(ChatColor.WHITE);
				
		sendTo.stream().forEach(p -> p.sendMessage(alert.create()));
		
	}
	
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		
		List<String> suggestions = new ArrayList<>();
		
		switch (args.length) {
		
			case 2:
				if (!sender.hasPermission("proxy.replyhelpop")) break;
				
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
