package fi.dy.masa.lowtechcrafting.network;

import net.minecraft.util.ResourceLocation;
import fi.dy.masa.lowtechcrafting.network.message.MessageSyncSlot;
import fi.dy.masa.lowtechcrafting.reference.Reference;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler
{
    public static final String PROTOCOL = "0.2";

    public static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder
                                                    .named(new ResourceLocation(Reference.MOD_ID, "sync"))
                                                    .clientAcceptedVersions(PROTOCOL::equals)
                                                    .serverAcceptedVersions(PROTOCOL::equals)
                                                    .networkProtocolVersion(() -> PROTOCOL).simpleChannel();

    public static void registerMessages()
    {
        INSTANCE.registerMessage(0, MessageSyncSlot.class, MessageSyncSlot::toBytes, MessageSyncSlot::new, MessageSyncSlot::handle);
    }
}