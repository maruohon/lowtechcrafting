package fi.dy.masa.lowtechcrafting.inventory.wrapper;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import fi.dy.masa.lowtechcrafting.inventory.ItemHandlerCraftResult;
import fi.dy.masa.lowtechcrafting.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.lowtechcrafting.util.InventoryUtils;
import net.minecraftforge.common.util.INBTSerializable;

public class InventoryCraftingWrapper extends CraftingInventory implements INBTSerializable<CompoundNBT>
{
    private final int inventoryWidth;
    private final int inventoryHeight;
    private final ItemHandlerCraftResult craftResult;
    private final NonNullList<ItemStack> gridCache;
    private ItemStackHandlerTileEntity craftMatrix;
    private Optional<ICraftingRecipe> lastCraftedRecipe = Optional.empty();
    @Nullable private ICraftingRecipe recipe;
    @Nullable private World world;
    private boolean inhibitResultUpdate;
    private boolean gridDirty;

    public InventoryCraftingWrapper(int width, int height, ItemStackHandlerTileEntity craftMatrix, ItemHandlerCraftResult resultInventory)
    {
        super(null, 0, 0); // dummy

        this.inventoryWidth = width;
        this.inventoryHeight = height;
        this.craftMatrix = craftMatrix;
        this.craftResult = resultInventory;
        this.gridCache = NonNullList.withSize(width * height, ItemStack.EMPTY);

        this.updateGridCache();
    }

    public void setWorld(World world)
    {
        this.world = world;
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
    public int getSizeInventory()
    {
        return this.craftMatrix.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        return slot >= this.getSizeInventory() ? ItemStack.EMPTY : this.craftMatrix.getStackInSlot(slot);
    }

    @Override
    public boolean isEmpty()
    {
        final int invSize = this.craftMatrix.getSlots();

        for (int slot = 0; slot < invSize; ++slot)
        {
            if (this.getStackInSlot(slot).isEmpty() == false)
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
    public ItemStack removeStackFromSlot(int slot)
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
    public ItemStack decrStackSize(int slot, int amount)
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
    public void setInventorySlotContents(int slot, ItemStack stack)
    {
        this.craftMatrix.setStackInSlot(slot, stack);
        this.checkUpdateGridCacheForSlot(slot);
    }

    @Override
    public int getInventoryStackLimit()
    {
        return this.craftMatrix.getSlotLimit(0);
    }

    @Override
    public void clear()
    {
        for (int slot = 0; slot < this.craftMatrix.getSlots(); slot++)
        {
            this.craftMatrix.setStackInSlot(slot, ItemStack.EMPTY);
        }

        this.updateGridCache();
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player)
    {
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack)
    {
        return true;
    }

    private void setCraftResult(ItemStack stack)
    {
        this.craftResult.setStackInSlot(0, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
    }

    public void updateCraftingOutput()
    {
        if (this.gridDirty && this.inhibitResultUpdate == false && this.world != null && this.world.isRemote == false)
        {
            Optional<ICraftingRecipe> optional = this.lastCraftedRecipe;

            if (optional.isPresent() == false || optional.get().matches(this, this.world) == false)
            {
                optional = this.world.getServer().getRecipeManager().getRecipe(IRecipeType.CRAFTING, this, this.world);
            }

            ItemStack stack = ItemStack.EMPTY;

            if (optional.isPresent())
            {
                ICraftingRecipe recipe = optional.get();
                this.craftResult.setRecipe(recipe);
                this.recipe = recipe;
                stack = recipe.getCraftingResult(this);
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
    public void fillStackedContents(RecipeItemHelper recipeItemHelper)
    {
        final int invSize = this.craftMatrix.getSlots();

        for (int slot = 0; slot < invSize; slot++)
        {
            recipeItemHelper.accountPlainStack(this.craftMatrix.getStackInSlot(slot));
        }
    }

    public void openInventory(PlayerEntity player)
    {
    }

    public void closeInventory(PlayerEntity player)
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
    public CompoundNBT serializeNBT()
    {
        return this.craftMatrix.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt)
    {
        this.craftMatrix.deserializeNBT(nbt);
        this.updateGridCache();
    }
}