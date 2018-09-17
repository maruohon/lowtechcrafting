package fi.dy.masa.lowtechcrafting.inventory.wrapper;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import fi.dy.masa.lowtechcrafting.inventory.IItemHandlerSize;

public class ItemHandlerWrapperCrafter implements IItemHandler, IItemHandlerSize, INBTSerializable<NBTTagCompound>
{
    private final IItemHandlerModifiable inventoryCraftingGridBase;
    private final IItemHandler inventoryCraftingOutput;
    private final IItemHandler inventoryOutput;
    private final InventoryCraftingWrapper inventoryCraftingWrapper;

    public ItemHandlerWrapperCrafter(
            IItemHandlerModifiable inventoryCraftingGridBase,
            IItemHandler inventoryOutput,
            IItemHandler inventoryCraftingOutput,
            InventoryCraftingWrapper inventoryCraftingWrapper)
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
            ItemStack stack = this.inventoryOutput.getStackInSlot(0);

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
        // Crafting output slot
        if (slot == 0)
        {
            ItemStack stack = this.inventoryOutput.getStackInSlot(0);

            if (simulate == false)
            {
                // If the output buffer inventory slot is empty, then craft and move one set of crafted items into it
                if (stack.isEmpty())
                {
                    // Craft one set of items
                    stack = this.inventoryCraftingOutput.extractItem(0, stack.getCount(), false);
                    this.inventoryOutput.insertItem(0, stack, false);
                }

                // ... and then extract from that output buffer inventory slot
                return this.inventoryOutput.extractItem(0, amount, false);
            }
            // Simulating...
            else
            {
                // Items in the output buffer, use those
                if (stack.isEmpty() == false)
                {
                    return this.inventoryOutput.extractItem(0, amount, true);
                }

                // No output buffer items, use the crafting recipe output instead
                return this.inventoryCraftingOutput.extractItem(0, amount, true);
            }
        }
        // Crafting grid slots
        else
        {
            ItemStack stack = this.inventoryCraftingGridBase.extractItem(slot - 1, amount, simulate);

            if (simulate == false)
            {
                this.inventoryCraftingWrapper.markDirty();
            }

            return stack;
        }
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        // Don't allow anything into the crafting output slots
        if (slot == 0)
        {
            return stack;
        }

        stack = this.inventoryCraftingGridBase.insertItem(slot - 1, stack, simulate);

        // If actually adding items, update the recipe output
        if (simulate == false)
        {
            this.inventoryCraftingWrapper.markDirty();
        }

        return stack;
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