package fi.dy.masa.lowtechcrafting.inventory.wrapper;

import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;
import fi.dy.masa.lowtechcrafting.inventory.ItemHandlerCraftResult;
import fi.dy.masa.lowtechcrafting.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.lowtechcrafting.util.InventoryUtils;
import net.minecraftforge.common.util.INBTSerializable;

public class InventoryCraftingWrapper extends CraftingContainer implements INBTSerializable<CompoundTag>
{
    private final int inventoryWidth;
    private final int inventoryHeight;
    private final ItemHandlerCraftResult craftResult;
    private final NonNullList<ItemStack> gridCache;
    private final ItemStackHandlerTileEntity craftMatrix;
    private final Supplier<Level> worldSupplier;
    private Optional<CraftingRecipe> lastCraftedRecipe = Optional.empty();
    @Nullable private CraftingRecipe recipe;
    @Nullable private Level world;
    private boolean inhibitResultUpdate;
    private boolean gridDirty;

    public InventoryCraftingWrapper(int width, int height, ItemStackHandlerTileEntity craftMatrix,
            ItemHandlerCraftResult resultInventory, Supplier<Level> worldSupplier)
    {
        super(null, 0, 0); // dummy

        this.inventoryWidth = width;
        this.inventoryHeight = height;
        this.craftMatrix = craftMatrix;
        this.craftResult = resultInventory;
        this.gridCache = NonNullList.withSize(width * height, ItemStack.EMPTY);
        this.worldSupplier = worldSupplier;

        this.updateGridCache();
    }

    @Nullable
    private Level getWorld()
    {
        if (this.world == null)
        {
            this.world = this.worldSupplier.get();
        }

        return this.world;
    }

    public void setInhibitResultUpdate(boolean inhibitUpdate)
    {
        this.inhibitResultUpdate = inhibitUpdate;
    }

    @Override
    public int getHeight()
    {
        return this.inventoryHeight;
    }

    @Override
    public int getWidth()
    {
        return this.inventoryWidth;
    }

    @Override
    public int getContainerSize()
    {
        return this.craftMatrix.getSlots();
    }

    @Override
    public ItemStack getItem(int slot)
    {
        return slot >= this.getContainerSize() ? ItemStack.EMPTY : this.craftMatrix.getStackInSlot(slot);
    }

    @Override
    public boolean isEmpty()
    {
        final int invSize = this.craftMatrix.getSlots();

        for (int slot = 0; slot < invSize; ++slot)
        {
            if (this.getItem(slot).isEmpty() == false)
            {
                return false;
            }
        }

        return true;
    }

    public void updateGridCache()
    {
        for (int slot = 0; slot < this.craftMatrix.getSlots(); ++slot)
        {
            this.checkUpdateGridCacheForSlot(slot);
        }
    }

    public void checkUpdateGridCacheForSlot(int slot)
    {
        ItemStack stackNew = this.craftMatrix.getStackInSlot(slot);

        if (InventoryUtils.areItemStacksEqual(this.gridCache.get(slot), stackNew) == false)
        {
            this.gridCache.set(slot, stackNew.isEmpty() ? ItemStack.EMPTY : stackNew.copy());
            this.gridDirty = true;
        }
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot)
    {
        ItemStack stack = this.craftMatrix.extractItem(slot, this.craftMatrix.getStackInSlot(slot).getCount(), false);

        if (stack.isEmpty() == false)
        {
            this.gridCache.set(slot, ItemStack.EMPTY);
            this.gridDirty = true;
        }

        return stack;
    }

    @Override
    public ItemStack removeItem(int slot, int amount)
    {
        // This goes against the Forge IItemHandler contract,
        // but the vanilla ServerRecipeBookHelper goes into an infinite loop
        // if the original stack instance doesn't shrink... >_>

        ItemStack stackOrig = this.craftMatrix.getStackInSlot(slot);
        ItemStack stackReturn = stackOrig.copy();

        amount = Math.min(amount, stackOrig.getCount());
        stackReturn.setCount(amount);
        stackOrig.shrink(amount);

        this.craftMatrix.setStackInSlot(slot, stackOrig); // To mark the underlying inventory dirty
        this.checkUpdateGridCacheForSlot(slot);

        return stackReturn;
    }

    @Override
    public void setItem(int slot, ItemStack stack)
    {
        this.craftMatrix.setStackInSlot(slot, stack);
        this.checkUpdateGridCacheForSlot(slot);
    }

    @Override
    public int getMaxStackSize()
    {
        return this.craftMatrix.getSlotLimit(0);
    }

    @Override
    public void clearContent()
    {
        for (int slot = 0; slot < this.craftMatrix.getSlots(); slot++)
        {
            this.craftMatrix.setStackInSlot(slot, ItemStack.EMPTY);
        }

        this.updateGridCache();
    }

    @Override
    public boolean stillValid(Player player)
    {
        return true;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack)
    {
        return true;
    }

    private void setCraftResult(ItemStack stack)
    {
        this.craftResult.setStackInSlot(0, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
    }

    public void updateCraftingOutput()
    {
        Level world = this.getWorld();

        if (this.gridDirty && this.inhibitResultUpdate == false && world != null && world.isClientSide == false)
        {
            Optional<CraftingRecipe> optional = this.lastCraftedRecipe;

            if (optional.isPresent() == false || optional.get().matches(this, world) == false)
            {
                optional = world.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, this, world);
            }

            ItemStack stack = ItemStack.EMPTY;

            if (optional.isPresent())
            {
                CraftingRecipe recipe = optional.get();
                this.craftResult.setRecipe(recipe);
                this.recipe = recipe;
                stack = recipe.assemble(this);
            }

            this.setCraftResult(stack);
            //player.connection.sendPacket(new SPacketSetSlot(this.windowId, 0, stack));
            this.gridDirty = false;
        }
    }

    public void onCraft()
    {
        if (this.recipe != null)
        {
            this.lastCraftedRecipe = Optional.of(this.recipe);
        }
    }

    @Override
    public void fillStackedContents(StackedContents recipeItemHelper)
    {
        final int invSize = this.craftMatrix.getSlots();

        for (int slot = 0; slot < invSize; slot++)
        {
            recipeItemHelper.accountSimpleStack(this.craftMatrix.getStackInSlot(slot));
        }
    }

    public void startOpen(Player player)
    {
    }

    public void stopOpen(Player player)
    {
    }

    public int getField(int id)
    {
        return 0;
    }

    public void setField(int id, int value)
    {
    }

    public int getFieldCount()
    {
        return 0;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        return this.craftMatrix.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        this.craftMatrix.deserializeNBT(nbt);
        this.updateGridCache();
    }
}