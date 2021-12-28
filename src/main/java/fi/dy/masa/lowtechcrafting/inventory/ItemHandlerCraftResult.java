package fi.dy.masa.lowtechcrafting.inventory;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import fi.dy.masa.lowtechcrafting.inventory.wrapper.InventoryCraftingWrapper;
import fi.dy.masa.lowtechcrafting.util.EntityUtils;
import fi.dy.masa.lowtechcrafting.util.InventoryUtils;

public class ItemHandlerCraftResult extends ItemStackHandlerBasic
{
    private final Supplier<Level> worldSupplier;
    private final Supplier<Player> playerSupplier;
    private final Supplier<BlockPos> posSupplier;
    @Nullable private Level world;
    @Nullable private Player player;
    @Nullable private BlockPos pos;
    @Nullable private InventoryCraftingWrapper craftMatrix;
    @Nullable private CraftingRecipe recipe;

    public ItemHandlerCraftResult(Supplier<Level> worldSupplier, Supplier<Player> playerSupplier, Supplier<BlockPos> posSupplier)
    {
        super(1);

        this.worldSupplier = worldSupplier;
        this.playerSupplier = playerSupplier;
        this.posSupplier = posSupplier;
    }

    public void setCraftMatrix(InventoryCraftingWrapper craftMatrix)
    {
        this.craftMatrix = craftMatrix;
    }

    public void setRecipe(@Nullable CraftingRecipe recipe)
    {
        this.recipe = recipe;
    }

    @Nullable
    public CraftingRecipe getRecipe()
    {
        return this.recipe;
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

    @Nullable
    private Player getPlayer()
    {
        if (this.player == null)
        {
            this.player = this.playerSupplier.get();
        }

        return this.player;
    }

    @Nullable
    private BlockPos getPos()
    {
        if (this.pos == null)
        {
            this.pos = this.posSupplier.get();
        }

        return this.pos;
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        // Lazy update the result slot
        this.craftMatrix.updateCraftingOutput();

        return super.getStackInSlot(slot);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        // Lazy update the result slot
        this.craftMatrix.updateCraftingOutput();

        // Always simulate, to not actually empty out the result slot.
        // This is because it would not be re-set unless the grid contents change,
        // ie. crafting from multiple input items at once would break.
        ItemStack stack = super.extractItem(slot, this.getStackInSlot(slot).getCount(), true);

        if (simulate == false)
        {
            this.onCraft(stack);
        }

        return stack;
    }

    private void onCraft(ItemStack stack)
    {
        Player player = this.getPlayer();
        Level world = this.getWorld();
        BlockPos pos = this.getPos();

        if (player == null || world == null || pos == null)
        {
            return;
        }

        stack.onCraftedBy(world, player, stack.getCount());
        ForgeEventFactory.firePlayerCraftingEvent(player, stack, this.craftMatrix);
        ForgeHooks.setCraftingPlayer(player);

        //NonNullList<ItemStack> remainingItems = world.getRecipeManager().getRecipeNonNull(IRecipeType.CRAFTING, this.craftMatrix, this.world);
        NonNullList<ItemStack> remainingItems = this.getRemainingItems();

        ForgeHooks.setCraftingPlayer(null);

        // Prevent unnecessary updates via the markDirty() method, while updating the grid contents
        this.craftMatrix.setInhibitResultUpdate(true);
        this.craftMatrix.onCraft();

        for (int slot = 0; slot < remainingItems.size(); slot++)
        {
            ItemStack stackInSlot = this.craftMatrix.getItem(slot);
            ItemStack remainingItemsInSlot = remainingItems.get(slot);

            if (stackInSlot.isEmpty() == false)
            {
                this.craftMatrix.removeItem(slot, 1);
                stackInSlot = this.craftMatrix.getItem(slot);
            }

            if (remainingItemsInSlot.isEmpty() == false)
            {
                if (stackInSlot.isEmpty())
                {
                    this.craftMatrix.setItem(slot, remainingItemsInSlot);
                }
                else if (InventoryUtils.areItemStacksEqual(stackInSlot, remainingItemsInSlot))
                {
                    remainingItemsInSlot.grow(stackInSlot.getCount());
                    this.craftMatrix.setItem(slot, remainingItemsInSlot);
                }
                else
                {
                    EntityUtils.dropItemStacksInWorld(world, pos, remainingItemsInSlot, -1, true);
                }
            }
        }

        /*
        IRecipe<?> recipe = this.getRecipe();

        if (recipe != null && recipe.isDynamic() == false)
        {
            // This will crash when the RecipeBook tries to send a packet to the FakePlayer
            //this.player.unlockRecipes(Lists.newArrayList(recipe));
            this.setRecipe(null);
        }
        */

        // Re-enable updates, and force update the output
        this.craftMatrix.setInhibitResultUpdate(false);
    }

    /**
     * This is the same as <b>NonNullList<ItemStack> remainingItems = world.getRecipeManager().getRecipeNonNull(IRecipeType.CRAFTING, this.craftMatrix, world);</b>
     * except that the recipe won't be queried again for nothing...
     * @return
     */
    private NonNullList<ItemStack> getRemainingItems()
    {
        if (this.recipe != null)
        {
            return this.recipe.getRemainingItems(this.craftMatrix);
        }
        else
        {
            NonNullList<ItemStack> items = NonNullList.withSize(this.craftMatrix.getContainerSize(), ItemStack.EMPTY);

            for (int i = 0; i < items.size(); ++i)
            {
                items.set(i, this.craftMatrix.getItem(i));
            }

            return items;
        }
    }
}
