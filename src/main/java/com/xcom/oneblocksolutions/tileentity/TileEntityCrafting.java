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
import com.xcom.oneblocksolutions.inventory.wrapper.ItemHandlerWrapperCrafterContainer;
import com.xcom.oneblocksolutions.util.Util;
import net.minecraft.entity.player.EntityPlayer;
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

public class TileEntityCrafting extends TileEntity {

    private ItemStackHandlerTileEntity itemHandlerBase;
    private ItemStackHandlerTileEntity inventoryOutput;
    private InventoryCraftingWrapper inventoryCrafting;
    private ItemHandlerCraftResult inventoryCraftResult;
    private ItemHandlerWrapperCrafter crafter;
    private IItemHandler itemHandlerExternal;
    private String customInventoryName;
    private FakePlayer fakePlayer;
    private final String tileEntityName;

    public TileEntityCrafting() {
        this.tileEntityName = BlockCraftingTable.REGISTRY_NAME;
        this.itemHandlerBase = new ItemStackHandlerTileEntity(0, 9,  1, false, "Items", this);
        this.inventoryOutput = new ItemStackHandlerTileEntity(1, 1, 64, false, "ItemsOut", this);
        this.inventoryCraftResult = new ItemHandlerCraftResult();
        this.inventoryCrafting = new InventoryCraftingWrapper(3, 3, this.itemHandlerBase, this.inventoryCraftResult);

        this.crafter = new ItemHandlerWrapperCrafter(
                this.itemHandlerBase,
                this.inventoryOutput,
                this.inventoryCraftResult,
                this.inventoryCrafting);

        this.itemHandlerExternal = this.crafter;
    }

    /**
     * Returns an inventory wrapper for use in Containers/Slots.<br>
     */
    public IItemHandler getWrappedInventoryForContainer(EntityPlayer player) {
        return new ItemHandlerWrapperCrafterContainer(this.itemHandlerBase,
                this.itemHandlerExternal, this.inventoryOutput, true);
    }

    public InventoryCraftingWrapper getCraftingGridWrapperInventory() {
        return this.inventoryCrafting;
    }

    public ItemStackHandlerTileEntity getBaseItemHandler() {
        return itemHandlerBase;
    }

    @Override
    public void onLoad() {
        if (this.getWorld().isRemote == false)
        {
            FakePlayer player = this.getPlayer();
            this.inventoryCrafting.setPlayer(player);
            this.inventoryCraftResult.init(this.inventoryCrafting, this.getWorld(), player, this.getPos());
            //this.crafter.onLoad();
        }
    }

    public void dropInventories()
    {
        Util.dropInventoryContentsInWorld(this.getWorld(), this.getPos(), this.getBaseItemHandler());
        Util.dropInventoryContentsInWorld(this.getWorld(), this.getPos(), this.inventoryOutput);
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
        this.itemHandlerBase.deserializeNBT(nbt);
        this.inventoryOutput.deserializeNBT(nbt);

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
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);

        nbt.merge(this.itemHandlerBase.serializeNBT());
        nbt.merge(this.inventoryOutput.serializeNBT());

        if (this.hasCustomName())
        {
            nbt.setString("CustomName", this.customInventoryName);
        }

        return nbt;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
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
    public SPacketUpdateTileEntity getUpdatePacket() {
        if (this.getWorld() != null) {
            return new SPacketUpdateTileEntity(this.getPos(), 0, this.getUpdateTag());
        }

        return null;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return this.itemHandlerExternal != null;
        }

        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.itemHandlerExternal);
        }

        return super.getCapability(capability, facing);
    }

    public void inventoryChanged(int inventoryId, int slot) {
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
}