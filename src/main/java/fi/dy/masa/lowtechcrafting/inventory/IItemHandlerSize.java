package fi.dy.masa.lowtechcrafting.inventory;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public interface IItemHandlerSize extends IItemHandler
{
    int getInventoryStackLimit();

    int getItemStackLimit(int slot, ItemStack stack);
}