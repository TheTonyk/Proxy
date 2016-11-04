package com.thetonyk.Proxy.Commands;

import java.util.Collection;

import com.thetonyk.Proxy.Main;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class BroadcastCommand extends Command {
	
	public BroadcastCommand() {
		
		super("broadcast", "proxy.broadcast", "bc");
		
	}
	
	public void execute(CommandSender sender, String[] args) {
		
		ProxiedPlayer player = (ProxiedPlayer) sender;
		Collection<ProxiedPlayer> toSend = player.getServer().getInfo().getPlayers();
		
		if (args.length < 1) {
			
			ComponentBuilder message = Main.getPrefix().append("Usage: /broadcast <message>").color(ChatColor.GRAY);
			sender.sendMessage(message.create());
			return;
			
		}
		
		String arg = args[0];
		
		for (int i = 1; i < args.length; i++) {
			
			arg += " " + args[i];
			
		}
		
		ComponentBuilder message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
		.append(arg).color(ChatColor.AQUA).bold(true);		
				
		toSend.stream().forEach(p -> p.sendMessage(message.create()));
		
	}
	
}
