package com.xcom.oneblocksolutions.gui;

import com.xcom.oneblocksolutions.inventory.container.ContainerCrafting;
import com.xcom.oneblocksolutions.tileentity.TileEntityCrafting;
import com.xcom.oneblocksolutions.util.BlockUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class OneBlockSolutionsGuiHandler implements IGuiHandler
{
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        if (player == null || world == null)
        {
            return null;
        }

        switch (id)
        {
            case 0:
                TileEntityCrafting te = BlockUtil.getTileEntitySafely(world, new BlockPos(x, y, z), TileEntityCrafting.class);

                if (te != null)
                {
                    return new ContainerCrafting(player, te);
                }

                break;

            default:
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        if (player == null || world == null)
        {
            return null;
        }

        switch (id)
        {
            case 0:
                TileEntityCrafting te = BlockUtil.getTileEntitySafely(world, new BlockPos(x, y, z), TileEntityCrafting.class);

                if (te != null)
                {
                    return new GUICraftingTable(new ContainerCrafting(player, te), te);
                }

                break;

            default:
        }

        return null;
    }

}