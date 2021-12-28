package fi.dy.masa.lowtechcrafting.inventory.container.base;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import com.mojang.datafixers.util.Pair;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;
import fi.dy.masa.lowtechcrafting.LowTechCrafting;
import fi.dy.masa.lowtechcrafting.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.lowtechcrafting.inventory.wrapper.PlayerInvWrapperNoSync;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ContainerBase extends AbstractContainerMenu //<C extends net.minecraft.inventory.IInventory> extends RecipeBookContainer<C>
{
    protected final Player player;
    protected final boolean isClient;
    protected final Inventory playerInventory;
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

    public ContainerBase(int windowId, MenuType<?> type, Player player, IItemHandler inventory)
    {
        super(type, windowId);

        this.player = player;
        this.inventory = inventory;
        this.isClient = player.getCommandSenderWorld().isClientSide;
        this.playerInventory = player.getInventory();
        this.playerInv = new PlayerInvWrapperNoSync(this.playerInventory);
        this.mergeSlotRangesExtToPlayer = new ArrayList<>();
        this.mergeSlotRangesPlayerToExt = new ArrayList<>();

        // Init the ranges to an empty range by default
        this.customInventorySlots       = new MergeSlotRange(0, 0);
        this.playerMainSlotsIncHotbar   = new MergeSlotRange(0, 0);
        this.playerMainSlots            = new MergeSlotRange(0, 0);
        this.playerHotbarSlots          = new MergeSlotRange(0, 0);
        this.playerOffhandSlots         = new MergeSlotRange(0, 0);
        this.playerArmorSlots           = new MergeSlotRange(0, 0);
    }

    @Override
    public boolean stillValid(Player playerIn)
    {
        return true;
    }

    protected void addSlots(int playerInventoryX, int playerInventoryY)
    {
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

        int playerInvStart = this.slots.size();

        // Player inventory
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                this.addSlot(new SlotItemHandlerGeneric(this.playerInv, i * 9 + j + 9, posX + j * 18, posY + i * 18));
            }
        }

        this.playerMainSlots = new MergeSlotRange(playerInvStart, 27);
        int playerHotbarStart = this.slots.size();

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
        this.playerOffhandSlots = new MergeSlotRange(this.slots.size(), 1);

        // Add the Offhand slot
        this.addSlot(new SlotItemHandlerGeneric(this.playerInv, 40, posX, posY)
        {
            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon()
            {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
            }
        });
    }

    public Player getPlayer()
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

    protected ItemStack getCursorStack()
    {
        return this.getCarried();
    }

    protected void setCursorStack(ItemStack stack)
    {
        this.setCarried(stack);
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot)
    {
        return (slot instanceof SlotItemHandler) &&
                //(slot instanceof SlotItemHandlerCraftResult) == false &&
                this.getCursorStack().isEmpty() == false;
    }

    @Override
    public Slot getSlot(int slotId)
    {
        return slotId >= 0 && slotId < this.slots.size() ? super.getSlot(slotId) : null;
    }

    public SlotItemHandlerGeneric getSlotItemHandler(int slotId)
    {
        Slot slot = this.getSlot(slotId);

        return (slot instanceof SlotItemHandlerGeneric) ? (SlotItemHandlerGeneric) slot : null;
    }

    @Override
    public void setItem(int slotNum, int stateId, ItemStack stack)
    {
        Slot slot = this.getSlot(slotNum);

        if (slot instanceof SlotItemHandlerGeneric)
        {
            ((SlotItemHandlerGeneric) slot).syncStack(stack);
        }
        else
        {
            super.setItem(slotNum, stateId, stack);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotNum)
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
    protected boolean transferStackFromSlot(Player player, int slotNum)
    {
        Slot slot = this.getSlot(slotNum);

        if (slot == null || slot.hasItem() == false || slot.mayPickup(player) == false)
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

    protected boolean transferStackFromPlayerMainInventory(Player player, int slotNum)
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

    protected boolean transferStackToPrioritySlots(Player player, int slotNum, boolean reverse)
    {
        boolean ret = false;

        for (MergeSlotRange slotRange : this.mergeSlotRangesPlayerToExt)
        {
            ret |= this.transferStackToSlotRange(player, slotNum, slotRange, reverse);
        }

        return ret;
    }

    protected boolean transferStackToSlotRange(Player player, int slotNum, MergeSlotRange slotRange, boolean reverse)
    {
        SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotNum);

        if (slot == null || slot.hasItem() == false || slot.mayPickup(player) == false)
        {
            return false;
        }

        ItemStack stack = slot.getItem().copy();
        int amount = Math.min(stack.getCount(), stack.getMaxStackSize());
        stack.setCount(amount);

        // Simulate the merge
        stack = this.mergeItemStack(stack, slotRange, reverse, true);

        if (stack.isEmpty() == false)
        {
            // If the item can't be put back to the slot, then we need to make sure that the whole
            // stack can be merged elsewhere before trying to (partially) merge it. Important for crafting slots!
            // Or if nothing could be merged, then also abort.
            if (slot.mayPlace(stack) == false || stack.getCount() == amount)
            {
                return false;
            }

            // Can merge at least some of the items, get the amount that can be merged
            amount -= stack.getCount();
        }

        // Get the actual stack for non-simulated merging
        stack = slot.remove(amount);
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
        return stack.isEmpty() == false ? Math.min(slot.getMaxStackSize(stack), stack.getMaxStackSize()) : slot.getMaxStackSize();
    }

    /**
     * This should NOT be called from anywhere in this mod, but just in case...
     */
    @Override
    protected boolean moveItemStackTo(ItemStack stack, int slotStart, int slotEndExclusive, boolean reverse)
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

            if (slot != null && slot.hasItem() && slot.mayPlace(stack))
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

                if (slot != null && slot.hasItem() == false && slot.mayPlace(stack))
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
}
