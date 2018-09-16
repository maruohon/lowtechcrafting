package fi.dy.masa.lowtechcrafting.init;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import fi.dy.masa.lowtechcrafting.blocks.BlockCraftingTable;
import fi.dy.masa.lowtechcrafting.reference.Names;
import fi.dy.masa.lowtechcrafting.reference.Reference;
import fi.dy.masa.lowtechcrafting.tileentity.TileEntityCrafting;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class Blocks
{
    public static final Block CRAFTING_TABLE = new BlockCraftingTable();

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().register(CRAFTING_TABLE.setRegistryName(Reference.MOD_ID, Names.CRAFTING_TABLE));
        GameRegistry.registerTileEntity(TileEntityCrafting.class, CRAFTING_TABLE.getRegistryName());
    }

    @SubscribeEvent
    public static void registerItemBlocks(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(new ItemBlock(CRAFTING_TABLE).setRegistryName(CRAFTING_TABLE.getRegistryName()));
    }
}
