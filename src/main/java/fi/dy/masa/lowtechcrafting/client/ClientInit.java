package fi.dy.masa.lowtechcrafting.client;

import net.minecraft.client.gui.screens.MenuScreens;
import fi.dy.masa.lowtechcrafting.gui.AutoCraftingTableScreen;
import fi.dy.masa.lowtechcrafting.reference.ModObjects;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientInit
{
    public static void registerScreenFactories(final FMLClientSetupEvent event)
    {
        MenuScreens.register(ModObjects.CONTAINER_TYPE_CRAFTING_TABLE, AutoCraftingTableScreen::new);
    }
}
