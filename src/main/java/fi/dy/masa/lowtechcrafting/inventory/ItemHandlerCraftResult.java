package fi.dy.masa.lowtechcrafting.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import fi.dy.masa.lowtechcrafting.inventory.wrapper.InventoryCraftingWrapper;
import fi.dy.masa.lowtechcrafting.util.EntityUtils;
import fi.dy.masa.lowtechcrafting.util.InventoryUtils;

public class ItemHandlerCraftResult extends ItemStackHandlerBasic
{
    @Nullable
    private World world;
    @Nullable
    private BlockPos pos = BlockPos.ORIGIN;
    @Nullable
    private EntityPlayer player;
    @Nullable
    private InventoryCraftingWrapper craftMatrix;
    @Nullable
    private IRecipe recipe;

    public ItemHandlerCraftResult()
    {
        super(1);
    }

    public void init(InventoryCraftingWrapper craftMatrix, World world, EntityPlayer player, BlockPos pos)
    {
        this.craftMatrix = craftMatrix;
        this.world = world;
        this.player = player;
        this.pos = pos;
    }

    public void setRecipe(@Nullable IRecipe recipe)
    {
        this.recipe = recipe;
    }

    @Nullable
    public IRecipe getRecipe()
    {
        return this.recipe;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        ItemStack stack = super.extractItem(slot, this.getStackInSlot(slot).getCount(), simulate);

        if (simulate == false && this.player != null)
        {
            this.onCraft(this.player, stack);
        }

        return stack;
    }

    private void onCraft(ItemStack stack)
    {
        stack.onCrafting(this.world, this.player, stack.getCount());
        net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerCraftingEvent(this.player, stack, this.craftMatrix);

        IRecipe recipe = this.getRecipe();

        if (recipe != null && recipe.isDynamic() == false)
        {
            // This will crash when the RecipeBook tries to send a packet to the FakePlayer
            //this.player.unlockRecipes(Lists.newArrayList(recipe));
            this.setRecipe(null);
        }
    }

    private void onCraft(EntityPlayer player, ItemStack stack)
    {
        this.onCraft(stack);

        net.minecraftforge.common.ForgeHooks.setCraftingPlayer(player);
        NonNullList<ItemStack> remainingItems = CraftingManager.getRemainingItems(this.craftMatrix, player.getEntityWorld());
        net.minecraftforge.common.ForgeHooks.setCraftingPlayer(null);

        // Prevent unnecessary updates via the markDirty() method, while updating the grid contents
        this.craftMatrix.setInhibitResultUpdate(true);

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
                    EntityUtils.dropItemStacksInWorld(this.world, this.pos, remainingItemsInSlot, -1, true);
                }
            }
        }

        // Re-enable updates, and force update the output
        this.craftMatrix.setInhibitResultUpdate(false);
        this.craftMatrix.markDirty();
    }
}
