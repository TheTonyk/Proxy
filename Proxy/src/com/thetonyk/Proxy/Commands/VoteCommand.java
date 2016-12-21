package com.thetonyk.Proxy.Commands;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.thetonyk.Proxy.Main;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class VoteCommand extends Command {
	
	private static ProxyServer proxy = ProxyServer.getInstance();
	private static ScheduledTask task = null;
	private static Set<UUID> yes = new HashSet<>();
	private static Set<UUID> no = new HashSet<>();
	
	public VoteCommand() {
		
		super("vote", "proxy.vote", "v");
		
	}
	
	public void execute(CommandSender sender, String[] args) {
		
		if (args.length < 1) {
			
			ComponentBuilder message = Main.getPrefix().append("Usage: /vote " + (sender.hasPermission("proxy.createvote") ? "<message>" : "<yes|no>")).color(ChatColor.GRAY);
			sender.sendMessage(message.create());
			return;
			
		}
		
		if (!sender.hasPermission("proxy.createvote") && !args[0].equalsIgnoreCase("yes") && !args[0].equalsIgnoreCase("no")) {
			
			ComponentBuilder message = Main.getPrefix().append("Usage: /vote <yes|no>").color(ChatColor.GRAY);
			sender.sendMessage(message.create());
			return;
			
		}
		
		if (args[0].equalsIgnoreCase("yes") || args[0].equalsIgnoreCase("no")) {
			
			if (task == null) {
				
				ComponentBuilder message = Main.getPrefix().append("There is no vote running.").color(ChatColor.GRAY);
				sender.sendMessage(message.create());
				return;
				
			}
			
			if (!(sender instanceof ProxiedPlayer)) {
				
				ComponentBuilder message = Main.getPrefix().append("Only players can vote.").color(ChatColor.GRAY);
				sender.sendMessage(message.create());
				return;
				
			}
			
			ProxiedPlayer player = (ProxiedPlayer) sender;
			
			if (yes.contains(player.getUniqueId()) || no.contains(player.getUniqueId())) {
				
				ComponentBuilder message = Main.getPrefix().append("You have already voted.").color(ChatColor.GRAY);
				sender.sendMessage(message.create());
				return;
				
			}
			
			if (args[0].equalsIgnoreCase("yes")) {
				
				yes.add(player.getUniqueId());
				
				ComponentBuilder message = Main.getPrefix()
				.append("You voted ").color(ChatColor.GRAY)
				.append("Yes").color(ChatColor.GREEN)
				.append(".").color(ChatColor.GRAY);
				
				sender.sendMessage(message.create());
				return;
				
			} else {
				
				no.add(player.getUniqueId());
				
				ComponentBuilder message = Main.getPrefix()
				.append("You voted ").color(ChatColor.GRAY)
				.append("No").color(ChatColor.RED)
				.append(".").color(ChatColor.GRAY);
				
				sender.sendMessage(message.create());
				return;
				
			}
			
		}
		
		if (task != null) {
			
			ComponentBuilder message = Main.getPrefix().append("A vote is already running.").color(ChatColor.GRAY);
			sender.sendMessage(message.create());
			return;
			
		}
		
		String arg = args[0];
		
		for (int i = 1; i < args.length; i++) {
			
			arg += " " + args[i];
			
		}
		
		String vote = arg;
		
		ComponentBuilder message = Main.getPrefix()
		.append("A vote has started: ").color(ChatColor.GRAY)
		.append("(").color(ChatColor.DARK_GRAY)
		.append("End in 30s").color(ChatColor.GRAY)
		.append(")").color(ChatColor.DARK_GRAY);
		
		proxy.broadcast(message.create());
		
		message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
		.append(vote).color(ChatColor.GRAY);
		
		proxy.broadcast(message.create());
		
		message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
		.append("Reply: ").color(ChatColor.GRAY)
		.append("Yes").color(ChatColor.GREEN).italic(true)
		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("Click here to vote ").color(ChatColor.GRAY)
				.append("Yes").color(ChatColor.GREEN)
				.append(".").color(ChatColor.GRAY).create()))
		.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vote yes"))
		.append(" | ").retain(FormatRetention.NONE).color(ChatColor.DARK_GRAY)
		.append("No").color(ChatColor.RED).italic(true)
		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("Click here to vote ").color(ChatColor.GRAY)
				.append("No").color(ChatColor.RED)
				.append(".").color(ChatColor.GRAY).create()))
		.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vote no"));
		
		proxy.broadcast(message.create());
		
		proxy.getScheduler().schedule(Main.plugin, new Runnable() {

			public void run() {
				
				ComponentBuilder message = Main.getPrefix()
				.append("The vote end in ").color(ChatColor.GRAY)
				.append("10s").color(ChatColor.GOLD)
				.append(".").color(ChatColor.GRAY);
				
				proxy.broadcast(message.create());
				
				message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
				.append(vote).color(ChatColor.GRAY);
				
				proxy.broadcast(message.create());
				
				message = new ComponentBuilder("⫸ ").color(ChatColor.DARK_GRAY)
				.append("Reply: ").color(ChatColor.GRAY)
				.append("Yes").color(ChatColor.GREEN).italic(true)
				.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
						new ComponentBuilder("Click here to vote ").color(ChatColor.GRAY)
						.append("Yes").color(ChatColor.GREEN)
						.append(".").color(ChatColor.GRAY).create()))
				.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vote yes"))
				.append(" | ").retain(FormatRetention.NONE).color(ChatColor.DARK_GRAY)
				.append("No").color(ChatColor.RED).italic(true)
				.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
						new ComponentBuilder("Click here to vote ").color(ChatColor.GRAY)
						.append("No").color(ChatColor.RED)
						.append(".").color(ChatColor.GRAY).create()))
				.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vote no"));
				
				proxy.broadcast(message.create());
				
			}
			
		}, 20, TimeUnit.SECONDS);
		
		task = proxy.getScheduler().schedule(Main.plugin, new Runnable() {

			public void run() {
				
				ComponentBuilder message = Main.getPrefix()
				.append("Vote ended, the results are: ").color(ChatColor.GRAY)
				.append(yes.size() + " yes").color(ChatColor.GREEN)
				.append(" and ").color(ChatColor.GRAY)
				.append(no.size() + " no").color(ChatColor.RED)
				.append(".").color(ChatColor.GRAY);
				
				proxy.broadcast(message.create());
				
				task = null;
				
				yes.clear();
				no.clear();
				
			}
			
		}, 30, TimeUnit.SECONDS);
		
	}

}
