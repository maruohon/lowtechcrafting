package fi.dy.masa.lowtechcrafting.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import fi.dy.masa.lowtechcrafting.inventory.container.ContainerCrafting;
import fi.dy.masa.lowtechcrafting.tileentity.TileEntityCrafting;
import fi.dy.masa.lowtechcrafting.util.BlockUtil;

public class LowTechCraftingGuiHandler implements IGuiHandler
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