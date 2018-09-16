package fi.dy.masa.lowtechcrafting.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import fi.dy.masa.lowtechcrafting.network.message.MessageSyncSlot;
import fi.dy.masa.lowtechcrafting.reference.Reference;

public class PacketHandler
{
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID.toLowerCase());

    public static void init()
    {
        INSTANCE.registerMessage(MessageSyncSlot.Handler.class, MessageSyncSlot.class, 0, Side.CLIENT);
    }
}