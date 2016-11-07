package com.thetonyk.Proxy.Managers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PermissionsManager {
	
	public static void setPermissions(ProxiedPlayer player) throws SQLException {
		
		UUID uuid = player.getUniqueId();
		Rank rank = PlayersManager.getRank(uuid);
		
		player.setPermission("proxy.about", true);
		player.setPermission("proxy.help", true);
		player.setPermission("proxy.ignore", true);
		player.setPermission("proxy.lobby", true);
		player.setPermission("proxy.message", true);
		
		switch (rank) {
			case PLAYER:
			case FAMOUS:
			case BUILDER:
			case FRIEND:
				return;
			default:
				break;
		}
		
		player.setPermission("proxy.alerts", true);
		player.setPermission("proxy.punish", true);
		player.setPermission("proxy.info", true);
		
		if (rank == Rank.MOD) return;
		
		player.setPermission("proxy.broadcast", true);
		player.setPermission("proxy.chat", true);
		player.setPermission("proxy.cmdspy", true);
		player.setPermission("proxy.privatechat", true);
		
		if (rank != Rank.ADMIN) return;
		
		player.setPermission("proxy.seeips", true);
		player.setPermission("proxy.cancelpunishments", true);
		player.setPermission("proxy.globalbroadcast", true);
		player.setPermission("proxy.rank", true);
		player.addGroups("admin");
		
	}
	
	public static void clearPermissions(ProxiedPlayer player) {
		
		Collection<String> permissions = new ArrayList<String>(player.getPermissions());
		Collection<String> groups = new ArrayList<String>(player.getGroups());
		
		permissions.stream().filter(p -> p.startsWith("proxy.")).forEach(p -> player.setPermission(p, false));
		groups.stream().forEach(g -> player.removeGroups(g));
		
	}
	
	public static void reloadPermissions(ProxiedPlayer player) throws SQLException {
		
		clearPermissions(player);
		setPermissions(player);
		
	}
	
	public enum Rank {
		
		PLAYER("", new ComponentBuilder("Player").color(ChatColor.GRAY)), FAMOUS("§bFamous §8| ", new ComponentBuilder("Famous").color(ChatColor.AQUA)), BUILDER("§2Build §8| ", new ComponentBuilder("Builder").color(ChatColor.DARK_GREEN)), STAFF("§cStaff §8| ", new ComponentBuilder("Staff").color(ChatColor.RED)), MOD("§9Mod §8| ", new ComponentBuilder("Moderator").color(ChatColor.BLUE)), ADMIN("§4Admin §8| ", new ComponentBuilder("Admin").color(ChatColor.DARK_RED)), FRIEND("§3Friend §8| ", new ComponentBuilder("Friend").color(ChatColor.DARK_AQUA));
		
		String prefix;
		BaseComponent[] name;
		
		private Rank(String prefix, ComponentBuilder name) {
			
			this.prefix = prefix;
			this.name = name.create();
			
		}
		
		public String getPrefix() {
			
			return prefix;
			
		}
		
		public BaseComponent[] getName() {
			
			return name;
			
		}
		
	}

}
