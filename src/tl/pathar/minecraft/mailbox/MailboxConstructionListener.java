package tl.pathar.minecraft.mailbox;

import com.pablo67340.SQLiteLib.Database.Database;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.RedstoneWallTorch;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MailboxConstructionListener implements Listener {
    private MailboxPlugin plugin;
    private Database database;

    public MailboxConstructionListener(MailboxPlugin _plugin) {
        plugin = _plugin;
        database = plugin.sqlLib.getDatabase("Mailbox");

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            Mailbox playerMailbox = MailboxHelper.checkMail(database, player, plugin, true);

            if (playerMailbox != null && playerMailbox.structure.isValidMailbox()) {
                plugin.mailboxes.put(player.getName(), playerMailbox);
            }
        }
    }

    @EventHandler
    public void onBarrelInventory(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();

        if (inventory.getType() == InventoryType.BARREL) {
            Location inventoryLocation = inventory.getLocation();

            for (Map.Entry<String, Mailbox> entry : plugin.mailboxes.entrySet()) {
                String playerName = entry.getKey();
                Mailbox mailbox = entry.getValue();

                if (mailbox.structure != null && mailbox.owner != null && mailbox.structure.isValidMailbox() && inventoryLocation.equals(mailbox.structure.barrel.getLocation())) {
                    MailboxHelper.checkMail(database, mailbox.owner, plugin, false);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Mailbox playerMailbox = MailboxHelper.checkMail(database, player, plugin, true);

        if (playerMailbox != null && playerMailbox.structure.isValidMailbox()) {
            plugin.mailboxes.put(player.getName(), playerMailbox);
        }
    }

    @EventHandler
    public void onRedStone(BlockPhysicsEvent event) {
        Block block = event.getBlock();

        if (block.getType() == Material.REDSTONE_WALL_TORCH) {
            if (block.getMetadata("IsMailboxFlag").size() > 0 && block.getMetadata("IsMailboxFlag").get(0).asBoolean()) {
                if (block.getMetadata("MailboxHasMail").size() > 0 && !block.getMetadata("MailboxHasMail").get(0).asBoolean()) {
                    RedstoneWallTorch redstoneWallTorchBlockData = (RedstoneWallTorch) block.getBlockData();
                    redstoneWallTorchBlockData.setLit(false);
                    block.setBlockData(redstoneWallTorchBlockData);
                } else {
                    RedstoneWallTorch redstoneWallTorchBlockData = (RedstoneWallTorch) block.getBlockData();
                    redstoneWallTorchBlockData.setLit(true);
                    block.setBlockData(redstoneWallTorchBlockData);
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        Player player = event.getPlayer();

        switch (block.getType()) {
            case BARREL:
            case OAK_FENCE:
            case REDSTONE_WALL_TORCH:
                MailboxStructure mailbox = MailboxHelper.getMailboxStructureFromPart(block);

                if (mailbox.isValidMailbox()) {
                    player.sendMessage("You've constructed a mailbox!");

                    Barrel barrelState = (Barrel) mailbox.barrel.getState();
                    RedstoneWallTorch redstoneWallTorchBlockData = (RedstoneWallTorch) mailbox.redstoneWallTorch.getBlockData();

                    mailbox.redstoneWallTorch.setMetadata("IsMailboxFlag", new FixedMetadataValue(plugin, true));

                    barrelState.setCustomName(player.getName() + "'s Mailbox");
                    barrelState.update();

                    redstoneWallTorchBlockData.setLit(false);
                    mailbox.redstoneWallTorch.setBlockData(redstoneWallTorchBlockData);

                    Location barrelLocation = mailbox.barrel.getLocation();

                    database.executeStatement(
                            String.format(
                                    "INSERT INTO Mailboxes (\"Player\", \"X\", \"Y\", \"Z\") VALUES (\"%s\", %d, %d, %d)",
                                    player.getName(),
                                    barrelLocation.getBlockX(),
                                    barrelLocation.getBlockY(),
                                    barrelLocation.getBlockZ()
                            )
                    );
                }
        }
    }

    @EventHandler
    public void onBlockDestroy(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        switch (block.getType()) {
            case BARREL:
            case OAK_FENCE:
            case REDSTONE_WALL_TORCH:
                MailboxStructure mailbox = MailboxHelper.getMailboxStructureFromPart(block);

                if (mailbox.isValidMailbox()) {
                    Location barrelLocation = mailbox.barrel.getLocation();
                    String mailboxOwner = (String)database.queryValue(
                            String.format(
                                    "SELECT * FROM Mailboxes WHERE X = %d AND Y = %d AND Z = %d",
                                    barrelLocation.getBlockX(),
                                    barrelLocation.getBlockY(),
                                    barrelLocation.getBlockZ()
                            ),
                            "Player"
                    );

                    database.executeStatement(
                            String.format(
                                    "DELETE FROM Mailboxes WHERE X = %d AND Y = %d AND Z = %d",
                                    barrelLocation.getBlockX(),
                                    barrelLocation.getBlockY(),
                                    barrelLocation.getBlockZ()
                            )
                    );

                    if (!mailboxOwner.equals(player.getName())) {
                        plugin.getServer().broadcastMessage("Hey, " + player.getName() + " bashed in " + mailboxOwner + "'s mailbox!");
                    } else {
                        player.sendMessage("Your mailbox has been destroyed.");
                    }
                }

                break;
        }
    }
}
