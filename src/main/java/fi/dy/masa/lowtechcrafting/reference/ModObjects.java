package fi.dy.masa.lowtechcrafting.reference;

import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import fi.dy.masa.lowtechcrafting.inventory.container.ContainerCrafting;
import fi.dy.masa.lowtechcrafting.tileentity.TileEntityCrafting;
import net.minecraftforge.registries.ObjectHolder;

public class ModObjects
{
    @ObjectHolder(Reference.MOD_ID + ":" + Names.CRAFTING_TABLE)
    public static final Block BLOCK_CRAFTING_TABLE = null;

    @ObjectHolder(Reference.MOD_ID + ":" + Names.CRAFTING_TABLE)
    public static final ContainerType<ContainerCrafting> CONTAINER_TYPE_CRAFTING_TABLE = null;

    @ObjectHolder(Reference.MOD_ID + ":" + Names.CRAFTING_TABLE)
    public static final Item ITEM_CRAFTING_TABLE = null;

    @ObjectHolder(Reference.MOD_ID + ":" + Names.CRAFTING_TABLE)
    public static final TileEntityType<TileEntityCrafting> TILE_TYPE_CRAFTING_TABLE = null;
}
