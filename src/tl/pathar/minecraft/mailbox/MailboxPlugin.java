package tl.pathar.minecraft.mailbox;

import com.pablo67340.SQLiteLib.Main.SQLiteLib;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class MailboxPlugin extends JavaPlugin {
    public SQLiteLib sqlLib;
    public Map<String, Mailbox> mailboxes;

    @Override
    public void onEnable() {
        sqlLib = SQLiteLib.hookSQLiteLib(this);

        sqlLib.initializeDatabase("Mailbox", "CREATE TABLE IF NOT EXISTS \"Mailboxes\" (\n" +
                "\t\"Id\"\tINTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,\n" +
                "\t\"Player\"\tTEXT,\n" +
                "\t\"X\"\tINTEGER,\n" +
                "\t\"Y\"\tINTEGER,\n" +
                "\t\"Z\"\tINTEGER\n" +
                ");");

        getCommand("checkmail").setExecutor(new MailboxCommand(this));
        // getServer().getPluginManager().registerEvents(new MailboxListener(this), this);
        getServer().getPluginManager().registerEvents(new MailboxConstructionListener(this), this);
    }

    @Override
    public void onDisable() {

    }

}
