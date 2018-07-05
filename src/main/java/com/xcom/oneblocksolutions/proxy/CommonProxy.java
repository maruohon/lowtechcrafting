package com.xcom.oneblocksolutions.proxy;

import com.xcom.oneblocksolutions.OneBlockSolutions;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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
                OneBlockSolutions.logger.warn("Invalid side in getPlayerFromMessageContext(): " + ctx.side);
                return null;
        }
    }

}
