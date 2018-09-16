package fi.dy.masa.lowtechcrafting.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.crafting.IRecipeContainer;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import fi.dy.masa.lowtechcrafting.inventory.ItemHandlerCraftResult;
import fi.dy.masa.lowtechcrafting.inventory.container.base.ContainerCustomSlotClick;
import fi.dy.masa.lowtechcrafting.inventory.container.base.MergeSlotRange;
import fi.dy.masa.lowtechcrafting.inventory.slot.SlotItemHandlerCraftResult;
import fi.dy.masa.lowtechcrafting.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.lowtechcrafting.inventory.wrapper.InventoryCraftingWrapper;
import fi.dy.masa.lowtechcrafting.tileentity.TileEntityCrafting;
import fi.dy.masa.lowtechcrafting.util.InventoryUtils;

public class ContainerCrafting extends ContainerCustomSlotClick implements IRecipeContainer
{
    protected final TileEntityCrafting te;
    private final InventoryCraftingWrapper invCraftingGrid;
    private final ItemHandlerCraftResult invCraftResult;
    private final IItemHandler invCraftingWrapper;
    private int craftingSlot;

    public ContainerCrafting(EntityPlayer player, TileEntityCrafting te)
    {
        super(player, te.getCraftingWrapperInventory());

        this.te = te;
        this.invCraftingGrid = te.getCraftingGridWrapperInventory();
        this.invCraftResult = te.getCraftResultInventory();
        this.invCraftingWrapper = te.getCraftingWrapperInventory();

        this.reAddSlots(8, 84);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return this.te.isInvalid() == false;
    }

    @Override
    protected void addCustomInventorySlots()
    {
        this.customInventorySlots = new MergeSlotRange(this.inventorySlots.size(), 10);

        int posX = 30;
        int posY = 17;

        IItemHandler invGrid = this.isClient ? new ItemStackHandler(9) : new InvWrapper(this.invCraftingGrid);
        IItemHandler invOutput = this.isClient ? new ItemStackHandler(1) : this.invCraftingWrapper;

        // The output slot must be slot number 0, and the crafting grid slots must follow,
        // for the vanilla RecipeBook Ghost Recipes to be rendered at the correct locations
        this.craftingSlot = this.inventorySlots.size();

        // The first slot in the inventory is the crafting output slot
        this.addSlotToContainer(new SlotItemHandlerCraftResult(this.invCraftingGrid, this.invCraftResult, invOutput, 0, 124, 35, this.player));

        for (int r = 0; r < 3; r++)
        {
            for (int c = 0; c < 3; c++)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(invGrid, r * 3 + c, posX + c * 18, posY + r * 18));
            }
        }

        // Update the output
        this.invCraftingGrid.markDirty();
    }

    @Override
    protected void shiftClickSlot(int slotNum, EntityPlayer player)
    {
        if (slotNum != this.craftingSlot)
        {
            super.shiftClickSlot(slotNum, player);
            return;
        }

        SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotNum);

        if (slot != null && slot.getHasStack())
        {
            ItemStack stackOrig = slot.getStack().copy();
            int num = 64;

            while (num-- > 0)
            {
                // Could not transfer the items, or ran out of some of the items, so the crafting result changed, bail out now
                if (this.transferStackFromSlot(player, slotNum) == false ||
                    InventoryUtils.areItemStacksEqual(stackOrig, slot.getStack()) == false)
                {
                    break;
                }
            }
        }
    }

    @Override
    protected void rightClickSlot(int slotNum, EntityPlayer player)
    {
        // Crafting output slot: just take the full stack as you would when left clicking
        if (slotNum == this.craftingSlot)
        {
            super.leftClickSlot(slotNum, player);
        }
        else
        {
            super.rightClickSlot(slotNum, player);
        }

        /*
        // This implements a craft-one-stack-on-right-click functionality
        SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotNum);

        if (slot != null && slot.getHasStack())
        {
            ItemStack stackOrig = slot.getStack().copy();
            int num = stackOrig.getMaxStackSize() / stackOrig.getCount();

            while (num-- > 0)
            {
                super.leftClickSlot(slotNum, player);

                // Ran out of some of the ingredients, so the crafting result changed, stop here
                if (InventoryUtils.areItemStacksEqual(stackOrig, slot.getStack()) == false)
                {
                    break;
                }
            }
        }
        */
    }

    @Override
    public ItemStack slotClick(int slotNum, int dragType, ClickType clickType, EntityPlayer player)
    {
        super.slotClick(slotNum, dragType, clickType, player);

        if (this.isClient == false && slotNum == this.craftingSlot)
        {
            this.syncSlotToClient(this.craftingSlot);
            this.syncCursorStackToClient();
        }

        return ItemStack.EMPTY;
    }

    @Override
    public InventoryCraftResult getCraftResult()
    {
        // dummy
        return new InventoryCraftResult();
    }

    @Override
    public InventoryCrafting getCraftMatrix()
    {
        return this.invCraftingGrid;
    }
}
