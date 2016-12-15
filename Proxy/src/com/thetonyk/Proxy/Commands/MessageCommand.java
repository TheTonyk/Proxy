package com.thetonyk.Proxy.Commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.thetonyk.Proxy.Main;
import com.thetonyk.Proxy.Managers.PermissionsManager.Rank;
import com.thetonyk.Proxy.Managers.PlayersManager;
import com.thetonyk.Proxy.Managers.Settings;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class MessageCommand extends Command implements TabExecutor {
	
	private static ProxyServer proxy = ProxyServer.getInstance();
	public static Map<UUID, String> lastMessage = new HashMap<>();
	
	public MessageCommand() {
		
		super("message", "proxy.message", "msg", "tell", "w");
		
	}
	
	public void execute(CommandSender sender, String[] args) {
		
		if (args.length < 2) {
			
			ComponentBuilder message = Main.getPrefix().append("Usage: /message <player> <message>").color(ChatColor.GRAY);
			sender.sendMessage(message.create());
			return;
			
		}
		
		ProxiedPlayer sendTo = proxy.getPlayer(args[0]);
		
		if (sendTo == null && !args[0].equalsIgnoreCase("CONSOLE")) {
			
			ComponentBuilder message = Main.getPrefix()
			.append("The player '").color(ChatColor.GRAY)
			.append(args[0]).color(ChatColor.GOLD)
			.append("' is not online.").color(ChatColor.GRAY);
			
			sender.sendMessage(message.create());
			return;
			
		}
		
		String arg = args[1];
		
		for (int i = 2; i < args.length; i++) {
			
			arg += " " + args[i];
			
		}
		
		if (!(sender instanceof ProxiedPlayer) || sendTo == null) {
			
			ComponentBuilder message = new ComponentBuilder("Private ").color(ChatColor.GOLD)
			.append("| ").color(ChatColor.DARK_GRAY)
			.append(sendTo == null ? "CONSOLE" : sendTo.getName()).color(ChatColor.GRAY)
			.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
					new ComponentBuilder("Send another message to ").color(ChatColor.GRAY)
					.append(sendTo == null ? "CONSOLE" : sendTo.getName()).color(ChatColor.GREEN)
					.append(".").color(ChatColor.GRAY).create()))
			.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/message " + (sendTo == null ? "CONSOLE" : sendTo.getName()) + " "))
			.append(" ⫷ ").retain(FormatRetention.NONE).color(ChatColor.RED)
			.append(arg).color(ChatColor.WHITE);
			
			sender.sendMessage(message.create());
			
			message = new ComponentBuilder("Private ").color(ChatColor.GOLD)
			.append("| ").color(ChatColor.DARK_GRAY)
			.append(sender.getName()).color(ChatColor.GRAY)
			.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
					new ComponentBuilder("Reply to ").color(ChatColor.GRAY)
					.append(sender.getName()).color(ChatColor.GREEN)
					.append(".").color(ChatColor.GRAY).create()))
			.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/message " + sender.getName() + " "))
			.append(" ⫸ ").retain(FormatRetention.NONE).color(ChatColor.GREEN)
			.append(arg).color(ChatColor.WHITE);
					
			if (sendTo == null) proxy.getConsole().sendMessage(message.create());
			else sendTo.sendMessage(message.create());
			
			lastMessage.put(sendTo == null ? null : sendTo.getUniqueId(), sender.getName());
			return;
			
		}
		
		ProxiedPlayer player = (ProxiedPlayer) sender;
		ServerInfo server = player.getServer().getInfo();
		ServerInfo sendToServer = sendTo.getServer().getInfo();
		
		if (player.getUniqueId().equals(sendTo.getUniqueId())) {
			
			ComponentBuilder message = Main.getPrefix().append("You can't send messages to yourself.").color(ChatColor.GRAY);
			sender.sendMessage(message.create());
			return;
			
		}
		
		Settings senderSettings;
		Settings sendToSettings;
		Rank senderRank;
		Rank sendToRank;
		
		try {
			
			senderSettings = Settings.getSettings(player.getUniqueId());
			sendToSettings = Settings.getSettings(sendTo.getUniqueId());
			senderRank = PlayersManager.getRank(player.getUniqueId());
			sendToRank = PlayersManager.getRank(sendTo.getUniqueId());
			
		} catch (SQLException exception) {
			
			ComponentBuilder message = Main.getPrefix().append("An error has occured while sending the message. Please try again later.").color(ChatColor.GRAY);
			sender.sendMessage(message.create());
			return;
			
		}
		
		if (!senderSettings.getMessages()) {
			
			ComponentBuilder message = Main.getPrefix().append("Your private messages are disabled.").color(ChatColor.GRAY);
			sender.sendMessage(message.create());
			return;
			
		}
		
		if (!sendToSettings.getMessages()) {
			
			ComponentBuilder message = Main.getPrefix()
			.append("You can't send messages to '").color(ChatColor.GRAY)
			.append(sendTo.getName()).color(ChatColor.GREEN)
			.append("'.").color(ChatColor.GRAY);
			
			sender.sendMessage(message.create());
			return;
			
		}
		
		ComponentBuilder message = new ComponentBuilder("Private ").color(ChatColor.GOLD)
		.append("| ").color(ChatColor.DARK_GRAY)
		.append(sendTo.getName()).color(ChatColor.GRAY)
		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("Send another message to ").color(ChatColor.GRAY)
				.append(sendTo.getName()).color(ChatColor.GREEN)
				.append(".").color(ChatColor.GRAY).create()))
		.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/message " + sendTo.getName() + " "))
		.append(" ⫷ ").retain(FormatRetention.NONE).color(ChatColor.RED)
		.append(arg).color(ChatColor.WHITE);
		
		sender.sendMessage(message.create());
		
		if (sendToSettings.getIgnored().contains(player.getUniqueId())) return;
		
		message = new ComponentBuilder("Private ").color(ChatColor.GOLD)
		.append("| ").color(ChatColor.DARK_GRAY)
		.append(sender.getName()).color(ChatColor.GRAY)
		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("Reply to ").color(ChatColor.GRAY)
				.append(sender.getName()).color(ChatColor.GREEN)
				.append(".").color(ChatColor.GRAY).create()))
		.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/message " + sender.getName() + " "))
		.append(" ⫸ ").retain(FormatRetention.NONE).color(ChatColor.GREEN)
		.append(arg).color(ChatColor.WHITE);
				
		sendTo.sendMessage(message.create());
		
		lastMessage.put(sendTo.getUniqueId(), sender.getName());
		
		if (senderRank != Rank.ADMIN && sendToRank != Rank.ADMIN) {
			
			BaseComponent[] alert = new ComponentBuilder(sender.getName()).color(ChatColor.DARK_GREEN)
			.append(" ⫸ ").color(ChatColor.DARK_GRAY)
			.append(sendTo.getName()).color(ChatColor.DARK_GREEN)
			.append(": ").color(ChatColor.DARK_GRAY)
			.append(arg).color(ChatColor.GRAY).italic(true).create();
			
			Main.socialspy.keySet().stream().filter(u -> !u.equals(player.getUniqueId()) && !u.equals(sendTo.getUniqueId()) && (Main.socialspy.get(u).equals(server) || Main.socialspy.get(u).equals(sendToServer) || Main.socialspy.get(u) == null) && proxy.getPlayer(u) != null).forEach(u -> proxy.getPlayer(u).sendMessage(alert));
			
		}
		
	}
	
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		
		ProxiedPlayer player = sender instanceof ProxiedPlayer ? (ProxiedPlayer) sender : null;
		List<String> suggestions = new ArrayList<>();
		
		switch (args.length) {
		
			case 1:
				Set<String> online = new HashSet<>();
				
				proxy.getPlayers().stream().filter(p -> player == null || !p.equals(player)).forEach(p -> online.add(p.getName()));
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
