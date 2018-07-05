package com.xcom.oneblocksolutions.tileentity;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.mojang.authlib.GameProfile;
import com.xcom.oneblocksolutions.Reference;
import com.xcom.oneblocksolutions.blocks.BlockCraftingTable;
import com.xcom.oneblocksolutions.inventory.ItemHandlerCraftResult;
import com.xcom.oneblocksolutions.inventory.ItemStackHandlerTileEntity;
import com.xcom.oneblocksolutions.inventory.wrapper.InventoryCraftingWrapper;
import com.xcom.oneblocksolutions.inventory.wrapper.ItemHandlerWrapperCrafter;
import com.xcom.oneblocksolutions.util.InventoryUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileEntityCrafting extends TileEntity
{
    private ItemStackHandlerTileEntity itemHandlerCraftingGrid;
    private ItemStackHandlerTileEntity itemHandlerOutput;
    private InventoryCraftingWrapper inventoryCrafting;
    private ItemHandlerCraftResult itemHandlerCraftResult;
    private ItemHandlerWrapperCrafter itemHandlerWrapperCrafter;
    private IItemHandler itemHandlerExternal;
    private String customInventoryName;
    private FakePlayer fakePlayer;
    private final String tileEntityName;

    public TileEntityCrafting()
    {
        this.tileEntityName = BlockCraftingTable.REGISTRY_NAME;
        this.itemHandlerCraftingGrid    = new ItemStackHandlerTileEntity(0, 9, 64, false, "Items", this);
        this.itemHandlerOutput          = new ItemStackHandlerTileEntity(1, 1, 64, false, "ItemsOut", this);
        this.itemHandlerCraftResult     = new ItemHandlerCraftResult();
        this.inventoryCrafting          = new InventoryCraftingWrapper(3, 3, this.itemHandlerCraftingGrid, this.itemHandlerCraftResult);

        this.itemHandlerWrapperCrafter = new ItemHandlerWrapperCrafter(
                this.itemHandlerCraftingGrid,
                this.itemHandlerOutput,
                this.itemHandlerCraftResult,
                this.inventoryCrafting);

        this.itemHandlerExternal = new ItemHandlerWrapperCrafterExternal(this.itemHandlerWrapperCrafter);
    }

    public InventoryCraftingWrapper getCraftingGridWrapperInventory()
    {
        return this.inventoryCrafting;
    }

    public ItemHandlerCraftResult getCraftResultInventory()
    {
        return this.itemHandlerCraftResult;
    }

    public IItemHandler getCraftingWrapperInventory()
    {
        return this.itemHandlerWrapperCrafter;
    }

    @Override
    public void onLoad()
    {
        if (this.getWorld().isRemote == false)
        {
            FakePlayer player = this.getPlayer();
            this.inventoryCrafting.setPlayer(player);
            this.itemHandlerCraftResult.init(this.inventoryCrafting, this.getWorld(), player, this.getPos());
            //this.crafter.onLoad();
        }
    }

    public void dropInventories()
    {
        InventoryUtils.dropInventoryContentsInWorld(this.getWorld(), this.getPos(), this.itemHandlerCraftingGrid);
        InventoryUtils.dropInventoryContentsInWorld(this.getWorld(), this.getPos(), this.itemHandlerOutput);
    }

    /**
     * Gets a FakePlayer, which are unique per dimension and per TileEntity type.
     * ONLY call this on the server side!!!
     * @return
     */
    @Nonnull
    protected FakePlayer getPlayer()
    {
        if (this.fakePlayer == null && this.getWorld() instanceof WorldServer)
        {
            int dim = this.getWorld().provider.getDimension();

            this.fakePlayer = FakePlayerFactory.get((WorldServer) this.getWorld(),
                    new GameProfile(new UUID(dim, dim), Reference.MODID + ":" + this.tileEntityName));
        }

        return this.fakePlayer;
    }

    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        this.itemHandlerCraftingGrid.deserializeNBT(nbt);
        this.itemHandlerOutput.deserializeNBT(nbt);

        if (nbt.hasKey("CustomName", Constants.NBT.TAG_STRING))
        {
            this.customInventoryName = nbt.getString("CustomName");
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.readFromNBTCustom(nbt); // This call needs to be at the super-most custom TE class
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt = super.writeToNBT(nbt);

        nbt.merge(this.itemHandlerCraftingGrid.serializeNBT());
        nbt.merge(this.itemHandlerOutput.serializeNBT());

        if (this.hasCustomName())
        {
            nbt.setString("CustomName", this.customInventoryName);
        }

        return nbt;
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        // The tag from this method is used for the initial chunk packet,
        // and it needs to have the TE position!
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("x", this.getPos().getX());
        nbt.setInteger("y", this.getPos().getY());
        nbt.setInteger("z", this.getPos().getZ());

        // Add the per-block data to the tag
        return nbt;
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        if (this.getWorld() != null)
        {
            return new SPacketUpdateTileEntity(this.getPos(), 0, this.getUpdateTag());
        }

        return null;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return this.itemHandlerExternal != null;
        }

        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.itemHandlerExternal);
        }

        return super.getCapability(capability, facing);
    }

    public void inventoryChanged(int inventoryId, int slot)
    {
    }

    public void setInventoryName(String name)
    {
        this.customInventoryName = name;
    }

    public boolean hasCustomName()
    {
        return this.customInventoryName != null && this.customInventoryName.length() > 0;
    }

    public String getName()
    {
        return this.hasCustomName() ? this.customInventoryName : Reference.MODID;
    }

    public static class ItemHandlerWrapperCrafterExternal implements IItemHandler
    {
        private final IItemHandler inventoryCrafter;

        public ItemHandlerWrapperCrafterExternal(IItemHandler inventoryCrafter)
        {
            this.inventoryCrafter = inventoryCrafter;
        }

        @Override
        public int getSlots()
        {
            return this.inventoryCrafter.getSlots();
        }

        @Override
        public int getSlotLimit(int slot)
        {
            return this.inventoryCrafter.getSlotLimit(slot);
        }

        @Override
        public ItemStack getStackInSlot(int slot)
        {
            return this.inventoryCrafter.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            if (slot == 0)
            {
                return stack;
            }

            // Already items in the slot, don't insert anything
            if (this.inventoryCrafter.getStackInSlot(slot).getCount() > 0)
            {
                return stack;
            }

            // Trying to insert more than one item: only insert one of them
            if (stack.getCount() > 1)
            {
                ItemStack stackInsert = stack.copy();
                stackInsert.setCount(1);
                stackInsert = this.inventoryCrafter.insertItem(slot, stackInsert, simulate);

                // Successfully inserted, return the original stack shrunk by one
                if (stackInsert.isEmpty())
                {
                    stack = stack.copy();
                    stack.shrink(1);
                    return stack;
                }
                // Could not insert, return the original stack
                else
                {
                    return stack;
                }
            }
            // Only inserting one item, handle it directly
            else
            {
                return this.inventoryCrafter.insertItem(slot, stack, simulate);
            }
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            return this.inventoryCrafter.extractItem(slot, amount, simulate);
        }
    }
}