package com.thetonyk.Proxy.Commands;

import com.thetonyk.Proxy.Main;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.plugin.Command;

public class HelpCommand extends Command {
	
	public HelpCommand() {
		
		super("help", "proxy.help", "?");
		
	}
	
	public void execute(CommandSender sender, String[] args) {
		
		ComponentBuilder message = Main.getPrefix().append("Help informations:").color(ChatColor.GRAY);
		
		sender.sendMessage(message.create());
		
		message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
		.append("Contact a staff member if you need help.").color(ChatColor.GRAY);
		
		sender.sendMessage(message.create());
		
		message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
		.append("We recommend using the ").color(ChatColor.GRAY)
		.append("1.8.9").color(ChatColor.GOLD)
		.append(" version of Minecraft.").color(ChatColor.GRAY);
		
		sender.sendMessage(message.create());
		
		message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
		.append("Twitter: ").color(ChatColor.GRAY)
		.append(Main.TWITTER).color(ChatColor.AQUA).italic(true)
		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
			new ComponentBuilder("Visit our ").color(ChatColor.GRAY)
			.append("Twitter").color(ChatColor.GREEN)
			.append(".").color(ChatColor.GRAY).create()))
		.event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://twitter.com/" + Main.TWITTER.substring(1)));
		
		sender.sendMessage(message.create());
		
	}

}
