package fi.dy.masa.lowtechcrafting.blocks;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;

import fi.dy.masa.lowtechcrafting.tileentity.BlockEntityCrafting;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockCraftingTable extends BaseEntityBlock
{
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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new BlockEntityCrafting(pos, state);
    }

    public boolean isTileEntityValid(BlockEntity te)
    {
        return te != null && te.isRemoved() == false;
    }

    @Override
    public RenderShape getRenderShape(BlockState p_49232_)
    {
        return RenderShape.MODEL;
    }

    @Override
    @Deprecated
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock())
        {
            BlockEntity te = world.getBlockEntity(pos);

            if (te instanceof BlockEntityCrafting)
            {
                ((BlockEntityCrafting) te).dropInventories();
                world.updateNeighbourForOutputSignal(pos, this);
            }

            world.removeBlockEntity(pos);
        }
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        BlockEntity te = world.getBlockEntity(pos);

        if (te instanceof BlockEntityCrafting tec)
        {
            CompoundTag nbt = stack.getTag();

            // If the ItemStack has a tag containing saved TE data, restore it to the just placed block/TE
            if (nbt != null && nbt.contains("BlockEntityTag", Tag.TAG_COMPOUND))
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
    @Deprecated
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) // onBlockActivated
    {
        BlockEntity te = world.getBlockEntity(pos);

        if (te instanceof BlockEntityCrafting && this.isTileEntityValid(te))
        {
            if (world.isClientSide == false && player instanceof ServerPlayer)
            {
                NetworkHooks.openScreen((ServerPlayer) player, (BlockEntityCrafting) te, pos);
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    @Deprecated
    public boolean hasAnalogOutputSignal(BlockState state)
    {
        return true;
    }

    @Override
    @Deprecated
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos)
    {
        BlockEntity te = world.getBlockEntity(pos);

        if (te != null && this.isTileEntityValid(te))
        {
            LazyOptional<IItemHandler> optional = te.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.NORTH);

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