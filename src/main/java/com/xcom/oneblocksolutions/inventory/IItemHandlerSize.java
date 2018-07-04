package com.xcom.oneblocksolutions.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public interface IItemHandlerSize extends IItemHandler
{
    public int getInventoryStackLimit();

    public int getItemStackLimit(int slot, ItemStack stack);
}