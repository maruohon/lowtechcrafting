package fi.dy.masa.lowtechcrafting.inventory;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import fi.dy.masa.lowtechcrafting.inventory.wrapper.InventoryCraftingWrapper;
import fi.dy.masa.lowtechcrafting.util.EntityUtils;
import fi.dy.masa.lowtechcrafting.util.InventoryUtils;

public class ItemHandlerCraftResult extends ItemStackHandlerBasic
{
    private final Supplier<World> worldSupplier;
    private final Supplier<PlayerEntity> playerSupplier;
    private final Supplier<BlockPos> posSupplier;
    @Nullable private World world;
    @Nullable private PlayerEntity player;
    @Nullable private BlockPos pos;
    @Nullable private InventoryCraftingWrapper craftMatrix;
    @Nullable private ICraftingRecipe recipe;

    public ItemHandlerCraftResult(Supplier<World> worldSupplier, Supplier<PlayerEntity> playerSupplier, Supplier<BlockPos> posSupplier)
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

    public void setRecipe(@Nullable ICraftingRecipe recipe)
    {
        this.recipe = recipe;
    }

    @Nullable
    public ICraftingRecipe getRecipe()
    {
        return this.recipe;
    }

    @Nullable
    private World getWorld()
    {
        if (this.world == null)
        {
            this.world = this.worldSupplier.get();
        }

        return this.world;
    }

    @Nullable
    private PlayerEntity getPlayer()
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
        PlayerEntity player = this.getPlayer();
        World world = this.getWorld();
        BlockPos pos = this.getPos();

        if (player == null || world == null || pos == null)
        {
            return;
        }

        stack.onCrafting(world, player, stack.getCount());
        net.minecraftforge.fml.hooks.BasicEventHooks.firePlayerCraftingEvent(player, stack, this.craftMatrix);
        net.minecraftforge.common.ForgeHooks.setCraftingPlayer(player);

        //NonNullList<ItemStack> remainingItems = world.getRecipeManager().getRecipeNonNull(IRecipeType.CRAFTING, this.craftMatrix, this.world);
        NonNullList<ItemStack> remainingItems = this.getRemainingItems();

        net.minecraftforge.common.ForgeHooks.setCraftingPlayer(null);

        // Prevent unnecessary updates via the markDirty() method, while updating the grid contents
        this.craftMatrix.setInhibitResultUpdate(true);
        this.craftMatrix.onCraft();

        for (int slot = 0; slot < remainingItems.size(); slot++)
        {
            ItemStack stackInSlot = this.craftMatrix.getStackInSlot(slot);
            ItemStack remainingItemsInSlot = remainingItems.get(slot);

            if (stackInSlot.isEmpty() == false)
            {
                this.craftMatrix.decrStackSize(slot, 1);
                stackInSlot = this.craftMatrix.getStackInSlot(slot);
            }

            if (remainingItemsInSlot.isEmpty() == false)
            {
                if (stackInSlot.isEmpty())
                {
                    this.craftMatrix.setInventorySlotContents(slot, remainingItemsInSlot);
                }
                else if (InventoryUtils.areItemStacksEqual(stackInSlot, remainingItemsInSlot))
                {
                    remainingItemsInSlot.grow(stackInSlot.getCount());
                    this.craftMatrix.setInventorySlotContents(slot, remainingItemsInSlot);
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
            NonNullList<ItemStack> items = NonNullList.withSize(this.craftMatrix.getSizeInventory(), ItemStack.EMPTY);

            for (int i = 0; i < items.size(); ++i)
            {
                items.set(i, this.craftMatrix.getStackInSlot(i));
            }

            return items;
        }
    }
}
