package fi.dy.masa.lowtechcrafting.inventory.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import fi.dy.masa.lowtechcrafting.inventory.ItemHandlerCraftResult;
import fi.dy.masa.lowtechcrafting.inventory.container.base.ContainerCustomSlotClick;
import fi.dy.masa.lowtechcrafting.inventory.container.base.MergeSlotRange;
import fi.dy.masa.lowtechcrafting.inventory.slot.SlotItemHandlerCraftResult;
import fi.dy.masa.lowtechcrafting.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.lowtechcrafting.inventory.wrapper.InventoryCraftingWrapper;
import fi.dy.masa.lowtechcrafting.reference.ModObjects;
import fi.dy.masa.lowtechcrafting.tileentity.TileEntityCrafting;
import fi.dy.masa.lowtechcrafting.util.InventoryUtils;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class ContainerCrafting extends ContainerCustomSlotClick //<InventoryCraftingWrapper> implements IRecipeContainer
{
    protected final TileEntityCrafting te;
    private final InventoryCraftingWrapper invCraftingGrid;
    private final ItemHandlerCraftResult invCraftResult;
    private final IItemHandler invCraftingWrapper;
    private int craftingSlot;

    public ContainerCrafting(int windowId, PlayerInventory playerInv, PacketBuffer extraData)
    {
        this(windowId, playerInv.player, ((TileEntityCrafting) playerInv.player.getEntityWorld().getTileEntity(extraData.readBlockPos())));
    }

    public ContainerCrafting(int windowId, PlayerEntity player, TileEntityCrafting te)
    {
        super(windowId, ModObjects.CONTAINER_TYPE_CRAFTING_TABLE, player, te.getCraftingWrapperInventory());

        this.te = te;
        this.invCraftingGrid = te.getCraftingGridWrapperInventory();
        this.invCraftResult = te.getCraftResultInventory();
        this.invCraftingWrapper = te.getCraftingWrapperInventory();

        this.reAddSlots(8, 84);
    }

    @Override
    public boolean canInteractWith(PlayerEntity player)
    {
        return this.te.isRemoved() == false;
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
        this.addSlot(new SlotItemHandlerCraftResult(this.invCraftingGrid, this.invCraftResult, invOutput, 0, 124, 35, this.player));

        for (int r = 0; r < 3; r++)
        {
            for (int c = 0; c < 3; c++)
            {
                this.addSlot(new SlotItemHandlerGeneric(invGrid, r * 3 + c, posX + c * 18, posY + r * 18));
            }
        }

        // Update the output
        this.invCraftingGrid.markDirty();
    }

    @Override
    protected void shiftClickSlot(int slotNum, PlayerEntity player)
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
    protected void rightClickSlot(int slotNum, PlayerEntity player)
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
    public void detectAndSendChanges()
    {
        // Lazy update the output before syncing the slots
        this.invCraftingGrid.updateCraftingOutput();

        super.detectAndSendChanges();
    }

    @Override
    public ItemStack slotClick(int slotNum, int dragType, ClickType clickType, PlayerEntity player)
    {
        super.slotClick(slotNum, dragType, clickType, player);

        if (this.isClient == false && slotNum == this.craftingSlot)
        {
            this.syncSlotToClient(this.craftingSlot);
            this.syncCursorStackToClient();
        }

        return ItemStack.EMPTY;
    }

    /*
    @Override
    public CraftResultInventory getCraftResult()
    {
        // dummy
        return new CraftResultInventory();
    }

    @Override
    public CraftingInventory getCraftMatrix()
    {
        return this.invCraftingGrid;
    }

    @Override
    public void func_201771_a(RecipeItemHelper helper)
    {
        this.invCraftingGrid.fillStackedContents(helper);
    }

    @Override
    public void clear()
    {
        this.invCraftingGrid.clear();
        this.invCraftResult.setStackInSlot(0, ItemStack.EMPTY);
    }

    @Override
    public boolean matches(IRecipe<? super InventoryCraftingWrapper> recipeIn)
    {
        return recipeIn.matches(this.invCraftingGrid, this.player.getEntityWorld());
    }

    @Override
    public int getWidth()
    {
        return this.invCraftingGrid.getWidth();
    }

    @Override
    public int getHeight()
    {
        return this.invCraftingGrid.getHeight();
    }

    @Override
    public List<RecipeBookCategories> getRecipeBookCategories()
    {
        return ImmutableList.of(RecipeBookCategories.SEARCH, RecipeBookCategories.EQUIPMENT, RecipeBookCategories.BUILDING_BLOCKS, RecipeBookCategories.MISC, RecipeBookCategories.REDSTONE);
    }
    */
}
