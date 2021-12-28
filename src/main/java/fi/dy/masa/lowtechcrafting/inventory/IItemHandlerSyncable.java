package fi.dy.masa.lowtechcrafting.inventory;

import net.minecraft.world.item.ItemStack;

public interface IItemHandlerSyncable
{
    /**
     * Used to sync an ItemStack into a slot, even if the stack normally
     * wouldn't be allowed in that slot.
     */
    void syncStackInSlot(int slot, ItemStack stack);
}
