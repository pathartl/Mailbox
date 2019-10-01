package com.pablo67340.SQLiteLib.Error;

import java.util.logging.Level;

import com.pablo67340.SQLiteLib.Main.SQLiteLib;

public class Error {
	public static void execute(Exception ex) {
		SQLiteLib.INSTANCE.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
	}

	public static void close(Exception ex) {
		SQLiteLib.INSTANCE.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
	}
}
