package fi.dy.masa.lowtechcrafting.inventory.container.base;

import java.util.ArrayList;
import java.util.List;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import fi.dy.masa.lowtechcrafting.LowTechCrafting;
import fi.dy.masa.lowtechcrafting.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.lowtechcrafting.inventory.wrapper.PlayerInvWrapperNoSync;
import fi.dy.masa.lowtechcrafting.network.PacketHandler;
import fi.dy.masa.lowtechcrafting.network.message.MessageSyncSlot;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerBase extends Container //<C extends net.minecraft.inventory.IInventory> extends RecipeBookContainer<C>
{
    public static final int GUI_ACTION_SCROLL_MOVE  = 0;
    public static final int GUI_ACTION_SCROLL_SET   = 1;
    protected final PlayerEntity player;
    protected final boolean isClient;
    protected final PlayerInventory playerInventory;
    protected final IItemHandlerModifiable playerInv;
    protected final IItemHandler inventory;
    protected MergeSlotRange customInventorySlots;
    protected MergeSlotRange playerMainSlots;
    protected MergeSlotRange playerHotbarSlots;
    protected MergeSlotRange playerMainSlotsIncHotbar;
    protected MergeSlotRange playerOffhandSlots;
    protected MergeSlotRange playerArmorSlots;
    protected List<MergeSlotRange> mergeSlotRangesExtToPlayer;
    protected List<MergeSlotRange> mergeSlotRangesPlayerToExt;

    protected final List<IContainerListener> listeners = new ArrayList<>();
    protected final NonNullList<ItemStack> inventoryItemStacks = NonNullList.create();

    public ContainerBase(int windowId, ContainerType<?> type, PlayerEntity player, IItemHandler inventory)
    {
        super(type, windowId);

        this.player = player;
        this.inventory = inventory;
        this.isClient = player.getEntityWorld().isRemote;
        this.playerInventory = player.inventory;
        this.playerInv = new PlayerInvWrapperNoSync(player.inventory);
        this.mergeSlotRangesExtToPlayer = new ArrayList<MergeSlotRange>();
        this.mergeSlotRangesPlayerToExt = new ArrayList<MergeSlotRange>();

        // Init the ranges to an empty range by default
        this.customInventorySlots       = new MergeSlotRange(0, 0);
        this.playerMainSlotsIncHotbar   = new MergeSlotRange(0, 0);
        this.playerMainSlots            = new MergeSlotRange(0, 0);
        this.playerHotbarSlots          = new MergeSlotRange(0, 0);
        this.playerOffhandSlots         = new MergeSlotRange(0, 0);
        this.playerArmorSlots           = new MergeSlotRange(0, 0);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn)
    {
        return true;
    }

    protected void reAddSlots(int playerInventoryX, int playerInventoryY)
    {
        this.inventorySlots.clear();
        this.inventoryItemStacks.clear();

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(playerInventoryX, playerInventoryY);
    }

    /**
     * Adds the "custom inventory" slots to the container (ie. the inventory that this container is for).
     * This must be called before addPlayerInventorySlots() (ie. the order of slots in the container
     * is important for the transferStackInSlot() method)!
     */
    protected void addCustomInventorySlots()
    {
    }

    /**
     * Adds the player inventory slots to the container.
     * posX and posY are the positions of the top-left-most slot of the player inventory.
     */
    protected void addPlayerInventorySlots(int posX, int posY)
    {
        // This should usually be sufficient, assuming the custom slots are added first
        //this.customInventorySlots = new SlotRange(0, this.inventorySlots.size());

        int playerInvStart = this.inventorySlots.size();

        // Player inventory
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                this.addSlot(new SlotItemHandlerGeneric(this.playerInv, i * 9 + j + 9, posX + j * 18, posY + i * 18));
            }
        }

        this.playerMainSlots = new MergeSlotRange(playerInvStart, 27);
        int playerHotbarStart = this.inventorySlots.size();

        // Player inventory hotbar
        for (int i = 0; i < 9; i++)
        {
            this.addSlot(new SlotItemHandlerGeneric(this.playerInv, i, posX + i * 18, posY + 58));
        }

        this.playerMainSlotsIncHotbar = new MergeSlotRange(playerInvStart, 36);
        this.playerHotbarSlots = new MergeSlotRange(playerHotbarStart, 9);
    }

    protected void addOffhandSlot(int posX, int posY)
    {
        this.playerOffhandSlots = new MergeSlotRange(this.inventorySlots.size(), 1);

        // Add the Offhand slot
        this.addSlot(new SlotItemHandlerGeneric(this.playerInv, 40, posX, posY)
        {
            @Override
            public Pair<ResourceLocation, ResourceLocation> func_225517_c_()
            {
                return Pair.of(PlayerContainer.field_226615_c_, PlayerContainer.field_226620_h_);
            }
        });
    }

    public PlayerEntity getPlayer()
    {
        return this.player;
    }

    public SlotRange getPlayerMainInventorySlotRange()
    {
        return this.playerMainSlotsIncHotbar;
    }

    public SlotRange getPlayerArmorSlots()
    {
        return this.playerArmorSlots;
    }

    public SlotRange getCustomInventorySlotRange()
    {
        return this.customInventorySlots;
    }

    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slot)
    {
        return (slot instanceof SlotItemHandler) &&
//                (slot instanceof SlotItemHandlerCraftResult) == false &&
                this.playerInventory.getItemStack().isEmpty() == false;
    }

    @Override
    public Slot getSlot(int slotId)
    {
        return slotId >= 0 && slotId < this.inventorySlots.size() ? super.getSlot(slotId) : null;
    }

    public SlotItemHandlerGeneric getSlotItemHandler(int slotId)
    {
        Slot slot = this.getSlot(slotId);

        return (slot instanceof SlotItemHandlerGeneric) ? (SlotItemHandlerGeneric) slot : null;
    }

    // Override because the vanilla lists are now private in 1.14 >_>
    @Override
    public void addListener(IContainerListener listener)
    {
        if (this.listeners.contains(listener) == false)
        {
            this.listeners.add(listener);
            listener.sendAllContents(this, this.getInventory());
            this.detectAndSendChanges();
        }
    }

    // Override because the vanilla lists are now private in 1.14 >_>
    @Override
    public void removeListener(IContainerListener listener)
    {
        this.listeners.remove(listener);
    }

    // Override because the vanilla lists are now private in 1.14 >_>
    @Override
    protected Slot addSlot(Slot slotIn)
    {
        slotIn.slotNumber = this.inventorySlots.size();
        this.inventorySlots.add(slotIn);
        this.inventoryItemStacks.add(ItemStack.EMPTY);
        return slotIn;
    }

    protected void syncCursorStackToClient()
    {
        this.syncStackToClient(-1, this.player.inventory.getItemStack());
    }

    protected void syncSlotToClient(int slotNum)
    {
        if (slotNum >= 0 && slotNum < this.inventorySlots.size())
        {
            this.syncStackToClient(slotNum, this.getSlot(slotNum).getStack());
        }
    }

    protected void syncStackToClient(int slotNum, ItemStack stack)
    {
        for (int i = 0; i < this.listeners.size(); ++i)
        {
            IContainerListener listener = this.listeners.get(i);

            //listener.sendSlotContents(this, slotNum, stack);
            if (listener instanceof ServerPlayerEntity)
            {
                ServerPlayerEntity player = (ServerPlayerEntity) listener;
                PacketHandler.INSTANCE.sendTo(new MessageSyncSlot(this.windowId, slotNum, stack), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
            }
        }
    }

    /**
     * Will put the given stack into the slot, ignoring any validity checks.
     * Note: A slotId == -1 will sync the stack in the player's cursor.
     * This will and should only be used for syncing slots to the client.
     * @param stack
     */
    public void syncStackInSlot(int slotId, ItemStack stack)
    {
        if (slotId == -1)
        {
            this.player.inventory.setItemStack(stack);
        }
        else
        {
            Slot slot = this.getSlot(slotId);

            if (slot instanceof SlotItemHandlerGeneric)
            {
                ((SlotItemHandlerGeneric) slot).syncStack(stack);
            }
            else
            {
                this.putStackInSlot(slotId, stack);
            }
        }
    }

    @Override
    public void detectAndSendChanges()
    {
        if (this.isClient == false)
        {
            for (int slot = 0; slot < this.inventorySlots.size(); slot++)
            {
                ItemStack currentStack = this.inventorySlots.get(slot).getStack();
                ItemStack prevStack = this.inventoryItemStacks.get(slot);

                if (ItemStack.areItemStacksEqual(prevStack, currentStack) == false)
                {
                    prevStack = currentStack.isEmpty() ? ItemStack.EMPTY : currentStack.copy();
                    this.inventoryItemStacks.set(slot, prevStack);
                    this.syncStackToClient(slot, prevStack);
                }
            }
        }
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotNum)
    {
        this.transferStackFromSlot(player, slotNum);
        return ItemStack.EMPTY;
    }

    /**
     * Transfers the stack from the given slot into other parts of the inventory,
     * or other inventories in this Container.
     * The player's inventory and the armor slots have highest "swap priority",
     * after that come player inventory to the "priority slots" that can be added to
     * the list of "priority slot" SlotRanges, and after that come the rest of the "custom inventory".
     * Returns false if no items were moved, true otherwise
     */
    protected boolean transferStackFromSlot(PlayerEntity player, int slotNum)
    {
        Slot slot = this.getSlot(slotNum);

        if (slot == null || slot.getHasStack() == false || slot.canTakeStack(player) == false)
        {
            return false;
        }

        // From player armor or offhand slots to the player main inventory
        if (this.playerArmorSlots.contains(slotNum) || this.playerOffhandSlots.contains(slotNum))
        {
            return this.transferStackToSlotRange(player, slotNum, this.playerMainSlotsIncHotbar, false);
        }
        // From player main inventory to armor slots or the "external" inventory
        else if (this.playerMainSlotsIncHotbar.contains(slotNum))
        {
            return this.transferStackFromPlayerMainInventory(player, slotNum);
        }

        // From external inventory to player inventory
        return this.transferStackToSlotRange(player, slotNum, this.playerMainSlotsIncHotbar, true);
    }

    protected boolean transferStackFromPlayerMainInventory(PlayerEntity player, int slotNum)
    {
        if (this.transferStackToSlotRange(player, slotNum, this.playerArmorSlots, false))
        {
            return true;
        }

        if (this.transferStackToPrioritySlots(player, slotNum, false))
        {
            return true;
        }

        return this.transferStackToSlotRange(player, slotNum, this.customInventorySlots, false);
    }

    protected boolean transferStackToPrioritySlots(PlayerEntity player, int slotNum, boolean reverse)
    {
        boolean ret = false;

        for (MergeSlotRange slotRange : this.mergeSlotRangesPlayerToExt)
        {
            ret |= this.transferStackToSlotRange(player, slotNum, slotRange, reverse);
        }

        return ret;
    }

    protected boolean transferStackToSlotRange(PlayerEntity player, int slotNum, MergeSlotRange slotRange, boolean reverse)
    {
        SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotNum);

        if (slot == null || slot.getHasStack() == false || slot.canTakeStack(player) == false)
        {
            return false;
        }

        ItemStack stack = slot.getStack().copy();
        int amount = Math.min(stack.getCount(), stack.getMaxStackSize());
        stack.setCount(amount);

        // Simulate the merge
        stack = this.mergeItemStack(stack, slotRange, reverse, true);

        if (stack.isEmpty() == false)
        {
            // If the item can't be put back to the slot, then we need to make sure that the whole
            // stack can be merged elsewhere before trying to (partially) merge it. Important for crafting slots!
            // Or if nothing could be merged, then also abort.
            if (slot.isItemValid(stack) == false || stack.getCount() == amount)
            {
                return false;
            }

            // Can merge at least some of the items, get the amount that can be merged
            amount -= stack.getCount();
        }

        // Get the actual stack for non-simulated merging
        stack = slot.decrStackSize(amount);
        slot.onTake(player, stack);

        // Actually merge the items
        stack = this.mergeItemStack(stack, slotRange, reverse, false);

        // If they couldn't fit after all, then return them.
        // This shouldn't happen, and will cause some issues like gaining XP from nothing in furnaces.
        if (stack.isEmpty() == false)
        {
            slot.insertItem(stack, false);

            LowTechCrafting.logger.warn("Failed to merge all items in '{}'. This shouldn't happen and should be reported.",
                    this.getClass().getSimpleName());
        }

        return true;
    }

    /**
     * Returns the maximum allowed stack size, based on the given ItemStack and the inventory's max stack size.
     */
    protected int getMaxStackSizeFromSlotAndStack(Slot slot, ItemStack stack)
    {
        return stack.isEmpty() == false ? Math.min(slot.getItemStackLimit(stack), stack.getMaxStackSize()) : slot.getSlotStackLimit();
    }

    /**
     * This should NOT be called from anywhere in this mod, but just in case...
     */
    @Override
    protected boolean mergeItemStack(ItemStack stack, int slotStart, int slotEndExclusive, boolean reverse)
    {
        return false;
    }

    /**
     * Merge the given ItemStack to the slot range provided.
     * If simulate is true, then we are checking if the WHOLE stack can be merged.
     * @return If simulate is false, then true is returned if at least some of the items were merged.
     * If simulate is true, then true is returned only if ALL the items were successfully merged.
     */
    protected ItemStack mergeItemStack(ItemStack stack, MergeSlotRange slotRange, boolean reverse, boolean simulate)
    {
        int slotStart = slotRange.first;
        int slotEndExclusive = slotRange.lastExc;
        int slotIndex = (reverse ? slotEndExclusive - 1 : slotStart);

        // First try to merge the stack into existing stacks in the container
        while (stack.isEmpty() == false && slotIndex >= slotStart && slotIndex < slotEndExclusive)
        {
            SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotIndex);

            if (slot != null && slot.getHasStack() && slot.isItemValid(stack))
            {
                stack = slot.insertItem(stack, simulate);
            }

            slotIndex = (reverse ? slotIndex - 1 : slotIndex + 1);
        }

        // If there are still items to merge after merging to existing stacks, then try to add it to empty slots
        if (stack.isEmpty() == false && slotRange.existingOnly == false)
        {
            slotIndex = (reverse ? slotEndExclusive - 1 : slotStart);

            while (stack.isEmpty() == false && slotIndex >= slotStart && slotIndex < slotEndExclusive)
            {
                SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotIndex);

                if (slot != null && slot.getHasStack() == false && slot.isItemValid(stack))
                {
                    stack = slot.insertItem(stack, simulate);
                }

                slotIndex = (reverse ? slotIndex - 1 : slotIndex + 1);
            }
        }

        return stack;
    }

    protected void addMergeSlotRangeExtToPlayer(int start, int numSlots)
    {
        this.addMergeSlotRangeExtToPlayer(start, numSlots, false);
    }

    protected void addMergeSlotRangeExtToPlayer(int start, int numSlots, boolean existingOnly)
    {
        this.mergeSlotRangesExtToPlayer.add(new MergeSlotRange(start, numSlots, existingOnly));
    }

    protected void addMergeSlotRangePlayerToExt(int start, int numSlots)
    {
        this.addMergeSlotRangePlayerToExt(start, numSlots, false);
    }

    protected void addMergeSlotRangePlayerToExt(int start, int numSlots, boolean existingOnly)
    {
        this.mergeSlotRangesPlayerToExt.add(new MergeSlotRange(start, numSlots, existingOnly));
    }

    public void performGuiAction(PlayerEntity player, int action, int element)
    {
    }

    /*
    @Override
    public void func_201771_a(RecipeItemHelper p_201771_1_)
    {
    }

    @Override
    public void clear()
    {
    }

    @Override
    public boolean matches(IRecipe<? super C> recipeIn)
    {
        return false;
    }

    @Override
    public int getOutputSlot()
    {
        return 0;
    }

    @Override
    public int getWidth()
    {
        return 3;
    }

    @Override
    public int getHeight()
    {
        return 3;
    }

    @Override
    public int getSize()
    {
        return 10;
    }
    */
}
