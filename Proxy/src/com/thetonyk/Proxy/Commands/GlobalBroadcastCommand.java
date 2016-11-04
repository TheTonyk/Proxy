package com.thetonyk.Proxy.Commands;

import com.thetonyk.Proxy.Main;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;

public class GlobalBroadcastCommand extends Command {

	private static ProxyServer proxy = ProxyServer.getInstance();
	
	public GlobalBroadcastCommand() {
		
		super("globalbroadcast", "proxy.globalbroadcast", "gbroadcast", "gbc");

	}

	public void execute(CommandSender sender, String[] args) {
		
		if (args.length < 1) {
			
			ComponentBuilder message = Main.getPrefix().append("Usage: /globalbroadcast <message>").color(ChatColor.GRAY);
			sender.sendMessage(message.create());
			return;
			
		}
		
		String arg = args[0];
		
		for (int i = 1; i < args.length; i++) {
			
			arg += " " + args[i];
			
		}
		
		BaseComponent[] space = new ComponentBuilder("").create();
		
		ComponentBuilder message = Main.getPrefix()
		.append(arg).color(ChatColor.AQUA).bold(true);
		
		proxy.broadcast(space);
		proxy.broadcast(space);
		proxy.broadcast(message.create());
		proxy.broadcast(space);
		proxy.broadcast(space);
		
	}

}
