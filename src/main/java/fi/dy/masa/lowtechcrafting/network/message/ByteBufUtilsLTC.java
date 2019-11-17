package fi.dy.masa.lowtechcrafting.network.message;

import java.io.IOException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import fi.dy.masa.lowtechcrafting.LowTechCrafting;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.EncoderException;

public class ByteBufUtilsLTC
{
    public static void writeItemStackToBuffer(ByteBuf buf, ItemStack stack)
    {
        if (stack.isEmpty())
        {
            buf.writeInt(-1);
            return;
        }

        buf.writeInt(Item.getIdFromItem(stack.getItem()));
        buf.writeInt(stack.getCount());

        CompoundNBT tag = stack.getItem().getShareTag(stack);
        writeNBTTagCompoundToBuffer(buf, tag);
    }

    public static ItemStack readItemStackFromBuffer(ByteBuf buf) throws IOException
    {
        ItemStack stack = ItemStack.EMPTY;
        int id = buf.readInt();

        if (id >= 0)
        {
            int stackSize = buf.readInt();
            stack = new ItemStack(Item.getItemById(id), stackSize);
            stack.setTag(readNBTTagCompoundFromBuffer(buf));
        }

        return stack;
    }

    public static void writeNBTTagCompoundToBuffer(ByteBuf buf, CompoundNBT tag)
    {
        if (tag == null)
        {
            buf.writeByte(0);
            return;
        }

        try
        {
            CompressedStreamTools.write(tag, new ByteBufOutputStream(buf));
        }
        catch (IOException ioexception)
        {
            LowTechCrafting.logger.error("IOException while trying to write a NBTTagCompound to ByteBuf");
            throw new EncoderException(ioexception);
        }
    }

    public static CompoundNBT readNBTTagCompoundFromBuffer(ByteBuf buf) throws IOException
    {
        int i = buf.readerIndex();
        byte b0 = buf.readByte();

        if (b0 == 0)
        {
            return null;
        }
        else
        {
            buf.readerIndex(i);
            return CompressedStreamTools.read(new ByteBufInputStream(buf), new NBTSizeTracker(2097152L));
        }
    }
}