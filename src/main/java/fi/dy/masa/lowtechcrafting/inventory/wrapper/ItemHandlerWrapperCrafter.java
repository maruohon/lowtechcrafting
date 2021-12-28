package fi.dy.masa.lowtechcrafting.inventory.wrapper;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import fi.dy.masa.lowtechcrafting.inventory.IItemHandlerSize;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

public class ItemHandlerWrapperCrafter implements IItemHandler, IItemHandlerSize, INBTSerializable<CompoundTag>
{
    private final IItemHandlerModifiable inventoryCraftingGridBase;
    private final IItemHandler inventoryOutputBuffer;
    private final IItemHandler inventoryCraftingOutput;
    private final InventoryCraftingWrapper inventoryCraftingWrapper;

    public ItemHandlerWrapperCrafter(
            IItemHandlerModifiable inventoryCraftingGridBase,
            IItemHandler inventoryOutputBuffer,
            IItemHandler inventoryCraftingOutput,
            InventoryCraftingWrapper inventoryCraftingWrapper)
    {
        this.inventoryCraftingGridBase = inventoryCraftingGridBase;
        this.inventoryOutputBuffer = inventoryOutputBuffer;
        this.inventoryCraftingOutput = inventoryCraftingOutput;
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
            ItemStack stack = this.inventoryOutputBuffer.getStackInSlot(0);

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
            ItemStack stack = this.inventoryOutputBuffer.getStackInSlot(0);

            if (simulate == false)
            {
                // If the output buffer inventory slot is empty, then craft and move one set of crafted items into it
                if (stack.isEmpty())
                {
                    // Craft one set of items (the amount is internally set to the current stack size)
                    stack = this.inventoryCraftingOutput.extractItem(0, 64, false);
                    this.inventoryOutputBuffer.insertItem(0, stack, false);
                }

                // ... and then extract from that output buffer inventory slot
                return this.inventoryOutputBuffer.extractItem(0, amount, false);
            }
            // Simulating...
            else
            {
                // Items in the output buffer, use those
                if (stack.isEmpty() == false)
                {
                    return this.inventoryOutputBuffer.extractItem(0, amount, true);
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
                this.inventoryCraftingWrapper.checkUpdateGridCacheForSlot(slot - 1);
            }

            return stack;
        }
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        if (this.isItemValid(slot, stack) == false)
        {
            return stack;
        }

        stack = this.inventoryCraftingGridBase.insertItem(slot - 1, stack, simulate);

        // If actually adding items, mark the grid dirty
        if (simulate == false)
        {
            this.inventoryCraftingWrapper.checkUpdateGridCacheForSlot(slot - 1);
        }

        return stack;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack)
    {
        // Don't allow anything into the crafting output slot
        if (slot == 0)
        {
            return false;
        }

        return true;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
    }
}