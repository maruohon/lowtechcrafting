package fi.dy.masa.lowtechcrafting.inventory.slot;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import fi.dy.masa.lowtechcrafting.inventory.ItemHandlerCraftResult;
import net.minecraftforge.items.IItemHandler;

public class SlotItemHandlerCraftResult extends SlotItemHandlerGeneric
{
    private final PlayerEntity player;
    private final CraftingInventory craftMatrix;
    private final ItemHandlerCraftResult craftResult;
    private int amountCrafted;

    public SlotItemHandlerCraftResult(
            CraftingInventory craftMatrix,
            ItemHandlerCraftResult craftResult,
            IItemHandler inventoryWrapper,
            int index, int xPosition, int yPosition, PlayerEntity player)
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
            net.minecraftforge.fml.hooks.BasicEventHooks.firePlayerCraftingEvent(this.player, stack, this.craftMatrix);
        }

        this.amountCrafted = 0;

        IRecipe<?> recipe = this.craftResult.getRecipe();

        if (recipe != null && recipe.isDynamic() == false)
        {
            this.player.unlockRecipes(ImmutableList.of(recipe));
            this.craftResult.setRecipe(null);
        }
    }
}
