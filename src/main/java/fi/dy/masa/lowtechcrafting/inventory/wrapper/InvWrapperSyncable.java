package fi.dy.masa.lowtechcrafting.inventory.wrapper;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import fi.dy.masa.lowtechcrafting.inventory.IItemHandlerSyncable;

public class InvWrapperSyncable extends InvWrapper implements IItemHandlerSyncable
{
    public InvWrapperSyncable(Container inv)
    {
        super(inv);
    }

    @Override
    public void syncStackInSlot(int slot, ItemStack stack)
    {
        if (this.getInv() instanceof IItemHandlerSyncable)
        {
            ((IItemHandlerSyncable) this.getInv()).syncStackInSlot(slot, stack);
        }
        else
        {
            this.setStackInSlot(slot, stack);
        }
    }
}
