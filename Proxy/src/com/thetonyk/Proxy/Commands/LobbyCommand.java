package com.thetonyk.Proxy.Commands;

import com.thetonyk.Proxy.Main;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class LobbyCommand extends Command {
	
	private static ProxyServer proxy = ProxyServer.getInstance();
	
	public LobbyCommand() {
		
		super("lobby", "proxy.lobby", "hub");
		
	}
	
	public void execute(CommandSender sender, String[] args) {
		
		ProxiedPlayer player = (ProxiedPlayer) sender;
		ServerInfo server = player.getServer().getInfo();
		ServerInfo lobby = proxy.getServerInfo("lobby");
		
		if (lobby == null) lobby = proxy.getServerInfo("hub");
		
		if (lobby == null) {
			
			ComponentBuilder message = Main.getPrefix().append("An error has occured, please try again later.").color(ChatColor.GRAY);
			sender.sendMessage(message.create());
			return;
			
		}
		
		if (server == lobby) {
			
			ComponentBuilder message = Main.getPrefix().append("You are already in the lobby.").color(ChatColor.GRAY);
			sender.sendMessage(message.create());
			return;
			
		}
		
		Callback<Boolean> callback = new Callback<Boolean>() {

			public void done(Boolean done, Throwable error) {
				
				if (error == null) return;
				
				ComponentBuilder message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
				.append("An error has occured while connecting to the server").color(ChatColor.GRAY)
				.append(" ⫷\n\n").color(ChatColor.DARK_GRAY)
				.append("Please try again later or contact us on Twitter ").color(ChatColor.GRAY)
				.append(Main.TWITTER).color(ChatColor.AQUA);
				
				player.disconnect(message.create());
				
			}
			
		};
		
		player.connect(lobby, callback);
		
	}

}
