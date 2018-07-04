package com.xcom.oneblocksolutions.inventory.container;

import com.xcom.oneblocksolutions.inventory.container.base.MergeSlotRange;
import com.xcom.oneblocksolutions.inventory.slot.SlotItemHandlerGeneric;
import com.xcom.oneblocksolutions.tileentity.TileEntityCrafting;
import net.minecraft.entity.player.EntityPlayer;

public class ContainerCrafting extends ContainerOBSBase
{
    int posX = 30;
    int posY = 17;

    public ContainerCrafting(EntityPlayer player, TileEntityCrafting te)
    {
        super(player, te);

        this.reAddSlots(8, 84);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        this.customInventorySlots = new MergeSlotRange(this.inventorySlots.size(), 9);

        for (int r = 0; r < 3; r++)
        {
            for (int c = 0; c < 3; c++)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, r * 3 + c, posX + c * 18, posY + r * 18));
            }
        }
    }

    protected void reAddSlots(int playerInventoryX, int playerInventoryY)
    {
        this.inventorySlots.clear();
        this.inventoryItemStacks.clear();

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(playerInventoryX, playerInventoryY);
    }
}
