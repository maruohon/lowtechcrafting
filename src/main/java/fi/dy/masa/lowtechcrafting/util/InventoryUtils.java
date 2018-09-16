package fi.dy.masa.lowtechcrafting.util;

import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.oredict.OreDictionary;
import fi.dy.masa.lowtechcrafting.inventory.IItemHandlerSize;
import fi.dy.masa.lowtechcrafting.inventory.ItemStackHandlerBasic;
import fi.dy.masa.lowtechcrafting.inventory.container.base.SlotRange;

public class InventoryUtils
{
    public static final int SLOT_ITER_LIMIT = 256;
    public static final ItemStackHandlerBasic NULL_INV = new ItemStackHandlerBasic(0);

    public static int calcRedstoneFromInventory(IItemHandler inv)
    {
        final int slots = inv.getSlots();
        int items = 0;
        int capacity = 0;

        for (int slot = 0; slot < slots; ++slot)
        {
            ItemStack stack = inv.getStackInSlot(slot);

            if ((inv instanceof IItemHandlerSize) && stack.isEmpty() == false)
            {
                capacity += ((IItemHandlerSize) inv).getItemStackLimit(slot, stack);
            }
            else
            {
                capacity += inv.getSlotLimit(slot);
            }

            if (stack.isEmpty() == false)
            {
                items += stack.getCount();
            }
        }

        if (capacity > 0)
        {
            int strength = (14 * items) / capacity;

            // Emit a signal strength of 1 as soon as there is one item in the inventory
            if (items > 0)
            {
                strength += 1;
            }

            return strength;
        }

        return 0;
    }

    /**
     * Drops all the ItemStacks from the given inventory into the world as EntityItems
     * @param world
     * @param pos
     * @param inv
     */
    public static void dropInventoryContentsInWorld(World world, BlockPos pos, IItemHandler inv)
    {
        final int invSize = inv.getSlots();

        for (int slot = 0; slot < invSize; ++slot)
        {
            ItemStack stack = inv.getStackInSlot(slot);

            if (stack.isEmpty() == false)
            {
                EntityUtils.dropItemStacksInWorld(world, pos, stack, -1, true);
            }
        }
    }

