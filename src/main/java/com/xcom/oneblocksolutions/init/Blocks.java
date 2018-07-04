package com.xcom.oneblocksolutions.init;

import com.xcom.oneblocksolutions.Reference;
import com.xcom.oneblocksolutions.blocks.BlockCraftingTable;
import com.xcom.oneblocksolutions.tileentity.TileEntityCrafting;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = Reference.MODID)
public class Blocks {

    public static Block CRAFTING_TABLE = new BlockCraftingTable();

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(CRAFTING_TABLE.setRegistryName(Reference.MODID, BlockCraftingTable.REGISTRY_NAME));
        GameRegistry.registerTileEntity(TileEntityCrafting.class, CRAFTING_TABLE.getRegistryName());
    }

    @SubscribeEvent
    public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemBlock(CRAFTING_TABLE).setRegistryName(CRAFTING_TABLE.getRegistryName()));
    }
}
