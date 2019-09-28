package tl.pathar.minecraft.mailbox;

import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Fence;
import org.bukkit.block.data.type.RedstoneWallTorch;

public class MailboxStructure {
    public Block barrel = null;
    public Block fence = null;
    public Block redstoneWallTorch = null;

    public boolean isValidMailbox() {
        // Not a very good verification, but it's quick.
        if (barrel == null || fence == null || redstoneWallTorch == null) {
            return false;
        }

        return true;
    }
}