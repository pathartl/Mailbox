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
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static java.lang.Integer.parseInt;

public class MailboxHelper {
    public static void checkMail(MailboxPlugin plugin, Player player) {
        Database database = plugin.sqlLib.getDatabase("Mailbox");

        checkMail(database, player);
    }

    public static void checkMail(Database database, Player player) {

        String databaseCoordinates = (String)database.queryValue("SELECT X || ',' || Y || ',' || Z as Coordinates FROM Mailboxes WHERE Player = \"" + player.getName() + "\"", "Coordinates");
        String[] explodedCoordinates = databaseCoordinates.split(",");

        int[] coordinates = new int[] {
                parseInt(explodedCoordinates[0]),
                parseInt(explodedCoordinates[1]),
                parseInt(explodedCoordinates[2])
        };

        Block block = player.getWorld().getBlockAt(coordinates[0], coordinates[1], coordinates[2]);
        MailboxStructure mailbox = getMailboxStructureFromPart(block);

        if (mailbox.isValidMailbox()) {
            Barrel barrelState = (Barrel) block.getState();
            Inventory barrelInventory = barrelState.getInventory();
            ItemStack[] stack = barrelInventory.getContents();

            boolean gotMail = false;

            for (int i = 0; i < stack.length && !gotMail; i++) {
                if (stack[i] != null) {
                    gotMail = true;
                }
            }

            if (gotMail) {
                player.sendMessage("You've got mail!");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            }
        }
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
