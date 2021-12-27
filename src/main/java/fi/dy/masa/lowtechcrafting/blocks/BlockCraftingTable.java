package fi.dy.masa.lowtechcrafting.blocks;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import fi.dy.masa.lowtechcrafting.tileentity.TileEntityCrafting;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BlockCraftingTable extends Block
{
    public static final ResourceLocation CONTENTS = new ResourceLocation("contents");

    public BlockCraftingTable()
    {
        super(Block.Properties.of(
                Material.WOOD,
                MaterialColor.WOOD)
                .strength(2.5F)
                .sound(SoundType.WOOD));

        this.registerDefaultState(this.stateDefinition.any());
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new TileEntityCrafting();
    }

    public boolean isTileEntityValid(TileEntity te)
    {
        return te != null && te.isRemoved() == false;
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock())
        {
            TileEntity te = world.getBlockEntity(pos);

            if (te instanceof TileEntityCrafting)
            {
                ((TileEntityCrafting) te).dropInventories();
                world.updateNeighbourForOutputSignal(pos, this);
            }

            world.removeBlockEntity(pos);
        }
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
    {
        TileEntity te = world.getBlockEntity(pos);

        if (te instanceof TileEntityCrafting)
        {
            TileEntityCrafting tec = (TileEntityCrafting) te;
            CompoundNBT nbt = stack.getTag();

            // If the ItemStack has a tag containing saved TE data, restore it to the just placed block/TE
            if (nbt != null && nbt.contains("BlockEntityTag", Constants.NBT.TAG_COMPOUND))
            {
                tec.readFromNBTCustom(nbt.getCompound("BlockEntityTag"));
            }
            else
            {
                if (stack.hasCustomHoverName())
                {
                    tec.setInventoryName(stack.getHoverName().getString());
                }
            }
        }
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) // onBlockActivated
    {
        TileEntity te = world.getBlockEntity(pos);

        if (te instanceof TileEntityCrafting && this.isTileEntityValid(te))
        {
            if (world.isClientSide == false && player instanceof ServerPlayerEntity)
            {
                NetworkHooks.openGui((ServerPlayerEntity) player, (TileEntityCrafting) te, pos);
            }

            return ActionResultType.SUCCESS;
        }

        return ActionResultType.PASS;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state)
    {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, World world, BlockPos pos)
    {
        TileEntity te = world.getBlockEntity(pos);

        if (te != null && this.isTileEntityValid(te))
        {
            LazyOptional<IItemHandler> optional = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.NORTH);

            if (optional.isPresent())
            {
                return calcRedstoneFromInventory(optional.orElse(null));
            }
        }

        return 0;
    }

    public static int calcRedstoneFromInventory(@Nullable IItemHandler inv)
    {
        if (inv != null)
        {
            final int numSlots = inv.getSlots();

            if (numSlots > 0)
            {
                int nonEmptyStacks = 0;

                // Ignore the output slot, start from slot 1
                for (int slot = 1; slot < numSlots; ++slot)
                {
                    ItemStack stack = inv.getStackInSlot(slot);

                    if (stack.isEmpty() == false)
                    {
                        ++nonEmptyStacks;
                    }
                }

                return (nonEmptyStacks * 15) / 9;
            }
        }

        return 0;
    }
}