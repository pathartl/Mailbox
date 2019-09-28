package tl.pathar.minecraft.mailbox;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class AttachableHelper {
    public static Block getAttachedBlock(Block attachedBlock) throws NotImplementedException {
        if (attachedBlock.getType() == Material.REDSTONE_WALL_TORCH) {
            Directional directional = (Directional)attachedBlock.getBlockData();

            BlockFace facing = directional.getFacing();

            return attachedBlock.getRelative(facing.getOppositeFace());
        } else {
            throw new NotImplementedException();
        }
    }
}
