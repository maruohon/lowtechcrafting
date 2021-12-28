package fi.dy.masa.lowtechcrafting.reference;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import fi.dy.masa.lowtechcrafting.inventory.container.ContainerCrafting;
import fi.dy.masa.lowtechcrafting.tileentity.BlockEntityCrafting;
import net.minecraftforge.registries.ObjectHolder;

public class ModObjects
{
    @ObjectHolder(Reference.MOD_ID + ":" + Names.CRAFTING_TABLE)
    public static final Block BLOCK_CRAFTING_TABLE = null;

    @ObjectHolder(Reference.MOD_ID + ":" + Names.CRAFTING_TABLE)
    public static final MenuType<ContainerCrafting> CONTAINER_TYPE_CRAFTING_TABLE = null;

    @ObjectHolder(Reference.MOD_ID + ":" + Names.CRAFTING_TABLE)
    public static final Item ITEM_CRAFTING_TABLE = null;

    @ObjectHolder(Reference.MOD_ID + ":" + Names.CRAFTING_TABLE)
    public static final BlockEntityType<BlockEntityCrafting> TILE_TYPE_CRAFTING_TABLE = null;
}
