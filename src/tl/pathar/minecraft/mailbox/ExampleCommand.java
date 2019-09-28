package tl.pathar.minecraft.mailbox;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ExampleCommand implements CommandExecutor {
    JavaPlugin plugin;

    public ExampleCommand(JavaPlugin _plugin) {
        plugin = _plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String commandName = cmd.getName().toLowerCase();

        if (!commandName.equals("mailbox")) {
            return false;
        }

        sender.sendMessage("Hello world!");

        return true;
    }
}
