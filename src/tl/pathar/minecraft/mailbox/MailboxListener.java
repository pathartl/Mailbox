package tl.pathar.minecraft.mailbox;

import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

public class MailboxListener implements Listener {
    public Mailbox plugin;

    public MailboxListener(Mailbox _plugin) {
        plugin = _plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block block = e.getClickedBlock();
            Material blockType = block.getType();

            if (blockType == Material.BARREL) {
                Barrel barrel = (Barrel)block.getState();

                String expectedPlayerBarrelName = String.format("%s's Mailbox", e.getPlayer().getName());

                e.getPlayer().sendMessage(expectedPlayerBarrelName);

                if (barrel.getCustomName().equalsIgnoreCase(expectedPlayerBarrelName)) {
                    e.getPlayer().sendMessage("WHAT DO WE WANT");
                    plugin.getServer().getPlayer("pathartl").sendMessage("I WANT YOUR EYES");
                }
            }
        }
    }
}
