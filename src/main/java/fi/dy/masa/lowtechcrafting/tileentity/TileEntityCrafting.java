package fi.dy.masa.lowtechcrafting.tileentity;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import fi.dy.masa.lowtechcrafting.inventory.ItemHandlerCraftResult;
import fi.dy.masa.lowtechcrafting.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.lowtechcrafting.inventory.container.ContainerCrafting;
import fi.dy.masa.lowtechcrafting.inventory.wrapper.InventoryCraftingWrapper;
import fi.dy.masa.lowtechcrafting.inventory.wrapper.ItemHandlerWrapperCrafter;
import fi.dy.masa.lowtechcrafting.reference.ModObjects;
import fi.dy.masa.lowtechcrafting.reference.Names;
import fi.dy.masa.lowtechcrafting.reference.Reference;
import fi.dy.masa.lowtechcrafting.util.InventoryUtils;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileEntityCrafting extends TileEntity implements INamedContainerProvider
{
    private ItemStackHandlerTileEntity itemHandlerCraftingGrid;
    private ItemStackHandlerTileEntity itemHandlerOutputBuffer;
    private InventoryCraftingWrapper inventoryCrafting;
    private ItemHandlerCraftResult itemHandlerCraftResult;
    private ItemHandlerWrapperCrafter itemHandlerWrapperCrafter;
    private IItemHandler itemHandlerExternal;
    private String customInventoryName;
    private FakePlayer fakePlayer;
    private final String tileEntityName;

    public TileEntityCrafting()
    {
        super(ModObjects.TILE_TYPE_CRAFTING_TABLE);

        this.tileEntityName = Names.CRAFTING_TABLE;
        this.itemHandlerCraftingGrid    = new ItemStackHandlerTileEntity(0, 9, 64, false, "Items", this);
        this.itemHandlerOutputBuffer    = new ItemStackHandlerTileEntity(1, 1, 64, false, "ItemsOut", this);
        this.itemHandlerCraftResult     = new ItemHandlerCraftResult();
        this.inventoryCrafting          = new InventoryCraftingWrapper(3, 3, this.itemHandlerCraftingGrid, this.itemHandlerCraftResult);

        this.itemHandlerWrapperCrafter = new ItemHandlerWrapperCrafter(
                this.itemHandlerCraftingGrid,
                this.itemHandlerOutputBuffer,
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
        World world = this.getWorld();

        if (world.isRemote == false)
        {
            this.inventoryCrafting.setWorld(world);
            this.itemHandlerCraftResult.init(this.inventoryCrafting, world, this::getPlayer, this.getPos());
        }
    }

    public void dropInventories()
    {
        InventoryUtils.dropInventoryContentsInWorld(this.getWorld(), this.getPos(), this.itemHandlerCraftingGrid);
        InventoryUtils.dropInventoryContentsInWorld(this.getWorld(), this.getPos(), this.itemHandlerOutputBuffer);
    }

    /**
     * Gets a FakePlayer, which are unique per dimension and per TileEntity type.
     * ONLY call this on the server side!!!
     * @return
     */
    @Nonnull
    protected FakePlayer getPlayer()
    {
        if (this.fakePlayer == null && this.getWorld() instanceof ServerWorld)
        {
            int dim = this.getWorld().getDimension().hashCode();

            this.fakePlayer = FakePlayerFactory.get((ServerWorld) this.getWorld(),
                    new GameProfile(new UUID(dim, dim), Reference.MOD_ID + ":" + this.tileEntityName));
        }

        return this.fakePlayer;
    }

    public void readFromNBTCustom(CompoundNBT nbt)
    {
        this.itemHandlerOutputBuffer.deserializeNBT(nbt);
        this.inventoryCrafting.deserializeNBT(nbt);

        if (nbt.contains("CustomName", Constants.NBT.TAG_STRING))
        {
            this.customInventoryName = nbt.getString("CustomName");
        }
    }

    @Override
    public void read(CompoundNBT nbt)
    {
        super.read(nbt);
        this.readFromNBTCustom(nbt); // This call needs to be at the super-most custom TE class
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt)
    {
        nbt = super.write(nbt);

        nbt.merge(this.itemHandlerOutputBuffer.serializeNBT());
        nbt.merge(this.inventoryCrafting.serializeNBT());

        if (this.hasCustomName())
        {
            nbt.putString("CustomName", this.customInventoryName);
        }

        return nbt;
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        // The tag from this method is used for the initial chunk packet,
        // and it needs to have the TE position!
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("x", this.getPos().getX());
        nbt.putInt("y", this.getPos().getY());
        nbt.putInt("z", this.getPos().getZ());

        // Add the per-block data to the tag
        return nbt;
    }

    @Override
    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        if (this.getWorld() != null)
        {
            return new SUpdateTileEntityPacket(this.getPos(), 0, this.getUpdateTag());
        }

        return null;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return LazyOptional.of(() -> this.itemHandlerExternal).cast();
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
    public ITextComponent getDisplayName()
    {
        return new TranslationTextComponent(this.getName());
    }

    @Override
    public Container createMenu(int windowId, PlayerInventory playerInv, PlayerEntity player)
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

        @Override
        public boolean isItemValid(int slot, ItemStack stack)
        {
            if (slot == 0 || this.inventoryCrafter.getStackInSlot(slot).isEmpty() == false)
            {
                return false;
            }

            /*
            // Already items in the output buffer slot, don't insert anything
            if (this.inventoryCrafter.getStackInSlot(0).getCount() > 0)
            {
                return false;
            }
            */

            return true;
        }
    }
}