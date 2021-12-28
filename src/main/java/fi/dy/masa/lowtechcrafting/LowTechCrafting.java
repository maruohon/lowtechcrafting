package fi.dy.masa.lowtechcrafting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.IContainerFactory;
import fi.dy.masa.lowtechcrafting.blocks.BlockCraftingTable;
import fi.dy.masa.lowtechcrafting.client.ClientInit;
import fi.dy.masa.lowtechcrafting.inventory.container.ContainerCrafting;
import fi.dy.masa.lowtechcrafting.reference.ModObjects;
import fi.dy.masa.lowtechcrafting.reference.Names;
import fi.dy.masa.lowtechcrafting.reference.Reference;
import fi.dy.masa.lowtechcrafting.tileentity.BlockEntityCrafting;

@Mod(Reference.MOD_ID)
public class LowTechCrafting
{
    public static final Logger logger = LogManager.getLogger(Reference.MOD_ID);

    public LowTechCrafting()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientInit::registerScreenFactories);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEventHandler
    {
        @SubscribeEvent
        public static void onRegisterBlocks(RegistryEvent.Register<Block> event)
        {
            String name = Reference.MOD_ID + ":" + Names.CRAFTING_TABLE;
            event.getRegistry().register((new BlockCraftingTable()).setRegistryName(name));
        }

        @SubscribeEvent
        public static void onRegisterContainerTypes(RegistryEvent.Register<MenuType<?>> event)
        {
            String name = Reference.MOD_ID + ":" + Names.CRAFTING_TABLE;
            event.getRegistry().register(createContainerType(ContainerCrafting::new).setRegistryName(name));
        }

        @SubscribeEvent
        public static void onRegisterItems(RegistryEvent.Register<Item> event)
        {
            String name = Reference.MOD_ID + ":" + Names.CRAFTING_TABLE;
            Block block = ModObjects.BLOCK_CRAFTING_TABLE;
            event.getRegistry().register((new BlockItem(block, (new Item.Properties()).tab(CreativeModeTab.TAB_REDSTONE))).setRegistryName(name));
        }

        @SubscribeEvent
        public static void onRegisterBlockEntityTypes(RegistryEvent.Register<BlockEntityType<?>> event)
        {
            String name = Reference.MOD_ID + ":" + Names.CRAFTING_TABLE;
            event.getRegistry().register(BlockEntityType.Builder.of(BlockEntityCrafting::new, Blocks.CRAFTING_TABLE).build(null).setRegistryName(name));
        }

        public static <T extends AbstractContainerMenu> MenuType<T> createContainerType(IContainerFactory<T> factory)
        {
            return new MenuType<>(factory);
        }
    }
}
