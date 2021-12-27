package fi.dy.masa.lowtechcrafting.inventory.container.base;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import fi.dy.masa.lowtechcrafting.inventory.IItemHandlerSize;
import fi.dy.masa.lowtechcrafting.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.lowtechcrafting.util.InventoryUtils;
import net.minecraftforge.items.IItemHandler;

public class ContainerCustomSlotClick extends ContainerBase //<C extends net.minecraft.inventory.IInventory> extends ContainerBase<C>
{
    /**
     * This is a non-Container-wrapped, possibly a lockable inventory.
     * If this field is set, it is used for the stack size cycling
     * and slot locking features, if applicable.
     */
    @Nullable
    protected IItemHandlerSize inventoryNonContainerWrapped;
    protected boolean isDragging;
    protected DragType dragType;
    protected final Set<Integer> draggedSlots = new HashSet<Integer>();
    protected int selectedSlot = -1;
    private int selectedSlotLast = -1;

    public ContainerCustomSlotClick(int windowId, ContainerType<?> type, PlayerEntity player, IItemHandler inventory)
    {
        super(windowId, type, player, inventory);
    }

    public int getSelectedSlot()
    {
        return this.selectedSlot;
    }

    @Override
    public void broadcastChanges()
    {
        if (this.isClient == false)
        {
            for (int i = 0; i < this.listeners.size(); i++)
            {
                if (this.selectedSlot != this.selectedSlotLast)
                {
                    this.listeners.get(i).setContainerData(this, 0x0100, this.selectedSlot & 0xFFFF);
                }
            }

            this.selectedSlotLast = this.selectedSlot;
        }

        super.broadcastChanges();
    }

    @Override
    public void setData(int id, int data)
    {
        if (id == 0x0100)
        {
            // Convert from a short back to int
            this.selectedSlot = (int) ((short) data);
        }
        else
        {
            super.setData(id, data);
        }
    }

    protected void startDragging(DragType dragType)
    {
        this.isDragging = true;
        this.dragType = dragType;
        this.draggedSlots.clear();
    }

    protected void dragging(int slotNum)
    {
        this.draggedSlots.add(slotNum);
    }

    protected ItemStack putItemsToSlot(SlotItemHandlerGeneric slot, ItemStack stack, int amount)
    {
        if (stack.isEmpty())
        {
            return ItemStack.EMPTY;
        }

        if (amount < stack.getCount())
        {
            ItemStack stackInsert = stack.split(amount);
            stackInsert = slot.insertItem(stackInsert, false);

            if (stackInsert.isEmpty() == false)
            {
                stack.grow(stackInsert.getCount());
            }

            return stack;
        }

        return slot.insertItem(stack, false);
    }

    protected boolean takeItemsFromSlotToCursor(SlotItemHandlerGeneric slot, int amount)
    {
        ItemStack stackCursor = this.playerInventory.getCarried();
        ItemStack stackSlot = slot.getItem();

        if (slot.mayPickup(this.player) == false || stackSlot.isEmpty() ||
            (stackCursor.isEmpty() == false && InventoryUtils.areItemStacksEqual(stackCursor, stackSlot) == false))
        {
            return false;
        }

        amount = Math.min(amount, stackSlot.getMaxStackSize());
        int spaceAvailable = stackSlot.getMaxStackSize();

        if (stackCursor.isEmpty() == false)
        {
            spaceAvailable = stackCursor.getMaxStackSize() - stackCursor.getCount();
        }

        amount = Math.min(amount, spaceAvailable);

        /*
        // only allow taking the whole stack from crafting slots
        if (amount <= 0 || ((slot instanceof SlotItemHandlerCraftResult) && spaceAvailable < stackSlot.getCount()))
        {
            return false;
        }
        */

        stackSlot = slot.remove(amount);

        if (stackSlot.isEmpty() == false)
        {
            slot.onTake(this.player, stackSlot);

            if (stackCursor.isEmpty())
            {
                stackCursor = stackSlot;
            }
            else
            {
                stackCursor.grow(stackSlot.getCount());
            }

            this.playerInventory.setCarried(stackCursor);

            return true;
        }

        return false;
    }

