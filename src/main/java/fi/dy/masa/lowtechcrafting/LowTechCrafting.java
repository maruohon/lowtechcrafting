package fi.dy.masa.lowtechcrafting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import fi.dy.masa.lowtechcrafting.blocks.BlockCraftingTable;
import fi.dy.masa.lowtechcrafting.client.ClientInit;
import fi.dy.masa.lowtechcrafting.inventory.container.ContainerCrafting;
import fi.dy.masa.lowtechcrafting.network.PacketHandler;
import fi.dy.masa.lowtechcrafting.reference.ModObjects;
import fi.dy.masa.lowtechcrafting.reference.Names;
import fi.dy.masa.lowtechcrafting.reference.Reference;
import fi.dy.masa.lowtechcrafting.tileentity.TileEntityCrafting;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.IContainerFactory;

@Mod(Reference.MOD_ID)
public class LowTechCrafting
{
    public static final Logger logger = LogManager.getLogger(Reference.MOD_ID);

    public LowTechCrafting()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientInit::registerScreenFactories);
    }

    public void onCommonSetup(final FMLCommonSetupEvent  event)
    {
        PacketHandler.registerMessages();
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
        public static void onRegisterContainerTypes(RegistryEvent.Register<ContainerType<?>> event)
        {
            String name = Reference.MOD_ID + ":" + Names.CRAFTING_TABLE;
            event.getRegistry().register(createContainerType(ContainerCrafting::new).setRegistryName(name));
        }

        @SubscribeEvent
        public static void onRegisterItems(RegistryEvent.Register<Item> event)
        {
            String name = Reference.MOD_ID + ":" + Names.CRAFTING_TABLE;
            Block block = ModObjects.BLOCK_CRAFTING_TABLE;
            event.getRegistry().register((new BlockItem(block, (new Item.Properties()).group(ItemGroup.REDSTONE))).setRegistryName(name));
        }

        @SubscribeEvent
        public static void onRegisterBlockEntityTypes(RegistryEvent.Register<TileEntityType<?>> event)
        {
            String name = Reference.MOD_ID + ":" + Names.CRAFTING_TABLE;
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityCrafting::new, Blocks.CRAFTING_TABLE).build(null).setRegistryName(name));
        }

        public static <T extends Container> ContainerType<T> createContainerType(IContainerFactory<T> factory)
        {
            return new ContainerType<>(factory);
        }
    }
}
