package com.thetonyk.Proxy;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.google.common.collect.Lists;
import com.thetonyk.Proxy.Managers.DatabaseManager;
import com.thetonyk.Proxy.Managers.Settings;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class Main extends Plugin {
	
	public static Main plugin;
	private static ProxyServer proxy = ProxyServer.getInstance();
	private static Configuration configuration = null;
	
	public static Set<ServerInfo> muted = new HashSet<>();
	public static Map<UUID, ServerInfo> cmdspy = new HashMap<>();
	public static Map<UUID, ServerInfo> socialspy = new HashMap<>();
	
	public static String name;
	public static String twitter;
	public static String channel;
	
	public void onEnable() {
		
		plugin = this;
		
		if (!this.getDataFolder().exists()) this.getDataFolder().mkdir();
		
		File config = new File(this.getDataFolder(), "config.yml");
		
		try {
			
			if (!config.exists()) {
				
				config.createNewFile();
				configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);
				
				configuration.set("name", "Server");
				configuration.set("twitter", "@TheTonyk");
				configuration.set("channel", "server");
				configuration.set("SQLHost", "localhost");
				configuration.set("SQLDatabase", "database");
				configuration.set("SQLUser", "user");
				configuration.set("SQLPass", "pass");
				
				ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, config);
				
			} else {
				
				configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);
				
			}
			
		} catch (IOException exception) {
			
			proxy.getLogger().severe("[Main] Unable to get configuration !");
			proxy.stop("§8⫸ §7An error has occured while starting §a" + Main.name + " §8⫷");
			return;
			
		}
		
		name = configuration.getString("name", "Server");
		twitter = configuration.getString("twitter", "@TheTonyk");
		channel = configuration.getString("channel", "server");
		
		try {
			
			Iterator<Class<?>> classes = getClasses(Lists.newArrayList("Settings")).iterator();
			
			while (classes.hasNext()) {
				
				Object instance = classes.next().newInstance();
				
				if (instance instanceof Listener) {
					
					proxy.getPluginManager().registerListener(this, (Listener) instance);
					
				}
				
				if (instance instanceof Command) {
					
					proxy.getPluginManager().registerCommand(this, (Command) instance);
					
				}
				
				classes.remove();
				
			}
			
		} catch (InstantiationException | IllegalAccessException | IOException exception) {
			
			proxy.getLogger().severe("[Main] Unable to register listeners and commands !");
			proxy.stop("§8⫸ §7An error has occured while starting §a" + Main.name + " §8⫷");
			return;
			
		}
		
		proxy.registerChannel(channel);
		
	}
	
	public void onDisable() {
		
		plugin = null;
		
		try {
			
			DatabaseManager.close();
			
		} catch (SQLException exception) {
			
			proxy.getLogger().severe("[Main] Unable to properly close the connection pool.");
			
		}
		
	}
	
	private static List<Class<?>> getClasses(List<String> excludes) throws IOException {
		
		List<Class<?>> classes = new ArrayList<Class<?>>();
		JarFile jar = new JarFile(Main.plugin.getFile());
		Enumeration<JarEntry> entries = jar.entries();
		
		if (excludes == null) excludes = new ArrayList<>();
		
		while (entries.hasMoreElements()) {
			
			JarEntry entry = entries.nextElement();
			String file = entry.getName().replace("/", ".");
			
			if (!file.startsWith("com.thetonyk") || !file.endsWith(".class") || file.contains("$")) continue;
			
			int index = file.substring(0, file.length() - 6).lastIndexOf(".");
			String name = file.substring(index + 1, file.length() - 6);
			
			if (excludes.contains(name)) continue;
			
			Class<?> instance;
			
			try {
				
				instance = Class.forName(file.substring(0, file.length() - 6));
				
			} catch (ClassNotFoundException exception) { continue; }
			
			classes.add(instance);
			
		}
		
		jar.close();
		return classes;
		
	}
	
	public static ComponentBuilder getPrefix() {
		
		return new ComponentBuilder(name + " ").color(ChatColor.BLUE).bold(true).append("⫸ ").color(ChatColor.DARK_GRAY).bold(false);
		
	}
	
	public static Configuration getConfiguration() {
		
		return configuration;
		
	}
	
	public static Main getPlugin() {
		
		return plugin;
		
	}

}
