package com.thetonyk.Proxy.Managers;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Settings {
	
	private static Gson gson = new Gson();
	
	private int id;
	private UUID player;
	private boolean players;
	private boolean chat;
	private boolean mentions;
	private boolean messages;
	private Set<UUID> ignored;
	
	private Settings(int id, UUID player, boolean players, boolean chat, boolean mentions, boolean messages, String ignored) {
		
		this.id = id;
		this.player = player;
		this.players = players;
		this.chat = chat;
		this.mentions = mentions;
		this.messages = messages;
		
		Type type = new TypeToken<Set<UUID>>(){}.getType();
		this.ignored = ignored.length() < 1 ? new HashSet<UUID>() : gson.fromJson(ignored, type);
		
	}
	
	public UUID getPlayer() {
		
		return this.player;
		
	}
	
	public boolean getPlayers() {
		
		return this.players;
		
	}
	
	public void setPlayers(boolean players) throws SQLException {
		
		this.players = players;
		
		DatabaseManager.updateQuery("UPDATE settings SET players = " + (players ? 1 : 0) + " WHERE id = " + this.id + ";");
		
	}

	public boolean getChat() {
		
		return this.chat;
		
	}
	
	public void setChat(boolean chat) throws SQLException {
		
		this.chat = chat;
		
		DatabaseManager.updateQuery("UPDATE settings SET chat = " + (chat ? 1 : 0) + " WHERE id = " + this.id + ";");
		
	}
	
	public boolean getMentions() {
		
		return this.mentions;
		
	}
	
	public void setMentions(boolean mentions) throws SQLException {
		
		this.mentions = mentions;
		
		DatabaseManager.updateQuery("UPDATE settings SET mentions = " + (mentions ? 1 : 0) + " WHERE id = " + this.id + ";");
		
	}
	
	public boolean getMessages() {
		
		return this.messages;
		
	}
	
	public void setMessages(boolean messages) throws SQLException {
		
		this.messages = messages;
		
		DatabaseManager.updateQuery("UPDATE settings SET messages = " + (messages ? 1 : 0) + " WHERE id = " + this.id + ";");
		
	}
	
	public Set<UUID> getIgnored() {
		
		return this.ignored;
		
	}
	
	public void setIgnored(Set<UUID> ignored) throws SQLException {
		
		this.ignored = ignored;
		
		DatabaseManager.updateQuery("UPDATE settings SET ignored = '" + (ignored.isEmpty() ? "" : gson.toJson(ignored)) + "' WHERE id = " + this.id + ";");
		
	}
	
	public static Settings getSettings(UUID uuid) throws SQLException {
		
		int id = Integer.valueOf(PlayersManager.getField(uuid, "id"));
		
		try (Connection connection = DatabaseManager.getConnection();
		Statement statement = connection.createStatement();
		ResultSet query = statement.executeQuery("SELECT * FROM settings WHERE id = " + id + ";")) {
			
			if (!query.next()) return null;
		
			boolean players = query.getBoolean("players");
			boolean chat = query.getBoolean("chat");
			boolean mentions = query.getBoolean("mentions");
			boolean messages = query.getBoolean("messages");
			String ignored = query.getString("ignored");
			
			return new Settings(id, uuid, players, chat, mentions, messages, ignored);
		
		}
		
	}

}
