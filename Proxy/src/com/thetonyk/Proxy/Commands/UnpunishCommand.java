package com.thetonyk.Proxy.Commands;

import java.sql.SQLException;

import com.thetonyk.Proxy.Main;
import com.thetonyk.Proxy.Managers.DatabaseManager;
import com.thetonyk.Proxy.Managers.PlayersManager;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class UnpunishCommand extends Command {
	
	public UnpunishCommand() {
		
		super("unpunish", "proxy.cancelpunishments");
		
	}
	
	public void execute(CommandSender sender, String[] args) {
			
		ProxiedPlayer player = (ProxiedPlayer) sender;
		
		if (args.length < 1) {
			
			ComponentBuilder message = Main.getPrefix().append("Please use: /info <player>").color(ChatColor.GRAY);
			sender.sendMessage(message.create());
			return;
			
		}
		
		int id;
		
		try {
			
			id = Integer.valueOf(args[0]);
		
		} catch (NumberFormatException exception) {
			
			ComponentBuilder message = Main.getPrefix().append("The punishment id format is not correct.").color(ChatColor.GRAY);
			sender.sendMessage(message.create());
			return;
			
		}
		
		try {
			
			boolean exist = DatabaseManager.exist("punishments", "id", null, id);
			
			if (!exist) {
				
				ComponentBuilder message = Main.getPrefix().append("The punishment can't be found.").color(ChatColor.GRAY);
				sender.sendMessage(message.create());
				return;
				
			}
			
			int playerId = Integer.valueOf(PlayersManager.getField(player.getUniqueId(), "id"));
			
			DatabaseManager.updateQuery("UPDATE cancelled = " + playerId + " WHERE id = " + id + ";");
			
			
		} catch (SQLException exception) {
			
			ComponentBuilder error = Main.getPrefix().append("An error has occured while processing the unpunishment. Please try again later.").color(ChatColor.GRAY);
			sender.sendMessage(error.create());
			return;
			
		}
		
		ComponentBuilder message = Main.getPrefix().append("The punishment has been cancelled.").color(ChatColor.GRAY);
		sender.sendMessage(message.create());
		return;
		
	}

}
