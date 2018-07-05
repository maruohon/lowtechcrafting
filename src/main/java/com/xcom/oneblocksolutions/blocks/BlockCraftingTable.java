package com.xcom.oneblocksolutions.blocks;

import com.xcom.oneblocksolutions.OneBlockSolutions;
import com.xcom.oneblocksolutions.tileentity.TileEntityCrafting;
import com.xcom.oneblocksolutions.util.BlockUtil;
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
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class BlockCraftingTable extends Block
{
    public static final String UNLOCALIZED_NAME = "craftingtable";
    public static final String REGISTRY_NAME = "craftingtable";

    public BlockCraftingTable()
    {
        super(Material.WOOD);

        this.setUnlocalizedName(UNLOCALIZED_NAME);
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
                player.openGui(OneBlockSolutions.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
            }

            return true;
        }

        return false;
    }
}