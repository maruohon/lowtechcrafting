package fi.dy.masa.lowtechcrafting.inventory;

import net.minecraft.item.ItemStack;

public interface IItemHandlerSelective
{
    public boolean isItemValidForSlot(int slot, ItemStack stack);

    public boolean canExtractFromSlot(int slot);
}
