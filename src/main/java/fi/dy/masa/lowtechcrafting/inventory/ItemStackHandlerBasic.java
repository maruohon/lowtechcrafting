package fi.dy.masa.lowtechcrafting.inventory;

import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;
import fi.dy.masa.lowtechcrafting.util.NBTUtils;

public class ItemStackHandlerBasic implements IItemHandlerModifiable, INBTSerializable<CompoundTag>, IItemHandlerSelective, IItemHandlerSize
{
    protected final NonNullList<ItemStack> items;
    private final boolean allowCustomStackSizes;
    private int inventorySize;
    private int stackLimit;
    private String tagName;

    public ItemStackHandlerBasic(int invSize)
    {
        this(invSize, 64, false, "Items");
    }

    public ItemStackHandlerBasic(int invSize, int stackLimit, boolean allowCustomStackSizes, String tagName)
    {
        this.inventorySize = invSize;
        this.tagName = tagName;
        this.allowCustomStackSizes = allowCustomStackSizes;
        this.items = NonNullList.withSize(invSize, ItemStack.EMPTY);
        this.setStackLimit(stackLimit);
    }

    public void setInventorySize(int invSize)
    {
        this.inventorySize = Mth.clamp(invSize, 0, this.items.size());
    }

    @Override
    public int getSlots()
    {
        return this.inventorySize;
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return this.stackLimit;
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot)
    {
        return this.items.get(slot);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack)
    {
        this.items.set(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
        this.onContentsChanged(slot);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
    {
        if (stack.isEmpty() || this.isItemValidForSlot(slot, stack) == false)
        {
            return stack;
        }

        ItemStack existingStack = this.items.get(slot);
        boolean hasStack = existingStack.isEmpty() == false;
        int existingStackSize = hasStack ? existingStack.getCount() : 0;
        int max = this.getItemStackLimit(slot, stack);

        if (this.allowCustomStackSizes == false)
        {
            max = Math.min(max, stack.getMaxStackSize());
        }
        // Don't allow stacking non-stacking items
        else if (stack.getMaxStackSize() == 1)
        {
            max = 1;
        }

        // Existing items in the target slot
        if (hasStack)
        {
            // If the slot is already full, or the to-be-inserted item is different
            if (existingStackSize >= max ||
                stack.getItem() != existingStack.getItem() ||
                ItemStack.tagMatches(stack, existingStack) == false)
            {
                return stack;
            }
        }

        int amount = Math.min(max - existingStackSize, stack.getCount());

        if (amount <= 0)
        {
            return stack;
        }

        if (simulate == false)
        {
            if (hasStack)
            {
                existingStack.grow(amount);
            }
            else
            {
                ItemStack newStack = stack.copy();
                newStack.setCount(amount);
                this.items.set(slot, newStack);
            }

            this.onContentsChanged(slot);
        }

        if (amount < stack.getCount())
        {
            ItemStack stackRemaining = stack.copy();
            stackRemaining.shrink(amount);

            return stackRemaining;
        }
        else
        {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (this.canExtractFromSlot(slot) == false)
        {
            return ItemStack.EMPTY;
        }

        ItemStack existingStack = this.items.get(slot);

        if (existingStack.isEmpty())
        {
            return ItemStack.EMPTY;
        }

        amount = Math.min(amount, Math.min(existingStack.getCount(), existingStack.getMaxStackSize()));

        ItemStack stack;

        if (simulate)
        {
            stack = existingStack.copy();
            stack.setCount(amount);

            return stack;
        }
        else
        {
            if (amount == existingStack.getCount())
            {
                stack = existingStack;
                this.items.set(slot, ItemStack.EMPTY);
            }
            else
            {
                stack = existingStack.split(amount);

                if (existingStack.getCount() <= 0)
                {
                    this.items.set(slot, ItemStack.EMPTY);
                }
            }

            this.onContentsChanged(slot);
        }

        return stack;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag wrapper = new CompoundTag();
        CompoundTag nbt = new CompoundTag();

        if (this.inventorySize != this.items.size())
        {
            nbt.putByte("SlotCount", (byte) this.inventorySize);
        }

        NBTUtils.writeItemsToTag(nbt, this.items, "Items", true);
        wrapper.put(this.tagName, nbt);

        return wrapper;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        nbt = nbt.getCompound(this.tagName);

        if (nbt.contains("SlotCount", Tag.TAG_BYTE))
        {
            this.setInventorySize(nbt.getByte("SlotCount"));
        }

        NBTUtils.readStoredItemsFromTag(nbt, this.items, "Items");
    }

    @Override
    public int getInventoryStackLimit()
    {
        return this.stackLimit;
    }

    @Override
    public int getItemStackLimit(int slot, ItemStack stack)
    {
        if (this.allowCustomStackSizes)
        {
            return this.getInventoryStackLimit();
        }

        return Math.min(stack.getMaxStackSize(), this.getSlotLimit(slot));
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack)
    {
        return true;
    }

    @Override
    public boolean canExtractFromSlot(int slot)
    {
        return true;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack)
    {
        return true;
    }

    public void setStackLimit(int stackLimit)
    {
        this.stackLimit = stackLimit;
    }

    public void onContentsChanged(int slot)
    {
    }

    /**
     * Sets the NBTTagList tag name that stores the items of this inventory in the container ItemStack
     * @param tagName
     */
    public void setItemStorageTagName(@Nonnull String tagName)
    {
        this.tagName = tagName;
    }

    public String getItemStorageTagName()
    {
        return this.tagName;
    }
}
