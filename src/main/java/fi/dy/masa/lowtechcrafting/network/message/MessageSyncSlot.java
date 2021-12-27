package fi.dy.masa.lowtechcrafting.network.message;

import java.io.IOException;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import fi.dy.masa.lowtechcrafting.LowTechCrafting;
import fi.dy.masa.lowtechcrafting.inventory.container.base.ContainerBase;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageSyncSlot
{
    private int windowId;
    private int slotNum;
    private ItemStack stack = ItemStack.EMPTY;

    public MessageSyncSlot(PacketBuffer buf)
    {
        try
        {
            this.windowId = buf.readVarInt();
            this.slotNum = buf.readShort();
            this.stack = ByteBufUtilsLTC.readItemStackFromBuffer(buf);
        }
        catch (IOException e)
        {
            LowTechCrafting.logger.warn("MessageSyncSlot: Exception while reading data from buffer", e);
        }
    }

    public MessageSyncSlot(int windowId, int slotNum, ItemStack stack)
    {
        this.windowId = windowId;
        this.slotNum = slotNum;
        this.stack = stack;
    }

    public void toBytes(PacketBuffer buf)
    {
        buf.writeVarInt(this.windowId);
        buf.writeShort(this.slotNum);
        ByteBufUtilsLTC.writeItemStackToBuffer(buf, this.stack);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier)
    {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            if (ctx.getDirection() != NetworkDirection.PLAY_TO_CLIENT)
            {
                LowTechCrafting.logger.error("Wrong side in MessageSyncSlot: " + ctx.getDirection());
                return;
            }

            Minecraft mc = Minecraft.getInstance();

            if (mc == null || mc.player == null)
            {
                LowTechCrafting.logger.error("Minecraft or player was null in MessageSyncSlot");
                return;
            }

            if (mc.player.containerMenu instanceof ContainerBase && this.windowId == mc.player.containerMenu.containerId)
            {
                //System.out.printf("MessageSyncSlot - slot: %3d stack: %s\n", message.slotNum, message.stack);
                ((ContainerBase) mc.player.containerMenu).syncStackInSlot(this.slotNum, this.stack);
            }

            ctx.setPacketHandled(true);
        });
    }
}
