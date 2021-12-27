package fi.dy.masa.lowtechcrafting.util;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class EntityUtils
{
    public static void dropItemStacksInWorld(World worldIn, BlockPos pos, ItemStack stack, int amountOverride, boolean dropFullStacks)
    {
        dropItemStacksInWorld(worldIn, pos, stack, amountOverride, dropFullStacks, true);
    }

    public static void dropItemStacksInWorld(World worldIn, BlockPos pos, ItemStack stack, int amountOverride, boolean dropFullStacks, boolean randomMotion)
    {
        double x = worldIn.random.nextFloat() * -0.5d + 0.75d + pos.getX();
        double y = worldIn.random.nextFloat() * -0.5d + 0.75d + pos.getY();
        double z = worldIn.random.nextFloat() * -0.5d + 0.75d + pos.getZ();

        dropItemStacksInWorld(worldIn, new Vector3d(x, y, z), stack, amountOverride, dropFullStacks, randomMotion);
    }

    public static void dropItemStacksInWorld(World worldIn, Vector3d pos, ItemStack stack, int amountOverride, boolean dropFullStacks, boolean randomMotion)
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
                num = Math.min(worldIn.random.nextInt(23) + 10, max);
            }

            num = Math.min(num, amount);
            amount -= num;

            ItemStack dropStack = stack.copy();
            dropStack.setCount(num);

            ItemEntity item = new ItemEntity(worldIn, pos.x, pos.y, pos.z, dropStack);
            item.setDefaultPickUpDelay();

            Vector3d motion;

            if (randomMotion)
            {
                double motionScale = 0.04d;
                motion = new Vector3d( worldIn.random.nextGaussian() * motionScale,
                                    worldIn.random.nextGaussian() * motionScale + 0.3,
                                    worldIn.random.nextGaussian() * motionScale);
            }
            else
            {
                motion = new Vector3d(0, 0, 0);
            }

            item.setDeltaMovement(motion);
            worldIn.addFreshEntity(item);
        }
    }
}
