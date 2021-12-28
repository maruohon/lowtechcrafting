package fi.dy.masa.lowtechcrafting.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class NBTUtils
{
    /**
     * Sets the root compound tag in the given ItemStack. An empty compound will be stripped completely.
     */
    @Nonnull
    public static ItemStack setRootCompoundTag(@Nonnull ItemStack stack, @Nullable CompoundTag nbt)
    {
        if (nbt != null && nbt.isEmpty())
        {
            nbt = null;
        }

        stack.setTag(nbt);
        return stack;
    }

    /**
     * Get the root compound tag from the ItemStack.
     * If one doesn't exist, then it will be created and added if <b>create</b> is true, otherwise null is returned.
     */
    @Nullable
    public static CompoundTag getRootCompoundTag(@Nonnull ItemStack stack, boolean create)
    {
        CompoundTag nbt = stack.getTag();

        if (create == false)
        {
            return nbt;
        }

        // create = true
        if (nbt == null)
        {
            nbt = new CompoundTag();
            stack.setTag(nbt);
        }

        return nbt;
    }

    /**
     * Get a compound tag by the given name <b>tagName</b> from the other compound tag <b>nbt</b>.
     * If one doesn't exist, then it will be created and added if <b>create</b> is true, otherwise null is returned.
     */
    @Nullable
    public static CompoundTag getCompoundTag(@Nullable CompoundTag nbt, @Nonnull String tagName, boolean create)
    {
        if (nbt == null)
        {
            return null;
        }

        if (create == false)
        {
            return nbt.contains(tagName, Tag.TAG_COMPOUND) ? nbt.getCompound(tagName) : null;
        }

        // create = true

        if (nbt.contains(tagName, Tag.TAG_COMPOUND) == false)
        {
            nbt.put(tagName, new CompoundTag());
        }

        return nbt.getCompound(tagName);
    }

    /**
     * Returns a compound tag by the given name <b>tagName</b>. If <b>tagName</b> is null,
     * then the root compound tag is returned instead. If <b>create</b> is <b>false</b>
     * and the tag doesn't exist, null is returned and the tag is not created.
     * If <b>create</b> is <b>true</b>, then the tag(s) are created and added if necessary.
     */
    @Nullable
    public static CompoundTag getCompoundTag(@Nonnull ItemStack stack, @Nullable String tagName, boolean create)
    {
        CompoundTag nbt = getRootCompoundTag(stack, create);

        if (tagName != null)
        {
            nbt = getCompoundTag(nbt, tagName, create);
        }

        return nbt;
    }

    /**
     * Return the byte value from a tag <b>tagName</b>, or 0 if it doesn't exist.
     * If <b>containerTagName</b> is not null, then the value is retrieved from inside a compound tag by that name.
     */
    public static byte getByte(@Nonnull ItemStack stack, @Nullable String containerTagName, @Nonnull String tagName)
    {
        CompoundTag nbt = getCompoundTag(stack, containerTagName, false);
        return nbt != null ? nbt.getByte(tagName) : 0;
    }

    /**
     * Set a byte value in the given ItemStack's NBT in a tag <b>tagName</b>. If <b>containerTagName</b>
     * is not null, then the value is stored inside a compound tag by that name.
     */
    public static void setByte(@Nonnull ItemStack stack, @Nullable String containerTagName, @Nonnull String tagName, byte value)
    {
        CompoundTag nbt = getCompoundTag(stack, containerTagName, true);
        nbt.putByte(tagName, value);
    }

    /**
     * Cycle a byte value in the given NBT. If <b>containerTagName</b>
     * is not null, then the value is stored inside a compound tag by that name.
     */
    public static void cycleByteValue(@Nonnull CompoundTag nbt, @Nonnull String tagName, int minValue, int maxValue, boolean reverse)
    {
        byte value = nbt.getByte(tagName);

        if (reverse)
        {
            if (--value < minValue)
            {
                value = (byte) maxValue;
            }
        }
        else
        {
            if (++value > maxValue)
            {
                value = (byte) minValue;
            }
        }

        nbt.putByte(tagName, value);
    }

    /**
     * Cycle a byte value in the given ItemStack's NBT in a tag <b>tagName</b>. If <b>containerTagName</b>
     * is not null, then the value is stored inside a compound tag by that name.
     * The low end of the range is 0.
     */
    public static void cycleByteValue(@Nonnull ItemStack stack, @Nullable String containerTagName,
                                      @Nonnull String tagName, int maxValue, boolean reverse)
    {
        CompoundTag nbt = getCompoundTag(stack, containerTagName, true);
        cycleByteValue(nbt, tagName, 0, maxValue, reverse);
    }

    /**
     * Reads a byte array from NBT into the provided int array.
     * The number of elements read is the minimum of the provided array's length and
     * the read byte array's length.
     * @param arr
     * @param nbt
     * @param tagName
     */
    public static void readByteArrayIntoIntArray(int[] arr, CompoundTag nbt, String tagName)
    {
        byte[] arrBytes = nbt.getByteArray(tagName);
        final int len = Math.min(arr.length, arrBytes.length);

        for (int i = 0; i < len; i++)
        {
            arr[i] = arrBytes[i];
        }
    }

    /**
     * Writes the provided int array into NBT as a byte array, by casting each element into a byte.
     * @param arr
     * @param nbt
     * @param tagName
     */
    public static void writeIntArrayAsByteArray(int[] arr, CompoundTag nbt, String tagName)
    {
        byte[] bytes = new byte[arr.length];

        for (int i = 0; i < arr.length; i++)
        {
            bytes[i] = (byte) arr[i];
        }

        nbt.putByteArray(tagName, bytes);
    }

    /**
     * Reads a byte array from NBT into the provided byte array.
     * The number of elements read is the minimum of the provided array's length and
     * the read byte array's length.
     * @param arr
     * @param nbt
     * @param tagName
     */
    public static void readByteArray(byte[] arr, CompoundTag nbt, String tagName)
    {
        byte[] arrNbt = nbt.getByteArray(tagName);
        final int len = Math.min(arr.length, arrNbt.length);
        System.arraycopy(arrNbt, 0, arr, 0, len);
    }

    /**
     * Reads an int array from NBT into the provided int array.
     * The number of elements read is the minimum of the provided array's length and
     * the read byte array's length.
     * @param arr
     * @param nbt
     * @param tagName
     */
    public static void readIntArray(int[] arr, CompoundTag nbt, String tagName)
    {
        int[] arrNbt = nbt.getIntArray(tagName);
        final int len = Math.min(arr.length, arrNbt.length);
        System.arraycopy(arrNbt, 0, arr, 0, len);
    }

    /**
     * Reads an ItemStack from the given compound tag, including the Ender Utilities-specific custom stackSize.
     * @param tag
     * @return
     */
    @Nonnull
    public static ItemStack loadItemStackFromTag(@Nonnull CompoundTag tag)
    {
        ItemStack stack = ItemStack.of(tag);

        if (tag.contains("ActualCount", Tag.TAG_INT))
        {
            stack.setCount(tag.getInt("ActualCount"));
        }

        return stack.isEmpty() ? ItemStack.EMPTY : stack;
    }

    @Nonnull
    public static CompoundTag storeItemStackInTag(@Nonnull ItemStack stack, @Nonnull CompoundTag tag)
    {
        if (stack.isEmpty() == false)
        {
            stack.save(tag);

            if (stack.getCount() > 127)
            {
                // Prevent overflow and negative stack sizes
                tag.putByte("Count", (byte) (stack.getCount() & 0x7F));
                tag.putInt("ActualCount", stack.getCount());
            }
        }

        return tag;
    }

    /**
     * Reads the stored items from the provided NBTTagCompound, from a NBTTagList by the name <b>tagName</b>
     * and writes them to the provided list of ItemStacks <b>items</b>.<br>
     * <b>NOTE:</b> The list should be initialized to be large enough for all the stacks to be read!
     * @param items
     * @param tagName
     */
    public static void readStoredItemsFromTag(@Nonnull CompoundTag nbt, NonNullList<ItemStack> items, @Nonnull String tagName)
    {
        if (nbt.contains(tagName, Tag.TAG_LIST) == false)
        {
            return;
        }

        ListTag nbtTagList = nbt.getList(tagName, Tag.TAG_COMPOUND);
        int num = nbtTagList.size();
        int listSize = items.size();

        for (int i = 0; i < num; ++i)
        {
            CompoundTag tag = nbtTagList.getCompound(i);
            int slotNum = tag.getShort("Slot");

            if (slotNum >= 0 && slotNum < listSize)
            {
                items.set(slotNum, loadItemStackFromTag(tag));
            }
            /*else
            {
                EnderUtilities.logger.warn("Failed to read items from NBT, invalid slot: " + slotNum + " (max: " + (items.length - 1) + ")");
            }*/
        }
    }

    /**
     * Reads the stored items from the provided NBTTagCompound, from a NBTTagList by the name <b>tagName</b>
     * and writes them to a new empty list of ItemStacks.<br>
     * @param tagName
     * @return the list of ItemStack read. Can be an empty list.
     */
    @Nonnull
    public static NonNullList<ItemStack> readStoredItemsFromTag(@Nonnull CompoundTag nbt, @Nonnull String tagName)
    {
        NonNullList<ItemStack> items = NonNullList.create();

        if (nbt.contains(tagName, Tag.TAG_LIST) == false)
        {
            return items;
        }

        ListTag nbtTagList = nbt.getList(tagName, Tag.TAG_COMPOUND);
        final int size = nbtTagList.size();

        for (int i = 0; i < size; ++i)
        {
            CompoundTag tag = nbtTagList.getCompound(i);
            items.add(loadItemStackFromTag(tag));
        }

        return items;
    }

    /**
     * Writes the ItemStacks in <b>items</b> to a new NBTTagList and returns that list.
     * @param items
     */
    @Nonnull
    public static ListTag createTagListForItems(NonNullList<ItemStack> items)
    {
        ListTag nbtTagList = new ListTag();
        final int invSlots = items.size();

        // Write all the ItemStacks into a TAG_List
        for (int slotNum = 0; slotNum < invSlots; slotNum++)
        {
            ItemStack stack = items.get(slotNum);

            if (stack.isEmpty() == false)
            {
                CompoundTag tag = storeItemStackInTag(stack, new CompoundTag());

                if (invSlots <= 127)
                {
                    tag.putByte("Slot", (byte) slotNum);
                }
                else
                {
                    tag.putShort("Slot", (short) slotNum);
                }

                nbtTagList.add(tag);
            }
        }

        return nbtTagList;
    }

    /**
     * Writes the ItemStacks in <b>items</b> to the NBTTagCompound <b>nbt</b>
     * in a NBTTagList by the name <b>tagName</b>.
     * @param nbt
     * @param items
     * @param tagName the NBTTagList tag name where the items will be written to
     * @param keepExtraSlots set to true to append existing items in slots that are outside of the currently written slot range
     */
    @Nonnull
    public static CompoundTag writeItemsToTag(@Nonnull CompoundTag nbt, NonNullList<ItemStack> items,
                                                 @Nonnull String tagName, boolean keepExtraSlots)
    {
        int invSlots = items.size();
        ListTag nbtTagList = createTagListForItems(items);

        if (keepExtraSlots && nbt.contains(tagName, Tag.TAG_LIST))
        {
            // Read the old items and append any existing items that are outside the current written slot range
            ListTag nbtTagListExisting = nbt.getList(tagName, Tag.TAG_COMPOUND);
            final int count = nbtTagListExisting.size();

            for (int i = 0; i < count; i++)
            {
                CompoundTag tag = nbtTagListExisting.getCompound(i);
                int slotNum = tag.getShort("Slot");

                if (slotNum >= invSlots)
                {
                    nbtTagList.add(tag);
                }
            }
        }

        // Write the items to the compound tag
        if (nbtTagList.size() > 0)
        {
            nbt.put(tagName, nbtTagList);
        }
        else
        {
            nbt.remove(tagName);
        }

        return nbt;
    }
}