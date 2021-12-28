package fi.dy.masa.lowtechcrafting.inventory.wrapper;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.RangedWrapper;

public class PlayerMainInvWrapperNoSync extends RangedWrapper
{
    private final Inventory inventoryPlayer;

    public PlayerMainInvWrapperNoSync(Inventory inv)
    {
        super(new InvWrapper(inv), 0, inv.items.size());

        this.inventoryPlayer = inv;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        ItemStack stackRemaining = super.insertItem(slot, stack, simulate);

        if (stackRemaining.isEmpty() || stackRemaining.getCount() != stack.getCount())
        {
            // the stack in the slot changed, animate it
            ItemStack stackSlot = this.getStackInSlot(slot);

            if (stackSlot.isEmpty() == false)
            {
                if (this.getPlayerInventory().player.getCommandSenderWorld().isClientSide)
                {
                    stackSlot.setPopTime(5);
                }
            }
        }

        return stackRemaining;
    }

    public Inventory getPlayerInventory()
    {
        return inventoryPlayer;
    }
}