    /**
     * Checks if the given ItemStacks have the same item, damage and NBT. Ignores stack sizes.
     * Can be given empty ItemStacks as input.
     * @param stack1
     * @param stack2
     * @return Returns true if the ItemStacks have the same item, damage and NBT tags.
     */
    public static boolean areItemStacksEqual(@Nonnull ItemStack stack1, @Nonnull ItemStack stack2)
    {
        if (stack1.isEmpty() || stack2.isEmpty())
        {
            return stack1.isEmpty() == stack2.isEmpty();
        }

        return stack1.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack1, stack2);
    }

    /**
     * Checks if the ItemStack <b>stackTarget</b> is valid to be used as a substitution
     * for <b>stackReference</b> via the OreDictionary keys.
     * @param stackTarget
     * @param stackReference
     * @return
     */
    public static boolean areItemStacksOreDictMatch(@Nonnull ItemStack stackTarget, @Nonnull ItemStack stackReference)
    {
        int[] ids = OreDictionary.getOreIDs(stackReference);

        for (int id : ids)
        {
            List<ItemStack> oreStacks = OreDictionary.getOres(OreDictionary.getOreName(id), false);

            for (ItemStack oreStack : oreStacks)
            {
                if (OreDictionary.itemMatches(stackTarget, oreStack, false))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns the slot number of the first empty slot in the given inventory, or -1 if there are no empty slots.
     * @return first empty slot, or -1
     */
    public static int getFirstEmptySlot(IItemHandler inv)
    {
        final int invSize = inv.getSlots();

        for (int slot = 0; slot < invSize; ++slot)
        {
            if (inv.getStackInSlot(slot).isEmpty())
            {
                return slot;
            }
        }

        return -1;
    }

    /**
     * Returns the slot number of the next non-empty slot, starting from startSlot and wrapping
     * around the end of the inventory. If no items are found, -1 is returned.
     * @param inv
     * @param startSlot
     * @return the slot number of the next non-empty slot, or -1
     */
    public static int getNextNonEmptySlot(IItemHandler inv, int startSlot)
    {
        final int invSize = inv.getSlots();
        int slot = startSlot;

        for (int i = 0; i < invSize; ++i, ++slot)
        {
            if (slot >= invSize)
            {
                slot = 0;
            }

            if (inv.getStackInSlot(slot).isEmpty() == false)
            {
                return slot;
            }
        }

        return -1;
    }

    /**
     * Get the slot number of the first slot containing a matching ItemStack (including NBT, ignoring stackSize) in the given inventory.
     * Note: stack can be empty.
     * @return The slot number of the first slot with a matching ItemStack, or -1 if there were no matches.
     */
    public static int getSlotOfFirstMatchingItemStack(IItemHandler inv, ItemStack stack)
    {
        return getSlotOfFirstMatchingItemStackWithinSlotRange(inv, stack, 0, inv.getSlots());
    }

    /**
     * Get the slot number of the first slot containing a matching ItemStack (including NBT, ignoring stackSize) within the given slot range.
     * Note: stack can be empty.
     * @return The slot number of the first slot with a matching ItemStack, or -1 if there were no matches.
     */
    public static int getSlotOfFirstMatchingItemStackWithinSlotRange(IItemHandler inv, ItemStack stack, int slotStart, int slotLastExc)
    {
        final int slotLast = Math.min(inv.getSlots() - 1, slotLastExc - 1);

        for (int slot = slotStart; slot <= slotLast; ++slot)
        {
            ItemStack stackSlot = inv.getStackInSlot(slot);

            if (areItemStacksEqual(stackSlot, stack))
            {
                return slot;
            }
        }

        return -1;
    }

    /**
     * @param inv
     * @return true if all the slots in the inventory are empty
     */
    public static boolean isInventoryEmpty(IItemHandler inv)
    {
        final int invSize = inv.getSlots();

        for (int slot = 0; slot < invSize; ++slot)
        {
            if (inv.getStackInSlot(slot).isEmpty() == false)
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks whether all slots in the inventory <b>inv</b> match the stacks in the list <b>template</b>.
     * Note that the list must be at least as long as the inventory has slots!
     * @param inv
     * @param template
     * @return
     */
    public static boolean doesInventoryMatchTemplate(IItemHandler inv, NonNullList<ItemStack> template)
    {
        final int invSize = inv.getSlots();

        for (int slot = 0; slot < invSize; slot++)
        {
            if (areItemStacksEqual(inv.getStackInSlot(slot), template.get(slot)) == false)
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks whether there are items in the inventory <b>inv</b>, that shouldn't be there according to <b>template</b>.
     * This check is per-slot.<br>
     * Note that the list must be at least as long as the inventory has slots!
     * @param inv
     * @param template
     * @return
     */
    public static boolean doesInventoryHaveNonMatchingItems(IItemHandler inv, NonNullList<ItemStack> template)
    {
        final int invSize = inv.getSlots();

        for (int slot = 0; slot < invSize; slot++)
        {
            ItemStack stackTemplate = template.get(slot);
            ItemStack stackInv = inv.getStackInSlot(slot);

            // Items in slot, which should be empty, or should be some other item
            if (stackInv.isEmpty() == false && areItemStacksEqual(stackInv, stackTemplate) == false)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Sets all the stacks in <b>inv</b> from the list <b>stacks</b>.
     * @param inv
     * @param stacks
     * @param copy if true, then the stacks are copied, instead of set to the same reference
     */
    public static void setInventoryContents(IItemHandlerModifiable inv, NonNullList<ItemStack> stacks, boolean copy)
    {
        final int invSize = inv.getSlots();

        if (copy)
        {
            for (int slot = 0; slot < invSize; slot++)
            {
                ItemStack stack = stacks.get(slot);
                inv.setStackInSlot(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
            }
        }
        else
        {
            for (int slot = 0; slot < invSize; slot++)
            {
                inv.setStackInSlot(slot, stacks.get(slot));
            }
        }
    }

    /**
     * Sets all slots to ItemStack.EMPTY
     * @param inv
     */
    public static void clearInventory(IItemHandlerModifiable inv)
    {
        final int invSize = inv.getSlots();

        for (int slot = 0; slot < invSize; slot++)
        {
            inv.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    /**
     * Creates a copy of the whole inventory and returns it in a new NonNullList.
     * @param inv
     * @return a NonNullList containing a copy of the entire inventory
     */
    public static NonNullList<ItemStack> createInventorySnapshot(IItemHandler inv)
    {
        final int invSize = inv.getSlots();
        NonNullList<ItemStack> items = NonNullList.withSize(invSize, ItemStack.EMPTY);

        for (int slot = 0; slot < invSize; slot++)
        {
            ItemStack stack = inv.getStackInSlot(slot);

            if (stack.isEmpty() == false)
            {
                items.set(slot, stack.copy());
            }
        }

        return items;
    }

    /**
     * Tries to move the entire stack from the source inventory and slot to the destination.
     * If the simulation fails to insert the entire stack, then the operation is aborted before
     * actually moving any items.
     * @param invSrc
     * @param slotSrc
     * @param invDst
     * @param slotDst
     * @return
     */
    public static InvResult tryMoveEntireStackOnly(IItemHandler invSrc, int slotSrc, IItemHandler invDst, int slotDst)
    {
        ItemStack stack = invSrc.extractItem(slotSrc, 64, true);

        if (stack.isEmpty() == false)
        {
            stack = invDst.insertItem(slotDst, stack, true);

            // Successfully inserted the entire stack while simulating, now actually move it
            if (stack.isEmpty())
            {
                stack = invSrc.extractItem(slotSrc, 64, false);
                int sizeOrig = stack.getCount();
                stack = invDst.insertItem(slotDst, stack, false);

                // Failed to move the entire stack after all...
                if (stack.isEmpty() == false)
                {
                    boolean movedSome = stack.getCount() != sizeOrig;
                    invSrc.insertItem(slotSrc, stack, false);

                    return movedSome ? InvResult.MOVED_SOME : InvResult.MOVED_NOTHING;
                }

                return InvResult.MOVED_ALL;
            }
        }

        return InvResult.MOVED_NOTHING;
    }

    /**
     * Tries to insert the given ItemStack stack to the target inventory.
     * The return value is a stack of the remaining items that couldn't be inserted.
     * If all items were successfully inserted, then an empty stack is returned.
     */
    public static ItemStack tryInsertItemStackToInventory(IItemHandler inv, ItemStack stack, boolean simulate)
    {
        final int invSize = inv.getSlots();

        for (int slot = 0; slot < invSize; ++slot)
        {
            stack = inv.insertItem(slot, stack, simulate);

            if (stack.isEmpty())
            {
                return ItemStack.EMPTY;
            }
        }

        return stack;
    }

    /**
     * Tries to insert the given ItemStack stack to the target inventory.
     * The return value is a stack of the remaining items that couldn't be inserted.
     * If all items were successfully inserted, then an empty stack is returned.
     */
    public static ItemStack tryInsertItemStackToInventoryStackFirst(IItemHandler inv, ItemStack stackIn, boolean simulate)
    {
        return tryInsertItemStackToInventoryWithinSlotRangeStackFirst(inv, stackIn, 0, inv.getSlots(), simulate);
    }

    /**
     * Tries to insert the given ItemStack stack to the target inventory, inside the given slot range.
     * The return value is a stack of the remaining items that couldn't be inserted.
     * If all items were successfully inserted, then an empty stack is returned.
     */
    public static ItemStack tryInsertItemStackToInventoryWithinSlotRangeStackFirst(IItemHandler inv, ItemStack stack,
            int slotFirst, int slotLastExc, boolean simulate)
    {
        final int slotLast = Math.min(slotLastExc - 1, inv.getSlots() - 1);

        // First try to add to existing stacks
        for (int slot = slotFirst; slot <= slotLast; ++slot)
        {
            if (inv.getStackInSlot(slot).isEmpty() == false)
            {
                stack = inv.insertItem(slot, stack, simulate);

                if (stack.isEmpty())
                {
                    return ItemStack.EMPTY;
                }
            }
        }

        // Second round, try to add to any slot
        for (int slot = slotFirst; slot <= slotLast; ++slot)
        {
            stack = inv.insertItem(slot, stack, simulate);

            if (stack.isEmpty())
            {
                return ItemStack.EMPTY;
            }
        }

        return stack;
    }

    /**
     * Tries to move all items from the inventory invSrc into invDst.
     */
    public static InvResult tryMoveAllItems(IItemHandler invSrc, IItemHandler invDst)
    {
        boolean movedAll = true;
        boolean movedSome = false;
        final int invSize = invSrc.getSlots();

        for (int slot = 0; slot < invSize; slot++)
        {
            ItemStack stack;
            int limit = SLOT_ITER_LIMIT;

            while (limit-- > 0)
            {
                stack = invSrc.extractItem(slot, 64, false);

                if (stack.isEmpty())
                {
                    break;
                }

                final int sizeOrig = stack.getCount();
                stack = tryInsertItemStackToInventory(invDst, stack, false);
                final int sizeNew = stack.getCount();

                if (sizeNew != sizeOrig)
                {
                    movedSome = true;
                }

                // Can't insert anymore items
                if (stack.isEmpty() == false)
                {
                    // Put the rest of the items back to the source inventory
                    invSrc.insertItem(slot, stack, false);
                    movedAll = false;
                    break;
                }
            }
        }

        return (movedAll && movedSome) ? InvResult.MOVED_ALL : (movedSome ? InvResult.MOVED_SOME : InvResult.MOVED_NOTHING);
    }

    /**
     * Tries to insert the given ItemStack stack to the target inventory, inside the given slot range.
     * The return value is a stack of the remaining items that couldn't be inserted.
     * If all items were successfully inserted, then null is returned.
     */
    public static ItemStack tryInsertItemStackToInventoryWithinSlotRange(IItemHandler inv, @Nonnull ItemStack stack,
            int slotStart, int slotLastExc)
    {
        final int lastSlot = Math.min(slotLastExc - 1, inv.getSlots() - 1);

        // First try to add to existing stacks
        for (int slot = slotStart; slot <= lastSlot; slot++)
        {
            if (inv.getStackInSlot(slot).isEmpty() == false)
            {
                stack = inv.insertItem(slot, stack, false);

                if (stack.isEmpty())
                {
                    return ItemStack.EMPTY;
                }
            }
        }

        // Second round, try to add to any slot
        for (int slot = slotStart; slot <= lastSlot; slot++)
        {
            stack = inv.insertItem(slot, stack, false);

            if (stack.isEmpty())
            {
                return ItemStack.EMPTY;
            }
        }

        return stack;
    }

    /**
     * Tries to move the stack from the slot <b>slotSrc</b> from <b>invSrc</b> into the other inventory <b>invDst</b>.
     * @param invSrc
     * @param invDst
     * @param slotSrc
     */
    public static InvResult tryMoveStackToOtherInventory(IItemHandler invSrc, IItemHandler invDst, int slotSrc)
    {
        ItemStack stack;
        int limit = SLOT_ITER_LIMIT;
        InvResult result = InvResult.MOVED_NOTHING;

        while (limit > 0)
        {
            // Simulated extract
            stack = invSrc.extractItem(slotSrc, 64, true);

            if (stack.isEmpty())
            {
                return limit < SLOT_ITER_LIMIT ? InvResult.MOVED_ALL : InvResult.MOVED_NOTHING;
            }

            final int sizeOrig = stack.getCount();
            stack = tryInsertItemStackToInventory(invDst, stack, false);
            final int sizeNew = stack.getCount();

            if (sizeNew != sizeOrig)
            {
                // Actual extract - extract as many items as could be inserted from the simulate-extracted stack
                invSrc.extractItem(slotSrc, sizeOrig - sizeNew, false);
                result = InvResult.MOVED_SOME;
            }

            // All items couldn't be inserted
            if (sizeNew > 0)
            {
                break;
            }

            limit--;
        }

        return result;
    }

    /**
     * Tries to move the stack from invSrc, slotSrc into the slot with the smallest
     * existing stack size in invDst, that is in the list of slots targetSlots.
     * @param invSrc
     * @param invDst
     * @param slotSrc
     * @param targetSlots
     * @return
     */
    public static InvResult tryMoveStackToSmallestStackInOtherInventory(
            IItemHandler invSrc, IItemHandler invDst, int slotSrc, List<Integer> targetSlots)
    {
        int slotSmallest = -1;
        int countSmallest = -1;

        for (int slot : targetSlots)
        {
            int count = invDst.getStackInSlot(slot).getCount();

            if (countSmallest < 0 || count < countSmallest)
            {
                countSmallest = count;
                slotSmallest = slot;
            }
        }

        if (slotSmallest != -1)
        {
            return tryMoveStack(invSrc, slotSrc, invDst, slotSmallest, 64);
        }

        return InvResult.MOVED_NOTHING;
    }

    /**
     * Tries to move the entire stack (up to 64 items) from the src slot and inventory
     * to the destination slot and inventory. Does not simulate first.
     */
    public static InvResult tryMoveStack(IItemHandler invSrc, int slotSrc, IItemHandler invDst, int slotDst)
    {
        return tryMoveStack(invSrc, slotSrc, invDst, slotDst, 64);
    }

    /**
     * Tries to move a stack up to maxAmount items from the src slot and inventory
     * to the destination slot and inventory. Does not simulate first.
     */
    public static InvResult tryMoveStack(IItemHandler invSrc, int slotSrc, IItemHandler invDst, int slotDst, int maxAmount)
    {
        ItemStack stack = invSrc.extractItem(slotSrc, maxAmount, true);

        if (stack.isEmpty() == false)
        {
            final int sizeOrig = stack.getCount();
            stack = invDst.insertItem(slotDst, stack, false);
            final int sizeNew = stack.getCount();

            if (sizeNew != sizeOrig)
            {
                // Actual extract - extract as many items as could be inserted from the simulate-extracted stack
                invSrc.extractItem(slotSrc, sizeOrig - sizeNew, false);
                return sizeNew == 0 ? InvResult.MOVED_ALL : InvResult.MOVED_SOME;
            }
        }

        return InvResult.MOVED_NOTHING;
    }

    /**
     * Extract items from the given slot until the resulting stack's stackSize equals amount
     */
    public static ItemStack extractItemsFromSlot(IItemHandler inv, int slot, int amount)
    {
        ItemStack stackExtract = inv.extractItem(slot, amount, false);

        if (stackExtract.isEmpty())
        {
            return ItemStack.EMPTY;
        }

        if ((stackExtract.getMaxStackSize() * SLOT_ITER_LIMIT) < amount && inv instanceof IItemHandlerModifiable)
        {
            amount -= stackExtract.getCount();
            ItemStack stackSlot = inv.getStackInSlot(slot);

            if (stackSlot.isEmpty() == false)
            {
                if (stackSlot.getCount() <= amount)
                {
                    stackExtract.grow(stackSlot.getCount());
                    ((IItemHandlerModifiable) inv).setStackInSlot(slot, ItemStack.EMPTY);
                }
                else
                {
                    stackExtract.grow(amount);
                    stackSlot = stackSlot.copy();
                    stackSlot.shrink(amount);
                    ((IItemHandlerModifiable) inv).setStackInSlot(slot, stackSlot);
                }
            }

            return stackExtract;
        }

        int loops = 0;

        while (stackExtract.getCount() < amount && loops < SLOT_ITER_LIMIT)
        {
            ItemStack stackTmp = inv.extractItem(slot, amount - stackExtract.getCount(), false);

            if (stackTmp.isEmpty())
            {
                break;
            }

            stackExtract.grow(stackTmp.getCount());
            loops++;
        }

        //System.out.printf("extractItemsFromSlot(): slot: %d, requested amount: %d, loops %d, extracted: %s\n", slot, amount, loops, stack);
        return stackExtract;
    }

    /**
     * Collects items from the inventory that are identical to stackTemplate and makes a new ItemStack
     * out of them, up to stackSize = maxAmount. If <b>reverse</b> is true, then the items are collected
     * starting from the end of the given inventory.
     * If no matching items are found, an empty stack is returned.
     */
    public static ItemStack collectItemsFromInventory(IItemHandler inv, @Nonnull ItemStack stackTemplate, int maxAmount, boolean reverse)
    {
        return collectItemsFromInventory(inv, stackTemplate, maxAmount, reverse, false);
    }

    /**
     * Collects items from the inventory that are identical to stackTemplate and makes a new ItemStack
     * out of them, up to stackSize = maxAmount. If <b>reverse</b> is true, then the items are collected
     * starting from the end of the given inventory.
     * If no matching items are found, an empty stack is returned.
     */
    public static ItemStack collectItemsFromInventory(IItemHandler inv, @Nonnull ItemStack stackTemplate,
            int maxAmount, boolean reverse, boolean useOreDict)
    {
        return collectItemsFromInventoryFromSlotRange(inv, stackTemplate, new SlotRange(inv), maxAmount, reverse, useOreDict);
    }

    /**
     * Collects items from the inventory from within the given SlotRange that are identical to stackTemplate and makes a new ItemStack
     * out of them, up to stackSize = maxAmount. If <b>reverse</b> is true, then the items are collected
     * starting from the end of the given inventory.
     * If no matching items are found, an empty stack is returned.
     */
    public static ItemStack collectItemsFromInventoryFromSlotRange(IItemHandler inv, @Nonnull ItemStack stackTemplate,
            SlotRange range, int amount, boolean reverse, boolean useOreDict)
    {
        if (range.first >= inv.getSlots())
        {
            return ItemStack.EMPTY;
        }

        int inc = reverse ? -1 : 1;
        final int lastSlot = Math.min(range.lastInc, inv.getSlots() - 1);
        final int start = reverse ? lastSlot : range.first;
        ItemStack stack = stackTemplate.copy();
        stack.setCount(0);
        //System.out.printf("amount: %d range: %s stack: %s start: %d inc: %d\n", amount, range, stack, start, inc);

        for (int slot = start; slot >= range.first && slot <= lastSlot && stack.getCount() < amount; slot += inc)
        {
            ItemStack stackTmp = inv.getStackInSlot(slot);

            if (stackTmp.isEmpty())
            {
                continue;
            }

            if (areItemStacksEqual(stackTmp, stackTemplate))
            {
                stackTmp = extractItemsFromSlot(inv, slot, amount - stack.getCount());
                //System.out.printf("extracted %s from slot %d\n", stackTmp, slot);

                if (stackTmp.isEmpty() == false)
                {
                    stack.grow(stackTmp.getCount());
                }
            }
            else if (useOreDict && areItemStacksOreDictMatch(stackTmp, stackTemplate))
            {
                // This is the first match, and since it's an OreDictionary match ie. different actual
                // item, we convert the stack to the matched item.
                if (stack.getCount() == 0)
                {
                    stack = stackTmp.copy();
                    stack.setCount(0);
                }

                stackTmp = extractItemsFromSlot(inv, slot, amount - stack.getCount());

                if (stackTmp.isEmpty() == false)
                {
                    stack.grow(stackTmp.getCount());
                }
            }
        }

        return stack.getCount() > 0 ? stack : ItemStack.EMPTY;
    }

    /**
     * Returns the slot number of the first non-empty slot in the given inventory, or -1 if there are no items.
     */
    public static int getFirstNonEmptySlot(IItemHandler inv)
    {
        for (int i = 0; i < inv.getSlots(); ++i)
        {
            if (inv.getStackInSlot(i).isEmpty() == false)
            {
                return i;
            }
        }

        return -1;
    }

    /**
     * Returns an ItemStack of up to maxAmount items from the first slot possible to extract from.
     */
    public static ItemStack getItemsFromFirstNonEmptySlot(IItemHandler inv, int maxAmount, boolean simulate)
    {
        ItemStack stack;

        for (int i = 0; i < inv.getSlots(); i++)
        {
            stack = inv.extractItem(i, maxAmount, simulate);

            if (stack.isEmpty() == false)
            {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    public static boolean tryShiftSlots(IItemHandlerModifiable inv, int startSlot, boolean backwards)
    {
        if (startSlot < 0 || startSlot >= inv.getSlots())
        {
            return false;
        }

        // Shift slots backwards by one
        if (backwards && inv.getStackInSlot(startSlot).isEmpty())
        {
            final int lastSlot = inv.getSlots() - 2;

            for (int slot = startSlot; slot <= lastSlot; slot++)
            {
                inv.setStackInSlot(slot, inv.getStackInSlot(slot + 1));
            }

            inv.setStackInSlot(inv.getSlots() - 1, ItemStack.EMPTY);

            return true;
        }
        // Shift slots forward by one
        else if (backwards == false && inv.getStackInSlot(inv.getSlots() - 1).isEmpty())
        {
            for (int slot = inv.getSlots() - 1; slot > startSlot; slot--)
            {
                inv.setStackInSlot(slot, inv.getStackInSlot(slot - 1));
            }

            inv.setStackInSlot(startSlot, ItemStack.EMPTY);

            return true;
        }

        return false;
    }

    public static enum InvResult
    {
        NO_WORK,
        MOVED_NOTHING,
        MOVED_SOME,
        MOVED_ALL;
    }
}
