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
    public boolean mayPlace(ItemStack stack)
    {
        return false;
    }

    @Override
    public ItemStack remove(int amount)
    {
        if (this.hasItem())
        {
            this.amountCrafted += Math.min(amount, this.getItem().getCount());
        }

        return super.remove(amount);
    }

    @Override
    protected void onQuickCraft(ItemStack stack, int amount)
    {
        this.amountCrafted += amount;
        this.checkTakeAchievements(stack);
    }

    @Override
    protected void checkTakeAchievements(ItemStack stack)
    {
        if (this.amountCrafted > 0)
        {
            stack.onCraftedBy(this.player.getCommandSenderWorld(), this.player, this.amountCrafted);
            net.minecraftforge.fml.hooks.BasicEventHooks.firePlayerCraftingEvent(this.player, stack, this.craftMatrix);
        }

        this.amountCrafted = 0;

        IRecipe<?> recipe = this.craftResult.getRecipe();

        if (recipe != null && recipe.isSpecial() == false)
        {
            this.player.awardRecipes(ImmutableList.of(recipe));
            this.craftResult.setRecipe(null);
        }
    }
}
