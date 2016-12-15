package com.thetonyk.Proxy.Commands;

import com.thetonyk.Proxy.Main;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class BroadcastCommand extends Command {
	
	private static ProxyServer proxy = ProxyServer.getInstance();
	
	public BroadcastCommand() {
		
		super("broadcast", "proxy.broadcast", "bc");
		
	}
	
	public void execute(CommandSender sender, String[] args) {
		
		ServerInfo server;
		
		if (args.length < 1 || (args.length < 2 && !(sender instanceof ProxiedPlayer))) {
			
			ComponentBuilder message = Main.getPrefix().append("Usage: /broadcast <message> [server]").color(ChatColor.GRAY);
			sender.sendMessage(message.create());
			return;
			
		} else {
			
			ProxiedPlayer player = (ProxiedPlayer) sender;
			server = player.getServer().getInfo();
			
		}
		
		if (args.length >= 2) {
			
			server = proxy.getServerInfo(args[1]);
			
			if (server == null) {
				
				ComponentBuilder message = Main.getPrefix()
				.append("'").color(ChatColor.GRAY)
				.append(args[1]).color(ChatColor.GOLD)
				.append("' is not a server.").color(ChatColor.GRAY);
				
				sender.sendMessage(message.create());
				return;
				
			}
			
		}
		
		String arg = args[0];
		
		for (int i = 1; i < args.length; i++) {
			
			arg += " " + args[i];
			
		}
		
		ComponentBuilder message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
		.append(arg).color(ChatColor.AQUA).bold(true);		
				
		server.getPlayers().stream().forEach(p -> p.sendMessage(message.create()));
		
	}
	
}
