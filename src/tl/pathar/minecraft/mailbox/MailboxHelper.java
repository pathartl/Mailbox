package tl.pathar.minecraft.mailbox;

import com.pablo67340.SQLiteLib.Database.Database;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.RedstoneWallTorch;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import static java.lang.Integer.parseInt;

public class MailboxHelper {
    public static Mailbox getMailbox(Database database, Player player, MailboxPlugin plugin) {
        Mailbox mailbox = null;
        String databaseCoordinates = (String)database.queryValue("SELECT X || ',' || Y || ',' || Z as Coordinates FROM Mailboxes WHERE Player = \"" + player.getName() + "\"", "Coordinates");
        String[] explodedCoordinates = databaseCoordinates.split(",");

        int[] coordinates = new int[] {
                parseInt(explodedCoordinates[0]),
                parseInt(explodedCoordinates[1]),
                parseInt(explodedCoordinates[2])
        };

        Block block = player.getWorld().getBlockAt(coordinates[0], coordinates[1], coordinates[2]);
        MailboxStructure mailboxStructure = getMailboxStructureFromPart(block);

        if (mailboxStructure.isValidMailbox()) {
            mailbox = new Mailbox();

            mailbox.structure = mailboxStructure;
            mailbox.owner = player;
            mailbox.hasMail = false;

            Barrel barrelState = (Barrel) block.getState();
            Inventory barrelInventory = barrelState.getInventory();

            mailbox.stacks = barrelInventory.getContents();

            for (int i = 0; i < mailbox.stacks.length && !mailbox.hasMail; i++) {
                if (mailbox.stacks[i] != null) {
                    mailbox.hasMail = true;
                }
            }

            mailbox.structure.redstoneWallTorch.setMetadata("IsMailboxFlag", new FixedMetadataValue(plugin, true));
            mailbox.structure.redstoneWallTorch.setMetadata("MailboxHasMail", new FixedMetadataValue(plugin, mailbox.hasMail));
        }

        return mailbox;
    }

    public static Mailbox checkMail(MailboxPlugin plugin, Player player, boolean calledByOwner) {
        Database database = plugin.sqlLib.getDatabase("Mailbox");

        return checkMail(database, player, plugin, calledByOwner);
    }

    public static Mailbox checkMail(Database database, Player player, MailboxPlugin plugin, boolean calledByOwner) {
        Mailbox mailbox = getMailbox(database, player, plugin);

        if (mailbox != null) {
            if (mailbox.hasMail) {
                player.sendMessage("You've got mail!");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                mailbox.structure.redstoneWallTorch.setMetadata("MailboxHasMail", new FixedMetadataValue(plugin, true));
            } else {
                if (calledByOwner) {
                    player.sendMessage("You have no mail.");
                }

                mailbox.structure.redstoneWallTorch.setMetadata("MailboxHasMail", new FixedMetadataValue(plugin, false));

                RedstoneWallTorch redstoneWallTorchBlockData = (RedstoneWallTorch) mailbox.structure.redstoneWallTorch.getBlockData();
                redstoneWallTorchBlockData.setLit(false);
                mailbox.structure.redstoneWallTorch.setBlockData(redstoneWallTorchBlockData);
            }
        } else {
            player.sendMessage("You do not have a mailbox.");
        }

        return mailbox;
    }

    public static MailboxStructure getMailboxStructureFromPart(Block partOfMailbox) {
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

    private static Block blockAtCoordinate(int x, int y, int z, World world) {
        Location location = new Location(world, x, y, z);

        return location.getBlock();
    }
}
