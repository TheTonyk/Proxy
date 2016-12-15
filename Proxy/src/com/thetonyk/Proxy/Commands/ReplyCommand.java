package com.thetonyk.Proxy.Commands;

import java.sql.SQLException;
import java.util.UUID;

import com.thetonyk.Proxy.Main;
import com.thetonyk.Proxy.Managers.PlayersManager;
import com.thetonyk.Proxy.Managers.Settings;
import com.thetonyk.Proxy.Managers.PermissionsManager.Rank;

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

public class ReplyCommand extends Command {

	private static ProxyServer proxy = ProxyServer.getInstance();
	
	public ReplyCommand() {
		
		super("reply", "proxy.message", "r");
		
	}
	
	public void execute(CommandSender sender, String[] args) {
		
		UUID uuid = null;
		
		if (sender instanceof ProxiedPlayer) {
			
			ProxiedPlayer player = (ProxiedPlayer) sender;
			uuid = player.getUniqueId();
			
		}
		
		if (args.length < 1) {
			
			ComponentBuilder message = Main.getPrefix().append("Usage: /reply <message>").color(ChatColor.GRAY);
			sender.sendMessage(message.create());
			return;
			
		}
		
		if (!MessageCommand.lastMessage.containsKey(uuid)) {
			
			ComponentBuilder message = Main.getPrefix().append("You have nobody to reply to.").color(ChatColor.GRAY);
			sender.sendMessage(message.create());
			return;
			
		}
		
		String lastMessage = MessageCommand.lastMessage.get(uuid);
		ProxiedPlayer sendTo = proxy.getPlayer(lastMessage);
		
		if (sendTo == null && !lastMessage.equalsIgnoreCase("CONSOLE")) {
			
			ComponentBuilder message = Main.getPrefix()
			.append("The player '").color(ChatColor.GRAY)
			.append(lastMessage).color(ChatColor.GOLD)
			.append("' is not online.").color(ChatColor.GRAY);
			
			sender.sendMessage(message.create());
			return;
			
		}
		
		String arg = args[0];
		
		for (int i = 1; i < args.length; i++) {
			
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
			
			MessageCommand.lastMessage.put(sendTo == null ? null : sendTo.getUniqueId(), sender.getName());
			return;
			
		}
		
		ProxiedPlayer player = (ProxiedPlayer) sender;
		ServerInfo server = player.getServer().getInfo();
		ServerInfo sendToServer = sendTo.getServer().getInfo();
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
		
		MessageCommand.lastMessage.put(sendTo.getUniqueId(), sender.getName());
		
		if (senderRank != Rank.ADMIN && sendToRank != Rank.ADMIN) {
			
			BaseComponent[] alert = new ComponentBuilder(sender.getName()).color(ChatColor.DARK_GREEN)
			.append(" ⫸ ").color(ChatColor.DARK_GRAY)
			.append(sendTo.getName()).color(ChatColor.DARK_GREEN)
			.append(": ").color(ChatColor.DARK_GRAY)
			.append(arg).color(ChatColor.GRAY).italic(true).create();
			
			Main.socialspy.keySet().stream().filter(u -> !u.equals(player.getUniqueId()) && !u.equals(sendTo.getUniqueId()) && (Main.socialspy.get(u).equals(server) || Main.socialspy.get(u).equals(sendToServer) || Main.socialspy.get(u) == null) && proxy.getPlayer(u) != null).forEach(u -> proxy.getPlayer(u).sendMessage(alert));
			
		}
		
	}
	
}
