package com.xcom.oneblocksolutions.inventory.container;

import com.xcom.oneblocksolutions.inventory.container.base.MergeSlotRange;
import com.xcom.oneblocksolutions.inventory.slot.SlotItemHandlerGeneric;
import com.xcom.oneblocksolutions.inventory.wrapper.InvWrapperSyncable;
import com.xcom.oneblocksolutions.inventory.wrapper.InventoryCraftingWrapper;
import com.xcom.oneblocksolutions.tileentity.TileEntityCrafting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.IItemHandler;

public class ContainerCrafting extends ContainerOBSBase
{
    private final InventoryCraftingWrapper invCraftingGrid;
    private final IItemHandler invCraftingGridWrapped;

    public ContainerCrafting(EntityPlayer player, TileEntityCrafting te)
    {
        super(player, te);

        this.invCraftingGrid = te.getCraftingGridWrapperInventory();
        this.invCraftingGridWrapped = new InvWrapperSyncable(this.invCraftingGrid);

        this.reAddSlots(8, 84);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        this.customInventorySlots = new MergeSlotRange(this.inventorySlots.size(), 9);

        int posX = 30;
        int posY = 17;

        for (int r = 0; r < 3; r++)
        {
            for (int c = 0; c < 3; c++)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.invCraftingGridWrapped, r * 3 + c, posX + c * 18, posY + r * 18));
            }
        }

        // The first slot in the inventory is the crafting output slot
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, 0, 124, 35));

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