    protected boolean swapSlots(SlotItemHandlerGeneric slot1, SlotItemHandlerGeneric slot2)
    {
        if ((slot1.hasItem() && slot1.mayPickup(this.player) == false) ||
            (slot2.hasItem() && slot2.mayPickup(this.player) == false))
        {
            return false;
        }

        ItemStack stack1 = slot1.getItem();
        ItemStack stack2 = slot2.getItem();

        if ((stack1.isEmpty() == false && slot2.mayPlace(stack1) == false) ||
            (stack2.isEmpty() == false && slot1.mayPlace(stack2) == false))
        {
            return false;
        }

        if (stack1.isEmpty() == false)
        {
            slot1.onTake(this.player, stack1);
        }

        if (stack2.isEmpty() == false)
        {
            slot2.onTake(this.player, stack2);
        }

        slot1.set(stack2.copy());
        slot2.set(stack1.copy());

        return true;
    }

    protected void endDragging()
    {
        if (this.playerInventory.getCarried().isEmpty() == false)
        {
            if (this.dragType == DragType.DRAG_MIDDLE)
            {
                this.endDraggingMiddle();
            }
            else
            {
                this.endDraggingLeftOrRight(this.dragType == DragType.DRAG_RIGHT);
            }
        }

        this.isDragging = false;
    }

    protected void endDraggingLeftOrRight(boolean isRightClick)
    {
        ItemStack stackCursor = this.playerInventory.getCarried().copy();
        int numSlots = this.draggedSlots.size();
        int itemsPerSlot = isRightClick ? 1 : (numSlots > 0 ? stackCursor.getCount() / numSlots : stackCursor.getCount());

        for (int slotNum : this.draggedSlots)
        {
            if (stackCursor.isEmpty())
            {
                break;
            }

            SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotNum);

            if (slot != null)
            {
                int amount = Math.min(itemsPerSlot, this.getMaxStackSizeFromSlotAndStack(slot, stackCursor));
                amount = Math.min(amount, stackCursor.getCount());
                stackCursor = this.putItemsToSlot(slot, stackCursor, amount);
            }
        }

