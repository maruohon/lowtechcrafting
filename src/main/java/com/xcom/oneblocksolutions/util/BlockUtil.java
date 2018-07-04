package com.xcom.oneblocksolutions.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;

public class BlockUtil {

    @Nullable
    public static <T extends TileEntity> T getTileEntitySafely(IBlockAccess world, BlockPos pos, Class<T> tileClass) {
        TileEntity te;

        if (world instanceof ChunkCache) {
            ChunkCache chunkCache = (ChunkCache) world;
            te = chunkCache.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
        } else {
            te = world.getTileEntity(pos);
        }

        if (tileClass.isInstance(te)) {
            return tileClass.cast(te);
        } else {
            return null;
        }
    }
}
