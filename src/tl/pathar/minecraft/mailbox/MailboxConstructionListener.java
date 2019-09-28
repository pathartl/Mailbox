package tl.pathar.minecraft.mailbox;

import com.pablo67340.SQLiteLib.Database.Database;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.RedstoneWallTorch;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class MailboxConstructionListener implements Listener {
    private Mailbox plugin;
    private Database database;

    public MailboxConstructionListener(Mailbox _plugin) {
        plugin = _plugin;
        database = plugin.sqlLib.getDatabase("Mailbox");
    }

    @EventHandler
    public void onRedStone(BlockPhysicsEvent event) {
        Block block = event.getBlock();

        if (block.getType() == Material.REDSTONE_WALL_TORCH) {
            if (block.getMetadata("IsMailboxFlag").get(0).asBoolean()) {
                RedstoneWallTorch redstoneWallTorchBlockData = (RedstoneWallTorch) block.getBlockData();

                redstoneWallTorchBlockData.setLit(false);
                block.setBlockData(redstoneWallTorchBlockData);
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
                MailboxStructure mailbox = getMailboxStructureFromPart(block);

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
                MailboxStructure mailbox = getMailboxStructureFromPart(block);

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

    private MailboxStructure getMailboxStructureFromPart(Block partOfMailbox) {
        World world = partOfMailbox.getWorld();
        Location location = partOfMailbox.getLocation();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        MailboxStructure structure = new MailboxStructure();

        switch (partOfMailbox.getType()) {
            case BARREL:
                structure.barrel = partOfMailbox;
                Directional directional = (Directional)partOfMailbox.getBlockData();
                BlockFace facing = directional.getFacing();

                Block possibleFenceBlock = blockAtCoordinate(x, y - 1, z, world);

                if (possibleFenceBlock.getType() == Material.OAK_FENCE) {
                    structure.fence = possibleFenceBlock;
                }

                BlockFace mailboxFlagFace = null;

                switch (facing) {
                    case NORTH:
                        mailboxFlagFace = BlockFace.EAST;
                        break;

                    case EAST:
                        mailboxFlagFace = BlockFace.SOUTH;
                        break;

                    case SOUTH:
                        mailboxFlagFace = BlockFace.WEST;
                        break;

                    case WEST:
                        mailboxFlagFace = BlockFace.NORTH;
                        break;
                }

                if (mailboxFlagFace != null) {
                    structure.redstoneWallTorch = partOfMailbox.getRelative(mailboxFlagFace);
                }

                break;

            case OAK_FENCE:
                Block possibleBarrel = partOfMailbox.getRelative(BlockFace.UP);

                if (possibleBarrel.getType() == Material.BARREL) {
                    structure = getMailboxStructureFromPart(possibleBarrel);
                }

                break;

            case REDSTONE_WALL_TORCH:
                try {
                    structure = getMailboxStructureFromPart(AttachableHelper.getAttachedBlock(partOfMailbox));
                } catch (Exception e) {
                }
                break;
        }

        return structure;
    }

    private Block blockAtCoordinate(int x, int y, int z, World world) {
        Location location = new Location(world, x, y, z);

        return location.getBlock();
    }
}
