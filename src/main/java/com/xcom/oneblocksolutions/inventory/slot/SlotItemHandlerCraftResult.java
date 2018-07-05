package com.xcom.oneblocksolutions.inventory.slot;

import com.google.common.collect.Lists;
import com.xcom.oneblocksolutions.inventory.ItemHandlerCraftResult;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.items.IItemHandler;

public class SlotItemHandlerCraftResult extends SlotItemHandlerGeneric
{
    private final EntityPlayer player;
    private final InventoryCrafting craftMatrix;
    private final ItemHandlerCraftResult craftResult;
    private int amountCrafted;

    public SlotItemHandlerCraftResult(
            InventoryCrafting craftMatrix,
            ItemHandlerCraftResult craftResult,
            IItemHandler inventoryWrapper,
            int index, int xPosition, int yPosition, EntityPlayer player)
    {
        super(inventoryWrapper, index, xPosition, yPosition);

        this.player = player;
        this.craftMatrix = craftMatrix;
        this.craftResult = craftResult;
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return false;
    }

    @Override
    public ItemStack decrStackSize(int amount)
    {
        if (this.getHasStack())
        {
            this.amountCrafted += Math.min(amount, this.getStack().getCount());
        }

        return super.decrStackSize(amount);
    }

    @Override
    protected void onCrafting(ItemStack stack, int amount)
    {
        this.amountCrafted += amount;
        this.onCrafting(stack);
    }

    @Override
    protected void onCrafting(ItemStack stack)
    {
        if (this.amountCrafted > 0)
        {
            stack.onCrafting(this.player.getEntityWorld(), this.player, this.amountCrafted);
            net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerCraftingEvent(this.player, stack, this.craftMatrix);
        }

        this.amountCrafted = 0;

        IRecipe recipe = this.craftResult.getRecipe();

        if (recipe != null && recipe.isDynamic() == false)
        {
            this.player.unlockRecipes(Lists.newArrayList(recipe));
            this.craftResult.setRecipe(null);
        }
    }
}
