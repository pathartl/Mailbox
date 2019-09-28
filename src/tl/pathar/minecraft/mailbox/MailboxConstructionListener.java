package tl.pathar.minecraft.mailbox;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Fence;
import org.bukkit.block.data.type.RedstoneWallTorch;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class MailboxConstructionListener implements Listener {
    private Mailbox plugin;

    public MailboxConstructionListener(Mailbox _plugin) {
        plugin = _plugin;
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

                    Barrel barrel = (Barrel) mailbox.barrel.getState();

                    barrel.setCustomName(player.getName() + "'s Mailbox");
                }
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

                Block possibleFenceBlock = blockAtCoordinate(x, y - 1, z, world);

                if (possibleFenceBlock.getType() == Material.OAK_FENCE) {
                    structure.fence = possibleFenceBlock;
                }

                Block[] surroundingBlocks = new Block[4];

                surroundingBlocks[0] = blockAtCoordinate(x + 1, y, z, world);
                surroundingBlocks[1] = blockAtCoordinate(x - 1, y, z, world);
                surroundingBlocks[2] = blockAtCoordinate(x, y, z + 1, world);
                surroundingBlocks[3] = blockAtCoordinate(x, y, z - 1, world);

                boolean alreadyFoundTorch = false;
                boolean otherSpotsEmpty = true;

                for (int i = 0; i < 4; i++) {
                    // If anything but redstone torch or air, fail
                    if (surroundingBlocks[i].getType() != Material.AIR && surroundingBlocks[i].getType() != Material.REDSTONE_WALL_TORCH) {
                        otherSpotsEmpty = false;
                    }

                    if (surroundingBlocks[i].getType() == Material.REDSTONE_WALL_TORCH) {
                        // If we've found a torch here, but have already found a torch in our loop, fail
                        if (alreadyFoundTorch) {
                            otherSpotsEmpty = false;
                        }

                        alreadyFoundTorch = true;
                    }

                    if (alreadyFoundTorch && otherSpotsEmpty) {
                        structure.redstoneWallTorch = surroundingBlocks[i];
                    }
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
