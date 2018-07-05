package com.xcom.oneblocksolutions.inventory.container;

import com.xcom.oneblocksolutions.inventory.ItemHandlerCraftResult;
import com.xcom.oneblocksolutions.inventory.container.base.ContainerBase;
import com.xcom.oneblocksolutions.inventory.container.base.MergeSlotRange;
import com.xcom.oneblocksolutions.inventory.slot.SlotItemHandlerCraftResult;
import com.xcom.oneblocksolutions.inventory.slot.SlotItemHandlerGeneric;
import com.xcom.oneblocksolutions.inventory.wrapper.InventoryCraftingWrapper;
import com.xcom.oneblocksolutions.tileentity.TileEntityCrafting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class ContainerCrafting extends ContainerBase
{
    private final InventoryCraftingWrapper invCraftingGrid;
    private final ItemHandlerCraftResult invCraftResult;
    private final boolean isClient;

    public ContainerCrafting(EntityPlayer player, TileEntityCrafting te)
    {
        super(player, te);

        this.isClient = player.getEntityWorld().isRemote;
        this.invCraftingGrid = te.getCraftingGridWrapperInventory();
        this.invCraftResult = te.getCraftResultInventory();

        this.reAddSlots(8, 84);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        this.customInventorySlots = new MergeSlotRange(this.inventorySlots.size(), 10);

        int posX = 30;
        int posY = 17;

        IItemHandler invGrid = this.isClient ? new ItemStackHandler(9) : new InvWrapper(this.invCraftingGrid);

        for (int r = 0; r < 3; r++)
        {
            for (int c = 0; c < 3; c++)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(invGrid, r * 3 + c, posX + c * 18, posY + r * 18));
            }
        }

        // The first slot in the inventory is the crafting output slot
        this.addSlotToContainer(new SlotItemHandlerCraftResult(this.invCraftingGrid, this.invCraftResult, 0, 124, 35, this.player));

        // Update the output
        this.invCraftingGrid.markDirty();
    }

    protected void reAddSlots(int playerInventoryX, int playerInventoryY)
    {
        this.inventorySlots.clear();
        this.inventoryItemStacks.clear();

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(playerInventoryX, playerInventoryY);
    }
}
