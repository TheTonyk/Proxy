package com.thetonyk.Proxy.Commands;

import com.thetonyk.Proxy.Main;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.plugin.Command;

public class AboutCommand extends Command {
	
	public AboutCommand() {
		
		super("about", "proxy.about", "thetonyk");
		
	}
	
	public void execute(CommandSender sender, String[] args) {
		
		ComponentBuilder message = Main.getPrefix()
		.append("Plugins by TheTonyk for ").color(ChatColor.GRAY)
		.append(Main.NAME).color(ChatColor.GREEN);
		
		sender.sendMessage(message.create());
		
		message = Main.getPrefix()
		.append("Twitter: ").color(ChatColor.GRAY)
		.append("@TheTonyk").color(ChatColor.AQUA).italic(true)
		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
			new ComponentBuilder("Visit the Twitter of ").color(ChatColor.GRAY)
			.append("TheTonyk").color(ChatColor.GREEN)
			.append(".").color(ChatColor.GRAY).create()))
		.event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://twitter.com/TheTonyk"));
		
		sender.sendMessage(message.create());
		
	}

}
