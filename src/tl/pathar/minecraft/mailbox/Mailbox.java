package tl.pathar.minecraft.mailbox;

import org.bukkit.plugin.java.JavaPlugin;

public class Mailbox extends JavaPlugin {
    @Override
    public void onEnable() {
        getCommand("mailbox").setExecutor(new ExampleCommand(this));
        // getServer().getPluginManager().registerEvents(new MailboxListener(this), this);
        getServer().getPluginManager().registerEvents(new MailboxConstructionListener(this), this);
    }

    @Override
    public void onDisable() {

    }

}
