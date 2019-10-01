package com.pablo67340.SQLiteLib.Main;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.pablo67340.SQLiteLib.Database.Database;
import com.pablo67340.SQLiteLib.Database.SQLite;

public class SQLiteLib {

	public static JavaPlugin INSTANCE;

	private Map<String, Database> databases = new HashMap<>();

	public SQLiteLib(JavaPlugin _plugin) {
		INSTANCE = _plugin;

		_plugin.getDataFolder().mkdirs();
	}
	
	/**
	 * Get the current instance of the plugin within the server. Needed to hook into
	 * the API to save things.
	 * <p>
	 * 
	 * @return the {@link SQLiteLib}'s prefix.
	 */
	public static SQLiteLib hookSQLiteLib(Plugin hostPlugin) {
        SQLiteLib plugin = (SQLiteLib) Bukkit.getPluginManager().getPlugin("SQLiteLib");
        if (plugin == null) {
            Bukkit.getLogger().severe("SQLiteLib is not yet ready! You have you called hookSQLiteLib() too early.");
            return null;
        }
        return plugin;
    }

	/**
	 * 
	 * @param Database
	 *            name
	 * @param Initial
	 *            statement once the database is created. Usually used to create
	 *            tables.
	 * 
	 *            Sets the string sent to player when an item cannot be purchased.
	 */
	public void initializeDatabase(String databaseName, String createStatement) {
		Database db = new SQLite(databaseName, createStatement, INSTANCE.getDataFolder());
		db.load();
		databases.put(databaseName, db);
	}
	
	/**
	 * 
	 * @param Database
	 *            name
	 * @param Initial
	 *            statement once the database is created. Usually used to create
	 *            tables.
	 * 
	 *            Sets the string sent to player when an item cannot be purchased.
	 * @param Plugin to create database file inside.
	 */
	public void initializeDatabase(Plugin plugin, String databaseName, String createStatement) {
		Database db = new SQLite(databaseName, createStatement, plugin.getDataFolder());
		db.load();
		databases.put(databaseName, db);
	}

	/**
	 * Get the global list of currently loaded databased.
	 * <p>
	 * 
	 * @return the {@link SQLiteLib}'s global database list.
	 */
	public Map<String, Database> getDatabases() {
		return databases;
	}

	/**
	 * 
	 * @param Database
	 *            name
	 * 
	 *            Gets a specific {@link Database}'s class.
	 */
	public Database getDatabase(String databaseName) {
		return getDatabases().get(databaseName);
	}

}
