package fi.dy.masa.lowtechcrafting.blocks;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.lowtechcrafting.LowTechCrafting;
import fi.dy.masa.lowtechcrafting.reference.Names;
import fi.dy.masa.lowtechcrafting.tileentity.TileEntityCrafting;
import fi.dy.masa.lowtechcrafting.util.BlockUtil;

public class BlockCraftingTable extends Block
{
    public BlockCraftingTable()
    {
        super(Material.WOOD);

        this.setTranslationKey(Names.CRAFTING_TABLE);
        this.setHardness(2.5f);
        this.setCreativeTab(CreativeTabs.REDSTONE);
        this.setSoundType(SoundType.WOOD);
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TileEntityCrafting();
    }

    public boolean isTileEntityValid(TileEntity te)
    {
        return te != null && te.isInvalid() == false;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        TileEntityCrafting te = BlockUtil.getTileEntitySafely(world, pos, TileEntityCrafting.class);

        if (te != null)
        {
            te.dropInventories();
            world.updateComparatorOutputLevel(pos, this);
        }

        world.removeTileEntity(pos);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        TileEntityCrafting te = BlockUtil.getTileEntitySafely(world, pos, TileEntityCrafting.class);

        if (te != null)
        {
            NBTTagCompound nbt = stack.getTagCompound();

            // If the ItemStack has a tag containing saved TE data, restore it to the just placed block/TE
            if (nbt != null && nbt.hasKey("BlockEntityTag", Constants.NBT.TAG_COMPOUND))
            {
                te.readFromNBTCustom(nbt.getCompoundTag("BlockEntityTag"));
            }
            else
            {
                if (te instanceof TileEntityCrafting && stack.hasDisplayName())
                {
                    ((TileEntityCrafting) te).setInventoryName(stack.getDisplayName());
                }
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntityCrafting te = BlockUtil.getTileEntitySafely(world, pos, TileEntityCrafting.class);

        if (te != null && this.isTileEntityValid(te))
        {
            if (world.isRemote == false)
            {
                player.openGui(LowTechCrafting.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state)
    {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos)
    {
        TileEntityCrafting te = BlockUtil.getTileEntitySafely(world, pos, TileEntityCrafting.class);

        if (te != null && this.isTileEntityValid(te))
        {
            IItemHandler inv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH);
            return calcRedstoneFromInventory(inv);
        }

        return 0;
    }

    public static int calcRedstoneFromInventory(@Nullable IItemHandler inv)
    {
        if (inv == null)
        {
            return 0;
        }
        else
        {
            final int numSlots = inv.getSlots();
            int nonEmptyStacks = 0;

            for (int slot = 0; slot < numSlots; ++slot)
            {
                ItemStack stack = inv.getStackInSlot(slot);

                if (stack.isEmpty() == false)
                {
                    ++nonEmptyStacks;
                }
            }

            float slotsWithItemsFraction = (float) nonEmptyStacks / (float) numSlots;

            return MathHelper.floor(slotsWithItemsFraction * 14.0F) + (nonEmptyStacks > 0 ? 1 : 0);
        }
    }
}