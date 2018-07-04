package com.xcom.oneblocksolutions.inventory;

import com.xcom.oneblocksolutions.tileentity.TileEntityCrafting;

public class ItemStackHandlerTileEntity extends ItemStackHandlerBasic
{
    protected final TileEntityCrafting te;
    protected final int inventoryId;

    public ItemStackHandlerTileEntity(int invSize, TileEntityCrafting te)
    {
        this(0, invSize, te);
    }

    public ItemStackHandlerTileEntity(int inventoryId, int invSize, TileEntityCrafting te)
    {
        super(invSize);
        this.te = te;
        this.inventoryId = inventoryId;
    }

    public ItemStackHandlerTileEntity(int inventoryId, int invSize, int stackLimit,
                                      boolean allowCustomStackSizes, String tagName, TileEntityCrafting te)
    {
        super(invSize, stackLimit, allowCustomStackSizes, tagName);
        this.te = te;
        this.inventoryId = inventoryId;
    }

    @Override
    public void onContentsChanged(int slot)
    {
        super.onContentsChanged(slot);

        this.te.inventoryChanged(this.inventoryId, slot);
        this.te.markDirty();
    }

    public int getInventoryId()
    {
        return this.inventoryId;
    }
}
