package com.xcom.oneblocksolutions.inventory.wrapper;

import com.xcom.oneblocksolutions.inventory.ItemHandlerCraftResult;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InventoryCraftingWrapper extends InventoryCrafting
{
    private final int inventoryWidth;
    private final int inventoryHeight;
    private final ItemHandlerCraftResult craftResult;
    private IItemHandlerModifiable craftMatrix;
    @Nullable
    private EntityPlayerMP player;
    private boolean inhibitResultUpdate;

    public InventoryCraftingWrapper(int width, int height, IItemHandlerModifiable craftMatrix, ItemHandlerCraftResult resultInventory)
    {
        super(null, 0, 0); // dummy

        this.inventoryWidth = width;
        this.inventoryHeight = height;
        this.craftMatrix = craftMatrix;
        this.craftResult = resultInventory;
    }

    public void setPlayer(EntityPlayerMP player)
    {
        this.player = player;
    }

    public void setCraftMatrix(IItemHandlerModifiable craftMatrix)
    {
        this.craftMatrix = craftMatrix;
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

    @Override
    public ItemStack getStackInRowAndColumn(int row, int column)
    {
        if (row >= 0 && row < this.inventoryWidth && column >= 0 && column <= this.inventoryHeight)
        {
            return this.getStackInSlot(row + column * this.inventoryWidth);
        }

        return  ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStackFromSlot(int slot)
    {
        return this.craftMatrix.extractItem(slot, Integer.MAX_VALUE, false);
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount)
    {
        ItemStack stack = this.craftMatrix.extractItem(slot, amount, false);

        if (stack.isEmpty() == false)
        {
            this.markDirty();
        }

        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack)
    {
        this.craftMatrix.setStackInSlot(slot, stack);
        this.markDirty();
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

        this.markDirty();
    }

    @Override
    public void markDirty()
    {
        super.markDirty();

        this.updateCraftingOutput();
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player)
    {
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack)
    {
        return true;
    }

    private void setCraftResult(@Nonnull ItemStack stack)
    {
        this.craftResult.setStackInSlot(0, stack);
    }

    public void updateCraftingOutput()
    {
        if (this.inhibitResultUpdate == false && this.player != null && this.player.getEntityWorld().isRemote == false)
        {
            World world = this.player.getEntityWorld();
            IRecipe recipe = CraftingManager.findMatchingRecipe(this, world);
            ItemStack stack = ItemStack.EMPTY;

            // isHidden() is rather something like "isNBT()" or "modifiesData()"
            if (recipe != null)
            //&& (recipe.isHidden() || world.getGameRules().getBoolean("doLimitedCrafting") == false || this.player.getRecipeBook().containsRecipe(recipe)))
            {
                this.craftResult.setRecipe(recipe);
                stack = recipe.getCraftingResult(this);
            }

            this.setCraftResult(stack);
            //player.connection.sendPacket(new SPacketSetSlot(this.windowId, 0, stack));
        }
    }

    @Override
    public void fillStackedContents(RecipeItemHelper recipeItemHelper)
    {
        final int invSize = this.craftMatrix.getSlots();

        for (int slot = 0; slot < invSize; slot++)
        {
            recipeItemHelper.accountStack(this.craftMatrix.getStackInSlot(slot));
        }
    }

    public void openInventory(EntityPlayer player)
    {
    }

    public void closeInventory(EntityPlayer player)
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
}