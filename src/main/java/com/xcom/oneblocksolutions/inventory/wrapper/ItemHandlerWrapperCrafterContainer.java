package com.xcom.oneblocksolutions.inventory.wrapper;

import com.xcom.oneblocksolutions.inventory.IItemHandlerSelective;
import com.xcom.oneblocksolutions.inventory.IItemHandlerSize;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * Wraps the "base" IItemHandler (which has no slot-specific insert or extract restrictions),
 * and the "wrapper handler" which is the external-facing inventory with the slot-specific restrictions
 * in place. Uses the base handler for everything else except insertItem(), which is called via the wrapper
 * handler instead. The idea of this is to have the slot-specific checks in place when manually putting
 * items into Slots in a container, but still allow manually taking items from every slot,
 * whereas the wrapper (ie. the externally exposed inventory) might have restriction in place
 * on what can be extracted from what slots, for example furnace fuel and input item slots.
 * Also, another important factor is having the setStackInSlot() available for the Container to sync slots!
 *
 * @author masa
 */
public class ItemHandlerWrapperCrafterContainer implements IItemHandlerModifiable, IItemHandlerSelective, IItemHandlerSize//, IItemHandlerSyncable
{
    protected final IItemHandlerModifiable baseHandlerModifiable;
    protected final IItemHandler wrapperHandler;
    protected final IItemHandlerModifiable outputSlot;
    private final boolean useWrapperForExtract;

    public ItemHandlerWrapperCrafterContainer(IItemHandlerModifiable baseHandler, IItemHandler wrapperHandler, IItemHandlerModifiable outputSlot)
    {
        this(baseHandler, wrapperHandler, outputSlot, false);
    }

    public ItemHandlerWrapperCrafterContainer(IItemHandlerModifiable baseHandler, IItemHandler wrapperHandler,
            IItemHandlerModifiable outputSlot, boolean useWrapperForExtract)
    {
        this.baseHandlerModifiable = baseHandler;
        this.wrapperHandler = wrapperHandler;
        this.outputSlot = outputSlot;
        this.useWrapperForExtract = useWrapperForExtract;
    }

    @Override
    public int getSlots()
    {
        return this.wrapperHandler.getSlots();
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return this.wrapperHandler.getSlotLimit(slot);
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        return this.wrapperHandler.getStackInSlot(slot);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack)
    {
        if (slot == 0)
        {
            this.outputSlot.setStackInSlot(slot, stack);
        }
        else
        {
            this.baseHandlerModifiable.setStackInSlot(slot - 1, stack);
        }
    }

    /*
    @Override
    public void syncStackInSlot(int slot, ItemStack stack)
    {
        if (slot == 0)
        {
            this.outputSlot.setStackInSlot(slot, stack);
        }
        else
        {
            this.baseHandlerModifiable.setStackInSlot(slot - 1, stack);
        }
    }
    */

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        return this.wrapperHandler.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (this.useWrapperForExtract)
        {
            return this.wrapperHandler.extractItem(slot, amount, simulate);
        }
        else
        {
            return this.baseHandlerModifiable.extractItem(slot, amount, simulate);
        }
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack)
    {
        if (this.wrapperHandler instanceof IItemHandlerSelective)
        {
            return ((IItemHandlerSelective) this.wrapperHandler).isItemValidForSlot(slot, stack);
        }

        return true;
    }

    @Override
    public boolean canExtractFromSlot(int slot)
    {
        if (this.wrapperHandler instanceof IItemHandlerSelective)
        {
            return ((IItemHandlerSelective) this.wrapperHandler).canExtractFromSlot(slot);
        }

        return true;
    }

    @Override
    public int getInventoryStackLimit()
    {
        if (this.wrapperHandler instanceof IItemHandlerSize)
        {
            return ((IItemHandlerSize) this.wrapperHandler).getInventoryStackLimit();
        }

        return 64;
    }

    @Override
    public int getItemStackLimit(int slot, ItemStack stack)
    {
        if (this.wrapperHandler instanceof IItemHandlerSize)
        {
            return ((IItemHandlerSize) this.wrapperHandler).getItemStackLimit(slot, stack);
        }

        return this.getInventoryStackLimit();
    }
}
