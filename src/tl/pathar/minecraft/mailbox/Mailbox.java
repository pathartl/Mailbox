package tl.pathar.minecraft.mailbox;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Mailbox {
    public MailboxStructure structure;
    public Player owner;
    public boolean hasMail;
    public ItemStack[] stacks;
    public long lastUpdated;
}
