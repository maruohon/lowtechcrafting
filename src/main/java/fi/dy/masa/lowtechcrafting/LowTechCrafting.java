package fi.dy.masa.lowtechcrafting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import fi.dy.masa.lowtechcrafting.blocks.BlockCraftingTable;
import fi.dy.masa.lowtechcrafting.gui.AutoCraftingTableScreen;
import fi.dy.masa.lowtechcrafting.inventory.container.ContainerCrafting;
import fi.dy.masa.lowtechcrafting.reference.Names;
import fi.dy.masa.lowtechcrafting.reference.Reference;
import fi.dy.masa.lowtechcrafting.tileentity.BlockEntityCrafting;

@Mod(Reference.MOD_ID)
public class LowTechCrafting
{
    public static final Logger logger = LogManager.getLogger(Reference.MOD_ID);

    // Create Deferred Registers to hold the Blocks, Items etc. which will all be registered under the "lowtechcrafting" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Reference.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Reference.MOD_ID);

    public static final RegistryObject<Block> BLOCK_CRAFTING_TABLE = BLOCKS.register(Names.CRAFTING_TABLE, BlockCraftingTable::new);
    public static final RegistryObject<BlockEntityType<?>> BLOCK_ENTITY_TYPE_CRAFTING_TABLE = BLOCK_ENTITY_TYPES.register(Names.CRAFTING_TABLE, () -> BlockEntityType.Builder.of(BlockEntityCrafting::new, Blocks.CRAFTING_TABLE).build(null));
    public static final RegistryObject<MenuType<ContainerCrafting>> MENU_TYPE_CRAFTING_TABLE = MENU_TYPES.register(Names.CRAFTING_TABLE, () -> createMenuType(ContainerCrafting::new));

    public LowTechCrafting()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(ClientInit::registerScreenFactories);

        ITEMS.register(Names.CRAFTING_TABLE, () -> new BlockItem(BLOCK_CRAFTING_TABLE.get(), new Item.Properties().tab(CreativeModeTab.TAB_REDSTONE)));

        // Register the Deferred Registers to the mod event bus so that the blocks, items etc. get registered
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
    }

    public static <T extends AbstractContainerMenu> MenuType<T> createMenuType(IContainerFactory<T> factory)
    {
        return new MenuType<>(factory);
    }

    public static class ClientInit
    {
        public static void registerScreenFactories(final FMLClientSetupEvent event)
        {
            MenuScreens.register(LowTechCrafting.MENU_TYPE_CRAFTING_TABLE.get(), AutoCraftingTableScreen::new);
        }
    }
}
