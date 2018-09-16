package fi.dy.masa.lowtechcrafting.proxy;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import fi.dy.masa.lowtechcrafting.LowTechCrafting;
import fi.dy.masa.lowtechcrafting.init.Blocks;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    @Override
    public EntityPlayer getClientPlayer()
    {
        return FMLClientHandler.instance().getClientPlayerEntity();
    }

    @Override
    public EntityPlayer getPlayerFromMessageContext(MessageContext ctx)
    {
        switch (ctx.side)
        {
            case CLIENT:
                return FMLClientHandler.instance().getClientPlayerEntity();

            case SERVER:
                return ctx.getServerHandler().player;

            default:
                LowTechCrafting.logger.warn("Invalid side in getPlayerFromMessageContext(): " + ctx.side);
                return null;
        }
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
        registerItemBlockModels();
    }

    private static void registerItemBlockModels()
    {
        registerItemBlockModel(Blocks.CRAFTING_TABLE, 0, "normal");
    }

    private static void registerItemBlockModel(Block block, int meta, String fullVariant)
    {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), meta, new ModelResourceLocation(block.getRegistryName(), fullVariant));
    }
}