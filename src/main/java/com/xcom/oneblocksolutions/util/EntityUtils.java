package com.xcom.oneblocksolutions.util;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityUtils
{
    public static void dropItemStacksInWorld(World worldIn, BlockPos pos, ItemStack stack, int amountOverride, boolean dropFullStacks)
    {
        dropItemStacksInWorld(worldIn, pos, stack, amountOverride, dropFullStacks, true);
    }

    public static void dropItemStacksInWorld(World worldIn, BlockPos pos, ItemStack stack, int amountOverride, boolean dropFullStacks, boolean randomMotion)
    {
        double x = worldIn.rand.nextFloat() * -0.5d + 0.75d + pos.getX();
        double y = worldIn.rand.nextFloat() * -0.5d + 0.75d + pos.getY();
        double z = worldIn.rand.nextFloat() * -0.5d + 0.75d + pos.getZ();

        dropItemStacksInWorld(worldIn, new Vec3d(x, y, z), stack, amountOverride, dropFullStacks, randomMotion);
    }

    public static void dropItemStacksInWorld(World worldIn, Vec3d pos, ItemStack stack, int amountOverride, boolean dropFullStacks, boolean randomMotion)
    {
        int amount = stack.getCount();
        int max = stack.getMaxStackSize();
        int num = max;

        if (amountOverride > 0)
        {
            amount = amountOverride;
        }

        while (amount > 0)
        {
            if (dropFullStacks == false)
            {
                num = Math.min(worldIn.rand.nextInt(23) + 10, max);
            }

            num = Math.min(num, amount);
            amount -= num;

            ItemStack dropStack = stack.copy();
            dropStack.setCount(num);

            EntityItem entityItem = new EntityItem(worldIn, pos.x, pos.y, pos.z, dropStack);

            if (randomMotion)
            {
                double motionScale = 0.04d;
                entityItem.motionX = worldIn.rand.nextGaussian() * motionScale;
                entityItem.motionY = worldIn.rand.nextGaussian() * motionScale + 0.3d;
                entityItem.motionZ = worldIn.rand.nextGaussian() * motionScale;
            }
            else
            {
                entityItem.motionX = 0d;
                entityItem.motionY = 0d;
                entityItem.motionZ = 0d;
            }

            worldIn.spawnEntity(entityItem);
        }
    }
}
