package fi.dy.masa.lowtechcrafting.tileentity;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.lowtechcrafting.inventory.ItemHandlerCraftResult;
import fi.dy.masa.lowtechcrafting.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.lowtechcrafting.inventory.container.ContainerCrafting;
import fi.dy.masa.lowtechcrafting.inventory.wrapper.InventoryCraftingWrapper;
import fi.dy.masa.lowtechcrafting.inventory.wrapper.ItemHandlerWrapperCrafter;
import fi.dy.masa.lowtechcrafting.reference.ModObjects;
import fi.dy.masa.lowtechcrafting.reference.Names;
import fi.dy.masa.lowtechcrafting.reference.Reference;
import fi.dy.masa.lowtechcrafting.util.InventoryUtils;

public class BlockEntityCrafting extends BlockEntity implements MenuProvider
{
    private final ItemStackHandlerTileEntity itemHandlerCraftingGrid;
    private final ItemStackHandlerTileEntity itemHandlerOutputBuffer;
    private final InventoryCraftingWrapper inventoryCrafting;
    private final ItemHandlerCraftResult itemHandlerCraftResult;
    private final ItemHandlerWrapperCrafter itemHandlerWrapperCrafter;
    private final IItemHandler itemHandlerExternal;
    private final LazyOptional<IItemHandler> inventoryCapability;
    private final String tileEntityName;
    private String customInventoryName;
    private FakePlayer fakePlayer;

    public BlockEntityCrafting(BlockPos pos, BlockState state)
    {
        super(ModObjects.TILE_TYPE_CRAFTING_TABLE, pos, state);

        this.tileEntityName = Names.CRAFTING_TABLE;
        this.itemHandlerCraftingGrid    = new ItemStackHandlerTileEntity(0, 9, 64, false, "Items", this);
        this.itemHandlerOutputBuffer    = new ItemStackHandlerTileEntity(1, 1, 64, false, "ItemsOut", this);
        this.itemHandlerCraftResult     = new ItemHandlerCraftResult(this::getLevel, this::getPlayer, this::getBlockPos);
        this.inventoryCrafting          = new InventoryCraftingWrapper(3, 3, this.itemHandlerCraftingGrid, this.itemHandlerCraftResult, this::getLevel);
        this.itemHandlerCraftResult.setCraftMatrix(this.inventoryCrafting);

        this.itemHandlerWrapperCrafter = new ItemHandlerWrapperCrafter(
                this.itemHandlerCraftingGrid,
                this.itemHandlerOutputBuffer,
                this.itemHandlerCraftResult,
                this.inventoryCrafting);

        this.itemHandlerExternal = new ItemHandlerWrapperCrafterExternal(this.itemHandlerWrapperCrafter);
        this.inventoryCapability = LazyOptional.of(() -> this.itemHandlerExternal);
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

    public void dropInventories()
    {
        InventoryUtils.dropInventoryContentsInWorld(this.getLevel(), this.getBlockPos(), this.itemHandlerCraftingGrid);
        InventoryUtils.dropInventoryContentsInWorld(this.getLevel(), this.getBlockPos(), this.itemHandlerOutputBuffer);
    }

    /**
     * Gets a FakePlayer, which are unique per dimension and per TileEntity type.
     * ONLY call this on the server side!!!
     */
    @Nonnull
    protected FakePlayer getPlayer()
    {
        if (this.fakePlayer == null && this.getLevel() instanceof ServerLevel)
        {
            int dim = this.getLevel().dimension().location().toString().hashCode();

            this.fakePlayer = FakePlayerFactory.get((ServerLevel) this.getLevel(),
                    new GameProfile(new UUID(dim, dim), Reference.MOD_ID + ":" + this.tileEntityName));
        }

        return this.fakePlayer;
    }

    public void readFromNBTCustom(CompoundTag nbt)
    {
        this.itemHandlerOutputBuffer.deserializeNBT(nbt);
        this.inventoryCrafting.deserializeNBT(nbt);

        if (nbt.contains("CustomName", Tag.TAG_STRING))
        {
            this.customInventoryName = nbt.getString("CustomName");
        }
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        this.readFromNBTCustom(tag); // This call needs to be at the super-most custom TE class
    }

    @Override
    public CompoundTag save(CompoundTag nbt)
    {
        nbt = super.save(nbt);

        nbt.merge(this.itemHandlerOutputBuffer.serializeNBT());
        nbt.merge(this.inventoryCrafting.serializeNBT());

        if (this.hasCustomName())
        {
            nbt.putString("CustomName", this.customInventoryName);
        }

        return nbt;
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        // The tag from this method is used for the initial chunk packet,
        // and it needs to have the TE position!
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("x", this.getBlockPos().getX());
        nbt.putInt("y", this.getBlockPos().getY());
        nbt.putInt("z", this.getBlockPos().getZ());

        // Add the per-block data to the tag
        return nbt;
    }

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        if (this.getLevel() != null)
        {
            return ClientboundBlockEntityDataPacket.create(this);
        }

        return null;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return this.inventoryCapability.cast();
        }

        return super.getCapability(capability, side);
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
        return this.hasCustomName() ? this.customInventoryName : "tile." + this.tileEntityName + ".name";
    }

    @Override
    public Component getDisplayName()
    {
        return new TranslatableComponent(this.getName());
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, Player player)
    {
        return new ContainerCrafting(windowId, player, this);
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
            return slot == 0 ? this.inventoryCrafter.getSlotLimit(slot) : 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot)
        {
            return this.inventoryCrafter.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            if (this.isItemValid(slot, stack) == false)
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
                // else: Could not insert, return the original stack
                return stack;
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

        @Override
        public boolean isItemValid(int slot, ItemStack stack)
        {
            return slot != 0 && this.inventoryCrafter.getStackInSlot(slot).isEmpty() != false;

            /*
            // Already items in the output buffer slot, don't insert anything
            if (this.inventoryCrafter.getStackInSlot(0).getCount() > 0)
            {
                return false;
            }
            */
        }
    }
}