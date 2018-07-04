package com.xcom.oneblocksolutions.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NBTUtils
{
    /**
     * Sets the root compound tag in the given ItemStack. An empty compound will be stripped completely.
     */
    @Nonnull
    public static ItemStack setRootCompoundTag(@Nonnull ItemStack stack, @Nullable NBTTagCompound nbt)
    {
        if (nbt != null && nbt.hasNoTags())
        {
            nbt = null;
        }

        stack.setTagCompound(nbt);
        return stack;
    }

    /**
     * Get the root compound tag from the ItemStack.
     * If one doesn't exist, then it will be created and added if <b>create</b> is true, otherwise null is returned.
     */
    @Nullable
    public static NBTTagCompound getRootCompoundTag(@Nonnull ItemStack stack, boolean create)
    {
        NBTTagCompound nbt = stack.getTagCompound();

        if (create == false)
        {
            return nbt;
        }

        // create = true
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
        }

        return nbt;
    }

    /**
     * Get a compound tag by the given name <b>tagName</b> from the other compound tag <b>nbt</b>.
     * If one doesn't exist, then it will be created and added if <b>create</b> is true, otherwise null is returned.
     */
    @Nullable
    public static NBTTagCompound getCompoundTag(@Nullable NBTTagCompound nbt, @Nonnull String tagName, boolean create)
    {
        if (nbt == null)
        {
            return null;
        }

        if (create == false)
        {
            return nbt.hasKey(tagName, Constants.NBT.TAG_COMPOUND) ? nbt.getCompoundTag(tagName) : null;
        }

        // create = true

        if (nbt.hasKey(tagName, Constants.NBT.TAG_COMPOUND) == false)
        {
            nbt.setTag(tagName, new NBTTagCompound());
        }

        return nbt.getCompoundTag(tagName);
    }

    /**
     * Returns a compound tag by the given name <b>tagName</b>. If <b>tagName</b> is null,
     * then the root compound tag is returned instead. If <b>create</b> is <b>false</b>
     * and the tag doesn't exist, null is returned and the tag is not created.
     * If <b>create</b> is <b>true</b>, then the tag(s) are created and added if necessary.
     */
    @Nullable
    public static NBTTagCompound getCompoundTag(@Nonnull ItemStack stack, @Nullable String tagName, boolean create)
    {
        NBTTagCompound nbt = getRootCompoundTag(stack, create);

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
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, false);
        return nbt != null ? nbt.getByte(tagName) : 0;
    }

    /**
     * Set a byte value in the given ItemStack's NBT in a tag <b>tagName</b>. If <b>containerTagName</b>
     * is not null, then the value is stored inside a compound tag by that name.
     */
    public static void setByte(@Nonnull ItemStack stack, @Nullable String containerTagName, @Nonnull String tagName, byte value)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, true);
        nbt.setByte(tagName, value);
    }

    /**
     * Cycle a byte value in the given NBT. If <b>containerTagName</b>
     * is not null, then the value is stored inside a compound tag by that name.
     */
    public static void cycleByteValue(@Nonnull NBTTagCompound nbt, @Nonnull String tagName, int minValue, int maxValue, boolean reverse)
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

        nbt.setByte(tagName, value);
    }

    /**
     * Cycle a byte value in the given ItemStack's NBT in a tag <b>tagName</b>. If <b>containerTagName</b>
     * is not null, then the value is stored inside a compound tag by that name.
     * The low end of the range is 0.
     */
    public static void cycleByteValue(@Nonnull ItemStack stack, @Nullable String containerTagName,
                                      @Nonnull String tagName, int maxValue, boolean reverse)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, true);
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
    public static void readByteArrayIntoIntArray(int[] arr, NBTTagCompound nbt, String tagName)
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
    public static void writeIntArrayAsByteArray(int[] arr, NBTTagCompound nbt, String tagName)
    {
        byte[] bytes = new byte[arr.length];

        for (int i = 0; i < arr.length; i++)
        {
            bytes[i] = (byte) arr[i];
        }

        nbt.setByteArray(tagName, bytes);
    }

    /**
     * Reads a byte array from NBT into the provided byte array.
     * The number of elements read is the minimum of the provided array's length and
     * the read byte array's length.
     * @param arr
     * @param nbt
     * @param tagName
     */
    public static void readByteArray(byte[] arr, NBTTagCompound nbt, String tagName)
    {
        byte[] arrNbt = nbt.getByteArray(tagName);
        final int len = Math.min(arr.length, arrNbt.length);

        for (int i = 0; i < len; i++)
        {
            arr[i] = arrNbt[i];
        }
    }

    /**
     * Reads an int array from NBT into the provided int array.
     * The number of elements read is the minimum of the provided array's length and
     * the read byte array's length.
     * @param arr
     * @param nbt
     * @param tagName
     */
    public static void readIntArray(int[] arr, NBTTagCompound nbt, String tagName)
    {
        int[] arrNbt = nbt.getIntArray(tagName);
        final int len = Math.min(arr.length, arrNbt.length);

        for (int i = 0; i < len; i++)
        {
            arr[i] = arrNbt[i];
        }
    }

    /**
     * Reads an ItemStack from the given compound tag, including the Ender Utilities-specific custom stackSize.
     * @param tag
     * @return
     */
    @Nonnull
    public static ItemStack loadItemStackFromTag(@Nonnull NBTTagCompound tag)
    {
        ItemStack stack = new ItemStack(tag);

        if (tag.hasKey("ActualCount", Constants.NBT.TAG_INT))
        {
            stack.setCount(tag.getInteger("ActualCount"));
        }

        return stack.isEmpty() ? ItemStack.EMPTY : stack;
    }

    @Nonnull
    public static NBTTagCompound storeItemStackInTag(@Nonnull ItemStack stack, @Nonnull NBTTagCompound tag)
    {
        if (stack.isEmpty() == false)
        {
            stack.writeToNBT(tag);

            if (stack.getCount() > 127)
            {
                // Prevent overflow and negative stack sizes
                tag.setByte("Count", (byte) (stack.getCount() & 0x7F));
                tag.setInteger("ActualCount", stack.getCount());
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
    public static void readStoredItemsFromTag(@Nonnull NBTTagCompound nbt, NonNullList<ItemStack> items, @Nonnull String tagName)
    {
        if (nbt.hasKey(tagName, Constants.NBT.TAG_LIST) == false)
        {
            return;
        }

        NBTTagList nbtTagList = nbt.getTagList(tagName, Constants.NBT.TAG_COMPOUND);
        int num = nbtTagList.tagCount();
        int listSize = items.size();

        for (int i = 0; i < num; ++i)
        {
            NBTTagCompound tag = nbtTagList.getCompoundTagAt(i);
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
    public static NonNullList<ItemStack> readStoredItemsFromTag(@Nonnull NBTTagCompound nbt, @Nonnull String tagName)
    {
        NonNullList<ItemStack> items = NonNullList.create();

        if (nbt.hasKey(tagName, Constants.NBT.TAG_LIST) == false)
        {
            return items;
        }

        NBTTagList nbtTagList = nbt.getTagList(tagName, Constants.NBT.TAG_COMPOUND);
        final int size = nbtTagList.tagCount();

        for (int i = 0; i < size; ++i)
        {
            NBTTagCompound tag = nbtTagList.getCompoundTagAt(i);
            items.add(loadItemStackFromTag(tag));
        }

        return items;
    }

    /**
     * Writes the ItemStacks in <b>items</b> to a new NBTTagList and returns that list.
     * @param items
     */
    @Nonnull
    public static NBTTagList createTagListForItems(NonNullList<ItemStack> items)
    {
        NBTTagList nbtTagList = new NBTTagList();
        final int invSlots = items.size();

        // Write all the ItemStacks into a TAG_List
        for (int slotNum = 0; slotNum < invSlots; slotNum++)
        {
            ItemStack stack = items.get(slotNum);

            if (stack.isEmpty() == false)
            {
                NBTTagCompound tag = storeItemStackInTag(stack, new NBTTagCompound());

                if (invSlots <= 127)
                {
                    tag.setByte("Slot", (byte) slotNum);
                }
                else
                {
                    tag.setShort("Slot", (short) slotNum);
                }

                nbtTagList.appendTag(tag);
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
    public static NBTTagCompound writeItemsToTag(@Nonnull NBTTagCompound nbt, NonNullList<ItemStack> items,
                                                 @Nonnull String tagName, boolean keepExtraSlots)
    {
        int invSlots = items.size();
        NBTTagList nbtTagList = createTagListForItems(items);

        if (keepExtraSlots && nbt.hasKey(tagName, Constants.NBT.TAG_LIST))
        {
            // Read the old items and append any existing items that are outside the current written slot range
            NBTTagList nbtTagListExisting = nbt.getTagList(tagName, Constants.NBT.TAG_COMPOUND);
            final int count = nbtTagListExisting.tagCount();

            for (int i = 0; i < count; i++)
            {
                NBTTagCompound tag = nbtTagListExisting.getCompoundTagAt(i);
                int slotNum = tag.getShort("Slot");

                if (slotNum >= invSlots)
                {
                    nbtTagList.appendTag(tag);
                }
            }
        }

        // Write the items to the compound tag
        if (nbtTagList.tagCount() > 0)
        {
            nbt.setTag(tagName, nbtTagList);
        }
        else
        {
            nbt.removeTag(tagName);
        }

        return nbt;
    }
}