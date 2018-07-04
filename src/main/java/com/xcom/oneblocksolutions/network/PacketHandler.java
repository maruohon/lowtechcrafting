package com.xcom.oneblocksolutions.network;

import com.xcom.oneblocksolutions.Reference;
import com.xcom.oneblocksolutions.network.message.MessageSyncSlot;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler
{
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MODID.toLowerCase());

    public static void init()
    {
        INSTANCE.registerMessage(MessageSyncSlot.Handler.class,                 MessageSyncSlot.class,                 0, Side.CLIENT);
    }
}