package fi.dy.masa.lowtechcrafting.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import fi.dy.masa.lowtechcrafting.LowTechCrafting;

public class CommonProxy
{
    public EntityPlayer getClientPlayer()
    {
        return null;
    }

    public EntityPlayer getPlayerFromMessageContext(MessageContext ctx)
    {
        switch (ctx.side)
        {
            case SERVER:
                return ctx.getServerHandler().player;
            default:
                LowTechCrafting.logger.warn("Invalid side in getPlayerFromMessageContext(): " + ctx.side);
                return null;
        }
    }

}
