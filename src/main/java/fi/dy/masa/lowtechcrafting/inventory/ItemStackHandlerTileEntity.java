package fi.dy.masa.lowtechcrafting.inventory;

import fi.dy.masa.lowtechcrafting.tileentity.BlockEntityCrafting;

public class ItemStackHandlerTileEntity extends ItemStackHandlerBasic
{
    protected final BlockEntityCrafting te;
    protected final int inventoryId;

    public ItemStackHandlerTileEntity(int invSize, BlockEntityCrafting te)
    {
        this(0, invSize, te);
    }

    public ItemStackHandlerTileEntity(int inventoryId, int invSize, BlockEntityCrafting te)
    {
        super(invSize);
        this.te = te;
        this.inventoryId = inventoryId;
    }

    public ItemStackHandlerTileEntity(int inventoryId, int invSize, int stackLimit,
                                      boolean allowCustomStackSizes, String tagName, BlockEntityCrafting te)
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
        this.te.setChanged();
    }

    public int getInventoryId()
    {
        return this.inventoryId;
    }
}
