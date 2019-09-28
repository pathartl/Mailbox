package tl.pathar.minecraft.mailbox;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MailboxCommand implements CommandExecutor {
    Mailbox plugin;

    public MailboxCommand(Mailbox _plugin) {
        plugin = _plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String commandName = cmd.getName().toLowerCase();

        if (!commandName.equals("checkmail")) {
            return false;
        }

        Player player = plugin.getServer().getPlayer(sender.getName());

        MailboxHelper.checkMail(plugin, player);

        return true;
    }
}