        this.playerInventory.setCarried(stackCursor);
    }

    protected void endDraggingMiddle()
    {
        if (this.player.abilities.instabuild)
        {
            for (int slotNum : this.draggedSlots)
            {
                ItemStack stackCursor = this.playerInventory.getCarried().copy();
                SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotNum);

                if (slot != null && (slot.hasItem() == false || InventoryUtils.areItemStacksEqual(stackCursor, slot.getItem())))
                {
                    int amount = stackCursor.getMaxStackSize() - slot.getItem().getCount();

                    if (amount > 0)
                    {
                        stackCursor.setCount(amount);
                        slot.insertItem(stackCursor, false);
                    }
                }
            }
        }
    }

    protected void leftClickOutsideInventory(PlayerEntity player)
    {
        ItemStack stackCursor = this.playerInventory.getCarried();

        if (stackCursor.isEmpty() == false)
        {
            int max = stackCursor.getMaxStackSize();

            while (stackCursor.getCount() > max)
            {
                ItemStack stackDrop = stackCursor.copy();
                stackDrop.setCount(max);
                player.drop(stackDrop, true);
            }

            player.drop(stackCursor, true);
            this.playerInventory.setCarried(ItemStack.EMPTY);
        }
    }

    protected void rightClickOutsideInventory(PlayerEntity player)
    {
        ItemStack stackCursor = this.playerInventory.getCarried();

        if (stackCursor.isEmpty() == false)
        {
            ItemStack stackDrop = stackCursor.split(1);
            player.drop(stackDrop, true);
            this.playerInventory.setCarried(stackCursor.isEmpty() ? ItemStack.EMPTY : stackCursor);
        }
    }

    protected void leftClickSlot(int slotNum, PlayerEntity player)
    {
        SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotNum);

        if (slot == null)
        {
            return;
        }

        ItemStack stackCursor = this.playerInventory.getCarried();
        ItemStack stackSlot = slot.getItem();

        // Items in cursor
        if (stackCursor.isEmpty() == false)
        {
            // Empty slot or identical items: try to add to slot
            if (stackSlot.isEmpty() || InventoryUtils.areItemStacksEqual(stackCursor, stackSlot))
            {
                // Can put items into the slot
                if (slot.mayPlace(stackCursor))
                {
                    this.playerInventory.setCarried(slot.insertItem(stackCursor, false));
                }
                // Can't put items into the slot (for example a crafting output slot); take items instead
                else if (stackSlot.isEmpty() == false)
                {
                    this.takeItemsFromSlotToCursor(slot, stackSlot.getCount());
                }
            }
            // Different items, try to swap the stacks
            else if (slot.mayPickup(this.player) && slot.canTakeAll() && slot.mayPlace(stackCursor) &&
                     stackSlot.getCount() <= stackSlot.getMaxStackSize() &&
                     slot.getMaxStackSize(stackCursor) >= stackCursor.getCount())
            {
                this.playerInventory.setCarried(slot.remove(stackSlot.getCount()));
                slot.onTake(this.player, stackSlot);
                slot.insertItem(stackCursor, false);
            }
        }
        // Empty cursor, trying to take items from the slot into the cursor
        else if (stackSlot.isEmpty() == false)
        {
            this.takeItemsFromSlotToCursor(slot, stackSlot.getCount());
        }
    }

    protected void rightClickSlot(int slotNum, PlayerEntity player)
    {
        SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotNum);

        if (slot == null)
        {
            return;
        }

        ItemStack stackCursor = this.playerInventory.getCarried();
        ItemStack stackSlot = slot.getItem();

        // Items in cursor
        if (stackCursor.isEmpty() == false)
        {
            // Empty slot or identical items: try to add to slot
            if (stackSlot.isEmpty() || InventoryUtils.areItemStacksEqual(stackCursor, stackSlot))
            {
                // Can put items into the slot
                if (slot.mayPlace(stackCursor))
                {
                    if (slot.insertItem(stackCursor.split(1), false).isEmpty() == false)
                    {
                        stackCursor.grow(1);
                    }

                    this.playerInventory.setCarried(stackCursor.isEmpty() ? ItemStack.EMPTY : stackCursor);
                }
                // Can't put items into the slot (for example a furnace output slot or a crafting output slot); take items instead
                else if (stackSlot.isEmpty() == false)
                {
                    this.takeItemsFromSlotToCursor(slot, Math.min(stackSlot.getMaxStackSize() / 2, stackSlot.getCount()));
                }
            }
            // Different items, try to swap the stacks
            else if (slot.mayPickup(this.player) && slot.canTakeAll() && slot.mayPlace(stackCursor) &&
                    stackSlot.getCount() <= stackSlot.getMaxStackSize() &&
                    slot.getMaxStackSize(stackCursor) >= stackCursor.getCount())
            {
                this.playerInventory.setCarried(slot.remove(stackSlot.getCount()));
                slot.onTake(this.player, stackSlot);
                slot.insertItem(stackCursor, false);
            }
        }
        // Empty cursor, trying to take items from the slot into the cursor
        else if (stackSlot.isEmpty() == false)
        {
            // only allow taking the whole stack from crafting slots
            int amount = stackSlot.getCount();

            //if ((slot instanceof SlotItemHandlerCraftResult) == false)
            {
                amount = Math.min((int) Math.ceil((double) stackSlot.getCount() / 2.0d),
                                  (int) Math.ceil((double) stackSlot.getMaxStackSize() / 2.0d));
            }

            this.takeItemsFromSlotToCursor(slot, amount);
        }
    }

    protected void middleClickSlot(int slotNum, PlayerEntity player)
    {
        if (player.abilities.instabuild &&
            player.inventory.getCarried().isEmpty() &&
            this.getSlot(slotNum).hasItem())
        {
            ItemStack stack = this.getSlot(slotNum).getItem().copy();
            stack.setCount(stack.getMaxStackSize());
            player.inventory.setCarried(stack);
        }
        else
        {
            this.swapSlots(slotNum, player);
        }
    }

    protected void swapSlots(int slotNum, PlayerEntity player)
    {
        SlotItemHandlerGeneric slot1 = this.getSlotItemHandler(slotNum);

        // Only allow swapping in this inventory (which supports the large stacks)
        // NOTE: This assumes that the swappable "main" inventory is the "inventory" reference in this Container
        if (slot1 != null && slot1.getItemHandler() == this.inventory)
        {
            if (this.selectedSlot >= 0 && this.selectedSlot < this.slots.size())
            {
                // Don't swap with self
                if (this.selectedSlot != slotNum)
                {
                    SlotItemHandlerGeneric slot2 = this.getSlotItemHandler(this.selectedSlot);

                    if (slot2 != null)
                    {
                        this.swapSlots(slot1, slot2);
                    }
                }

                this.selectedSlot = -1;
            }
            else
            {
                this.selectedSlot = slotNum;
            }
        }
    }

    /**
     * Toggles the locked state for the given slot and sets the template stack
     * to the one in the cursor.<br>
     * If <b>requireStorageKey</b> is true, then a "storage key"
     * item needs to be held in the cursor for this to do anything.<br>
     * If <b>requireStorageKey</b> is false, then the template stack is set to
     * whatever is currently in the cursor, and the locked status is toggled.
     * @param slotNum
     * @param inv
     * @return true if the slot locked status was toggled, false if the conditions weren't met
     */
    /*
    protected boolean toggleSlotLocked(int slotNum, ItemStackHandlerLockable inv)
    {
        Slot slot = this.getSlot(slotNum);
        int slotIndex = slot != null ? slot.getSlotIndex() : -1;
        ItemStack stackCursor = this.player.inventory.getItemStack();

        if (slotIndex != -1)
        {
            if (stackCursor.isEmpty() == false)
            {
                inv.setTemplateStackInSlot(slotIndex, stackCursor);
            }
            else
            {
                inv.setTemplateStackInSlot(slotIndex, slot.getStack());
            }

            inv.toggleSlotLocked(slotIndex);
            return true;
        }

        return false;
    }
    */

    /**
     * When there is a single, identical item in the cursor as in the slot,
     * then the slot's stack size is incremented by times 10 on each click.
     * If there are more than one item in the cursor, then the stack size is
     * set to the maximum, or cleared if it's already at the maximum.
     * If the items in the cursor are different than the items in the slot,
     * then the cursor stack is copied to the slot.
     * @param slotNum
     * @param inv
     * @return
     */
    protected boolean cycleStackSize(int slotNum, IItemHandlerSize inv)
    {
        SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotNum);
        ItemStack stackCursor = player.inventory.getCarried();

        if (slot != null && stackCursor.isEmpty() == false)
        {
            ItemStack stackSlot = slot.getItem();

            if (InventoryUtils.areItemStacksEqual(stackCursor, stackSlot))
            {
                int max = inv.getItemStackLimit(slotNum, stackCursor);

                // When middle clicked with an identical single item in cursor, cycle the stack size in x10 increments
                if (stackCursor.getCount() == 1)
                {
                    long newSize = (long) stackSlot.getCount() * 10L;

                    if (newSize >= 0 && newSize <= max)
                    {
                        stackSlot = stackSlot.copy();
                        stackSlot.setCount((int) newSize);
                        slot.set(stackSlot);
                    }
                    else
                    {
                        slot.set(ItemStack.EMPTY);
                    }
                }
                // When middle clicked with an identical item in cursor (when holding more than one),
                // fill the stack to the maximum size in one go, or clear the slot.
                else
                {
                    if (stackSlot.getCount() < max)
                    {
                        stackSlot = stackSlot.copy();
                        stackSlot.setCount(max);
                        slot.set(stackSlot);
                    }
                    else
                    {
                        slot.set(ItemStack.EMPTY);
                    }
                }
            }
            // Different items in cursor, copy the stack from the cursor to the slot
            else
            {
                slot.set(stackCursor.copy());
            }

            return true;
        }

        return false;
    }

    protected void leftDoubleClickSlot(int slotNum, PlayerEntity player)
    {
        SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotNum);
        ItemStack stackCursor = this.playerInventory.getCarried();

        if (slot != null && stackCursor.isEmpty() == false && this.canTakeItemForPickAll(stackCursor, slot))
        {
            // FIXME add a slot-aware version
            ItemStack stackTmp = InventoryUtils.collectItemsFromInventory(
                    slot.getItemHandler(), stackCursor, stackCursor.getMaxStackSize() - stackCursor.getCount(), true);

            if (stackTmp.isEmpty() == false)
            {
                stackCursor.grow(stackTmp.getCount());
                this.playerInventory.setCarried(stackCursor);
            }
        }
    }

    protected void shiftClickSlot(int slotNum, PlayerEntity player)
    {
        SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotNum);
        ItemStack stackSlot = slot != null ? slot.getItem() : ItemStack.EMPTY;

        if (stackSlot.isEmpty() == false && this.transferStackFromSlot(player, slotNum))
        {
            slot.onTake(player, stackSlot);
        }
    }

    protected void pressDropKey(int slotNum, PlayerEntity player, boolean wholeStack)
    {
        SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotNum);
        ItemStack stackSlot = slot != null ? slot.getItem() : ItemStack.EMPTY;

        if (stackSlot.isEmpty() == false && slot.mayPickup(this.player))
        {
            ItemStack stackDrop = slot.remove(wholeStack ? stackSlot.getCount() : 1);

            if (stackDrop.isEmpty() == false)
            {
                slot.onTake(player, stackDrop);
                player.drop(stackDrop, true);
            }
        }
    }

    protected void pressHotbarKey(int slotNum, int button, PlayerEntity player)
    {
        SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotNum);

        if (slot == null)
        {
            return;
        }

        ItemStack stackSlot = slot != null ? slot.getItem() : ItemStack.EMPTY;
        ItemStack stackHotbar = this.playerInv.getStackInSlot(button);

        // The stack in the slot is null or the stack size is small enough to fit into one regular stack,
        // and the stack in the hotbar is null or the stack is small enough to fit to the slot
        if ((stackSlot.isEmpty() || stackSlot.getCount() <= stackSlot.getMaxStackSize()) &&
            (stackHotbar.isEmpty() || stackHotbar.getCount() <= this.getMaxStackSizeFromSlotAndStack(slot, stackHotbar)) &&
             slot.mayPickup(this.player) && slot.mayPlace(stackHotbar))
        {
            slot.set(stackHotbar);
            this.playerInv.setStackInSlot(button, stackSlot);

            if (stackSlot.isEmpty() == false)
            {
                slot.onTake(player, stackSlot);
            }
        }
        // Hotbar slot is empty, but the whole stack doesn't fit into it
        else if (stackHotbar.isEmpty() && stackSlot.isEmpty() == false)
        {
            int num = Math.min(stackSlot.getMaxStackSize(), this.playerInventory.getMaxStackSize());
            num = Math.min(num, stackSlot.getCount());
            stackHotbar = slot.remove(num);
            slot.onTake(player, stackHotbar);
            this.playerInv.setStackInSlot(button, stackHotbar);
        }
        // Matching items in both slots
        else if (stackHotbar.isEmpty() == false && stackSlot.isEmpty() == false &&
                InventoryUtils.areItemStacksEqual(stackHotbar, stackSlot))
        {
            int num = Math.min(stackHotbar.getMaxStackSize() - stackHotbar.getCount(), stackSlot.getCount());

            // Fill the hotbar stack if it has still space
            if (num > 0)
            {
                stackHotbar.grow(num);
                slot.remove(num);
                slot.onTake(player, stackSlot);
                this.playerInv.setStackInSlot(button, stackHotbar);
            }
            // ... otherwise take the stack from it
            else if (slot.mayPlace(stackHotbar))
            {
                this.playerInv.setStackInSlot(button, this.putItemsToSlot(slot, stackHotbar, stackHotbar.getCount()));
            }
        }
    }

    @Override
    public ItemStack clicked(int slotNum, int dragType, ClickType clickType, PlayerEntity player)
    {
        //String side = this.player.worldObj.isRemote ? "client" : "server";
        //EnderUtilities.logger.info(String.format("slotClick(): side: %s slotNum: %d, button: %d type: %s", side, slotNum, dragType, clickType));

        // slotNum: real button: 0 type: PICKUP - regular  left click - on button down with empty cursor, on button up with stack in cursor
        // slotNum: real button: 1 type: PICKUP - regular right click - on button down with empty cursor, on button up with stack in cursor

        // slotNum: real button: 0 type: QUICK_MOVE - shift +  left click - on button down with empty cursor, on button up with stack in cursor
        // slotNum: real button: 1 type: QUICK_MOVE - shift + right click - on button down with empty cursor, on button up with stack in cursor

        // slotNum: -1   button: 0 type: PICKUP -  left click with a stack in cursor inside inventory but not on a slot - on button up
        // slotNum: -1   button: 1 type: PICKUP - right click with a stack in cursor inside inventory but not on a slot - on button up

        // slotNum: -999 button: 0 type: PICKUP -  left click outside of inventory screen with a stack in cursor - on button up
        // slotNum: -999 button: 1 type: PICKUP - right click outside of inventory screen with a stack in cursor - on button up

        // slotNum: real button: 2 type: CLONE  - middle click on any slot - on button down with empty cursor, on button up with a stack in cursor
        // slotNum: -1   button: 2 type: CLONE  - middle click with a stack in cursor inside inventory but not on a slot - on button up

        // slotNum: -999 button: 0 type: THROW - (shift +)  left click outside the inventory with an empty cursor - on button down
        // slotNum: -999 button: 1 type: THROW - (shift +) right click outside the inventory with an empty cursor - on button down

        // slotNum: real button: 0 type: THROW - pressing Q over a slot which has items - on button down
        // slotNum: real button: 1 type: THROW - pressing Ctrl + Q over a slot which has items - on button down

        // slotNum: real button: 0..8 type: SWAP - pressing hotbar number key over a slot - on button down, only with empty cursor

        // slotNum: real button: 0 type: PICKUP_ALL - left double-click with an empty cursor on a slot with items (fires after a regular PICKUP)

        // slotNum: -999 button: 0 type: QUICK_CRAFT - left click drag with stack in cursor start - after drag ends, as the first call
        // slotNum: real button: 1 type: QUICK_CRAFT - left click drag with stack in cursor - for each slot dragged over - after drag ends, in sequence
        // slotNUm: -999 button: 2 type: QUICK_CRAFT - left click drag with stack in cursor end - after drag ends, as the last call

        // slotNum: -999 button: 4 type: QUICK_CRAFT - right click drag with stack in cursor start - after drag ends, as the first call
        // slotNum: real button: 5 type: QUICK_CRAFT - right click drag with stack in cursor - for each slot dragged over - after drag ends, in sequence
        // slotNUm: -999 button: 6 type: QUICK_CRAFT - right click drag with stack in cursor end - after drag ends, as the last call

        // slotNum: -999 button: 8 type: QUICK_CRAFT - middle click drag with stack in cursor start - after drag ends, as the first call
        // slotNum: real button: 9 type: QUICK_CRAFT - middle click drag with stack in cursor - for each slot dragged over - after drag ends, in sequence
        // slotNUm: -999 button: 10 type: QUICK_CRAFT - middle click drag with stack in cursor end - after drag ends, as the last call

        if (this.isDragging)
        {
            // End of dragging
            if (clickType == ClickType.QUICK_CRAFT && (dragType == 2 || dragType == 6 || dragType == 10))
            {
                this.endDragging();
            }
            // This gets called for each slot that was dragged over
            else if (clickType == ClickType.QUICK_CRAFT && (dragType == 1 || dragType == 5 || dragType == 9))
            {
                this.dragging(slotNum);
            }
        }
        // Starting a left or right or middle click drag
        else if (clickType == ClickType.QUICK_CRAFT && (dragType == 0 || dragType == 4 || dragType == 8))
        {
            DragType dt = dragType == 0 ? DragType.DRAG_LEFT : (dragType == 4 ? DragType.DRAG_RIGHT : DragType.DRAG_MIDDLE);
            this.startDragging(dt);
        }
        // Left or right click outside inventory with a stack in cursor
        else if (clickType == ClickType.PICKUP && slotNum == -999)
        {
            // Left click outside of inventory screen - drop the stack from the cursor
            if (dragType == 0)
            {
                this.leftClickOutsideInventory(player);
            }
            // Right click outside of inventory screen - drop one item from the cursor
            else if (dragType == 1)
            {
                this.rightClickOutsideInventory(player);
            }
        }
        // Regular left click on a slot
        else if (clickType == ClickType.PICKUP && dragType == 0)
        {
            this.leftClickSlot(slotNum, player);
        }
        // Regular right click on a slot
        else if (clickType == ClickType.PICKUP && dragType == 1)
        {
            this.rightClickSlot(slotNum, player);
        }
        // Shift left click or shift right click on a slot, they both do the same
        else if (clickType == ClickType.QUICK_MOVE && (dragType == 0 || dragType == 1))
        {
            this.shiftClickSlot(slotNum, player);
        }
        // Pressing the drop key (Q) while hovering over a stack
        else if (clickType == ClickType.THROW && (dragType == 0 || dragType == 1))
        {
            this.pressDropKey(slotNum, player, dragType == 1);
        }
        // Pressing a hotbar hotkey over a slot
        else if (clickType == ClickType.SWAP && dragType >= 0 && dragType <= 8)
        {
            this.pressHotbarKey(slotNum, dragType, player);
        }
        // Left double-click with an empty cursor over a slot with items
        else if (clickType == ClickType.PICKUP_ALL && dragType == 0)
        {
            this.leftDoubleClickSlot(slotNum, player);
        }
        else
        {
            /*if (this.inventoryNonWrapped instanceof ItemStackHandlerLockable)
            {
                ItemStackHandlerLockable inv = (ItemStackHandlerLockable) this.inventoryNonWrapped;

                // Middle click or middle click drag
                if (((clickType == ClickType.CLONE && dragType == 2) ||
                    (clickType == ClickType.QUICK_CRAFT && dragType == 9)) &&
                    slotNum >= 0 && slotNum < inv.getSlots())
                {
                    this.toggleSlotLocked(slotNum, inv);
                    return ItemStack.EMPTY;
                }
                else
                {
                    this.middleClickSlot(slotNum, player);
                }
            }
            // Middle click on a slot - select the slot for swapping, or swap the contents with the selected slot
            else */if (clickType == ClickType.CLONE && dragType == 2)
            {
                this.middleClickSlot(slotNum, player);
            }
        }

        if (this.isClient == false)
        {
            this.broadcastChanges();
        }

        return ItemStack.EMPTY;
    }

    public enum DragType
    {
        DRAG_LEFT,
        DRAG_RIGHT,
        DRAG_MIDDLE
    }
}
