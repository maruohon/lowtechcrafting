package fi.dy.masa.lowtechcrafting.inventory;

import net.minecraft.item.ItemStack;

public interface IItemHandlerSelective
{
    boolean isItemValidForSlot(int slot, ItemStack stack);

    boolean canExtractFromSlot(int slot);
}
