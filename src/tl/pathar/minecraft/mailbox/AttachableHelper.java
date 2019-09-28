package tl.pathar.minecraft.mailbox;

import jdk.jshell.spi.ExecutionControl;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.material.Attachable;

public class AttachableHelper {
    public static Block getAttachedBlock(Block attachedBlock) throws ExecutionControl.NotImplementedException {
        if (attachedBlock.getType() == Material.REDSTONE_WALL_TORCH) {
            Directional directional = (Directional)attachedBlock.getBlockData();

            BlockFace facing = directional.getFacing();

            return attachedBlock.getRelative(facing.getOppositeFace());
        } else {
            throw new ExecutionControl.NotImplementedException("Only redstone wall torches are available at this time.");
        }
    }
}
