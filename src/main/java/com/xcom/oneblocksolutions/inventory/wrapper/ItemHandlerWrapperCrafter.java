package com.xcom.oneblocksolutions.inventory.wrapper;

import com.xcom.oneblocksolutions.inventory.IItemHandlerSize;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

public class ItemHandlerWrapperCrafter implements IItemHandler, IItemHandlerSize, INBTSerializable<NBTTagCompound>
{
    private final IItemHandlerModifiable inventoryCraftingGridBase;
    private final IItemHandler inventoryCraftingOutput;
    private final IItemHandler inventoryOutput;
    private final InventoryCraftingWrapper inventoryCraftingWrapper;

    public ItemHandlerWrapperCrafter(
            IItemHandlerModifiable inventoryCraftingGridBase,
            IItemHandler inventoryCraftingOutput,
            InventoryCraftingWrapper inventoryCraftingWrapper,
            IItemHandler inventoryOutput)
    {
        this.inventoryCraftingGridBase = inventoryCraftingGridBase;
        this.inventoryCraftingOutput = inventoryCraftingOutput;
        this.inventoryOutput = inventoryOutput;
        this.inventoryCraftingWrapper = inventoryCraftingWrapper;
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return 64;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public int getItemStackLimit(int slot, ItemStack stack)
    {
        return 64;
    }

    @Override
    public int getSlots()
    {
        return 10;
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        // The first "virtual slot" is for the output slot.
        // The rest of the slots are for the crafting grid.
        if (slot == 0)
        {
            ItemStack stack = this.inventoryCraftingOutput.getStackInSlot(0);

            // If there are items in the output inventory (after a partial craft operation), show those
            if (stack.isEmpty() == false)
            {
                return stack;
            }
            // Otherwise show the current crafting recipe output
            else
            {
                return this.inventoryCraftingOutput.getStackInSlot(0);
            }
        }

        return this.inventoryCraftingGridBase.getStackInSlot(slot - 1);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        // Don't allow anything into the crafting output slots
        if (slot == 0)
        {
            return stack;
        }

        return this.inventoryCraftingGridBase.insertItem(slot - 1, stack, simulate);
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        return new NBTTagCompound();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
    }
}